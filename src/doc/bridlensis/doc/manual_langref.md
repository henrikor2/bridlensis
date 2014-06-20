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
