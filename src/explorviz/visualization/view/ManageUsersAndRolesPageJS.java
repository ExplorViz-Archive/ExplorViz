package explorviz.visualization.view;

public class ManageUsersAndRolesPageJS {
	public static native void init() /*-{
		$wnd
				.jQuery("#addUser")
				.on(
						"click touchstart",
						function() {
							var result = $wnd.jQuery("#addUserForm")
									.serialize();
							@explorviz.visualization.view.ManageUsersAndRolesPage::addUserForm(Ljava/lang/String;)(result);
						});
	}-*/;

	public static native void destroy() /*-{
		$wnd.jQuery("#addUser").off("click touchstart");
	}-*/;
}
