package bridlensis;

import java.util.Iterator;
import java.util.List;

import bridlensis.env.Callable;
import bridlensis.env.ComparisonStatement;
import bridlensis.env.SimpleTypeObject;
import bridlensis.env.TypeObject;
import bridlensis.env.UserFunction;
import bridlensis.env.Variable;

public class NSISStatements {

	public static final TypeObject NULL = SimpleTypeObject
			.special("${BRIDLE_NULL}");
	public static final String DEFAULT_INDENT = "    ";
	public static final String NEWLINE_MARKER = "\r\n";

	private static StringBuilder begin(String indent) {
		StringBuilder sb = new StringBuilder(80);
		sb.append(indent);
		return sb;
	}

	public static String nullDefine() {
		StringBuilder sb = new StringBuilder(80);
		sb.append("!ifndef BRIDLE_NULL");
		sb.append(NEWLINE_MARKER);
		sb.append(DEFAULT_INDENT);
		sb.append("!define BRIDLE_NULL \"BridleNSIS_NULL\"");
		sb.append(NEWLINE_MARKER);
		sb.append("!endif");
		sb.append(NEWLINE_MARKER);
		sb.append(NEWLINE_MARKER);
		return sb.toString();
	}

	public static String variableDeclare(String indent, Variable var) {
		StringBuilder sb = begin(indent);
		sb.append("Var /GLOBAL ");
		sb.append(var.getName());
		return sb.toString();
	}

	public static String variableAssign(String indent, Variable var,
			TypeObject value) {
		StringBuilder sb = begin(indent);
		sb.append("StrCpy ");
		sb.append(var.getValue());
		sb.append(' ');
		sb.append(value.getValue());
		return sb.toString();
	}

	public static String functionBegin(String indent, UserFunction function) {
		StringBuilder sb = begin(indent);
		sb.append("Function ");
		sb.append(function.getName());
		Iterator<Variable> args = function.argumentsIterator();
		while (args.hasNext()) {
			sb.append(NEWLINE_MARKER);
			if (indent.length() != 0) {
				sb.append(indent);
				sb.append(indent);
			} else {
				sb.append(DEFAULT_INDENT);
			}
			sb.append("Pop ");
			sb.append(args.next().getValue());
		}
		return sb.toString();
	}

	public static String functionReturn(String indent, Callable function,
			TypeObject value) {
		StringBuilder sb = begin(indent);
		if (value != null) {
			sb.append("Push ");
			sb.append(value.getValue());
			sb.append(NEWLINE_MARKER);
			sb.append(indent);
		}
		sb.append("Return");
		return sb.toString();
	}

	public static String functionEnd(String indent) {
		StringBuilder sb = begin(indent);
		sb.append("FunctionEnd");
		return sb.toString();
	}

	public static String include(String indent, String filename) {
		StringBuilder sb = begin(indent);
		sb.append("!include \"");
		sb.append(filename);
		sb.append('"');
		return sb.toString();
	}

	public static String logicLibComparisonStatement(String indent,
			ComparisonStatement statement) {
		return logicLibComparisonStatement(indent, "", statement);
	}

	public static String logicLibComparisonStatement(String indent,
			String startPrefix, ComparisonStatement statement) {
		StringBuilder sb = begin(indent);
		sb.append("${");
		sb.append(startPrefix);
		sb.append(statement.getKey());
		if (statement.isNot()) {
			sb.append("Not");
		}
		sb.append('}');
		for (TypeObject left : statement.getLeft()) {
			sb.append(' ');
			sb.append(left.getValue());
		}
		if (statement.getCompare() != null) {
			sb.append(' ');
			sb.append(statement.getCompare());
		}
		for (TypeObject right : statement.getRight()) {
			sb.append(' ');
			sb.append(right.getValue());
		}
		return sb.toString();
	}

	public static String logicLibDefine(String indent, String def) {
		StringBuilder sb = begin(indent);
		sb.append("${");
		sb.append(def);
		sb.append("}");
		return sb.toString();
	}

	public static String clearErrors(String indent) {
		StringBuilder sb = begin(indent);
		sb.append("ClearErrors");
		return sb.toString();
	}

	public static String callOnError(String indent, Callable callable,
			List<TypeObject> args, Variable returnVar)
			throws InvalidSyntaxException {
		StringBuilder sb = begin(indent);
		sb.append("IfErrors +2");
		sb.append(NEWLINE_MARKER);
		sb.append(indent);
		sb.append(callable.statementFor(DEFAULT_INDENT, args, returnVar));
		return sb.toString();
	}
}
