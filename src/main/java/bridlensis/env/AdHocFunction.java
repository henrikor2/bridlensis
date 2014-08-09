package bridlensis.env;

import java.util.List;

import bridlensis.InvalidSyntaxException;
import bridlensis.NSISStatements;

class AdHocFunction extends Callable {

	public AdHocFunction(String name) {
		super(name);
	}

	@Override
	public int getMandatoryArgsCount() {
		return 0;
	}

	@Override
	public int getArgsCount() {
		return Callable.UNLIMITED_ARGS;
	}

	@Override
	public Variable getArgument(int index) {
		throw new AssertionError();
	}

	@Override
	public ReturnType getReturnType() {
		return ReturnType.OPTIONAL;
	}

	@Override
	public String statementFor(String indent, List<TypeObject> args,
			Variable returnVar) throws InvalidSyntaxException {
		StringBuilder sb = new StringBuilder(indent);
		for (int i = args.size() - 1; i >= 0; i--) {
			sb.append("Push ");
			sb.append(args.get(i).getValue());
			sb.append(NSISStatements.NEWLINE_MARKER);
			sb.append(indent);
		}
		sb.append("Call ");
		sb.append(getName());
		if (returnVar != null) {
			sb.append(NSISStatements.NEWLINE_MARKER);
			sb.append(indent);
			sb.append("Pop ");
			sb.append(returnVar.getValue());
		}
		return sb.toString();
	}

	@Override
	public String getDescription() {
		throw new AssertionError();
	}

	@Override
	public String getMarkdownHelp() {
		throw new AssertionError();
	}

}
