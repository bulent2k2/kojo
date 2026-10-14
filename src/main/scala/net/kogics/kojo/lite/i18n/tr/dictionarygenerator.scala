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
package net.kogics.kojo.lite.i18n.tr

import java.io.File

import scala.io.Source

import scalariform.lexer.ScalaLexer
import scalariform.lexer.Token
import scalariform.lexer.Tokens

/**
 * Generates translation-dictionary.tsv from the Turkish wrapper sources.
 *
 *   ./sbt.sh "runMain net.kogics.kojo.lite.i18n.tr.DictionaryGenerator"      # from the repo root
 *
 * (On a modern JDK, use the launcher-jar recipe in CLAUDE.md instead of sbt.sh.)
 *
 * WHAT IT EXTRACTS: only declarations whose RHS is a plain chain of names --
 *   def ileri(adım: Kesir): Birim = englishTurtle.forward(adım)   -> ileri  forward
 *   val siyah = Renkler.siyah  ... Renkler.siyah = r.black         -> siyah  black   (chain is resolved)
 *   type Belki[B] = Option[B]                                       -> Belki  Option
 *   val (a, b) = (x.foo, y.bar)                                     -> a foo, b bar
 * Definitions whose body is a block, if, match, etc. are skipped because they carry no
 * "English counterpart"; this is a limit, not a bug. Rows whose target stays Turkish
 * (the chain cannot be resolved) are skipped as well.
 *
 * WHY TOKENS: a first version built with regular expressions took type parameters such as
 * `apply`, `Col`, `C2`, and names inside comments/strings for "pairs"
 * (measured: 58 bogus ambiguities). The patched scalariform knows both the Turkish
 * keywords and the string/comment boundaries correctly.
 */
object DictionaryGenerator {
  import TranslationDictionary.{Row, MemberOnlyNote, AliasNote, BlockBodyNote, DeprecatedNote}

  /** Files that are not wrappers: dictionary tables, output translation, help text... */
  val skippedFiles = Set("dict.scala", "translate.scala", "help.scala", "templates.scala",
    "translator.scala", "translatormain.scala", "translationdictionary.scala", "dictionarygenerator.scala",
    "translationvalidator.scala", "eskiz.scala") // the translator itself is not a wrapper; eskiz.scala: the Eskiz interface + a reflection adapter

  private val ascii = "^[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)?$".r
  /** English objects that are written qualified in the English API: `math.sqrt`, `math.Pi`. */
  private val keepQualified = Set("math", "Picture")
  /** Turkish wrapper classes constructed without `new`: `Yöney2B(v.rotate(a))` and `Eşlem(m.filter(f))` wrap the inner chain
    * (otherwise bogus rivals such as `ele -> collection.mutable.Map` appeared and tied with `ele -> filter`, #86). */
  private val wrapperConstructors = Set("Yöney2B", "Eşlem")
  /** Names that every object has and that make no sense to translate; if they enter the dictionary,
    * bogus pairs such as `apply -> Resim` with 28 sources appear (measured). */
  private val universalNames = Set("apply", "unapply", "toString", "equals", "hashCode", "copy", "main", "compare")
  /** `class BüyütBD(...) extends ComposableTransformer`: a super class without parentheses is not
    * an API name but a base class; taking it for the target produced `büyüt -> ComposableTransformer`
    * (measured). A chain with parentheses (extends Dönüşüm(richBuiltins.rot(a))) is still taken. */
  private val baseClasses = Set("ComposableTransformer", "Transformer", "AnyRef", "Any", "Serializable", "Product",
    "Exception", "RuntimeException", "Enumeration", "Ordered", "Ordering", "Iterable", "Seq", "IndexedSeq", "Iterator")
  /** Names of 1-2 letters are not wrappers, they are type parameters or internal helpers (Col, C2, S, f2, ts). */
  private def isWrapperName(name: String) = name.length > 2 && !universalNames(name)
  /** `def nokta(p: Nokta) = p`: the RHS is a parameter, not an English API. There are hardly any
    * English API names of 1-2 letters; measured: without this filter p/x/n were the most frequent
    * "ambiguous" names (294/250/205 times). */
  /** toString/equals CAN be targets (yazıya -> toString; measured: without it `x.yazıya` is not translated);
    * apply/unapply/copy/main cannot. */
  private val notTargets = Set("apply", "unapply", "copy", "main")
  private def isTargetName(en: String) = { val lastPart = en.substring(en.lastIndexOf('.') + 1); lastPart.length > 2 && !notTargets(lastPart) }
  private val declarationTokenTypes = Set(Tokens.DEF, Tokens.VAL, Tokens.VAR, Tokens.TYPE)
  private val openers = Set(Tokens.LPAREN, Tokens.LBRACKET, Tokens.LBRACE)
  private val closers = Set(Tokens.RPAREN, Tokens.RBRACKET, Tokens.RBRACE)

  def sourceFiles(root: File): Seq[File] = {
    val i18n = new File(root, "src/main/scala/net/kogics/kojo/lite/i18n")
    val tr = new File(i18n, "tr")
    val trFiles = Option(tr.listFiles()).getOrElse(Array.empty[File])
      .filter(f => f.getName.endsWith(".scala") && !skippedFiles(f.getName)).sortBy(_.getName)
    new File(i18n, "trInit.scala") +: trFiles.toSeq
  }

