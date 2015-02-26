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
		var text = "";

		val keys = errors.keySet.toList

		Collections.sort(keys)
		Collections.reverse(keys)

		for (Long timestamp : keys) {
			val value = errors.get(timestamp)
			text = text + "<b>" + convertToPrettyTime(timestamp) + "</b>:&nbsp;" + value.replaceAll("\n","<br/>").replaceAll("\t","&nbsp;&nbsp;&nbsp;&nbsp;") + "<br/>"
		}

		currentText = text

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
