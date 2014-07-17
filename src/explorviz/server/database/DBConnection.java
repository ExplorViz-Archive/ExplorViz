package explorviz.server.database;

import java.sql.*;

import org.h2.tools.Server;

import explorviz.server.login.LoginServiceImpl;
import explorviz.server.login.User;

public class DBConnection {
	private static Server server;
	private static Connection conn;

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
		final ResultSet resultSet = queryForAllUsers();
		final boolean alreadyInitialized = resultSet.next();

		if (!alreadyInitialized) {
			final User admin = LoginServiceImpl.generateUser("admin", "explorVizPass");
			createUser(admin);
		}
	}

	private static ResultSet queryForAllUsers() throws SQLException {
		conn.createStatement()
				.execute(
						"CREATE TABLE IF NOT EXISTS ExplorVizUser(ID int NOT NULL AUTO_INCREMENT, username VARCHAR(255) NOT NULL, hashedPassword VARCHAR(4096) NOT NULL, salt VARCHAR(4096) NOT NULL, PRIMARY KEY (ID));");

		final ResultSet resultSet = conn.createStatement().executeQuery(
				"SELECT * FROM ExplorVizUser;");
		return resultSet;
	}

	public static void createUsersForExperimentIfNotExist(final int userAmount) {
		try {
			final ResultSet resultSet = queryForAllUsers();
			boolean alreadyAdded = false;
			while (resultSet.next()) {
				if (resultSet.getString("username").equalsIgnoreCase("user1")) {
					alreadyAdded = true;
					break;
				}
			}
			if (!alreadyAdded) {
				System.out.println("Generating users for experiment");
				final String[] pwList = new String[] { "rbtewm", "sfhbxf", "xvdgrp", "cqzohz",
						"krmopt", "ejdsfe", "iuifko", "okurfy" };
				for (int i = 1; i <= userAmount; i++) {
					final String user = "user" + i;
					final String pw = pwList[i % pwList.length];

					createUser(LoginServiceImpl.generateUser(user, pw));
					System.out.println("Experiment user: " + user + "; " + pw);
				}
			}
		} catch (final SQLException e) {
			e.printStackTrace();
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
		try {
			conn.createStatement().execute(
					"UPDATE ExplorVizUser" + " SET hashedPassword='" + user.getHashedPassword()
							+ "',salt='" + user.getSalt() + "',firstLogin=" + user.isFirstLogin()
							+ " WHERE username='" + user.getUsername() + "';");
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	public static User getUserByName(final String username) {
		try {
			final ResultSet resultSet = conn.createStatement().executeQuery(
					"SELECT * FROM ExplorVizUser WHERE username='" + username + "';");

			final boolean found = resultSet.next();
			if (found) {
				return new User(resultSet.getString("username"),
						resultSet.getString("hashedPassword"), resultSet.getString("salt"),
						resultSet.getBoolean("firstLogin"));
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
					"INSERT INTO ExplorVizUser(username,hashedPassword,salt) VALUES ('"
							+ user.getUsername() + "','" + user.getHashedPassword() + "','"
							+ user.getSalt() + "');");
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}
}
