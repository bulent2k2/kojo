/*
 * Copyright (C) 2026
 *   Bulent Basaran <ben@scala.org> https://github.com/bulent2k2
 *
 * The contents of this file are subject to the GNU General Public License
 * Version 3 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.gnu.org/copyleft/gpl.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 */
package net.kogics.kojo.lite

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL
import java.security.MessageDigest
import java.util.zip.ZipFile

import scala.io.Source

import net.kogics.kojo.util.Utils

/**
 * Fetches a keyword-patched Scala toolchain on demand, so that a Kojo package
 * does not have to carry ~20 MB of patched compiler that only the speakers of
 * one language ever load. Language-agnostic: `lang` is the toolchain code
 * (`tr`, `sv`, ...); the assets live in a `v<version>-<lang>` release.
 *
 * The jars land in ~/.kojo/lite/scala-<lang>/<version>/ - user-writable, so no
 * admin rights are needed and the download survives a reinstall. Nothing is
 * fetched unless a keyword toolchain is actually selected (by user language
 * or by -Dkojo.toolchain=<lang>), and a failure is never fatal: Kojo falls
 * back to the stock toolchain and says why.
 *
 * Every downloaded jar must clear three checks before it is used:
 *   - its SHA-256 matches the published SHA256SUMS
 *   - it opens as a zip (a truncated download fails here, not at runtime)
 *   - scala-library.jar reports the Scala version Kojo was built against,
 *     so a toolchain from a different Scala release can never be paired
 *     with Kojo's classes
 *
 * -Dkojo.toolchain.url=<base> overrides the download location, which is how
 * the tests drive the whole path from a local directory, offline.
 */
object ScalaToolchainFetcher {
  val jarNames = Seq("scala-library.jar", "scala-reflect.jar", "scala-compiler.jar", "scalariform.jar")
  val checksumFileName = "SHA256SUMS"

  private val defaultBaseUrl = "https://github.com/bulent2k2/scala-2/releases/download"

  // A dead or stalled connection must not hang the launcher: connect fast,
  // and give a stalled read a minute before giving up and falling back.
  private val ConnectTimeoutMs = 15 * 1000
  private val ReadTimeoutMs = 60 * 1000

  // Staged (.part) files carry a per-JVM tag, so two Kojo instances fetching
  // concurrently cannot clobber each other's half-written files; the rename
  // into the final name at the end is per-file last-writer-wins of verified,
  // identical content.
  private val stagingTag = java.lang.Long.toHexString(System.nanoTime())

  def baseUrlFor(version: String, lang: String): String =
    System.getProperty("kojo.toolchain.url", s"$defaultBaseUrl/v$version-$lang").stripSuffix("/")

  def cacheDir(version: String, lang: String): File =
    new File(s"${Utils.userDir}${File.separator}.kojo${File.separator}lite${File.separator}scala-$lang", version)

  /** True when every jar of the toolchain is present in `dir`. */
  def isComplete(dir: File): Boolean =
    dir.isDirectory && jarNames.forall(n => new File(dir, n).isFile)

  /**
   * Returns a directory holding the `lang` keyword toolchain for `version`,
   * fetching it first if it is not cached yet. None if it could not be made
   * available - the caller then stays on the stock toolchain.
   */
  def ensureAvailable(version: String, lang: String, progress: FetchProgress = ConsoleProgress): Option[File] = {
    val dir = cacheDir(version, lang)
    if (isComplete(dir)) Some(dir)
    else
      try fetchInto(dir, version, lang, progress)
      catch {
        case e: Throwable =>
          progress.message(s"[WARNING] Could not fetch the '$lang' Scala toolchain: ${e.getMessage}")
          None
      }
      finally {
        discardPartials(dir)
        progress.finished()
      }
  }

  /** Removes anything a failed fetch left half-written. */
  private def discardPartials(dir: File): Unit =
    Option(dir.listFiles).foreach(_.filter(_.getName.endsWith(".part")).foreach(_.delete()))

