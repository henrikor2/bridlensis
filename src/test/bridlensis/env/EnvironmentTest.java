package bridlensis.env;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class EnvironmentTest {

	@Test
	public void testLoadBuiltinFunctions() {
		SimpleNameGenerator nameGenerator = new SimpleNameGenerator();
		Environment env = new Environment();
		env.loadBuiltinFunctions(nameGenerator);
		try {
			env.registerUserFunction("DetailPrint");
			fail();
		} catch (EnvironmentException e) {
			System.err.println(e.getMessage());
		}
	}

	@Test
	public void testLoadBuiltinVariables() throws EnvironmentException {
		Environment env = new Environment();
		assertFalse(env.containsVariable("r9", null));
		env.loadBuiltinVariables();
		assertTrue(env.containsVariable("r2", null));
		assertTrue(env.containsVariable("r3", null));
		assertTrue(env.containsVariable("r9", null));
	}

	@Test
	public void testRegisterVariable() throws EnvironmentException {
		Environment env = new Environment();
		env.loadBuiltinVariables();
		assertEquals("a", env.registerVariable("a", null).getName());
		try {
			env.registerVariable("a", null);
			fail();
		} catch (EnvironmentException e) {
			System.err.println(e.getMessage());
		}
		UserFunction foo = env.registerUserFunction("foo");
		assertEquals("foo.a", env.registerVariable("a", foo).getName());
		assertTrue(env.containsVariable("foo.a", null));

		assertTrue(env.containsVariable("instdir", null));
		assertEquals("foo.instdir", env.registerVariable("instdir", foo)
				.getName());
		assertTrue(env.containsVariable("foo.instdir", null));
		assertTrue(env.containsVariable("instdir", foo));

		try {
			env.registerVariable("$a", null);
			fail();
		} catch (EnvironmentException e) {
			System.err.println(e.getMessage());
		}
		try {
			env.registerVariable("function", null);
			fail();
		} catch (EnvironmentException e) {
			System.err.println(e.getMessage());
		}
	}

	@Test
	public void testContainsVariable() throws EnvironmentException {
		Environment env = new Environment();
		assertFalse(env.containsVariable("a", null));
		assertEquals("a", env.registerVariable("a", null).getName());
		assertTrue(env.containsVariable("a", null));
		UserFunction foo = env.registerUserFunction("foo");
		assertFalse(env.containsVariable("a", foo));
	}

	@Test
	public void testGetVariable() throws EnvironmentException {
		Environment env = new Environment();
		try {
			env.getVariable("a", null);
			fail();
		} catch (EnvironmentException e) {
			System.err.println(e.getMessage());
		}
		Variable a = env.registerVariable("a", null);
		assertEquals(a, env.getVariable("a", null));

		UserFunction foo = env.registerUserFunction("foo");
		try {
			env.getVariable("a", foo);
			fail();
		} catch (EnvironmentException e) {
			System.err.println(e.getMessage());
		}
		assertEquals(env.registerVariable("a", foo), env.getVariable("a", foo));
	}

	@Test
	public void testGetCallable() throws EnvironmentException {
		Environment env = new Environment();
		try {
			env.getCallable("a");
			fail();
		} catch (EnvironmentException e) {
			System.err.println(e.getMessage());
		}
		UserFunction a = env.registerUserFunction("a");
		assertEquals(a, env.getCallable("a"));
	}

	@Test
	public void testRegisterFunction() throws EnvironmentException {
		Environment env = new Environment();
		try {
			env.registerUserFunction("global");
			fail();
		} catch (EnvironmentException e) {
			System.err.println(e.getMessage());
		}
		try {
			env.registerUserFunction("$foo");
			fail();
		} catch (EnvironmentException e) {
			System.err.println(e.getMessage());
		}
		assertEquals(".oninit", env.registerUserFunction(".onInit").getName());
	}

}
