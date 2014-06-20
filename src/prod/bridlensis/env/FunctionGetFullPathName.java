package bridlensis.env;

import java.util.List;

import bridlensis.NSISStatements;

class FunctionGetFullPathName extends Callable {

	private static final int PATH_INDEX = 0;
	private static final int OPTIONS_INDEX = 1;

	protected FunctionGetFullPathName() {
		super("GetFullPathName");
	}

	@Override
	public int getMandatoryArgsCount() {
		return 1;
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
		sb.append("GetFullPathName ");
		if (!args.get(OPTIONS_INDEX).equals(NSISStatements.NULL)) {
			String options = SimpleTypeObject.stripString(args
					.get(OPTIONS_INDEX));
			if (!options.isEmpty()) {
				sb.append(options);
				sb.append(' ');
			}
		}
		sb.append(returnVar.getValue());
		sb.append(' ');
		sb.append(args.get(PATH_INDEX).getValue());
		return sb.toString();
	}

}
