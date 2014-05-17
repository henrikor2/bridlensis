package bridlensis.env;

import java.util.List;

import bridlensis.InvalidSyntaxException;
import bridlensis.NSISStatements;

public class FunctionFile implements Callable {

	private static final int FILE_INDEX = 0;
	private static final int OPTIONS_INDEX = 1;
	private static final int OUTPATH_INDEX = 2;

	FunctionFile() {
	}

	@Override
	public int getMandatoryArgsCount() {
		return 1;
	}

	@Override
	public int getArgsCount() {
		return 3;
	}

	@Override
	public ReturnType getReturnType() {
		return ReturnType.VOID;
	}

	@Override
	public String statementFor(String indent, List<TypeObject> args,
			Variable returnVar) throws InvalidSyntaxException {
		StringBuilder sb = new StringBuilder(indent);
		if (!args.get(OUTPATH_INDEX).equals(NSISStatements.NULL)) {
			sb.append("SetOutPath ");
			sb.append(args.get(OUTPATH_INDEX).getValue());
			sb.append(NSISStatements.NEWLINE_MARKER);
			sb.append(indent);
		}
		sb.append("File ");
		if (!args.get(OPTIONS_INDEX).equals(NSISStatements.NULL)) {
			String options = NSISStatements.deString(args.get(OPTIONS_INDEX));
			if (!options.isEmpty()) {
				sb.append(options);
				sb.append(' ');
			}
		}
		sb.append(args.get(FILE_INDEX).getValue());
		return sb.toString();
	}

	@Override
	public String toString() {
		return "Function[file]";
	}

}
