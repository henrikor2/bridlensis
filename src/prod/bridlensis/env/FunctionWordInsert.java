package bridlensis.env;

import java.util.List;

class FunctionWordInsert implements Callable {

	private static final int STRING_INDEX = 0;
	private static final int DELIMITER_INDEX = 1;
	private static final int WORD_INDEX = 2;
	private static final int OPTIONS_INDEX = 3;

	private final boolean caseSensitive;

	FunctionWordInsert(boolean caseSensitive) {
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
	public String statementFor(String indent, List<TypeObject> args,
			Variable returnVar) {
		StringBuilder sb = new StringBuilder(indent);
		sb.append("${WordInsert");
		if (caseSensitive) {
			sb.append('S');
		}
		sb.append("} ");
		sb.append(args.get(STRING_INDEX).getValue());
		sb.append(' ');
		sb.append(args.get(DELIMITER_INDEX).getValue());
		sb.append(' ');
		sb.append(args.get(WORD_INDEX).getValue());
		sb.append(' ');
		sb.append(args.get(OPTIONS_INDEX).getValue());
		sb.append(' ');
		sb.append(returnVar.getValue());
		return sb.toString();
	}

	@Override
	public String toString() {
		return "Function[wordinsert]";
	}

}
