package bridlensis.doc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

import org.markdown4j.Markdown4jProcessor;

import bridlensis.env.BuiltinElements;

public class HTMLConvert {

	private static final String UTF_8 = "UTF-8";

	private static final String MANUAL_INTRO = "manual_intro.md";
	private static final String MANUAL_USAGE = "manual_usage.md";
	private static final String MANUAL_LANGREF = "manual_langref.md";
	private static final String MANUAL_FUNCREF = "manual_funcref.md";
	private static final String MANUAL_FUNCREF_INSTRUCTIONS = "manual_funcref_instructions.md";
	private static final String MANUAL_FUNCREF_HEADERS = "manual_funcref_headers.md";
	private static final String MANUAL_CSS = "manual.css";
	private static final String MANUAL_HTML = "Manual.html";

	private static final String RELNOTES_MD = "release_notes.md";
	private static final String RELNOTES_CSS = "manual.css";
	private static final String RELNOTES_HTML = "Release Notes.html";

	public static void main(String[] args) {
		int exitCode = 0;
		try {
			writeReleaseNotes();
			writeManual();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			exitCode = 1;
		}
		System.exit(exitCode);
	}

	private static void writeReleaseNotes() throws IOException {
		try (BufferedWriter output = beginHTMLFile(new File(RELNOTES_HTML),
				new Scanner(new File(RELNOTES_CSS), UTF_8))) {
			markdownToHtml(RELNOTES_MD, output);
			endHTMLFile(output);
		}
		System.out.println("Release Notes done");
	}

	private static void writeManual() throws IOException {
		try (BufferedWriter output = beginHTMLFile(new File(MANUAL_HTML),
				new Scanner(new File(MANUAL_CSS), UTF_8))) {
			markdownToHtml(MANUAL_INTRO, output);
			markdownToHtml(MANUAL_USAGE, output);
			markdownToHtml(MANUAL_LANGREF, output);
			markdownToHtml(MANUAL_FUNCREF, output);
			markdownToHtml(MANUAL_FUNCREF_HEADERS, output);
			writeFunctionsReferences(
					BuiltinElements.getBuiltinHeaderFunctionsDef(), output);
			markdownToHtml(MANUAL_FUNCREF_INSTRUCTIONS, output);
			writeFunctionsReferences(
					BuiltinElements.getBuiltinInstructionsDef(), output);
			output.write("<p style=\"color: #CCCCCC; margin-top: 24px;\">;eof BridleNSIS Manual</p>");
			endHTMLFile(output);
		}
		System.out.println("Manual done");
	}

	private static void writeFunctionsReferences(InputStream input,
			BufferedWriter output) throws IOException {
		try (Scanner scanner = new Scanner(input)) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (line.startsWith("#")) {
					output.write("<h4>");
					output.write(line.substring(1).trim());
					output.write(":</h4>");
				} else if (line.length() > 0) {
					output.write("<p class=\"func\">");
					if (line.indexOf(" output") != -1) {
						output.write("ret = ");
						line = line.replaceFirst(" output", "");
					}
					String[] parts = line.split("\\s+");
					output.write(parts[0]);
					output.write("(");
					for (int i = 1; i < parts.length; i++) {
						output.write(parts[i]);
						if (i + 1 < parts.length) {
							output.write(", ");
						}
					}
					output.write(")</p>");
				}
			}
		}
	}

	private static void markdownToHtml(String markdownFile,
			BufferedWriter output) throws IOException,
			UnsupportedEncodingException, FileNotFoundException, AssertionError {
		try (InputStreamReader inputUTF8 = getInputUTF8(markdownFile)) {
			output.write(new Markdown4jProcessor().process(inputUTF8));
		}
	}

	private static InputStreamReader getInputUTF8(String fileName)
			throws UnsupportedEncodingException, FileNotFoundException,
			IOException, AssertionError {
		InputStreamReader input;
		input = new InputStreamReader(new FileInputStream(new File(fileName)),
				UTF_8);
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

	private static BufferedWriter beginHTMLFile(File htmlFile,
			Scanner cssFileScanner) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(htmlFile));

		writer.write("<html>\r\n");
		writer.write("<head>\r\n");
		writer.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\r\n");
		writer.write("</head>\r\n");

		while (cssFileScanner.hasNextLine()) {
			writer.write(cssFileScanner.nextLine());
			writer.write("\r\n");
		}
		cssFileScanner.close();

		writer.write("<body>\r\n");
		return writer;
	}

}
