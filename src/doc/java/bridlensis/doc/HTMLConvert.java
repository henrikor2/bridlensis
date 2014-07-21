package bridlensis.doc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

import org.markdown4j.Markdown4jProcessor;

public class HTMLConvert {

	private static final String UTF_8 = "UTF-8";

	private static final String MANUAL_MD = "manual.md";
	private static final String MANUAL_CSS = "manual.css";
	private static final String MANUAL_MD_HTML_FILENAME = "Manual.html";

	private static final String RELNOTES_MD = "release_notes.md";
	private static final String RELNOTES_CSS = MANUAL_CSS;
	private static final String RELNOTES_HTML_FILENAME = "Release Notes.html";

	public static void main(String[] args) {
		int exitCode = 0;
		try {
			File outDir = new File((args.length > 0 ? args[0] : ".") + '\\');
			if (!outDir.exists() && !outDir.isDirectory()) {
				if (!outDir.mkdirs()) {
					System.out.println("Uable to create outdir "
							+ outDir.getAbsolutePath());
					System.exit(1);
				}
			}
			writeReleaseNotes(outDir);
			writeManual(outDir);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			exitCode = 1;
		}
		System.exit(exitCode);
	}

	private static void writeReleaseNotes(File outDir) throws IOException {
		try (BufferedWriter output = beginHTMLFile(RELNOTES_CSS,
				outDir.getPath() + System.getProperty("file.separator")
						+ RELNOTES_HTML_FILENAME)) {
			markdownToHtml(RELNOTES_MD, output);
			endHTMLFile(output);
		}
		System.out.println("Release Notes done");
	}

	private static void writeManual(File outDir) throws IOException {
		try (BufferedWriter output = beginHTMLFile(MANUAL_CSS, outDir.getPath()
				+ System.getProperty("file.separator")
				+ MANUAL_MD_HTML_FILENAME)) {
			markdownToHtml(MANUAL_MD, output);
			output.write("<p style=\"color: #CCCCCC; margin-top: 24px;\">;eof BridleNSIS Manual</p>");
			endHTMLFile(output);
		}
		System.out.println("Manual done");
	}

	private static void markdownToHtml(String markdownFile,
			BufferedWriter output) throws IOException,
			UnsupportedEncodingException, FileNotFoundException, AssertionError {
		try (InputStreamReader input = openMarkdownSourceFile(markdownFile)) {
			Markdown4jProcessor processor = new Markdown4jProcessor()
					.registerPlugins(new BuiltinVariablesPlugin(),
							new ReservedWordsPlugin(), new FunctionsPlugin(),
							new VersionPlugin(), new UsagePlugin());
			output.write(processor.process(input));
		}
	}

	private static InputStreamReader openMarkdownSourceFile(String fileName)
			throws UnsupportedEncodingException, FileNotFoundException,
			IOException, AssertionError {
		InputStreamReader input = new InputStreamReader(
				HTMLConvert.class.getResourceAsStream(fileName), UTF_8);
		if (input.read() != 0xFEFF) {
			throw new java.lang.AssertionError(fileName
					+ " must be UTF-8 with BOM.");
		}
		return input;
	}

	private static void endHTMLFile(BufferedWriter writer) throws IOException {
		writer.write("\r\n</body>\r\n</html>");
		writer.flush();
		writer.close();
	}

	private static BufferedWriter beginHTMLFile(String cssFileName,
			String outputFileName) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outputFileName), UTF_8));
		writer.write("<html>\r\n");
		writer.write("<head>\r\n");
		writer.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\r\n");
		writer.write("</head>\r\n");
		try (Scanner cssFileScanner = new Scanner(
				HTMLConvert.class.getResourceAsStream(cssFileName), UTF_8)) {
			while (cssFileScanner.hasNextLine()) {
				writer.write(cssFileScanner.nextLine());
				writer.write("\r\n");
			}
		}
		writer.write("<body>\r\n");
		return writer;
	}
}
