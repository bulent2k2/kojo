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

/**
 * Translation dictionary: Turkish (Koco) name <-> English (Kojo) name.
 *
 * CONSISTS OF TWO FILES, both in the resources directory (src/main/resources/i18n/tr/):
 *
 *   translation-dictionary.tsv  GENERATED. Never edited by hand. DictionaryGenerator reads
 *                        the Turkish wrapper sources (trInit.scala + the .scala files in the tr
 *                        directory) and extracts the pairs from one-line forwarders such as
 *                        `def ileri(adım) = englishTurtle.forward(adım)`.
 *                        The wrappers THEMSELVES are the dictionary; we do not
 *                        keep a second copy. Its freshness is checked by TranslatorTest.
 *
 *   translation-rules.tsv  BY HAND. The places the generated dictionary cannot resolve on its own:
 *                        a Turkish name that maps to a different English name depending on the
 *                        receiver (`sil()` clear, `resim.sil()` erase), which Turkish spelling is
 *                        canonical in the reverse direction, object names that have no
 *                        wrapper chain (Resim <-> Picture), and the
 *                        "do not translate" exceptions.
 *
 * WHY SEPARATE: the generated file is mechanical and large; the rules file is small and every
 * line is a deliberate decision. Mixing them loses track of which line is there and why
 * (see the same rationale in kojojs-dev/araclar/adlar.py).
 *
 * TSV format (tab-separated, lines starting with `#` are comments):
 *   dictionary: kind  tr  en  source          kind: def|val|var|type|class|object
 *   rules:      direction  name  context  target  note   direction: tr>en|en>tr, context: bare|member|*,
 *                                          target "-" means "do not translate this name"
 */
object TranslationDictionary {
  /** A row of the dictionary. `source` is a FILE name, it carries no line number: with a line
    * number, an unrelated line shift in the wrapper files turned the freshness test red
    * (measured: #60, then again when #58/#61 reached master; both times the only change was a
    * line number). `count` is the number of definitions in that file that give the same pair:
    * the frequency table looks at HOW MANY definitions go to a target, and since we only
    * record the file level, that information is written to a column so that it is not lost. */
  final case class Row(kind: String, tr: String, en: String, source: String, count: Int = 1, note: String = "") {
    /** `note` is a SET OF TAGS: zero or more tags separated by commas ("member-only", "deprecated",
      * "deprecated,member-only"). The assumption that it holds a single value was broken in #63: a
      * deprecated name can also be member-only. Turning the tag into a set, instead of adding a column,
      * leaves the TSV schema (`kind tr en source count [note]`) as it was. */
    def notes: Set[String] = if (note.isEmpty) Set.empty else note.split(',').iterator.map(_.trim).filter(_.nonEmpty).toSet
    def hasNote(tag: String): Boolean = notes(tag)
  }
  /** Tag in the `note` column: the English name does not resolve bare in the Kojo prelude (a member
    * such as x.length, or an inner name); it is used only in a member context. The generator marks
    * it by asking the compiler. */
  val MemberOnlyNote = "member-only"
  /** The definition has `@deprecated` on it. In EN->TR, a deprecated Turkish name loses to its
    * non-deprecated sibling when the votes are equal (see candidates). In TR->EN nothing changes:
    * an old Koco script that says `sil_geri` must still be translatable -- so this is a RANKING
    * criterion, not a filter (#63). */
  val DeprecatedNote = "deprecated"
  /** Temporary note inside the generator: the definition body is a `{ ... }` block, and the target is
    * the chain of the first statement. Chain resolution does not go through Turkish wrapper names
    * (see resolveChains). */
  val BlockBodyNote = "block-body"
  /** If it appears in the `note` column of a rule: the English name takes NO parentheses at script
    * level (the `def saveStyle = ...` forwarder of TurtleWorldAPI) but the Turkish wrapper is called
    * with `()`; in TR->EN the `()` is swallowed. The generator CANNOT DETECT this -- the wrapper calls
    * `englishTurtle.saveStyle()` with parentheses (parenthesized in Turtle); the script-level name is a
    * separate definition. A job for a rule. */
  val NoParensNote = "no-parens"
  final case class Rule(direction: String, name: String, context: String, target: String, note: String)

