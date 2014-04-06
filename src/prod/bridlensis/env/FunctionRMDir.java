package bridlensis.env;

public class FunctionRMDir extends Callable {

	public static final int DIR_INDEX = 0;
	public static final int OPTIONS_INDEX = 1;

	public FunctionRMDir() {
		super("rmdir");
	}

	@Override
	public int getMandatoryArgsCount() {
		return 1;
	}

	@Override
	public int getArgsCount() {
		return 2;
	}

	@Override
	public boolean hasReturn() {
		return true;
	}

}
