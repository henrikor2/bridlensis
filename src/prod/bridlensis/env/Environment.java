package bridlensis.env;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Environment {

	private static final String ALLOWED_NAME_REGEX = "[abcdefghijklmnopqrstuvwxyz0123456789_]*";

	private static final String GLOBAL_VARIABLE_PREFIX = "global.";

	private static List<String> RESERVED_WORDS = Arrays.asList("var", "global",
			"function", "return", "functionend", "if", "or", "and", "elseif",
			"else", "endif", "do", "while", "until", "continue", "break",
			"loop");

	public static Scanner getBuiltinInstructionsDef() {
		return new Scanner(
				Environment.class
						.getResourceAsStream("builtin_instructions.conf"));
	}

	public static Scanner getBuiltinVariablesDef() {
		return new Scanner(
				Environment.class.getResourceAsStream("builtin_variables.conf"));
	}

	private Map<String, Callable> callables;
	private Map<String, Variable> vars;
	private NameGenerator nameGenerator;

	public Environment(NameGenerator nameGenerator) {
		this.vars = new HashMap<>();
		this.callables = new HashMap<String, Callable>();
		this.nameGenerator = nameGenerator;
	}

	public NameGenerator getNameGenerator() {
		return nameGenerator;
	}

	public void loadBuiltinInstructions() {
		addBuiltinFunction(new FunctionMsgBox(nameGenerator));
		addBuiltinFunction(new FunctionFileCopy());
		addBuiltinFunction(new FunctionDelete());

		Scanner scanner = getBuiltinInstructionsDef();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.length() > 0 && line.charAt(0) != '#') {
				Instruction instruction = Instruction.parse(line);
				callables.put(instruction.getName(), instruction);
			}
		}
		scanner.close();
	}

	private void addBuiltinFunction(Callable function) {
		callables.put(function.getName(), function);
	}

	public void loadBuiltinVariables() {
		Scanner scanner = getBuiltinVariablesDef();
		while (scanner.hasNext()) {
			Variable variable = new Variable(scanner.next().toLowerCase());
			vars.put(variable.getName(), variable);
		}
		scanner.close();
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
		Variable variable = vars.get(getNormalizedVariableName(name,
				enclosingFunction));
		if (variable == null) {
			throw new EnvironmentException(String.format(
					"Unknown variable '%s'", name));
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
