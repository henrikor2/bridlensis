package bridlensis.env;

import java.util.List;

import bridlensis.InvalidSyntaxException;
import bridlensis.NSISStatements;

public class FunctionFile extends CustomFunction {

	private static final int FILE_INDEX = 0;
	private static final int OPTIONS_INDEX = 1;
	private static final int OUTPATH_INDEX = 2;

	protected FunctionFile() {
		super(1, ReturnType.VOID, "File");
		registerArguments("file", "options", "outpath");
	}

	@Override
	public String statementFor(String indent, List<TypeObject> args,
			Variable returnVar) throws InvalidSyntaxException {
		StringBuilder sb = new StringBuilder(indent);
		if (!args.get(OUTPATH_INDEX).equals(NSISStatements.NULL)) {
			sb.append("SetOutPath ");
			sb.append(args.get(OUTPATH_INDEX).getValue());
			sb.append(NSISStatements.NEWLINE_MARKER);
			sb.append(indent);
		}
		sb.append("File ");
		if (!args.get(OPTIONS_INDEX).equals(NSISStatements.NULL)) {
			String options = SimpleTypeObject.stripString(args
					.get(OPTIONS_INDEX));
			if (!options.isEmpty()) {
				sb.append(options);
				sb.append(' ');
			}
		}
		sb.append(args.get(FILE_INDEX).getValue());
		return sb.toString();
	}

	@Override
	public String getMarkdownHelp() {
		return "Adds file(s) to be extracted. Wildcards are supported. See NSIS File "
				+ "instruction documentation for options. Uses the current output path unless "
				+ "argument `outpath` is given. \r\n"
				+ "\r\n"
				+ "    File(\"somefile.dat\")\r\n"
				+ "    \r\n"
				+ "    File(\"c:\\autoexec.bat\", \\       ; Specific file\r\n"
				+ "         \"/oname=my autoexec.bak\")  ; As \"my autoexec.bak\"\r\n"
				+ "    \r\n"
				+ "    File(\"*.html\", \"\", \\            ; All HTML files\r\n"
				+ "         instdir + \"\\doc\")          ; To $INSTDIR\\doc\r\n";
	}

}
