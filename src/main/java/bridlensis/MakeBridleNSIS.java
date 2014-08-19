package bridlensis;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Scanner;

import bridlensis.env.DefaultNameGenerator;
import bridlensis.env.EnvironmentFactory;
import bridlensis.env.NameGenerator;

public class MakeBridleNSIS {

	public static final String VERSION;

	public static final int EXIT_OUTDIRERROR = 10;
	public static final int EXIT_MAKEBRIDLENSISERROR = 11;
	public static final int EXIT_MAKENSIS_NOT_FOUND = 12;
	public static final int EXIT_MAKENSISERROR = 13;

	private static final String MAKENSIS_EXE = "makensis.exe";

	private static final Logger logger = Logger.getInstance();

	static {
		try (Scanner versionFileScanner = new Scanner(MakeBridleNSIS.class
				.getClassLoader().getResourceAsStream("bridlensis/VERSION"),
				"UTF-8")) {
			VERSION = versionFileScanner.nextLine();
		}
	}

	public static String usage() {
		StringBuilder sb = new StringBuilder();
		try (Scanner scanner = new Scanner(MakeBridleNSIS.class
				.getClassLoader().getResourceAsStream("bridlensis/USAGE"),
				"UTF-8")) {
			while (scanner.hasNextLine()) {
				sb.append(scanner.nextLine().replaceFirst("%VERSION%", VERSION));
				sb.append("\r\n");
			}
		}
		return sb.toString();
	}

	public static String header() {
		StringBuilder sb = new StringBuilder();
		sb.append("BridleNSIS v");
		sb.append(VERSION);
		sb.append(" - Copyright (c) 2014 Contributors\r\n");
		sb.append("See the User Manual for license details and credits.\r\n");
		return sb.toString();
	}

	public static void main(String[] args) {
		logger.warn(header());

		BridleNSISArguments arguments = parseArguments(args);

		if (arguments.getInputFile() == null) {
			logger.warn("Usage: ");
			logger.warn("  " + usage());
			System.exit(0);
			return;
		}

		logger.setLogLevel(arguments.getLogLevel());

		int exitCode;
		try {
			exitCode = execute(arguments);
		} catch (BridleNSISException e) {
			logger.error(e);
			exitCode = e.getErrorCode();
		}

		System.exit(exitCode);
	}

	public static int execute(BridleNSISArguments arguments)
			throws BridleNSISException {
		File outputFile = getOutFile(arguments.getOutDir(),
				arguments.getInputFile());
		makeBridleNSIS(new DefaultNameGenerator(), arguments.getInputFile(),
				outputFile, arguments.getEncoding(),
				arguments.getExcludeFiles());
		String nsisHome = findNSISHome(arguments.getNsisHome());
		return makeNSIS(arguments.getDir(), outputFile.getAbsolutePath(),
				nsisHome, arguments.getNSISOptions());
	}

