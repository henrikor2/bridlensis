package bridlensis.env;

public class DefaultNameGenerator implements NameGenerator {

	private char prefix;
	private short count;

	public DefaultNameGenerator() {
		prefix = 'b';
		count = 1;
	}

	@Override
	public String generate() {
		if (count == Short.MAX_VALUE) {
			prefix++;
			count = 1;
		}
		StringBuilder name = new StringBuilder(6);
		name.append(prefix);
		name.append(String.format("%05d", count++));
		return name.toString();
	}

}