  def readFile(f: File): String = {
    val s = Source.fromFile(f, "UTF-8")
    try s.mkString finally s.close()
  }

  /** Raw pairs from one file (chains not yet resolved; `en` may still be Turkish). */
  def extractRaw(fileName: String, code: String): Seq[Row] = {
    val allTokens = ScalaLexer.rawTokenise(code, forgiveErrors = true, scalaVersion = "2.13.0")
    val ts = allTokens.filterNot(t => t.tokenType == Tokens.WS || t.tokenType.isComment || t.tokenType.isNewline).toVector
    val lineNumber: Int => Int = { val lineStarts = 0 +: code.zipWithIndex.collect { case ('\n', i) => i + 1 }
      ofs => lineStarts.count(_ <= ofs) }
    def id(t: Token) = t.tokenType.isId && !t.text.startsWith("`") && t.text.exists(_.isLetter)
    val rows = Vector.newBuilder[Row]

    /** If ts(i) is an opener, the position of the matching closer; otherwise i. */
    def findMatch(i0: Int): Int = {
      if (i0 >= ts.length || !openers(ts(i0).tokenType)) return i0
      var i = i0; var depth = 0
      while (i < ts.length) {
        if (openers(ts(i).tokenType)) depth += 1
        else if (closers(ts(i).tokenType)) { depth -= 1; if (depth == 0) return i }
        i += 1
      }
      ts.length - 1
    }

    /**
     * Does the declaration at ts(i0) start with `@deprecated` (#63).
     *
     * In the token sequence whitespace and comments are filtered out, so the annotation sits
     * directly in front of the declaration: `@ deprecated ( "..." , "..." ) val sil_geri`. Both the
     * form with parentheses and the one without exist; modifiers (private, lazy, override) may come in between.
     */
    def isDeprecated(i0: Int): Boolean = {
      var i = i0 - 1
      while (i >= 0 && ts(i).tokenType.isKeyword && !declarationTokenTypes(ts(i).tokenType)) i -= 1
      if (i >= 0 && closers(ts(i).tokenType)) {
        var depth = 0
        var openerFound = false
        while (i >= 0 && !openerFound) {
          if (closers(ts(i).tokenType)) depth += 1
          else if (openers(ts(i).tokenType)) { depth -= 1; if (depth == 0) openerFound = true }
          if (!openerFound) i -= 1
        }
        i -= 1
      }
      i >= 1 && ts(i).text == "deprecated" && ts(i - 1).tokenType == Tokens.AT
    }

    /** Does one of the definition's parameters occur inside the call parenthesis at ts(i). */
    def argumentUsesParameter(i0: Int, params: Set[String]): Boolean = {
      if (i0 >= ts.length || ts(i0).tokenType != Tokens.LPAREN) return false
      val closingIndex = findMatch(i0)
      (i0 + 1 until closingIndex).exists(j => id(ts(j)) && params(ts(j).text))
    }

    // Reads the chain `id (DOT id)*` starting at position i; returns the last name and the next position.
    def chain(i0: Int, params: Set[String] = Set.empty): Option[(String, Int)] = {
      val hasNew = i0 < ts.length && ts(i0).tokenType == Tokens.NEW
      var i = if (hasNew) i0 + 1 else i0
      // `= { RepeatCommands.repeat(n) { ... } }`: definitions with a block body whose first
      // statement is a call chain (yinele, yineleİçin, zıpla...). If the first statement is not a
      // chain (val, if, ...) None is returned and the definition is skipped -- a limit, not a bug.
      if (i < ts.length && ts(i).tokenType == Tokens.LBRACE) return chain(i + 1, params)
      if (i >= ts.length || !id(ts(i))) return None
      val parts = Vector.newBuilder[String]
      parts += ts(i).text
      var lastName = ts(i).text; i += 1
      while (i + 1 < ts.length && ts(i).tokenType == Tokens.DOT && id(ts(i + 1))) { lastName = ts(i + 1).text; parts += lastName; i += 2 }
      // `def karekökü(x) = math.sqrt(x)`, `richBuiltins.Picture.circle(r)`: in the English API
      // sqrt and circle are NOT bare -- they are `math.sqrt`, `Picture.circle`. Dropping the
      // qualifier produced translations that did not compile (measured: angles.kojo, "not found: value sqrt").
      // Only for the known API objects: the other qualifiers (englishTurtle, richBuiltins)
      // are internal names of the wrapper and do not reach the API.
      val p = parts.result()
      val rootIndex = p.lastIndexWhere(keepQualified)
      if (rootIndex >= 0 && rootIndex < p.length - 1) lastName = p.drop(rootIndex).mkString(".")
      // `new Resim(richBuiltins.Picture.circle(r))`: the outer wrapper class is not the target,
      // the target is the chain INSIDE the parenthesis. The criterion is NOT "has a Turkish letter" --
      // Resim, Renk, Nokta are ASCII -- the criterion is that it is constructed with `new`. If there
      // is no chain inside (new Kaplumbağa(t)) the class itself remains the target.
      // If there is no chain inside (new AColor(120, 0, 200), new Kaplumbağa(x, y)) there is NO target: `val mor =
      // new AColor(...)` is a value, not an alias -- counting the class name as the target
      // produced `mor -> AColor` (measured: circles.kojo). For new Kaplumbağa there is a rule (yeniKaplumbağa).
      // `Yöney2B(rb.bouncePicVectorOffStage(p, v))`: a wrapper constructor without `new` is the same --
      // the target is the inner chain (measured: lunar-lander.kojo, sahneKenarındanYansıtma -> Vector2D).
      val isWrapperConstructor = p.length == 1 && wrapperConstructors(p.head)
      // `new Eşlem[A,D](Map.from(...))`: the type parameter list sits between the class name and
      // the parenthesis and blocked the descent -- the target stayed `Eşlem` and was tied
      // to `collection.mutable.Map` by a hand rule (measured: 12 lines including eşleme/öbekleEsnek,
      // `çiftler.eşleme` -> `çiftler.collection.mutable.Map`). The bracket list is skipped.
      if ((hasNew || isWrapperConstructor) && i < ts.length && ts(i).tokenType == Tokens.LBRACKET)
        i = findMatch(i) + 1
      if ((hasNew || isWrapperConstructor) && i < ts.length && ts(i).tokenType == Tokens.LPAREN) return chain(i + 1, params) match {
        case Some(inner) if !params(inner._1) => Some(inner)
        case _                                => None
      }
      // `Utils.awtColorToDoodleColor(r).lighten(açıklık)`: the chain was cut at the call
      // parenthesis and the target stayed `awtColorToDoodleColor`. But "take the last one" is
      // wrong too: in `y.grouped(boy).map(_.toSeq)` the meaningful one is the FIRST call.
      // The criterion was chosen by measurement (8 of 8 definitions): the call that takes THE
      // DEFINITION'S OWN PARAMETER is the meaningful one -- `lighten(açıklık)` yes, `map(_.toSeq)` no.
      // If there is no such call the first one stays, i.e. the old behavior.
      if (params.nonEmpty) {
        var links = Vector((lastName, argumentUsesParameter(i, params)))
        var j = i
        var keepGoing = true
        while (keepGoing && j < ts.length && ts(j).tokenType == Tokens.LPAREN) {
          val closingIndex = findMatch(j)
          if (closingIndex + 2 < ts.length && ts(closingIndex + 1).tokenType == Tokens.DOT && id(ts(closingIndex + 2))) {
            j = closingIndex + 3
            links :+= ((ts(closingIndex + 2).text, argumentUsesParameter(j, params)))
          }
          else keepGoing = false
        }
        links.lastIndexWhere(_._2) match {
          case k if k > 0 => return Some((links(k)._1, j))
          case _          => ()
        }
      }
      Some((lastName, i))
    }

    // Skips from the start of a declaration up to the `=` (parameters, kind); None if there is no `=` at depth 0.
    def indexOfEquals(i0: Int): Option[Int] = {
      var i = i0; var depth = 0
      while (i < ts.length) {
        val t = ts(i)
        if (openers(t.tokenType)) depth += 1
        else if (closers(t.tokenType)) { depth -= 1; if (depth < 0) return None }
        else if (depth == 0 && t.tokenType == Tokens.EQUALS) return Some(i)
        else if (depth == 0 && (declarationTokenTypes(t.tokenType) || t.tokenType.isKeyword && t.tokenType != Tokens.IMPLICIT && t.tokenType != Tokens.OVERRIDE && t.tokenType != Tokens.FINAL && t.tokenType != Tokens.LAZY && t.tokenType != Tokens.PRIVATE && t.tokenType != Tokens.PROTECTED)) return None
        i += 1
      }
      None
    }

    // Parameter names in the signature: every name that occurs in the form `name:`. In definitions such as
    // `def çiz(rler: Resim*) = rler.foreach(...)` the RHS starts with a parameter; taking it for an English API
    // polluted the dictionary (measured: p/x/n/rler were the most frequent "ambiguous" names).
    def parameterNames(fromIndex: Int, toIndex: Int): Set[String] =
      (fromIndex until toIndex).collect { case j if id(ts(j)) && j + 1 < ts.length && ts(j + 1).tokenType == Tokens.COLON => ts(j).text }.toSet

    // package.scala: it has no wrappers, but it has the core type aliases (type Nokta = Point) and
    // constants (val çokHızlı = superFast); defs and keyword lists (_trKeywords,
    // trKeywordSet) are not taken. Taking only types made the speed constants disappear (measured: eye.kojo).
    val isPackageFile = fileName == "package.scala"
    def acceptedInPackage(kind: String, name: String) = !isPackageFile || ((kind == "type" || kind == "val") && !name.startsWith("_") && !name.toLowerCase.contains("keyword"))
    val typesOnly = false
    // data.scala: the hand-built `nDef("silVeSakla", "cleari")` table (Name(tr, en, ne)). Not a wrapper,
    // directly a pair; empty en = "has no English counterpart". Read from the same token stream.
    if (fileName == "data.scala") {
      val kinds = Map("nDef" -> "def", "nVal" -> "val", "nType" -> "type", "nMet" -> "def")
      var j = 0
      while (j + 5 < ts.length) {
        if (id(ts(j)) && kinds.contains(ts(j).text) && ts(j + 1).tokenType == Tokens.LPAREN &&
            ts(j + 2).tokenType == Tokens.STRING_LITERAL && ts(j + 3).tokenType == Tokens.COMMA && ts(j + 4).tokenType == Tokens.STRING_LITERAL) {
          val tr = ts(j + 2).text.stripPrefix("\"").stripSuffix("\""); val en = ts(j + 4).text.stripPrefix("\"").stripSuffix("\"")
          if (en.nonEmpty && tr != en && isWrapperName(tr) && isTargetName(en)) rows += Row(kinds(ts(j).text), tr, en, s"$fileName:${lineNumber(ts(j).offset)}")
        }
        j += 1
      }
      return rows.result()
    }
    // Enclosing object: the members inside `object Resim { ... }`. Members whose English counterpart is BARE
    // (picStack, stageArea) are written in tr as `Resim.x`; the Dictionary builds both directions from this.
    // The depth also filters out LOCAL vals: `def f = { val faktor = math.pow(...) }` is not a
    // wrapper (measured: bogus pair faktor -> pow).
    val objectStack = scala.collection.mutable.Stack.empty[(String, Int)]
    var depth = 0
    var pendingObject: Option[String] = None
    def enclosingObject: Option[String] = objectStack.headOption.map(_._1).filter(_.nonEmpty)
    def atMemberLevel: Boolean = depth <= 1 || objectStack.headOption.exists(_._2 == depth)
    var i = 0
    while (i < ts.length) {
      val t = ts(i)
      // object/class/trait: the body is member level. When only object was taken, a `val çokHızlı = ...`
      // in a trait body was taken for a local and filtered out (measured: eye.kojo, fireworks-canvas.kojo).
      if ((t.tokenType == Tokens.OBJECT || t.tokenType == Tokens.CLASS || t.tokenType == Tokens.TRAIT) && i + 1 < ts.length && id(ts(i + 1)))
        pendingObject = Some(if (t.tokenType == Tokens.OBJECT) ts(i + 1).text else "")
      if (t.tokenType == Tokens.LBRACE) { depth += 1; pendingObject.foreach(n => objectStack.push((n, depth))); pendingObject = None }
      if (t.tokenType == Tokens.RBRACE) { while (objectStack.headOption.exists(_._2 >= depth)) objectStack.pop(); depth -= 1 }
      val normalizedKind = t.text match { case "dez" => "val"; case "den" => "var"; case "tanım" => "def"; case "tür" => "type"; case x => x }
      if (declarationTokenTypes(t.tokenType) && i + 1 < ts.length && acceptedInPackage(normalizedKind, ts(i + 1).text)) {
        val kind = normalizedKind
        val source = s"$fileName:${lineNumber(t.offset)}"
        val next = ts(i + 1)
        if (id(next)) {
          indexOfEquals(i + 2).foreach { e =>
            val params = parameterNames(i + 2, e)
            val isLocalValue = (kind == "val" || kind == "var") && !atMemberLevel
            val hasBlockBody = e + 1 < ts.length && ts(e + 1).tokenType == Tokens.LBRACE
            chain(e + 1, params).foreach { case (en, _) =>
              if (en != next.text && !params(en) && !isLocalValue && isWrapperName(next.text) && isTargetName(en)) {
                val trName = if (enclosingObject.contains("Resim") && !en.contains('.') && kind == "def") "Resim." + next.text else next.text
                val tags = Seq(if (hasBlockBody) BlockBodyNote else "", if (isDeprecated(i)) DeprecatedNote else "").filter(_.nonEmpty)
                rows += Row(kind, trName, en, source, note = tags.mkString(","))
              }
            }
          }
        }
        else if (next.tokenType == Tokens.LPAREN) {
          // val (a, b, c) = (x, y, z)
          var j = i + 2; val names = Vector.newBuilder[String]
          while (j < ts.length && ts(j).tokenType != Tokens.RPAREN) { if (id(ts(j))) names += ts(j).text; j += 1 }
          if (j + 2 < ts.length && ts(j + 1).tokenType == Tokens.EQUALS && ts(j + 2).tokenType == Tokens.LPAREN) {
            var k = j + 3; val targets = Vector.newBuilder[Option[String]]
            var depth = 0; var keepGoing = true
            while (keepGoing && k < ts.length) {
              chain(k) match {
                case Some((en, k2)) if depth == 0 => targets += Some(en); k = k2
                case _ =>
                  ts(k).tokenType match {
                    case tt if openers(tt)             => depth += 1; targets += None; k += 1
                    case Tokens.RPAREN if depth == 0   => keepGoing = false
                    case tt if closers(tt)             => depth -= 1; k += 1
                    case Tokens.COMMA                  => k += 1
                    case _                             => targets += None; k += 1
                  }
              }
              // after reading an item, skip ahead to the comma or the closing parenthesis
              if (keepGoing) {
                var d2 = 0
                while (k < ts.length && !(d2 == 0 && (ts(k).tokenType == Tokens.COMMA || ts(k).tokenType == Tokens.RPAREN))) {
                  if (openers(ts(k).tokenType)) d2 += 1 else if (closers(ts(k).tokenType)) d2 -= 1
                  k += 1
                }
                if (k < ts.length && ts(k).tokenType == Tokens.COMMA) k += 1
                else keepGoing = false
              }
            }
            val nameList = names.result(); val targetList = targets.result()
            if (nameList.size == targetList.size) nameList.zip(targetList).foreach { case (tr, Some(en)) if en != tr && isWrapperName(tr) && isTargetName(en) => rows += Row(kind, tr, en, source); case _ => }
          }
        }
      }
      // `class DöndürBD(açı: Kesir) extends Dönüşüm(richBuiltins.rot(açı))`: transformers
      // and Turkish classes are built like this; class name -> the chain in the parenthesis of extends
      // (otherwise the name of the super class). Then `döndür -> DöndürBD -> rot` is resolved transitively.
      else if ((t.tokenType == Tokens.CLASS || t.tokenType == Tokens.OBJECT) && i + 1 < ts.length && id(ts(i + 1)) && !typesOnly) {
        val name = ts(i + 1).text
        var j = i + 2; var depth = 0
        def isStop(tt: scalariform.lexer.TokenType) = tt == Tokens.EXTENDS || tt == Tokens.LBRACE || declarationTokenTypes(tt) || tt == Tokens.CLASS || tt == Tokens.OBJECT
        while (j < ts.length && !(depth == 0 && isStop(ts(j).tokenType))) {
          if (openers(ts(j).tokenType)) depth += 1 else if (closers(ts(j).tokenType)) depth -= 1
          j += 1
        }
        if (j < ts.length && ts(j).tokenType == Tokens.EXTENDS) {
          val params = parameterNames(i + 2, j)
          chain(j + 1, params).foreach { case (superClass, k) =>
            // Only a chain with parentheses: `extends Dönüşüm(richBuiltins.rot(a))` -> rot. A super
            // class name without parentheses (extends TurkishTurtle, extends ComposableImageEffect) is not an API but an
            // internal class; counting it as the target produced Kaplumbağa -> TurkishTurtle (measured). Real ones such as
            // Yöney2B -> Vector2D are in the rules file.
            val innerChain = if (k < ts.length && ts(k).tokenType == Tokens.LPAREN) chain(k + 1, params).map(_._1).filterNot(params) else None
            innerChain.foreach { target =>
              if (target != name && isWrapperName(name) && isTargetName(target))
                rows += Row(if (t.tokenType == Tokens.CLASS) "class" else "object", name, target, s"$fileName:${lineNumber(t.offset)}")
            }
            locally(superClass)
          }
        }
      }
      i += 1
    }
    rows.result()
  }

