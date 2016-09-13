package com.lmu.tokt.mt;

import java.sql.Connection;
import java.sql.DriverManager;

public class SqliteConnection {

	private static final String TAG = SqliteConnection.class.getSimpleName();

	public static Connection getConntection() {

		try {
			Class.forName("org.sqlite.JDBC");
			Connection connection = DriverManager.getConnection("jdbc:sqlite:Authenticator.sqlite");
			System.out.println(TAG + ": Getting DB connection successful");
			return connection;
		} catch (Exception e) {
			System.out.println(TAG + ": ERROR getting DB connection. Exception: " + e.toString());
			return null;
		}
	}

}
