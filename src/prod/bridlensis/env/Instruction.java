package bridlensis.env;

import java.util.ArrayList;
import java.util.List;

import bridlensis.NSISStatements;

class Instruction implements Callable {

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
		return new Instruction(parts[0], argsCount, returnArgIndex);
	}

	private Instruction(String displayName, int argsCount, int returnArgIndex) {
		this.displayName = displayName;
		this.argsCount = argsCount;
		this.returnArgIndex = returnArgIndex;
	}

	public String getDisplayName() {
		return displayName;
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
	public ReturnType getReturnType() {
		return returnArgIndex == -1 ? ReturnType.VOID : ReturnType.REQUIRED;
	}

	@Override
	public String statementFor(String indent, List<TypeObject> args,
			Variable returnVar) {
		StringBuilder sb = new StringBuilder(indent);
		ArrayList<TypeObject> cArgs = new ArrayList<>(args);
		if (getReturnType() != ReturnType.VOID) {
			cArgs.add(returnArgIndex, returnVar);
		}
		sb.append(displayName);
		sb.append(' ');
		for (TypeObject cArg : cArgs) {
			if (!cArg.equals(NSISStatements.NULL)) {
				sb.append(cArg.getValue());
				sb.append(' ');
			}
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return "Instruction[" + displayName + "]";
	}

}