  /** Resolves Turkish targets transitively: siyah -> Renkler.siyah -> black. At most 4 hops.
    * The criterion for a "Turkish target" is not letters but THE NAME BEING DEFINED HERE: silipSakla -> silVeSakla
    * is written in ASCII but silVeSakla is a wrapper; taking it for English made the translation
    * print silVeSakla() (measured: animated-square-creation.kojo). */
  def resolveChains(raw: Seq[Row], ruleTargets: Map[String, String] = Map.empty): Seq[Row] = {
    // Resolution order: wrapper rows first, the data.scala table last. In the opposite order
    // `çokHızlı -> tr.çokHızlı` was resolved to the table's SuperFast and gave a second vote
    // to the wrong target (measured: eye.kojo, "not found: value SuperFast").
    val targetsOf: Map[String, Seq[String]] = raw.groupBy(_.tr).map { case (tr, ss) =>
      tr -> ss.sortBy(s => s.source.startsWith("data.scala")).map(_.en).distinct }
    def resolve(en: String, depth: Int, seen: Set[String]): Option[String] =
      if (depth == 0 || seen(en)) None
      else if (targetsOf.contains(en)) targetsOf(en).iterator.flatMap(e => resolve(e, depth - 1, seen + en)).nextOption()
      // `canlan -> tepkiVer`: the target is ASCII but a Turkish name; since it has a block body there is no generated row
      // for it, the rules file knows it (tepkiVer -> react). A rule is also a link (measured: hunted.kojo).
      else if (ruleTargets.contains(en)) Some(ruleTargets(en))
      else if (ascii.matches(en)) Some(en)
      else None
    // A definition with a block body (`def üçgen(en) = { yinele(3) { ... } }`): if the first statement is another Turkish
    // wrapper this is an IMPLEMENTATION, not an alias -- when chained, `üçgen -> repeat`
    // came out and the user's `tanım üçgen` definition became `def repeat` (measured:
    // sierpinski-tri.kojo). A row with a block body is linked only directly to an English target.
    raw.flatMap { s =>
      if (s.hasNote(BlockBodyNote)) { if (targetsOf.contains(s.en)) None else resolve(s.en, 1, Set(s.tr)).map(en => s.copy(en = en, note = "")) }
      else resolve(s.en, 4, Set(s.tr)).map(en => s.copy(en = en))
    }
  }

