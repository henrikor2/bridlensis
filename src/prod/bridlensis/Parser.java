package bridlensis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

import bridlensis.env.Callable;
import bridlensis.env.Callable.ReturnType;
import bridlensis.env.Environment;
import bridlensis.env.EnvironmentException;
import bridlensis.env.UserFunction;
import bridlensis.env.ComparisonStatement;
import bridlensis.env.Variable;

public class Parser {

	private File baseDir;
	private File outDir;
	private String encoding;
	private Collection<String> excludeFiles;
	private Environment environment;
	private StatementFactory statementFactory;
	private UserFunction enclosingFunction = null;
	private int fileCount = 0;
	private int inputLines = 0;

	public Parser(Environment environment, File baseDir, File outDir,
			String encoding, Collection<String> excludeFiles) {
		this.baseDir = baseDir;
		this.outDir = outDir;
		this.encoding = encoding;
		this.environment = environment;
		this.statementFactory = new StatementFactory(environment);
		this.excludeFiles = new ArrayList<String>();
		if (excludeFiles != null) {
			this.excludeFiles.addAll(excludeFiles);
		}
	}

	public int getInputLines() {
		return inputLines;
	}

	public int getFileCount() {
		return fileCount;
	}

	public void parse(String inputFileName, String outputFileName)
			throws IOException, ParserException {
		File inputFile = new File(baseDir, inputFileName);
		System.out.println("Begin parse file: " + inputFile.getAbsolutePath());
		BufferedWriter writer = null;
		try {
			writer = getOutputWriter(outputFileName);
			writer.write(statementFactory.nullDefine());
			parseFile(inputFile, writer);
		} finally {
			if (writer != null) {
				try {
					writer.flush();
					writer.close();
				} catch (IOException e) {
					// Ignore
				}
			}
		}
	}

