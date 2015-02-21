package explorviz.visualization.landscapeinformation

import java.util.Map
import java.util.Collections
import com.google.gwt.i18n.client.DefaultDateTimeFormatInfo
import com.google.gwt.i18n.client.DateTimeFormat
import java.util.Date

class EventViewer {

	private static String currentText

	def static void openDialog() {
		EventViewerJS::openDialog
		EventViewerJS::setEventText(currentText)
	}

	def static void updateEventView(Map<Long, String> events) {
		var text = "";

		val keys = events.keySet.toList

		Collections.sort(keys)
		Collections.reverse(keys)

		for (Long timestamp : keys) {
			text = text + "<b>" + convertToPrettyTime(timestamp) + "</b>:&nbsp;" + events.get(timestamp) + "<br/>"
		}
		
				val sb = new StringBuilder()

		for (Long timestamp : keys) {
			sb.append("<b>")
			sb.append(convertToPrettyTime(timestamp))
			sb.append("</b>:&nbsp;")
			sb.append(events.get(timestamp))
			sb.append("<br/>")
		}

		currentText = sb.toString

		EventViewerJS::setEventText(currentText)
	}

	def static private String convertToPrettyTime(long timeInMillis) {
		val pattern = "yyyy-MM-dd HH:mm:ss"
		val info = new DefaultDateTimeFormatInfo()
		val dtf = new DateTimeFormat(pattern, info) {
		};
		dtf.format(new Date(timeInMillis))
	}
}
