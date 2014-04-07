package bridlensis;

import java.util.Iterator;
import java.util.List;

import bridlensis.env.Callable;
import bridlensis.env.Callable.ReturnType;
import bridlensis.env.ComparisonStatement;
import bridlensis.env.Environment;
import bridlensis.env.EnvironmentException;
import bridlensis.env.UserFunction;
import bridlensis.env.Variable;

public class StatementFactory {

	public static final String NULL = "${BRIDLE_NULL}";
	public static final String DEFAULT_INDENT = "    ";

	private Environment environment;
	private Variable functionNullReturn = null;

	StatementFactory(Environment environment) {
		this.environment = environment;
	}

	private static StringBuilder begin(String indent) {
		StringBuilder sb = new StringBuilder(80);
		sb.append(indent);
		return sb;
	}

	public String nullDefine() {
		StringBuilder sb = new StringBuilder(80);
		sb.append("!ifndef BRIDLE_NULL");
		sb.append(InputReader.NEW_LINE);
		sb.append(DEFAULT_INDENT);
		sb.append("!define BRIDLE_NULL \"BridleNSIS_NULL\"");
		sb.append(InputReader.NEW_LINE);
		sb.append("!endif");
		sb.append(InputReader.NEW_LINE);
		sb.append(InputReader.NEW_LINE);
		return sb.toString();
	}

	public String variableDeclare(String indent, Variable var) {
		StringBuilder sb = begin(indent);
		sb.append("Var /GLOBAL ");
		sb.append(var.getName());
		return sb.toString();
	}

	public String variableAssign(String indent, Variable var, String value) {
		StringBuilder sb = begin(indent);
		sb.append("StrCpy ");
		sb.append(var.getNSISExpression());
		sb.append(' ');
		sb.append(value);
		return sb.toString();
	}

	public String functionBegin(String indent, UserFunction function) {
		StringBuilder sb = begin(indent);

		Iterator<Variable> args = function.argumentsIterator();
		while (args.hasNext()) {
			sb.append(variableDeclare("", args.next()));
			sb.append(InputReader.NEW_LINE);
			sb.append(indent);
		}

		sb.append("Function ");
		sb.append(function.getName());

		args = function.argumentsIterator();
		while (args.hasNext()) {
			sb.append(InputReader.NEW_LINE);
			if (indent.length() != 0) {
				sb.append(indent);
				sb.append(indent);
			} else {
				sb.append(DEFAULT_INDENT);
			}
			sb.append("Pop $");
			sb.append(args.next());
		}

		return sb.toString();
	}

	public String functionReturn(String indent, Callable function, String value) {
		StringBuilder sb = begin(indent);
		if (value != null) {
			sb.append("Push ");
			sb.append(value);
			sb.append(InputReader.NEW_LINE);
			sb.append(indent);
		}
		sb.append("Return");
		return sb.toString();
	}

	public String functionEnd(String indent) {
		StringBuilder sb = begin(indent);
		sb.append("FunctionEnd");
		return sb.toString();
	}

	public String call(String indent, Callable callable, List<String> args,
			Variable returnVar) throws InvalidSyntaxException,
			EnvironmentException {
		StringBuilder sb = new StringBuilder();
		if (returnVar == null && callable.getReturnType() != ReturnType.VOID
				&& callable.getReturnType() != ReturnType.OPTIONAL
				&& callable.getReturnType() != ReturnType.ERRORFLAG) {
			if (functionNullReturn == null) {
				functionNullReturn = environment.registerVariable(
						"bridlensis_nullvar", null);
				sb.append(variableDeclare(indent, functionNullReturn));
				sb.append(InputReader.NEW_LINE);
			}
			returnVar = functionNullReturn;
		} else if (returnVar != null
				&& callable.getReturnType() == ReturnType.ERRORFLAG) {
			sb.append(beginValueReturnFunctionStatement(indent, returnVar));
		}
		sb.append(callable.statementFor(indent, args, returnVar));
		if (returnVar != null
				&& callable.getReturnType() == ReturnType.ERRORFLAG) {
			sb.append(endValueReturnFunctionStatement(indent, returnVar));
		}
		return sb.toString();
	}

	public static String deString(String expr) {
		if (expr.length() > 1 && expr.charAt(0) == '"') {
			return expr.substring(1, expr.length() - 1);
		}
		return expr;
	}

	private static StringBuilder beginValueReturnFunctionStatement(
			String indent, Variable returnVar) {
		StringBuilder sb = begin(indent);
		if (returnVar != null) {
			sb.append("StrCpy ");
			sb.append(returnVar.getNSISExpression());
			sb.append(" 1");
			sb.append(InputReader.NEW_LINE);
			sb.append(indent);
			sb.append("ClearErrors");
			sb.append(InputReader.NEW_LINE);
			sb.append(indent);
		}
		return sb;
	}

	private static String endValueReturnFunctionStatement(String indent,
			Variable returnVar) {
		StringBuilder sb = new StringBuilder();
		if (returnVar != null) {
			sb.append(InputReader.NEW_LINE);
			sb.append(indent);
			sb.append("IfErrors +2");
			sb.append(InputReader.NEW_LINE);
			sb.append(indent);
			sb.append(DEFAULT_INDENT);
			sb.append("StrCpy ");
			sb.append(returnVar.getNSISExpression());
			sb.append(" 0");
		}
		return sb.toString();
	}

	public String include(String indent, String filename) {
		StringBuilder sb = begin(indent);
		sb.append("!include \"");
		sb.append(filename);
		sb.append('"');
		return sb.toString();
	}

	public String logicLibComparisonStatement(String indent,
			ComparisonStatement statement) {
		return logicLibComparisonStatement(indent, "", statement);
	}

	public String logicLibComparisonStatement(String indent,
			String startPrefix, ComparisonStatement statement) {
		StringBuilder sb = begin(indent);
		sb.append("${");
		sb.append(startPrefix);
		sb.append(statement.getKey());
		if (statement.isNot()) {
			sb.append("Not");
		}
		sb.append('}');
		for (String left : statement.getLeft()) {
			sb.append(' ');
			sb.append(left);
		}
		if (statement.getCompare() != null) {
			sb.append(' ');
			sb.append(statement.getCompare());
		}
		for (String right : statement.getRight()) {
			sb.append(' ');
			sb.append(right);
		}
		return sb.toString();
	}

	public String logicLibDefine(String indent, String def) {
		StringBuilder sb = begin(indent);
		sb.append("${");
		sb.append(def);
		sb.append("}");
		return sb.toString();
	}

}
