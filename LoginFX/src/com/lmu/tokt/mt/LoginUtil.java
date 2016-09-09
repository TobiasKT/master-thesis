package com.lmu.tokt.mt;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLException;

import com.lmu.tokt.mt.util.AppConstants;

public class LoginUtil {

	public static LoginUtil instance;

	private String username;

	private BufferedImage background;

	private BufferedImage avatar;

	private LoginModel mLoginModel;

	private int serverport;

	public LoginUtil() {
		instance = this;
		mLoginModel = new LoginModel();
		loadUserSettings();
	}

	public static LoginUtil getInstance() {
		if (instance == null) {
			return new LoginUtil();
		} else {
			return instance;
		}
	}

	private void loadUserSettings() {

		try {
			String username = mLoginModel.getLastUser();

			if (username == null) {
				username = mLoginModel.getFirtUserInDB();
			}
			this.username = username;

			BufferedImage image = mLoginModel.readImageFromDB(username, AppConstants.IMAGE_TYPE_BACKGROUND);
			this.background = image;

			image = mLoginModel.readImageFromDB(username, AppConstants.IMAGE_TYPE_AVATAR);
			this.avatar = image;

			this.serverport = mLoginModel.getServerPort();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public String getUsername() {
		return username;
	}

	public void setUsernamne(String username) {
		this.username = username;
	}

	public BufferedImage getBackground() {
		return background;
	}

	public void setBackground(BufferedImage background) {
		this.background = background;
	}

	public BufferedImage getAvatar() {
		return avatar;
	}

	public void setAvatar(BufferedImage avatar) {
		this.avatar = avatar;
	}

	public int getServerport() {
		return serverport;
	}

	public void setServerport(int serverport) {
		this.serverport = serverport;
	}

}
