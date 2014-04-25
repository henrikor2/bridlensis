package bridlensis;

import static org.junit.Assert.*;

import org.junit.Test;

public class InputTextTest {

	@Test
	public void testSet() {
		InputText line = new InputText();
		line.set("DetailPrint hello", 0);
		assertEquals("DetailPrint hello", line.toString());
		assertEquals(0, line.cursorPos());

		line.set("  DetailPrint hello", 2);
		assertEquals("  DetailPrint hello", line.toString());
		assertEquals(2, line.cursorPos());
	}

	@Test
	public void testEndsWith() {
		InputText line = new InputText();
		line.set("DetailPrint \"hello \\ ", 0);
		assertTrue(line.endsWith('\\', " \t\r\n\\"));
	}

	@Test
	public void testAppend() {
		InputText line = new InputText();
		line.set("DetailPrint \"hello \\ ", 0);
		line.append("\r\n  world!\"");
		assertEquals(0, line.cursorPos());
		assertEquals("DetailPrint \"hello \\ \r\n  world!\"", line.toString());
	}

	@Test
	public void testIsAtEnd() {
		InputText line = new InputText();
		line.set("DetailPrint \"hello \\ ", 0);
		assertFalse(line.isAtEnd());
		line.skip(20);
		assertFalse(line.isAtEnd());
		line.skip(1);
		assertTrue(line.isAtEnd());
	}

	@Test
	public void testGoToEnd() {
		InputText line = new InputText();
		line.set("DetailPrint \"hello \\ ", 0);
		assertFalse(line.isAtEnd());
		line.goToEnd();
		assertTrue(line.isAtEnd());
	}

	@Test
	public void testSkip() {
		InputText line = new InputText();
		line.set("    a = \"hello\" + foo(bar(a))", 4);
		assertEquals(4, line.cursorPos());
		line.skip(10);
		assertEquals(14, line.cursorPos());
		line.skip(19);
		assertEquals(29, line.cursorPos());
	}

	@Test
	public void testCharAtCursorIn() {
		InputText line = new InputText();
		line.set("    a = \"hello\" + foo(bar(a)) ; hellooo", 4);
		assertTrue(line.charAtCursorIn("a"));
		line.skip(4);
		assertTrue(line.charAtCursorIn("\""));
		line.skip(10);
		assertTrue(line.charAtCursorIn("fxx"));
		line.skip(12);
		assertTrue(line.charAtCursorIn(";#"));
	}

	@Test
	public void testCursorPrecededBy() {
		InputText line = new InputText();
		line.set("    a = \"hello $\\\"world$\\\"\"", 17);
		assertTrue(line.cursorPrecededBy("$\\"));
	}

	@Test
	public void testCursorFollowedBy() {
		InputText line = new InputText();
		line.set("    a = 1 ; hellooo \\ ", 4);
		assertTrue(line.cursorFollowedBy("a = 1"));
		line.append("\r\n    world \\ ");
		line.skip(24);
		assertTrue(line.cursorFollowedBy("world"));
		line.append("\r\n    !");
		line.skip(14);
		assertTrue(line.cursorFollowedBy("!"));

		line.set("    a = 1 /* hellooo */ ", 4);
		assertTrue(line.cursorFollowedBy("a = 1"));
		assertFalse(line.cursorFollowedBy("A = 1"));
		line.skip(6);
		assertTrue(line.cursorFollowedBy("/*"));
		line.skip(13);
		assertTrue(line.cursorFollowedBy(" "));
	}

	@Test
	public void testSeekString() {
		InputText line = new InputText();
		line.set("    a = 1 /* hellooo */ ", 4);
		assertTrue(line.seekString("a = 1"));
		assertEquals(4, line.cursorPos());
		assertTrue(line.seekString("/*"));
		assertEquals(10, line.cursorPos());
		assertTrue(line.seekString("*/"));
		assertEquals(21, line.cursorPos());
		assertFalse(line.seekString("X"));
		assertTrue(line.isAtEnd());
	}

	@Test
	public void testSeekChars() {
		InputText line = new InputText();
		line.set("    a = 1 ; hello", 4);
		assertTrue(line.seekChars("="));
		assertEquals(6, line.cursorPos());
		assertTrue(line.seekChars(";"));
		assertEquals(10, line.cursorPos());
		assertFalse(line.seekChars("X"));
		assertTrue(line.isAtEnd());
	}

}
