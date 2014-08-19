# BridleNSIS

BridleNSIS is a language extension for NSIS (Nullsoft Scriptable Install System) designed to make things easier to express and rein in verbosity of NSIS at places.

Latest version: [0.4.1](https://github.com/henrikor2/bridlensis/raw/master/dist/BridleNSIS-0.4.1.exe)

## Build From Source Code

Build from source code requires JDK 1.7 or newer, NSIS, and either Apache Ant 1.8 (or newer) or Gradle 2.0.

1.   Compile BridleNSIS Java classes: `>ant|gradle classes`
2.   Compile and run tests: `>ant|gradle test`
3.   Create distribution jar package: `>ant|gradle jar`
4.   Generate documentation: `>ant|gradle doc`
5.   Compile installer: `>ant|gradle installer`

Or simply build all by `>ant|gradle`.
