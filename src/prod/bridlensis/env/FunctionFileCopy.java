package bridlensis.env;

public class FunctionFileCopy extends Callable {

	public static final int SOURCE_INDEX = 0;
	public static final int TARGET_INDEX = 1;

	FunctionFileCopy() {
		super("filecopy");
	}

	@Override
	public int getMandatoryArgsCount() {
		return 2;
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
