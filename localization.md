Kojo can be localized at four levels.

* Level 1 - localization of the UI
* Level 2 - localization of the basic turtle and looping commands (so that children can program in the target language).
* Level 3 - localization of the existing turtle graphics samples.
* Level 4 - localization of the Scala *keywords* themselves, so that children write `värde x = 5` instead of `val x = 5`. This is a much bigger step than Levels 1-3 — it needs a patched Scala compiler and editor support, not just translated strings — and only makes sense for a language with an active community willing to maintain it (so far: Turkish).

The following are the steps that need to be followed to do the localization:

The first step is to fork and then clone the kojo repo. You can then make the required localization enhancements in your repo and send pull requests.

In the discussion below, let's assume that the language code for the target language is `xx`.

### Level 1
Create `Bundle_xx.properties` by tanslating the following file:  
https://github.com/litan/kojo/blob/master/src/main/resources/net/kogics/kojo/lite/Bundle.properties  

Add your language code to the list here:  
https://github.com/litan/kojo/blob/master/src/main/scala/net/kogics/kojo/lite/LangMenuFactory.scala#L31

Add your language (localized) name to the map here:  
https://github.com/litan/kojo/blob/master/src/main/scala/net/kogics/kojo/lite/LangMenuFactory.scala#L75

Then send a pull request.

### Level 2
Create `level2_xx.properties` by translating the following file:  
https://github.com/litan/kojo/blob/master/l10n-level2/level2.properties

Then send a pull request.

FYI, with the help of `level2_xx.properties`, the following files will be modified by a Kojo core-developer to wire in the level-2 changes:
* xxInit.scala (generated from the above properties file).
* xx.tw.scala
* LangInit.scala

Here's an example checkin for a wiring-in:  
https://github.com/litan/kojo/commit/852c18a6124fe773063f846db8fda9b7b705ab4c

### Level 3
This is best explained with an example:

if you want to localize the following sample for Swedish:  
`src/main/resources/samples/spiral.kojo`  
Then you just need to just create the following localized version of the sample:  
`src/main/resources/samples/sv/spiral.kojo`  
The version of spiral.kojo under the sv directory will get picked up when Kojo is running in Swedish mode.


### Level 4

Levels 1-3 are translation and resource work. Level 4 is different in kind: it
changes the *language the compiler parses*, so it touches the toolchain and the
editor, not just properties files. Turkish is the reference implementation
(`scala-tr/`, `lite/i18n/tr/`); a stub for a Swedish start is in `scala-sv/`.

There are four pieces. None of them is optional if you want the samples and the
editor to work in the localized keywords.

**1. A patched Scala compiler.**
The scanner has to accept the localized keywords. This is a ~5 KB patch to three
files of scala/scala (`Scanners.scala`, `StdNames.scala`, and an optional banner
in `Main.scala`); see `scala-tr/turkish-keywords.patch` for the model and
`scala-sv/swedish-keywords.patch` for a Swedish draft. The localized keywords are
*added* (English keeps working), and are listed FIRST in `allKeywords` so that
compiler error messages still report the English keyword. Build the patched
`scala-library`/`scala-reflect`/`scala-compiler` jars from a clean `v2.13.18`
checkout (`sbt 'set Global/baseVersionSuffix := ""' dist/mkPack`).

Choosing the keyword strings is the delicate part, and it is a language question,
not a coding one: a chosen keyword can no longer be used as an identifier, so it
must not collide with the Level-2 API or the samples. See `scala-sv/KEYWORDS.md`.

**2. The runtime toolchain toggle.**
Kojo ships the stock compiler and swaps in the patched one at launch for the
localized language — see `lite/ScalaToolchain.scala`, which today chooses between
`scala-en` and `scala-tr` by `user.language`, and `ScalaToolchainFetcher`, which
downloads the patched jars on demand from a GitHub release. Adding a language
means (a) publishing its patched jars as a release, and (b) generalizing the
`tr`-vs-`en` choice in `ScalaToolchain` to pick the right variant per language.
Doing this as a general "language → toolchain" lookup, rather than a second
hard-coded branch, is strongly preferred.

**3. Code formatting.**
The in-editor formatter (`ScriptEditor`'s format action → `ScalaFormatter.format`)
uses scalariform's parser, which has its own keyword table. Stock scalariform
does not know the localized keywords, so it mis-parses localized code and
formatting fails. The Turkish build therefore ships a **patched scalariform**
(`lib/scalariform.jar`, from a fork of scalariform whose `Keywords` set includes
the Turkish words; the pristine English copy is kept at `scala-tr/en/scalariform.jar`).
A Swedish edition needs the same: add the Swedish keywords to a scalariform fork,
build it, and ship it as the `scala-sv` toolchain's `scalariform.jar`.

**4. Syntax highlighting (colors).**
`lexer/ScalariformTokenMaker.scala` colors keywords. It already re-marks any token
that scalariform returned as an identifier but that the localized keyword set
recognizes — via `net.kogics.kojo.lite.i18n.tr.isTurkishKeyword` and the
`turkishKeywords` set in `lite/i18n/tr/package.scala`. A new language needs the
same keyword set exposed to the token maker. The clean way is to generalize
`isTurkishKeyword` to "is a keyword in the current language", backed by a
per-language set, so completion (`xscala/CodeCompletionUtils.scala`) and templates
(`xscala/CodeTemplates.scala`) — which also read `turkishKeywords` today — pick up
the new language for free.

**In short:** Level 4 = one small compiler patch (mechanical, per the stub) plus a
generalization of the currently Turkish-specific toolchain/editor plumbing to be
language-agnostic. The first is a contributor's PR to the language's own scala
fork; the second is best done once, in Kojo core, so the third language after
Turkish and Swedish is nearly free.
