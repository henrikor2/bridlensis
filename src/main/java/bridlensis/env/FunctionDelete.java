package bridlensis.env;

import java.util.List;

import bridlensis.NSISStatements;

class FunctionDelete extends CustomFunction {

	private static final int FILE_INDEX = 0;
	private static final int OPTIONS_INDEX = 1;

	protected FunctionDelete() {
		super(1, ReturnType.ERRORFLAG, "FileDelete", "Delete");
		registerArguments("file", "options");
	}

	@Override
	public String statementFor(String indent, List<TypeObject> args,
			Variable returnVar) {
		StringBuilder sb = new StringBuilder(indent);
		sb.append("Delete ");
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
		return "Delete file (which can be a file or wildcard, but should be specified with a "
				+ "full path) from the target system. See NSIS documentation for supported "
				+ "options (`/REBOOTOK`) and their behavior. Error flag is cleared if the return "
				+ "value `0` for success is assigned for a variable or used in operation. Returns "
				+ "`1` for error with error flag set.\r\n"
				+ "\r\n"
				+ "    If Delete(\"C:\\autoexec.bat\") == 0\r\n"
				+ "        DetailPrint \"File delete succeeded.\"\r\n"
				+ "        ...\r\n";
	}

}