  /** Constant tables: between `object Çalgı { val AkustikBas = 32 }` (muzik.scala) and
    * `object Instrument { val ACOUSTIC_BASS = 32 }` (music/Instrument.scala), and between `object Görünüş
    * { val araba = "/media/costumes/car.png" }` and `class Costume { val car = ... }`
    * there is no wrapper, the match is made by VALUE (a number or string constant). Names carrying the same value
    * are matched in order (Piyano/PIANO, AkustikKuyruklu/ACOUSTIC_GRAND); a name beyond the end of the order is bound to the first.
    * The rows are written qualified on both sides (`Çalgı.AkustikBas` ->
    * `Instrument.ACOUSTIC_BASS`): TR->EN swallows the receiver and writes the full name, EN->TR takes the receiver from the rule
    * (Instrument -> Çalgı) and the member from here. Alias receivers (Çizim = Görünüş) only for
    * TR->EN, with the `alias` note. */
  private val constantDefinition = "val\\s+([A-Za-zİıŞşĞğÖöÜüÇç_][A-Za-z0-9İıŞşĞğÖöÜüÇç_]*)\\s*(?::\\s*[A-Za-zİıŞşĞğÖöÜüÇç_]+)?\\s*=\\s*(\\d+|\"[^\"]*\")".r
  /** (Turkish file, Turkish object, aliases, English file, English object). */
  val constantObjectPairs: Seq[(String, String, Seq[String], String, String)] = Seq(
    ("lite/i18n/tr/muzik.scala", "Çalgı", Nil, "music/Instrument.scala", "Instrument"),
    ("lite/i18n/tr/cizim.scala", "Görünüş", Seq("Çizim"), "turtle/TurtleWorldAPI.scala", "Costume"),
    ("lite/i18n/tr/cizim.scala", "Görünüş", Seq("Çizim"), "turtle/TurtleWorldAPI.scala", "Background"),
    ("lite/i18n/tr/cizim.scala", "Artalan", Nil, "turtle/TurtleWorldAPI.scala", "Background"),
    ("lite/i18n/tr/ses.scala", "Ses", Nil, "turtle/TurtleWorldAPI.scala", "Sound"))
  private def objectBody(code: String, objectName: String): Option[(String, Int)] = {
    val startIndex = ("(object|class)\\s+" + java.util.regex.Pattern.quote(objectName) + "\\s*\\{").r.findFirstMatchIn(code).map(_.start).getOrElse(-1)
    if (startIndex < 0) return None
    val body = code.substring(startIndex)
    var d = 0; var i = body.indexOf('{'); var endIndex = -1
    while (endIndex < 0 && i < body.length) { body(i) match { case '{' => d += 1; case '}' => d -= 1; if (d == 0) endIndex = i; case _ => }; i += 1 }
    Some((body.substring(0, if (endIndex < 0) body.length else endIndex), code.substring(0, startIndex).count(_ == '\n') + 1))
  }
  private def constants(file: File, objectName: String): Seq[(String, String, Int)] =
    if (!file.exists()) Nil
    else objectBody(readFile(file), objectName).toSeq.flatMap { case (body, startLine) =>
      constantDefinition.findAllMatchIn(body).toSeq.map(m => (m.group(1), m.group(2), startLine + body.substring(0, m.start).count(_ == '\n')))
    }
  def constantObjects(root: File): Seq[Row] = constantObjectPairs.flatMap { case (trFile, trObject, aliases, enFile, enObject) =>
    val sourceRoot = new File(root, "src/main/scala/net/kogics/kojo")
    val tr = constants(new File(sourceRoot, trFile), trObject)
    val en = constants(new File(sourceRoot, enFile), enObject)
    val trByValue = tr.groupBy(_._2); val enByValue = en.groupBy(_._2)
    def ordered(value: String, i: Int, other: Map[String, Seq[(String, String, Int)]]) =
      other.get(value).map(ks => ks(math.min(i, ks.size - 1))._1)
    val trFileName = trFile.substring(trFile.lastIndexOf('/') + 1); val enFileName = enFile.substring(enFile.lastIndexOf('/') + 1)
    // A Turkish constant with no English counterpart (Görünüş.top1: the collidium images are not in Costume):
    // the target is the value itself -- `Görünüş.top1` -> `"/media/collidium/ball1.png"`. A string
    // constant is valid bare in the API; Resim.imge(Görünüş.top1) is written with the path in English anyway.
    val trSide = trByValue.toSeq.flatMap { case (v, entries) => entries.zipWithIndex.map { case ((name, _, line), i) =>
      Row("val", trObject + "." + name, ordered(v, i, enByValue).map(enObject + "." + _).getOrElse(v), s"$trFileName:$line") } }
    val enSide = enByValue.toSeq.flatMap { case (v, entries) => entries.zipWithIndex.flatMap { case ((name, _, line), i) =>
      ordered(v, i, trByValue).map(t => Row("val", trObject + "." + t, enObject + "." + name, s"$enFileName:$line")) } }
    val baseRows = (trSide ++ enSide).distinct
    baseRows ++ aliases.flatMap(alias => baseRows.map(s => s.copy(tr = alias + s.tr.substring(s.tr.indexOf('.')), note = AliasNote)))
  }

