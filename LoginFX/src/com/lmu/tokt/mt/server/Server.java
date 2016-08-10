package com.lmu.tokt.mt.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import com.lmu.tokt.mt.LoginController;

public class Server extends Thread {

	// Terminal Ports Listen: netstat -an | grep -i "listen" , sudo lsof -i -P |
	// grep -i "listen" , netstat -atp tcp | grep -i "listen"

	/*-------------------------------------------*/
	//ifconfig | grep "inet " | grep -v 127.0.0.1
	
	private ServerSocket mServerSocket;

	private static int mPort = 8080;

	private boolean isHeartRateAvailabele = false;
	
	private LoginController mLoginController;
	
	public Server(LoginController login){
		mLoginController = login;
	}

	public void run() {

		try {
			mServerSocket = new ServerSocket(mPort);
			System.out.println("Server starting at port number: " + mPort);

			// Client connecting
			System.out.println("Waiting for clients to connect");
			Socket socket = mServerSocket.accept();
			System.out.println("A client has connected.");

			// Send message to the client
			BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			bufferedWriter.write("This is a message from the Server");
			bufferedWriter.newLine();
			bufferedWriter.flush();

			// Receive message from the client
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String data;
			while ((data = bufferedReader.readLine()) != null) {
				System.out.println("Message from the client: " + data);

				String sensorDataTyp = data.split(":")[0];
				switch (sensorDataTyp) {
				case "HEARTRATE":
					double heartrate = Double.parseDouble(data.split(":")[1]);
					if (heartrate > 40 && heartrate < 160) {
						authenticated(true);
					} else {
						authenticated(false);
					}
					break;

				default:
					break;
				}

			}

			System.out.println("Server has ended");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private synchronized void authenticated(boolean isAuthenticated) {

		if (isAuthenticated) {
			mLoginController.getTxtPassword().setDisable(true);
			mLoginController.getLblAuthenticated().setVisible(true);
			mLoginController.getImgAuthenticatedSucceeded().setVisible(true);
			// TODO send to watch
		} else {
			mLoginController.getTxtPassword().clear();
			mLoginController.getTxtPassword().setDisable(false);
			mLoginController.getLblAuthenticated().setVisible(false);
			mLoginController.getImgAuthenticatedSucceeded().setVisible(false);
			// TODO send to watch
		}

	}

	public boolean getIsHeartRateAvailable() {
		return isHeartRateAvailabele;
	}

}
