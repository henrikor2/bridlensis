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

	@Override
	public String getMarkdownHelp() {
		return "Returns the full path of the file specified. If the path portion of the "
				+ "parameter is not found, the error flag will be set and return value will be "
				+ "empty. See NSIS documentation for supported options (`/SHORT`). \r\n"
				+ "\r\n" 
				+ "    absolutePath = GetFullPathName(programfiles + \"\\NSIS\")\r\n";
	}

}
