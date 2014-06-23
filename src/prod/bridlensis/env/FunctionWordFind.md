Multi-features string function. Acts as a convenience function for the NSIS [WordFind](http://nsis.sourceforge.net/WordFind) when passing `delim1` alone, as [WordFind2X](http://nsis.sourceforge.net/WordFind2X) when defining `delim2`, and as [WordFind3X](http://nsis.sourceforge.net/WordFind3X) when defining also `center`. See NSIS documentation for `options` and detailed usage. Use `WordFindS(...)` for case-sensitive string comparison.

    r1 = WordFind("C:\io.sys C:\Program Files C:\WINDOWS", "-02", " C:\") ; <-- "Program Files"
    r2 = WordFindS("[C:\io.sys];[c:\logo.sys];[C:\WINDOWS]", "+2", "[C:\", "];") ; <-- "WINDOWS"
    r3 = WordFind("[1.AAB];[2.BAA];[3.BBB];", "+1", "[", "];", "AA") ; <-- "1.AAB"
