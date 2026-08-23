# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repository is

Kojo is a Scala 2.13 / Swing desktop learning environment (turtle graphics, pictures, games, music) built on Piccolo2D. This repo (`bulent2k2/kojo`) is a fork of `litan/kojo` whose purpose is **Turkish localization ("Koco")** — including a patched Scala compiler that accepts Turkish keywords. Keep that in mind for every change: fork-specific work should stay inside the Turkish/i18n layer where possible to ease merges from upstream.

## Build and test commands

Java 8 on the PATH is the safest assumption for building (README says Java 8; runtime supports 8–21). The JVM flags `-XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled` in `build.sbt`, `sbt.sh`, and `StubMain.scala` are intentional for the Java 8 target — do not "modernize" them.

```bash
./sbt.sh clean package     # build (sbt launcher jar is committed; no global sbt needed)
./sbt.sh test              # run unit tests
./sbt.sh run               # run Kojo (main class: net.kogics.kojo.lite.DesktopMain)
./sbt.sh                   # then `~compile` / `~test` for incremental auto-compile/test
```

Run a single test class (tests run under JUnit 4 via `@RunWith(JUnitRunner)`):

```bash
./sbt.sh 'testOnly net.kogics.kojo.turtle.TurtleTest'
./sbt.sh 'testOnly net.kogics.kojo.lite.i18n.TurkishAPITest'
```

Caveats:
- **Tests need a display.** `TestEnv` constructs real Swing/Piccolo objects; there is no headless mode. On a bare container use `xvfb-run ./sbt.sh test`.
- `src/itest/` is not wired into `build.sbt`; `sbt test` never runs it.
- **No CI exists** — nothing validates builds automatically; always run `./sbt.sh test` yourself.
- Release packaging: `makezip.sh` (Linux/generic zip), `make-windows-zip.sh`, `stage-i4j-installer` + `installer.i4j/` (install4j projects: `kojo.install4j` English, `koco.install4j` Turkish). All of them call `stage-scala-toolchains.sh`, which stages both Scala toolchains under `lib/scala-en` (stock jars, downloaded from Maven Central and cached in the gitignored `scala-en-jars/`) and `lib/scala-tr` (the Turkish-keyword jars). `installer/jarlist.txt` must be updated when a dependency version changes (it deliberately excludes the four toolchain jars — those are staged by `stage-scala-toolchains.sh`). These scripts contain hard-coded developer paths and need a `scala` CLI on PATH — not portable as-is.

## The patched Scala compiler (`scala-tr/`)

`build.sbt` sets `scalaHome := Some(file("./scala-tr/build/pack"))`: sbt uses the **committed** jars in `scala-tr/build/pack/lib/` as the Scala toolchain instead of Maven artifacts (hence `autoScalaLibrary := false`). These jars come from a fork of scala/scala (`bulent2k2/scala-2`) with Turkish keywords added to the scanner (`dez`=val, `den`=var, `eğer`=if, `tanım`=def, …); see `scala-tr/README` for how they were built (requires JDK 8/11).

- **Never delete or clean `scala-tr/build/pack/lib`** — the build cannot resolve a Scala instance without it.
- Bumping `scalaVer` in `build.sbt` without rebuilding the patched jars causes a version mismatch.
- `lib/scalariform.jar` is likewise a patched scalariform that knows the Turkish keywords (used by the in-app editor); `scala-tr/en/scalariform.jar` is the pristine English reference copy.

### Runtime toolchain toggle

