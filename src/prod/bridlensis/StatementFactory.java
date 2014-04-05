package bridlensis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import bridlensis.env.Callable;
import bridlensis.env.Environment;
import bridlensis.env.EnvironmentException;
import bridlensis.env.FunctionDelete;
import bridlensis.env.FunctionFileCopy;
import bridlensis.env.UserFunction;
import bridlensis.env.ComparisonStatement;
import bridlensis.env.Instruction;
import bridlensis.env.FunctionMsgBox;
import bridlensis.env.Variable;
import bridlensis.env.FunctionMsgBox.ButtonGroup;
import bridlensis.env.FunctionMsgBox.ReturnOption;

public class StatementFactory {

	public static final String NULL = "${BRIDLE_NULL}";
	private static final String DEFAULT_INDENT = "    ";

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
		if (callable instanceof UserFunction) {
			return callUserFunction(indent, (UserFunction) callable, args,
					returnVar);
		} else if (callable instanceof FunctionMsgBox) {
			return callMsgBox(indent, (FunctionMsgBox) callable, args,
					returnVar);
		} else if (callable instanceof FunctionFileCopy) {
			return callFileCopy(indent, args, returnVar);
		} else if (callable instanceof FunctionDelete) {
			return callDelete(indent, args, returnVar);
		} else {
			return callInstruction(indent, (Instruction) callable, args,
					returnVar);
		}
	}

	private Variable getFunctionNullReturn(String indent, StringBuilder sb)
			throws EnvironmentException {
		if (functionNullReturn == null) {
			sb.append(InputReader.NEW_LINE);
			sb.append(indent);
			functionNullReturn = environment.registerVariable(
					"bridlensis_nullvar", null);
			sb.append(variableDeclare(indent, functionNullReturn));
		}
		return functionNullReturn;
	}

	private String callUserFunction(String indent, UserFunction function,
			List<String> args, Variable returnVar) throws EnvironmentException {
		StringBuilder sb = begin(indent);
		for (int i = args.size() - 1; i >= 0; i--) {
			sb.append("Push ");
			sb.append(args.get(i));
			sb.append(InputReader.NEW_LINE);
			sb.append(indent);
		}
		sb.append("Call ");
		sb.append(function.getName());
		if (function.hasReturn()) {
			if (returnVar == null) {
				returnVar = getFunctionNullReturn(indent, sb);
			}
			sb.append(InputReader.NEW_LINE);
			sb.append(indent);
			sb.append("Pop ");
			sb.append(returnVar.getNSISExpression());
		}
		return sb.toString();
	}

	private String callInstruction(String indent, Instruction instruction,
			List<String> argValues, Variable returnVar)
			throws EnvironmentException {
		StringBuilder sb = begin(indent);
		ArrayList<String> cArgs = new ArrayList<>(argValues);
		if (instruction.hasReturn()) {
			if (returnVar == null) {
				returnVar = getFunctionNullReturn(indent, sb);
			}
			cArgs.add(instruction.getReturnArgIndex(),
					returnVar.getNSISExpression());
		}
		sb.append(instruction.getDisplayName());
		sb.append(' ');
		for (String cArg : cArgs) {
			if (!cArg.equals(NULL)) {
				sb.append(cArg);
				sb.append(' ');
			}
		}
		return sb.toString();
	}

	private static String deString(String expr) {
		if (expr.length() > 1 && expr.charAt(0) == '"') {
			return expr.substring(1, expr.length() - 1);
		}
		return expr;
	}

	private String callMsgBox(String indent, FunctionMsgBox msgBox,
			List<String> argValues, Variable returnVar)
			throws InvalidSyntaxException {
		ButtonGroup buttonGroup;
		try {
			buttonGroup = ButtonGroup.valueOf(deString(argValues
					.get(FunctionMsgBox.BUTTONGROUP_INDEX)));
		} catch (IllegalArgumentException e) {
			throw new InvalidSyntaxException(String.format(
					"Invalid button group argument '%s'",
					argValues.get(FunctionMsgBox.BUTTONGROUP_INDEX)));
		}
		StringBuilder sb = begin(indent);
		sb.append("MessageBox ");
		sb.append(FunctionMsgBox.optionsList(buttonGroup,
				deString(argValues.get(FunctionMsgBox.OPTIONS_INDEX))));
		sb.append(' ');
		sb.append(argValues.get(FunctionMsgBox.MESSAGE_INDEX));

		if (!argValues.get(FunctionMsgBox.SDRETURN_INDEX).equals(NULL)) {
			String button = deString(argValues
					.get(FunctionMsgBox.SDRETURN_INDEX));
			if (!FunctionMsgBox.containsButton(button)) {
				throw new InvalidSyntaxException(
						"Unsupported MsgBox SD return " + button);
			}
			sb.append(" /SD ID");
			sb.append(button);
		}

		if (returnVar != null) {
			String exit_jump = msgBox.createExitGoTo();
			indent += DEFAULT_INDENT;
			StringBuilder sbRet = new StringBuilder();
			for (ReturnOption ro : msgBox.returnOptions(buttonGroup)) {
				sb.append(" ID");
				sb.append(ro.getReturnValue());
				sb.append(' ');
				sb.append(ro.getGoTo());

				sbRet.append(InputReader.NEW_LINE);
				sbRet.append(indent);
				sbRet.append(ro.getGoTo());
				sbRet.append(':');
				sbRet.append(InputReader.NEW_LINE);
				sbRet.append(indent + DEFAULT_INDENT);
				sbRet.append("StrCpy ");
				sbRet.append(returnVar.getNSISExpression());
				sbRet.append(" \"");
				sbRet.append(ro.getReturnValue());
				sbRet.append('"');
				sbRet.append(InputReader.NEW_LINE);
				sbRet.append(indent + DEFAULT_INDENT);
				sbRet.append("GoTo ");
				sbRet.append(exit_jump);
			}
			sb.append(sbRet);
			sb.append(InputReader.NEW_LINE);
			sb.append(indent);
			sb.append(exit_jump);
			sb.append(':');
		}

		return sb.toString();
	}

	private String callFileCopy(String indent, List<String> argValues,
			Variable returnVar) {
		StringBuilder sb = beginBuiltinFunctionStatement(indent, returnVar);
		sb.append("CopyFiles /SILENT ");
		sb.append(argValues.get(FunctionFileCopy.SOURCE_INDEX));
		sb.append(" ");
		sb.append(argValues.get(FunctionFileCopy.TARGET_INDEX));
		sb.append(endBuiltinFunctionStatement(indent, returnVar));
		return sb.toString();
	}

	private String callDelete(String indent, List<String> argValues,
			Variable returnVar) {
		StringBuilder sb = beginBuiltinFunctionStatement(indent, returnVar);
		sb.append("Delete ");
		if (!argValues.get(FunctionDelete.REBOOTOK_INDEX).equals(NULL)) {
			sb.append("/REBOOTOK ");
		}
		sb.append(argValues.get(FunctionDelete.FILE_INDEX));
		sb.append(endBuiltinFunctionStatement(indent, returnVar));
		return sb.toString();
	}

	private static StringBuilder beginBuiltinFunctionStatement(String indent,
			Variable returnVar) {
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

	private static String endBuiltinFunctionStatement(String indent,
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
