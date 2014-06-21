package bridlensis.env;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bridlensis.InvalidSyntaxException;

public abstract class Callable {

	public static enum ReturnType {
		VOID, OPTIONAL, REQUIRED, ERRORFLAG
	}

	private final List<String> aliases;
	private final List<Variable> arguments;

	protected Callable(String... aliases) {
		if (aliases.length == 0) {
			throw new AssertionError("Function name not defined");
		}
		this.aliases = Arrays.asList(aliases);
		this.arguments = new ArrayList<Variable>();
	}

	public String getName() {
		return aliases.get(0);
	}

	public Iterable<String> getAliases() {
		return aliases;
	}

	public void addArgument(Variable arg) {
		arguments.add(arg);
	}

	public int getArgsCount() {
		return arguments.size();
	}

	public Variable getArgument(int index) {
		return arguments.get(index);
	}

	public abstract int getMandatoryArgsCount();

	public abstract ReturnType getReturnType();

	public abstract String statementFor(String indent, List<TypeObject> args,
			Variable returnVar) throws InvalidSyntaxException;

	public String getDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append(getName());
		sb.append('(');
		for (int i = 0; i < arguments.size(); i++) {
			if (i != 0) {
				sb.append(", ");
			}
			sb.append(arguments.get(i).getName());
		}
		sb.append(')');
		return sb.toString();
	}

	@Override
	public String toString() {
		return "Function[" + getDescription() + "]";
	}

}