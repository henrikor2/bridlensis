package bridlensis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;

import bridlensis.env.SimpleNameGenerator;

public class MakeBridleNSISTest {

	private static final String DEFAULT_ENCODING = "Cp1252";

	private static final String BASE_DIR = "src/test/bridlensis/";

	private File tempDir;

	@Before
	public void setUp() throws IOException {
		tempDir = Files.createTempDirectory("BRIDLE").toFile();
		tempDir.deleteOnExit();
	}

	@Test
	public void testVariables() throws FileNotFoundException, IOException,
			BridleNSISException {
		File inputFile = new File(BASE_DIR, "Variables.nsh");
		File expectedFile = new File(BASE_DIR, "Variables.bnsh");
		File outputFile = new File(tempDir, "Variables.bnsh");
		outputFile.deleteOnExit();

		MakeBridleNSIS.makeBridleNSIS(new SimpleNameGenerator(), inputFile,
				outputFile, DEFAULT_ENCODING, null);
		assertFiles(expectedFile, outputFile, DEFAULT_ENCODING);
	}

	@Test
	public void testFunctions() throws IOException, ParserException,
			BridleNSISException {
		File inputFile = new File(BASE_DIR, "Functions.nsh");
		File expectedFile = new File(BASE_DIR, "Functions.bnsh");
		File outputFile = new File(tempDir, "Functions.bnsh");
		outputFile.deleteOnExit();

		MakeBridleNSIS.makeBridleNSIS(new SimpleNameGenerator(), inputFile,
				outputFile, DEFAULT_ENCODING, null);
		assertFiles(expectedFile, outputFile, DEFAULT_ENCODING);
	}

	@Test
	public void testInclude() throws IOException, ParserException,
			BridleNSISException {
		File inputFile = new File(BASE_DIR, "Include1.nsh");
		File expectedFile1 = new File(BASE_DIR, "Include1.bnsh");
		File expectedFile2 = new File(BASE_DIR, "Include2.bnsh");
		File outputFile1 = new File(tempDir, "Include1.bnsh");
		File outputFile2 = new File(tempDir, "Include2.bnsh");
		outputFile1.deleteOnExit();
		outputFile2.deleteOnExit();

		try {
			// Excluding Include2.nsh will fail the Makebridle process
			MakeBridleNSIS.makeBridleNSIS(new SimpleNameGenerator(), inputFile,
					outputFile1, DEFAULT_ENCODING,
					Arrays.asList("Include2.nsh"));
			fail();
		} catch (BridleNSISException e) {
			// All good
			System.err.println(e.getMessage());
		}

		MakeBridleNSIS.makeBridleNSIS(new SimpleNameGenerator(), inputFile,
				outputFile1, DEFAULT_ENCODING, null);
		assertFiles(expectedFile1, outputFile1, DEFAULT_ENCODING);
		assertFiles(expectedFile2, outputFile2, DEFAULT_ENCODING);
	}

	@Test
	public void testI18N() throws IOException, ParserException,
			BridleNSISException {
		File inputFile = new File(BASE_DIR, "I18N.nsi");
		File inputFile_ja = new File(BASE_DIR, "I18N_ja.nsh");
		File expectedFile = new File(BASE_DIR, "I18N.bnsi");
		File expectedFile_ja1 = new File(BASE_DIR, "I18N_ja1.bnsh");
		File expectedFile_ja2 = new File(BASE_DIR, "I18N_ja2.bnsh");
		File outputFile = new File(tempDir, "I18N.bnsi");
		File outputFile_ja = new File(tempDir, "I18N_ja.bnsh");

		// Parser fails to read any input if using wrong encoding
		MakeBridleNSIS.makeBridleNSIS(new SimpleNameGenerator(), inputFile,
				outputFile, "UTF-16LE", null);
		assertFiles(expectedFile, outputFile, "UTF-16LE");
		assertTrue(outputFile.delete());
		assertEquals(2, outputFile_ja.length()); // Output file contains BOM
		assertTrue(outputFile_ja.delete());

		// Must use SJIS for parsing the Japanese file
		MakeBridleNSIS.makeBridleNSIS(new SimpleNameGenerator(), inputFile_ja,
				outputFile_ja, "SJIS", null);
		assertFiles(expectedFile_ja1, outputFile_ja, "SJIS");
		assertTrue(outputFile_ja.delete());

		// Use exclude to skip language files
		MakeBridleNSIS.makeBridleNSIS(new SimpleNameGenerator(), inputFile,
				outputFile, "UTF-16LE", Arrays.asList("I18N_ja.nsh"));
		assertFiles(expectedFile, outputFile, "UTF-16LE");
		assertTrue(outputFile.delete());
		assertFiles(expectedFile_ja2, outputFile_ja, "SJIS");
		assertTrue(outputFile_ja.delete());
	}

	private void assertFiles(File expectedFile, File actualFile, String encoding)
			throws FileNotFoundException, IOException {
		Scanner expected = new Scanner(expectedFile, encoding);
		Scanner actual = new Scanner(actualFile, encoding);
		int lineNumber = 0;
		while (expected.hasNextLine()) {
			assertTrue("Expected has more lines than the actual.",
					actual.hasNextLine());
			lineNumber++;
			assertEquals(lineNumber + ": " + expected.nextLine(), lineNumber
					+ ": " + actual.nextLine());
		}
		assertFalse("Actual has more lines than the expected.",
				actual.hasNextLine());
		expected.close();
		actual.close();
		assertEquals(expectedFile.length(), actualFile.length());
	}

}
