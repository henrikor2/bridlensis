package bridlensis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bridlensis.InputReader.Word;
import bridlensis.InputReader.WordTail;
import bridlensis.env.Callable;
import bridlensis.env.ComparisonStatement;
import bridlensis.env.Environment;
import bridlensis.env.EnvironmentException;
import bridlensis.env.SimpleTypeObject;
import bridlensis.env.TypeObject;
import bridlensis.env.UserFunction;
import bridlensis.env.Variable;
import bridlensis.env.Callable.ReturnType;
import bridlensis.env.TypeObject.Type;

class StatementParser {

	private Environment environment;
	private UserFunction enclosingFunction = null;
	private Variable functionNullReturn = null;

	public StatementParser(Environment environment) {
		this.environment = environment;
	}

	public Environment getEnvironment() {
		return environment;
	}

	public String parseVarDeclare(InputReader reader)
			throws InvalidSyntaxException, EnvironmentException {
		Word name = reader.nextWord();
		Variable variable = environment.registerVariable(name.asName(),
				enclosingFunction);
		return NSISStatements.variableDeclare(reader.getIndent(), variable);
	}

	public String parseVarAssign(Word varName, InputReader reader)
			throws InvalidSyntaxException, EnvironmentException {
		StringBuilder sb = new StringBuilder();
		Variable variable;

		if (environment.containsVariable(varName.asName(), enclosingFunction)) {
			variable = environment.getVariable(varName.asName(),
					enclosingFunction);
		} else {
			variable = registerAndDeclareVariable(varName.asName(),
					reader.getIndent(), sb);
		}

		Word word = reader.nextWord();
		TypeObject value;
		WordTail tail = reader.getWordTail();
		if (tail.isConcatenation()) {
			value = parseExpression(word, sb, reader);
		} else if (tail.isFunctionArgsOpen()) {
			// Direct function return assign to avoid declaring yet another
			// dummy variable for function return
			sb.append(parseCall(word, variable, reader));
			if (reader.getWordTail().isConcatenation()) {
				sb.append(NSISStatements.NEWLINE_MARKER);
				value = parseExpression(variable, sb, reader);
			} else {
				return sb.toString();
			}
		} else if (word.getType() == Type.NAME) {
			value = environment.getVariable(word.asName(), enclosingFunction);
		} else {
			value = word;
		}
		sb.append(NSISStatements.variableAssign(reader.getIndent(), variable,
				value));
		return sb.toString();
	}

	public String parseFunctionBegin(InputReader reader)
			throws InvalidSyntaxException, EnvironmentException {
		if (enclosingFunction != null) {
			throw new InvalidSyntaxException(
					"Cannot declare function within a function");
		}
		enclosingFunction = environment.registerUserFunction(reader.nextWord()
				.asName());
		StringBuilder sb = new StringBuilder();
		while (reader.hasNextWord()) {
			String argName = reader.nextWord().asName();
			Variable argVariable = registerAndDeclareVariable(argName,
					reader.getIndent(), sb);
			enclosingFunction.addArgument(argVariable);
		}
		sb.append(NSISStatements.functionBegin(reader.getIndent(),
				enclosingFunction));
		return sb.toString();
	}

	public String parseFunctionReturn(InputReader reader)
			throws InvalidSyntaxException, EnvironmentException {
		if (!(enclosingFunction != null)) {
			throw new InvalidSyntaxException(
					"Return is not allowed outside function");
		}
		StringBuilder sb = new StringBuilder();
		TypeObject value = null;
		if (reader.hasNextWord()) {
			enclosingFunction.setHasReturn(true);
			Word word = reader.nextWord();
			WordTail tail = reader.getWordTail();
			if (tail.isFunctionArgsOpen() || tail.isConcatenation()) {
				value = parseExpression(word, sb, reader);
			} else if (word.getType() == Type.NAME) {
				value = environment.getVariable(word.asName(),
						enclosingFunction);
			} else {
				value = word;
			}
		}
		sb.append(NSISStatements.functionReturn(reader.getIndent(),
				enclosingFunction, value));
		return sb.toString();
	}

	public String parseFunctionEnd(InputReader reader)
			throws InvalidSyntaxException {
		if (!(enclosingFunction != null)) {
			throw new InvalidSyntaxException(
					"FunctionEnd is not allowed outside function");
		}
		enclosingFunction = null;
		return NSISStatements.functionEnd(reader.getIndent());
	}

