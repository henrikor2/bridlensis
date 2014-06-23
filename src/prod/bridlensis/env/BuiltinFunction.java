package bridlensis.env;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

abstract class BuiltinFunction extends Callable {

	public static BuiltinFunction parse(String line,
			Class<? extends BuiltinFunction> instanceClass) {
		String[] parts = line.split(" ");
		List<String> args = new ArrayList<>();
		Integer returnArgIndex = null;
		for (int i = 1; i < parts.length; i++) {
			if (parts[i].equals("output")) {
				returnArgIndex = i - 1;
			} else {
				args.add(parts[i]);
			}
		}
		BuiltinFunction function;
		try {
			function = instanceClass.getConstructor(String.class).newInstance(
					parts[0]);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new java.lang.AssertionError(e);
		}
		if (returnArgIndex != null) {
			function.setReturnArgIndex(returnArgIndex);
		}
		for (String arg : args) {
			function.registerArguments(arg);
		}
		return function;
	}

	private int returnArgIndex = -1;

	protected BuiltinFunction(String name) {
		super(name);
	}

	protected void setReturnArgIndex(int index) {
		returnArgIndex = index;
	}

	protected int getReturnArgIndex() {
		return returnArgIndex;
	}

	@Override
	public int getMandatoryArgsCount() {
		return 0;
	}

	@Override
	public ReturnType getReturnType() {
		return returnArgIndex == -1 ? ReturnType.VOID : ReturnType.REQUIRED;
	}

	@Override
	public String getDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append(getName());
		sb.append('(');
		for (int i = 0; i < getArgsCount(); i++) {
			if (i != 0) {
				sb.append(", ");
			}
			sb.append(getArgument(i).getName());
		}
		sb.append(')');
		return sb.toString();
	}
}
