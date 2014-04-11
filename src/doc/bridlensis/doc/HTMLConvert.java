package bridlensis.doc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

import org.markdown4j.Markdown4jProcessor;

import bridlensis.env.Environment;

public class HTMLConvert {

	private static final String MANUAL_MD = "manual.md";
	private static final String MANUAL_CSS = "manual.css";
	private static final String MANUAL_HTML = "Manual.html";

	public static void main(String[] args) {
		int exitCode = 0;

		BufferedWriter output = null;
		InputStreamReader input = null;
		try {
			input = new InputStreamReader(new FileInputStream(new File(
					MANUAL_MD)), "UTF-8");
			if (input.read() != 0xFEFF) {
				throw new java.lang.AssertionError(MANUAL_MD
						+ " must be UTF-8 with BOM.");
			}
			output = beginHTMLFile(new File(MANUAL_HTML), new Scanner(new File(
					MANUAL_CSS), "UTF-8"));
			output.write(new Markdown4jProcessor().process(input));
			writeInstructions(output);
			output.write("<p style=\"color: #CCCCCC; margin-top: 24px;\">;eof BridleNSIS Manual</p>");
			endHTMLFile(output);
			System.out.println("Manual done");
		} catch (IOException e) {
			System.err.println(e.getMessage());
			exitCode = 1;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					// Ignore
				}
			}
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					// Ignore
				}
			}
		}

		System.exit(exitCode);
	}

	private static void writeInstructions(BufferedWriter output)
			throws IOException {
		Scanner scanner = Environment.getBuiltinInstructionsDef();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.startsWith("#")) {
				output.write("<h4>");
				output.write(line.substring(1).trim());
				output.write(":</h4>");
			} else if (line.length() > 0) {
				output.write("<p class=\"func\">");
				if (line.indexOf(" output") != -1) {
					output.write("val = ");
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
		scanner.close();
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
