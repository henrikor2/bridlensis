package bridlensis.doc;

import java.util.List;
import java.util.Map;

import org.markdown4j.Plugin;

import bridlensis.env.Callable;
import bridlensis.env.Callable.ReturnType;
import bridlensis.env.EnvironmentFactory;

public class FunctionsPlugin extends Plugin {

	public FunctionsPlugin() {
		super("functions");
	}

	@Override
	public void emit(StringBuilder out, List<String> lines,
			Map<String, String> params) {
		Iterable<Callable> functions;
		String type = params.get("type");
		if ("instruction".equalsIgnoreCase(type)) {
			functions = EnvironmentFactory.getBuiltinInstructionFunctions();
		} else if ("header".equalsIgnoreCase(type)) {
			functions = EnvironmentFactory.getBuiltinHeaderFunctions();
		} else {
			throw new AssertionError("Unknown type " + type);
		}
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