	private BufferedWriter getOutputWriter(String outputFileName)
			throws UnsupportedEncodingException, FileNotFoundException,
			IOException {
		File outputFile = new File(outDir, outputFileName);
		System.out.println("Output file: " + outputFile.getAbsolutePath());
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outputFile), encoding));
		if (encoding.equalsIgnoreCase("UTF-16LE")) {
			writer.write("\uFEFF");
		} else if (encoding.equalsIgnoreCase("UTF-16BE")) {
			writer.write("\uFFFE");
		}
		return writer;
	}

	private void parseFile(File inputFile, BufferedWriter writer)
			throws IOException, ParserException {
		Scanner source = new Scanner(inputFile, encoding);
		InputReader reader = new InputReader(source);
		fileCount++;
		try {
			while (reader.goToNextStatement()) {
				writer.write(parseStatement(reader));
				writer.write(InputReader.NEW_LINE);
			}
			System.out.println(String.format(
					"End parsing %d lines in file %s.", reader.getLinesRead(),
					inputFile.getAbsolutePath()));
			inputLines += reader.getLinesRead();
		} catch (InvalidSyntaxException | EnvironmentException e) {
			throw new ParserException(inputFile.getAbsolutePath(),
					reader.getLinesRead(), e);
		} finally {
			source.close();
		}
	}

	protected String parseStatement(InputReader reader)
			throws InvalidSyntaxException, EnvironmentException,
			ParserException {
		if (!reader.hasNextWord()) {
			return reader.getCurrentStatement();
		}
		String word = reader.nextWord().toLowerCase();
		String tail = reader.getWordTail();
		if (tail.startsWith("=")) {
			return parseVarAssign(word, reader);
		} else if (tail.startsWith("(")) {
			return parseCall(word, null, reader);
		} else if (word.equals("var")) {
			return parseVarDeclare(reader);
		} else if (word.equals("function")) {
			return parseFunctionBegin(reader);
		} else if (word.equals("return")) {
			return parseFunctionReturn(reader);
		} else if (word.equals("functionend")) {
			return parseFunctionEnd(reader);
		} else if (word.equals("if") || word.equals("elseif")) {
			return parseIf(word, reader);
		} else if (word.equals("else")) {
			return statementFactory.logicLibDefine(reader.getIndent(), "Else");
		} else if (word.equals("endif")) {
			return statementFactory.logicLibDefine(reader.getIndent(), "EndIf");
		} else if (word.equals("do")) {
			return parseDoLoop("Do", reader);
		} else if (word.equals("continue")) {
			return statementFactory.logicLibDefine(reader.getIndent(),
					"Continue");
		} else if (word.equals("break")) {
			return statementFactory.logicLibDefine(reader.getIndent(), "Break");
		} else if (word.equals("loop")) {
			return parseDoLoop("Loop", reader);
		} else if (word.equals("") && tail.equals("!") && reader.hasNextWord()
				&& (word = reader.nextWord().toLowerCase()).equals("include")) {

			return parseInclude(reader);
		}
		return reader.getCurrentStatement();
	}

	private String parseDoLoop(String keyword, InputReader reader)
			throws InvalidSyntaxException, EnvironmentException {
		if (!reader.hasNextWord()) {
			return statementFactory.logicLibDefine(reader.getIndent(), keyword);
		}
		StringBuilder sb = new StringBuilder();
		ComparisonStatement statement = getComparisonStatement(reader
				.nextWord().toLowerCase(), reader, sb);
		if (statement.isNot()) {
			throw new InvalidSyntaxException(String.format(
					"Illegal modifier 'Not' in %s statement", keyword));
		}
		sb.append(statementFactory.logicLibComparisonStatement(
				reader.getIndent(), keyword, statement));
		return sb.toString();
	}

	private String parseIf(String keyword, InputReader reader)
			throws InvalidSyntaxException, EnvironmentException {
		StringBuilder sb = new StringBuilder();
		StringBuilder buffer = new StringBuilder();

		if (!reader.hasNextWord()) {
			throw new InvalidSyntaxException(
					"Unexpexted end of statement after 'if'");
		}

		ComparisonStatement ifStatement = getComparisonStatement(keyword,
				reader, buffer);
		sb.append(statementFactory.logicLibComparisonStatement(
				reader.getIndent(), ifStatement));

		while (reader.hasNextWord()) {
			String word = reader.nextWord();
			if (word.equalsIgnoreCase("and") || word.equalsIgnoreCase("or")) {
				sb.append(InputReader.NEW_LINE);
				sb.append(statementFactory.logicLibComparisonStatement(
						reader.getIndent(),
						getComparisonStatement(word.toLowerCase(), reader,
								buffer)));
			} else {
				throw new InvalidSyntaxException(String.format(
						"Unexpected operation '%s' in IF statement", word));
			}
		}

		if (buffer.length() > 0) {
			sb.insert(0, buffer.toString());
		}
		return sb.toString();
	}

	private ComparisonStatement getComparisonStatement(String keyword,
			InputReader reader, StringBuilder buffer)
			throws InvalidSyntaxException, EnvironmentException {
		String key;
		if (keyword.equals("if")) {
			key = "If";
		} else if (keyword.equals("and")) {
			key = "AndIf";
		} else if (keyword.equals("or")) {
			key = "OrIf";
		} else if (keyword.equals("elseif")) {
			key = "ElseIf";
		} else if (keyword.equals("while")) {
			key = "While";
		} else if (keyword.equals("until")) {
			key = "Until";
		} else {
			throw new InvalidSyntaxException(String.format(
					"Internal error: Unsupported operation '%s'", keyword));
		}

		ComparisonStatement statement = new ComparisonStatement(key);

		String left = reader.nextWord();
		if (left.equalsIgnoreCase("not")) {
			statement.setNot(true);
			if (!reader.hasNextWord()) {
				throw new InvalidSyntaxException("Unexpected end of 'if'");
			}
			left = parseExpression(reader.nextWord(), buffer, reader);
		} else {
			left = parseExpression(left, buffer, reader);
		}
		statement.addLeft(left);

		while (reader.hasNextWord()) {
			String compare = reader.getWordTail().replaceAll("[^=!\\<\\>]", "");
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

	private String parseInclude(InputReader reader)
			throws InvalidSyntaxException, ParserException {
		String inputFileName = reader.nextWord();
		if (isString(inputFileName)) {
			inputFileName = inputFileName.substring(1,
					inputFileName.length() - 1);
		}
		String statement;
		File inputFile = new File(baseDir, inputFileName);
		if (excludeFiles.contains(inputFileName)
				|| excludeFiles.contains(inputFile.getAbsolutePath())) {
			System.out.println(String.format(
					"Include file '%s' omitted being marked as excluded.",
					inputFileName));
			String outputFileName = MakeBridleNSIS
					.getBridleNSISFileName(inputFileName);
			File outputFile = new File(outDir, outputFileName);
			// Copy include file to outdir
			System.out.println(String.format(
					"Copy file '%s' to directory '%s'",
					inputFile.getAbsolutePath(), outDir.getAbsolutePath()));
			copyFile(inputFile, outputFile, reader.getLinesRead());
			statement = statementFactory.include(reader.getIndent(),
					outputFileName);
		} else if (!inputFile.exists()) {
			System.out
					.println(String
							.format("Include file '%s' not found, assuming it's found by NSIS.",
									inputFileName));
			statement = reader.getCurrentStatement();
		} else {
			System.out
					.println("Follow include: " + inputFile.getAbsolutePath());
			String outputFileName = MakeBridleNSIS
					.getBridleNSISFileName(inputFileName);
			try {
				BufferedWriter writer = null;
				try {
					writer = getOutputWriter(outputFileName);
					parseFile(inputFile, writer);
				} finally {
					if (writer != null) {
						try {
							writer.flush();
							writer.close();
						} catch (IOException e) {
							// Ignore
						}
					}
				}
			} catch (ParserException | IOException e) {
				throw new InvalidSyntaxException(e.getMessage(), e);
			}
			statement = statementFactory.include(reader.getIndent(),
					outputFileName);
		}
		return statement;
	}

	private void copyFile(File sourceFile, File destFile, int lineNumber)
			throws ParserException {
		FileChannel source = null;
		FileChannel destination = null;
		try {
			if (!destFile.exists()) {
				destFile.createNewFile();
			}
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} catch (IOException e) {
			throw new ParserException(sourceFile.getAbsolutePath(), lineNumber,
					e);
		} finally {
			if (source != null) {
				try {
					source.close();
				} catch (IOException e) {
					// Ignore
				}
			}
			if (destination != null) {
				try {
					destination.close();
				} catch (IOException e) {
					// Ignore
				}
			}
		}
	}

	private String parseVarDeclare(InputReader reader)
			throws InvalidSyntaxException, EnvironmentException {
		if (!reader.hasNextWord()) {
			throw new InvalidSyntaxException("Variable name not defined");
		}
		String name = reader.nextWord();
		Variable variable = environment.registerVariable(name,
				enclosingFunction);
		return statementFactory.variableDeclare(reader.getIndent(), variable);
	}

	private String parseVarAssign(String varName, InputReader reader)
			throws InvalidSyntaxException, EnvironmentException {
		StringBuilder sb = new StringBuilder();
		Variable variable;

		if (environment.containsVariable(varName, enclosingFunction)) {
			variable = environment.getVariable(varName, enclosingFunction);
		} else {
			variable = environment.registerVariable(varName, enclosingFunction);
			sb.append(statementFactory.variableDeclare(reader.getIndent(),
					variable));
			sb.append(InputReader.NEW_LINE);
		}

		if (!reader.hasNextWord()) {
			throw new InvalidSyntaxException(String.format(
					"Variable '%s' assign not defined", varName));
		}
		String value = reader.nextWord();
		String tail = reader.getWordTail();
		if (tail.endsWith("+")) {
			value = parseExpression(value, sb, reader);
		} else if (tail.startsWith("(")) {
			sb.append(parseCall(value, variable, reader));
			if (reader.getWordTail().endsWith("+")) {
				sb.append(InputReader.NEW_LINE);
				value = parseExpression(variable.getName(), sb, reader);
			} else {
				return sb.toString();
			}
		} else if (!isString(value) && !isUntouchable(value)) {
			value = environment.getVariable(value, enclosingFunction)
					.getNSISExpression();
		}
		sb.append(statementFactory.variableAssign(reader.getIndent(), variable,
				value));
		return sb.toString();
	}

	private String parseFunctionBegin(InputReader reader)
			throws InvalidSyntaxException, EnvironmentException {
		if (enclosingFunction != null) {
			throw new InvalidSyntaxException(
					"Cannot declare function within a function");
		}
		enclosingFunction = environment.registerUserFunction(reader.nextWord());
		while (reader.hasNextWord()) {
			enclosingFunction.addArgument(environment.registerVariable(
					reader.nextWord(), enclosingFunction));
		}
		return statementFactory.functionBegin(reader.getIndent(),
				enclosingFunction);
	}

	private String parseFunctionReturn(InputReader reader)
			throws InvalidSyntaxException, EnvironmentException {
		if (!(enclosingFunction != null)) {
			throw new InvalidSyntaxException(
					"Return is not allowed outside function");
		}
		StringBuilder sb = new StringBuilder();
		String value = null;
		if (reader.hasNextWord()) {
			enclosingFunction.setHasReturn(true);
			value = reader.nextWord();
			String tail = reader.getWordTail();
			if (tail.startsWith("(") || tail.endsWith("+")) {
				value = parseExpression(value, sb, reader);
			} else if (!isString(value) && !isUntouchable(value)) {
				value = environment.getVariable(value, enclosingFunction)
						.getNSISExpression();
			}
		}
		sb.append(statementFactory.functionReturn(reader.getIndent(),
				enclosingFunction, value));
		return sb.toString();
	}

	private String parseFunctionEnd(InputReader reader)
			throws InvalidSyntaxException {
		if (!(enclosingFunction != null)) {
			throw new InvalidSyntaxException(
					"FunctionEnd is not allowed outside function");
		}
		enclosingFunction = null;
		return statementFactory.functionEnd(reader.getIndent());
	}

	private String parseCall(String name, Variable returnVar, InputReader reader)
			throws InvalidSyntaxException, EnvironmentException {
		Callable callable = environment.getCallable(name);
		if (returnVar != null && callable.getReturnType() == ReturnType.VOID) {
			throw new InvalidSyntaxException("Function doesn't return a value");
		}

		StringBuilder sb = new StringBuilder();
		ArrayList<String> args = new ArrayList<String>();

		// Parse arguments
		String tail = reader.getWordTail();
		while (reader.hasNextWord() && !tail.matches(".*\\).*")) {
			String arg = reader.nextWord();
			tail = reader.getWordTail();
			if (!tail.startsWith(")")
					&& (tail.startsWith("(") || tail.endsWith("+"))) {
				arg = parseExpression(arg, sb, reader);
			} else if (!isString(arg) && !isUntouchable(arg)) {
				arg = environment.getVariable(arg, enclosingFunction)
						.getNSISExpression();
			}
			args.add(arg);
		}
		if (args.size() > callable.getArgsCount()) {
			throw new InvalidSyntaxException(
					String.format(
							"Too many function arguments (expected at most %d, provided %d)",
							callable.getArgsCount(), args.size()));
		} else if (args.size() < callable.getMandatoryArgsCount()) {
			throw new InvalidSyntaxException(
					String.format(
							"Too few function arguments provided (expected at minimum %d, provided %d)",
							callable.getMandatoryArgsCount(), args.size()));
		}

		// Push empty arguments for function if they're not defined
		for (int i = args.size(); i < callable.getArgsCount(); i++) {
			args.add(StatementFactory.NULL);
		}

		sb.append(statementFactory.call(reader.getIndent(), callable, args,
				returnVar));
		return sb.toString();
	}

	protected String parseExpression(String expr, StringBuilder buffer,
			InputReader reader) throws InvalidSyntaxException,
			EnvironmentException {
		String tail = reader.getWordTail();
		if (tail.startsWith("(")) {
			// Function call
			Variable fReturn = parseInExpressionCall(expr, buffer, reader);
			if (tail.endsWith("==") || tail.endsWith("!=")) {
				expr = fReturn.getName();
			} else {
				expr = parseExpression(fReturn.getName(), buffer, reader);
			}
		} else if (tail.endsWith("+")) {
			// Concatenation
			if (!reader.hasNextWord()) {
				throw new InvalidSyntaxException(
						"'+' must be followed by an expression");
			}
			String right = reader.nextWord();
			if (reader.getWordTail().startsWith("(")) {
				right = parseExpression(right, buffer, reader);
			}
			expr = parseExpression(String.format("\"%s%s\"",
					getNSISExpression(expr), getNSISExpression(right)), buffer,
					reader);
		}
		if (!isString(expr) && !isUntouchable(expr)) {
			expr = environment.getVariable(expr, enclosingFunction)
					.getNSISExpression();
		}
		return expr;
	}

	private String getNSISExpression(String expr) throws EnvironmentException {
		if (isString(expr)) {
			expr = expr.substring(1, expr.length() - 1);
		} else if (!isUntouchable(expr)) {
			expr = environment.getVariable(expr, enclosingFunction)
					.getNSISExpression();
		}
		return expr;
	}

	private Variable parseInExpressionCall(String callableName,
			StringBuilder buffer, InputReader reader)
			throws InvalidSyntaxException, EnvironmentException {
		Variable fReturn = environment.registerVariable(environment
				.getNameGenerator().generate(), enclosingFunction);
		buffer.append(statementFactory.variableDeclare(reader.getIndent(),
				fReturn));
		buffer.append(InputReader.NEW_LINE);
		buffer.append(parseCall(callableName, fReturn, reader));
		buffer.append(InputReader.NEW_LINE);
		return fReturn;
	}

	private static boolean isUntouchable(String expr) {
		if (expr.charAt(0) == '$') {
			return true; // It's NSIS constant or lang string
		}

		// Check for numeric value
		char[] charArray = expr.toCharArray();
		for (int i = charArray[0] == '-' ? 1 : 0; i < charArray.length; i++) {
			if (!Character.isDigit(charArray[i]))
				return false; // It's not numeric
		}
		return true;// It's numeric
	}

	private static boolean isString(String expr) {
		char c = expr.charAt(0);
		return (c == '"' || c == '\'');
	}

}
