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

  // lib/{scala-en,scala-tr} dirs with toolchain jars, plus a regular jar in lib
  def makeLibDir(): File = {
    val lib = Files.createTempDirectory("kojo-toolchain-test").toFile
    lib.deleteOnExit()
    val toolchainJars = Seq("scala-library.jar", "scala-reflect.jar", "scala-compiler.jar", "scalariform.jar")
    Seq(ScalaToolchain.englishDirName, ScalaToolchain.turkishDirName).foreach { variant =>
      val dir = new File(lib, variant)
      dir.mkdirs()
      toolchainJars.foreach(new File(dir, _).createNewFile())
    }
    new File(lib, "kojo.jar").createNewFile()
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
}
