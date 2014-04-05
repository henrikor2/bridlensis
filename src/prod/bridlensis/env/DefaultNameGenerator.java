package bridlensis.env;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DefaultNameGenerator implements NameGenerator {

	private MessageDigest md;

	public DefaultNameGenerator() {
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String generate() {
		md.reset();
		md.update(Double.toHexString(Math.random()).getBytes());
		byte[] digest = md.digest();

		StringBuilder name = new StringBuilder(6);
		name.append('s');
		for (int i = 0; i < 5; i++) {
			int val = (digest[i] & 0xff) + 0x100;
			name.append(Integer.toString(val, 16).substring(1));
		}
		return name.toString();
	}

}
