package bridlensis.env;

import java.util.Arrays;
import java.util.List;

import bridlensis.InvalidSyntaxException;

public abstract class Callable {

	public static enum ReturnType {
		VOID, OPTIONAL, REQUIRED, ERRORFLAG
	}

	private final List<String> aliases;

	protected Callable(String... aliases) {
		if (aliases.length == 0) {
			throw new AssertionError("Function name not defined");
		}
		this.aliases = Arrays.asList(aliases);
	}

	public String getName() {
		return aliases.get(0);
	}

	public Iterable<String> getAliases() {
		return aliases;
	}

	public abstract int getMandatoryArgsCount();

	public abstract int getArgsCount();

	public abstract ReturnType getReturnType();

	public abstract String statementFor(String indent, List<TypeObject> args,
			Variable returnVar) throws InvalidSyntaxException;

	@Override
	public String toString() {
		return "Function[" + getName() + "]";
	}

}