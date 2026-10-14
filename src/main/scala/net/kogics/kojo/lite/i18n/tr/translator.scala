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

import scalariform.lexer.ScalaLexer
import scalariform.lexer.Token
import scalariform.lexer.TokenType
import scalariform.lexer.Tokens

/**
 * Translator: Turkish (Koco) script <-> English (Kojo) script.
 *
 * HOW: the script is tokenised with the patched scalariform; only KEYWORD and
 * IDENTIFIER tokens are changed, everything else (whitespace, comments, strings,
 * numbers) stays as it is. So the output is aligned line by line with the input;
 * comments are not translated (the code is translated, not the prose).
 *
 * WHY NOT AN AST: the translation is name for name; no structural information is
 * needed. The only thing structure would add is resolving ambiguity that depends on
 * the receiver (`sil()` clear, `resim.sil()` erase); the rule "is the previous token
 * a `.`" plus translation-rules.tsv covers that. Measured: 803 of the 861 Turkish
 * names have a single target.
 *
 * WHY NOT REGULAR EXPRESSIONS: in `s"$x ve ${x+1} dez"` the `x` is CODE (must be
 * translated) and `dez` is a STRING (must be left alone). The patched lexer knows
 * this distinction, a regular expression does not; also, the lexer hands us Turkish
 * keywords such as `dez`, `tanım`, `eğer` as keyword TOKENS, so we do not search
 * text, we go by token type.
 *
 * LIMITS (honestly):
 *   - Names that depend on the receiver's TYPE (yazı.boyu -> length, resim.boyu -> height)
 *     cannot be told apart at word level; the most frequent one is taken and written
 *     to Report.ambiguities. The definitive proof is the target language's compiler:
 *     compile the output.
 *   - If a name of the same class has been bound to a single target BY A HAND RULE
 *     (en>tr take -> al), the choice has a single candidate, so it does NOT end up in
 *     Report.ambiguities either. `take` is `al` in most collections (13 definitions),
 *     but `alSırayla` in Eşlem/Eşlek (where `al` = get). On a String-keyed map the
 *     wrong choice is loud (type mismatch, does not compile); on a NUMBER-keyed one it
 *     is silent: `Eşlek(1->"bir",...).al(2)` compiles but means `get`, not `take`. If
 *     you translate `take` on a map, check the output by eye.
 *   - If the user's own names collide with a name in the dictionary (dez renk = 3) they
 *     are translated consistently (definition and use together); a working program
 *     stays a working program.
 *   - Meaning embedded in an argument value (göster() = setVisible(true)) cannot be carried over.
 *   - Names that are not in the dictionary are left as they are and counted in
 *     Report.untouchedNames: both the user's own names and the dictionary's gaps are there.
 */
object Translator {
  sealed trait Direction
  case object TurkishToEnglish extends Direction
  case object EnglishToTurkish extends Direction

  final case class Ambiguity(name: String, chosen: String, alternatives: Seq[String], line: Int)

  final case class Report(translatedCount: Int, ambiguities: Seq[Ambiguity], untouchedNames: Map[String, Int],
                          knownToDictionary: Set[String] = Set.empty) {
    /** Names that are likely to have stayed in the source language: those with Turkish letters AND those the
      * dictionary recognises as Turkish but deliberately did not translate (ASCII spellings such as karesi are caught this way too). */
    def remainingTurkishNames: Map[String, Int] = untouchedNames.filter { case (name, _) => name.exists(isTurkishLetter) || knownToDictionary(name) }
    def summary(direction: Direction): String = {
      val sb = new StringBuilder
      sb.append(s"translated names: $translatedCount\n")
      if (ambiguities.nonEmpty) {
        sb.append(s"ambiguous choices (${ambiguities.size}) -- the most frequent was taken, verify by compiling:\n")
        ambiguities.foreach(b => sb.append(f"  line ${b.line}%4d  ${b.name} -> ${b.chosen}   (others: ${b.alternatives.mkString(", ")})\n"))
      }
      val suspects = direction match { case TurkishToEnglish => remainingTurkishNames; case EnglishToTurkish => Map.empty[String, Int] }
      if (suspects.nonEmpty) {
        sb.append(s"Turkish names left (${suspects.size}) -- not in the dictionary; either the user's own names or gaps in the dictionary:\n")
        suspects.toSeq.sortBy { case (name, n) => (-n, name) }.foreach { case (name, n) => sb.append(f"  $name ($n)\n") }
      }
      val otherUntouchedCount = untouchedNames.size - suspects.size
      if (otherUntouchedCount > 0) sb.append(s"other untouched names: $otherUntouchedCount\n")
      sb.toString
    }
  }

