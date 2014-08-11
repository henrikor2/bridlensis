package bridlensis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;

import bridlensis.env.SimpleNameGenerator;

public class MakeBridleNSISTest {

	private static final String DEFAULT_ENCODING = "Cp1252";

	private File tempDir;

	@Before
	public void setUp() throws IOException {
		tempDir = Files.createTempDirectory("BRIDLE").toFile();
		tempDir.deleteOnExit();
	}

	private File getResourceAsFile(String name) {
		try {
			return new File(ClassLoader.getSystemResource("bridlensis/" + name)
					.toURI());
		} catch (URISyntaxException e) {
			throw new AssertionError();
		}
	}

	@Test
	public void testVariables() throws FileNotFoundException, IOException,
			BridleNSISException {
		File inputFile = getResourceAsFile("Variables.nsh");
		File expectedFile = getResourceAsFile("Variables.bnsh");
		File outputFile = new File(tempDir, "Variables.bnsh");
		outputFile.deleteOnExit();

		MakeBridleNSIS.makeBridleNSIS(new SimpleNameGenerator(), inputFile,
				outputFile, DEFAULT_ENCODING, null, System.out);
		assertFiles(expectedFile, outputFile, DEFAULT_ENCODING);
	}

	@Test
	public void testFunctions() throws IOException, ParserException,
			BridleNSISException {
		File inputFile = getResourceAsFile("Functions.nsh");
		File expectedFile = getResourceAsFile("Functions.bnsh");
		File outputFile = new File(tempDir, "Functions.bnsh");
		outputFile.deleteOnExit();

		MakeBridleNSIS.makeBridleNSIS(new SimpleNameGenerator(), inputFile,
				outputFile, DEFAULT_ENCODING, null, System.out);
		assertFiles(expectedFile, outputFile, DEFAULT_ENCODING);
	}

	@Test
	public void testInclude() throws IOException, ParserException,
			BridleNSISException {
		File inputFile = getResourceAsFile("Include1.nsh");
		File expectedFile1 = getResourceAsFile("Include1.bnsh");
		File expectedFile2 = getResourceAsFile("Include2.bnsh");
		File outputFile1 = new File(tempDir, "Include1.bnsh");
		File outputFile2 = new File(tempDir, "Include2.bnsh");
		outputFile1.deleteOnExit();
		outputFile2.deleteOnExit();

		try {
			// Excluding Include2.nsh will fail the Makebridle process
			MakeBridleNSIS.makeBridleNSIS(new SimpleNameGenerator(), inputFile,
					outputFile1, DEFAULT_ENCODING,
					Arrays.asList("Include2.nsh"), System.out);
			fail();
		} catch (BridleNSISException e) {
			// All good
			System.err.println(e.getMessage());
		}

		MakeBridleNSIS.makeBridleNSIS(new SimpleNameGenerator(), inputFile,
				outputFile1, DEFAULT_ENCODING, null, System.out);
		assertFiles(expectedFile1, outputFile1, DEFAULT_ENCODING);
		assertFiles(expectedFile2, outputFile2, DEFAULT_ENCODING);
	}

	@Test
	public void testI18N() throws IOException, ParserException,
			BridleNSISException {
		File inputFile = getResourceAsFile("I18N.nsi");
		File inputFile_ja = getResourceAsFile("I18N_ja.nsh");
		File expectedFile = getResourceAsFile("I18N.bnsi");
		File expectedFile_ja1 = getResourceAsFile("I18N_ja1.bnsh");
		File expectedFile_ja2 = getResourceAsFile("I18N_ja2.bnsh");
		File outputFile = new File(tempDir, "I18N.bnsi");
		File outputFile_ja = new File(tempDir, "I18N_ja.bnsh");

		// Parser fails to read any input if using wrong encoding
		MakeBridleNSIS.makeBridleNSIS(new SimpleNameGenerator(), inputFile,
				outputFile, "UTF-16LE", null, System.out);
		assertFiles(expectedFile, outputFile, "UTF-16LE");
		assertTrue(outputFile.delete());
		assertEquals(2, outputFile_ja.length()); // Output file contains BOM
		assertTrue(outputFile_ja.delete());

		// Must use SJIS for parsing the Japanese file
		MakeBridleNSIS.makeBridleNSIS(new SimpleNameGenerator(), inputFile_ja,
				outputFile_ja, "SJIS", null, System.out);
		assertFiles(expectedFile_ja1, outputFile_ja, "SJIS");
		assertTrue(outputFile_ja.delete());

		// Use exclude to skip language files
		MakeBridleNSIS.makeBridleNSIS(new SimpleNameGenerator(), inputFile,
				outputFile, "UTF-16LE", Arrays.asList("I18N_ja.nsh"),
				System.out);
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
