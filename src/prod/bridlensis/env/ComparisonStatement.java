package bridlensis.env;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import bridlensis.env.TypeObject.Type;

public class ComparisonStatement {

	private String key;
	private boolean not;
	private List<TypeObject> left;
	private String compare;
	private List<TypeObject> right;

	public ComparisonStatement(String key) {
		this.key = key;
		this.not = false;
		this.left = new ArrayList<>();
		this.compare = null;
		this.right = new ArrayList<>();
	}

	public String getKey() {
		return key;
	}

	public boolean isNot() {
		return not;
	}

	public Collection<TypeObject> getLeft() {
		return left;
	}

	public String getCompare() {
		return compare;
	}

	public Collection<TypeObject> getRight() {
		return right;
	}

	public void setNot(boolean value) {
		not = value;
	}

	public void addLeft(TypeObject expr) {
		left.add(expr);
	}

	public void setCompare(String value) {
		compare = value;
	}

	public void addRight(TypeObject expr) {
		right.add(expr);
	}

	public Type getLastLeftType() {
		return left.get(left.size() - 1).getType();
	}

	@Override
	public String toString() {
		return "ComparisonStatement[key=" + key + ", not=" + not + ", left="
				+ left + ", compare=" + compare + ", right=" + right + "]";
	}

}