  private val turkishLetters = "çğıöşüÇĞİÖŞÜ".toSet
  private def isTurkishLetter(c: Char) = turkishLetters(c)

  lazy val dictionary: TranslationDictionary.Dictionary = TranslationDictionary.load()

  /**
   * Keyword table: token type -> (English, Turkish). Built from dict.keywordTranslation,
   * but BOTH spellings of every pair are run through the lexer and must give the same
   * token type; the ones that do not (the Scala 3 words scalariform does not know:
   * given/verilen) stay out of the table and are translated from translation-rules.tsv.
   *
   * The criterion is NOT `isKeyword`: scalariform counts true/false/null as constants
   * (isKeyword=false, isLiteral=true); filtering with it left doğru/yanlış/yok
   * untranslated (measured: 36 pairs instead of 39). The criterion is: both spellings
   * go to the same token type, which is NOT an identifier.
   */
  lazy val keywordTable: Map[TokenType, (String, String)] = {
    def tokenTypeOf(w: String): Option[TokenType] =
      ScalaLexer.rawTokenise(w + " ", forgiveErrors = true, scalaVersion = "2.13.0").headOption
        .map(_.tokenType).filterNot(tt => tt.isId || tt == Tokens.EOF)
    dict.keywordTranslation.toSeq.flatMap { case (en, tr) =>
      (tokenTypeOf(en), tokenTypeOf(tr)) match {
        case (Some(te), Some(tt)) if te == tt => Some(te -> (en, tr))
        case _                                => None
      }
    }.toMap
  }

  private val identifierPattern = "^[\\p{L}_][\\p{L}\\p{N}_]*$".r
  private lazy val turkishKeywordSet: Set[String] = dict.turkishKeywords.toSet
  private val definitionKeywords: Set[TokenType] = Set(Tokens.DEF, Tokens.VAL, Tokens.VAR, Tokens.CLASS, Tokens.OBJECT, Tokens.TYPE, Tokens.TRAIT)

