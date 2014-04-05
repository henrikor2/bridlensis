package bridlensis.env;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UserFunction extends Callable {

	private List<Variable> args = new ArrayList<>();
	private boolean hasReturn = false;

	UserFunction(String name) {
		super(name);
	}

	public void addArgument(Variable arg) {
		args.add(arg);
	}

	@Override
	public int getMandatoryArgsCount() {
		// All arguments are mandatory
		return args.size();
	}

	@Override
	public int getArgsCount() {
		return args.size();
	}

	@Override
	public boolean hasReturn() {
		return hasReturn;
	}

	public void setHasReturn(boolean hasReturn) {
		this.hasReturn = hasReturn;
	}

	public Iterator<Variable> argumentsIterator() {
		return args.iterator();
	}

}