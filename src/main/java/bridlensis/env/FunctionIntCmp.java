package bridlensis.env;

import java.util.List;

import bridlensis.InvalidSyntaxException;
import bridlensis.NSISStatements;

public class FunctionIntCmp extends CustomFunction {

	private static final int VAL1_INDEX = 0;
	private static final int VAL2_INDEX = 1;
	private static final int EQL_INDEX = 2;
	private static final int LESS_INDEX = 3;
	private static final int MORE_INDEX = 4;

	protected FunctionIntCmp() {
		super(3, ReturnType.VOID, "IntCmp");
		registerArguments("val1", "val2", "jump_if_equal", "jump_if_val1_less",
				"jump_if_val1_more");
	}

	@Override
	public String statementFor(String indent, List<TypeObject> args,
			Variable returnVar) throws InvalidSyntaxException {
		StringBuilder sb = new StringBuilder(indent);
		sb.append("IntCmp ");
		sb.append(args.get(VAL1_INDEX).getValue());
		sb.append(" ");
		sb.append(args.get(VAL2_INDEX).getValue());
		sb.append(" ");
		sb.append(SimpleTypeObject.stripString(args.get(EQL_INDEX)));
		if (!args.get(LESS_INDEX).equals(NSISStatements.NULL)) {
			sb.append(" ");
			sb.append(SimpleTypeObject.stripString(args.get(LESS_INDEX)));
			if (!args.get(MORE_INDEX).equals(NSISStatements.NULL)) {
				sb.append(" ");
				sb.append(SimpleTypeObject.stripString(args.get(MORE_INDEX)));
			}
		}
		return sb.toString();
	}

	@Override
	public String getMarkdownHelp() {
		return "Compares two integers val1 and val2. "
				+ "If val1 and val2 are equal, Gotos jump_if_equal, "
				+ "otherwise if val1 < val2, Gotos jump_if_val1_less, "
				+ "otherwise if val1 > val2, Gotos jump_if_val1_more.\r\n"
				+ "\r\n"
				+ "    IntCmp(r0, 5, \"is5\", \"lessthan5\", \"morethan5\")\r\n"
				+ "    is5:\r\n" 
				+ "        DetailPrint(\"$$R0 == 5\")\r\n"
				+ "        Goto(\"done\")\r\n" 
				+ "    lessthan5:\r\n"
				+ "        DetailPrint(\"$$R0 < 5\")\r\n"
				+ "        Goto(\"done\")\r\n" 
				+ "    morethan5:\r\n"
				+ "        DetailPrint(\"$$R0 > 5\")\r\n"
				+ "        Goto(\"done\")\r\n" 
				+ "    done:\r\n";
	}

}
