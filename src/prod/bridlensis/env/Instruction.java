package bridlensis.env;

public class Instruction extends Callable {

	private String displayName;
	private int argsCount;
	private int returnArgIndex;

	static Instruction parse(String line) {
		String[] parts = line.split(" ");
		int returnArgIndex = -1;
		for (int i = 1; i < parts.length; i++) {
			if (parts[i].equals("output")) {
				returnArgIndex = i - 1;
				break;
			}
		}
		int argsCount = returnArgIndex == -1 ? parts.length - 1
				: parts.length - 2;
		return new Instruction(parts[0].toLowerCase(), parts[0], argsCount,
				returnArgIndex);
	}

	private Instruction(String name, String displayName, int argsCount,
			int returnArgIndex) {
		super(name);
		this.displayName = displayName;
		this.argsCount = argsCount;
		this.returnArgIndex = returnArgIndex;
	}

	@Override
	public int getMandatoryArgsCount() {
		return 0;
	}

	@Override
	public int getArgsCount() {
		return argsCount;
	}

	@Override
	public boolean hasReturn() {
		return getReturnArgIndex() != -1;
	}

	public String getDisplayName() {
		return displayName;
	}

	public int getReturnArgIndex() {
		return returnArgIndex;
	}

}