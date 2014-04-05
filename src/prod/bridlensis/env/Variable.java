package bridlensis.env;

public class Variable {

	private final String name;

	Variable(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return name;
	}

	public String getNSISExpression() {
		return "$" + name;
	}

}