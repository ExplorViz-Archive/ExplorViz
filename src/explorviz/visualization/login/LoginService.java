package explorviz.visualization.login;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("LoginService")
public interface LoginService extends RemoteService {
	public Boolean isLoggedIn();

	public Boolean tryLogin(String username, String password, Boolean rememberMe);

	public void logout();

	public String getCurrentUsername();

	public void register(String username, String password);
}
