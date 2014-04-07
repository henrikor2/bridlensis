package bridlensis.env;

import java.util.List;

import bridlensis.InvalidSyntaxException;

public class FunctionCopy implements Callable {

	private static final int SOURCE_INDEX = 0;
	private static final int TARGET_INDEX = 1;

	FunctionCopy() {
	}

	@Override
	public int getMandatoryArgsCount() {
		return 2;
	}

	@Override
	public int getArgsCount() {
		return 2;
	}

	@Override
	public ReturnType getReturnType() {
		return ReturnType.ERRORFLAG;
	}

	@Override
	public String statementFor(String indent, List<String> args,
			Variable returnVar) throws InvalidSyntaxException {
		StringBuilder sb = new StringBuilder(indent);
		sb.append("CopyFiles /SILENT ");
		sb.append(args.get(SOURCE_INDEX));
		sb.append(" ");
		sb.append(args.get(TARGET_INDEX));
		return sb.toString();
	}

}
