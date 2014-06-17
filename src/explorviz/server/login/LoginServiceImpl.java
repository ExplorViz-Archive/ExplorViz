package explorviz.server.login;

import java.sql.SQLException;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import explorviz.server.database.DBConnection;
import explorviz.visualization.engine.Logging;
import explorviz.visualization.login.LoginService;

public class LoginServiceImpl extends RemoteServiceServlet implements LoginService {

	private static final long serialVersionUID = 4691982194784805198L;

	static {
		final Factory<org.apache.shiro.mgt.SecurityManager> factory = new IniSecurityManagerFactory();
		final org.apache.shiro.mgt.SecurityManager securityManager = factory.getInstance();
		SecurityUtils.setSecurityManager(securityManager);

		try {
			DBConnection.connect();
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

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
	public Boolean tryLogin(final String username, final String password, final Boolean rememberMe) {
		final Subject currentUser = SecurityUtils.getSubject();

		if (!currentUser.isAuthenticated()) {
			final UsernamePasswordToken token = new UsernamePasswordToken(username, password);
			token.setRememberMe(rememberMe);

			try {
				currentUser.login(token);
				Logging.log("User [" + currentUser.getPrincipal().toString()
						+ "] logged in successfully.");
				return true;
			} catch (final UnknownAccountException uae) {
				Logging.log("There is no user with username of " + token.getPrincipal());
			} catch (final IncorrectCredentialsException ice) {
				Logging.log("Password for account " + token.getPrincipal() + " was incorrect!");
			} catch (final LockedAccountException lae) {
				Logging.log("The account for username " + token.getPrincipal() + " is locked.  "
						+ "Please contact your administrator to unlock it.");
			} catch (final AuthenticationException ae) {
				Logging.log(ae.getLocalizedMessage());
			}
		}

		return false;
	}

	@Override
	public void logout() {
		final Subject currentUser = SecurityUtils.getSubject();
		final User user = DBConnection.getUserByName(getCurrentUsername());
		user.setFirstLogin(false);
		DBConnection.updateUser(user);
		currentUser.logout();
	}

	@Override
	public void register(final String username, final String password) {
		final User user = generateUser(username, password);
		DBConnection.createUser(user);
	}

	public static User generateUser(final String username, final String plainTextPassword) {
		final RandomNumberGenerator rng = new SecureRandomNumberGenerator();
		final Object salt = rng.nextBytes();

		final String hashedPasswordBase64 = new Sha256Hash(plainTextPassword, salt, 1024)
				.toBase64();

		return new User(username, hashedPasswordBase64, salt.toString(), true);
	}

	@Override
	public String getCurrentUsername() {
		final Subject currentUser = SecurityUtils.getSubject();

		if ((currentUser == null) || (currentUser.getPrincipal() == null)) {
			return "";
		}

		return currentUser.getPrincipal().toString();
	}
}