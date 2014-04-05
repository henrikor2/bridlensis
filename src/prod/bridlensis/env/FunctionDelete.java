package bridlensis.env;

public class FunctionDelete extends Callable {

	public static final int FILE_INDEX = 0;
	public static final int REBOOTOK_INDEX = 1;

	FunctionDelete() {
		super("delete");
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