	private static BridleNSISArguments parseArguments(String[] args) {
		BridleNSISArguments arguments = new BridleNSISArguments();
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-d")) {
				arguments.setDir(new File(args[++i]));
			} else if (args[i].equals("-n")) {
				arguments.setNsisHome(args[++i]);
			} else if (args[i].equals("-o")) {
				arguments.setOutDir(new File(args[++i]));
			} else if (args[i].equals("-e")) {
				arguments.setEncoding(args[++i]);
			} else if (args[i].equals("-x")) {
				arguments.addAllExcludes(Arrays.asList(args[++i].split(System
						.getProperty("path.separator"))));
			} else if (args[i].equals("-q")) {
				arguments.setLogLevel(Logger.WARN);
			} else if (args[i].equals("-v")) {
				arguments.setLogLevel(Logger.DEBUG);
			} else if (args[i].startsWith("/")) {
				arguments.addNSISOption(args[i]);
			} else {
				arguments.setInputFile(new File(args[i]));
			}
		}
		return arguments;
	}

	private static File getOutFile(File outDir, File inputFile)
			throws BridleNSISException {
		if (outDir == null) {
			File parent = inputFile.getAbsoluteFile().getParentFile();
			if (parent == null) {
				throw new BridleNSISException(EXIT_OUTDIRERROR,
						"Unable to resolve outdir based on input file "
								+ inputFile.getAbsolutePath());
			}
			outDir = parent;
		}
		if (!outDir.mkdir() && !outDir.exists() && !outDir.isDirectory()) {
			throw new BridleNSISException(EXIT_OUTDIRERROR,
					"Unable to create outdir " + outDir.getAbsolutePath());
		}
		return new File(outDir, convertToBridleFilename(inputFile.getName()));
	}

	private static String findNSISHome(String startDir)
			throws BridleNSISException {
		if (startDir != null && !startDir.trim().isEmpty()
				&& isNSISHome(startDir)) {
			return startDir;
		}

		if (isNSISHome("")) {
			return "";
		}

		String homeX86 = System.getenv("ProgramFiles(x86)")
				+ System.getProperty("file.separator") + "NSIS";
		if (isNSISHome(homeX86)) {
			return homeX86;
		}

		String homeX64 = System.getenv("ProgramFiles")
				+ System.getProperty("file.separator") + "NSIS";
		if (isNSISHome(homeX64)) {
			return homeX64;
		}

		throw new BridleNSISException(EXIT_MAKENSIS_NOT_FOUND,
				"NSIS home not found.");
	}

	private static boolean isNSISHome(String dir) {
		String exec;
		if (dir == null || dir.trim().isEmpty()) {
			exec = MAKENSIS_EXE;
		} else {
			exec = new File(dir, MAKENSIS_EXE).getPath();
		}
		try {
			StringBuilder logMsg = new StringBuilder();
			ProcessBuilder builder = new ProcessBuilder(exec, "/VERSION");
			Process process = builder.start();
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(process.getInputStream(),
							System.getProperty("file.encoding")))) {
				String version = reader.readLine();
				logMsg.append("Detected NSIS version " + version);
			} catch (IOException e) {
				logMsg.append("Unable to detected NSIS version");
			}
			if (!exec.contains(System.getProperty("file.separator"))) {
				logMsg.append(" in system %PATH%");
			} else {
				logMsg.append(" in folder " + dir);
			}
			logMsg.append(".");
			logger.info(logMsg.toString());
			return (process.waitFor() == 0);
		} catch (IOException | InterruptedException e) {
			return false;
		}
	}

	static String convertToBridleFilename(String filename) {
		String outputFileName;
		int fileExtIndex = filename.lastIndexOf('.');
		if (fileExtIndex != -1) {
			outputFileName = filename.substring(0, fileExtIndex) + ".b"
					+ filename.substring(fileExtIndex + 1);
		} else {
			outputFileName = filename + ".bnsi";
		}
		return outputFileName;
	}

	protected static void makeBridleNSIS(NameGenerator nameGenerator,
			File inputFile, File outputFile, String encoding,
			Collection<String> excludeFiles) throws BridleNSISException {
		if (outputFile.equals(inputFile)) {
			throw new BridleNSISException(EXIT_MAKEBRIDLENSISERROR,
					"Cannot override input file");
		}

		File baseDir = inputFile.getParentFile();
		File outDir = outputFile.getParentFile();

		logger.info("Output: " + outDir.getAbsolutePath());
		logger.info("Encoding: " + encoding);
		logger.info("");

		StatementParser statementParser = new StatementParser(
				EnvironmentFactory.build(nameGenerator), nameGenerator);
		Parser parser = new Parser(statementParser, baseDir, outDir, encoding,
				excludeFiles);

		long time = System.currentTimeMillis();
		try {
			parser.parse(inputFile.getName(), outputFile.getName());
		} catch (IOException | ParserException e) {
			throw new BridleNSISException(EXIT_MAKEBRIDLENSISERROR, e);
		}
		time = System.currentTimeMillis() - time;
		time = time < 1000 ? 1 : time / 1000;

		logger.warn(String.format(
				"%nParsed in %d seconds total of %d lines in %d file(s).%n",
				time, parser.getInputLines(), parser.getFileCount()));
	}

	private static int makeNSIS(File dir, String filename, String nsisHome,
			Collection<String> nsisOptions) throws BridleNSISException {
		ArrayList<String> cmd = new ArrayList<String>();

		String exec = new File(nsisHome, MAKENSIS_EXE).getAbsolutePath();
		cmd.add(exec);

		StringBuilder logMsg = new StringBuilder();
		logMsg.append("Execute: ");
		logMsg.append("\"" + exec + "\" ");

		for (String option : nsisOptions) {
			cmd.add(option);
			logMsg.append("\"" + option + "\" ");
		}

		cmd.add(filename);

		logMsg.append("\"" + filename + "\"");
		logger.info(logMsg.toString());

		ProcessBuilder builder = new ProcessBuilder(cmd);
		if (dir == null) {
			dir = new File(System.getProperty("user.dir"));
		}
		logger.info("Work directory: \"" + dir.getAbsolutePath() + "\" ");
		builder.directory(dir);
		builder.redirectErrorStream(true);
		Process process;
		try {
			logger.warn("\nMakeNSIS ---->\n");
			process = builder.start();
		} catch (IOException e) {
			throw new BridleNSISException(EXIT_MAKENSISERROR, e);
		}

		InputStream processInput = process.getInputStream();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				processInput, "Cp1252"))) {
			String line;
			while ((line = reader.readLine()) != null) {
				logger.warn(line);
			}
		} catch (IOException e) {
			logger.warn("Unable to read makensis.exe output: " + e.getMessage());
		}

		try {
			return process.waitFor();
		} catch (InterruptedException e) {
			throw new BridleNSISException(EXIT_MAKENSISERROR, e);
		}
	}

}
