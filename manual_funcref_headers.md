### NSIS Headers As Functions

BridleNSIS gives programmers a function-like access to several NSIS built-in headers (macros). Bridle allows passing zero to maximum defined number of arguments so please refer to [the NSIS User Guide Appendix E: Useful Headers](http://nsis.sourceforge.net/Docs/AppendixE.html#E) documentation for usage. All headers, including the ones not listed below, can still be used in plain NSIS syntax, for example `${Locate} "C:\ftp" "/L=F /M=RPC DCOM.rar /S=1K" "Example1"`.

Supported headers as functions are:
