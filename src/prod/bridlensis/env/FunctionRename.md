Rename source file to target file. See NSIS documentation for supported options (`/REBOOTOK`) and their behavior. Error flag is cleared if the return value `0` for success is assigned for a variable or used in operation. Returns `1` for error with error flag set.

    If Rename("C:\autoexec.bat", "C:\autoexec.bak") == 0
        DetailPrint "Rename succeeded."
        ...