  /** Auxiliary chain links: they do not enter the dictionary, they only serve chain resolution.
    * `def kalemBoyu(b) = KalemBoyuBD(b)`; `case class KalemBoyuBD(...) { def apply(r) = new
    * Resim(picture.StrokeWidth(w)(r.p)) }`; picture/package.scala: `def strokeWidth(w) =
    * StrokeWidthc(w)`. From these the links `KalemBoyuBD -> StrokeWidthc` (Turkish class, via the
    * spelling of the English class name without the `c`) and `StrokeWidthc -> strokeWidth` (English wrapper)
    * come out; the chain reaches `kalemBoyu -> strokeWidth`. In the English API the bare
    * name may be different (penThickness): the compiler probe and the rules decide. */
  private val transformerClassPattern = "case (?:class|object)\\s+([A-Za-z0-9İıŞşĞğÖöÜüÇç_]+)[^{}\\n]*(?:\\n[^{}\\n]*)?\\{\\s*def apply\\(r: Resim\\) = new Resim\\(picture\\.([A-Za-z]+)\\(".r
  private val englishWrapperPattern = "\\n  (?:def|val)\\s+([a-zA-Z]+)(?:\\([^\\n=]*\\))?\\s*=\\s*([A-Z][A-Za-z]*c)\\b".r
  /** `case class Xc(...) extends Composable... { def apply(p: Picture) = Y(...) }` */
  // `case object` and the form without parameters count too: FlipXc/FlipYc/AxesOnc
  // (transforms.scala:318-327) take no parameters and are an `object`. When only `case class` was searched
  // they did not enter the table and the raw inner class name was used (`FlipX`, `AxesOn`) -- both
  // EXIST in the source but cannot be composed, so the phantom counter did not catch them either.
  // Measured: `r * eksenler` did not compile ("not found: value AxesOn"), `r.yansıtX()` went to a
  // member that Picture does not have. For this, a transformer script was added to TranslatorCompileTest.
  private val composableClassPattern =
    "case (?:class|object) ([A-Za-z]+c)(?:\\([^)]*\\))?\\s*extends\\s+Composable[A-Za-z]*\\s*\\{\\s*def apply\\(p: Picture\\) =\\s*([A-Za-z]+)".r

