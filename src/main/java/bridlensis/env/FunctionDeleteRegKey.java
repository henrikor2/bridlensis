package bridlensis.env;

import java.util.List;

import bridlensis.NSISStatements;

class FunctionDeleteRegKey extends CustomFunction {

	private static final int ROOT_KEY_INDEX = 0;
	private static final int SUBKEY_INDEX = 1;
	private static final int OPTIONS_INDEX = 2;

	protected FunctionDeleteRegKey() {
		super(2, ReturnType.ERRORFLAG, "DeleteRegKey");
		registerArguments("root", "key", "options");
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
	public String getMarkdownHelp() {
		return "Deletes a registry key. See NSIS documentation for supported options "
				+ "(`/ifempty`). Error flag is cleared if the return value `0` for success is "
				+ "assigned for a variable or used in operation. Returns `1` for error with error "
				+ "flag set.\r\n"
				+ "\r\n"
				+ "    DeleteRegKey(\"HKLM\", \"Software\\BridleNSIS\", \"/ifempty\")\r\n";
	}

}
