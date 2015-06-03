package explorviz.visualization.databasequeries

import explorviz.shared.model.Application
import explorviz.visualization.landscapeexchange.LandscapeExchangeManager
import com.google.gwt.safehtml.shared.SafeHtmlUtils
import explorviz.visualization.main.AlertDialogJS

class DatabaseQueries {
	def static void open(Application app) {
		if (app.databaseQueries.size == 0) {
			AlertDialogJS::showAlertDialog("No Queries Available",
					"Sorry, no database queries are available.")
			return
		}

		if (!LandscapeExchangeManager::isStopped()) {
			LandscapeExchangeManager::stopAutomaticExchange(System::currentTimeMillis().toString())
		}

		var tableContent = "<thead><tr><th style='text-align: center !important;'>SQL Statement</th><th style='text-align: center !important;'>Return Value</th><th style='text-align: center !important;'>Duration In Millis</th></tr></thead><tbody>"

		for (query : app.databaseQueries) {
			if (!query.SQLStatement.empty)
				tableContent = tableContent + "<tr><td>" + SafeHtmlUtils.htmlEscape(query.SQLStatement) + "</td><td>" + SafeHtmlUtils.htmlEscape(query.returnValue) + "</td><td>" + convertToMilliSecondTime(query.timeInNanos) + "</td></tr>"
		}

		DatabaseQueriesJS.openDialog(tableContent + "</tbody>")
	}


	private def static String convertToMilliSecondTime(float x) {
		val result = (x / (1000 * 1000)).toString()

		result.substring(0, Math.min(result.indexOf('.') + 3, result.length - 1))
	}
}
