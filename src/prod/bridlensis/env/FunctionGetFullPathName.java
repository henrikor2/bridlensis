package bridlensis.env;

import java.util.List;

import bridlensis.NSISStatements;

class FunctionGetFullPathName extends CustomFunction {

	private static final int PATH_INDEX = 0;
	private static final int OPTIONS_INDEX = 1;

	protected FunctionGetFullPathName() {
		super(1, ReturnType.REQUIRED, "GetFullPathName");
		registerArguments("path", "options");
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
