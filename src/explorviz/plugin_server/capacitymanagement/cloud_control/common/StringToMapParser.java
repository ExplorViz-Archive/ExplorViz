package explorviz.plugin_server.capacitymanagement.cloud_control.common;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author dj, jgi Maps a string. Help class for terminal
 */
public class StringToMapParser {

	private final String splitRegEx;
	private final HashMap<String, String> map;

	public StringToMapParser(final String splitRegEx, final HashMap<String, String> map) {
		this.splitRegEx = splitRegEx;
		this.map = map;
	}

	public HashMap<String, String> getMap() {
		return map;
	}

	public void parseAndAddString(final String str, final int keyPos, final int valuePos) {

		final String[] v = splitAndTrim(str);

		if ((keyPos < v.length) && (valuePos < v.length)) {
			map.put(v[keyPos], v[valuePos]);
		}
	}

	public void parseAndAddStringList(final List<String> list) {
		this.parseAndAddStringList(list, 0, 1);
	}

	public void parseAndAddStringList(final List<String> list, final int keyPos, final int valuePos) {
		for (final String s : list) {
			parseAndAddString(s, keyPos, valuePos);
		}
	}

	private String[] splitAndTrim(String str) {
		final String head = str.substring(0, splitRegEx.length() - 1);
		if (Pattern.matches(splitRegEx, head)) {
			str = str.substring(head.length(), str.length() - 1);
		}

		final String[] v = str.split(splitRegEx);

		for (int i = 0; i < v.length; i++) {
			v[i] = v[i].trim();
		}
		return v;
	}
}
