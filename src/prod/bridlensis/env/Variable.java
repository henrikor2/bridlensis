package bridlensis.env;

public class Variable implements TypeObject {

	private final String name;

	Variable(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "Variable[" + name + "]";
	}

	@Override
	public Type getType() {
		return Type.VARIABLE;
	}

	@Override
	public String getValue() {
		return "$" + name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + name.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TypeObject)) {
			return false;
		}
		TypeObject other = (TypeObject) obj;
		return (other.getType().equals(getType()) && other.getValue().equals(
				getValue()));
	}
}
