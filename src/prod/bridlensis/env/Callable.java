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

	public final String getName() {
		return aliases.get(0);
	}

	public final List<String> getAliases() {
		return aliases;
	}

	protected final void registerArguments(String... args) {
		for (String arg : args) {
			registerArgument(new Variable(arg));
		}
	}

	protected void registerArgument(Variable arg) {
		arguments.add(arg);
	}

	public final int getArgsCount() {
		return arguments.size();
	}

	public final Variable getArgument(int index) {
		return arguments.get(index);
	}

	public abstract int getMandatoryArgsCount();

	public abstract ReturnType getReturnType();

	public abstract String statementFor(String indent, List<TypeObject> args,
			Variable returnVar) throws InvalidSyntaxException;

	public abstract String getDescription();

	public abstract String getMarkdownHelp();

	@Override
	public String toString() {
		return "Function[" + getDescription() + "]";
	}

}
