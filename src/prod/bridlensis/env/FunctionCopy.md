Copies files silently from the source to the target on the installing system. Returns `0` for success with error flag cleared, `1` for error with error flag set. Error flag is not cleared unless function return value is assigned for a variable or used in operation.

    If Copy("C:\autoexec.bat", $%TEMP%) <> 0
        Abort "File copy failed."
    EndIf
