Returns the full path of the file specified. If the path portion of the parameter is not found, the error flag will be set and return value will be empty. See NSIS documentation for supported options (`/SHORT`). 

    GetFullPathName(programfiles + "\NSIS")
