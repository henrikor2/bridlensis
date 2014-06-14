package bridlensis.env;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class BuiltinElements {

	public static InputStream getBuiltinFunctionsDef() {
		return BuiltinElements.class
				.getResourceAsStream("builtin_instructions.conf");
	}

	public static Map<String, Callable> loadBuiltinFunctions(
			NameGenerator nameGenerator) {
		Map<String, Callable> functions = new HashMap<>();

		// NSIS instructions as functions
		Scanner scanner = new Scanner(getBuiltinFunctionsDef());
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.length() > 0 && line.charAt(0) != '#') {
				Instruction instruction = Instruction.parse(line);
				add(functions, instruction, instruction.getDisplayName()
						.toLowerCase());
			}
		}
		scanner.close();

		// Built-in Bridle functions
		add(functions,
				new FunctionMsgBox(nameGenerator, functions.get("strcpy")),
				"msgbox");
		add(functions, new FunctionFile(), "file");
		add(functions, new FunctionReserveFile(), "reservefile");
		add(functions, new FunctionCopy(), "filecopy", "copy");
		add(functions, new FunctionDelete(), "filedelete", "delete");
		add(functions, new FunctionRename(), "filerename", "rename");
		add(functions, new FunctionRMDir(), "rmdir");
		add(functions, new FunctionDeleteRegKey(), "deleteregkey");
		add(functions, new FunctionGetFullPathName(), "getfullpathname");
		add(functions, new FunctionWordFind(false), "wordfind");
		add(functions, new FunctionWordFind(true), "wordfinds");
		add(functions, new FunctionWordReplace(false), "wordreplace");
		add(functions, new FunctionWordReplace(true), "wordreplaces");
		add(functions, new FunctionWordInsert(false), "wordinsert");
		add(functions, new FunctionWordInsert(true), "wordinserts");
		add(functions, new FunctionConfigRead(false), "configread");
		add(functions, new FunctionConfigRead(true), "configreads");
		add(functions, new FunctionConfigWrite(false), "configwrite");
		add(functions, new FunctionConfigWrite(true), "configwrites");

		return functions;
	}

	private static void add(Map<String, Callable> functions, Callable function,
			String... aliases) {
		for (String name : aliases) {
			if (functions.containsKey(name)) {
				throw new java.lang.AssertionError("Function " + name
						+ " already defined.");
			}
			functions.put(name, function);
		}
	}

	public static Map<String, Variable> loadBuiltinVariables() {
		Map<String, Variable> vars = new HashMap<>();
		Scanner scanner = new Scanner(
				BuiltinElements.class
						.getResourceAsStream("builtin_variables.conf"));
		while (scanner.hasNext()) {
			Variable variable = new Variable(scanner.next().toLowerCase());
			add(vars, variable);
		}
		scanner.close();
		return vars;
	}

	private static void add(Map<String, Variable> vars, Variable variable)
			throws AssertionError {
		if (vars.containsKey(variable.getName())) {
			throw new java.lang.AssertionError("Variable " + variable.getName()
					+ " already defined.");
		}
		vars.put(variable.getName(), variable);
	}

}
