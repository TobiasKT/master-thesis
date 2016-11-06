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

	private static final String TAG = LoginModel.class.getSimpleName();

	private Connection connection;

	public LoginModel() {
		connection = SqliteConnection.getConntection();
		if (connection == null) {
			System.out.println(TAG + ": NO Connection to SQLite-DB");
		}
	}

	public boolean isDBConnected() {
		try {
			return !connection.isClosed();
		} catch (SQLException e) {
			System.out.println(TAG + ": ERROR getting db connection state. Exception: " + e.toString());
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
				//System.out.println(TAG + ": Credentials CORRECT.");
				return true;
			} else {
				//System.out.println(TAG + ": Credentials INCORRECT.");
				return false;
			}

		} catch (Exception e) {
			System.out.println(TAG + ": ERROR validate user credentials. Exception: " + e.toString());
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
			//System.out.println(TAG + ": Register new user in DB successful");
			return true;

		} catch (SQLException sql) {
			System.out.println(TAG + ": ERROR register new user in DB. Exception: " + sql.toString());
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
			System.out.println(TAG + ": INVALID imageType in updateImageInDBSucceeded");
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
			//System.out.println(TAG + ": Saving image to User (" + dbField + ") in DB successful!");
			return true;
		} catch (SQLException e) {
			System.out.println(TAG + ": SQL ERROR saving image to User (" + dbField + "). Exception: " + e.toString());
			return false;
		} catch (FileNotFoundException e) {
			System.out.println(TAG + ": FILE ERROR saving image to User (" + dbField + "). Exception: " + e.toString());
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
			//System.out.println(TAG + ": INVALID imageType in readImageFromDB");
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
					//System.out.println(TAG + ": Reading blob (" + dbField + ") from DB successful");
					return image;
				} else {
					//System.out.println(TAG + ": NO blob (" + dbField + ") found in DB");
					return null;
				}

			} else {
				//System.out.println(TAG + ": NO blob (" + dbField + ") found in DB");
				return null;
			}

		} catch (SQLException e) {
			System.out.println(TAG + ": SQL ERROR reading blob (" + dbField + "). Exception: " + e.toString());
			return null;
		} catch (IOException e) {
			System.out
					.println(TAG + ": FILE ERROR  writing blob (" + dbField + ") to file. Exception: " + e.toString());
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
			//System.out.println(TAG + ": Saving last logged in user in DB successful");
			return true;
		} catch (SQLException e) {
			System.out.println(TAG + ": ERROR saving last logged in user in DB. Exception: " + e.toString());
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
				//System.out.println(TAG + ": Getting last logged in user from DB successful");
				return resultSet.getString(1);
			} else {
				//System.out.println(TAG + ": No last logged in User");
				return null;
			}
		} catch (SQLException e) {
			System.out.println(TAG + ": ERROR getting last logged in user from DB. Exception: " + e.toString());
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
				//System.out.println(TAG + ": Getting first user from DB successful");
				return resultSet.getString(1);
			} else {
				//System.out.println(TAG + ": No user registered");
				return null;
			}
		} catch (SQLException e) {
			System.out.println(TAG + ": ERROR getting first user in DB. Exception: " + e.toString());
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
				//System.out.println(TAG + ": Getting server port from DB successful");
				return resultSet.getInt(1);
			} else {
				//System.out.println(TAG + ": NO svaed server port in DB. Return default port ("
					//	+ AppConstants.DEFAULT_SERVER_PORT + ")");
				return AppConstants.DEFAULT_SERVER_PORT;
			}
		} catch (SQLException e) {
			System.out.println(TAG + ": ERROR getting server port from DB! " + e.toString());
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
			//System.out.println(TAG + ": Saving server port in DB successful");
			return true;
		} catch (SQLException e) {
			System.out.println(TAG + ": ERROR saving server port in DB. Exception: " + e.toString());
			return false;
		} finally {
			preparedStatement.close();
		}

	}

	public boolean updateDidIntroState(int state) throws SQLException {

		PreparedStatement preparedStatement = null;
		String query = "UPDATE State SET didIntro = ? WHERE id = 1";
		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, state);
			preparedStatement.executeUpdate();
			//System.out.println(TAG + ": Saving didIntro in DB successful");
			return true;
		} catch (SQLException e) {
			System.out.println(TAG + ": ERROR saving didIntro state in DB. Exception: " + e.toString());
			return false;
		} finally {
			preparedStatement.close();
		}

	}

	public int getDidIntroState() throws SQLException {
		ResultSet resultSet = null;
		PreparedStatement preparedStatement = null;
		String query = "Select didIntro FROM State WHERE id = 1";

		try {
			preparedStatement = connection.prepareStatement(query);
			resultSet = preparedStatement.executeQuery();

			if (resultSet.next()) {
				//System.out.println(TAG + ": Getting didIntro state from DB successful");
				return resultSet.getInt(1);
			} else {
				//System.out.println(TAG + ": NO saved didIntro state in DB. Return default state (" + 0 + ")");
				return 0;
			}
		} catch (SQLException e) {
			System.out.println(TAG + ": ERROR getting didIntro state from DB! " + e.toString());
			return 0;
		} finally {
			preparedStatement.close();
		}
	}
}
