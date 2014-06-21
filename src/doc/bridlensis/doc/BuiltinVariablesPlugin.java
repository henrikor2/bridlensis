package bridlensis.doc;

import java.util.List;
import java.util.Map;

import org.markdown4j.Plugin;

import bridlensis.env.EnvironmentFactory;
import bridlensis.env.Variable;

public class BuiltinVariablesPlugin extends Plugin {

	public BuiltinVariablesPlugin() {
		super("builtinvars");
	}

	@Override
	public void emit(StringBuilder out, List<String> lines,
			Map<String, String> params) {
		out.append("<p>Supported built-in variables include: ");
		List<Variable> variables = EnvironmentFactory.getBuiltinVariables();
		out.append(variablePrintout(variables.get(0)));
		for (int i = 1; i < variables.size() - 1; i++) {
			out.append(", ");
			out.append(variablePrintout(variables.get(i)));
		}
		out.append(", and ");
		out.append(variablePrintout(variables.get(variables.size() - 1)));
		out.append(".</p>");
	}

	private String variablePrintout(Variable variable) {
		return "<code>" + variable.getName() + "</code>";
	}

}
