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
