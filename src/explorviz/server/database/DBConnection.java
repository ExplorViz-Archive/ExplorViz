package explorviz.server.database;

import java.security.SecureRandom;
import java.sql.*;
import java.util.Random;

import org.h2.tools.Server;
import org.json.JSONArray;
import org.json.JSONObject;

import explorviz.server.login.LoginServlet;
import explorviz.shared.auth.Role;
import explorviz.shared.auth.User;

public class DBConnection {
	private static Server server;
	private static Connection conn;

	public final static String USER_PREFIX = "user";

	private static final Random RANDOM = new SecureRandom();

	public static final int PASSWORD_LENGTH = 8;

	private DBConnection() {
	}

	public static void connect() throws SQLException {
		final Server server = Server.createTcpServer();
		server.start();

		try {
			Class.forName("org.h2.Driver");
			conn = DriverManager.getConnection("jdbc:h2:~/.explorviz/.explorvizDB", "sa", "");
			conn.createStatement()
					.execute("CREATE USER IF NOT EXISTS shiro PASSWORD 'kad8961asS';");
		} catch (final ClassNotFoundException e) {
			e.printStackTrace();
			server.stop();
		}

		initIfEmptyDB();
	}

	private static void initIfEmptyDB() throws SQLException {
		createTablesIfNotExists();

		final ResultSet users = queryUsers();
		final boolean alreadyInitialized = users.next();

		if (!alreadyInitialized) {
			createUser(LoginServlet.generateUser("admin", "explorVizPass"));
			createRole(new Role(-1, "admin"));
			createUserToRole("admin", "admin");
			createUser(LoginServlet.generateUser("DemoUser", "DemoUser"));
		}
	}

	private static void createTablesIfNotExists() throws SQLException {
		conn.createStatement().execute(
				"CREATE TABLE IF NOT EXISTS ExplorVizUser(ID int NOT NULL AUTO_INCREMENT, username VARCHAR(255) NOT NULL, hashedPassword VARCHAR(4096) NOT NULL, salt VARCHAR(4096) NOT NULL, firstLogin BOOLEAN NOT NULL, questionnairePrefix VARCHAR(4096), experimentFinished BOOLEAN, PRIMARY KEY (ID));");
		conn.createStatement().execute(
				"CREATE TABLE IF NOT EXISTS ExplorVizRole(ID int NOT NULL AUTO_INCREMENT, rolename VARCHAR(255) NOT NULL, PRIMARY KEY (ID));");
		conn.createStatement().execute(
				"CREATE TABLE IF NOT EXISTS ExplorVizUserToRole(ID int NOT NULL AUTO_INCREMENT, userid int, roleid int, PRIMARY KEY (ID), FOREIGN KEY (userid) REFERENCES ExplorVizUser(ID), FOREIGN KEY (roleid) REFERENCES ExplorVizRole(ID));");
	}

	private static ResultSet queryUsers() throws SQLException {
		return conn.createStatement().executeQuery("SELECT * FROM ExplorVizUser;");
	}

	public static void createUsersForExperimentIfNotExist(final int userAmount) {
		final User maybeUser = getUserByName(USER_PREFIX + "1");

		if (maybeUser == null) {
			System.out.println("Generating users for experiment");

			for (int i = 1; i <= userAmount; i++) {
				final String user = USER_PREFIX + i;
				final String pw = generateRandomPassword();

				createUser(LoginServlet.generateUser(user, pw));
				System.out.println("Experiment user: " + user + "; " + pw);
			}
		}
	}

	public static JSONArray createUsersForQuestionnaire(final String prefix, final int userAmount) {

		final User lastUser = getLastExperimentUser();

		int lastID = 1;

		if (lastUser != null) {
			lastID = Integer.parseInt(lastUser.getUsername().substring("user".length())) + 1;
		}

		System.out.println("Generating users for experiment");

		final JSONArray allUsers = getQuestionnaireUsers(prefix);

		for (int i = lastID; i < (userAmount + lastID); i++) {
			final String user = USER_PREFIX + i;
			final String pw = generateRandomPassword();

			createUser(LoginServlet.generateUser(user, pw, prefix));

			final JSONObject jsonUser = new JSONObject();
			jsonUser.put("username", user);
			jsonUser.put("pw", pw);

			allUsers.put(jsonUser);

			System.out.println("Experiment user: " + user + "; " + pw);
		}

		return allUsers;
	}

	private static User getLastExperimentUser() {

		try {
			final ResultSet resultSet = conn.createStatement().executeQuery(
					"SELECT * FROM ExplorVizUser WHERE LENGTH(username) = (SELECT MAX(LENGTH(username)) FROM ExplorVizUser WHERE username LIKE 'user%') ORDER BY username DESC LIMIT 1;");

			if (resultSet.next()) {

				final User user = new User(resultSet.getInt("ID"), resultSet.getString("username"),
						resultSet.getString("hashedPassword"), resultSet.getString("salt"),
						resultSet.getBoolean("firstLogin"),
						resultSet.getString("questionnairePrefix"),
						resultSet.getBoolean("experimentFinished"));

				return user;
			} else {
				return null;
			}
		} catch (final SQLException e) {
			System.out.println(e);
			return null;
		}
	}

	public static void closeConnections() {
		try {
			conn.close();
			server.stop();
		} catch (final SQLException e) {
		}
	}

