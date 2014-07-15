package explorviz.visualization.engine.main;

import java.util.ArrayList;
import java.util.List;

public class ClassnameSplitter {
	private static final int MAX_EVEN_CHAR_DISTRIBUTION = 8;

	static List<String> splitClassname(final String classname, final int minimumCharacters,
			final int maxLines) {
		final List<String> result = new ArrayList<String>();

		if (classname.length() < minimumCharacters) {
			result.add(classname);
			return result;
		}

		final List<String> words = splitAtUppercaseAndDollar(classname);
		if (atLeastTwoWords(words)) {
			doWordDistribution(classname, maxLines, result, words);
		} else {
			doHyphenation(classname, maxLines, result);
		}

		return result;
	}

	private static List<String> splitAtUppercaseAndDollar(final String name) {
		final List<String> result = new ArrayList<String>();

		final char[] charArray = name.toCharArray();
		final char[] buffer = new char[charArray.length];
		int bufferIndex = 0;
		for (final char c : charArray) {
			final int charInt = c;
			if (bufferIndex > 0) {
				if ((65 <= charInt) && (charInt <= 90)) { // A - Z
					result.add(String.valueOf(buffer, 0, bufferIndex));
					bufferIndex = 0;
					buffer[bufferIndex++] = c;
				} else if (charInt == 36) { // $
					buffer[bufferIndex++] = c;
					result.add(String.valueOf(buffer, 0, bufferIndex));
					bufferIndex = 0;
				} else {
					buffer[bufferIndex++] = c;
				}
			} else {
				buffer[bufferIndex++] = c;
			}
		}

		if (bufferIndex > 0) {
			result.add(String.valueOf(buffer, 0, bufferIndex));
		}

		return result;
	}

	private static boolean atLeastTwoWords(final List<String> words) {
		return words.size() > 1;
	}

	private static boolean areMaxLinesReached(final int maxLines, final List<String> result) {
		return (result.size() + 1) >= maxLines;
	}

	private static void doWordDistribution(final String classname, final int maxLines,
			final List<String> result, final List<String> words) {
		String buffer = "";
		final int charLengthBalance = Math.round(classname.length() / (float) maxLines);
		for (final String word : words) {
			if (((buffer.length() + word.length()) <= charLengthBalance)
					|| areMaxLinesReached(maxLines, result)) {
				buffer += word;
			} else {
				result.add(buffer);
				buffer = word;
			}
		}
		if (!buffer.isEmpty()) {
			result.add(buffer);
		}

		// check for distribution too uneven)
		if (maxLines == 2) {
			final String firstResult = result.get(0);
			final String secondResult = result.get(1);
			if (Math.abs(firstResult.length() - secondResult.length()) > MAX_EVEN_CHAR_DISTRIBUTION) {
				result.clear();
				doHyphenation(classname, maxLines, result);
			}
		}
	}

	protected static void doHyphenation(final String oneWord, final int maxLines,
			final List<String> result) {
		// TODO try to split before vowel
		final int charLengthBalance = Math.round(oneWord.length() / (float) maxLines);

		// just split
		for (int i = 0; i < maxLines; i++) {
			result.add(oneWord.substring(charLengthBalance * i,
					Math.min(charLengthBalance * (i + 1), oneWord.length())));
		}
	}
}
