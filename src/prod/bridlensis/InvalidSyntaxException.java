package bridlensis;

public class InvalidSyntaxException extends Exception {

	private static final long serialVersionUID = 1L;

	public InvalidSyntaxException(String message) {
		this(message, null);
	}

	public InvalidSyntaxException(String message, Throwable cause) {
		super(message, cause);
	}

}
