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
import java.nio.file.Files
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.Matchers

/**
 * Drives the whole on-demand fetch path against a local directory served over
 * file://, so the tests never touch the network.
 */
@RunWith(classOf[JUnitRunner])
class ScalaToolchainFetcherTest extends FunSuite with Matchers {
  val version = "2.13.18"

  def sha256(f: File): String = {
    val digest = MessageDigest.getInstance("SHA-256")
    digest.digest(Files.readAllBytes(f.toPath)).map(b => f"$b%02x").mkString
  }

  /** A minimal but real jar; scala-library.jar gets a library.properties. */
  def makeJar(dir: File, name: String, scalaVersion: Option[String]): File = {
    val jar = new File(dir, name)
    val zos = new ZipOutputStream(new FileOutputStream(jar))
    try {
      zos.putNextEntry(new ZipEntry("marker.txt"))
      zos.write(s"$name for test".getBytes("UTF-8"))
      zos.closeEntry()
      scalaVersion.foreach { v =>
        zos.putNextEntry(new ZipEntry("library.properties"))
        zos.write(s"version.number=$v\nmaven.version.number=$v\n".getBytes("UTF-8"))
        zos.closeEntry()
      }
    }
    finally zos.close()
    jar
  }

  /** Builds a fake release directory, with SHA256SUMS, and returns its file:// URL. */
  def publish(libraryVersion: String = s"$version-20260823-123456-abcdef0"): (File, String) = {
    val remote = Files.createTempDirectory("kojo-toolchain-remote").toFile
    val jars = ScalaToolchainFetcher.jarNames.map { n =>
      makeJar(remote, n, if (n == "scala-library.jar") Some(libraryVersion) else None)
    }
    val sums = jars.map(j => s"${sha256(j)}  ${j.getName}").mkString("\n") + "\n"
    Files.write(new File(remote, ScalaToolchainFetcher.checksumFileName).toPath, sums.getBytes("UTF-8"))
    (remote, remote.toURI.toString)
  }

  def withRemote(url: String)(body: => Unit): Unit = {
    val old = System.getProperty("kojo.toolchain.url")
    try {
      System.setProperty("kojo.toolchain.url", url)
      body
    }
    finally {
      if (old == null) System.clearProperty("kojo.toolchain.url") else System.setProperty("kojo.toolchain.url", old)
    }
  }

  /** ensureAvailable caches under the user's home; run each case in its own version dir. */
  def freshVersion(tag: String): String = s"$version-test-$tag"

  test("a published toolchain is fetched, verified and installed") {
    val v = freshVersion("ok")
    val (remote, url) = publish(s"$v-20260823-123456-abcdef0")
    val dir = ScalaToolchainFetcher.cacheDir(v)
    deleteRecursively(dir)
    withRemote(url) {
      val got = ScalaToolchainFetcher.ensureAvailable(v, _ => ())
      got.isDefined should be(true)
      ScalaToolchainFetcher.isComplete(got.get) should be(true)
      // nothing half-written left behind
      got.get.listFiles.count(_.getName.endsWith(".part")) should be(0)
    }
    deleteRecursively(remote)
    deleteRecursively(dir)
  }

  test("an already cached toolchain is used without fetching") {
    val v = freshVersion("cached")
    val dir = ScalaToolchainFetcher.cacheDir(v)
    deleteRecursively(dir)
    dir.mkdirs()
    ScalaToolchainFetcher.jarNames.foreach(n => makeJar(dir, n, None))
    withRemote("file:///nonexistent-so-a-fetch-would-fail/") {
      ScalaToolchainFetcher.ensureAvailable(v, _ => ()) should be(Some(dir))
    }
    deleteRecursively(dir)
  }

  test("a tampered jar is rejected and nothing is installed") {
    val (remote, url) = publish()
    // corrupt one jar after the checksums were published
    Files.write(new File(remote, "scala-reflect.jar").toPath, "not the jar you signed".getBytes("UTF-8"))
    val v = freshVersion("tampered")
    val dir = ScalaToolchainFetcher.cacheDir(v)
    deleteRecursively(dir)
    withRemote(url) {
      ScalaToolchainFetcher.ensureAvailable(v, _ => ()) should be(None)
    }
    ScalaToolchainFetcher.isComplete(dir) should be(false)
    // and no half-written file is left lying around
    Option(dir.listFiles).getOrElse(Array.empty).count(_.getName.endsWith(".part")) should be(0)
    deleteRecursively(remote)
    deleteRecursively(dir)
  }

  test("a toolchain built for another Scala release is rejected") {
    val (remote, url) = publish("2.13.15-20230909-175640-c8d4123")
    val v = freshVersion("wrongversion")
    val dir = ScalaToolchainFetcher.cacheDir(v)
    deleteRecursively(dir)
    withRemote(url) {
      ScalaToolchainFetcher.ensureAvailable(v, _ => ()) should be(None)
    }
    ScalaToolchainFetcher.isComplete(dir) should be(false)
    deleteRecursively(remote)
    deleteRecursively(dir)
  }

  test("an unreachable download leaves no cache and reports the reason") {
    val v = freshVersion("offline")
    val dir = ScalaToolchainFetcher.cacheDir(v)
    deleteRecursively(dir)
    val said = collection.mutable.ListBuffer.empty[String]
    withRemote("file:///definitely/not/here/") {
      ScalaToolchainFetcher.ensureAvailable(v, said += _) should be(None)
    }
    said.exists(_.contains("[WARNING]")) should be(true)
    ScalaToolchainFetcher.isComplete(dir) should be(false)
    deleteRecursively(dir)
  }

  def deleteRecursively(f: File): Unit = {
    if (f.isDirectory) Option(f.listFiles).foreach(_.foreach(deleteRecursively))
    f.delete()
  }
}
