package explorviz.visualization.engine.optional

import com.google.gwt.dom.client.Element
import explorviz.visualization.main.ClientConfiguration

public class FPSCounter {
	static var fpsCounter = 0
	static var lastTimeFpsUpdate = 0L
	static Element fpsLabel

	private new() {
	}

	def static init(Element fpsLabelParam) {
		fpsCounter = 0
		fpsLabel = fpsLabelParam
		if (fpsLabel != null) {
			if (ClientConfiguration::showFPS) {
				fpsLabel.setInnerText("FPS: " + 0)
			}
		}
		lastTimeFpsUpdate = System::currentTimeMillis()
	}

	def static countFPS() {
		if (ClientConfiguration::showFPS) {
			fpsCounter = fpsCounter + 1
			val currentTimeMillis = System::currentTimeMillis()
			if ((currentTimeMillis - lastTimeFpsUpdate) >= 1000) {
				if (fpsLabel != null) {
					fpsLabel.setInnerText("FPS: " + fpsCounter)
				}
				lastTimeFpsUpdate = currentTimeMillis
				fpsCounter = 0
			}
		}
	}
}