	public String parseCall(Word name, Variable returnVar, InputReader reader)
			throws InvalidSyntaxException, EnvironmentException {
		StringBuilder sb = new StringBuilder();
		Callable callable = environment.getCallable(name.asName());
		List<TypeObject> args = parseAndValidateFunctionArguments(callable,
				returnVar, reader, sb);
		sb.append(call(reader.getIndent(), callable, args, returnVar));
		return sb.toString();
	}

	private List<TypeObject> parseAndValidateFunctionArguments(
			Callable function, Variable returnVar, InputReader reader,
			StringBuilder buffer) throws InvalidSyntaxException,
			EnvironmentException {
		if (function.getReturnType() == ReturnType.VOID && returnVar != null) {
			throw new InvalidSyntaxException("Function doesn't return a value");
		}
		ArrayList<TypeObject> args = new ArrayList<>();
		WordTail tail = reader.getWordTail();
		while (reader.hasNextWord() && !tail.containsFunctionArgsClose()) {
			TypeObject arg;
			Word word = reader.nextWord();
			tail = reader.getWordTail();
			if (!tail.isFunctionArgsClose()
					&& (tail.isFunctionArgsOpen() || tail.isConcatenation())) {
				arg = parseExpression(word, buffer, reader);
			} else if (word.getType() == Type.NAME) {
				Variable variable = environment.getVariable(word.asName(),
						enclosingFunction);
				arg = variable;
			} else {
				arg = word;
			}
			args.add(arg);
		}
		if (function.getArgsCount() > -1
				&& args.size() > function.getArgsCount()) {
			throw new InvalidSyntaxException(
					String.format(
							"Too many function arguments (expected at most %d, provided %d)",
							function.getArgsCount(), args.size()));
		} else {
			if (args.size() < function.getMandatoryArgsCount()) {
				throw new InvalidSyntaxException(
						String.format(
								"Too few function arguments provided (expected at minimum %d, provided %d)",
								function.getMandatoryArgsCount(), args.size()));
			}
		}
		// Push empty arguments for function if they're not defined
		for (int i = args.size(); i < function.getArgsCount(); i++) {
			args.add(NSISStatements.NULL);
		}
		return args;
	}

	protected String call(String indent, Callable callable,
			List<TypeObject> args, Variable returnVar)
			throws InvalidSyntaxException, EnvironmentException {
		StringBuilder sb = new StringBuilder();
		if (returnVar == null
				&& callable.getReturnType() == ReturnType.REQUIRED) {
			if (functionNullReturn == null) {
				functionNullReturn = environment.registerVariable(
						"bridlensis_nullvar", null);
				sb.append(NSISStatements.variableDeclare(indent,
						functionNullReturn));
				sb.append(NSISStatements.NEWLINE_MARKER);
			}
			returnVar = functionNullReturn;
		} else if (returnVar != null
				&& callable.getReturnType() == ReturnType.ERRORFLAG) {
			sb.append(environment.getCallable("strcpy").statementFor(indent,
					Arrays.asList(new SimpleTypeObject(1)), returnVar));
			sb.append(NSISStatements.NEWLINE_MARKER);
			sb.append(NSISStatements.clearErrors(indent));
			sb.append(NSISStatements.NEWLINE_MARKER);
		}
		sb.append(callable.statementFor(indent, args, returnVar));
		if (returnVar != null
				&& callable.getReturnType() == ReturnType.ERRORFLAG) {
			sb.append(NSISStatements.NEWLINE_MARKER);
			sb.append(NSISStatements.callOnError(indent,
					environment.getCallable("strcpy"),
					Arrays.asList(new SimpleTypeObject(0)), returnVar));
		}
		return sb.toString();
	}

	public String parseDoLoop(String keyword, InputReader reader)
			throws InvalidSyntaxException, EnvironmentException {
		String define = Character.toUpperCase(keyword.charAt(0))
				+ keyword.substring(1);
		if (!reader.hasNextWord()) {
			return NSISStatements.logicLibDefine(reader.getIndent(), define);
		}
		StringBuilder sb = new StringBuilder();
		ComparisonStatement statement = parseComparisonStatement(
				reader.nextWord(), reader, sb);
		if (statement.isNot()) {
			throw new InvalidSyntaxException(String.format(
					"Illegal modifier 'Not' in %s statement", define));
		}
		sb.append(NSISStatements.logicLibComparisonStatement(
				reader.getIndent(), define, statement));
		return sb.toString();
	}

