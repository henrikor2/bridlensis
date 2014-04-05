package bridlensis.env;

import java.util.ArrayList;
import java.util.Collection;

public class ComparisonStatement {

	private String key;
	private boolean not;
	private Collection<String> left;
	private String compare;
	private Collection<String> right;

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

	public Collection<String> getLeft() {
		return left;
	}

	public String getCompare() {
		return compare;
	}

	public Collection<String> getRight() {
		return right;
	}

	public void setNot(boolean value) {
		not = value;
	}

	public void addLeft(String expr) {
		left.add(expr);
	}

	public void setCompare(String value) {
		compare = value;
	}

	public void addRight(String expr) {
		right.add(expr);
	}

}
