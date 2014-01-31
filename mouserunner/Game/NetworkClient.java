package mouserunner.Game;

import mouserunner.Game.Entity.Mouse;
import mouserunner.Game.Entity.Cat;
import Server.Server;
import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import mouserunner.Managers.GameplayManager;
import mouserunner.Menu.Lobby;
import mouserunner.System.Direction;
import mouserunner.System.SyncObject;
import mouserunner.System.Timer;

/**
 * This class is the controller of a clients networking in game 
 * and in the lobby
 * @author Zorek
 */
public class NetworkClient {
	private Socket socket;
	private String name;
	private Lobby lobby;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private Sender sender;
	private boolean host;
	private Syncer syncer;
	
	private int debugInt = 0;
	
	// Game
	private Game game;
	private boolean gameReady;
	private long serverTimeStamp;

	/**
	 * Sets the Game instance, this needs to be set if the client wants to run a game.
	 * @param game
	 */
	public void setGame(Game game) {
		this.game = game;
	}
	
	public NetworkClient(String host, String name, Lobby lobby, boolean bhost) throws UnknownHostException {
		System.out.println("Client created");
		try {
			socket = new Socket(host, Server.port);
			System.out.println("Client connected to server");
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			System.out.println("Client could not read from network stream");
		}
		this.name = name;
		this.lobby = lobby;
		this.host = bhost;
		
		// Connect player
		sender = new Sender(out);
		sender.sendData(0, name);
		new Listener(in).start();
	}
	
	private NetworkClient getNetworkClient() {
		return this;
	}
	
	public void sendDisconnect() {
		sender.sendData(1);
	}
	
	public void sendChatMessage(String message) {
		sender.sendData(2, message);
	}
	
	public void sendChangeColor(Color color) {
		//sender.sendData("2 "+message);
	}
	
	public void sendChangeSetting(String setting) {
		//sender.sendData("2 "+message);
	}
	
	public void sendReady(boolean ready) {
		System.out.println("Client: Im ready");
		sender.sendData(5, ready);
	}
	
	public void sendStartGame() {
		sender.sendData(6);
	}
	
	public void sendSyncRequest(int type) {
		sender.sendData(11, type);
	}
	
	public boolean isGameReady() {
		return gameReady;
	}
	
	public long getServerTimeStamp() {
		return serverTimeStamp;
	}
	
	// Start and finish is used for debuging, they measure the time from that the
	// Client tries to place and the arrow is actually placed
	public long start,finish;
	public void sendArrowRequest(Direction dir, int x, int y) {
		start = Calendar.getInstance().getTimeInMillis();
		sender.sendData(13, Direction.dirTiInt(dir) + " " + x + " " +y);
	}
	
	public boolean isHost() {
		return this.host;
	}
	
	private class Listener extends Thread {

		private ObjectInputStream in;
		private boolean running;

		public Listener(ObjectInputStream in) {
			this.in = in;
			running = true;
		}

		@Override
		public void run() {
			while (running) {
				listen();
			}
			try {
				socket.close();
				System.out.println("Client successfully closed connection to server");
			} catch (IOException e) {
				System.out.println("Client could not close connection to server");
			}
			System.out.println("Client: Network client for " + name + " terminated");
		}
		
