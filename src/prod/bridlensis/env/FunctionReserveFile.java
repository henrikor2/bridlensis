package bridlensis.env;

import java.util.List;

import bridlensis.StatementFactory;

class FunctionReserveFile implements Callable {

	private static final int FILE_INDEX = 0;
	private static final int OPTIONS_INDEX = 1;

	FunctionReserveFile() {
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
		return ReturnType.VOID;
	}

	@Override
	public String statementFor(String indent, List<String> args,
			Variable returnVar) {
		StringBuilder sb = new StringBuilder(indent);
		sb.append("ReserveFile ");
		if (!args.get(OPTIONS_INDEX).equals(StatementFactory.NULL)) {
			String options = StatementFactory.deString(args.get(OPTIONS_INDEX));
			if (!options.isEmpty()) {
				sb.append(options);
				sb.append(' ');
			}
		}
		sb.append(args.get(FILE_INDEX));
		return sb.toString();
	}

}
