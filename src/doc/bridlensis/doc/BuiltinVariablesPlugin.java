package bridlensis.doc;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.markdown4j.Plugin;

import bridlensis.env.EnvironmentFactory;

public class BuiltinVariablesPlugin extends Plugin {

	public BuiltinVariablesPlugin() {
		super("builtinvars");
	}

	@Override
	public void emit(StringBuilder out, List<String> lines,
			Map<String, String> params) {
		out.append("<pre><code>");
		try (Scanner scanner = new Scanner(
				EnvironmentFactory.getBuiltinVariablesDef())) {
			out.append(scanner.next().toLowerCase());
			while (scanner.hasNextLine()) {
				out.append(scanner.nextLine());
				out.append("\r\n");
			}
		}
		out.append("</code></pre>");
	}

}
