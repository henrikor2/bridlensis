package bridlensis;

import java.io.PrintStream;

public class Logger {

	public static final int ERROR = 0;
	public static final int WARN = 1;
	public static final int INFO = 2;
	public static final int DEBUG = 3;

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

	public void setLogLevel(int level) {
		this.level = level;
	}

	private void log(int level, String msg) {
		if (level <= this.level) {
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
