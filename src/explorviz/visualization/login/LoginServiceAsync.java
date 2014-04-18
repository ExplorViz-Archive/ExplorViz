package explorviz.visualization.login;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface LoginServiceAsync {

	void isLoggedIn(AsyncCallback<Boolean> callback);

	void logout(AsyncCallback<Void> callback);

	void register(String username, String password, AsyncCallback<Void> callback);

	void getCurrentUsername(AsyncCallback<String> callback);

	void tryLogin(String username, String password, Boolean rememberMe,
			AsyncCallback<Boolean> callback);

}
