## Usage

BridleNSIS compiler will parse the input file(s) and convert them to pure NSIS. Converted files (.snsi or .snsh) are then passed to the NSIS compiler (makensis.exe) automatically.

Use Java 1.7 or newer to run BridleNSIS compiler:

    java -jar bridlensis.jar [-n <NSIS home>] [-o <outdir>] [-e <encoding>] [-x <file1:file2:..>] <script file> [<NSIS options>]

Arguments:

*   __-n &lt;NSIS home&gt;__: NSIS home directory (tried to detect automatically if not specified)
*   __-o &lt;output&gt;__: Output directory for converted script files (.snsi or .snsh)
*   __-e &lt;encoding&gt;__: File encoding (defaults to Windows system encoding)
*   __-x &lt;files&gt;__: Colon-separated list of files to exclude (or not to follow when found in !include)
*   __&lt;script file&gt;__: BridleNSIS script file to compile
*   __&lt;NSIS options&gt;__: Options passed to NSIS compiler, e.g. /Dname=value

Error codes:

*   __10__: Unable to create or resolve output directory
*   __11__: Errors in BridleNSIS script
*   __12__: NSIS home directory not found
*   __13__: Unexpected error when executing makensis.exe

Otherwise BridleNSIS returns whatever makensis.exe returns.

Example:

    java -jar bridlensis.jar -n "C:\Program Files(x86)\NSIS" example.nsi


### Multilingual Installers

Unlike NSIS BridleNSIS can handle only one encoding and character set at the time. Compiler uses the Windows system encoding unless defined in argument `-e`. See [Java documentation](http://docs.oracle.com/javase/7/docs/technotes/guides/intl/encoding.doc.html) for the list of supported character encodings.

When building Unicode installer with NSIS v3.0 or newer you probably want to use `-e UTF-16LE`. With non-Unicode installers and mixed character sets you must separate the multilingual strings to own files (see NSIS instruction `LangString`) and add them to excluded files list by using `-x` argument.

Example:

    java -jar bridlensis.jar -e Cp1252 -x "LangStrings_ru.nsh:LangStrings_ja.nsh" MultiLanguageProject.nsi


### Editor Plugins

*   **[BridleNSIS Sublime Text](https://github.com/idleberg/BridleNSIS-Sublime-Text)**
    BridleNSIS syntax definitions and completions for Sublime Text. The former work for TextMate as well.
