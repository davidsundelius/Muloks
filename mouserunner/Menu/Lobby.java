package mouserunner.Menu;

import LevelCreator.LevelManager;
import LevelCreator.RulesetManager;
import com.sun.opengl.util.j2d.TextRenderer;
import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;
import javax.media.opengl.GL;
import Server.Server;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import mouserunner.Game.NetworkClient;
import mouserunner.Managers.ConfigManager;
import mouserunner.Managers.FontManager;
import mouserunner.Managers.GameplayManager;
import mouserunner.Menu.Components.Button;
import mouserunner.Menu.Components.ChatTextbox;
import mouserunner.Menu.Components.ChatWindow;
import mouserunner.Menu.Components.List;
import mouserunner.Menu.Components.MapList;
import mouserunner.Menu.Components.MenuComponent;
import mouserunner.Menu.Components.PlayerList;
import mouserunner.Menu.Components.RulesetList;
import mouserunner.Menu.Components.SendButton;
import mouserunner.Menu.Components.Textbox;
import mouserunner.System.Command;

/**
 * 
 * @author Zorek
 */
public class Lobby extends Menu {
	public final static long serverRefreshDelay = 2000;
	
	//Lobby system
	boolean multicasting;
	private LobbyState state;
	private boolean host;
	private Server server;
	private NetworkClient client;
	private TextRenderer text;

	public Lobby() {
		host = false;
		command = null;
		client = null;
		server = null;
		menuPointer = 0;
		menuComponents = new ArrayList<MenuComponent>();
		multicasting=false;
		setState(LobbyState.SELECTLOBBY);
		text = new TextRenderer(FontManager.getInstance().getFont("Assets/Misc/Meow.ttf", Font.PLAIN, 16));
	}

	private void setState(LobbyState newState) {
		menuComponentLock.lock();
		menuPointer = 0;
		state = newState;
		menuComponents.clear();
		switch (newState) {
			case SELECTLOBBY:
				backTexture = "LobbySelect";
				menuComponents.add(new Button(637, 21, "Host game"));
				menuComponents.add(new Button(267, 308, "Join game"));
				menuComponents.add(new Button(267, 20, "Delete favorite"));
				menuComponents.add(new Textbox(561, 251, "", 15));
				menuComponents.add(new Textbox(561, 189, "", 15));
				menuComponents.add(new Button(561, 156, "Ädd to favorites"));
				menuComponents.add(new MapList(242, 59, 200, 240));
				menuComponents.add(new Textbox(207, 370, ConfigManager.getInstance().name, 7));
				menuComponents.add(new Button(26, 45, "Back to main menu"));
				multicasting=true;
				new ServerListRefresher().start();
				break;
			case CREATEGAME:
				multicasting=false;
				backTexture = "LobbyCreate";
				menuComponents.add(new Button(26, 105, "Create game"));
				menuComponents.add(new Textbox(207, 370, "", Server.SERVERNAMELENGTH));
				RulesetList newRList = new RulesetList(226, 98);
				newRList.addAll(RulesetManager.generateFileList());
				menuComponents.add(newRList);
				List newList = new List(374, 98);
				newList.addAll(LevelManager.generateFileList());
				newList.add("Random choose of level");
				menuComponents.add(newList);
				menuComponents.add(new Button(400, 45, "Ädd"));
				menuComponents.add(new Button(610, 45, "Remove"));
				menuComponents.add(new List(586, 98));		
				menuComponents.add(new Button(26, 45, "Back to game list"));
				break;
			case GAMELOBBY:
				multicasting=false;
				backTexture = "LobbyGame";
				menuComponents.add(new Button(26, 105, "Start game!"));
				menuComponents.add(new ChatWindow(226, 85));
				menuComponents.add(new ChatTextbox(226, 45, ""));
				menuComponents.add(new SendButton(500, 45, "Send"));
				menuComponents.add(new PlayerList(619, 45));
				menuComponents.add(new Button(26, 45, "Disconnect"));
				break;
		}
		menuComponentLock.unlock();
		changeFocus();
	}

