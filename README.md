# BridleNSIS

BridleNSIS is a language extension for NSIS (Nullsoft Scriptable Install System) designed to make things easier to express and rein in verbosity of NSIS at places.

In order to build the project you need JDK 1.7 or newer, Apache ANT 1.8 or newer, and NSIS for compiling the installer. If NSIS home directory is not found from the Windows %PATH% define it in property `bridle.nsis.home` at `build.properties` file.

1.   Compile BridleNSIS Java classes: `ant compile`
2.   Compile and run JUnit tests: `ant test`
3.   Create distributable jar package: `ant jar`
4.   Generate HTML documents: `ant doc`
5.   Make installer: `ant installer`

Or simply build all by `ant`.
