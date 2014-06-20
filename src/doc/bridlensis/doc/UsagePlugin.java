package bridlensis.doc;

import java.util.List;
import java.util.Map;

import org.markdown4j.Plugin;

import bridlensis.MakeBridleNSIS;

public class UsagePlugin extends Plugin {

	public UsagePlugin() {
		super("usage");
	}

	@Override
	public void emit(StringBuilder out, List<String> lines,
			Map<String, String> params) {
		out.append("<pre><code>");
		out.append(encodeHTML(MakeBridleNSIS.usage()));
		out.append("</code></pre>\r\n");
	}

	private String encodeHTML(String text) {
		StringBuilder sb = new StringBuilder();
		for (char c : text.toCharArray()) {
			switch (c) {
			case '<':
				sb.append("&lt;");
				break;
			case '>':
				sb.append("&gt;");
				break;
			default:
				sb.append(c);
			}
		}
		return sb.toString();
	}

}
