package bridlensis;

import java.io.PrintStream;

public class Logger {

	private static final int ERROR = 4;
	private static final int WARN = 3;
	private static final int INFO = 2;
	private static final int DEBUG = 1;

	private static Logger instance = null;

	public static Logger getInstance() {
		if (instance == null) {
			instance = new Logger();
		}
		return instance;
	}

	private PrintStream output;
	private int level;

	private Logger() {
		output = System.out;
		level = INFO;
	}

	public void setPrintStream(PrintStream printStream) {
		output = printStream;
	}

	private void log(int level, String msg) {
		if (level >= this.level) {
			output.println(msg);
		}
	}

	public void error(BridleNSISException e) {
		log(ERROR, e.getMessage());
	}

	public void warn(InputReader reader, String message) {
		warn(String.format("%s, line %d: %s", reader.getFile(),
				reader.getLinesRead(), message));
	}

	public void warn(String message) {
		log(WARN, message);
	}

	public void info(String message) {
		log(INFO, message);
	}

	public void info(InputReader reader, String message) {
		info(String.format("%s, line %d: %s", reader.getFile(),
				reader.getLinesRead(), message));
	}

	public void debug(String message) {
		log(DEBUG, message);
	}

	public void debug(InputReader reader, String message) {
		debug(String.format("%s, line %d: %s", reader.getFile(),
				reader.getLinesRead(), message));
	}

}
