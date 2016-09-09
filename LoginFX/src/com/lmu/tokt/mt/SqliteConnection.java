package com.lmu.tokt.mt;

import java.sql.Connection;
import java.sql.DriverManager;

public class SqliteConnection {

	public static Connection getConntection() {

		try {
			Class.forName("org.sqlite.JDBC");
			Connection connection = DriverManager.getConnection("jdbc:sqlite:Authenticator.sqlite");
			return connection;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
