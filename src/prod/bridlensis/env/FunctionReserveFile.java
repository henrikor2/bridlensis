package bridlensis.env;

import java.util.List;

import bridlensis.NSISStatements;

class FunctionReserveFile extends CustomFunction {

	private static final int FILE_INDEX = 0;
	private static final int OPTIONS_INDEX = 1;

	protected FunctionReserveFile() {
		super(1, ReturnType.VOID, "ReserveFile");
		registerArguments("file", "options");
	}

	@Override
	public String statementFor(String indent, List<TypeObject> args,
			Variable returnVar) {
		StringBuilder sb = new StringBuilder(indent);
		sb.append("ReserveFile ");
		if (!args.get(OPTIONS_INDEX).equals(NSISStatements.NULL)) {
			String options = SimpleTypeObject.stripString(args
					.get(OPTIONS_INDEX));
			if (!options.isEmpty()) {
				sb.append(options);
				sb.append(' ');
			}
		}
		sb.append(args.get(FILE_INDEX).getValue());
		return sb.toString();
	}

	@Override
	public String getMarkdownHelp() {
		return "Reserves a file in the data block for later use. See NSIS ReserveFile "
				+ "instruction documentation for options.\r\n"
				+ "\r\n"
				+ "    ReserveFile(\"time.dll\", \"/plugin\")\r\n";
	}

}
