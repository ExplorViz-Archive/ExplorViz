package explorviz.visualization.landscapeinformation

import java.util.Map
import java.util.Collections
import com.google.gwt.i18n.client.DefaultDateTimeFormatInfo
import com.google.gwt.i18n.client.DateTimeFormat
import java.util.Date

class ErrorViewer {

	private static String currentText

	def static void openDialog() {
		ErrorViewerJS::openDialog
		ErrorViewerJS::setErrorText(currentText)
	}

	def static void updateErrorView(Map<Long, String> errors) {
		val keys = errors.keySet.toList

		Collections.sort(keys)
		Collections.reverse(keys)
		
		val sb = new StringBuilder()

		for (Long timestamp : keys) {
			val value = errors.get(timestamp)
			sb.append("<b>")
			sb.append(convertToPrettyTime(timestamp))
			sb.append("</b>:&nbsp;")
			sb.append(value.replaceAll("\n","<br/>").replaceAll("\t","&nbsp;&nbsp;&nbsp;&nbsp;"))
			sb.append("<br/>")
		}

		currentText = sb.toString

		ErrorViewerJS::setErrorText(currentText)
	}

	def static private String convertToPrettyTime(long timeInMillis) {
		val pattern = "yyyy-MM-dd HH:mm:ss"
		val info = new DefaultDateTimeFormatInfo()
		val dtf = new DateTimeFormat(pattern, info) {
		};
		dtf.format(new Date(timeInMillis))
	}
}
