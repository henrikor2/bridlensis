package bridlensis.env;

import java.util.List;

class FunctionWordReplace implements Callable {

	private static final int STRING_INDEX = 0;
	private static final int WORD1_INDEX = 1;
	private static final int WORD2_INDEX = 2;
	private static final int OPTIONS_INDEX = 3;

	private final boolean caseSensitive;

	FunctionWordReplace(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	@Override
	public int getMandatoryArgsCount() {
		return 4;
	}

	@Override
	public int getArgsCount() {
		return 4;
	}

	@Override
	public ReturnType getReturnType() {
		return ReturnType.REQUIRED;
	}

	@Override
	public String statementFor(String indent, List<String> args,
			Variable returnVar) {
		StringBuilder sb = new StringBuilder(indent);
		sb.append("${WordReplace");
		if (caseSensitive) {
			sb.append('S');
		}
		sb.append("} ");
		sb.append(args.get(STRING_INDEX));
		sb.append(' ');
		sb.append(args.get(WORD1_INDEX));
		sb.append(' ');
		sb.append(args.get(WORD2_INDEX));
		sb.append(' ');
		sb.append(args.get(OPTIONS_INDEX));
		sb.append(' ');
		sb.append(returnVar.getNSISExpression());
		return sb.toString();
	}

}
