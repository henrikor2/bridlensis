package bridlensis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.util.FileUtils;

public class ApacheAntTask extends Task {

	public static class NSISOption extends Task {

		private String value = null;

		public void setValue(String value) {
			this.value = value;
		}

	}

	public static class Exclude extends Task {

		private File file = null;

		public void setFile(File file) {
			this.file = file;
		}

	}

	private BridleNSISArguments args = new BridleNSISArguments();
	private boolean failOnError = true;
	private String resultProperty = null;
	private File outFile = null;

	public void setNsisHome(String nsisHome) {
		args.setNsisHome(nsisHome);
	}

	public void setFile(File file) {
		args.setInputFile(file);
	}

	public void setEncoding(String encoding) {
		args.setEncoding(encoding);
	}

	public void setOutput(File output) {
		args.setOutDir(output);
	}

	public void setExcludes(String excludes) {
		try (Scanner scanner = new Scanner(excludes)) {
			while (scanner.hasNext(System.getProperty("path.separator"))) {
				args.addExclude(scanner.next(System
						.getProperty("path.separator")));
			}
		}
	}

	public void addConfiguredExclude(Exclude exclude) {
		args.addExclude(exclude.file.getAbsolutePath());
	}

	public void addConfiguredNSISOption(NSISOption option) {
		args.addNSISOption(option.value);
	}

	public void setDir(File dir) {
		args.setDir(dir);
	}

	public void setFailOnError(boolean failOnError) {
		this.failOnError = failOnError;
	}

	public void setResultProperty(String resultProperty) {
		this.resultProperty = resultProperty;
	}

	public void setOutFile(File outFile) {
		this.outFile = outFile;
	}

	@Override
	public void execute() throws BuildException {
		validateArguments();
		PrintStream printStream = getPrintStream();
		try {
			int exitCode = MakeBridleNSIS.execute(args, printStream);
			if (exitCode != 0 && failOnError) {
				throw new BuildException("NSIS returned error code " + exitCode);
			}
			if (resultProperty != null) {
				getProject().setProperty(resultProperty,
						Integer.toString(exitCode));
			}
		} catch (BridleNSISException e) {
			if (failOnError) {
				throw new BuildException(e);
			} else {
				printStream.println(e.getMessage());
			}
		}
	}

	private void validateArguments() {
		if (args.getInputFile() == null) {
			throw new BuildException("Parameter 'file' not defined.");
		}
	}

	private PrintStream getPrintStream() throws BuildException {
		if (outFile == null) {
			return new PrintStream(new OutputStream() {

				private StringBuilder buffer = new StringBuilder(512);

				@Override
				public void write(int b) throws IOException {
					if (b == '\n') {
						log(buffer.toString());
						buffer = new StringBuilder(512);
					} else if (b != '\r') {
						buffer.append((char) b);
					}
				}
			});
		}
		if (!outFile.exists()) {
			try {
				if (!FileUtils.getFileUtils().createNewFile(outFile, true)) {
					throw new BuildException("Unable to create file "
							+ outFile.getAbsolutePath());
				}
			} catch (IOException e) {
				throw new BuildException(e);
			}
		}
		try {
			return new PrintStream(outFile);
		} catch (FileNotFoundException e) {
			throw new BuildException(e);
		}
	}

}
