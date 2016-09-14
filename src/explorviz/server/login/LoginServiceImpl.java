package explorviz.server.login;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import explorviz.server.database.DBConnection;
import explorviz.shared.auth.User;
import explorviz.visualization.login.LoginService;

public class LoginServiceImpl extends RemoteServiceServlet implements LoginService {

	private static final long serialVersionUID = 4691982194784805198L;

	@Override
	public Boolean isLoggedIn() {
		final Subject currentUser = SecurityUtils.getSubject();

		if (currentUser.isAuthenticated()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void logout() {
		final Subject currentUser = SecurityUtils.getSubject();
		final User user = DBConnection.getUserByName(getCurrentUsernameStatic());
		user.setFirstLogin(false);
		DBConnection.updateUser(user);
		currentUser.logout();
	}

	@Override
	public User getCurrentUser() {
		return DBConnection.getUserByName(getCurrentUsernameStatic());
	}

	@Override
	public void setFinishedExperimentState(final boolean finishedState) {
		final User user = DBConnection.getUserByName(getCurrentUsernameStatic());
		user.setExperimentFinished(finishedState);
		DBConnection.updateUser(user);
	}

	public static String getCurrentUsernameStatic() {
		final Subject currentUser = SecurityUtils.getSubject();

		if ((currentUser == null) || (currentUser.getPrincipal() == null)) {
			return "";
		}

		return currentUser.getPrincipal().toString();
	}
}