	public static void updateUser(final User user) {

		System.out.println("!!!!!!!!! " + user.isExperimentFinished());

		try {
			conn.createStatement().execute("UPDATE ExplorVizUser" + " SET hashedPassword='"
					+ user.getHashedPassword() + "',salt='" + user.getSalt() + "',firstLogin='"
					+ user.isFirstLogin() + "',experimentFinished=" + user.isExperimentFinished()
					+ " WHERE username='" + user.getUsername() + "';");
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	public static User getUserByName(final String username) {
		try {
			final ResultSet resultSet = conn.createStatement()
					.executeQuery("SELECT * FROM ExplorVizUser WHERE username='" + username + "';");

			if (resultSet.next()) {
				final User user = new User(resultSet.getInt("ID"), resultSet.getString("username"),
						resultSet.getString("hashedPassword"), resultSet.getString("salt"),
						resultSet.getBoolean("firstLogin"),
						resultSet.getString("questionnairePrefix"),
						resultSet.getBoolean("experimentFinished"));

				if (resultSet.getString("questionnairePrefix") != null) {
					user.setQuestionnairePrefix(resultSet.getString("questionnairePrefix"));
				}

				final ResultSet roleRelations = conn.createStatement().executeQuery(
						"SELECT * FROM ExplorVizUserToRole WHERE userid =" + user.getId() + ";");

				while (roleRelations.next()) {
					final Role role = getRoleById(roleRelations.getInt("roleid"));
					if (role != null) {
						user.addToRoles(role);
					}
				}

				return user;
			} else {
				return null;
			}
		} catch (final SQLException e) {
			return null;
		}
	}

	public static void removeUser(final String username) {
		try {
			conn.createStatement()
					.execute("DELETE FROM ExplorVizUser WHERE username='" + username + "';");
		} catch (final SQLException e) {
			System.err.println(e);
		}
	}

	public static boolean didUserFinishQuestionnaire(final String username) {

		final User user = getUserByName(username);

		return user.isExperimentFinished();
	}

	public static JSONArray getQuestionnaireUsers(final String questPrefix) {

		final JSONArray currentUsers = new JSONArray();

		try {
			final ResultSet resultSet = conn.createStatement().executeQuery(
					"SELECT * FROM ExplorVizUser WHERE questionnairePrefix='" + questPrefix + "';");

			while (resultSet.next()) {
				final User user = new User(resultSet.getInt("ID"), resultSet.getString("username"),
						resultSet.getString("hashedPassword"), resultSet.getString("salt"),
						resultSet.getBoolean("firstLogin"),
						resultSet.getString("questionnairePrefix"),
						resultSet.getBoolean("experimentFinished"));

				final JSONObject jsonUser = new JSONObject();
				jsonUser.put("username", user.getUsername());
				jsonUser.put("pw", "");
				jsonUser.put("expFinished", user.isExperimentFinished());
				currentUsers.put(jsonUser);
			}
		} catch (final SQLException e) {
			System.out.println(e);
			return null;
		}

		return currentUsers;
	}

	public static Role getRoleByName(final String rolename) {
		try {
			final ResultSet resultSet = conn.createStatement()
					.executeQuery("SELECT * FROM ExplorVizRole WHERE rolename='" + rolename + "';");

			final boolean found = resultSet.next();
			if (found) {
				return new Role(resultSet.getInt("ID"), resultSet.getString("rolename"));
			} else {
				return null;
			}
		} catch (final SQLException e) {
			return null;
		}
	}

	private static Role getRoleById(final int roleId) {
		try {
			final ResultSet resultSet = conn.createStatement()
					.executeQuery("SELECT * FROM ExplorVizRole WHERE ID=" + roleId + ";");

			if (resultSet.next()) {
				return new Role(resultSet.getInt("ID"), resultSet.getString("rolename"));
			} else {
				return null;
			}
		} catch (final SQLException e) {
			return null;
		}
	}

	public static void createUser(final User user) {
		try {
			conn.createStatement().execute(
					"INSERT INTO ExplorVizUser(username,hashedPassword,salt,firstLogin,questionnairePrefix) VALUES ('"
							+ user.getUsername() + "','" + user.getHashedPassword() + "','"
							+ user.getSalt() + "','" + user.isFirstLogin() + "', '"
							+ user.getQuestionnairePrefix() + "');");
		} catch (final SQLException e) {
			e.printStackTrace();
		}

	}

	public static void createRole(final Role role) {
		try {
			conn.createStatement().execute(
					"INSERT INTO ExplorVizRole(rolename) VALUES ('" + role.getRolename() + "');");
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	public static void createUserToRole(final String username, final String rolename) {
		try {
			final User user = getUserByName(username);
			final Role role = getRoleByName(rolename);

			if ((user != null) && (role != null)) {
				conn.createStatement()
						.execute("INSERT INTO ExplorVizUserToRole(userid, roleid) VALUES ("
								+ user.getId() + ", " + role.getId() + ");");
			}
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Generates a random password for experiment users
	 */
	public static String generateRandomPassword() {
		final String letters = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ23456789+!";

		String password = "";

		for (int i = 0; i < PASSWORD_LENGTH; i++) {
			final int index = (int) (RANDOM.nextDouble() * letters.length());
			password += letters.substring(index, index + 1);
		}

		return password;
	}
}
