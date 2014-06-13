package bridlensis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bridlensis.InputReader.Word;
import bridlensis.InputReader.WordTail;
import bridlensis.env.Callable;
import bridlensis.env.Callable.ReturnType;
import bridlensis.env.ComparisonStatement;
import bridlensis.env.Environment;
import bridlensis.env.EnvironmentException;
import bridlensis.env.NameGenerator;
import bridlensis.env.SimpleTypeObject;
import bridlensis.env.TypeObject;
import bridlensis.env.TypeObject.Type;
import bridlensis.env.UserFunction;
import bridlensis.env.Variable;

class StatementParser {

	private static final String NULLVAR = "bridlensis_nullvar";

	private Environment environment;
	private NameGenerator nameGenerator;
	private UserFunction enclosingFunction = null;
	private Variable functionNullReturn = null;

	public StatementParser(Environment environment, NameGenerator nameGenerator) {
		this.environment = environment;
		this.nameGenerator = nameGenerator;
	}

	public Environment getEnvironment() {
		return environment;
	}

	public String parseVarDeclare(InputReader reader)
			throws InvalidSyntaxException, EnvironmentException {
		StringBuilder sb = new StringBuilder();
		Word name = reader.nextWord();
		if (name.getType() == Type.SPECIAL) {
			name = reader.nextWord();
		}
		do {
			Variable variable = environment.registerVariable(name.asName(),
					enclosingFunction);
			sb.append(NSISStatements.variableDeclare(reader.getIndent(),
					variable));
			if (reader.hasNextWord()) {
				sb.append(NSISStatements.NEWLINE_MARKER);
				name = reader.nextWord();
			} else {
				break;
			}
		} while (true);
		return sb.toString();
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
		if (reader.hasNextWord()) {
			throw new InvalidSyntaxException(
					"Unexpected word at the end of statement");
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
				functionNullReturn = environment
						.registerVariable(NULLVAR, null);
				sb.append(NSISStatements.variableDeclare(indent,
						functionNullReturn));
				sb.append(NSISStatements.NEWLINE_MARKER);
			}
			returnVar = functionNullReturn;
		} else if (returnVar != null
				&& callable.getReturnType() == ReturnType.ERRORFLAG) {
			sb.append(environment.getCallable("strcpy").statementFor(indent,
					Arrays.asList(SimpleTypeObject.integer(1)), returnVar));
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
					Arrays.asList(SimpleTypeObject.integer(0)), returnVar));
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
		List<ComparisonStatement> statements = parseComparisonStatement(
				reader.nextWord(), reader, sb);
		if (statements.size() != 1) {
			throw new InvalidSyntaxException("Illegal loop syntax");
		}
		if (statements.get(0).isNot()) {
			throw new InvalidSyntaxException(String.format(
					"Illegal modifier 'Not' in %s statement", define));
		}
		sb.append(NSISStatements.logicLibComparisonStatement(
				reader.getIndent(), define, statements.get(0)));
		return sb.toString();
	}

	public String parseIf(Word keyword, InputReader reader)
			throws InvalidSyntaxException, EnvironmentException {
		StringBuilder sb = new StringBuilder();
		StringBuilder buffer = new StringBuilder();
		List<ComparisonStatement> statements = parseComparisonStatement(
				keyword, reader, buffer);
		for (int i = 0; i < statements.size(); i++) {
			if (i > 0) {
				sb.append(NSISStatements.NEWLINE_MARKER);
			}
			sb.append(NSISStatements.logicLibComparisonStatement(
					reader.getIndent(), statements.get(i)));
		}
		while (reader.hasNextWord()) {
			statements = parseComparisonStatement(reader.nextWord(), reader,
					buffer);
			for (ComparisonStatement statement : statements) {
				sb.append(NSISStatements.NEWLINE_MARKER);
				sb.append(NSISStatements.logicLibComparisonStatement(
						reader.getIndent(), statement));
			}
		}
		if (buffer.length() > 0) {
			sb.insert(0, buffer.toString());
		}
		return sb.toString();
	}

	private List<ComparisonStatement> parseComparisonStatement(Word keyword,
			InputReader reader, StringBuilder buffer)
			throws InvalidSyntaxException, EnvironmentException {
		List<ComparisonStatement> statements = new ArrayList<>();
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
				Word nextWord = reader.nextWord();
				String asName = nextWord.asName();
				if (asName.equals("and") || asName.equals("or")) {
					statements.addAll(parseComparisonStatement(nextWord,
							reader, buffer));
				} else {
					statement
							.addLeft(parseExpression(nextWord, buffer, reader));
				}
			} else {
				statement.setCompare(compare);
				break;
			}
		}

		if (reader.hasNextWord()) {
			statement.addRight(parseExpression(reader.nextWord(), buffer,
					reader));
		}

		statements.add(0, statement);
		return statements;
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
			leftValue = SimpleTypeObject.stripString(left);
		}

		String rightValue = SimpleTypeObject.stripString(parseExpression(
				reader.nextWord(), buffer, reader));

		return SimpleTypeObject.string(leftValue + rightValue);
	}

	private Variable parseInExpressionCall(TypeObject callableName,
			StringBuilder buffer, InputReader reader)
			throws InvalidSyntaxException, EnvironmentException {
		Variable fReturn = registerAndDeclareVariable(nameGenerator.generate(),
				reader.getIndent(), buffer);
		buffer.append(parseCall(new Word(callableName.getValue()), fReturn,
				reader));
		buffer.append(NSISStatements.NEWLINE_MARKER);
		return fReturn;
	}

	private Variable registerAndDeclareVariable(String name, String indent,
			StringBuilder buffer) throws EnvironmentException {
		String varName = (name == null) ? nameGenerator.generate() : name;
		Variable variable = environment.registerVariable(varName,
				enclosingFunction);
		buffer.append(NSISStatements.variableDeclare(indent, variable));
		buffer.append(NSISStatements.NEWLINE_MARKER);
		return variable;
	}

}
