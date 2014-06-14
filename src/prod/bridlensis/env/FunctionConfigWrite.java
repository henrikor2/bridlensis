package bridlensis.env;

import java.util.List;

class FunctionConfigWrite implements Callable {

	private static final int FILE_INDEX = 0;
	private static final int ENTRY_INDEX = 1;
	private static final int VALUE_INDEX = 2;

	private final boolean caseSensitive;

	FunctionConfigWrite(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	@Override
	public int getMandatoryArgsCount() {
		return 3;
	}

	@Override
	public int getArgsCount() {
		return 3;
	}

	@Override
	public ReturnType getReturnType() {
		return ReturnType.REQUIRED;
	}

	@Override
	public String statementFor(String indent, List<TypeObject> args,
			Variable returnVar) {
		StringBuilder sb = new StringBuilder(indent);
		sb.append("${ConfigWrite");
		if (caseSensitive) {
			sb.append('S');
		}
		sb.append("} ");
		sb.append(args.get(FILE_INDEX).getValue());
		sb.append(' ');
		sb.append(args.get(ENTRY_INDEX).getValue());
		sb.append(' ');
		sb.append(args.get(VALUE_INDEX).getValue());
		sb.append(' ');
		sb.append(returnVar.getValue());
		return sb.toString();
	}

	@Override
	public String toString() {
		return "Function[configwrite]";
	}

}
