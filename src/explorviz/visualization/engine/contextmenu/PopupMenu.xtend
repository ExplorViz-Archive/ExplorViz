package explorviz.visualization.engine.contextmenu

import com.google.gwt.safehtml.shared.SafeHtmlUtils
import com.google.gwt.user.client.Command
import com.google.gwt.user.client.ui.MenuBar
import com.google.gwt.user.client.ui.MenuItem
import com.google.gwt.user.client.ui.MenuItemSeparator
import com.google.gwt.user.client.ui.PopupPanel
import explorviz.visualization.engine.contextmenu.commands.DummyCommand
import explorviz.visualization.engine.main.WebGLStart
import java.util.ArrayList
import java.util.List

class PopupMenu {
	static val String SINGLE_INDENTION = "&nbsp;"
	static val String INDENTION = "&nbsp;&nbsp;"

	val List<MenuItem> entries = new ArrayList<MenuItem>()

	val popupPanel = new PopupPanel(true);
	val popupMenuBar = new MenuBar(true);
	var MenuItem titleMenu

	new() {
		init()
	}

	protected def init() {
		popupPanel.setStyleName("popup");

		titleMenu = new MenuItem("<div style='font-weight:bold;'>" + INDENTION + "Title" + "</div>", true,
			new DummyCommand());
		titleMenu.addStyleName("popup-item");
		popupMenuBar.addItem(titleMenu)

		addSeperator()

		popupMenuBar.setVisible(true);
		popupPanel.add(popupMenuBar);
	}

	def void addNewEntry(String label, Command command) {
		val entry = new MenuItem(INDENTION + INDENTION + INDENTION + label, true, command)
		entry.addStyleName("popup-item")
		entries.add(entry)
		popupMenuBar.addItem(entry)
	}

	def void addSeperator() {
		val seperator = new MenuItemSeparator()
		seperator.addStyleName("popup-item");
		popupMenuBar.addSeparator(seperator);
	}

	def void clear() {
		popupMenuBar.clearItems
		entries.clear
		popupPanel.clear
		init()
	}

	def void show(int x, int y, String titleName) {
		titleMenu.setHTML(
			"<div style='font-weight:bold;'>" + INDENTION + SafeHtmlUtils::htmlEscape(titleName) + "</div>")
		titleMenu.setEnabled(false)
		popupPanel.setPopupPosition(x, y + WebGLStart::navigationHeight + 2)
		popupPanel.show()
	}

	def void hide() {
		popupPanel.hide()
	}

	def void setEntryChecked(String label, boolean shouldBeChecked) {
		for (entry : entries) {
			if (entry.HTML.contains(label)) {
				if (shouldBeChecked) {
					entry.HTML = SINGLE_INDENTION +
						"<span class='glyphicon glyphicon-ok' style='width:14px;height:16px;'></span>" +
						SINGLE_INDENTION + label
				} else {
					entry.HTML = INDENTION + INDENTION + INDENTION + label
				}
			}
		}
	}
}