  def translateScript(code: String, direction: Direction, dictionary: TranslationDictionary.Dictionary = dictionary): (String, Report) = {
    val ts = ScalaLexer.rawTokenise(code, forgiveErrors = true, scalaVersion = "2.13.0")
    val lineStarts = 0 +: code.zipWithIndex.collect { case ('\n', i) => i + 1 }
    def lineNumber(t: Token) = lineStarts.count(_ <= t.offset)

    val output = new StringBuilder(code.length + code.length / 8)
    var translatedCount = 0
    val ambiguities = Vector.newBuilder[Ambiguity]
    val untouchedNames = scala.collection.mutable.Map.empty[String, Int].withDefaultValue(0)
    val tokens = ts.toVector
    def isSignificant(t: Token) = t.tokenType != Tokens.WS && !t.tokenType.isComment && !t.tokenType.isNewline && t.tokenType != Tokens.EOF
    def nextSignificant(i: Int): Option[Int] = (i + 1 until tokens.length).find(j => isSignificant(tokens(j)))
    def isIdentifier(t: Token) = t.tokenType.isId && identifierPattern.matches(t.text)
    val directionCode = direction match { case TurkishToEnglish => "tr>en"; case EnglishToTurkish => "en>tr" }
    def lookup(name: String, context: String) = direction match {
      case TurkishToEnglish => dictionary.lookupTurkishToEnglish(name, context)
      case EnglishToTurkish => dictionary.lookupEnglishToTurkish(name, context)
    }
    // Names the script defines ITSELF: those declared with def/val/var/class/object/type,
    // `name:` parameters, and the `x =>` / `(x, y) =>` lambda parameters. These are
    // translated consistently, but always as BARE names: at the definition site
    // `Picture.image` used to be shortened, while the use site became
    // `Picture.image(Picture.image, ...)` (measured: car-ride.kojo, genart-tri-mesh.kojo).
    val userNames: Set[String] = {
      val significantIndices = tokens.indices.filter(j => isSignificant(tokens(j)))
      val nameSet = Set.newBuilder[String]
      for (k <- significantIndices.indices) {
        val j = significantIndices(k); val t = tokens(j)
        def nextType = if (k + 1 < significantIndices.length) Some(tokens(significantIndices(k + 1)).tokenType) else None
        def previousType = if (k > 0) Some(tokens(significantIndices(k - 1)).tokenType) else None
        if (isIdentifier(t) && (previousType.exists(definitionKeywords) || nextType.contains(Tokens.COLON) || nextType.contains(Tokens.ARROW))) nameSet += t.text
        // `(a, b) =>`: the names inside the parentheses
        if (t.tokenType == Tokens.LPAREN) {
          var m = k + 1; var names = List.empty[String]; var wellFormed = true
          while (wellFormed && m < significantIndices.length && tokens(significantIndices(m)).tokenType != Tokens.RPAREN) {
            val u = tokens(significantIndices(m))
            if (isIdentifier(u)) names ::= u.text else if (u.tokenType != Tokens.COMMA) wellFormed = false
            m += 1
          }
          if (wellFormed && m + 1 < significantIndices.length && tokens(significantIndices(m + 1)).tokenType == Tokens.ARROW) names.foreach(nameSet += _)
        }
      }
      nameSet.result()
    }
    // Variables of type TuvalÇizim (TR->EN only): the declarations `canvas: TuvalÇizim` and the
    // lambda parameters of `canlandırTuvalÇizimle { canvas => ... }` (and of both blocks of the
    // setup variant). Why: `tuval.nokta(x, y)` -> point, `GeoNokta.nokta` -> vertex, both are
    // `nokta`; at word level the receiver's type is unknown and the most frequent one (vertex)
    // was taken -- the script compiled, but crashed when run, for lack of `beginShape`. If the
    // receiver is in this set, the member lookup is done with the virtual receiver `TuvalÇizim.`
    // (translation-rules.tsv: `nokta  TuvalÇizim.  point`).
    // A use without a declaration (a canvas passed on to another function) is not known; in that
    // case the old receiver-less behaviour applies.
    val canvasVariables: Set[String] =
      if (direction != TurkishToEnglish) Set.empty
      else {
        val sig = tokens.indices.filter(j => isSignificant(tokens(j))).toVector
        val nameSet = Set.newBuilder[String]
        def typeAt(k: Int) = tokens(sig(k)).tokenType
        def textAt(k: Int) = tokens(sig(k)).text
        for (k <- sig.indices) {
          // `name: TuvalÇizim`
          if (k >= 2 && textAt(k) == "TuvalÇizim" && typeAt(k - 1) == Tokens.COLON && isIdentifier(tokens(sig(k - 2)))) nameSet += textAt(k - 2)
          // `canlandırTuvalÇizimle { name => ...}`; in the setup variant the second block too
          if (textAt(k) == "canlandırTuvalÇizimle" || textAt(k) == "canlandırKurulumluTuvalÇizimle") {
            // take the lambda parameter of the block at m, return the position after the matching closer
            def scanBlock(m: Int): Int =
              if (m + 2 < sig.length && (typeAt(m) == Tokens.LBRACE || typeAt(m) == Tokens.LPAREN)) {
                if (isIdentifier(tokens(sig(m + 1))) && typeAt(m + 2) == Tokens.ARROW) nameSet += textAt(m + 1)
                val opener = typeAt(m); val closer = if (opener == Tokens.LBRACE) Tokens.RBRACE else Tokens.RPAREN
                var depth = 0; var n = m
                do {
                  if (typeAt(n) == opener) depth += 1 else if (typeAt(n) == closer) depth -= 1
                  n += 1
                } while (depth > 0 && n < sig.length)
                n
              } else sig.length
            val afterFirstBlock = scanBlock(k + 1)
            if (textAt(k) == "canlandırKurulumluTuvalÇizimle") scanBlock(afterFirstBlock)
          }
        }
        nameSet.result()
      }
    var previous: Option[Token] = None // the previous token that is not whitespace/comment
    var beforePrevious: Option[Token] = None
    var swallowed = Set.empty[Int]     // tokens swallowed when receiver+dot+name are replaced together
    // `collection.mutable.Map`, `java.awt.Color`: members of a package path are not translated --
    // there is no such thing as `collection.mutable.Eşlek` (measured: car-ride.kojo). If the
    // root of the chain is a known package, leave the chain alone until it ends. Exception:
    // `math.` is translated.
    val packageRoots = Set("scala", "java", "javax", "collection", "util", "net", "org", "com", "sun")
    // Java/Scala class objects: their members are not translated in either language
    // (`System.currentTimeMillis`, `Math.floor`, `SwingConstants.CENTER`, `Stream.from`,
    // `util.Random.shuffle`). Looking the member name up in the dictionary gave
    // `Math.taban`, `SwingConstants.merkez` (measured: genart-mondrian, subtraction-game,
    // prime-factors). Character is left out: the Harf object is reached through a rule.
    val untranslatedReceivers = Set("System", "Math", "SwingConstants", "BorderFactory", "Random", "Stream", "LazyList",
      "Integer", "Double", "Long", "Thread", "Runtime", "Files", "Paths")
    var inPackagePath = false
    // `^Resim.dikdörtgen(2,1)`: the argument list after a target with a receiver is reordered --
    // Picture.rect(height, width) = Resim.dikdörtgen(width, height). The output between the
    // parentheses is captured piece by piece (split at top-level commas) and written in the
    // given order at the closing parenthesis.
    final class Capture(val closingIndex: Int, val commaIndices: Set[Int], val order: Seq[String]) {
      val parts = scala.collection.mutable.ArrayBuffer(new StringBuilder)
    }
    var capture: Option[Capture] = None
    var pendingCapture: Option[Capture] = None // starts AFTER the target is written; otherwise the target went into the first part
    // Argument list in the target: `Resim.dikdörtgen(2,1)` reorders the arguments,
    // `math.pow(1,'2)` writes a CONSTANT for the second one. If the entry is a digit it is
    // the position of the argument (from 1); if it starts with `'` the rest is printed as it
    // is. When a wrapper adds an argument (karesi(x) = math.pow(x, 2)) this is the only way.
    val targetWithArguments = "^(.*)\\(((?:\\d+|'[^,()]*)(?:,(?:\\d+|'[^,()]*))*)\\)$".r
    def isArgumentIndex(entry: String) = entry.nonEmpty && entry.forall(_.isDigit)
    /** From the `(` token to the matching `)`: (index of the closer, indices of the top-level commas). */
    def argumentList(openIndex: Int): Option[(Int, Set[Int])] = {
      var d = 0; var k = openIndex; val commaIndices = Set.newBuilder[Int]
      while (k < tokens.length) {
        tokens(k).tokenType match {
          case Tokens.LPAREN | Tokens.LBRACKET | Tokens.LBRACE => d += 1
          case Tokens.RPAREN | Tokens.RBRACKET | Tokens.RBRACE => d -= 1; if (d == 0) return Some((k, commaIndices.result()))
          case Tokens.COMMA if d == 1                          => commaIndices += k
          case _                                               =>
        }
        k += 1
      }
      None
    }
    /** Applies the argument list in the target: the `(` ... `)` after it is captured and
      * rewritten at the closing parenthesis according to the entries. If the number of
      * arguments does not match nothing is done (the name is still translated, the list
      * stays as it is). NOT called in a definition context: there the parentheses hold the
      * parameter list, not arguments. */
    def setUpCapture(order: String, lastTokenIndex: Int): Unit = {
      val orderEntries = order.split(',').toSeq
      val openParen = nextSignificant(lastTokenIndex).filter(p => tokens(p).tokenType == Tokens.LPAREN)
      openParen.flatMap(argumentList) match {
        case Some((closingIndex, commaIndices)) if commaIndices.size + 1 == orderEntries.count(isArgumentIndex) =>
          swallowed = swallowed + openParen.get; pendingCapture = Some(new Capture(closingIndex, commaIndices, orderEntries))
        case _ => // the number of arguments does not match: translate the name, leave the list alone
      }
    }
    /** Splits the target into (name, argument list). */
    def splitTarget(target: String): (String, Option[String]) = target match {
      case targetWithArguments(name, order) => (name, Some(order))
      case _                                => (target, None)
    }
    // The user's own name (`case class Rectangle(width: Double)` / `r.width`): the definition
    // site was looked up as bare and the use site in the member context, so the two ended up in
    // different places (width stayed at the definition, became eni at the use -- measured:
    // genart-mondrian, genart-tiled-lines).
    // A user name that has a counterpart in the dictionary in ONLY ONE context goes to that
    // counterpart everywhere. A name that has a counterpart in both contexts (yazı, boyu) is
    // translated according to its context: the user's name collides with a script-library name,
    // and forcing a single counterpart breaks the library call (measured: tic-tac-toe
    // Resim.yazı -> Picture.write, tiled-lines ta.boyu -> ta.size).
    // A name with ONLY a MEMBER counterpart is translated only if the script really uses it as a
    // MEMBER (`r.width`). `dez merkez = ...; çiz(merkez)`, which never comes after a dot, is the
    // user's own variable: the dictionary's `merkez` is only the member `ay.değişmez.merkez`, a
    // bare `merkez` cannot refer to it. Before, `merkez` -> `CENTER`, `zaman` -> `hepsi` happened
    // (consistent, compiles, but unreadable; measured: the unit circle script).
    // TR->EN ONLY: in EN->TR the same filter broke 8 scripts (height, last, partition, koy: the
    // definition site was translated, the bare use site stayed -- measured: TranslatorCompileTest),
    // so the old, consistent behaviour continues there.
    val namesUsedAsMembers: Set[String] = {
      val significantTokens = tokens.filter(isSignificant)
      significantTokens.zip(significantTokens.drop(1)).collect { case (dot, nameToken) if dot.tokenType == Tokens.DOT && isIdentifier(nameToken) => nameToken.text }.toSet
    }
    lazy val userTranslations: Map[String, TranslationDictionary.Choice] = userNames.iterator.flatMap { name =>
      (lookup(name, TranslationDictionary.BareContext), lookup(name, TranslationDictionary.MemberContext)) match {
        case (Some(bare), None) => Some(name -> bare)
        case (None, Some(member)) if direction != TurkishToEnglish || namesUsedAsMembers(name) => Some(name -> member)
        case _                  => None
      }
    }.toMap

    for (i <- tokens.indices) {
      val t = tokens(i)
      val tt = t.tokenType
      val replacement: String =
        if (swallowed(i)) ""
        else if (keywordTable.contains(tt)) {
          val (en, tr) = keywordTable(tt)
          direction match {
            case TurkishToEnglish => en
            // `val den = 3` (denominator): a NAME for the English writer, but the `var` keyword for
            // the patched lexer. In an English script a keyword token with a Turkish spelling can
            // only be the author's identifier; backticks are Scala's solution. Left as it was,
            // the translation did not parse (measured: solving-linear-equations).
            case EnglishToTurkish => if (t.text == tr) s"`$tr`" else tr
          }
        }
        else if (tt.isId && !identifierPattern.matches(t.text) && lookup(t.text, TranslationDictionary.AnyContext).isDefined) {
          // `1 |-| 600` -> `1 to 600`: operator names are translated only from the rule file; the
          // dictionary tables are built from names with letters, operators such as `+` never get there.
          translatedCount += 1; lookup(t.text, TranslationDictionary.AnyContext).get.target
        }
        else if (isIdentifier(t)) {
          // `Resim.dizi(...)`: a member of the Turkish object is a bare function in English (picStack).
          // If a receiver-specific rule (the `Resim.` context) gives the target `^picStack`, the
          // receiver and the dot are swallowed, and three tokens come down to one name. We look
          // ahead: we look at the member before writing the receiver, there is no taking back.
          val receiverTarget = for {
            n <- nextSignificant(i) if tokens(n).tokenType == Tokens.DOT
            m <- nextSignificant(n) if isIdentifier(tokens(m))
            s <- lookup(tokens(m).text, t.text + ".") if s.target.startsWith(TranslationDictionary.SwallowReceiver)
          } yield (n, m, s.target.drop(1))
          val isMember = previous.exists(_.tokenType == Tokens.DOT)
          val memberPosition = nextSignificant(i).filter(n => tokens(n).tokenType == Tokens.DOT).flatMap(nextSignificant).filter(m => isIdentifier(tokens(m)))
          val isReceiver = !isMember && memberPosition.isDefined
          // `ColorMaker.khaki` (a colour name) and `ColorMaker.hsla(...)` (a constructor call) lead the
          // same receiver to two different objects in Turkish (Renkler / Renk). The distinguishing
          // feature: is the member called with parentheses. Context `receiver(` = the receiver of a called member.
          val isReceiverOfCall = isReceiver && memberPosition.flatMap(nextSignificant).exists(p => tokens(p).tokenType == Tokens.LPAREN)
          if (!isMember) inPackagePath = (packageRoots(t.text) || untranslatedReceivers(t.text)) && isReceiver
          // EN->TR: `ColorMaker.blueViolet` -- the receiver is translated (Renkler); if the member
          // has no Turkish form there is no such thing as `Renkler.blueViolet`. Koco accepts English
          // names as they are; do not touch a receiver whose member cannot be translated (measured:
          // flappy-ball, pong, subtraction-game).
          // Meaningless in TR->EN: both the receiver and the member must be English, leaving it does not save anything.
          // Even if the user's own name (`val square = ...; square.x`) is a receiver, the translation
          // at its definition site must be kept; otherwise `kare` was defined and `square.x` stayed
          // (measured: genart-mondrian).
          val untranslatableReceiver = direction == EnglishToTurkish && isReceiver && !userNames(t.text) && memberPosition.exists { m =>
            val memberName = tokens(m).text
            dictionary.isLeftUntouched(directionCode, memberName, t.text + ".") ||
              (lookup(memberName, t.text + ".").isEmpty && lookup(memberName, TranslationDictionary.MemberContext).isEmpty)
          }
          if (inPackagePath || untranslatableReceiver) { untouchedNames(t.text) += 1; t.rawText }
          else receiverTarget match {
            case Some((n, m, target)) =>
              swallowed = swallowed + n + m; translatedCount += 1
              val (name, order) = splitTarget(target)
              order.foreach(setUpCapture(_, m))
              name
            case None =>
              // Order: the plain rule specific to the receiver (`tuvalAlanı.` -> height), then the receiver/member/bare context.
              val receiverName = if (isMember) beforePrevious.filter(isIdentifier).map(_.text + ".") else None
              // If the receiver is a TuvalÇizim, the virtual `TuvalÇizim.` receiver is looked up first (see canvasVariables).
              val isCanvasReceiver = isMember && beforePrevious.exists(a => canvasVariables(a.text))
              // `r.saydamlık(0.5)` and `r.saydamlık` go to two different English methods of
              // different arity (opacityMod / opacity); if we do not tell them apart by context, a
              // rule at name level cannot distinguish the two (issue #75). It is searched ON THE
              // SAME LINE: nextSignificant skips the end of the line, so when `r.saydamlık` was at
              // the end of a line, a NEXT line that begins with `(` would have been taken for a call.
              val isMemberCall = isMember && nextSignificant(i).exists { n =>
                tokens(n).tokenType == Tokens.LPAREN && lineNumber(tokens(n)) == lineNumber(t)
              }
              val context =
                if (isMemberCall) TranslationDictionary.MemberCallContext
                else if (isMember) TranslationDictionary.MemberContext
                else if (isReceiverOfCall) TranslationDictionary.ReceiverCallContext
                else if (isReceiver) TranslationDictionary.ReceiverContext
                else TranslationDictionary.BareContext
              // `tanım dereceye(k) = ...`, `tanım araba(imge: Yazı)`: the defined name cannot be
              // qualified (`def math.toDegrees`, `araba(Picture.image: String)` do not parse --
              // measured: angles.kojo, car-ride.kojo). If def/val/... comes before it or `:` after
              // it, it is a definition context; the last part of the target is taken.
              val isDefinitionContext = userNames(t.text) || previous.exists(p => definitionKeywords(p.tokenType)) ||
                nextSignificant(i).exists(n => tokens(n).tokenType == Tokens.COLON)
              // The "-" rule for `SwingConstants.CENTER`: a receiver-specific "do not translate" does
              // not fall through to the member context (when it did, we got `SwingConstants.merkez` --
              // measured: subtraction-game).
              val isReceiverSpecificUntouched = receiverName.exists(a => dictionary.isLeftUntouched(directionCode, t.text, a))
              val isUserDefined = userTranslations.contains(t.text)
              // `engeller herbiriİçin { ... }`, `xs foreach f`: a postfix call, no dot before it but a member.
              // A name with no bare counterpart is also looked up in the member context if the token
              // before it is a value (a name, a constant, `)`, `]`) (measured: flappy-ball.kojo).
              val isPostfixPosition = !isMember && !isReceiver && previous.exists(p => p.tokenType.isId || p.tokenType.isLiteral ||
                p.tokenType == Tokens.RPAREN || p.tokenType == Tokens.RBRACKET)
              val choice =
                if (isUserDefined) Some(userTranslations(t.text))
                // An explicit "-" rule (vertex bare, setColumns bare) also cuts off the postfix fall-through (measured: tree2).
                else if (isReceiverSpecificUntouched || dictionary.isLeftUntouched(directionCode, t.text, context)) None
                else (if (isCanvasReceiver) lookup(t.text, "TuvalÇizim.") else None)
                  .orElse(receiverName.flatMap(a => lookup(t.text, a))).orElse(lookup(t.text, context))
                  // The user's own name (TR->EN) is not taken for a postfix and does not look in the member table: `zaman += 1`,
                  // even if the token before it is a value, `zaman` -> `hepsi` must not happen (it stays at the definition, which would be inconsistent).
                  .orElse(if (isPostfixPosition && !(direction == TurkishToEnglish && userNames(t.text))) lookup(t.text, TranslationDictionary.MemberContext) else None)
              choice match {
                case Some(s) =>
                  translatedCount += 1
                  if (!s.isDefinite) ambiguities += Ambiguity(t.text, s.target, s.alternatives, lineNumber(t))
                  // `biçimleriBelleğeYaz()` -> `saveStyle`: the English name has no parentheses
                  // (def saveStyle: Unit), in TR->EN the empty `()` after it is swallowed. In the
                  // opposite direction `()` is NOT ADDED: in Scala 2, writing `yazı` instead of
                  // `yazı()` is valid, but since Java methods such as `toString` are also marked
                  // "no parens", adding them broke parameter lists (measured: genart-tri-mesh.kojo,
                  // "not a legal formal parameter").
                  val emptyParens = for { n <- nextSignificant(i) if tokens(n).tokenType == Tokens.LPAREN
                                          m <- nextSignificant(n) if tokens(m).tokenType == Tokens.RPAREN } yield (n, m)
                  if (s.noParens && !isDefinitionContext && direction == TurkishToEnglish) emptyParens.foreach { case (n, m) => swallowed = swallowed + n + m }
                  val parenSuffix = ""
                  // Split the name from the argument list first: the constant may contain a dot ('0.5);
                  // if we do not split before taking the last part, the substring would fall inside the list.
                  val (targetName, argumentOrder) = splitTarget(s.target)
                  // In a definition context the parentheses are the PARAMETER list; if a capture were
                  // set up, `tanım karesi(x: Kesir)` would become `def pow(x: Kesir, 2)`.
                  if (!isDefinitionContext) argumentOrder.foreach(setUpCapture(_, i))
                  // In the member context too: `Matematik.karekökü` -> the receiver is already `math`, the member is `sqrt`.
                  (if (isDefinitionContext || isMember) targetName.substring(targetName.lastIndexOf('.') + 1) else targetName) + parenSuffix
                case None =>
                  untouchedNames(t.text) += 1
                  // A Turkish word that the lexer does not count as a keyword (verilen) may also have
                  // been used as a name; that goes into backticks too.
                  if (direction == EnglishToTurkish && turkishKeywordSet(t.text)) s"`${t.text}`" else t.rawText
              }
          }
        }
        else t.rawText
      capture match {
        case Some(cap) if i == cap.closingIndex =>
          val parts = cap.parts.map(_.toString.trim)
          val written = cap.order.map(entry => if (isArgumentIndex(entry)) parts(entry.toInt - 1) else entry.substring(1))
          output.append("(").append(written.mkString(", ")).append(")"); capture = None
        case Some(cap) if cap.commaIndices(i) => cap.parts += new StringBuilder
        case Some(cap)                        => cap.parts.last.append(replacement)
        case None                             => output.append(replacement)
      }
      if (pendingCapture.isDefined) { capture = pendingCapture; pendingCapture = None }
      if (isSignificant(t)) { beforePrevious = previous; previous = Some(t) }
    }
    val knownUntouched = direction match {
      case TurkishToEnglish => untouchedNames.keySet.filter(dictionary.turkishNames).toSet
      case EnglishToTurkish => Set.empty[String]
    }
    (output.toString, Report(translatedCount, ambiguities.result(), untouchedNames.toMap, knownUntouched))
  }

  def translateTurkishToEnglish(code: String): (String, Report) = translateScript(code, TurkishToEnglish)
  def translateEnglishToTurkish(code: String): (String, Report) = translateScript(code, EnglishToTurkish)

  /** Has a keyword of the source language been left in the script? The cheapest check of a translation. */
  def leftoverKeywords(code: String, direction: Direction): Seq[String] = {
    val forbidden: Set[String] = direction match {
      case TurkishToEnglish => keywordTable.values.map(_._2).toSet
      case EnglishToTurkish => keywordTable.values.map(_._1).toSet
    }
    ScalaLexer.rawTokenise(code, forgiveErrors = true, scalaVersion = "2.13.0")
      .filter(t => keywordTable.contains(t.tokenType) && forbidden(t.text)).map(_.text).distinct
  }
}