A packaged Kojo ships **both** toolchains — `lib/scala-en` (stock scala-library/reflect/compiler + pristine scalariform) and `lib/scala-tr` (the four Turkish-keyword-patched jars). The stock jars are staged **at the Scala version the patched jars were built from** — `stage-scala-toolchains.sh` reads `version.number` out of the patched `scala-library.jar` (currently 2.13.15, *not* `build.sbt`'s `scalaVer`), so the two toolchains differ only in the Turkish patches. The launcher JVM always boots on `lib/scala-en` (see `installer/bin/kojo`, `kojo.cmd`, `winlauncher-for-zip.xml`, and the install4j `scanDirectory` entries); before spawning the real Kojo JVM, `lite/ScalaToolchain.scala` (used by both `DesktopMain` and `NewKojoInstance`) reads the persisted `user.language` preference (`Kojolite-Prefs`, same node `KojoCtx` writes) and swaps the matching toolchain onto the child classpath — Turkish gets the patched compiler, every other language the stock one. `-Dkojo.toolchain=en|tr` on the launcher overrides the language-based choice (the Koco install4j package sets `-Dkojo.toolchain=tr` so it compiles Turkish keywords out of the box), and a `kojo.toolchain` entry in the per-install `kojo.properties` does the same. If no `scala-en`/`scala-tr` directory is on the launcher classpath (dev runs via sbt, where `scalaHome` pins the toolchain), the classpath is left untouched. Unit-tested in `lite/ScalaToolchainTest.scala`. Note: with `scalaHome` set, sbt substitutes the pack jars — the stock Maven scala jars never reach `dist/`, which is why `stage-scala-toolchains.sh` downloads them.

**Stale-launcher trap**: the committed `installer/bin/kojo.exe` is a launch4j binary with the classpath baked in — regenerate it from `installer/winlauncher-for-zip.xml` (launch4j) whenever the launcher classpath changes, or zip builds ship a launcher that can't find Scala. The zip scripts print a warning if the exe lacks the `lib/scala-en` entry.

## Architecture

### Startup: two JVMs

`net.kogics.kojo.lite.DesktopMain` (via `StubMain`) is only a launcher: it builds a classpath (including `~/.kojo/lite/libk` and `KOJO_CLASSPATH`) and **spawns a second JVM** running `net.kogics.kojo.lite.Main` — the real app. Debugging/profiling means attaching to the second process. `Main` builds the object graph in order: `KojoCtx` (first — sets user language) → `SpriteCanvas` → `TurtleWorldAPI` (`Tw`) → `DrawingCanvasAPI` (`TSCanvas`) → `staging.API` → `StoryTeller`/music players → `CodeExecutionSupport` → `ScriptEditor`, docked via docking-frames (`lite/topc/*Holder.scala`).

### Script execution pipeline

ScriptEditor → `lite/CodeExecutionSupport.scala` (the hub; defines the `RunContext` with its `compilerPrefix` wrapper around user code) → `xscala/ScalaCodeRunner2.scala`, whose single Akka actor (`InterpActor`) serializes all compile/run/completion requests. Two back-ends:
- Interpreter path: `xscala/kojoInterpreter.scala` wrapping `scala.tools.nsc.interpreter.IMain`.
- Compiler path: `xscala/CompilerAndRunner.scala` driving `nsc.Global` directly (plus an `interactive.Global` presentation compiler for completions).

Everything else runs on the Swing EDT via `Utils.runInSwingThread`.

### The Builtins API surface

`lite/Builtins.scala` (with `lite/CoreBuiltins.scala`) is the user-visible global namespace — every public member becomes a script-level identifier via `import builtins._`. Critical: the compiler prefix references `net.kogics.kojo.lite.Builtins.instance` **textually**, so renaming/moving `Builtins` breaks user scripts at runtime, not build time.

Adding a user-facing API is multi-sided: the method in `Builtins`/`CoreBuiltins`, `UserCommand.addSynopsis`/`addCompletion`, an entry in `xscala/Help.scala`, and — in this fork — a Turkish wrapper (see below).

### Key packages (`src/main/scala/net/kogics/kojo/`)

- `lite` — the application (Main, Builtins, CodeExecutionSupport, ScriptEditor, KojoCtx, canvas, i18n, trace/debugger)
- `xscala` — Scala compiler/interpreter integration
- `core` — UI-free interfaces/ADTs (`CodeRunner`, `SCanvas`, `Picture`, `TwMode`/`VanillaMode`)
- `picture`, `turtle`, `staging` — the three drawing APIs (Piccolo2D scene graph; JTS for collisions)
- `util/Utils.scala` — resource bundles, Swing threading, init-script loading
- `lib/` (repo root) — 16 committed jars not on Maven (docking-frames, RSyntaxTextArea, patched scalariform, jfugue, …), picked up as unmanaged deps

## Localization (the point of this fork)

Three levels (see `localization.md`):
1. **UI strings**: `src/main/resources/net/kogics/kojo/lite/Bundle_xx.properties`, wired in `lite/LangMenuFactory.scala`; accessed via `Utils.loadString`.
2. **Programming vocabulary**: translator artifacts in `l10n-level2/` (`level2_xx.properties` + `gen-level2.kojo` generator — has hard-coded paths, edit before use) generate `lite/i18n/xxInit.scala`. `lite/i18n/LangInit.scala.initPhase2` dispatches on `user.language`; runtime import glue lives in `src/main/resources/i18n/initk/<lang>.<mode>.kojo`, concatenated into every script's scope by `Utils.initCode`.
3. **Localized samples**: `src/main/resources/samples/<lang>/`, plus Turkish variants under `challenge/tr`, `mathgames/tr`, `robosim/tr`, `ka-bridge/tr`.

### Turkish layer

`lite/i18n/trInit.scala` (`TurkishAPI`) plus `lite/i18n/tr/` — 40 files, ~6.5k LOC (including `trInit.scala`) of Turkish-named wrappers (e.g. `cizim`/`resim`/`renk`/`sayi`/`dizi`/`yazi`), a Turkish help system (`help.scala`), an English→Turkish naming dictionary (`dict.scala`), and REPL-output/type-name translation (`translate.scala`). Shared upstream files carry small hooks into this package (`xscala/CodeCompletionUtils.scala`, `xscala/CodeTemplates.scala`, `xscala/Help.scala`, `lexer/ScalariformTokenMaker.scala`, `lite/KojoCompletionProvider.scala`, `lite/ScriptEditor.scala`, `lite/CodeExecutionSupport.scala`, `lite/OutputPane.scala`) — all no-ops unless `user.language == "tr"`.

Fork conventions:
- Keep new Turkish work inside `lite/i18n/` to minimize upstream merge conflicts. The Koco revision/date is deliberately kept in a comment at the top of `lite/i18n/tr/dict.scala`, **not** in `Versions.scala`.
- Turkish source uses non-ASCII identifiers (`ı ş ğ ö ü ç İ`); files must stay UTF-8. Beware the Turkish dotless-i trap in `toLowerCase`/`toUpperCase`.
- A new builtin should also get a Turkish wrapper in `lite/i18n/tr/` and, where relevant, `dict.scala`/`translate.scala` entries.

## Conventions

- Tests: ScalaTest pinned at 3.0.8 (old API: `org.scalatest.Matchers`, `org.scalatest.junit.JUnitRunner` — not the 3.1+ paths; don't upgrade casually), run under JUnit 4; named `<Thing>Test.scala` mirroring the main package layout. Shared harnesses: `lite/TestEnv.scala`, `lite/NoOpKojoCtx.scala`, `xscala/CompilerAndRunnerTestBase.scala`.
- `.scalafmt.conf` exists (scalafmt 3.7.1, maxColumn 120) but there is **no sbt formatting plugin** and much of the codebase (especially `lite/i18n/tr/`) is not formatted to it. Format only the code you touch; never bulk-reformat.
- License is GPLv3; every source file carries the header — keep it on new files.
