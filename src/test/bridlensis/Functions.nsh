Function arguments(a, b)
    b = "there"
    c = "y'all"
    global.R0 = a + b + c
    DetailPrint(global.R0)
FunctionEnd

b = "world"
R0 = "."
arguments("hello", b)
DetailPrint(b)
DetailPrint(R0)

Function return_foo()
    Return "foo"
    DetailPrint "Never gets here"
FunctionEnd

return_foo()

R0 = return_foo()
DetailPrint (R0)

;DetailPrint (return_foo())

Function join(a, b)
    Return a + b
FunctionEnd

; FunctionFile
File("autoexec.bat")
File("autoexec.bat", "/oname=autoexec.tmp")
File("*.*", "/r /nonfatal", instdir + "\doc")
File("*.html", "", instdir + "\doc")

; FunctionRename
Rename("autoexec.bak", "autoexec.bat")
FileRename("autoexec.bak", "autoexec.bat", "/REBOOTOK")

; RMDir
RMDir(pluginsdir)
RMDir($%TEMP% + "\BridleNSIS", "/r /REBOOTOK")

; FunctionReserveFile
ReserveFile("autoexec.bat")
ReserveFile("time.dll", "/plugin")
