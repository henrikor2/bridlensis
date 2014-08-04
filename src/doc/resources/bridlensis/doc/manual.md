# BridleNSIS

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


### Command-Line

BridleNSIS compiler will parse the input file(s) and convert them to pure NSIS. Converted files (.snsi or .snsh) are then passed to the NSIS compiler (makensis.exe) automatically.

Use Java 1.7 or newer to run BridleNSIS compiler:

%%% usage 
<!-- usage --> 
%%%


### Apache Ant

Use the following task definition to compile BridleNSIS scripts from Apache Ant:

    <taskdef name="makeBridleNSIS"
             classname="bridlensis.ApacheAntTask"
             classpath="path/to/bridlensis.jar" />

Parameters:

*   `file`: BridleNSIS script file to compile (__required__)
*   `nsishome`: NSIS home directory
*   `output`: Output directory for converted script files
*   `encoding`: Input/output file encoding
*   `excludes`: Colon-separated list of files to exclude

Optional nested elements:

*   `<nsisoption value="value" />`: NSIS compiler option
*   `<exclude file="path\to\file.nsi" />`: Exclude file definition

Example:

    <makeBridleNSIS file="MyInstaller.nsi"
                    nsishome="C:\Program Files (x86)\NSIS-3.0a2"
                    output="${java.io.tmpdir}">
        <nsisoption value="/V4" />
        <nsisoption value="/Dant.home=${ant.home}" />
    </makeBridleNSIS>


### Gradle

You can compile BridleNSIS scripts from Gradle by re-using the above Ant task.

    task(makeBridleNSIS) << {
        ant.taskdef(name: 'makeBridleNSIS', classname: 'bridlensis.ApacheAntTask') {
            classpath {
                fileset(dir: 'path/to/bridlensis', includes: 'BridleNSIS-*.jar')
            }
        }
        ant.makeBridleNSIS(file: 'MultiLanguageProject.nsi', encoding: 'Cp1252') {
            exclude(file: 'LangStrings_ru.nsh')
            exclude(file: 'LangStrings_ja.nsh')
        }
    }


### Multilingual Installers

Unlike NSIS BridleNSIS can handle only one encoding and character set at the time. Compiler uses the Windows system encoding unless overwritten by `-e` command-line argument. See [Java documentation](http://docs.oracle.com/javase/7/docs/technotes/guides/intl/encoding.doc.html) for the list of supported character encodings.

When building Unicode installer with NSIS v3.0 or newer you probably want to use encoding `UTF-16LE`. With non-Unicode installers and mixed character sets you must separate the multilingual strings to own files (see NSIS instruction `LangString`) and add them to excluded files list by using command-line argument `-x`.

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


### Macros

BridleNSIS syntax is not allowed inside macros and anything defined inside macros is not visible for Bridle parser. This is to avoid problems with compile time function and variable detection. A classic example is the shared installer and uninstaller function defined via macros:

    !macro myfunc un
    Function ${un}myfunc
        ...
    FunctionEnd
    !macroend
    
    !insertmacro myfunc ""
    !insertmacro myfunc "un."

In this case functions myfunc and un.myfunc cannot be called using Bridle syntax `myfunc()` or `un.myfun()`, neither you can use Bridle syntax inside myfunc body.


## Function Reference


### Built-in Functions

BridleNSIS built-in functions provides a convenience access to some NSIS features.

%%% functions type=custom 
    MsgBox
    File
    ReserveFile
    FileCopy
    FileDelete
    FileRename
    RMDir
    DeleteRegKey
    GetFullPathName
    WordFind
    StrCmp
    IntCmp
%%%


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
