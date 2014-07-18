package explorviz.server.login;

import java.util.logging.Logger;

import org.apache.shiro.authc.*;
import org.apache.shiro.realm.jdbc.JdbcRealm;

import explorviz.server.database.DBConnection;
import explorviz.shared.auth.User;

public class MyRealm extends JdbcRealm {
	private static final Logger log = Logger.getLogger("MyRealm");

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken token)
			throws AuthenticationException {
		final UsernamePasswordToken userPassToken = (UsernamePasswordToken) token;
		final String username = userPassToken.getUsername();

		if (username == null) {
			log.info("Username is null.");
			return null;
		}

		final PasswdSalt passwdSalt = getPasswordForUser(username);

		if (passwdSalt == null) {
			log.info("No account found for user [" + username + "]");
			return null;
		}

		final SaltedAuthenticationInfo info = new MySaltedAuthentificationInfo(username,
				passwdSalt.password, passwdSalt.salt);

		return info;
	}

	private PasswdSalt getPasswordForUser(final String username) {
		final User user = getUserByName(username);
		if (user == null) {
			return null;
		}
		return new PasswdSalt(user.getHashedPassword(), user.getSalt());
	}

	private User getUserByName(final String username) {
		return DBConnection.getUserByName(username);
	}

	class PasswdSalt {
		public String password;
		public String salt;

		public PasswdSalt(final String password, final String salt) {
			super();
			this.password = password;
			this.salt = salt;
		}
	}

}