	@Override
	public void executeEvent() {
		switch (state) {
			case SELECTLOBBY:
				Textbox t = (Textbox) menuComponents.get(7);
				ConfigManager.getInstance().name = t.getValue();
				switch (menuPointer) {
					case 0: //Host game
						ConfigManager.getInstance().saveSettings();
						host = true;
						GameplayManager.getInstance().reset();
						setState(LobbyState.CREATEGAME);
						break;
					case 1: //Join game
						ConfigManager.getInstance().saveSettings();
						host = false;
						GameplayManager.getInstance().reset();
						GameplayManager.getInstance().levels.add("the-complex.lvl");
						String listIp = ((MapList)menuComponents.get(6)).getValue();
						try {
							client = new NetworkClient(listIp, ConfigManager.getInstance().name, this, host);
						} catch (Exception e) {
							System.out.println("Client could not connect to server on: " + listIp);
							client = null;
							break;
						}
						setState(LobbyState.GAMELOBBY);
						break;
					case 2: //Delete favorite
						String s = ((MapList)menuComponents.get(6)).getValue();
						if(s!=null) {
							int index = ConfigManager.getInstance().favorites.indexOf(s)/2;
							ConfigManager.getInstance().favorites.remove(index);
							ConfigManager.getInstance().favorites.remove(index);
							MapList list = ((MapList)menuComponents.get(6));
							list.remove(index);
						}
						break;
					case 3: //Favorite IP
						break;
					case 4: //Favorite name
						break;
					case 5: //Add to favorites
						String ip = ((Textbox) menuComponents.get(3)).getValue();
						String name = ((Textbox) menuComponents.get(4)).getValue();
						ConfigManager.getInstance().favorites.add(name);
						ConfigManager.getInstance().favorites.add(ip);
						ConfigManager.getInstance().saveSettings();
						((MapList)menuComponents.get(6)).add(name, ip);
						break;
					case 6: //The game list
						break;
					case 7: //Name textbox
						break;
					case 8: //Back to main menu
						multicasting=false;
						ConfigManager.getInstance().saveSettings();
						command = Command.GOTOMENU;
						break;
				}
				break;
			case CREATEGAME:
				switch (menuPointer) {
					case 0: //Create game
						host = true;
						List rulesetList = (List)(menuComponents.get(2));
						List selectedLevelList = (List)(menuComponents.get(6));
						if(rulesetList.getValue()==null || selectedLevelList.size()==0 || ((Textbox)menuComponents.get(1)).getValue().equals("")) {
							System.out.println("Choose a ruleset, at least one level to play and a server name before starting the game");
							break;
						}
						try {
							server=new Server(((Textbox)menuComponents.get(1)).getValue());
						}catch(IOException e) {
							System.out.println("Could not host game, port is blocked\nServer stopped");
							server=null;
							setState(LobbyState.SELECTLOBBY);
							break;
						}
						try {
							client = new NetworkClient("127.0.0.1", ConfigManager.getInstance().name, this, host);
						} catch (Exception e) {
							System.out.println("Client could not connect to own server, network problem");
							client = null;
							server.shutdown();
							server=null;
							setState(LobbyState.SELECTLOBBY);
							break;
						}
						try {
							GameplayManager.getInstance().newGame(rulesetList.getValue());
						} catch(Exception e) {
							System.out.println("Could not load ruleset, syntax error, loading default");
							GameplayManager.getInstance().loadDefaultRuleset();
						}
						for(int i=0;i<selectedLevelList.size();i++) {
							String newLevel = selectedLevelList.get(i);
							if(newLevel.equals("Random choose of level"))
								GameplayManager.getInstance().levels.add(GameplayManager.getInstance().pickRandomLevel());
							else
								GameplayManager.getInstance().levels.add(newLevel);
						}
							
						
						setState(LobbyState.GAMELOBBY);
						break;
					case 1: //Server name textbox
						break;
					case 2: //Rulesetlist
						break;
					case 3: //All levels list
						break;
					case 4: //Button for inserting level
						String newEntry = ((List)(menuComponents.get(3))).getValue();
						if(newEntry!=null) {
							if(((List)(menuComponents.get(6))).size()!=5)
								((List)(menuComponents.get(6))).add(newEntry);
							else
								System.out.println("List is full, a tournament has a maximum of five games");
						} else
							System.out.println("Could not add any item to the list, select one in the list first");
						break;
					case 5: //Button for removing level
						((List)(menuComponents.get(6))).remove();
						break;
					case 6: //Selected levels list
						break;
					case 7: //Back to select lobby
						setState(LobbyState.SELECTLOBBY);
						break;
				}
				break;
			case GAMELOBBY:
				switch (menuPointer) {
					case 0: //Start game
						if(host)
							client.sendStartGame();
						break;
					case 1: //Chat window
						break;
					case 2: //Chat textbox
						ChatTextbox tb = (ChatTextbox)menuComponents.get(2);
						client.sendChatMessage(tb.getValue());
						tb.clear();
						break;
					case 3: //Chat send button
						tb = (ChatTextbox)menuComponents.get(2);
						client.sendChatMessage(tb.getValue());
						tb.clear();
						break;
					case 4: //Player list
						break;
					case 5: //Back to select lobby
						setState(LobbyState.SELECTLOBBY);
						if (host) {
							server.shutdown();
							server = null;
						} else {
							client.sendDisconnect();
						}
						client = null;
						break;
				}
		}
		changeFocus();
	}

