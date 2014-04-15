package bridlensis.env;

import java.util.List;

import bridlensis.StatementFactory;

class FunctionDeleteRegKey implements Callable {

	private static final int ROOT_KEY_INDEX = 0;
	private static final int SUBKEY_INDEX = 1;
	private static final int OPTIONS_INDEX = 2;

	FunctionDeleteRegKey() {
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
	public String statementFor(String indent, List<String> args,
			Variable returnVar) {
		StringBuilder sb = new StringBuilder(indent);
		sb.append("DeleteRegKey ");
		if (!args.get(OPTIONS_INDEX).equals(StatementFactory.NULL)) {
			String options = StatementFactory.deString(args.get(OPTIONS_INDEX));
			if (!options.isEmpty()) {
				sb.append(options);
				sb.append(' ');
			}
		}
		sb.append(args.get(ROOT_KEY_INDEX));
		sb.append(' ');
		sb.append(args.get(SUBKEY_INDEX));
		return sb.toString();
	}

}