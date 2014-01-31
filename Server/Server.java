package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import mouserunner.Game.Game;
import mouserunner.Game.Player;
import mouserunner.Managers.GameplayManager;
import mouserunner.System.Direction;
import mouserunner.System.Timer;

/**
 * The server program that handles lobby and game
 * @author Zorek
 */
public class Server {

	public final static int port = 1987;
	public final static int multiPort = 5000;
	public final static int multiResponse = 4544;
	public final static String multicastGroup = "224.0.0.1";
	public final static int SERVERNAMELENGTH = 16;
	public final static int SOCKETTIMEOUT = 1000;
	private ServerSocket listener;
	private List<ClientHandler> clientList;
	private boolean listen;
	private boolean annonce;
	private String name;	// Game
	private Game game;
	private boolean runningGame;
	public int interval;

	/**
	 * Creates a new server with the given name. The name needs to be shorter than
	 * Server.SERVERNAMELENGTH or an IllegalArgumentException is thrown
	 * @param name
	 * @throws java.io.IOException
	 */
	public Server(String name) throws IOException {
		if (name.length() > Server.SERVERNAMELENGTH) {
			throw new IllegalArgumentException("The Server name is to long");
		}
		this.name = name;
		clientList = new LinkedList<ClientHandler>();
		listen = true;
		this.annonce = true;
		this.runningGame = false;
		listen();
		listenServerIdentification();
	}

	private void listen() throws IOException {
		System.out.println("Server started");
		listener = new ServerSocket(port);
		System.out.println("Server starts to listen on: " + getIP() + ":" + port);
		final Server server = this;
		new Thread() {

			@Override
			public void run() {
				while (listen) {
					try {
						clientList.add(new ClientHandler(listener.accept(), server));
					} catch (IOException e) {
						System.out.println("Server has left listening mode");
					}
				}
			}
		}.start();
	}

	/**
	 * This method starts a thread that listens for multicast requests from other mouserunner
	 * instances that is looking for servers. Terminate this thread by setting annonce to false.
	 */
	private void listenServerIdentification() {

		new Thread() {

			@Override
			public void run() {
				try {
					MulticastSocket multiSocket = new MulticastSocket(multiPort);
					multiSocket = new MulticastSocket(multiPort);
					multiSocket.joinGroup(InetAddress.getByName(multicastGroup));
					byte[] data = new byte[0];
					DatagramPacket multiPacket = new DatagramPacket(data, data.length);

					while (annonce) {
						multiSocket.receive(multiPacket);

						System.out.println("Server sent indentification request to " + name);
						System.out.println("Server:  " + name + "'s requester is: " + multiPacket.getAddress().getHostAddress());

						// Sending identification to client
						System.out.println("Server: " + name + " is sending server identification");
						DatagramSocket responseSocket = new DatagramSocket();
						byte[] msg = new byte[Server.SERVERNAMELENGTH];
						byte[] serverName = name.getBytes();

						for (int i = 0; i < serverName.length; i++) {
							msg[i] = serverName[i];
						}

						DatagramPacket packet = new DatagramPacket(msg, msg.length);

						InetAddress address = multiPacket.getAddress();
						packet = new DatagramPacket(msg, msg.length, address, multiResponse);
						responseSocket.send(packet);
						responseSocket.close();
					}
					multiSocket.leaveGroup(Inet4Address.getByName(multicastGroup));
					multiSocket.close();

					System.out.println("Server " + name + " has stopped annoncing its existence");
				} catch (IOException e) {
					System.out.println("Exception in server identification");
				}
			}
		}.start();

		System.out.println("Server " + name + " is ready to identify himself");
	}
	
	public Game getGame() {
		return this.game;
	}

	public void initializeGame(Game game) {
		System.out.println("Server: Starting initialization of a new game");
		this.game=game;
		game.setGameTimer(new Timer());
		runningGame = false;
		interval = 1000 / 1000;
		
		for (ClientHandler ch : clientList) {
			ch.setGame(game);
		}

		for (int i = 0; i < GameplayManager.getInstance().players.size(); i++) {
			GameplayManager.getInstance().players.get(i).setReady(false);
		}
		System.out.println("Server: Sending game start message");
		broadcastMessage(6, Calendar.getInstance().getTimeInMillis() + " " + "5000");

		try {
			boolean gameReady = false;
			//Wait for all players to set status to ready
			System.out.println("Server: Standing by waiting for players");
			while (!gameReady) {
				gameReady = true;
				for (int i = 0; i < GameplayManager.getInstance().players.size(); i++) {
					if (!GameplayManager.getInstance().players.get(i).isReady()) {
						gameReady = false;
						break;
					}
				}
				Thread.sleep(50);
			}
			broadcastMessage(10);
			game.getGameTimer().setTimestamp();
			GameplayManager.getInstance().gameTimer = game.getGameTimer();
		} catch (InterruptedException interruptedException) {
			interruptedException.printStackTrace();
		}
		
		for (int i = 0; i < GameplayManager.getInstance().players.size(); i++)
			GameplayManager.getInstance().players.get(i).setReady(false);
		runningGame = true;
	}

	private void runGame() {
		new Thread() {
			@Override
			public void run() {				
				initializeGame(new Game(getServer()));
				
				while (runningGame) {
					int start = (int) (System.nanoTime() / 1000000);

					game.update();

					int time = (int) (System.nanoTime() / 1000000) - start;
					while (time < interval) {
						try {
							Thread.sleep((long) (interval - time));
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						time = (int) (System.nanoTime() / 1000000) - start;
					}
				}
			}
		}.start();
	}

	public void placeArrow(int arrowDir, int x, int y, Player player) {
		Direction dir;
		if (arrowDir == 0) {
			dir = Direction.LEFT;
		} else if (arrowDir == 1) {
			dir = Direction.RIGHT;
		} else if (arrowDir == 2) {
			dir = Direction.UP;
		} else {
			dir = Direction.DOWN;
		}

		// If the arrow where added all clients are notified
		if(game.placeArrow(Direction.intToDir(arrowDir), x, y, player)) {
			System.out.println("Server: Placed arrow sending this message: " + (arrowDir + " " + x + " " + y + " " + player.getName()));
			broadcastMessage(13, arrowDir + " " + x + " " + y + " " + player.getName());
		} else {
			System.out.println("Server: Arrow could not be placed");
		}
	}

	public void shutdown() {
		System.out.println("Server prepare for shutdown...");
		listen = false;
		annonce = false;
		try {
			listener.close();
		} catch (Exception e) {
			System.out.println("Server could not shutdown");
		}
		for (ClientHandler c : clientList) {
			c.close();
		}
		System.out.println("Server stopped");
	}

	public void startGame() {
		annonce = false;
		listen = false;
		runGame();
	}
	
	/**
	 * Broadcasts the message to all connected clients.
	 * @param type the type of the message
	 */
	public void broadcastMessage(int type) {
		broadcastMessage(type, "");
	}

	/**
	 * Broadcasts the message to all connected clients with the given parameter
	 * @param type the message type
	 * @param message the parameter of the message
	 */
	public void broadcastMessage(int type, Object message) {
		for (ClientHandler c : clientList) {
			c.send(type, message);
		}
	}

	public static String getIP() {
		String result = "0.0.0.0";
		try {
			result = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			System.out.println("Server could not recieve IP adress");
		}
		return result;
	}
	
	private Server getServer() {
		return this;
	}
	
	public void sendStatistics() {
		//Sends all info of the game to the players
	}
}
