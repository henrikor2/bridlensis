package bridlensis.env;

import java.util.List;

import bridlensis.InvalidSyntaxException;

public class FunctionCopy extends CustomFunction {

	private static final int SOURCE_INDEX = 0;
	private static final int TARGET_INDEX = 1;

	protected FunctionCopy() {
		super(2, ReturnType.ERRORFLAG, "FileCopy", "Copy");
		registerArguments("source", "target");
	}

	@Override
	public String statementFor(String indent, List<TypeObject> args,
			Variable returnVar) throws InvalidSyntaxException {
		StringBuilder sb = new StringBuilder(indent);
		sb.append("CopyFiles /SILENT ");
		sb.append(args.get(SOURCE_INDEX).getValue());
		sb.append(" ");
		sb.append(args.get(TARGET_INDEX).getValue());
		return sb.toString();
	}

	@Override
	public String getMarkdownHelp() {
		return "Copies files silently from the source to the target on the installing system. "
				+ "Returns `0` for success with error flag cleared, `1` for error with error flag "
				+ "set. Error flag is not cleared unless function return value is assigned for a "
				+ "variable or used in operation.\r\n"
				+ "\r\n"
				+ "    If Copy(\"C:\\autoexec.bat\", $%TEMP%) <> 0\r\n"
				+ "        Abort \"File copy failed.\"\r\n" 
				+ "    EndIf\r\n";
	}

}
