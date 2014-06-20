﻿# BridleNSIS

Copyright &copy; 2014 Henri Kor

%%% version 
<!-- version --> 
%%%


## Introduction

BridleNSIS is a language extension for NSIS (Nullsoft Scriptable Install System) designed to make things easier to express and rein in verbosity of NSIS at places. With BridleNSIS programmers can create NSIS installers for Windows using some syntactic sugar wherever seem reasonable. BridleNSIS is compatible with and fully transparent to vanilla NSIS\*. This means that programmers can start using BridleNSIS on their existing NSIS projects immediately without modifying the current code base\*\*.

_&#8220;I wish to personally thank [Tomi Tirri](https://github.com/ttirri) for numerous discussions and his contributions to my inspiration and knowledge in creating this project.&#8221;_ --Henri Kor

This document assumes that the reader is familiar with NSIS features and usage. Please refer to [the NSIS User Manual](http://nsis.sf.net/Docs/).

\* BridleNSIS compatibility has been tested up to NSIS version 3.0a2.
\*\* See restrictions for multi-language support and variable naming further in this document.


## Usage

BridleNSIS compiler will parse the input file(s) and convert them to pure NSIS. Converted files (.snsi or .snsh) are then passed to the NSIS compiler (makensis.exe) automatically.

Use Java 1.7 or newer to run BridleNSIS compiler:

    java -jar bridlensis.jar [-n <NSIS home>] [-o <outdir>] [-e <encoding>] [-x <file1:file2:..>] <script file> [<NSIS options>]

Arguments:

*   __-n &lt;NSIS home&gt;__: NSIS home directory (tried to detect automatically if not specified)
*   __-o &lt;output&gt;__: Output directory for converted script files (.snsi or .snsh)
*   __-e &lt;encoding&gt;__: File encoding (defaults to Windows system encoding)
*   __-x &lt;files&gt;__: Colon-separated list of files to exclude (or not to follow when found in !include)
*   __&lt;script file&gt;__: BridleNSIS script file to compile
*   __&lt;NSIS options&gt;__: Options passed to NSIS compiler, e.g. /Dname=value

Error codes:

*   __10__: Unable to create or resolve output directory
*   __11__: Errors in BridleNSIS script
*   __12__: NSIS home directory not found
*   __13__: Unexpected error when executing makensis.exe

Otherwise BridleNSIS returns whatever makensis.exe returns.

Example:

    java -jar bridlensis.jar -n "C:\Program Files(x86)\NSIS" example.nsi


### Multilingual Installers

Unlike NSIS BridleNSIS can handle only one encoding and character set at the time. Compiler uses the Windows system encoding unless defined in argument `-e`. See [Java documentation](http://docs.oracle.com/javase/7/docs/technotes/guides/intl/encoding.doc.html) for the list of supported character encodings.

When building Unicode installer with NSIS v3.0 or newer you probably want to use `-e UTF-16LE`. With non-Unicode installers and mixed character sets you must separate the multilingual strings to own files (see NSIS instruction `LangString`) and add them to excluded files list by using `-x` argument.

Example:

    java -jar bridlensis.jar -e Cp1252 -x "LangStrings_ru.nsh:LangStrings_ja.nsh" MultiLanguageProject.nsi


### Editor Plugins

*   **[BridleNSIS Sublime Text](https://github.com/idleberg/BridleNSIS-Sublime-Text)**
    BridleNSIS syntax definitions and completions for Sublime Text. The former work for TextMate as well.


## Language Reference

Unless stated otherwise BridleNSIS follows [the NSIS Scripting Reference](http://nsis.sf.net/Docs/Chapter4.html).


### Reserved Words

Reserved words are: 

%%% reservedwords 
<!-- reserved words --> 
%%%

Variable or function name cannot be a reserved word.


### Literals

Unlike in NSIS, all string literals must be encapsulated with `'` or `"` character.

NSIS global defines, language strings, and environment variables can be accessed directly, for example `${NSIS_VERSION}`.

BridleNSIS provides a special global define `${BRIDLE_NULL}`. It can be used with function calls to indicate null value of an argument.


### Literal Concatenation

BridleNSIS supports literal concatenation via `+` operator. All concatenations are processed as strings regardless of the member data types, including the ones that are seemingly numeric, e.g.`1 + 2 ; <-- "12"`. See function `IntOp` for integer operations.


### Variables, Constants, Defines

Variable name can only contain characters from the set of `abcdefghijklmnopqrstuvwxyz0123456789_`.

Variables can be introduced via `Var a` statement or via direct assignment `b = "hello"`. Variable assignment can be an expression, e.g. `a = b + " world!"`. NSIS [built-in variables](http://nsis.sf.net/Docs/Chapter4.html#4.2.2) (excluding $0..9) and [constants](http://nsis.sf.net/Docs/Chapter4.html#4.2.3) can be accessed directly as they would be any other variables in BridleNSIS.

    a = 1
    
    r0 = a ; Bare NSIS equivalent would be: \
             StrCpy $R0 $a
    
    instdir = programfiles + "\BridleNSIS" ; Bare NSIS equivalent would be: \
                                             StrCpy $INSTDIR "$PROGRAMFILES\BridleNSIS"

Supported built-in variables include: 

%%% builtinvars
<!-- built-in variables -->
%%%

Note that NSIS doesn't support writing some of the variables and consider them as constants, e.g. `programfiles` (`$PROGRAMFILES` in NSIS).


### Functions

Functions may have 0 to N arguments and a return value.

    Function Shout(a, b)
        Return a + b + "!"
    FunctionEnd

    DetailPrint(Shout("hello", " world")) ; <-- "hello world!"

Existing NSIS functions can be called the same way as BridleNSIS functions. They will not have any arguments or return value.

    Function NsisFunc
        ...
    FunctionEnd
    
    NsisFunc() ; Same as: \
                 Call NsisFunc

Variables introduced inside function can be accessed only within that function scope. Global variables can be accessed via `global.` prefix.

    r0 = "hello"
    
    Function Foo()
        r0 = " world"
        Return global.r0 + r0
    FunctionEnd
    
    DetailPrint(Foo()) ; <-- "hello world"


### If Statement

BridleNSIS relies on NSIS Logic Lib to add support for flow control and logic, therefore `LogicLib.nsh` must be imported before using If statements.

    !import LogicLib.nsh
    ...
    If [Not] <expr> [And|Or [Not] <expr> [And|Or ...]]
        ...
    [ElseIf [Not] <expr> [And|Or ...]]
        ...
    [Else]
        ...
    EndIf

Supported comparison tests for expressions are: `a == b`, `a != b`, `a < b`, `a <= b`, `a > b`, `a >= b`. String tests are case-insensitive. Integer tests compare signed integers. Functions and literal concatenations are allowed within tests. Logic Lib flag tests `${Abort}`, `${Errors}`, `${RebootFlag}`, `${Silent}` and `${FileExists} <name>` can be accessed directly.

    If a == Foo(r0) And Not ${Errors}
        DetailPrint("one")
    ElseIf a > 42
        DetailPrint("two") 
    Else
        DetailPrint("three")
    EndIf


### Do Loop

BridleNSIS relies on NSIS Logic Lib to add support for flow control and logic, therefore `LogicLib.nsh` must be imported before using Do loops.

    !import LogicLib.nsh
    ...
    Do [While|Until <expr>]
        ...
        [Continue]
        ...
        [Break]
        ...
    Loop [While|Until <expr>]

See supported comparison and flag tests in If statement.

    line = FileRead(handle)
    Do Until ${Errors}
        MsgBox("OK" line)
        line = FileRead(handle)
    Loop


## Function Reference

### Built-in Functions

BridleNSIS built-in functions provides a convenience access to some NSIS features.


#### MsgBox(buttons, message [, options [, sd]])

Displays a message box with buttons `buttons` containing the text `message`. 

*   __buttons__: One of the following `OK`, `OKCANCEL`, `ABORTRETRYIGNORE`, `RETRYCANCEL`, `YESNO`, or `YESNOCANCEL`
*   __message__: Message text
*   __options__: `|` separated list of zero or more options: `ICONEXCLAMATION`, `ICONINFORMATION`, `ICONQUESTION`, `ICONSTOP`, `USERICON`, `TOPMOST`, `SETFOREGROUND`, `RIGHT`, `RTLREADING`, `DEFBUTTON1`, `DEFBUTTON2`, `DEFBUTTON3`, and `DEFBUTTON4`. Refer to the NSIS MessageBox instruction documentation for details.
*   __sd__: Silent installer default return. Use empty string (or simply omit the argument) if message box is shown in silent install.

Function will return name of the button user selected: `OK`, `CANCEL`, `ABORT`, `RETRY`, `IGNORE`, `YES`, or `NO`.

    If MsgBox("YESNO", "Are you sure?") == "YES"
        ...


#### File(file [, options [, outpath]])

Adds file(s) to be extracted. Wildcards are supported. See NSIS File instruction documentation for options. Uses the current output path unless argument `outpath` is given. 

    File("somefile.dat")
    
    File("c:\autoexec.bat", \       ; Specific file
         "/oname=my autoexec.bak")  ; As "my autoexec.bak"
    
    File("*.html", "", \            ; All HTML files
         instdir + "\doc")          ; To $INSTDIR\doc


#### ReserveFile(file [, options])

Reserves a file in the data block for later use. See NSIS ReserveFile instruction documentation for options.

    ReserveFile("time.dll", "/plugin")


#### FileCopy(source, target)

Alias: Copy

Copies files silently from the source to the target on the installing system. Returns `0` for success with error flag cleared, `1` for error with error flag set. Error flag is not cleared unless function return value is assigned for a variable or used in operation.

    If Copy("C:\autoexec.bat", $%TEMP%) <> 0
        Abort "File copy failed."
    EndIf

#### FileDelete(file [, options])

Alias: Delete

Delete file (which can be a file or wildcard, but should be specified with a full path) from the target system. See NSIS documentation for supported options (`/REBOOTOK`) and their behavior. Error flag is cleared if the return value `0` for success is assigned for a variable or used in operation. Returns `1` for error with error flag set.

    If Delete("C:\autoexec.bat") == 0
        DetailPrint "File delete succeeded."
        ...

#### FileRename(source, target [, options])

Alias: Rename

Rename source file to target file. See NSIS documentation for supported options (`/REBOOTOK`) and their behavior. Error flag is cleared if the return value `0` for success is assigned for a variable or used in operation. Returns `1` for error with error flag set.

    If Rename("C:\autoexec.bat", "C:\autoexec.bak") == 0
        DetailPrint "Rename succeeded."
        ...


#### RMDir(dir [, options])

Remove the specified directory from the target system. See NSIS documentation for supported options (`/r` and `/REBOOTOK`) and their behavior. Error flag is cleared if the return value `0` for success is assigned for a variable or used in operation. Returns `1` for error with error flag set.

    If RMDir(pluginsdir) <> 0
        DetailPrint "Error when deleting directory $PLUGINSDIR."
        ...


#### DeleteRegKey(rootkey, subkey [, options])

Deletes a registry key. See NSIS documentation for supported options (`/ifempty`). Error flag is cleared if the return value `0` for success is assigned for a variable or used in operation. Returns `1` for error with error flag set.

    DeleteRegKey("HKLM", "Software\BridleNSIS", "/ifempty")


#### GetFullPathName(path [, options])

Returns the full path of the file specified. If the path portion of the parameter is not found, the error flag will be set and return value will be empty. See NSIS documentation for supported options (`/SHORT`). 

    GetFullPathName(programfiles + "\NSIS")


#### WordFind(string, options, delim1 [, delim2 [, center]])

Multi-features string function. Acts as a convenience function for the NSIS [WordFind](http://nsis.sourceforge.net/WordFind) when passing `delim1` alone, as [WordFind2X](http://nsis.sourceforge.net/WordFind2X) when defining `delim2`, and as [WordFind3X](http://nsis.sourceforge.net/WordFind3X) when defining also `center`. See NSIS documentation for `options` and detailed usage.

    r1 = WordFind("C:\io.sys C:\Program Files C:\WINDOWS", "-02", " C:\") ; <-- "Program Files"
    r2 = WordFind("[C:\io.sys];[C:\logo.sys];[C:\WINDOWS]", "+2", "[C:\", "];") ; <-- "logo.sys"
    r3 = WordFind("[1.AAB];[2.BAA];[3.BBB];", "+1", "[", "];", "AA") ; <-- "1.AAB"


#### WordFindS(string, options, delim1 [, delim2 [, center]])

Same as WordFind but case sensitive.


### NSIS Headers As Functions

BridleNSIS gives programmers a function-like access to several NSIS built-in headers (macros). Bridle allows passing zero to maximum defined number of arguments so please refer to [the NSIS User Guide Appendix E: Useful Headers](http://nsis.sourceforge.net/Docs/AppendixE.html#E) documentation for usage. All headers, including the ones not listed below, can still be used in plain NSIS syntax, for example `${Locate} "C:\ftp" "/L=F /M=RPC DCOM.rar /S=1K" "Example1"`.

Supported headers as functions are:

%%% functions type=header
<!-- header functions --> 
%%%


### NSIS Instructions As Functions

BridleNSIS gives programmers a function-like access to several NSIS built-in instructions. Bridle allows passing zero to maximum defined number of arguments so please refer to [the NSIS Instructions](http://nsis.sourceforge.net/Docs/Chapter4.html#4.9) documentation for usage. All NSIS instructions, including the ones not listed below, can still be used in plain NSIS syntax, for example `File /oname=somedata.temp something.dat`.

Supported instructions as functions are:

%%% functions type=instruction
<!-- instruction functions --> 
%%%