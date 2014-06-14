package bridlensis.env;

import java.util.List;

class FunctionConfigRead implements Callable {

	private static final int FILE_INDEX = 0;
	private static final int ENTRY_INDEX = 1;

	private final boolean caseSensitive;

	FunctionConfigRead(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	@Override
	public int getMandatoryArgsCount() {
		return 2;
	}

	@Override
	public int getArgsCount() {
		return 2;
	}

	@Override
	public ReturnType getReturnType() {
		return ReturnType.REQUIRED;
	}

	@Override
	public String statementFor(String indent, List<TypeObject> args,
			Variable returnVar) {
		StringBuilder sb = new StringBuilder(indent);
		sb.append("${ConfigRead");
		if (caseSensitive) {
			sb.append('S');
		}
		sb.append("} ");
		sb.append(args.get(FILE_INDEX).getValue());
		sb.append(' ');
		sb.append(args.get(ENTRY_INDEX).getValue());
		sb.append(' ');
		sb.append(returnVar.getValue());
		return sb.toString();
	}

	@Override
	public String toString() {
		return "Function[wordinsert]";
	}

}
