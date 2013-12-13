package explorviz.visualization.view

import explorviz.visualization.main.Configuration
import java.util.List
import java.util.ArrayList
import explorviz.visualization.main.PageControl
import explorviz.visualization.engine.navigation.Navigation

class ConfigurationPage implements IPage {
	override render(PageControl pageControl) {
	    Navigation::deregisterWebGLKeys()
	    
		pageControl.setView('''<table>
			<th>Name</th><th>Value</th>
			<tr><td>Show FPS</td><td>«createBooleanCombobox(Configuration::showFPS)»</td>
		 </table>'''.toString())
	}

	def private createBooleanCombobox(boolean selectedValue) {
		val possibilities = new ArrayList<String>
		possibilities.add("true")
		possibilities.add("false")
		createCombobox(possibilities, "width: 100px;", if (selectedValue) 0 else 1 )
	}

	def private createCombobox(List<String> possibilities, String style, int selectedIndex) {
		'''<select style="«style»" onchange="alert(this.value)">
		«FOR i : 0 .. possibilities.size-1»
			<option « if (i == selectedIndex) "selected" » value="«possibilities.get(i)»">«possibilities.get(i).toFirstUpper»</option>
		«ENDFOR»
		</select>'''
	}
}