  /** The target chosen for a name. If `alternatives` is non-empty the choice was ambiguous: the most
    * frequently seen target was taken, the others are reported. */
  final case class Choice(target: String, alternatives: Seq[String], noParens: Boolean = false) {
    def isDefinite: Boolean = alternatives.isEmpty
  }

  val dictionaryPath = "/i18n/tr/translation-dictionary.tsv"
  val rulesPath = "/i18n/tr/translation-rules.tsv"

  val BareContext = "bare"     // no `.` before it:  sil()
  val MemberContext = "member" // there is a `.` before it:  resim.sil()
  val AnyContext = "*"
  val LeaveUntouched = "-"
  /** Tag "alias": the row is for an ALIAS receiver (`Çizim.x`, Çizim = Görünüş). It enters only the
    * TR->EN lookup with a receiver; it is not an EN->TR candidate, otherwise it comes out as
    * "ambiguous" with an equal vote against the original name. */
  val AliasNote = "alias"
  /** If the target starts with this tag, the receiver and the dot are swallowed too: `Resim.dizi` -> `picStack`.
    * The context of such a rule is the name of the receiver, with a trailing dot: `Resim.` */
  val SwallowReceiver = "^"
  /** Context "receiver": the name is the receiver of a member (the Color in `Color.khaki`). In Turkish
    * the colors are in the `Renkler` object while the type name is `Renk`: the same English name goes to two places. */
  val ReceiverContext = "receiver"
  /** Context "receiver(": the receiver of a member that is being called (the ColorMaker in `ColorMaker.hsla(...)`).
    * Without a rule it falls to "receiver", and without that to "*". */
  val ReceiverCallContext = "receiver("
  /**
   * Context "member(": a member called WITH AN ARGUMENT LIST (`r.saydamlık(0.5)`), distinct from its
   * use without arguments (`r.saydamlık`). Without a rule it falls to "member", and without that to "*".
   *
   * WHY IT EXISTS: a Turkish name can go to two English methods with different arity, and a
   * name-level rule could not tell them apart. The measured example is `saydamlık`
   * (resim.scala:261 with an argument -> opacityMod, :308 without an argument -> opacity): all three candidates are
   * n=1, the decision was left to alphabetical order and `opac` won -- a row generated for a bare
   * scripting command that is not a member of Resim. Writing a single `member` rule broke half of it,
   * measured (issue #75):
   *   member opacityMod -> `r.saydamlık` broken ("missing argument list")
   *   member opacity    -> `r.saydamlık(0.5)` broken ("Double does not take parameters")
   */
  val MemberCallContext = "member("

  private def records(tsv: String): Iterator[Array[String]] =
    tsv.linesIterator.filterNot(l => l.trim.isEmpty || l.startsWith("#")).map(_.split('\t'))

  private def parseCount(field: String, record: Array[String]): Int =
    field.toIntOption.filter(_ > 0).getOrElse(sys.error(s"translation-dictionary.tsv: column 5 (count) must be a positive integer: ${record.mkString("|")}"))

  def parseRows(tsv: String): Seq[Row] = records(tsv).map { a => a match {
    case Array(kind, tr, en, source, count, note, _*) => Row(kind, tr, en, source, parseCount(count, a), note)
    case Array(kind, tr, en, source, count)           => Row(kind, tr, en, source, parseCount(count, a))
    // A missing column is an ERROR: `count` carries the frequency weight, and silently assuming 1 would
    // quietly weight the row wrongly (review #65). A defensive default is wrong on a load-bearing field.
    case _ => sys.error(s"translation-dictionary.tsv: malformed row (expected: kind tr en source count [note]): ${a.mkString("|")}")
  }}.toVector

  def parseRules(tsv: String): Seq[Rule] = records(tsv).map {
    case Array(direction, name, context, target, note, _*) => Rule(direction, name, context, target, note)
    case Array(direction, name, context, target)           => Rule(direction, name, context, target, "")
    case a                                                 => sys.error(s"translation-rules.tsv: malformed row: ${a.mkString("|")}")
  }.toVector

  def load(): Dictionary =
    new Dictionary(parseRows(readResource(dictionaryPath)), parseRules(readResource(rulesPath)))