	@Override
	public void view(GL gl) {
		super.view(gl);
		if(state==LobbyState.CREATEGAME||state==LobbyState.GAMELOBBY) {
			text.beginRendering(800, 600);
			text.draw(GameplayManager.getInstance().getRulesetName(), 95, 441);
			text.endRendering();
		}
		if (client != null) {
			text.beginRendering(800, 600);
			text.setColor(Color.WHITE);
			text.draw("Connected to IP adress: " + Server.getIP(), 480, 400);
			//Print text in the right menu of the GameLobby
			for(int i=0;i<GameplayManager.getInstance().levels.size();i++)
				text.draw(GameplayManager.getInstance().levels.get(i), 40, 230-i*19);
			text.endRendering();
		}
		if (server != null) {
			text.beginRendering(800, 600);
			text.setColor(Color.WHITE);
			text.draw("Server up is listening for connections", 480, 380);
			text.endRendering();
		}
	}
	
	private void generateGameList() throws BindException {
		if(state==LobbyState.SELECTLOBBY) {
			Map<String,String> map = getAvailableServers();
			menuComponentLock.lock();
			MapList list;
			try {
				 list = (MapList)menuComponents.get(6);
			}catch(Exception e) {
				//The lock was requested before a statechange, this will occur only
				//if the gamelist is not available
				menuComponentLock.unlock();
				return;
			}
			list.clear();
			for(int i=0;i<ConfigManager.getInstance().favorites.size();i+=2)
				list.add(ConfigManager.getInstance().favorites.get(i),ConfigManager.getInstance().favorites.get(i+1));
			list.addAll(map);
			menuComponentLock.unlock();
		}
	}
	
	public void generatePlayerList() throws BindException {
		if(state==LobbyState.GAMELOBBY) {
			menuComponentLock.lock();
			PlayerList list;
			try {
				list = (PlayerList)menuComponents.get(4);
			}catch(Exception e) {
				//The lock was requested before a statechange, this will occur only
				//if the gamelist is not available
				menuComponentLock.unlock();
				return;
			}
			list.clear();
			list.addAll(GameplayManager.getInstance().players);
			menuComponentLock.unlock();
		}
	}

	/**
	 * This method searches the local network for Mouserunner servers and returns
	 * a list of all servers found.
	 * Note that this method will block while searching for Servers, if there are several servers
	 * this method might run for several seconds.
	 * @return a List of avaliable servers
	 */
	private Map<String,String> getAvailableServers() throws BindException  {
		Map<String,String> map = new HashMap<String,String>();
		MulticastSocket socket=null;
		try {
			byte[] data = new byte[0];

			// Send a multicast message asking for servers to identify themselfs
			DatagramPacket packet;
			socket = new MulticastSocket();
			packet = new DatagramPacket(data, data.length, InetAddress.getByName(Server.multicastGroup), Server.multiPort);
			socket.setTimeToLive(1);
			socket.send(packet);

			// Receive responses from servers
			DatagramSocket responseSocket = new DatagramSocket(Server.multiResponse);
			responseSocket.setSoTimeout(Server.SOCKETTIMEOUT);
			byte[] msg = new byte[Server.SERVERNAMELENGTH];
			DatagramPacket responsePacket = new DatagramPacket(msg, msg.length);

			// This try-catch assumse that when  a timeout occur there is no more servers
			try {
				while (true) {
					responseSocket.receive(responsePacket);
					map.put(new String(msg).trim(),responsePacket.getAddress().getHostAddress());
				}
			} catch (SocketTimeoutException e) {
				// Catches this exception because the program should just keep going.
				//System.out.println("The client didn't get any information from the server");
			} finally {
				responseSocket.close();
			}
		} catch (IOException e) {
			if(e.getClass()==BindException.class)
				throw (BindException)e;
			e.printStackTrace();
		} finally {
			socket.close();
		}
		return map;
	}
	
	public void pushChat(String message) {
		if(state==LobbyState.GAMELOBBY) {
			menuComponentLock.lock();
			ChatWindow c = (ChatWindow)menuComponents.get(1);
			c.addMessage(message);
			menuComponentLock.unlock();
		}
	}
	
	public void startGame() {
		command = Command.NEWGAME;
	}
	
	public void exitGame() {
		if(host&&server!=null) {
			server.shutdown();
			server=null;
		}
	}
	
	/**
	 * This class updates the game list every x millisecond.
	 * 
	 * Editors note. This could be done with a java Timer object, might be changed to this
	 * in the future
	 */
	private class ServerListRefresher extends Thread {

		@Override
		public void run() {
			System.out.println("Client started the multicasting");
			while(multicasting)
				try {
					generateGameList();
				} catch (BindException e) {
					System.out.println("Could not start listening, retrying...");
				} finally {
					try {
						Thread.sleep(serverRefreshDelay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			System.out.println("Client stopped the multicasting");
		}
		
	}
	
	@Override
	public String toString() {
		return "Lobby";
	}
}

enum LobbyState {
	
	SELECTLOBBY,
	CREATEGAME,
	GAMELOBBY
}