	public String parseIf(Word keyword, InputReader reader)
			throws InvalidSyntaxException, EnvironmentException {
		StringBuilder sb = new StringBuilder();
		StringBuilder buffer = new StringBuilder();
		ComparisonStatement ifStatement = parseComparisonStatement(keyword,
				reader, buffer);
		sb.append(NSISStatements.logicLibComparisonStatement(
				reader.getIndent(), ifStatement));
		while (reader.hasNextWord()) {
			sb.append(NSISStatements.NEWLINE_MARKER);
			sb.append(NSISStatements.logicLibComparisonStatement(
					reader.getIndent(),
					parseComparisonStatement(reader.nextWord(), reader, buffer)));
		}
		if (buffer.length() > 0) {
			sb.insert(0, buffer.toString());
		}
		return sb.toString();
	}

	private ComparisonStatement parseComparisonStatement(Word keyword,
			InputReader reader, StringBuilder buffer)
			throws InvalidSyntaxException, EnvironmentException {
		String key = keyword.getValue();
		if (key.equalsIgnoreCase("and") || key.equalsIgnoreCase("or")) {
			key += "If";
		}
		ComparisonStatement statement = new ComparisonStatement(key);

		Word left = reader.nextWord();
		if (left.asName().equals("not")) {
			statement.setNot(true);
			left = reader.nextWord();
		}
		statement.addLeft(parseExpression(left, buffer, reader));

		while (reader.hasNextWord()) {
			String compare = reader.getWordTail().getComparison();
			if (compare.isEmpty()) {
				statement.addLeft(parseExpression(reader.nextWord(), buffer,
						reader));
			} else {
				statement.setCompare(compare);
				break;
			}
		}

		if (reader.hasNextWord()) {
			statement.addRight(parseExpression(reader.nextWord(), buffer,
					reader));
		}

		return statement;
	}

	protected TypeObject parseExpression(TypeObject expr, StringBuilder buffer,
			InputReader reader) throws InvalidSyntaxException,
			EnvironmentException {
		TypeObject object;
		WordTail tail = reader.getWordTail();
		if (tail.isFunctionArgsOpen()) {
			Variable fReturn = parseInExpressionCall(expr, buffer, reader);
			if (tail.isComparison()) {
				object = fReturn;
			} else {
				object = parseExpression(fReturn, buffer, reader);
			}
		} else if (tail.isConcatenation()) {
			TypeObject concat = concatenateWithNext(expr, buffer, reader);
			object = parseExpression(concat, buffer, reader);
		} else if (expr.getType() == Type.NAME) {
			object = environment
					.getVariable(expr.getValue(), enclosingFunction);
		} else {
			object = expr;
		}
		return object;
	}

	private TypeObject concatenateWithNext(TypeObject left,
			StringBuilder buffer, InputReader reader)
			throws EnvironmentException, InvalidSyntaxException {
		String leftValue;
		if (left.getType() == Type.NAME) {
			leftValue = environment.getVariable(left.getValue().toLowerCase(),
					enclosingFunction).getValue();
		} else {
			leftValue = NSISStatements.deString(left);
		}

		String rightValue = NSISStatements.deString(parseExpression(
				reader.nextWord(), buffer, reader));

		return new SimpleTypeObject(Type.STRING, String.format("%s%s",
				leftValue, rightValue));
	}

	private Variable parseInExpressionCall(TypeObject callableName,
			StringBuilder buffer, InputReader reader)
			throws InvalidSyntaxException, EnvironmentException {
		Variable fReturn = registerAndDeclareVariable(environment
				.getNameGenerator().generate(), reader.getIndent(), buffer);
		buffer.append(parseCall(new Word(callableName.getValue()), fReturn,
				reader));
		buffer.append(NSISStatements.NEWLINE_MARKER);
		return fReturn;
	}

	private Variable registerAndDeclareVariable(String name, String indent,
			StringBuilder buffer) throws EnvironmentException {
		String varName = (name == null) ? environment.getNameGenerator()
				.generate() : name;
		Variable variable = environment.registerVariable(varName,
				enclosingFunction);
		buffer.append(NSISStatements.variableDeclare(indent, variable));
		buffer.append(NSISStatements.NEWLINE_MARKER);
		return variable;
	}

}