  // Same as Utils.loadResource (UTF-8), but without tying it to the rest of Kojo: the translator
  // (Translator + TranslationDictionary + dict) is a library that depends only on scalariform, and it
  // will also run on the iKojo server (kojojs-dev#183). If a dependency creeps in, TranslatorLibraryTest breaks.
  private def readResource(path: String): String = {
    val stream = getClass.getResourceAsStream(path)
    require(stream != null, s"resource not found: $path")
    // Source, NOT InputStream.readAllBytes: that arrived in Java 9, and Kojo also targets Java 8
    try scala.io.Source.fromInputStream(stream, "UTF-8").mkString
    finally stream.close()
  }

  class Dictionary(val rows: Seq[Row], val rules: Seq[Rule]) {
    // Candidates are ordered by frequency: if a Turkish name goes to `min` in 22 places and to
    // `MaxValue` in 1, `min` comes first. On a tie the WRAPPER row beats the data.scala table -- the table
    // is hand-written and partly stale (it says çokHızlı=SuperFast, kosinüs=cos; the wrappers
    // say superFast and math.cos; measured). Last comes the name order (stability).
    // The fourth field is the row's `count`: in the source column, which has dropped to the file level, the
    // multiplicity lives there, and `ileri -> forward` on a single row can mean 3 definitions. Instead of COUNTING rows, the count is SUMMED.
    // Fifth field: whether the TARGET is deprecated (#63). It is consulted AFTER frequency and BEFORE the wrapper:
    // on an equal vote the non-deprecated sibling should win, but being deprecated should not beat a target
    // that has more sources -- frequency is still the dominant criterion.
    // Filled only in EN->TR: there the target is a Turkish name and we do not want to PRODUCE a name like
    // `back_space`. In TR->EN the target is English and we do not know whether it is deprecated; besides, we need to be able to
    // TRANSLATE a deprecated Turkish name, and that direction looks at the source, not the target.
    private def candidates(pairs: Seq[(String, String, Boolean, Int, Boolean)]): Map[String, Seq[(String, Int)]] =
      pairs.groupBy(_._1).map { case (name, ss) =>
        val tally = ss.groupBy(_._2).map { case (target, hs) =>
          (target, hs.map(_._4).sum, hs.filter(_._3).map(_._4).sum, hs.exists(_._5)) }
        name -> tally.toSeq
          .sortBy { case (target, n, wrapperCount, isDeprecated) => (-n, isDeprecated, -wrapperCount, target) }
          .map { case (target, n, _, _) => (target, n) }
      }
    private def fromWrapper(s: Row) = !s.source.startsWith("data.scala")

    // To count as "ambiguous", the second candidate must have at least HALF as many sources as the winner.
    // Measured: without a threshold, `sağ -> right(3) / RIGHT(1)` was reported on every use,
    // producing a useless report of 1011 lines over 90 scripts.
    private def seriousAlternatives(as: Seq[(String, Int)]): Seq[String] = as match {
      case (_, winnerCount) +: rest => rest.collect { case (target, n) if n * 2 >= winnerCount => target }
      case _                        => Nil
    }

    private val trCandidates = candidates(rows.map(s => (s.tr, s.en, fromWrapper(s), s.count, false)))
    private val enCandidates = candidates(rows.map(s => (s.en, s.tr, fromWrapper(s), s.count, s.hasNote(DeprecatedNote))))
    // For the bare context: English targets the compiler cannot resolve bare are left out.
    private val bareRows = rows.filterNot(_.hasNote(MemberOnlyNote))
    private val trBareCandidates = candidates(bareRows.map(s => (s.tr, s.en, fromWrapper(s), s.count, false)))
    private val enBareCandidates = candidates(bareRows.map(s => (s.en, s.tr, fromWrapper(s), s.count, s.hasNote(DeprecatedNote))))
    // `kuvveti -> math.pow`: in the reverse direction a single token `pow` arrives, with receiver `math`. Qualified
    // targets are indexed separately under the receiver context (`math.`); `math` itself is
    // translated to `Matematik`, and the member becomes `kuvveti` from here. Measured: without it `Matematik.pow` was left over.
    private val enReceiverCandidates = candidates(rows.collect {
      case s if s.en.contains('.') && !s.hasNote(AliasNote) => (s.en.substring(0, s.en.lastIndexOf('.') + 1) + "\u0000" + s.en.substring(s.en.lastIndexOf('.') + 1), s.tr, fromWrapper(s), s.count, s.hasNote(DeprecatedNote))
    })
    // `Resim.dizi -> picStack`: a member of the Turkish object is BARE in English. The generator writes the tr
    // of such rows qualified; from here the TR->EN lookup with a receiver gives `^picStack` (the receiver is swallowed),
    // while EN->TR already gives `Resim.dizi` through enCandidates.
    private val trReceiverCandidates = candidates(rows.collect {
      case s if s.tr.contains('.') => (s.tr.substring(0, s.tr.lastIndexOf('.') + 1) + "\u0000" + s.tr.substring(s.tr.lastIndexOf('.') + 1), SwallowReceiver + s.en, fromWrapper(s), s.count, false)
    })
    private def hasNoParensNote(k: Rule) = k.note.contains(NoParensNote)

