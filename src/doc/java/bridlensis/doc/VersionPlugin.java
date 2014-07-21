package bridlensis.doc;

import java.util.List;
import java.util.Map;

import org.markdown4j.Plugin;

import bridlensis.MakeBridleNSIS;

public class VersionPlugin extends Plugin {

	public VersionPlugin() {
		super("version");
	}

	@Override
	public void emit(StringBuilder out, List<String> lines,
			Map<String, String> params) {
		out.append("<p>Version ");
		out.append(MakeBridleNSIS.VERSION);
		out.append("</p>\r\n");
	}

}
