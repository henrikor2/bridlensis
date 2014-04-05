package bridlensis.env;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Test;

import bridlensis.InvalidSyntaxException;
import bridlensis.env.FunctionMsgBox;
import bridlensis.env.FunctionMsgBox.ButtonGroup;
import bridlensis.env.FunctionMsgBox.ReturnOption;

public class FunctionMsgBoxTest {

	@Test
	public void testGetArgsCount() {
		FunctionMsgBox mb = new FunctionMsgBox(null);
		assertEquals(4, mb.getArgsCount());
	}

	@Test
	public void testHasReturn() {
		FunctionMsgBox mb = new FunctionMsgBox(null);
		assertTrue(mb.hasReturn());
	}

	@Test
	public void testOptionList() {
		assertEquals("MB_OK", FunctionMsgBox.optionsList(ButtonGroup.OK, ""));
		assertEquals("MB_OKCANCEL|MB_ICONSTOP",
				FunctionMsgBox.optionsList(ButtonGroup.OKCANCEL, "ICONSTOP"));
		assertEquals(
				"MB_YESNOCANCEL|MB_ICONEXCLAMATION|MB_TOPMOST|MB_DEFBUTTON2",
				FunctionMsgBox.optionsList(ButtonGroup.YESNOCANCEL,
						"ICONEXCLAMATION|TOPMOST|DEFBUTTON2"));
	}

	@Test
	public void testReturnOptions() throws InvalidSyntaxException {
		FunctionMsgBox mb = new FunctionMsgBox(new SimpleNameGenerator());
		Iterator<ReturnOption> iterator;

		iterator = mb.returnOptions(ButtonGroup.OK).iterator();
		assertReturnOption("OK", "msgbox_s01", iterator.next());
		assertFalse(iterator.hasNext());

		iterator = mb.returnOptions(ButtonGroup.ABORTRETRYIGNORE).iterator();
		assertReturnOption("ABORT", "msgbox_s02", iterator.next());
		assertReturnOption("RETRY", "msgbox_s03", iterator.next());
		assertReturnOption("IGNORE", "msgbox_s04", iterator.next());
		assertFalse(iterator.hasNext());
	}

	private void assertReturnOption(String retVal, String jumpTo,
			ReturnOption ro) {
		assertEquals(retVal, ro.getReturnValue());
		assertEquals(jumpTo, ro.getGoTo());
	}

}
