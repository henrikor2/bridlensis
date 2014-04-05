package bridlensis.env;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import bridlensis.InvalidSyntaxException;
import bridlensis.StatementFactory;

public class FunctionMsgBox extends Callable {

	public static final int BUTTONGROUP_INDEX = 0;
	public static final int MESSAGE_INDEX = 1;
	public static final int OPTIONS_INDEX = 2;
	public static final int SDRETURN_INDEX = 3;

	private static final String GOTO_PREFIX = "msgbox_";

	private static List<String> buttons = Arrays.asList("OK", "CANCEL",
			"ABORT", "RETRY", "IGNORE", "YES", "NO");

	public static enum ButtonGroup {
		OK, OKCANCEL, ABORTRETRYIGNORE, RETRYCANCEL, YESNO, YESNOCANCEL
	};

	public static class ReturnOption {

		private final String returnValue;
		private final String goTo;

		public ReturnOption(String returnValue, String goToId) {
			this.returnValue = returnValue;
			this.goTo = GOTO_PREFIX + goToId;
		}

		public String getReturnValue() {
			return returnValue;
		}

		public String getGoTo() {
			return goTo;
		}

	}

	private NameGenerator nameGenerator;

	FunctionMsgBox(NameGenerator nameGenerator) {
		super("msgbox");
		this.nameGenerator = nameGenerator;
	}

	@Override
	public int getMandatoryArgsCount() {
		return 2;
	}

	@Override
	public int getArgsCount() {
		return 4;
	}

	@Override
	public boolean hasReturn() {
		return true;
	}

	public String createExitGoTo() {
		return GOTO_PREFIX + nameGenerator.generate();
	}

	public Collection<ReturnOption> returnOptions(ButtonGroup buttons)
			throws InvalidSyntaxException {
		switch (buttons) {
		case OK:
			return Arrays.asList(new ReturnOption("OK", nameGenerator
					.generate()));
		case OKCANCEL:
			return Arrays.asList(
					new ReturnOption("OK", nameGenerator.generate()),
					new ReturnOption("CANCEL", nameGenerator.generate()));
		case YESNO:
			return Arrays.asList(
					new ReturnOption("YES", nameGenerator.generate()),
					new ReturnOption("NO", nameGenerator.generate()));
		case YESNOCANCEL:
			return Arrays.asList(
					new ReturnOption("YES", nameGenerator.generate()),
					new ReturnOption("NO", nameGenerator.generate()),
					new ReturnOption("CANCEL", nameGenerator.generate()));
		case RETRYCANCEL:
			return Arrays.asList(
					new ReturnOption("RETRY", nameGenerator.generate()),
					new ReturnOption("CANCEL", nameGenerator.generate()));
		case ABORTRETRYIGNORE:
			return Arrays.asList(
					new ReturnOption("ABORT", nameGenerator.generate()),
					new ReturnOption("RETRY", nameGenerator.generate()),
					new ReturnOption("IGNORE", nameGenerator.generate()));
		}
		throw new InvalidSyntaxException(String.format(
				"Unsupported buttons definition '%s'", buttons));
	}

	public static String optionsList(ButtonGroup buttons, String options) {
		StringBuilder sb = new StringBuilder();
		sb.append("MB_");
		sb.append(buttons.toString());
		if (!options.equals(StatementFactory.NULL)) {
			for (String o : options.split("\\|")) {
				if (o.length() > 0) {
					sb.append("|MB_");
					sb.append(o);
				}
			}
		}
		return sb.toString();
	}

	public static boolean containsButton(String button) {
		return buttons.contains(button.toUpperCase());
	}

}
