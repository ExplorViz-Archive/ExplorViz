package explorviz.server.login;

import java.io.IOException;
import java.io.PrintWriter;
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

		final PrintWriter out = resp.getWriter();
		if (!currentUser.isAuthenticated()) {
			final UsernamePasswordToken token = new UsernamePasswordToken(username, password);
			token.setRememberMe(rememberMe);
			try {
				currentUser.login(token);
				resp.sendRedirect("/");
				return;
			} catch (final UnknownAccountException uae) {
				System.out.println("There is no user with username of " + token.getPrincipal());
			} catch (final IncorrectCredentialsException ice) {
				System.out.println("Password for account " + token.getPrincipal()
						+ " was incorrect!");
			} catch (final LockedAccountException lae) {
				System.out.println("The account for username " + token.getPrincipal()
						+ " is locked.");
			} catch (final AuthenticationException ae) {
				System.out.println(ae.getLocalizedMessage());
			}
		}

		out.println("failed");
		out.flush();
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