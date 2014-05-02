package bridlensis.env;

import java.util.List;

import bridlensis.StatementFactory;

class FunctionDelete implements Callable {

	private static final int FILE_INDEX = 0;
	private static final int OPTIONS_INDEX = 1;

	FunctionDelete() {
	}

	@Override
	public int getMandatoryArgsCount() {
		return 1;
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
	public String statementFor(String indent, List<TypeObject> args,
			Variable returnVar) {
		StringBuilder sb = new StringBuilder(indent);
		sb.append("Delete ");
		if (!args.get(OPTIONS_INDEX).equals(StatementFactory.NULL)) {
			String options = StatementFactory.deString(args.get(OPTIONS_INDEX));
			if (!options.isEmpty()) {
				sb.append(options);
				sb.append(' ');
			}
		}
		sb.append(args.get(FILE_INDEX).getValue());
		return sb.toString();
	}

}
