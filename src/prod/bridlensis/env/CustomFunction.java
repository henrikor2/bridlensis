package bridlensis.env;

public abstract class CustomFunction extends Callable {

	private final int mandatoryArgsCount;
	private final ReturnType returnType;

	protected CustomFunction(int mandatoryArgsCount, ReturnType returnType,
			String... aliases) {
		super(aliases);
		this.mandatoryArgsCount = mandatoryArgsCount;
		this.returnType = returnType;
	}

	@Override
	public final int getMandatoryArgsCount() {
		return mandatoryArgsCount;
	}

	@Override
	public final ReturnType getReturnType() {
		return returnType;
	}

	@Override
	public String getDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append(getName());
		sb.append('(');
		for (int i = 0; i < getArgsCount(); i++) {
			if (i >= getMandatoryArgsCount()) {
				sb.append(" [");
			}
			if (i != 0) {
				sb.append(", ");
			}
			sb.append(getArgument(i).getName());
		}
		for (int i = getMandatoryArgsCount(); i < getArgsCount(); i++) {
			sb.append(']');
		}
		sb.append(')');
		return sb.toString();
	}

}
