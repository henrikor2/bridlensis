package bridlensis.env;

public class FunctionRename extends Callable {

	public static final int SOURCE_INDEX = 0;
	public static final int TARGET_INDEX = 1;
	public static final int OPTIONS_INDEX = 2;

	FunctionRename() {
		super("rename");
	}

	@Override
	public int getMandatoryArgsCount() {
		return 2;
	}

	@Override
	public int getArgsCount() {
		return 3;
	}

	@Override
	public boolean hasReturn() {
		return true;
	}

}
