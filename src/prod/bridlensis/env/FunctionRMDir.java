package bridlensis.env;

import java.util.List;

import bridlensis.InvalidSyntaxException;
import bridlensis.NSISStatements;

public class FunctionRMDir extends Callable {

	private static final int DIR_INDEX = 0;
	private static final int OPTIONS_INDEX = 1;

	protected FunctionRMDir() {
		super("RMDir");
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

}
