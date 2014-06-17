package explorviz.server.login;

public class User {
	private final String username;
	private final String hashedPassword;
	private final String salt;

	private boolean firstLogin;

	public User(final String username, final String hashedPassword, final String salt,
			final boolean firstLogin) {
		this.username = username;
		this.hashedPassword = hashedPassword;
		this.salt = salt;
		this.firstLogin = firstLogin;
	}

	public String getUsername() {
		return username;
	}

	public String getHashedPassword() {
		return hashedPassword;
	}

	public String getSalt() {
		return salt;
	}

	public boolean isFirstLogin() {
		return firstLogin;
	}

	public void setFirstLogin(final boolean firstLogin) {
		this.firstLogin = firstLogin;
	}

}
