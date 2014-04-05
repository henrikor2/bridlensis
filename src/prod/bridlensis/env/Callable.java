package bridlensis.env;

public abstract class Callable {

	private final String name;

	Callable(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public abstract int getMandatoryArgsCount();

	public abstract int getArgsCount();

	public abstract boolean hasReturn();

	@Override
	public String toString() {
		return name;
	}

}