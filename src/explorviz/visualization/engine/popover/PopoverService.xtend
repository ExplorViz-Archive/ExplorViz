package explorviz.visualization.engine.popover

import explorviz.visualization.engine.main.WebGLStart

class PopoverService {
	static var boolean showing = false
	
	def static showPopover(String title, int absoluteX, int absoluteY,
			String htmlContent) {
		hidePopover()
		PopoverJS.initPopover(title, absoluteX, absoluteY + WebGLStart::navigationHeight - 7, htmlContent)
		PopoverJS.showPopover()
		showing = true
	}
	
	def static hidePopover() {
		if (showing) {
			PopoverJS.destroyPopover()
			showing = false
		}
	}
}