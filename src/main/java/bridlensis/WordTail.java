package bridlensis;

class WordTail {

	private String pattern;

	WordTail(String pattern) {
		this.pattern = pattern;
	}

	protected String getPattern() {
		return pattern;
	}

	@Override
	public String toString() {
		return "Tail[" + pattern + "]";
	}

	public boolean isCompilerCommand() {
		return pattern.equals("!");
	}

	public boolean isAssignment() {
		return pattern.equals("=");
	}

	public boolean isFunctionArgsOpen() {
		return pattern.startsWith("(");
	}

	public boolean isFunctionArgSeparator() {
		return pattern.contains(",");
	}

	public boolean isFunctionArgsClose() {
		return pattern.contains(")");
	}

	public boolean isConcatenation() {
		return pattern.endsWith("+");
	}

	public boolean isComparison() {
		return pattern.matches(".*(==|!=|[<>]).*");
	}

	public String getComparison() {
		return pattern.replaceAll("[^=!\\<\\>]", "");
	}

	public boolean isEmpty() {
		return pattern.isEmpty();
	}

	public void removeFunctionArgsClose() {
		pattern = pattern.replaceFirst("\\)", "");
	}

}