package explorviz.visualization.login;

import com.google.gwt.user.client.rpc.AsyncCallback;

import explorviz.shared.auth.User;

public interface LoginServiceAsync {

	void isLoggedIn(AsyncCallback<Boolean> callback);

	void logout(AsyncCallback<Void> callback);

	void register(String username, String password, AsyncCallback<Void> callback);

	void getCurrentUser(AsyncCallback<User> callback);

	void tryLogin(String username, String password, Boolean rememberMe,
			AsyncCallback<Boolean> callback);

}
