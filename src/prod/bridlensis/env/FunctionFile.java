package bridlensis.env;

public class FunctionFile extends Callable {

	public static final int FILE_INDEX = 0;
	public static final int OPTIONS_INDEX = 1;
	public static final int OUTPATH_INDEX = 2;

	public FunctionFile() {
		super("file");
	}

	@Override
	public int getMandatoryArgsCount() {
		return 1;
	}

	@Override
	public int getArgsCount() {
		return 3;
	}

	@Override
	public boolean hasReturn() {
		return false;
	}

}
