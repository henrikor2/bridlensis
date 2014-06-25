# BridleNSIS Release Notes

## Version 0.3.0

Release date: TBD

### What's New

*   BridleNSIS syntax is not allowed inside macros and anything defined inside macros is not visible for BridleNSIS parser
*   New convenience functions for all text and word function headers as defined in NSIS 3.0a2
*   Add support for defining several variables at once (`Var a, b, c`)
*   Improved syntax checks for function arguments and comparison statements
*   Improved integration with Sublime Text plugin

### Fixes

*   Fix parsing LogicLib flag tests if followed by another comparison statement (`If ${Errors} Or ...`)
*   Fix parsing instruction options starting with slash character (e.g. `Var /GLOBAL a`)

## Version 0.2.0

Release date: 2014-04-19

### What's New

*   New convenience functions File, ReserveFile, FileCopy, FileRename, RMDir, DeleteRegKey, GetFullPathName, WordFind(S), and WordReplace(S)
*   Function aliases Delete, Copy, Rename, and RMDir
*   Function FileDelete Optional argument rebootok must be passed as defined in NSIS, e.g. `Delete("file.dat", "/REBOOTOK")`
*   Improved NSIS home directory detection unless specified with command-line argument `-n`
*   Documented BridleNSIS process error codes
*   Showing off more BridleNSIS features in the example script

### Fixes

*   Fix `Not` not being a reserved word (may have caused ambiguous syntax and parser errors)
*   Fix `r2` ($R2) not being recognized as a built-in variable
*   Fix function optional arguments when using single-quote strings ('/arg')
*   Fix uninstaller leaving empty paths to registry

## Version 0.1.0

Release date: 2014-04-05

First public release
