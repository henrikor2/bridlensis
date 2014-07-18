package bridlensis;

import java.io.File;
import java.util.Scanner;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

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

	@Override
	public void execute() throws BuildException {
		if (args.getInputFile() == null) {
			throw new BuildException("Parameter 'file' not defined.");
		}
		try {
			int exitCode = MakeBridleNSIS.execute(args);
			if (exitCode != 0) {
				throw new BuildException("NSIS returned error code " + exitCode);
			}
		} catch (BridleNSISException e) {
			throw new BuildException(e);
		}
	}

}
