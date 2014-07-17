package bridlensis;

public class BridleNSISException extends Exception {

	private static final long serialVersionUID = 1L;

	private final int errorCode;

	public BridleNSISException(int errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public BridleNSISException(int errorCode, Exception e) {
		super(e);
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}

}
