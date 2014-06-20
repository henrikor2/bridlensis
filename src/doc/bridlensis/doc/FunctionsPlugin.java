package bridlensis.doc;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.markdown4j.Plugin;

import bridlensis.env.EnvironmentFactory;

public class FunctionsPlugin extends Plugin {

	public FunctionsPlugin() {
		super("functions");
	}

	@Override
	public void emit(StringBuilder out, List<String> lines,
			Map<String, String> params) {
		InputStream input;
		String type = params.get("type");
		if ("instruction".equalsIgnoreCase(type)) {
			input = EnvironmentFactory.getBuiltinInstructionsDef();
		} else if ("header".equalsIgnoreCase(type)) {
			input = EnvironmentFactory.getBuiltinHeaderFunctionsDef();
		} else {
			throw new AssertionError("Unknown type " + type);
		}
		try (Scanner scanner = new Scanner(input)) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (line.startsWith("#")) {
					out.append("<h4>");
					out.append(line.substring(1).trim());
					out.append(":</h4>\r\n");
				} else if (line.length() > 0) {
					out.append("<p class=\"func\">");
					if (line.indexOf(" output") != -1) {
						out.append("ret = ");
						line = line.replaceFirst(" output", "");
					}
					String[] parts = line.split("\\s+");
					out.append(parts[0]);
					out.append("(");
					for (int i = 1; i < parts.length; i++) {
						out.append(parts[i]);
						if (i + 1 < parts.length) {
							out.append(", ");
						}
					}
					out.append(")</p>\r\n");
				}
			}
		}
	}

}
