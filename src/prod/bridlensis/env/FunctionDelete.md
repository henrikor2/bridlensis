Delete file (which can be a file or wildcard, but should be specified with a full path) from the target system. See NSIS documentation for supported options (`/REBOOTOK`) and their behavior. Error flag is cleared if the return value `0` for success is assigned for a variable or used in operation. Returns `1` for error with error flag set.

    If Delete("C:\autoexec.bat") == 0
        DetailPrint "File delete succeeded."
        ...
