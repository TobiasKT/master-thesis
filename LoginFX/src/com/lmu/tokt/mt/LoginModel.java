package com.lmu.tokt.mt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginModel {

	private Connection connection;

	public LoginModel() {
		connection = SqliteConnection.getConntection();
		if (connection == null) {
			System.out.println("No Connection to SQLite-DB");
		}
	}

	public boolean isDBConnected() {
		try {
			return !connection.isClosed();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean isValidCredentials(String username, String password) throws SQLException {

		PreparedStatement preparedStatement = null;
		ResultSet resulsSet = null;
		String query = "SELECT * FROM user WHERE username = ? AND password = ?";

		try {

			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, username);
			preparedStatement.setString(2, password);

			resulsSet = preparedStatement.executeQuery();

			if (resulsSet.next()) {
				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;

		} finally {
			preparedStatement.close();
			resulsSet.close();
		}

	}

	// TODO boolean zurükgeben
	public boolean registerUserInDBSucceeded(String username, String password) throws SQLException {

		PreparedStatement preparedStatement = null;
		String query = "INSERT INTO User (username, password) VALUES (?,?)";

		try {

			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, username);
			preparedStatement.setString(2, password);
			preparedStatement.executeUpdate();
			System.out.println("Registration successful");
			return true;

		} catch (SQLException sql) {
			System.out.println(sql.toString());
			return false;

		} catch (Exception e) {
			System.out.println(e.toString());
			return false;
		} finally {
			preparedStatement.close();

		}
	}

}
