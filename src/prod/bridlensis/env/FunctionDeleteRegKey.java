package bridlensis.env;

import java.util.List;

import bridlensis.NSISStatements;

class FunctionDeleteRegKey implements Callable {

	private static final int ROOT_KEY_INDEX = 0;
	private static final int SUBKEY_INDEX = 1;
	private static final int OPTIONS_INDEX = 2;

	FunctionDeleteRegKey() {
	}

	@Override
	public int getMandatoryArgsCount() {
		return 2;
	}

	@Override
	public int getArgsCount() {
		return 3;
	}

	@Override
	public ReturnType getReturnType() {
		return ReturnType.ERRORFLAG;
	}

	@Override
	public String statementFor(String indent, List<TypeObject> args,
			Variable returnVar) {
		StringBuilder sb = new StringBuilder(indent);
		sb.append("DeleteRegKey ");
		if (!args.get(OPTIONS_INDEX).equals(NSISStatements.NULL)) {
			String options = SimpleTypeObject.stripString(args
					.get(OPTIONS_INDEX));
			if (!options.isEmpty()) {
				sb.append(options);
				sb.append(' ');
			}
		}
		sb.append(args.get(ROOT_KEY_INDEX).getValue());
		sb.append(' ');
		sb.append(args.get(SUBKEY_INDEX).getValue());
		return sb.toString();
	}

	@Override
	public String toString() {
		return "Function[deleteregkey]";
	}

}
