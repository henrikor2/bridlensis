# BridleNSIS

BridleNSIS is a language extension for NSIS (Nullsoft Scriptable Install System) designed to make things easier to express and rein in verbosity of NSIS at places.

Latest version: [0.3.0](https://github.com/henrikor2/bridlensis/raw/master/dist/BridleNSIS-0.3.0.exe)

## Build From Source Code

Build from source code requires JDK 1.7 or newer, NSIS, and either Apache ANT 1.8 (or newer) or Gradle 2.0.

1.   Compile BridleNSIS Java classes: `>ant|grdle classes`
2.   Compile and run JUnit tests: `>ant|grdle test`
3.   Create distribution jar package: `>ant|grdle jar`
4.   Generate HTML documents: `>ant|grdle doc`
5.   Compile NSIS installer: `>ant|grdle installer`

Or simply build all by `>ant|grdle`.
