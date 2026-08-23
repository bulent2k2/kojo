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
 *     scalariform jars (English and all non-Turkish languages)
 *   - lib/scala-tr: the same four jars patched with Turkish keywords
 *     (dez=val, den=var, tanim=def, ...)
 *
 * The launcher JVM always boots on lib/scala-en (see installer/bin/kojo and
 * friends). Before spawning the real Kojo JVM, `select` swaps the toolchain
 * entries on the classpath for the ones matching the persisted user language
 * (the same "Kojolite-Prefs"/"user.language" preference that KojoCtx writes;
 * a language change already requires a Kojo restart, which relaunches this
 * launcher). -Dkojo.toolchain=en|tr overrides the language-based choice.
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

  def variantDirName: String = System.getProperty("kojo.toolchain") match {
    case "en" => englishDirName
    case "tr" => turkishDirName
    case _    => if (userLanguage == "tr") turkishDirName else englishDirName
  }

  def select(cp: List[String]): List[String] = select(cp, variantDirName)

  def select(cp: List[String], variant: String): List[String] = {
    def variantParent(entry: String): Option[File] =
      Option(new File(entry).getParentFile).filter(dir => variantDirNames.contains(dir.getName))

    val toolchainRoots = cp.flatMap(e => variantParent(e).flatMap(dir => Option(dir.getParentFile))).distinct
    toolchainRoots match {
      case Nil => cp
      case root :: _ =>
        val variantDir = new File(root, variant)
        val jars = Utils.filesInDir(variantDir.getPath, "jar").sorted.map(new File(variantDir, _).getPath)
        if (jars.isEmpty) {
          println(s"[WARNING] No Scala toolchain jars found in $variantDir; classpath left unchanged.")
          cp
        }
        else {
          println(s"[INFO] Scala toolchain (user language '$userLanguage'): $variantDir")
          jars ::: cp.filterNot(e => variantParent(e).isDefined)
        }
    }
  }
}
