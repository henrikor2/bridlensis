package bridlensis.env;

import java.util.List;

import bridlensis.NSISStatements;

public class UserFunction extends Callable {

	private boolean hasReturn = false;

	protected UserFunction(String name) {
		super(name);
	}

	@Override
	public void registerArgument(Variable arg) {
		super.registerArgument(arg);
	}

	@Override
	public int getMandatoryArgsCount() {
		return getArgsCount();
	}

	@Override
	public ReturnType getReturnType() {
		return hasReturn ? ReturnType.REQUIRED : ReturnType.VOID;
	}

	public void setHasReturn(boolean hasReturn) {
		this.hasReturn = hasReturn;
	}

	@Override
	public String statementFor(String indent, List<TypeObject> args,
			Variable returnVar) {
		StringBuilder sb = new StringBuilder(indent);
		for (int i = args.size() - 1; i >= 0; i--) {
			sb.append("Push ");
			sb.append(args.get(i).getValue());
			sb.append(NSISStatements.NEWLINE_MARKER);
			sb.append(indent);
		}
		sb.append("Call ");
		sb.append(getName());
		if (hasReturn) {
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
}