  private def fetchInto(dir: File, version: String, lang: String, progress: FetchProgress): Option[File] = {
    val base = baseUrlFor(version, lang)
    progress.message(s"[INFO] Fetching the '$lang' Scala toolchain $version from $base")
    dir.mkdirs()
    val expected = checksums(s"$base/$checksumFileName")
    val staged = jarNames.map { name =>
      val want = expected.getOrElse(
        name,
        throw new RuntimeException(s"$checksumFileName has no entry for $name")
      )
      val tmp = new File(dir, s"$name.$stagingTag.part")
      val actual = download(new URL(s"$base/$name"), tmp, name, progress)
      if (!actual.equalsIgnoreCase(want)) {
        tmp.delete()
        throw new RuntimeException(s"checksum mismatch for $name (got $actual, expected $want)")
      }
      verifyZip(tmp, name)
      if (name == "scala-library.jar") verifyScalaVersion(tmp, version)
      (tmp, new File(dir, name))
    }
    // only publish the jars into their final names once all of them are good,
    // so an interrupted fetch can never leave a half-usable toolchain behind
    staged.foreach { case (tmp, target) => if (!tmp.renameTo(target)) throw new RuntimeException(s"cannot install $target") }
    progress.message(s"[INFO] '$lang' Scala toolchain ready in $dir")
    Some(dir)
  }

  private def checksums(url: String): Map[String, String] = {
    val conn = new URL(url).openConnection()
    conn.setConnectTimeout(ConnectTimeoutMs)
    conn.setReadTimeout(ReadTimeoutMs)
    val src = Source.fromInputStream(conn.getInputStream, "UTF-8")
    try
      src
        .getLines()
        .map(_.trim)
        .filter(_.nonEmpty)
        .flatMap { line =>
          // "<sha256>  <filename>", as produced by sha256sum
          line.split("\\s+").toList match {
            case sum :: name :: Nil => Some(new File(name).getName -> sum)
            case _                  => None
          }
        }
        .toMap
    finally src.close()
  }

  /** Downloads `url` into `target`, returning the SHA-256 of what arrived. */
  private def download(url: URL, target: File, name: String, progress: FetchProgress): String = {
    val digest = MessageDigest.getInstance("SHA-256")
    val conn = url.openConnection()
    conn.setConnectTimeout(ConnectTimeoutMs)
    conn.setReadTimeout(ReadTimeoutMs)
    progress.startJar(name, conn.getContentLengthLong)
    val in: InputStream = conn.getInputStream
    try {
      val out = new FileOutputStream(target)
      try {
        val buf = new Array[Byte](64 * 1024)
        var n = in.read(buf)
        while (n > 0) {
          if (progress.cancelled) throw new RuntimeException("download cancelled")
          digest.update(buf, 0, n)
          out.write(buf, 0, n)
          progress.bytes(n.toLong)
          n = in.read(buf)
        }
      }
      finally out.close()
    }
    finally in.close()
    digest.digest().map(b => f"$b%02x").mkString
  }

  private def verifyZip(jar: File, name: String): Unit = {
    val zf = new ZipFile(jar)
    try if (zf.size == 0) throw new RuntimeException(s"$name is empty")
    finally zf.close()
  }

  private def verifyScalaVersion(libraryJar: File, wanted: String): Unit = {
    val zf = new ZipFile(libraryJar)
    try {
      val entry = Option(zf.getEntry("library.properties"))
        .getOrElse(throw new RuntimeException("scala-library.jar has no library.properties"))
      val props = new java.util.Properties()
      val is = zf.getInputStream(entry)
      try props.load(is)
      finally is.close()
      val got = props.getProperty("version.number", "<unknown>")
      // the pack build stamps a suffix (2.13.18-20260823-...), so accept the
      // exact release or the release followed by a suffix - but not a longer
      // release that merely begins with the same digits
      if (!(got == wanted || got.startsWith(wanted + "-") || got.startsWith(wanted + "+"))) {
        throw new RuntimeException(s"toolchain is Scala $got, but Kojo needs $wanted")
      }
    }
    finally zf.close()
  }
}
