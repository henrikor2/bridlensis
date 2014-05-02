package bridlensis.env;

import java.util.List;

import bridlensis.InvalidSyntaxException;
import bridlensis.StatementFactory;

public class FunctionRename implements Callable {

	private static final int SOURCE_INDEX = 0;
	private static final int TARGET_INDEX = 1;
	private static final int OPTIONS_INDEX = 2;

	FunctionRename() {
	}

	@Override
	public int getMandatoryArgsCount() {
		return 2;
	}

	@Override
	public int getArgsCount() {
		return 3;
	}

	@Override
	public ReturnType getReturnType() {
		return ReturnType.ERRORFLAG;
	}

	@Override
	public String statementFor(String indent, List<TypeObject> args,
			Variable returnVar) throws InvalidSyntaxException {
		StringBuilder sb = new StringBuilder(indent);
		sb.append("Rename ");
		if (!args.get(OPTIONS_INDEX).equals(StatementFactory.NULL)) {
			String options = StatementFactory.deString(args.get(OPTIONS_INDEX));
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

}
