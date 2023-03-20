/*
 * Copyright (C) 2010 Lalit Pant <pant.lalit@gmail.com>
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

package net.kogics.kojo
package xscala

import java.net.URL

import scala.collection.mutable.ListBuffer
import scala.language.postfixOps
import scala.reflect.internal.Flags
import scala.reflect.internal.util.AbstractFileClassLoader
import scala.reflect.internal.util.BatchSourceFile
import scala.reflect.internal.util.OffsetPosition
import scala.reflect.internal.util.Position
import scala.reflect.internal.util.ScalaClassLoader.URLClassLoader
import scala.tools.nsc.Global
import scala.tools.nsc.Settings
import scala.tools.nsc.interactive
import scala.tools.nsc.io.VirtualDirectory
import scala.tools.nsc.reporters.Reporter
import scala.tools.util.PathResolver

import net.kogics.kojo.core.CompletionInfo
import net.kogics.kojo.core.Interpreter.IR
import net.kogics.kojo.core.MemberKind.Class
import net.kogics.kojo.core.MemberKind.Def
import net.kogics.kojo.core.MemberKind.Object
import net.kogics.kojo.core.MemberKind.Package
import net.kogics.kojo.core.MemberKind.PackageObject
import net.kogics.kojo.core.MemberKind.Trait
import net.kogics.kojo.core.MemberKind.Type
import net.kogics.kojo.core.MemberKind.Val
import net.kogics.kojo.core.RunContext
import net.kogics.kojo.util.Utils

trait CompilerListener {
  def error(msg: String, line: Int, column: Int, offset: Int, lineContent: String): Unit
  def warning(msg: String, line: Int, column: Int): Unit
  def info(msg: String, line: Int, column: Int): Unit
  def message(msg: String): Unit
}

// This class borrows code and ideas from scala.tools.nsc.Interpreter
class CompilerAndRunner(
  makeSettings: () => Settings,
  initCode:     => Option[String],
  listener:     CompilerListener,
  runContext:   RunContext
) extends StoppableCodeRunner {
  import language.postfixOps

  var counter = 0
  // The Counter above is used to define/create a new wrapper object for every run. The calling of the entry() 
  //.method within this object results in the initialization of the object, which causes the user submitted 
  // code to run.
  // If we don't increment the counter, the user code will not run (an object is initialized only once)
  // If this approach turns out to be too memory intensive, I'm sure there are other ways of running user 
  // submitted code.'
  val prefixHeader = "object Wrapper"
  val prefix0 = runContext.compilerPrefix

  def prefix = "%s%s\n".format(prefix0, initCode.getOrElse(""))

  var includedLines: Int = 0
  def prefixLines = prefix.linesIterator.size + includedLines

  val codeTemplate = """%s
%s
  }

  def entry() {
    new UserCode
  }
}
"""

  var offsetDelta: Int = _

  val virtualDirectory = new VirtualDirectory("(memory)", None)

  def makeSettings2() = {
    val stng = makeSettings()
    stng.outputDirs.setSingleOutput(virtualDirectory)
    //    stng.deprecation.value = true
    //    stng.feature.value = true
    //    stng.unchecked.value = true
    stng
  }

  val settings = makeSettings2()

  val compilerClasspath: List[URL] = new PathResolver(settings).resultAsURLs.toList
  var classLoader = makeClassLoader
  // needed to prevent pcompiler from making the interp's classloader as 
  // its context loader (which causes a mem leak)
  // we could make pcompiler lazy, but then the first completion takes a big hit 

  private def makeClassLoader = {
    val parent = new URLClassLoader(compilerClasspath, getClass.getClassLoader())
    new AbstractFileClassLoader(virtualDirectory, parent)
  }
  private def loadByName(s: String): Class[_] = (classLoader loadClass s)

  val reporter = new Reporter {
    override def info0(position: Position, msg: String, severity: Severity, force: Boolean): Unit = {
      //      severity.count += 1
      lazy val line = position.line - prefixLines - 1 // we added an extra line after the prefix in the code template. Take it off
      lazy val offset = position.start - offsetDelta - 1 // we added an extra newline char after the prefix
      severity match {
        case ERROR if position.isDefined =>
          listener.error(msg, line, position.column, offset, position.lineContent)
        case WARNING if position.isDefined =>
          listener.warning(msg, line, position.column)
        case INFO if position.isDefined =>
          listener.info(msg, line, position.column)
        case _ =>
          listener.message(msg)
      }
    }
  }

  val compiler = classLoader.asContext {
    new Global(settings, reporter)
  }

  def pfxWithCounter = "%s%d%s".format(prefixHeader, counter, prefix)

  def compilerCode(code00: String): Option[String] = {
    try {
      val (code0, inclLines, includedChars) = Utils.preProcessInclude(code00)
      val pfx = pfxWithCounter
      includedLines = inclLines
      offsetDelta = pfx.length + includedChars
      Some(codeTemplate.format(pfx, code0))
    }
    catch {
      case t: Throwable =>
        listener.message(Utils.exceptionMessage(t)); None
    }
  }

  def compile(code0: String, stopPhase: List[String] = List("cleanup")) = {
    compilerCode(code0) map { code =>
      if (compiler.settings.stopAfter.value != stopPhase) {
        // There seems to be a bug in the PhasesSetting contains method
        // which makes the compiler not see the new stopAfter value
        // So we make a new Settings
        compiler.currentSettings = makeSettings2()
        compiler.settings.stopAfter.value = stopPhase
      }

      val run = new compiler.Run
      reporter.reset()
      run.compileSources(List(new BatchSourceFile("scripteditor", code)))
      //    println(s"[Debug] Script checking done till phase: ${compiler.globalPhase.prev}")
      if (reporter.hasErrors) IR.Error else IR.Success
    } getOrElse (IR.Error)
  }

  def compileAndRun(code0: String) = {
    if (!runContext.isStoryRunning) {
      virtualDirectory.clear()
    }
    counter += 1
    val result = compile(code0, Nil)

    if (result == IR.Success) {
      if (Thread.interrupted) {
        IR.Error
      }
      else {
        try {
          classLoader = makeClassLoader
          classLoader.asContext {
            val loadedResultObject = loadByName("Wrapper%d" format (counter))
            loadedResultObject.getMethod("entry").invoke(loadedResultObject)
            IR.Success
          }
        }
        catch {
          case t: Throwable =>
            var realT = t
            while (realT.getCause != null) {
              realT = realT.getCause
            }
            if (realT.isInstanceOf[InterruptedException]) {
              //              listener.message("Execution thread interrupted.")
            }
            else {
              //              listener.message(Utils.stackTraceAsString(realT))
              Utils.reportException(realT)
            }
            IR.Error
        }
      }
    }
    else {
      IR.Error
    }
  }

  def stop(interpThread: Thread): Unit = {
    interpThread.interrupt()
  }

  def parse(code0: String, browseAst: Boolean) = {
    compilerCode(code0) map { code =>
      compiler.currentSettings = makeSettings2()
      compiler.settings.stopAfter.value = stopPhase()
      if (browseAst) {
        compiler.settings.browse.value = stopPhase()
      }
      val run = new compiler.Run
      reporter.reset()
      run.compileSources(List(new BatchSourceFile("scripteditor", code)))

      if (reporter.hasErrors) {
        IR.Error
      }
      else {
        compiler.printAllUnits()
        IR.Success
      }
    } getOrElse (IR.Error)
  }

  // phase after which you want to stop
  private def stopPhase() = {
    val ret = runContext.astStopPhase
    if (ret != null && ret != "") List(ret) else Nil
  }

  val preporter = new Reporter {
    override def info0(position: Position, msg: String, severity: Severity, force: Boolean): Unit = {
    }
  }

  class KGlobal(s: Settings, r: Reporter) extends interactive.Global(s, r) {
    def mkCompletionProposal(sym: Symbol, tpe: Type, inherited: Boolean, viaView: Symbol): CompletionInfo = {
      // code borrowed from Scala Eclipse Plugin, after my own hacks in this area failed with 2.10.1
      val kind = if (sym.isSourceMethod && !sym.hasFlag(Flags.ACCESSOR | Flags.PARAMACCESSOR)) Def
      else if (sym.hasPackageFlag) Package
      else if (sym.isClass) Class
      else if (sym.isTrait) Trait
      else if (sym.isPackageObject) PackageObject
      else if (sym.isModule) Object
      else if (sym.isType) Type
      else Val
      val name = sym.decodedName

      val returnType =
        if (sym.isMethod) tpe.finalResultType.toString
        else if (sym.isVal || sym.isVar) tpe.resultType.toString
        else name

      val signature =
        if (sym.isMethod) {
          "%s: %s".format(
            name +
            (if (!sym.typeParams.isEmpty) sym.typeParams.map { _.name }.mkString("[", ",", "]") else "") +
            tpe.paramss.map(_.map(_.tpe.toString).mkString("(", ", ", ")")).mkString,
            returnType
          )
        }
        else {
          s"$name: $returnType"
        }

      val container = sym.owner.enclClass.fullName

      // rudimentary relevance, place own members before inherited ones, and before view-provided ones
      var relevance = 100
      if (!sym.isLocalToBlock) relevance += 10 // non-local symbols are less relevant than local ones
      if (inherited) relevance += 10
      if (viaView != NoSymbol) relevance += 20
      if (sym.hasPackageFlag) relevance += 30
      // theoretically we'd need an 'ask' around this code, but given that
      // Any and AnyRef are definitely loaded, we call directly to definitions.
      if (sym.owner == definitions.AnyClass
        || sym.owner == definitions.AnyRefClass
        || sym.owner == definitions.ObjectClass) {
        relevance += 40
      }
      val pfx = prefix
      val casePenalty = if (name.take(pfx.length) != pfx.mkString) 50 else 0
      relevance += casePenalty

      val namesAndTypes = for {
        section <- sym.paramss
        if section.nonEmpty && !section.head.isImplicit
      } yield for (param <- section) yield (param.name.toString, param.tpe.toString)

      val (scalaParamNames, paramTypes) = namesAndTypes.map(_.unzip).unzip

      val isJavaMethod = sym.isJavaDefined && sym.isMethod

      CompletionInfo(
        kind,
        name,
        signature,
        container,
        relevance,
        sym.isJavaDefined,
        scalaParamNames,
        paramTypes,
        returnType,
        sym.fullName
      )
    }
  }

  val pcompiler = classLoader.asContext {
    new KGlobal(makeSettings(), preporter)
  }

  def typeAt(code0: String, offset: Int): String = {
    import interactive._

    compilerCode(code0) map { code =>
      classLoader.asContext {
        val source = new BatchSourceFile("scripteditor", code)
        val pos = new OffsetPosition(source, offset + offsetDelta + 1)

        var r1 = new Response[Unit]
        pcompiler.askReload(List(source), r1)

        var resp = new Response[pcompiler.Tree]
        pcompiler.askTypeAt(pos, resp)

        val respget = resp.get
        val response: pcompiler.Response[String] = pcompiler.askForResponse { () =>
          respget match {
            case Left(x) =>
              x match {
                case t: pcompiler.ValOrDefDef => t.tpt.toString
                case t: pcompiler.TypeDef     => t.name.toString
                case t: pcompiler.ClassDef    => t.name.toString
                case _                        => x.tpe.toString
              }

            case Right(y) =>
              // println("Right:" + y)
              ""
          }
        }
        response.get match {
          case Left(s)  => s
          case Right(_) => ""
        }
      }
    } getOrElse ("")
  }

  import core.CompletionInfo

  def completions(code: String, offset: Int, selection: Boolean): List[CompletionInfo] = {
    val augmentedCode =
      "%s ;} // %s".format(code.substring(0, offset), code.substring(offset))

    val queryOffset = if (selection) offset - 1 else offset

    completionQuery(augmentedCode, queryOffset, selection) match {
      case Nil => if (selection) completionQuery(code, queryOffset, selection) else Nil
      case _@ret => ret
    }
  }

  private def completionQuery(code0: String, offset: Int, selection: Boolean): List[CompletionInfo] = {
    import interactive._

    compilerCode(code0) map { code =>
      classLoader.asContext {
        val source = new BatchSourceFile("scripteditor", code)
        val pos = new OffsetPosition(source, offset + offsetDelta + 1)

        val r1 = new Response[Unit]
        pcompiler.askReload(List(source), r1)

        val resp = new Response[List[pcompiler.Member]]
        if (selection) {
          pcompiler.askTypeCompletion(pos, resp)
        }
        else {
          pcompiler.askScopeCompletion(pos, resp)
        }

        val completionTimeout = 3000
        resp.get(completionTimeout) match {
          case Some(Left(completions)) =>
            val response: pcompiler.Response[List[CompletionInfo]] = pcompiler.askForResponse { () =>
              val elb = new ListBuffer[CompletionInfo]
              completions.foreach { completion =>
                try {
                  completion match {
                    case pcompiler.TypeMember(sym, tpe, true, inherited, viaView, aliasInfo) if !sym.isConstructor /*&& nameMatches(sym)*/ =>
                      elb += pcompiler.mkCompletionProposal(sym, tpe, inherited, viaView)
                    case pcompiler.ScopeMember(sym, tpe, true, _, aliasInfo) if !sym.isConstructor /*&& nameMatches(sym)*/ =>
                      elb += pcompiler.mkCompletionProposal(sym, tpe, false, pcompiler.NoSymbol)
                    case _ =>
                  }
                }
                catch {
                  case t: Throwable =>
                    println("Completion Problem 0: " + t.getMessage())
                  // ignore, and move on to the next one
                }
              }
              elb.toList
            }
            response.get match {
              case Left(l)  => l
              case Right(_) => Nil
            }
          case Some(Right(_)) => Nil
          case None           => Nil
        }
      }
    } getOrElse (Nil)
  }
}
