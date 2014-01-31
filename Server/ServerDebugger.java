package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Erik
 */
public class ServerDebugger {

	public ServerDebugger() {
		try {
			System.out.println(" --- Client Started ---");
			Socket socket = new Socket("127.0.0.1", 1987);
			System.out.println("Client connected to server");
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			BufferedReader sysIn = new BufferedReader(new InputStreamReader(System.in));

			System.out.println("Please enter name: \n");

			String userName = sysIn.readLine();

			// Connect player
			out.println("0 " + userName);

			new ChatSender(userName, out).start();
			new ChatPrinter(socket).start();
		} catch (UnknownHostException ex) {
			Logger.getLogger(ServerDebugger.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(ServerDebugger.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	private static class ChatPrinter extends Thread {

		BufferedReader in;
		boolean running;

		public ChatPrinter(Socket socket) throws IOException {
			this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			running = true;
		}

		@Override
		public void run() {
			while (running) {

				try {
					if (in.ready()) {
						String line = in.readLine();
						if (line.startsWith("0")) {
							System.out.println(line.substring(2, line.length()) + " has connected \n");
						} else if (line.startsWith("1")) {
							running = false;
							System.out.println("Logging out");
						} else if (line.startsWith("2")) {
							System.err.println(Calendar.getInstance().getTime().toString() + " " + line.substring(2) + "\n");
						}
					} else {
						Thread.sleep(50);
					}
				} catch (InterruptedException ex) {
					Logger.getLogger(ServerDebugger.class.getName()).log(Level.SEVERE, null, ex);
				} catch (IOException ex) {
					Logger.getLogger(ServerDebugger.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
	}

	private static class ChatSender extends Thread {

		String name;
		PrintWriter out;
		BufferedReader sysIn;
		boolean running;

		public ChatSender(String name, PrintWriter out) {
			this.name = name;
			this.out = out;
			sysIn = new BufferedReader(new InputStreamReader(System.in));
			running = true;
		}

		@Override
		public void run() {
			String userInput;
			try {
				while ((userInput = sysIn.readLine()) != null && running) {
					if (userInput.equals("shutdown")) {
						out.println("1");
						running = false;
					}else if (userInput.equals("stop")) {
						out.println("7");
					} else {
						out.println("2 " + userInput);
					}
				}
			} catch (IOException ex) {
				Logger.getLogger(ServerDebugger.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
}