		@SuppressWarnings("unchecked") //Ignore the unchecked conversion warning in case 11
		private void listen() {
			try {
				// Waiting for new command
				int type = in.readInt();
				Object parameters = in.readObject();
//				Scanner sc = new Scanner((String)parameters);

				Player player;
				switch (type) {
					case 0: //A player connected
						Scanner playerScanner = new Scanner((String)parameters);
						String conName = playerScanner.next();
						//Add player to playerlist (the server does this if host)
						if (!host) {
							player = new Player(conName);
							GameplayManager.getInstance().players.add(player);
							GameplayManager.getInstance().ai.put(player, false);
							GameplayManager.getInstance().handicap.put(player, 0);
						}
						//Set the thisPlayer variable to assign control to the current player
						//if it is this player that has connected
						if (conName.equals(name)) {
							for (Player p : GameplayManager.getInstance().players) {
								if (p.getName().equals(name)) {
									GameplayManager.getInstance().thisPlayer = p;
								}
							}
						}
						//Regenerate the player list of the lobby
						lobby.generatePlayerList();
						System.out.println("Client: " + conName + " has connected");
						break;
					case 1: //A player has disconnected
						String disName = (String)parameters;
						//Update player list (the server controls this if host)
						if (!host) {
							player = new Player(disName);
							Iterator<Player> it = GameplayManager.getInstance().players.iterator();
							while (it.hasNext()) {
								if (it.next().equals(player)) {
									it.remove();
									GameplayManager.getInstance().ai.remove(player);
									GameplayManager.getInstance().handicap.remove(player);
									lobby.generatePlayerList();
									break;
								}
							}
						}

						if (disName.equals(name)) {
							//Server accepted disconnect
							lobby.exitGame();
							System.out.println("Client: Logging out");
							running = false;
						} else //Another player has logged off
						{
							System.out.println("Client: " + disName + " has logged out");
						}
						break;
					case 2: //Chat message recieved
						String message = (String)parameters;
						//Push message to chat window in lobby
						lobby.pushChat(message);
						System.out.println("Client<CHAT>: " + Calendar.getInstance().getTime().toString() + " " + message);
						break;
					case 3: //A player has changed color
						Scanner sc = new Scanner((String)parameters);
						System.out.println("Client: " + sc.next() + " has changed color to " + sc.next() + " " + sc.next() + " " + sc.next());
						lobby.generatePlayerList();
						break;
					case 4: //A player has changed a setting
						System.out.println("Client: Settings has been changed");
						break;
					case 5: //A player is set ready
						System.out.println("Client: " + ((String)parameters) + " is ready/unready");
						lobby.generatePlayerList();
						break;
					case 6: //A player has requested to start the game
						System.out.println("Client: Host has requested to initialize game");
						GameplayManager.getInstance().networkClient = getNetworkClient();
						Scanner sc2 = new Scanner((String)parameters);
						serverTimeStamp = sc2.nextLong();
						lobby.startGame();
						break;
					case 7: //Server has kicked or refused the player
						lobby.exitGame();
						System.out.println("Client: Server stopped, disconnecting");
						// Tells the ClientHandler to shutdown
						sender.sendData(7);
						running = false;
						break;

					case 10: // Start game
						System.out.println("Client: Server ordered a gamestart!");
						gameReady = true;
						// Starts the syncing
//						if(host) {
							syncer = new Syncer();
							syncer.start();
//						}
						break;
					case 11: // Sync
						TreeSet<SyncObject> inputSet = (TreeSet<SyncObject>) parameters;
						
						if(inputSet.size() > 0) {
							Class syncType = inputSet.first().type;
							// <editor-fold defaultstate="collapsed" desc=" Syncing ">
							if (syncType == Mouse.class) {
//								System.out.println("Client sync: Syncing mice");
								TreeSet<Mouse> mouseList = game.getMice();
								Iterator<Mouse> mi = mouseList.iterator();
								Iterator<SyncObject> so = inputSet.iterator();
								SyncObject s;
								Mouse m;
								boolean found;
								while (mi.hasNext()) {
									found = false;
									m = mi.next();
									so = inputSet.iterator();
									while (so.hasNext()) {
										s = so.next();
//										System.out.println("Client: Syncing mice s=" + s.id + " m=" + m.id);
										if (s.id == m.id) {
											m.synchronize(s);
											found = true;
											so.remove();
											break;
										}
									}
									// If the local mouse m's ID was not found in the synced list it should be removed
									if (!found) {
										System.out.println("Client: Killed mouse in sync with id " + m.id);
										m.kill(false);
									}
								}
								// If the server provied some Entites that were not in the loacl game, these are spawned
								Iterator<SyncObject> so2 = inputSet.iterator();
								while(so2.hasNext()) {
									s = so2.next();
									game.spawnEntity(s.type, s.x, s.y, Direction.intToDir(s.dir), s.id);
								}
							} else if(syncType == Cat.class) {
//								System.out.println("Client sync: Syncing cats");
								Iterator<Cat> ci = game.getCats().iterator();
								Iterator<SyncObject> so = inputSet.iterator();
								SyncObject s;
								Cat c;
								boolean found;
								while (ci.hasNext()) {
									found = false;
									c = ci.next();
									so = inputSet.iterator();
									while (so.hasNext()) {
										s = so.next();
										if (s.id == c.id) {
											c.synchronize(s);
											found = true;
											break;
										}
									}
									// If the local mouse m's ID was not found in the synced list it should be removed
									if (!found) {
										c.kill(false);
									}
								}
							} else if(syncType == Arrow.class) {
								System.out.println("Client sync: Syncing arrows");
							} else {
								System.out.println("Client sync: Unknown SyncObject type. Doing nothing");
							}
						}
						// </editor-fold>
						break;
					case 12: // Spawn Entity
						SyncObject syncedEntity = (SyncObject)parameters;
//						System.out.println("Client: spawns entity " + syncedEntity.getClass());
						game.spawnEntity(syncedEntity.type, syncedEntity.x, syncedEntity.y, Direction.intToDir(syncedEntity.dir), syncedEntity.id);
						break;
					case 13: // Place arrow
						finish = Calendar.getInstance().getTimeInMillis();
						System.out.println("Client: Place arrow, time on network was " + (finish - start) + " ms");
						Scanner sc3 = new Scanner((String)parameters);
						int arrowDir = sc3.nextInt();
						int x = sc3.nextInt();
						int y = sc3.nextInt();
						// Gets the player that placed the arrow
						String playerName = sc3.next();
						Player p = null;
						for (Player pl : GameplayManager.getInstance().players) {
							if (pl.getName().equals(playerName)) {
								p = pl;
							}
						}
						game.placeArrow(Direction.intToDir(arrowDir), x, y, p);
						break;
					default: //A command is not implemented yet
						System.err.println("Client: Unimplemented command recieved " + type);
						break;
				}
			} catch (ClassNotFoundException ex) {
				Logger.getLogger(NetworkClient.class.getName()).log(Level.SEVERE, null, ex);
			} catch (IOException e) {
				System.out.println("Client: Could not read from socket. Terminating connection");
				e.printStackTrace();
				try {
					in.close();
					this.running = false;
				} catch (IOException ex) {
					Logger.getLogger(NetworkClient.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
	}

	private class Sender {
		private ObjectOutputStream out;
		private ReentrantLock lock;

		public Sender(ObjectOutputStream out) {
			this.out = out;
			this.lock = new ReentrantLock(true);
		}
		
		/**
		 * Send a message to the server without any parameters
		 * @param type the type of the message
		 */
		public void sendData(int type) {
			sendData(type, "");
		}
		
		/**
		 * Semd a ,essage to the server with the given parametes. The class of the
		 * parameters is predefined by the message type, some excpect a String etc.
		 * @param type the type of the message
		 * @param userInput the parameters of the object.
		 */
		public void sendData(int type, Object userInput) {
			try {
				lock.lock();
				out.writeInt(type);
				out.writeObject(userInput);
				out.flush();
				out.reset();
			} catch (IOException ex) {
				System.out.println("Client: Failed to send message: " + userInput);
			} finally {
				lock.unlock();
			}
		}
	}
	
	/**
	 * This class sends sync requests.
	 */
	private class Syncer extends Thread {
		
		// Delays are used with sync intervals
		private long shortDelay = 50;
		// Some syncs happen more seldom, this variable sets the ratio between the sync modes
		private int longDelaySkips = 3;
		private Timer timer;
		private boolean running;
		
		public Syncer() {
			this.timer = new Timer();
			this.timer.setTimestamp();
			this.running = true;
		}
		
		public void stopSync() {
			this.running = false;
		}

		@Override
		public void run() {
			int skips = 0;
			while(running) {
				try {
					if(timer.read() > shortDelay) {
						// Request mouse sync
//						System.out.println("Client sync: Sent sync request, with timer at: " + timer.read());
						if(!game.getMice().isEmpty()) {
							sender.sendData(11, 0); // Mice
						}
						if(!game.getCats().isEmpty()) {
							sender.sendData(11, 1); // Cats
						}
						if(!game.getArrows().isEmpty()) {
							sender.sendData(11, 2); // Arrows
						}
						
						if(skips >= longDelaySkips) {
							// Sync scores and stuff
							sender.sendData(11, 3);
							skips = 0;
						}

						skips++;
						timer.setTimestamp();
					}
					
					// Sleep to reduce the load on the processor
					Thread.sleep(50);
				} catch(InterruptedException ex) {
					ex.printStackTrace();
				}
			}
		}
		
	}
}
