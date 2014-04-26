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

import bridlensis.InputReader.Word;
import bridlensis.InputReader.WordTail;
import bridlensis.env.Callable;
import bridlensis.env.Callable.ReturnType;
import bridlensis.env.Environment;
import bridlensis.env.EnvironmentException;
import bridlensis.env.UserFunction;
import bridlensis.env.ComparisonStatement;
import bridlensis.env.Variable;

public class Parser {

	public static final char UTF16BE_BOM = '\uFFFE';
	public static final char UTF16LE_BOM = '\uFEFF';
	public static final String NEWLINE_MARKER = "\r\n";

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
			writer.write(UTF16LE_BOM);
		} else if (encoding.equalsIgnoreCase("UTF-16BE")) {
			writer.write(UTF16BE_BOM);
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
				writer.write(Parser.NEWLINE_MARKER);
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
		Word word = reader.nextWord();
		String keyword = word.asName();
		if (reader.getWordTail().isAssignment()) {
			return parseVarAssign(word, reader);
		} else if (reader.getWordTail().isFunctionArgsOpen()) {
			return parseCall(word, null, reader);
		} else if (keyword.equals("var")) {
			return parseVarDeclare(reader);
		} else if (keyword.equals("function")) {
			return parseFunctionBegin(reader);
		} else if (keyword.equals("return")) {
			return parseFunctionReturn(reader);
		} else if (keyword.equals("functionend")) {
			return parseFunctionEnd(reader);
		} else if (keyword.equals("if") || keyword.equals("elseif")) {
			return parseIf(word, reader);
		} else if (keyword.equals("else")) {
			return statementFactory.logicLibDefine(reader.getIndent(), "Else");
		} else if (keyword.equals("endif")) {
			return statementFactory.logicLibDefine(reader.getIndent(), "EndIf");
		} else if (keyword.equals("do")) {
			return parseDoLoop("Do", reader);
		} else if (keyword.equals("continue")) {
			return statementFactory.logicLibDefine(reader.getIndent(),
					"Continue");
		} else if (keyword.equals("break")) {
			return statementFactory.logicLibDefine(reader.getIndent(), "Break");
		} else if (keyword.equals("loop")) {
			return parseDoLoop("Loop", reader);
		} else if (reader.getWordTail().isCompilerCommand()
				&& reader.nextWord().asName().equals("include")) {
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
		ComparisonStatement statement = parseComparisonStatement(
				reader.nextWord(), reader, sb);
		if (statement.isNot()) {
			throw new InvalidSyntaxException(String.format(
					"Illegal modifier 'Not' in %s statement", keyword));
		}
		sb.append(statementFactory.logicLibComparisonStatement(
				reader.getIndent(), keyword, statement));
		return sb.toString();
	}

	private String parseIf(Word keyword, InputReader reader)
			throws InvalidSyntaxException, EnvironmentException {
		StringBuilder sb = new StringBuilder();
		StringBuilder buffer = new StringBuilder();
		ComparisonStatement ifStatement = parseComparisonStatement(keyword,
				reader, buffer);
		sb.append(statementFactory.logicLibComparisonStatement(
				reader.getIndent(), ifStatement));
		while (reader.hasNextWord()) {
			sb.append(Parser.NEWLINE_MARKER);
			sb.append(statementFactory.logicLibComparisonStatement(
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
		statement.addLeft(parseExpression(left, buffer, reader).getValue());

		while (reader.hasNextWord()) {
			String compare = reader.getWordTail().getComparison();
			if (compare.isEmpty()) {
				statement.addLeft(parseExpression(reader.nextWord(), buffer,
						reader).getValue());
			} else {
				statement.setCompare(compare);
				break;
			}
		}

		if (reader.hasNextWord()) {
			statement.addRight(parseExpression(reader.nextWord(), buffer,
					reader).getValue());
		}

		return statement;
	}

	private String parseInclude(InputReader reader)
			throws InvalidSyntaxException, ParserException {
		String inputFileName = reader.nextWord().asBareString();
		File inputFile = new File(baseDir, inputFileName);
		String statement;
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
		Word name = reader.nextWord();
		Variable variable = environment.registerVariable(name.asName(),
				enclosingFunction);
		return statementFactory.variableDeclare(reader.getIndent(), variable);
	}

	private String parseVarAssign(Word varName, InputReader reader)
			throws InvalidSyntaxException, EnvironmentException {
		StringBuilder sb = new StringBuilder();
		Variable variable;

		if (environment.containsVariable(varName.asName(), enclosingFunction)) {
			variable = environment.getVariable(varName.asName(),
					enclosingFunction);
		} else {
			variable = environment.registerVariable(varName.asName(),
					enclosingFunction);
			sb.append(statementFactory.variableDeclare(reader.getIndent(),
					variable));
			sb.append(Parser.NEWLINE_MARKER);
		}

		Word value = reader.nextWord();
		WordTail tail = reader.getWordTail();
		if (tail.isConcatenation()) {
			value = parseExpression(value, sb, reader);
		} else if (tail.isFunctionArgsOpen()) {
			sb.append(parseCall(value, variable, reader));
			if (reader.getWordTail().isConcatenation()) {
				sb.append(Parser.NEWLINE_MARKER);
				value = parseExpression(new Word(variable.getName()), sb,
						reader);
			} else {
				return sb.toString();
			}
		} else if (!value.isString() && !value.isUntouchable()) {
			value = new Word(environment.getVariable(value.asName(),
					enclosingFunction).getNSISExpression());
		}
		sb.append(statementFactory.variableAssign(reader.getIndent(), variable,
				value.getValue()));
		return sb.toString();
	}

	private String parseFunctionBegin(InputReader reader)
			throws InvalidSyntaxException, EnvironmentException {
		if (enclosingFunction != null) {
			throw new InvalidSyntaxException(
					"Cannot declare function within a function");
		}
		enclosingFunction = environment.registerUserFunction(reader.nextWord()
				.asName());
		while (reader.hasNextWord()) {
			enclosingFunction.addArgument(environment.registerVariable(reader
					.nextWord().asName(), enclosingFunction));
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
		Word value = null;
		if (reader.hasNextWord()) {
			enclosingFunction.setHasReturn(true);
			value = reader.nextWord();
			WordTail tail = reader.getWordTail();
			if (tail.isFunctionArgsOpen() || tail.isConcatenation()) {
				value = parseExpression(value, sb, reader);
			} else if (!value.isString() && !value.isUntouchable()) {
				value = new Word(environment.getVariable(value.asName(),
						enclosingFunction).getNSISExpression());
			}
		}
		sb.append(statementFactory.functionReturn(reader.getIndent(),
				enclosingFunction, (value != null ? value.getValue() : null)));
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

	private String parseCall(Word name, Variable returnVar, InputReader reader)
			throws InvalidSyntaxException, EnvironmentException {
		Callable callable = environment.getCallable(name.asName());
		if (returnVar != null && callable.getReturnType() == ReturnType.VOID) {
			throw new InvalidSyntaxException("Function doesn't return a value");
		}

		StringBuilder sb = new StringBuilder();
		ArrayList<String> args = new ArrayList<String>();

		// Parse arguments
		WordTail tail = reader.getWordTail();
		while (reader.hasNextWord() && !tail.containsFunctionArgsClose()) {
			Word arg = reader.nextWord();
			tail = reader.getWordTail();
			if (!tail.isFunctionArgsClose()
					&& (tail.isFunctionArgsOpen() || tail.isConcatenation())) {
				arg = parseExpression(arg, sb, reader);
			} else if (!arg.isString() && !arg.isUntouchable()) {
				arg = new Word(environment.getVariable(arg.asName(),
						enclosingFunction).getNSISExpression());
			}
			args.add(arg.getValue());
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

	protected Word parseExpression(Word expr, StringBuilder buffer,
			InputReader reader) throws InvalidSyntaxException,
			EnvironmentException {
		WordTail tail = reader.getWordTail();
		if (tail.isFunctionArgsOpen()) {
			Variable fReturn = parseInExpressionCall(expr, buffer, reader);
			if (tail.isComparison()) {
				expr = new Word(fReturn.getNSISExpression());
			} else {
				expr = parseExpression(new Word(fReturn.getNSISExpression()),
						buffer, reader);
			}
		} else if (tail.isConcatenation()) {
			Word right = reader.nextWord();
			if (reader.getWordTail().isFunctionArgsOpen()) {
				right = parseExpression(right, buffer, reader);
			}
			expr = parseExpression(concatenate(expr, right), buffer, reader);
		}
		if (!expr.isString() && !expr.isUntouchable()) {
			expr = new Word(environment.getVariable(expr.asName(),
					enclosingFunction).getNSISExpression());
		}
		return expr;
	}

	private Word concatenate(Word left, Word right) throws EnvironmentException {
		return new Word(String.format("\"%s%s\"", convertToString(left),
				convertToString(right)));
	}

	private String convertToString(Word expr) throws EnvironmentException {
		if (expr.isString()) {
			expr = new Word(expr.asBareString());
		} else if (!expr.isUntouchable()) {
			expr = new Word(environment.getVariable(expr.asName(),
					enclosingFunction).getNSISExpression());
		}
		return expr.getValue();
	}

	private Variable parseInExpressionCall(Word callableName,
			StringBuilder buffer, InputReader reader)
			throws InvalidSyntaxException, EnvironmentException {
		Variable fReturn = environment.registerVariable(environment
				.getNameGenerator().generate(), enclosingFunction);
		buffer.append(statementFactory.variableDeclare(reader.getIndent(),
				fReturn));
		buffer.append(Parser.NEWLINE_MARKER);
		buffer.append(parseCall(callableName, fReturn, reader));
		buffer.append(Parser.NEWLINE_MARKER);
		return fReturn;
	}
}
