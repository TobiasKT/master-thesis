import java.io.*;
import java.net.*;
import java.util.Scanner;

public class EchoClient implements Runnable {

	private static final String SERVER_ENDED_CONNECTION = "server_end";

	private int PORT = 8080;
	private String SERVER_NAME = "192.168.178.96";

	private ObjectOutputStream mOutput;
	private ObjectInputStream mInput;
	private String mMessage = "";
	private Socket mConnection;

	public static void main(String[] args) throws IOException {

		EchoClient echoClient = new EchoClient();
		echoClient.run();

	}

	@Override
	public void run() {
		startRunning();

		System.out.println("\n Type in message: ");
		String message = input.nextLine();
		sendMessage(message);

	}

	private void startRunning() {
		try {
			connectToServer();
			setupStreams();
			whileDataExchange();
		} catch (EOFException eofException) {
			// System.out.println
			System.out.println("\n Client terminated the connection");
		} catch (IOException ioException) {
			ioException.printStackTrace();
		} finally {
			closeConnection();
		}
	}

	// connect to server
	private void connectToServer() throws IOException {
		System.out.println("Attempting connection... \n");
		mConnection = new Socket(SERVER_NAME, PORT);
		System.out.println("Connection Established! Connected to: " + mConnection.getInetAddress().getHostName());
	}

	// set up streams
	private void setupStreams() throws IOException {
		mOutput = new ObjectOutputStream(mConnection.getOutputStream());
		mOutput.flush();
		mInput = new ObjectInputStream(mConnection.getInputStream());
		System.out.println("\n The streams are now set up! \n");
	}

	private void whileDataExchange() throws IOException {
		do {
			try {
				mMessage = (String) mInput.readObject();
				System.out.println("\n" + mMessage);

			} catch (ClassNotFoundException classNotFoundException) {
				System.out.println("Unknown data received!");
			}
		} while (!mMessage.equals(SERVER_ENDED_CONNECTION));
	}

	private void closeConnection() {
		System.out.println("\n Closing the connection!");

		try {
			mOutput.close();
			mInput.close();
			mConnection.close();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	java.util.Scanner input = new Scanner(System.in);

	private void sendMessage(String message) {

		try {
			mOutput.writeObject("CLIENT - " + message);
			mOutput.flush();
			System.out.println("\nCLIENT - " + message);
		} catch (IOException ioException) {
			System.out.println("\n Oops! Something went wrong!");
		}
	}

	/*
	 * String serverHostname = new String("192.168.178.96");
	 * 
	 * if (args.length > 0) serverHostname = args[0];
	 * System.out.println("Attemping to connect to host " + serverHostname +
	 * " on port 10007.");
	 * 
	 * Socket echoSocket = null; BufferedWriter out = null; BufferedReader in =
	 * null;
	 * 
	 * try { // echoSocket = new Socket("taranis", 7); echoSocket = new
	 * Socket(serverHostname, 8080); out = new BufferedWriter(new
	 * OutputStreamWriter(echoSocket.getOutputStream(), "UTF-8")); in = new
	 * BufferedReader(new InputStreamReader(echoSocket.getInputStream(),
	 * "UTF-8")); } catch (UnknownHostException e) {
	 * System.err.println("Don't know about host: " + serverHostname);
	 * System.exit(1); } catch (IOException e) {
	 * System.err.println("Couldn't get I/O for " + "the connection to: " +
	 * serverHostname); System.exit(1); }
	 * 
	 * BufferedReader stdIn = new BufferedReader(new
	 * InputStreamReader(System.in)); String userInput;
	 * 
	 * System.out.print("input: "); while ((userInput = stdIn.readLine()) !=
	 * null) { out.write(userInput); System.out.println("echo: " +
	 * in.readLine()); System.out.print("input: "); }
	 * 
	 * out.close(); in.close(); stdIn.close(); echoSocket.close(); }
	 */
}
