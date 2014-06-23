Deletes a registry key. See NSIS documentation for supported options (`/ifempty`). Error flag is cleared if the return value `0` for success is assigned for a variable or used in operation. Returns `1` for error with error flag set.

    DeleteRegKey("HKLM", "Software\BridleNSIS", "/ifempty")
