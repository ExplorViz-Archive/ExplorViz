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

	public static native void fillUsers(String[] users) /*-{
		var selection = $wnd.jQuery("#users");

		var htmlString = '<table class="table table-fixed table-striped" style="width:100%;"><thead><tr><th>Username</th><th>Never logged in?</th></tr></thead><tbody>';

		var arrayLength = users.length - 1;
		for (var i = 0; i <= arrayLength; i++) {
			htmlString = htmlString + '<tr><td>' + users[i] + '</td><td>'
					+ users[i + 1] + '</td></tr>';
			i++;
		}

		htmlString += '</tbody></table>';
		selection.html(htmlString);
		//console.log(users);

	}-*/;
}
