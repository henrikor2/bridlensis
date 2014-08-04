package bridlensis.env;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class EnvironmentFactory {

	public static List<Variable> getBuiltinVariables() {
		List<Variable> variables = new ArrayList<>();
		try (Scanner scanner = new Scanner(
				EnvironmentFactory.class
						.getResourceAsStream("builtin_variables.conf"),
				"UTF-8")) {
			while (scanner.hasNext()) {
				variables.add(new Variable(scanner.next().toLowerCase()));
			}
		}
		return variables;
	}

	public static List<Callable> getBuiltinHeaderFunctions() {
		return getBuiltinFunctions(
				EnvironmentFactory.class
						.getResourceAsStream("builtin_functionheaders.conf"),
				HeaderFunction.class);
	}

	public static List<Callable> getBuiltinInstructionFunctions() {
		return getBuiltinFunctions(
				EnvironmentFactory.class
						.getResourceAsStream("builtin_instructions.conf"),
				Instruction.class);
	}

	private static List<Callable> getBuiltinFunctions(InputStream definitions,
			Class<? extends BuiltinFunction> instanceClass) {
		List<Callable> functions = new ArrayList<>();
		try (Scanner scanner = new Scanner(definitions, "UTF-8")) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (line.length() > 0 && line.charAt(0) != '#') {
					functions.add(BuiltinFunction.parse(line, instanceClass));
				}
			}
		}
		return functions;
	}

	public static List<Callable> getBuiltinCustomFunctions(
			NameGenerator nameGenerator, Callable functionStrCpy) {
		List<Callable> customFunctions = new ArrayList<>();
		customFunctions.add(new FunctionMsgBox(nameGenerator, functionStrCpy));
		customFunctions.add(new FunctionFile());
		customFunctions.add(new FunctionReserveFile());
		customFunctions.add(new FunctionCopy());
		customFunctions.add(new FunctionDelete());
		customFunctions.add(new FunctionRename());
		customFunctions.add(new FunctionRMDir());
		customFunctions.add(new FunctionDeleteRegKey());
		customFunctions.add(new FunctionGetFullPathName());
		customFunctions.add(new FunctionWordFind(false));
		customFunctions.add(new FunctionWordFind(true));
		customFunctions.add(new FunctionStrCmp());
		customFunctions.add(new FunctionIntCmp());
		return customFunctions;
	}

	public static Environment build(NameGenerator nameGenerator) {
		Environment environment = new Environment();

		// Built-in variables
		List<Variable> variables = getBuiltinVariables();
		for (Variable variable : variables) {
			environment.add(variable);
		}

		// NSIS instructions as functions
		List<Callable> instrcutions = getBuiltinInstructionFunctions();
		for (Callable instruction : instrcutions) {
			environment.add(instruction);
		}

		// NSIS function headers as functions
		List<Callable> headers = getBuiltinHeaderFunctions();
		for (Callable header : headers) {
			environment.add(header);
		}

		Callable functionStrCpy;
		try {
			functionStrCpy = environment.getCallable("strcpy");
		} catch (EnvironmentException e) {
			throw new AssertionError(e);
		}

		// Built-in Bridle functions
		List<Callable> customFunctions = getBuiltinCustomFunctions(
				nameGenerator, functionStrCpy);
		for (Callable function : customFunctions) {
			environment.add(function);
		}

		return environment;
	}

}
