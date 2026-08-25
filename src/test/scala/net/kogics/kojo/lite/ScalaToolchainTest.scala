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
import java.nio.file.Files

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.Matchers

@RunWith(classOf[JUnitRunner])
class ScalaToolchainTest extends FunSuite with Matchers {

  // lib/{scala-en,scala-tr} dirs with toolchain jars, plus a regular jar in lib.
  // deleteOnExit deletes in reverse registration order, so register each parent
  // before its children and the whole tree is cleaned up at JVM exit.
  def makeLibDir(): File = {
    val lib = Files.createTempDirectory("kojo-toolchain-test").toFile
    lib.deleteOnExit()
    val toolchainJars = Seq("scala-library.jar", "scala-reflect.jar", "scala-compiler.jar", "scalariform.jar")
    Seq(ScalaToolchain.englishDirName, ScalaToolchain.turkishDirName).foreach { variant =>
      val dir = new File(lib, variant)
      dir.mkdirs()
      dir.deleteOnExit()
      toolchainJars.foreach { name =>
        val jar = new File(dir, name)
        jar.createNewFile()
        jar.deleteOnExit()
      }
    }
    val kojoJar = new File(lib, "kojo.jar")
    kojoJar.createNewFile()
    kojoJar.deleteOnExit()
    lib
  }

  def launcherCp(lib: File): List[String] = {
    val enDir = new File(lib, ScalaToolchain.englishDirName)
    new File(lib, "kojo.jar").getPath :: enDir.listFiles.map(_.getPath).toList
  }

  test("classpath without toolchain dirs is left unchanged (dev mode)") {
    val cp = List("/some/place/scala-library.jar", "/some/place/kojo.jar")
    ScalaToolchain.select(cp, ScalaToolchain.turkishDirName) should be(cp)
  }

  test("selecting the Turkish variant swaps the toolchain jars") {
    val lib = makeLibDir()
    val selected = ScalaToolchain.select(launcherCp(lib), ScalaToolchain.turkishDirName)
    val trDir = new File(lib, ScalaToolchain.turkishDirName).getPath
    selected.filter(_.endsWith("scala-compiler.jar")) should be(List(s"$trDir${File.separator}scala-compiler.jar"))
    selected.count(_.contains(ScalaToolchain.englishDirName)) should be(0)
    selected should contain(new File(lib, "kojo.jar").getPath)
  }

  test("selecting the English variant keeps the stock toolchain jars") {
    val lib = makeLibDir()
    val selected = ScalaToolchain.select(launcherCp(lib), ScalaToolchain.englishDirName)
    val enDir = new File(lib, ScalaToolchain.englishDirName).getPath
    selected.filter(_.endsWith("scala-compiler.jar")) should be(List(s"$enDir${File.separator}scala-compiler.jar"))
    selected.count(_.contains(ScalaToolchain.turkishDirName)) should be(0)
  }

  test("missing variant dir leaves the classpath unchanged") {
    val lib = makeLibDir()
    val cp = launcherCp(lib)
    ScalaToolchain.select(cp, "scala-xx") should be(cp)
  }

  test("toolchain jars come before the other jars") {
    val lib = makeLibDir()
    val selected = ScalaToolchain.select(launcherCp(lib), ScalaToolchain.turkishDirName)
    selected.indexWhere(_.endsWith("scala-library.jar")) should be < selected.indexOf(new File(lib, "kojo.jar").getPath)
  }

  test("toolchain jar names are recognized, versioned or not") {
    import ScalaToolchain.isToolchainJarName
    isToolchainJarName("scala-library.jar") should be(true)
    isToolchainJarName("scala-library-2.13.3.jar") should be(true)
    isToolchainJarName("scala-reflect-2.13.18.jar") should be(true)
    isToolchainJarName("scala-compiler.jar") should be(true)
    isToolchainJarName("scalariform.jar") should be(true)
    isToolchainJarName("scala-swing_2.13-2.1.1.jar") should be(false)
    isToolchainJarName("scala-xml_2.13-1.2.0.jar") should be(false)
    isToolchainJarName("scala-parser-combinators_2.13-1.1.2.jar") should be(false)
    isToolchainJarName("kojo.jar") should be(false)
    isToolchainJarName("scala-library") should be(false)
  }

  test("stray toolchain jars are dropped when a variant is selected") {
    val lib = makeLibDir()
    val strayJar = new File(lib, "scala-library-2.13.3.jar")
    strayJar.createNewFile()
    strayJar.deleteOnExit()
    val selected = ScalaToolchain.select(strayJar.getPath :: launcherCp(lib), ScalaToolchain.englishDirName)
    selected should not contain strayJar.getPath
    val enDir = new File(lib, ScalaToolchain.englishDirName).getPath
    selected.filter(_.endsWith(s"scala-library.jar")) should be(List(s"$enDir${File.separator}scala-library.jar"))
  }

  test("stray toolchain jars are left alone when no variant dir is on the classpath (dev mode)") {
    // in dev runs the scalaHome pack jars ARE the toolchain - they must not be filtered
    val cp = List("/sbt/scala-tr/build/pack/lib/scala-library.jar", "/some/place/kojo.jar")
    ScalaToolchain.select(cp, ScalaToolchain.turkishDirName) should be(cp)
  }

  test("stray toolchain jars are dropped from user jar dirs (libk/extension)") {
    val libk = s"/u/.kojo/lite/libk"
    val entries = List(
      s"$libk/scala-library-2.13.3.jar",
      s"$libk/mylib.jar",
      s"$libk/scalariform.jar",
      s"$libk/scala-xml_2.13-1.2.0.jar"
    )
    ScalaToolchain.withoutStrayToolchainJars(libk, entries) should be(
      List(s"$libk/mylib.jar", s"$libk/scala-xml_2.13-1.2.0.jar")
    )
  }

  test("versionMismatch is quiet on a consistent toolchain") {
    // the test JVM runs on a single toolchain, so this must not fire
    ScalaToolchain.versionMismatch should be(None)
  }

  test("kojo.toolchain overrides the language-based choice") {
    val old = System.getProperty("kojo.toolchain")
    try {
      System.setProperty("kojo.toolchain", "tr")
      ScalaToolchain.variantDirName should be(ScalaToolchain.turkishDirName)
      System.setProperty("kojo.toolchain", "en")
      ScalaToolchain.variantDirName should be(ScalaToolchain.englishDirName)
    }
    finally {
      if (old == null) System.clearProperty("kojo.toolchain") else System.setProperty("kojo.toolchain", old)
    }
  }
}
