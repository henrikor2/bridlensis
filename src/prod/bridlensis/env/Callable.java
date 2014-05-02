package bridlensis.env;

import java.util.List;

import bridlensis.InvalidSyntaxException;

public interface Callable {

	public static enum ReturnType {
		VOID, OPTIONAL, REQUIRED, ERRORFLAG
	}

	int getMandatoryArgsCount();

	int getArgsCount();

	ReturnType getReturnType();

	String statementFor(String indent, List<TypeObject> args, Variable returnVar)
			throws InvalidSyntaxException;

}