package bridlensis;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import bridlensis.env.EnvironmentFactory;
import bridlensis.env.Environment;
import bridlensis.env.EnvironmentException;
import bridlensis.env.SimpleTypeObject;
import bridlensis.env.UserFunction;

public class NSISStatementsTest {

	private Environment env;

	public NSISStatementsTest() {
		env = EnvironmentFactory.build(null);
	}

	@Test
	public void testVariableDeclare() throws InvalidSyntaxException,
			EnvironmentException {
		assertEquals(
				"Var /GLOBAL a",
				NSISStatements.variableDeclare("",
						env.registerVariable("a", null)));
		assertEquals(
				"  Var /GLOBAL foo",
				NSISStatements.variableDeclare("  ",
						env.registerVariable("Foo", null)));
	}

	@Test
	public void testVariableAssign() throws InvalidSyntaxException,
			EnvironmentException {
		assertEquals(
				"  StrCpy $a 1",
				NSISStatements.variableAssign("  ",
						env.registerVariable("a", null),
						SimpleTypeObject.integer(1)));
		assertEquals(
				"StrCpy $foo $a",
				NSISStatements.variableAssign("",
						env.registerVariable("foo", null),
						SimpleTypeObject.special("$a")));
	}

	@Test
	public void testFunctionBegin() throws InvalidSyntaxException,
			EnvironmentException {
		UserFunction foo = env.registerUserFunction("Foo");
		assertEquals("    Function foo",
				NSISStatements.functionBegin("    ", foo));

		UserFunction bar = env.registerUserFunction("bar");
		bar.addArgument(env.registerVariable("a", bar));
		bar.addArgument(env.registerVariable("b", bar));
		bar.addArgument(env.registerVariable("c", bar));
		bar.setHasReturn(true);
		assertEquals(
				"  Function bar\r\n    Pop $bar.a\r\n    Pop $bar.b\r\n    Pop $bar.c",
				NSISStatements.functionBegin("  ", bar));
	}

	@Test
	public void testFunctionReturn() throws InvalidSyntaxException,
			EnvironmentException {
		UserFunction foo = env.registerUserFunction("Foo");
		assertEquals("    Return",
				NSISStatements.functionReturn("    ", foo, null));

		UserFunction bar = env.registerUserFunction("bar");
		bar.setHasReturn(true);
		assertEquals(
				"Push \"hello world!\"\r\nReturn",
				NSISStatements.functionReturn("", bar,
						SimpleTypeObject.string("hello world!")));
	}
}
