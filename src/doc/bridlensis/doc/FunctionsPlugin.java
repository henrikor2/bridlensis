package bridlensis.doc;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.markdown4j.Markdown4jProcessor;
import org.markdown4j.Plugin;

import bridlensis.env.Callable;
import bridlensis.env.Callable.ReturnType;
import bridlensis.env.Environment;
import bridlensis.env.EnvironmentException;
import bridlensis.env.EnvironmentFactory;

public class FunctionsPlugin extends Plugin {

	private static enum Type {
		INSTRUCTION, HEADER, CUSTOM
	}

	private static Environment environment = EnvironmentFactory.build(null);

	public FunctionsPlugin() {
		super("functions");
	}

	@Override
	public void emit(StringBuilder out, List<String> lines,
			Map<String, String> params) {
		Type type = Type.valueOf(params.get("type").toUpperCase());
		switch (type) {
		case INSTRUCTION:
			System.out.println("Print all instructions");
			printAll(EnvironmentFactory.getBuiltinInstructionFunctions(), out);
			break;
		case HEADER:
			System.out.println("Print all headers");
			printAll(EnvironmentFactory.getBuiltinHeaderFunctions(), out);
			break;
		case CUSTOM:
			try {
				for (String line : lines) {
					String name = line.trim().toLowerCase();
					System.out.println("Print function " + name);
					print(environment.getCallable(name), out);
				}
			} catch (EnvironmentException e) {
				throw new AssertionError(e);
			}
			break;
		}
	}

	private void print(Callable function, StringBuilder out) {
		out.append("<h4>");
		out.append(function.getDescription());
		out.append("</h4>\r\n");
		if (function.getAliases().size() > 1) {
			out.append("</p>Aliases: ");
			out.append(function.getAliases().get(1));
			for (int i = 2; i < function.getAliases().size(); i++) {
				out.append(", ");
				out.append(function.getAliases().get(i));
			}
			out.append("</p>\r\n");
		}
		try {
			out.append(new Markdown4jProcessor().process(function
					.getMarkdownHelp()));
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	private void printAll(Iterable<Callable> functions, StringBuilder out) {
		for (Callable function : functions) {
			out.append("<p class=\"func\">");
			if (function.getReturnType() != ReturnType.VOID) {
				out.append("ret = ");
			}
			out.append(function.getDescription());
			out.append("</p>\r\n");
		}
	}

}
