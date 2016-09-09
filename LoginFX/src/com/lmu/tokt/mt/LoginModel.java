package com.lmu.tokt.mt;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.imageio.ImageIO;

import com.lmu.tokt.mt.util.AppConstants;

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

	public boolean updateImageInDBSucceeded(String username, File file, int imageType) throws SQLException {

		String dbField = "";

		if (imageType == AppConstants.IMAGE_TYPE_BACKGROUND) {
			dbField = AppConstants.DB_FIELD_BACKGROUND;
		} else if (imageType == AppConstants.IMAGE_TYPE_AVATAR) {
			dbField = AppConstants.DB_FIELD_AVATAR;
		} else {
			System.out.println("Invalid imageType!");
			return false;
		}

		FileInputStream fis = null;
		PreparedStatement preparedStatement = null;
		String query = "UPDATE User SET " + dbField + " = ? WHERE username = " + "'" + username + "'";

		try {
			preparedStatement = connection.prepareStatement(query);
			fis = new FileInputStream(file);
			preparedStatement.setBinaryStream(1, fis, (int) file.length());
			preparedStatement.executeUpdate();
			System.out.println("Saving image to (" + dbField + " successful!");
			return true;
		} catch (SQLException e) {
			System.out.println("SQL Error saving image in (" + dbField + ")" + e.toString());
			return false;
		} catch (FileNotFoundException e) {
			System.out.println("File Error saving image in (" + dbField + ")" + e.toString());
			return false;
		} finally {
			preparedStatement.close();
		}

	}

	public BufferedImage readImageFromDB(String username, int imageType) throws SQLException, IOException {

		String dbField = "";

		if (imageType == AppConstants.IMAGE_TYPE_BACKGROUND) {
			dbField = AppConstants.DB_FIELD_BACKGROUND;
		} else if (imageType == AppConstants.IMAGE_TYPE_AVATAR) {
			dbField = AppConstants.DB_FIELD_AVATAR;
		} else {
			System.out.println("Invalid imageType!");
			return null;
		}

		InputStream input = null;
		ResultSet resultSet = null;
		PreparedStatement preparedStatement = null;
		String query = "Select " + dbField + " FROM User WHERE username = ? ";

		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, username);
			resultSet = preparedStatement.executeQuery();

			if (resultSet.next()) {
				byte[] blob = resultSet.getBytes(1);

				if (blob != null) {
					input = new ByteArrayInputStream(blob);
					BufferedImage image = ImageIO.read(input);
					return image;
				} else {
					return null;
				}

			} else {
				return null;
			}

		} catch (SQLException e) {
			System.out.println("SQL Error getting Image blob!" + e.toString());
			return null;
		} catch (IOException e) {
			System.out.println("File Error writing Image blob to file!" + e.toString());
			return null;
		} finally {
			preparedStatement.close();
			if (input != null) {
				input.close();
			}

		}

	}

	public boolean updateLastLoggedInUser(String username) throws SQLException {

		PreparedStatement preparedStatement = null;
		String query = "UPDATE State SET " + AppConstants.DB_FIELD_LAST_USER + " = ? WHERE id = 1";
		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, username);
			preparedStatement.executeUpdate();
			System.out.println("Saving last user successful!");
			return true;
		} catch (SQLException e) {
			System.out.println("Error saving last user! " + e.toString());
			return false;
		} finally {
			preparedStatement.close();
		}

	}

	public String getLastUser() throws SQLException {

		ResultSet resultSet = null;
		PreparedStatement preparedStatement = null;
		String query = "Select " + AppConstants.DB_FIELD_LAST_USER + " FROM State WHERE id = 1";

		try {
			preparedStatement = connection.prepareStatement(query);
			resultSet = preparedStatement.executeQuery();

			if (resultSet.next()) {
				return resultSet.getString(1);
			} else {
				return null;
			}
		} catch (SQLException e) {
			System.out.println("Error getting last username! " + e.toString());
			return null;
		} finally {
			preparedStatement.close();
		}

	}

	public String getFirtUserInDB() throws SQLException {
		ResultSet resultSet = null;
		PreparedStatement preparedStatement = null;
		String query = "Select " + AppConstants.DB_FIELD_USERNAME + " FROM User";

		try {
			preparedStatement = connection.prepareStatement(query);
			resultSet = preparedStatement.executeQuery();

			if (resultSet.next()) {
				return resultSet.getString(1);
			} else {
				return null;
			}
		} catch (SQLException e) {
			System.out.println("Error getting first user in DB! " + e.toString());
			return null;
		} finally {
			preparedStatement.close();
		}
	}

	public int getServerPort() throws SQLException {
		ResultSet resultSet = null;
		PreparedStatement preparedStatement = null;
		String query = "Select port FROM Server WHERE id = 1";

		try {
			preparedStatement = connection.prepareStatement(query);
			resultSet = preparedStatement.executeQuery();

			if (resultSet.next()) {
				return resultSet.getInt(1);
			} else {
				return AppConstants.DEFAULT_SERVER_PORT;
			}
		} catch (SQLException e) {
			System.out.println("Error getting server port from DB! " + e.toString());
			return AppConstants.DEFAULT_SERVER_PORT;
		} finally {
			preparedStatement.close();
		}
	}

	public boolean updateServerPort(int port) throws SQLException {

		PreparedStatement preparedStatement = null;
		String query = "UPDATE Server SET port = ? WHERE id = 1";
		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, port);
			preparedStatement.executeUpdate();
			System.out.println("Saving port successful!");
			return true;
		} catch (SQLException e) {
			System.out.println("Error saving port! " + e.toString());
			return false;
		} finally {
			preparedStatement.close();
		}

	}

}
