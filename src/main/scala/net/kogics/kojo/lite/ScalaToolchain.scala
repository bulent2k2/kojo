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
import java.util.prefs.Preferences

import net.kogics.kojo.util.Utils

/**
 * Picks the Scala toolchain for the real Kojo JVM, so that Turkish users get
 * the compiler patched with Turkish keywords and everybody else gets the
 * stock one.
 *
 * A packaged Kojo ships two toolchain directories next to the regular jars:
 *   - lib/scala-en: stock scala-library, scala-reflect, scala-compiler and
 *     scalariform jars (English and all other non-Turkish languages), at the
 *     same Scala version the patched jars were built from (staged by
 *     stage-scala-toolchains.sh)
 *   - lib/scala-tr: the Turkish-keyword-patched build of the same four jars
 *     (dez=val, den=var, tanim=def, ...)
 *
 * The launcher JVM always boots on lib/scala-en (see installer/bin/kojo and
 * friends). Before spawning the real Kojo JVM, `select` swaps the toolchain
 * entries on the classpath for the ones matching the persisted user language
 * (the same "Kojolite-Prefs"/"user.language" preference that KojoCtx writes;
 * a language change already requires a Kojo restart, which relaunches this
 * launcher). -Dkojo.toolchain=en|tr overrides the language-based choice, and
 * a kojo.toolchain entry in the per-install kojo.properties does the same
 * (the Koco install4j package sets -Dkojo.toolchain=tr, so it compiles
 * Turkish keywords out of the box even before a language preference exists).
 *
 * If no toolchain directory is on the launcher classpath - e.g. a dev run
 * via sbt, where scalaHome pins the toolchain, or an old-style package -
 * the classpath is left untouched.
 */
object ScalaToolchain {
  val englishDirName = "scala-en"
  val turkishDirName = "scala-tr"
  private val variantDirNames = Set(englishDirName, turkishDirName)
  val prefsNodeName = "Kojolite-Prefs" // keep in sync with KojoCtx.prefs

  def userLanguage: String = {
    val default = System.getProperty("user.language", "en")
    try {
      Preferences.userRoot().node(prefsNodeName).get("user.language", default)
    }
    catch {
      case _: Exception => default // prefs can be unavailable in sandboxed/headless setups
    }
  }

  private def parseOverride(value: String, source: String): Option[String] = value match {
    case "en" => Some(englishDirName)
    case "tr" => Some(turkishDirName)
    case other =>
      println(s"[WARNING] Ignoring unknown kojo.toolchain '$other' (from $source); expected 'en' or 'tr'.")
      None
  }

  def variantDirName: String = {
    val fromSysProp = Option(System.getProperty("kojo.toolchain")).flatMap(parseOverride(_, "system property"))
    def fromAppProp = Utils.appProperty("kojo.toolchain").flatMap(parseOverride(_, "kojo.properties"))
    def fromLanguage = if (userLanguage == "tr") turkishDirName else englishDirName
    fromSysProp.orElse(fromAppProp).getOrElse(fromLanguage)
  }


  /**
   * The Scala release Kojo is running on, e.g. "2.13.18" - taken from the
   * compiler on the launcher classpath. compiler.properties is not shaded by
   * anything else in lib/, unlike library.properties (scalariform bundles a
   * copy of that one).
   */
  def scalaRelease: Option[String] =
    try """(\d+\.\d+\.\d+)""".r.findFirstIn(scala.tools.nsc.Properties.versionNumberString)
    catch { case _: Throwable => None }

  /**
   * Where the jars of `variant` live. Normally the directory shipped inside the
   * package; when the Turkish toolchain was not packaged (the on-demand case),
   * the copy under ~/.kojo/lite/scala-tr, fetching it if this is the first time.
   */
  private def resolveVariantDir(packaged: File, variant: String): File = {
    def packagedHasJars = Utils.filesInDir(packaged.getPath, "jar").nonEmpty
    if (variant != turkishDirName || packagedHasJars) packaged
    else
      scalaRelease
        .flatMap(v => ScalaToolchainFetcher.ensureAvailable(v, FetchProgress.forLauncher()))
        .getOrElse {
          println("[WARNING] The Turkish Scala toolchain is not available; falling back to the stock one.")
          packaged
        }
  }

  def select(cp: List[String]): List[String] = select(cp, variantDirName)

  def select(cp: List[String], variant: String): List[String] = {
    def variantParent(entry: String): Option[File] =
      Option(new File(entry).getParentFile).filter(dir => variantDirNames.contains(dir.getName))

    val toolchainRoots = cp.flatMap(e => variantParent(e).flatMap(dir => Option(dir.getParentFile))).distinct
    toolchainRoots match {
      case Nil => cp
      case root :: _ =>
        val variantDir = resolveVariantDir(new File(root, variant), variant)
        val jars = Utils.filesInDir(variantDir.getPath, "jar").sorted.map(new File(variantDir, _).getPath)
        if (jars.isEmpty) {
          println(s"[WARNING] No Scala toolchain jars found in $variantDir; classpath left unchanged.")
          cp
        }
        else {
          val overrideProp = System.getProperty("kojo.toolchain", "<unset>")
          println(s"[INFO] Scala toolchain: $variantDir (user language '$userLanguage', kojo.toolchain=$overrideProp)")
          val expected = Set("scala-library.jar", "scala-reflect.jar", "scala-compiler.jar", "scalariform.jar")
          val missing = expected -- jars.map(new File(_).getName).toSet
          if (missing.nonEmpty) {
            println(s"[WARNING] Incomplete Scala toolchain in $variantDir; missing: ${missing.mkString(", ")}")
          }
          jars ::: cp.filterNot(e => variantParent(e).isDefined)
        }
    }
  }
}
