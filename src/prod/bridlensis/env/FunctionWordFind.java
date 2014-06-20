package bridlensis.env;

import java.util.List;

import bridlensis.NSISStatements;

class FunctionWordFind extends Callable {

	private static final int STRING_INDEX = 0;
	private static final int OPTIONS_INDEX = 1;
	private static final int DELIM1_INDEX = 2;
	private static final int DELIM2_INDEX = 3;
	private static final int CENTER_INDEX = 4;

	private final boolean caseSensitive;

	protected FunctionWordFind(boolean caseSensitive) {
		super("WordFind" + (caseSensitive ? "S" : ""));
		this.caseSensitive = caseSensitive;
	}

	@Override
	public int getMandatoryArgsCount() {
		return 3;
	}

	@Override
	public int getArgsCount() {
		return 5;
	}

	@Override
	public ReturnType getReturnType() {
		return ReturnType.REQUIRED;
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

}
