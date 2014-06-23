Adds file(s) to be extracted. Wildcards are supported. See NSIS File instruction documentation for options. Uses the current output path unless argument `outpath` is given. 

    File("somefile.dat")
    
    File("c:\autoexec.bat", \       ; Specific file
         "/oname=my autoexec.bak")  ; As "my autoexec.bak"
    
    File("*.html", "", \            ; All HTML files
         instdir + "\doc")          ; To $INSTDIR\doc
