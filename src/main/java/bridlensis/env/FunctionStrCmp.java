package bridlensis.env;

import java.util.List;

import bridlensis.InvalidSyntaxException;
import bridlensis.NSISStatements;

public class FunctionStrCmp extends CustomFunction {

	private static final int STR1_INDEX = 0;
	private static final int STR2_INDEX = 1;
	private static final int EQL_INDEX = 2;
	private static final int NEQL_INDEX = 3;

	protected FunctionStrCmp() {
		super(3, ReturnType.VOID, "StrCmp");
		registerArguments("str1", "str2", "jump_if_equal", "jump_if_not_equal");
	}

	@Override
	public String statementFor(String indent, List<TypeObject> args,
			Variable returnVar) throws InvalidSyntaxException {
		StringBuilder sb = new StringBuilder(indent);
		sb.append("StrCmp ");
		sb.append(args.get(STR1_INDEX).getValue());
		sb.append(" ");
		sb.append(args.get(STR2_INDEX).getValue());
		sb.append(" ");
		sb.append(SimpleTypeObject.stripString(args.get(EQL_INDEX)));
		if (!args.get(NEQL_INDEX).equals(NSISStatements.NULL)) {
			sb.append(" ");
			sb.append(SimpleTypeObject.stripString(args.get(NEQL_INDEX)));
		}
		return sb.toString();
	}

	@Override
	public String getMarkdownHelp() {
		return "Compares (case insensitively) str1 to str2. "
				+ "If str1 and str2 are equal, Gotos jump_if_equal, "
				+ "otherwise Gotos jump_if_not_equal.\r\n" 
				+ "\r\n"
				+ "    StrCmp(r0, \"a string\", 0, \"+3\")\r\n"
				+ "        DetailPrint(\"$$R0 == 'a string'\")\r\n"
				+ "        Goto(\"+2\")\r\n"
				+ "        DetailPrint(\"$$R0 != 'a string'\")\r\n";
	}

}
