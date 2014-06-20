package bridlensis.env;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Iterator;

import org.junit.Test;

import bridlensis.InvalidSyntaxException;
import bridlensis.env.Callable.ReturnType;
import bridlensis.env.FunctionMsgBox.ButtonGroup;
import bridlensis.env.FunctionMsgBox.ReturnOption;

public class FunctionMsgBoxTest {

	@Test
	public void testGetArgsCount() {
		FunctionMsgBox mb = msgBox();
		assertEquals(4, mb.getArgsCount());
	}

	private FunctionMsgBox msgBox() {
		SimpleNameGenerator nameGenerator = new SimpleNameGenerator();
		Environment env = EnvironmentFactory.build(nameGenerator);
		try {
			return new FunctionMsgBox(nameGenerator, env.getCallable("strcpy"));
		} catch (EnvironmentException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testGetReturnType() {
		FunctionMsgBox mb = msgBox();
		assertEquals(ReturnType.OPTIONAL, mb.getReturnType());
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
		FunctionMsgBox mb = msgBox();
		Iterator<ReturnOption> iterator;

		iterator = mb.returnOptions(ButtonGroup.OK).iterator();
		assertReturnOption("IDOK", "msgbox_s01", iterator.next());
		assertFalse(iterator.hasNext());

		iterator = mb.returnOptions(ButtonGroup.ABORTRETRYIGNORE).iterator();
		assertReturnOption("IDABORT", "msgbox_s02", iterator.next());
		assertReturnOption("IDRETRY", "msgbox_s03", iterator.next());
		assertReturnOption("IDIGNORE", "msgbox_s04", iterator.next());
		assertFalse(iterator.hasNext());
	}

	private void assertReturnOption(String id, String jumpTo, ReturnOption ro) {
		assertEquals(id, ro.getID());
		assertEquals(jumpTo, ro.getGoTo());
	}

}
