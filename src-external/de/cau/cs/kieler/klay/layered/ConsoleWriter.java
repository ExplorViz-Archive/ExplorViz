package de.cau.cs.kieler.klay.layered;

public class ConsoleWriter {

	final StringBuffer strBuffer = new StringBuffer();

	public ConsoleWriter(final String origPath) {
		console("original file: " + origPath);
	}

	public void write(final String str) {
		strBuffer.append(str);
	}

	public static native void console(String text) /*-{
		console.log(text);
	}-*/;

	public void close() {
		console(strBuffer.toString());
	}
}
