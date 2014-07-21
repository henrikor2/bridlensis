package bridlensis.env;

import java.util.List;

import bridlensis.InvalidSyntaxException;
import bridlensis.NSISStatements;

public class FunctionRMDir extends CustomFunction {

	private static final int DIR_INDEX = 0;
	private static final int OPTIONS_INDEX = 1;

	protected FunctionRMDir() {
		super(1, ReturnType.ERRORFLAG, "RMDir");
		registerArguments("dir", "options");
	}

	@Override
	public String statementFor(String indent, List<TypeObject> args,
			Variable returnVar) throws InvalidSyntaxException {
		StringBuilder sb = new StringBuilder(indent);
		sb.append("RMDir ");
		if (!args.get(OPTIONS_INDEX).equals(NSISStatements.NULL)) {
			String options = SimpleTypeObject.stripString(args
					.get(OPTIONS_INDEX));
			if (!options.isEmpty()) {
				sb.append(options);
				sb.append(' ');
			}
		}
		sb.append(args.get(DIR_INDEX).getValue());
		return sb.toString();
	}

	@Override
	public String getMarkdownHelp() {
		return "Remove the specified directory from the target system. See NSIS documentation "
				+ "for supported options (`/r` and `/REBOOTOK`) and their behavior. Error flag is "
				+ "cleared if the return value `0` for success is assigned for a variable or used "
				+ "in operation. Returns `1` for error with error flag set.\r\n"
				+ "\r\n"
				+ "    If RMDir(pluginsdir) <> 0\r\n"
				+ "        DetailPrint \"Error when deleting directory $PLUGINSDIR.\"\r\n"
				+ "        ...\r\n";
	}

}
