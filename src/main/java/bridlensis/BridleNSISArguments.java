package bridlensis;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BridleNSISArguments {

	private File inputFile;
	private File dir;
	private String encoding;
	private String nsisHome;
	private File outDir;
	private ArrayList<String> nsisOptions;
	private ArrayList<String> excludeFiles;
	private int logLevel;

	public BridleNSISArguments() {
		inputFile = null;
		encoding = System.getProperty("file.encoding");
		nsisHome = null;
		outDir = null;
		dir = null;
		nsisOptions = new ArrayList<String>();
		excludeFiles = new ArrayList<String>();
		logLevel = Logger.INFO;
	}

	public File getInputFile() {
		return inputFile;
	}

	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}

	public File getDir() {
		return dir;
	}

	public void setDir(File dir) {
		this.dir = dir;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getNsisHome() {
		return nsisHome;
	}

	public void setNsisHome(String nsisHome) {
		this.nsisHome = nsisHome;
	}

	public File getOutDir() {
		return outDir;
	}

	public void setOutDir(File outDir) {
		this.outDir = outDir;
	}

	public Collection<String> getNSISOptions() {
		return nsisOptions;
	}

	public Collection<String> getExcludeFiles() {
		return excludeFiles;
	}

	public void addNSISOption(String value) {
		nsisOptions.add(value);
	}

	public void addExclude(String filespec) {
		excludeFiles.add(filespec);
	}

	public void addAllExcludes(List<String> filespecs) {
		excludeFiles.addAll(excludeFiles);
	}

	public int getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(int logLevel) {
		this.logLevel = logLevel;
	}

}