  /** INNER class name -> COMPOSABLE class name (read from the sources in the picture directory).
    *
    * WHY A TABLE, WHY NOT `+ "c"`: the transformer chain sees the inner class from the body in `resim.scala`
    * (`new Resim(picture.StrokeWidth(w)(r.p))`), but the name written in a script
    * is the COMPOSABLE one (`StrokeWidthc`, because `*` and `->` are defined on it).
    * In most cases the two differ by one letter, so a `c` used to be appended. In two places it
    * does NOT work: the composable of `PointLightEffect` is `PointLightc`, that of `SpotLightEffect`
    * is `SpotLightc` -- the body has `Effect`, the class name does not. There `+ "c"` produced a name
    * that does NOT EXIST AT ALL in Kojo (inner class name + `c`); measured, only those two of the 996
    * English names in the TSV had no counterpart in the source. Those names are deliberately NOT written
    * here: the moment they occur in this file they mislead a counter that checks "is it in the
    * source" (exactly this happened during review).
    *
    * The table is built from two directions: the class's own root (`StrokeWidthc` -> `StrokeWidth`) and
    * the name called by the `apply` body (`PointLightc` -> `PointLightEffect`). Those whose body starts
    * with `p.x` (Fillc, Strokec) do not call an inner class, they come from their roots. */
  private val composableCache = scala.collection.mutable.Map.empty[String, Map[String, String]]
  def composables(root: File): Map[String, String] =
    composableCache.getOrElseUpdate(root.getPath, {
      val dir = new File(root, "src/main/scala/net/kogics/kojo/picture")
      val files = Option(dir.listFiles()).getOrElse(Array.empty[File]).filter(_.getName.endsWith(".scala"))
      val pairs = files.toSeq.flatMap { f =>
        composableClassPattern.findAllMatchIn(readFile(f)).toSeq.flatMap { m =>
          val composableName = m.group(1)
          val innerName = m.group(2)
          Seq(composableName.dropRight(1) -> composableName) ++ (if (innerName.length > 1) Seq(innerName -> composableName) else Nil)
        }
      }
      pairs.toMap
    })

