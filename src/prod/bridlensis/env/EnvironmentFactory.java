package bridlensis.env;

import java.io.InputStream;
import java.util.Scanner;

public class EnvironmentFactory {

	public static Environment build(NameGenerator nameGenerator) {
		Environment environment = new Environment();

		// Built-in variables
		try (Scanner scanner = new Scanner(
				EnvironmentFactory.class
						.getResourceAsStream("builtin_variables.conf"))) {
			while (scanner.hasNext()) {
				environment.add(new Variable(scanner.next().toLowerCase()));
			}
		}

		// NSIS instructions as functions
		add(environment, Instruction.class, getBuiltinInstructionsDef());

		// NSIS function headers as functions
		add(environment, HeaderFunction.class, getBuiltinHeaderFunctionsDef());

		// Built-in Bridle functions
		try {
			environment.add(new FunctionMsgBox(nameGenerator, environment
					.getCallable("strcpy")));
		} catch (EnvironmentException e) {
			throw new AssertionError(e);
		}
		environment.add(new FunctionFile());
		environment.add(new FunctionReserveFile());
		environment.add(new FunctionCopy());
		environment.add(new FunctionDelete());
		environment.add(new FunctionRename());
		environment.add(new FunctionRMDir());
		environment.add(new FunctionDeleteRegKey());
		environment.add(new FunctionGetFullPathName());
		environment.add(new FunctionWordFind(false));
		environment.add(new FunctionWordFind(true));

		return environment;
	}

	public static InputStream getBuiltinInstructionsDef() {
		return EnvironmentFactory.class
				.getResourceAsStream("builtin_instructions.conf");
	}

	public static InputStream getBuiltinHeaderFunctionsDef() {
		return EnvironmentFactory.class
				.getResourceAsStream("builtin_functionheaders.conf");
	}

	private static void add(Environment environment,
			Class<? extends BuiltinFunction> instanceClass,
			InputStream definitions) {
		Scanner scanner = new Scanner(definitions);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.length() > 0 && line.charAt(0) != '#') {
				BuiltinFunction function = BuiltinFunction.parse(line,
						instanceClass);
				environment.add(function);
			}
		}
		scanner.close();
	}
}
