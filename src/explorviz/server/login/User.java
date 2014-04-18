package explorviz.server.login;

public class User {
	private final String username;
	private final String hashedPassword;
	private final String salt;

	public User(final String username, final String hashedPassword, final String salt) {
		this.username = username;
		this.hashedPassword = hashedPassword;
		this.salt = salt;
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

}
