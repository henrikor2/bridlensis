package bridlensis.env;

import bridlensis.env.NameGenerator;

public class SimpleNameGenerator implements NameGenerator {

	private int step = 1;

	@Override
	public String generate() {
		StringBuilder name = new StringBuilder(3);
		name.append('s');
		if (step < 10) {
			name.append('0');
		}
		name.append(step++);
		return name.toString();
	}

}
