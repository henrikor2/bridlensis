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

}
