package explorviz.server.login;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;

import explorviz.server.database.DBConnection;
import explorviz.shared.auth.User;

public class LoginServlet extends HttpServlet {

	private static final long serialVersionUID = -7592398085199959557L;

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
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		final Subject currentUser = SecurityUtils.getSubject();
		final String username = req.getParameter("username");
		final String password = req.getParameter("password");
		final boolean rememberMe = Boolean.getBoolean(req.getParameter("rememberMe"));

		if (!currentUser.isAuthenticated()) {
			final UsernamePasswordToken token = new UsernamePasswordToken(username, password);
			token.setRememberMe(rememberMe);
			try {
				currentUser.login(token);
				if (username.startsWith(DBConnection.USER_PREFIX)) {
					System.out.println("User: " + username + " has logged in at "
							+ System.currentTimeMillis());
					final String lastChar = username.substring(username.length() - 1,
							username.length());
					try {
						final int lastNumber = Integer.parseInt(lastChar);

						if ((lastNumber % 2) == 0) {
							resp.sendRedirect("/APMExperiment.html");
							return;
						}
					} catch (final NumberFormatException e) {
					}
				}
				resp.sendRedirect("/");
				return;
			} catch (final UnknownAccountException uae) {
			} catch (final IncorrectCredentialsException ice) {
			} catch (final LockedAccountException lae) {
			} catch (final AuthenticationException ae) {
			}
		}

		resp.sendRedirect("/login.html?message=Login failed");
		return;
	}

	public void register(final String username, final String password) {
		DBConnection.createUser(generateUser(username, password));
	}

	public static User generateUser(final String username, final String plainTextPassword) {
		final RandomNumberGenerator rng = new SecureRandomNumberGenerator();
		final Object salt = rng.nextBytes();

		final String hashedPasswordBase64 = new Sha256Hash(plainTextPassword, salt, 1024)
		.toBase64();

		return new User(-1, username, hashedPasswordBase64, salt.toString(), true);
	}
}