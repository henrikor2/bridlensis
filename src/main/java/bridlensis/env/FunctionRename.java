package bridlensis.env;

import java.util.List;

import bridlensis.InvalidSyntaxException;
import bridlensis.NSISStatements;

public class FunctionRename extends CustomFunction {

	private static final int SOURCE_INDEX = 0;
	private static final int TARGET_INDEX = 1;
	private static final int OPTIONS_INDEX = 2;

	protected FunctionRename() {
		super(2, ReturnType.ERRORFLAG, "FileRename", "Rename");
		registerArguments("source", "target", "options");
	}

	@Override
	public String statementFor(String indent, List<TypeObject> args,
			Variable returnVar) throws InvalidSyntaxException {
		StringBuilder sb = new StringBuilder(indent);
		sb.append("Rename ");
		if (!args.get(OPTIONS_INDEX).equals(NSISStatements.NULL)) {
			String options = SimpleTypeObject.stripString(args
					.get(OPTIONS_INDEX));
			if (!options.isEmpty()) {
				sb.append(options);
				sb.append(' ');
			}
		}
		sb.append(args.get(FunctionRename.SOURCE_INDEX).getValue());
		sb.append(" ");
		sb.append(args.get(FunctionRename.TARGET_INDEX).getValue());
		return sb.toString();
	}

	@Override
	public String getMarkdownHelp() {
		return "Rename source file to target file. See NSIS documentation for supported "
				+ "options (`/REBOOTOK`) and their behavior. Error flag is cleared if the return "
				+ "value `0` for success is assigned for a variable or used in operation. Returns "
				+ "`1` for error with error flag set.\r\n"
				+ "\r\n"
				+ "    If Rename(\"C:\\autoexec.bat\", \"C:\\autoexec.bak\") == 0\r\n"
				+ "        DetailPrint \"Rename succeeded.\"\r\n"
				+ "        ...\r\n";
	}

}
