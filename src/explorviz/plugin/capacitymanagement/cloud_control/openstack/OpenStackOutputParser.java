package explorviz.plugin.capacitymanagement.cloud_control.openstack;

import java.util.HashMap;
import java.util.regex.Pattern;

import explorviz.plugin.capacitymanagement.cloud_control.common.StringToMapParser;

public class OpenStackOutputParser extends StringToMapParser {

	public OpenStackOutputParser() {
		super("\\|", new HashMap<String, String>());
	}

	@Override
	public void parseAndAddString(final String str, final int keyPos, final int valuePos) {
		if (Pattern.matches("[\\|[^\\|]*]{2,}\\|", str)) {
			super.parseAndAddString(str, keyPos, valuePos);
		}
	}
}
