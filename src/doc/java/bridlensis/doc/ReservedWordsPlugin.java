package bridlensis.doc;

import java.util.List;
import java.util.Map;

import org.markdown4j.Plugin;

import bridlensis.env.Environment;

public class ReservedWordsPlugin extends Plugin {

	public ReservedWordsPlugin() {
		super("reservedwords");
	}

	@Override
	public void emit(StringBuilder out, List<String> lines,
			Map<String, String> params) {
		out.append("<pre><code>");
		for (String word : Environment.getReservedWords()) {
			out.append(word);
			out.append(' ');
		}
		out.append("\r\n</code></pre>");
	}

}
