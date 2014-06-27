package bridlensis.env;

import java.util.List;

import bridlensis.NSISStatements;

class FunctionWordFind extends CustomFunction {

	private static final int STRING_INDEX = 0;
	private static final int OPTIONS_INDEX = 1;
	private static final int DELIM1_INDEX = 2;
	private static final int DELIM2_INDEX = 3;
	private static final int CENTER_INDEX = 4;

	private final boolean caseSensitive;

	protected FunctionWordFind(boolean caseSensitive) {
		super(3, ReturnType.REQUIRED, "WordFind" + (caseSensitive ? "S" : ""));
		this.caseSensitive = caseSensitive;
		registerArguments("string", "options", "delim1", "delim2", "center");
	}

	@Override
	public String statementFor(String indent, List<TypeObject> args,
			Variable returnVar) {
		StringBuilder sb = new StringBuilder(indent);
		sb.append("${WordFind");
		if (!args.get(CENTER_INDEX).equals(NSISStatements.NULL)) {
			sb.append("3X");
			if (caseSensitive) {
				sb.append('S');
			}
			sb.append("} ");
			sb.append(args.get(STRING_INDEX).getValue());
			sb.append(' ');
			sb.append(args.get(DELIM1_INDEX).getValue());
			sb.append(' ');
			sb.append(args.get(CENTER_INDEX).getValue());
			sb.append(' ');
			sb.append(args.get(DELIM2_INDEX).getValue());
			sb.append(' ');
		} else if (!args.get(DELIM2_INDEX).equals(NSISStatements.NULL)) {
			sb.append("2X");
			if (caseSensitive) {
				sb.append('S');
			}
			sb.append("} ");
			sb.append(args.get(STRING_INDEX).getValue());
			sb.append(' ');
			sb.append(args.get(DELIM1_INDEX).getValue());
			sb.append(' ');
			sb.append(args.get(DELIM2_INDEX).getValue());
			sb.append(' ');
		} else {
			if (caseSensitive) {
				sb.append('S');
			}
			sb.append("} ");
			sb.append(args.get(STRING_INDEX).getValue());
			sb.append(' ');
			sb.append(args.get(DELIM1_INDEX).getValue());
			sb.append(' ');
		}
		sb.append(args.get(OPTIONS_INDEX).getValue());
		sb.append(' ');
		sb.append(returnVar.getValue());
		return sb.toString();
	}

	@Override
	public String getMarkdownHelp() {
		return "Multi-features string function. Acts as a convenience function for the NSIS "
				+ "[WordFind](http://nsis.sourceforge.net/WordFind) when passing `delim1` alone, "
				+ "as [WordFind2X](http://nsis.sourceforge.net/WordFind2X) when defining "
				+ "`delim2`, and as [WordFind3X](http://nsis.sourceforge.net/WordFind3X) when "
				+ "defining also `center`. See NSIS documentation for `options` and detailed "
				+ "usage. Use `WordFindS(...)` for case-sensitive string comparison.\r\n"
				+ "\r\n"
				+ "    r1 = WordFind(\"C:\\io.sys C:\\Program Files C:\\WINDOWS\", \"-02\", \" C:\\\") ; <-- \"Program Files\"\r\n"
				+ "    r2 = WordFindS(\"[C:\\io.sys];[c:\\logo.sys];[C:\\WINDOWS]\", \"+2\", \"[C:\\\", \"];\") ; <-- \"WINDOWS\"\r\n"
				+ "    r3 = WordFind(\"[1.AAB];[2.BAA];[3.BBB];\", \"+1\", \"[\", \"];\", \"AA\") ; <-- \"1.AAB\"\r\n";
	}

}
