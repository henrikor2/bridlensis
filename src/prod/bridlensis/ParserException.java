package bridlensis;

public class ParserException extends Exception {

	private static final long serialVersionUID = 1L;

	private final Integer line;
	private final String filename;

	public ParserException(String fileName, Integer line, Throwable cause) {
		super(cause);
		this.line = line;
		this.filename = fileName;
	}

	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder();
		if (getCause() instanceof InvalidSyntaxException) {
			sb.append("Syntax error: ");
		}
		if (filename != null)
			sb.append(String.format("\"%s\" ", filename));
		if (line != null)
			sb.append(String.format("on line %d -- ", line));
		sb.append(getCause().getMessage());
		return sb.toString();
	}

}
