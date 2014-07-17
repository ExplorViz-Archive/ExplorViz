package explorviz.visualization.engine.popover;

public class PopoverJS {
	protected static native void initPopover(String title, int absoluteX, int absoluteY,
			String htmlContent) /*-{
		$wnd.jQuery("#genericPopover").show();
		$wnd.jQuery("#genericPopover").css('left', absoluteX + 'px');
		$wnd.jQuery("#genericPopover").css('top', absoluteY + 'px');
		$wnd.jQuery("#genericPopover").popover({
			title : '<div style="font-weight:bold;">' + title + '</div>',
			placement : 'auto top',
			content : htmlContent,
			trigger : 'manual',
			viewport : '#view',
			html : true,
			animation : true,
		});
	}-*/;

	protected static native void showPopover() /*-{
		$wnd.jQuery("#genericPopover").popover('show');
	}-*/;

	protected static native void destroyPopover() /*-{
		$wnd.jQuery("#genericPopover").popover('destroy');
	}-*/;
}