  def auxiliaryChains(root: File): Seq[Row] = {
    val pictureFile = new File(root, "src/main/scala/net/kogics/kojo/lite/i18n/tr/resim.scala")
    val packageFile = new File(root, "src/main/scala/net/kogics/kojo/picture/package.scala")
    val wrapperRows = if (packageFile.exists()) englishWrapperPattern.findAllMatchIn(readFile(packageFile)).toSeq.map(m => Row("def", m.group(2), m.group(1), "picture/package.scala")) else Nil
    val classRows = if (pictureFile.exists()) transformerClassPattern.findAllMatchIn(readFile(pictureFile)).toSeq.map { m =>
      Row("class", m.group(1), composables(root).getOrElse(m.group(2), m.group(2)), "resim.scala")
    } else Nil
    classRows ++ wrapperRows
  }

  def extract(root: File): Seq[Row] = {
    val raw = sourceFiles(root).flatMap { f =>
      val relative = f.getPath.stripPrefix(root.getPath).stripPrefix("/")
      extractRaw(relative.substring(relative.lastIndexOf('/') + 1), readFile(f))
    }
    val auxiliary = auxiliaryChains(root)
    val auxiliaryNames = auxiliary.map(_.tr).toSet
    val rules = { val f = new File(root, "src/main/resources" + TranslationDictionary.rulesPath); if (f.exists()) TranslationDictionary.parseRules(readFile(f)) else Nil }
    val ruleTargets: Map[String, String] = rules.collect {
      case k if k.direction == "tr>en" && (k.context == TranslationDictionary.AnyContext || k.context == TranslationDictionary.BareContext) &&
        k.target != TranslationDictionary.LeaveUntouched && !k.target.startsWith(TranslationDictionary.SwallowReceiver) => k.name -> k.target
    }.groupBy(_._1).map { case (name, hs) => name -> hs.head._2 }
    val resolved = (resolveChains(raw ++ auxiliary, ruleTargets).filterNot(s => auxiliaryNames(s.tr)) ++ constantObjects(root)).distinct
    // The compiler as referee: does the English target resolve by its bare name in the Kojo prelude? Rows that do
    // not resolve (members such as x.length, internal names such as SuperFast) are marked "member-only" and drop out of the
    // dictionary in the bare context. This single step eliminates the conflicting preferences of the sources.
    val probes = resolved.map(s => (s.en, s.kind == "type")).distinct
    val resolvedNames = TranslationValidator.resolvedBareNames(probes, turkish = false)
    merge(resolved.map(s => if (resolvedNames(s.en)) s else s.copy(note = (s.notes + MemberOnlyNote).toSeq.sorted.mkString(","))))
  }

