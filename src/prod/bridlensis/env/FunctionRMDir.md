Remove the specified directory from the target system. See NSIS documentation for supported options (`/r` and `/REBOOTOK`) and their behavior. Error flag is cleared if the return value `0` for success is assigned for a variable or used in operation. Returns `1` for error with error flag set.

    If RMDir(pluginsdir) <> 0
        DetailPrint "Error when deleting directory $PLUGINSDIR."
        ...
