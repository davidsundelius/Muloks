package Server;

import java.awt.Color;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Iterator;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import mouserunner.Game.Arrow;
import mouserunner.Game.Entity.Cat;
import mouserunner.Game.Player;
import mouserunner.Managers.GameplayManager;
import mouserunner.Game.Game;
import mouserunner.Game.Entity.Mouse;
import mouserunner.System.SyncObject;

/**
 * This class handles the server side socket for a given 
 * client
 * @author Zorek
 */
public class ClientHandler extends Thread {

	private Socket socket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private Player player;
	private Server server;
	private boolean listening;	// Game
	private Game game;

	public void setGame(Game game) {
		this.game = game;
	}

	public ClientHandler(Socket socket, Server server) {
		this.socket = socket;
		this.server = server;
		try {
			in = new ObjectInputStream(socket.getInputStream());
			out = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		listening = true;
		start();
	}

	/**
	 * Send a message to the Client without any parameters
	 * @param type the type of the message
	 */
	public void send(int type){
		send(type, "");
	}

	/**
	 * Send a message to the Client with the given parameter
	 * @param type the type of the message
	 * @param parameters the parameters
	 */
	public void send(int type, Object parameters){
		try {
			out.writeInt(type);
			out.writeObject(parameters);
			out.flush();
			out.reset();
		} catch (IOException ex) {
			Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void close() {
		send(7);
		listening = false;
	}

	@Override
	public void run() {
		System.out.println("Server: A new socket is created");
		String line = null;
		while (listening) {
			try {
				int type = in.readInt();
				Object parameters = in.readObject();
				switch (type) {
					case 0: //Player connected
						boolean accept = true;
						// The parameters are a String
						player = new Player((String) parameters);
						for (Player p : GameplayManager.getInstance().players) {
							if (p.equals(player)) {
								accept = false;
								break;
							}
						}
						if (accept) {
							for (Player p : GameplayManager.getInstance().players) {
								send(0, p.getName() + " " + p.getColor().getRed() + " " + p.getColor().getGreen() + " " + p.getColor().getBlue());
							}
							GameplayManager.getInstance().players.add(player);
							GameplayManager.getInstance().ai.put(player,false);
							GameplayManager.getInstance().handicap.put(player, 0);
							Scanner sc = new Scanner((String) parameters);
							server.broadcastMessage(0, sc.next() + " " + player.getColor().getRed() + " " + player.getColor().getGreen() + " " + player.getColor().getBlue());
							System.out.println("Server: " + player.getName() + " has connected");
						} else {
							send(7);
							System.out.println("Server: Refused connection for a new " + player.getName());
							listening = false;
						}
						break;
					case 1: //Player disconnected
						GameplayManager.getInstance().players.remove(player);
						GameplayManager.getInstance().ai.remove(player);
						GameplayManager.getInstance().handicap.remove(player);
						server.broadcastMessage(1, new String(player.getName()));
						System.out.println("Server: " + player.getName() + " has disconnected");
						listening = false;
						break;
					case 2: //Send chat message
						String message = player.getName() + ":" + ((String) parameters);
						server.broadcastMessage(2, message);
						System.out.println("Server<CHAT>: " + message);
						break;
					case 3: //Change color <Dont work>
						Scanner sc = new Scanner((String) parameters);
						player.setColor(new Color(sc.nextInt(), sc.nextInt(), sc.nextInt()));
						server.broadcastMessage(3, player.getName() + " " + player.getColor().getRed() + " " + player.getColor().getGreen() + " " + player.getColor().getBlue());
						System.out.println("Server: " + player.getName() + " has connected");
						break;
					case 4: //Change setting <Dont work>
						System.out.println("Server: " + player.getName() + " has tried to change a setting");
						break;
					case 5: //Set ready
						server.broadcastMessage(5, player.getName());
						player.setReady(true);
						System.out.println("Server: " + player.getName() + " is ready/unready");
						break;
					case 6: //Start game
						for (Player p : GameplayManager.getInstance().players) {
							if (!p.isReady()) {
								break;
							}
						}
						server.startGame();
						System.out.println("Server: " + player.getName() + " has started the game");
						break;
					case 7: //Server terminated
						//server.shutdown();
						this.listening = false;
						System.out.println("Server: " + player.getName() + " stops its ClientHandler");
						break;

					case 11: // Sync request
						//Object syncType = in.readObject();
						int syncType = Integer.valueOf(String.valueOf(parameters));
						//System.out.println("Server: debug " + syncType.getClass());
//						System.out.println("Server: Sync request (" + syncType + ")");
						TreeSet<SyncObject> so = new TreeSet<SyncObject>();
						switch (syncType) {
							case 0: // Mice								
								Iterator<Mouse> mi = game.getMice().iterator();
								while (mi.hasNext()) {
									so.add(mi.next().getSyncable());
								}
								send(11, so);
//								System.out.println("Server sync: Sent mouse list");
								break;
							case 1:
								Iterator<Cat> ci = game.getCats().iterator();
								while (ci.hasNext()) {
									so.add(ci.next().getSyncable());
								}
								send(11, so);
//								System.out.println("Server sync: Sent cat list");
								break;
							case 2:
								Iterator<Arrow> arri = game.getArrows().iterator();
//								while (arri.hasNext()) {
//									so.add(arri.next().getSyncable());
//								}
								send(11, so);
//								System.out.println("Server sync: Sent arrow list");
								break;
							case 3: // Score
								break;
							default:
								System.out.println("Server: Got bad sync object type (" + syncType + ")");
						}
						so = null;
						break;

					case 13: // Place arrow
						System.out.println("Server: Received arrow request");
						Scanner syncSc = new Scanner((String) parameters);
						int arrowType = syncSc.nextInt();
						int x = syncSc.nextInt();
						int y = syncSc.nextInt();

						server.placeArrow(arrowType, x, y, player);
						break;
					default: //Unknown message recieved
						System.out.println("Server got bad input: " + type + ", ignoring and continue listening for command");
						break;
				}
			} catch (ClassNotFoundException ex) {
				Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
			} catch (IOException e) {
				//e.printStackTrace();
				System.out.println("Server: Error reading from player " + player.getName());
				System.out.println("Server: Terminating connection");
				this.listening = false;
			}
//			catch(EOFException ex) {
//				System.out.println("Server: Connection to client was ");
//			}
		}

		try {
			socket.close();
			System.out.println("Server successfully closed connection to player " + player.getName());
		} catch (Exception e) {
			System.out.println("Server could not close connection to " + player.getName());
		}
	}
}