  /** During generation `source` carries a line number (which definition? -- needed while extracting);
    * WHEN WRITING the file the number is dropped and identical pairs in the same file collapse into one row.
    * Why: the number changes even when an unrelated insertion is made in the wrapper files and
    * turned the freshness test red, although the dictionary's content is the same. The multiplicity moves to the `count`
    * column, otherwise the frequency table (`ileri` in 3 definitions giving `forward`) would be weighted wrongly. */
  private def stripLineNumber(source: String) = {
    val i = source.lastIndexOf(':')
    if (i < 0 || !source.substring(i + 1).forall(_.isDigit)) source else source.substring(0, i)
  }
  def merge(rows: Seq[Row]): Seq[Row] =
    rows.groupBy(s => (s.kind, s.tr, s.en, stripLineNumber(s.source), s.note)).toSeq
      .map { case ((kind, tr, en, source, note), ss) => Row(kind, tr, en, source, ss.map(_.count).sum, note) }
      .sortBy(s => (s.tr, s.en, s.kind, s.source))

  def tsv(rows: Seq[Row]): String = {
    val header =
      """|# GENERATED FILE -- do not edit by hand. Generate with:
         |#   ./sbt.sh "runMain net.kogics.kojo.lite.i18n.tr.DictionaryGenerator"
         |# Source: the wrappers in lite/i18n/trInit.scala + lite/i18n/tr/*.scala.
         |# For manual decisions: translation-rules.tsv
         |#
         |# kind	tr	en	source	count	[note]
         |#   source: FILE name (deliberately no line number -- so that unrelated shifts do not pollute the dictionary)
         |#   count : number of definitions in that file giving this pair; the weight in the frequency table
         |#   note  : member-only = the English name does not resolve bare, only in the form x.name; alias = alias-receiver row; deprecated = the Turkish definition is deprecated
         |""".stripMargin
    header + rows.map(s => s"${s.kind}\t${s.tr}\t${s.en}\t${s.source}\t${s.count}" + (if (s.note.nonEmpty) "\t" + s.note else "")).mkString("", "\n", "\n")
  }

  def main(args: Array[String]): Unit = {
    val root = new File(args.headOption.getOrElse("."))
    val rows = extract(root)
    println(s"compiler probe: ${rows.count(_.hasNote(TranslationDictionary.MemberOnlyNote))} rows only in member context")
    val outputFile = new File(root, "src/main/resources" + TranslationDictionary.dictionaryPath)
    val old = if (outputFile.exists()) readFile(outputFile) else ""
    val newText = tsv(rows)
    java.nio.file.Files.write(outputFile.toPath, newText.getBytes("UTF-8"))
    val trCount = rows.map(_.tr).distinct.size
    val ambiguousCount = rows.groupBy(_.tr).count(_._2.map(_.en).distinct.size > 1)
    println(s"${rows.size} pairs, $trCount distinct Turkish names, $ambiguousCount with several targets -> ${outputFile.getPath}" +
      (if (old == newText) " (unchanged)" else " (UPDATED)"))
  }
}
