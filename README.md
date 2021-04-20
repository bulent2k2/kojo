### Kojo Links

* The [Kojo home-page][1] provides user-level information about Kojo.
* The [Kojo issue-tracker][2] let's you file bug reports.
* The [Kojo Localization file](localization.md) tells you how to translate Kojo to your language.
* The [Kojo AI repo](https://github.com/litan/kojo-ai-2) adds exciting AI capabilities (Neural Style Transfer and Object Detection via Deep Learning, Graphs, etc.) to Kojo.

### To start hacking:

* Make sure you have Java 8 on your path. 
* Run `./sbt.sh clean package` to build Kojo.
* Run `./sbt.sh test` to run the Kojo unit tests.
* Run `./sbt.sh run` to run Kojo (use `net.kogics.kojo.lite.DesktopMain` as the main class)
* As you modify the code, do incremental (and fast) auto-compilation and auto-testing using sbt:
```  
sbt
  > ~compile
  > ~test
```

### IDE setup

#### Intellij IDEA  
Import a new project via the Kojo SBT settings.

#### Eclipse  
Run `./sbt.sh eclipse` to generate project files for Eclipse. Then import the newly generated project into Eclipse.

For running Kojo from within an IDE, the main class is `net.kogics.kojo.lite.DesktopMain`. For debugging, the main class is `net.kogics.kojo.lite.Main`. 

*Eclipse Notes*: You need to tweak the Eclipse project generated by sbt. Right-click on the project in Eclipse, bring up *Properties*, go to *Java Build Path*, and then go to *Libraries*. Remove the *Scala Library* and *Scala Compiler* containers, and add the Scala library and compiler jars (from your local Scala install, or cached sbt jars). Your project *Libraries* should now contain the following Scala jars:

- scala-library.jar
- scala-compiler.jar
- scala-reflect.jar
- scala-actors-xx.jar
- scala-parser-combinators-xx.jar
- scala-xml-xx.jar
- scala-swing-xx.jar

#### Emacs

Put the following in your .emacs config file
```  
(require 'package)
(add-to-list 'package-archives '("melpa" . "https://melpa.org/packages/") t)
(package-initialize)
;; https://github.com/hvesalai/emacs-scala-mode
(use-package scala-mode
  :interpreter
    ("scala" . scala-mode))
(add-to-list 'auto-mode-alist '("\\.sc\\'" . scala-mode))
(add-to-list 'auto-mode-alist '("\\.kojo\\'" . scala-mode))
```

  [1]: http://www.kogics.net/kojo
  [2]: https://github.com/litan/kojo/issues
  
