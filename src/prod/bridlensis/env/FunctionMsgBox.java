package bridlensis.env;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import bridlensis.InvalidSyntaxException;
import bridlensis.Parser;
import bridlensis.StatementFactory;

class FunctionMsgBox implements Callable {

	private static final int BUTTONGROUP_INDEX = 0;
	private static final int MESSAGE_INDEX = 1;
	private static final int OPTIONS_INDEX = 2;
	private static final int SDRETURN_INDEX = 3;

	private static final String GOTO_PREFIX = "msgbox_";

	private static List<String> buttons = Arrays.asList("OK", "CANCEL",
			"ABORT", "RETRY", "IGNORE", "YES", "NO");

	protected static enum ButtonGroup {
		OK, OKCANCEL, ABORTRETRYIGNORE, RETRYCANCEL, YESNO, YESNOCANCEL
	};

	protected static class ReturnOption {

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
	public ReturnType getReturnType() {
		return ReturnType.OPTIONAL;
	}

	protected Collection<ReturnOption> returnOptions(ButtonGroup buttons)
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
		if (options != null) {
			for (String o : options.split("\\|")) {
				if (o.length() > 0) {
					sb.append("|MB_");
					sb.append(o);
				}
			}
		}
		return sb.toString();
	}

	@Override
	public String statementFor(String indent, List<TypeObject> args,
			Variable returnVar) throws InvalidSyntaxException {
		ButtonGroup buttonGroup;
		try {
			buttonGroup = ButtonGroup.valueOf(StatementFactory.deString(args
					.get(BUTTONGROUP_INDEX)));
		} catch (IllegalArgumentException e) {
			throw new InvalidSyntaxException(String.format(
					"Invalid button group argument '%s'",
					args.get(BUTTONGROUP_INDEX)));
		}
		StringBuilder sb = new StringBuilder(indent);
		sb.append("MessageBox ");
		sb.append(optionsList(buttonGroup,
				args.get(OPTIONS_INDEX).equals(StatementFactory.NULL) ? null
						: StatementFactory.deString(args.get(OPTIONS_INDEX))));
		sb.append(' ');
		sb.append(args.get(MESSAGE_INDEX).getValue());

		if (!args.get(SDRETURN_INDEX).equals(StatementFactory.NULL)) {
			String button = StatementFactory.deString(args.get(SDRETURN_INDEX));
			if (!buttons.contains(button.toUpperCase())) {
				throw new InvalidSyntaxException(
						"Unsupported MsgBox SD return " + button);
			}
			sb.append(" /SD ID");
			sb.append(button);
		}

		if (returnVar != null) {
			String exit_jump = GOTO_PREFIX + nameGenerator.generate();
			indent += StatementFactory.DEFAULT_INDENT;
			StringBuilder sbRet = new StringBuilder();
			for (ReturnOption ro : returnOptions(buttonGroup)) {
				sb.append(" ID");
				sb.append(ro.getReturnValue());
				sb.append(' ');
				sb.append(ro.getGoTo());

				sbRet.append(Parser.NEWLINE_MARKER);
				sbRet.append(indent);
				sbRet.append(ro.getGoTo());
				sbRet.append(':');
				sbRet.append(Parser.NEWLINE_MARKER);
				sbRet.append(indent);
				sbRet.append(StatementFactory.DEFAULT_INDENT);
				sbRet.append("StrCpy ");
				sbRet.append(returnVar.getValue());
				sbRet.append(" \"");
				sbRet.append(ro.getReturnValue());
				sbRet.append('"');
				sbRet.append(Parser.NEWLINE_MARKER);
				sbRet.append(indent);
				sbRet.append(StatementFactory.DEFAULT_INDENT);
				sbRet.append("GoTo ");
				sbRet.append(exit_jump);
			}
			sb.append(sbRet);
			sb.append(Parser.NEWLINE_MARKER);
			sb.append(indent);
			sb.append(exit_jump);
			sb.append(':');
		}

		return sb.toString();
	}

	@Override
	public String toString() {
		return "Function[msgbox]";
	}

}
