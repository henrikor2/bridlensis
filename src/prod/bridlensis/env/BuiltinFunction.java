package bridlensis.env;

import java.lang.reflect.InvocationTargetException;

abstract class BuiltinFunction extends Callable {

	protected static BuiltinFunction parse(String line,
			Class<? extends BuiltinFunction> instanceClass) {
		String[] parts = line.split(" ");
		int returnArgIndex = -1;
		for (int i = 1; i < parts.length; i++) {
			if (parts[i].equals("output")) {
				returnArgIndex = i - 1;
				break;
			}
		}
		int argsCount = returnArgIndex == -1 ? parts.length - 1
				: parts.length - 2;
		try {
			return instanceClass.getConstructor(String.class, Integer.TYPE,
					Integer.TYPE).newInstance(parts[0], argsCount,
					returnArgIndex);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new java.lang.AssertionError(e);
		}
	}

	private int argsCount;
	private int returnArgIndex;

	protected BuiltinFunction(String name, int argsCount, int returnArgIndex) {
		super(name);
		this.argsCount = argsCount;
		this.returnArgIndex = returnArgIndex;
	}

	protected int getReturnArgIndex() {
		return returnArgIndex;
	}

	@Override
	public int getMandatoryArgsCount() {
		return 0;
	}

	@Override
	public int getArgsCount() {
		return argsCount;
	}

	@Override
	public ReturnType getReturnType() {
		return returnArgIndex == -1 ? ReturnType.VOID : ReturnType.REQUIRED;
	}

}
