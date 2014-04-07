package bridlensis.env;

import java.util.List;

import bridlensis.InvalidSyntaxException;

public interface Callable {

	public static enum ReturnType {
		VOID, OPTIONAL, REQUIRED, ERRORFLAG
	}

	public abstract int getMandatoryArgsCount();

	public abstract int getArgsCount();

	public abstract ReturnType getReturnType();

	public abstract String statementFor(String indent, List<String> args,
			Variable returnVar) throws InvalidSyntaxException;

}