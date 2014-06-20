package bridlensis.env;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Environment {

	private static final String ALLOWED_NAME_REGEX = "[abcdefghijklmnopqrstuvwxyz0123456789_]*";

	private static final String GLOBAL_VARIABLE_PREFIX = "global.";

	private static List<String> RESERVED_WORDS = Arrays.asList("var", "global",
			"function", "return", "functionend", "if", "not", "or", "and",
			"elseif", "else", "endif", "do", "while", "until", "continue",
			"break", "loop");

	private final Map<String, Variable> vars;
	private final Map<String, Callable> callables;

	protected Environment() {
		vars = new HashMap<>();
		callables = new HashMap<>();
	}

	protected void add(Variable variable) {
		if (vars.containsKey(variable.getName())) {
			throw new AssertionError("Variable " + variable.getName()
					+ " already exists.");
		}
		vars.put(variable.getName(), variable);
	}

	protected void add(Callable callable) {
		for (String alias : callable.getAliases()) {
			String normalizedName = alias.toLowerCase();
			if (callables.containsKey(normalizedName)) {
				throw new AssertionError("Function alias " + alias
						+ " already exists.");
			}
			callables.put(normalizedName, callable);
		}
	}

	public Variable registerVariable(String name, UserFunction enclosingFunction)
			throws EnvironmentException {
		name = name.toLowerCase();
		if (RESERVED_WORDS.contains(name)) {
			throw new EnvironmentException(
					"Variable name cannot be a reserved word");
		}
		if (!name.matches(ALLOWED_NAME_REGEX)) {
			throw new EnvironmentException(
					"Variable name cannot contain special characters");
		}
		Variable variable;
		if (enclosingFunction != null) {
			variable = new Variable(enclosingFunction.getName() + "." + name);
		} else {
			variable = new Variable(name);
		}
		if (vars.containsKey(variable.getName())) {
			throw new EnvironmentException("Variable already defined");
		}
		vars.put(variable.getName(), variable);
		return variable;
	}

	public boolean containsVariable(String name, UserFunction enclosingFunction)
			throws EnvironmentException {
		return vars.containsKey(getNormalizedVariableName(name,
				enclosingFunction));
	}

	public Variable getVariable(String name, UserFunction enclosingFunction)
			throws EnvironmentException {
		String normalizedVariableName = getNormalizedVariableName(name,
				enclosingFunction);
		Variable variable = vars.get(normalizedVariableName);
		if (variable == null) {
			throw new EnvironmentException(String.format(
					"Unknown variable '%s'", normalizedVariableName));
		}
		return variable;
	}

	private String getNormalizedVariableName(String name,
			UserFunction enclosingFunction) throws EnvironmentException {
		name = name.toLowerCase();
		if (name.startsWith(GLOBAL_VARIABLE_PREFIX)) {
			name = name.substring(GLOBAL_VARIABLE_PREFIX.length());
		} else if (enclosingFunction != null
				&& !name.startsWith(enclosingFunction.getName() + ".")) {
			name = enclosingFunction.getName() + "." + name;
		}

		return name;
	}

	public Callable getCallable(String name) throws EnvironmentException {
		name = name.toLowerCase();
		if (!callables.containsKey(name)) {
			throw new EnvironmentException(String.format(
					"Function '%s' not found", name));
		}
		return callables.get(name);
	}

	public UserFunction registerUserFunction(String name)
			throws EnvironmentException {
		name = name.toLowerCase();
		if (callables.containsKey(name)) {
			throw new EnvironmentException(String.format(
					"Function '%s' already exists", name));
		}
		if (RESERVED_WORDS.contains(name)) {
			throw new EnvironmentException(
					"Function name cannot be a reserved word");
		}
		if (name.charAt(0) != '.' && !name.matches(ALLOWED_NAME_REGEX)) {
			throw new EnvironmentException(
					"Function name cannot contain special characters");
		}
		UserFunction function = new UserFunction(name);
		callables.put(name, function);
		return function;
	}

}
