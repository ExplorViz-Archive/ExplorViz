package explorviz.visualization.engine.popover

class PopoverService {
	static var boolean showing = false
	
	def static showPopover(String title, int absoluteX, int absoluteY,
			String htmlContent) {
		hidePopover()
		PopoverJS.initPopover(title, absoluteX + 10, absoluteY + 75, htmlContent)
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