    // First the rule that matches the context exactly (bare / member / receiver / `Receiver.`), then "*". A lookup
    // specific to a receiver (`Resim.`) does NOT fall to the "*" rule: that rule makes sense only for that receiver;
    // if it fell through, the ordinary member lookup would try "*" anyway.
    private def rule(direction: String, name: String, context: String): Option[Rule] =
      rules.find(k => k.direction == direction && k.name == name && k.context == context)
        .orElse(if (context == ReceiverCallContext) rules.find(k => k.direction == direction && k.name == name && k.context == ReceiverContext) else None)
        .orElse(if (context == MemberCallContext) rules.find(k => k.direction == direction && k.name == name && k.context == MemberContext) else None)
        .orElse(if (context.endsWith(".")) None else rules.find(k => k.direction == direction && k.name == name && k.context == AnyContext))

    // A lookup specific to a receiver (`resim.`) knows only RULES; if there is no rule it returns None so that the caller can
    // go on to the ordinary member/bare lookup. If it fell to the table, the `member ->
    // erase` rule for `resim.sil()` would never get its turn (measured: sil, boyu, Color, hsla all came from this).
    private def select(direction: String, name: String, context: String, table: Map[String, Seq[(String, Int)]]): Option[Choice] =
      rule(direction, name, context) match {
        case Some(k) if k.target == LeaveUntouched => None
        case Some(k)                               => Some(Choice(k.target, Nil, hasNoParensNote(k)))
        case None if context.endsWith(".")         =>
          val receiverTable = if (direction == "en>tr") enReceiverCandidates else trReceiverCandidates
          receiverTable.get(context + "\u0000" + name).map(as => Choice(as.head._1, seriousAlternatives(as)))
        case None                                  => table.get(name).map(as => Choice(as.head._1, seriousAlternatives(as)))
      }

    // WARNING: "member(" is also a member context. Skipping it here would be a silent and wide regression:
    // every member call WITH ARGUMENTS would fall to the bare table (trBareCandidates), and the rows marked `member-only`
    // -- a large part of the dictionary -- would stop being candidates.
    private def isMemberContext(context: String) =
      context == MemberContext || context == MemberCallContext || context.endsWith(".")
    /** Is there an explicit "do not translate" (-) rule for this name in this context? Direction: "tr>en" | "en>tr". */
    def isLeftUntouched(direction: String, name: String, context: String): Boolean = rule(direction, name, context).exists(_.target == LeaveUntouched)
    def lookupTurkishToEnglish(name: String, context: String): Option[Choice] =
      select("tr>en", name, context, if (isMemberContext(context)) trCandidates else trBareCandidates)
    def lookupEnglishToTurkish(name: String, context: String): Option[Choice] =
      select("en>tr", name, context, if (isMemberContext(context)) enCandidates else enBareCandidates)

    /** All the Turkish names the dictionary knows (including those in the rules). */
    def turkishNames: Set[String] = trCandidates.keySet ++ rules.filter(_.direction == "tr>en").map(_.name)
    def englishNames: Set[String] = enCandidates.keySet ++ rules.filter(_.direction == "en>tr").map(_.name)
  }
}
