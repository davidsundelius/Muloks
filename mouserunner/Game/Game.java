package mouserunner.Game;

import mouserunner.Game.Entity.GoldenMouse;
import mouserunner.Game.Entity.PowerupMouse;
import mouserunner.Game.Entity.Mouse;
import mouserunner.Game.Entity.AgentMouse;
import mouserunner.Game.Entity.Cat;
import mouserunner.Game.Entity.KamikazeMouse;
import mouserunner.Game.Entity.TacticalMouse;
import LevelCreator.MapGenerator;
import mouserunner.LevelComponents.Nest;
import mouserunner.LevelComponents.Tile;
import mouserunner.LevelComponents.EmptyTile;
import mouserunner.System.Command;
import mouserunner.System.SyncObject;
import mouserunner.System.Viewable;
import mouserunner.System.State;
import mouserunner.System.Updatable;
import mouserunner.System.Direction;
import mouserunner.System.Timer;
import mouserunner.Managers.GameplayManager;
import mouserunner.Managers.SoundManager;
import mouserunner.Managers.ConfigManager;
import mouserunner.EventListeners.CommandListener;
import mouserunner.EventListeners.GameKeyListener;
import mouserunner.EventListeners.GameMouseMotionListener;
import mouserunner.EventListeners.GameMouseListener;
import mouserunner.AI.AIAdv;
import mouserunner.Poweups.CanksAirstrike;
import mouserunner.Poweups.CanksAmbush;
import mouserunner.Poweups.MulokRetreat;
import mouserunner.Poweups.FavouredSpacecraft;
import mouserunner.Poweups.Rethink;
import mouserunner.Poweups.Powerup;
import mouserunner.Poweups.Rotate;
import mouserunner.Poweups.SlowDown;
import mouserunner.Poweups.SpeedUp;
import mouserunner.Poweups.BackupPlan;
import mouserunner.Poweups.SneakyCanks;
import Server.Server;
import Server.ServerCommandListener;
import java.nio.IntBuffer;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import com.sun.opengl.util.BufferUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Calendar;
import java.util.Random;
import java.io.IOException;
import java.util.TreeSet;
import mouserunner.Game.Entity.Entity;
import mouserunner.Game.Entity.Position;
import mouserunner.LevelComponents.Portal;

/**
 * A class used to create and play the game
 * @author Zorek
 */
public class Game implements State {
	//<editor-fold defaultstate="collapsed" desc=" Declarations ">
	//Gameplay stuff
	private TreeSet<Mouse> mice;
	private HashSet<Cat> cats;
	private HashSet<Arrow> arrows;
	private HashSet<Powerup> powerups;
	private HashSet<Billboard> billboards;
	AIAdv ais;
	//private Collection<Updatable> traps;
	private Collection<Viewable> viewables = new HashSet<Viewable>();
	private Collection<Mouse> spawnedMice = new TreeSet<Mouse>();
	private Collection<Cat> spawnedCats = new HashSet<Cat>();
	private UI ui;
	private Skyplane skyplane;
	private Level level;
	private Camera camera;
	private CommandListener commandListener;
	private Timer gameTimer,  spawnTimer;
	private Direction newArrowDir;
	//Picking stuff
	private int[] mousePos = {0, 0};
	private int renderMode;
	private IntBuffer selectionBuffer;
	//Gamesystem stuff
	private boolean pause;
	private Random randomGenerator;
	private boolean gameOver;
	private boolean nextGame;
	private boolean isSpawning;
	private Lock viewLock = new ReentrantLock();
	// This variable should be one of the private updater classes
	private Updatable gameUpdater;	// Network
	private Server server;
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc=" Constructor & initialize ">
	/**
	 * Constructs a new game, spawns all game objects and prepare variables.
	 */
	public Game(Server server) {
		//Sets initial values of variables used in the game
		gameTimer = new Timer();
		spawnTimer = new Timer();
		GameplayManager.getInstance().gameTimer = null;
		randomGenerator = new Random(Calendar.getInstance().getTimeInMillis());

		//Updatables
		mice = new TreeSet<Mouse>();
		cats = new HashSet<Cat>();
		arrows = new HashSet<Arrow>();
		powerups = new HashSet<Powerup>();
		billboards = new HashSet<Billboard>();
		

		//Create new UI
		ui = new UI(this);
		//Creates new camera
		camera = new Camera(false);
		//Set game state variables
		pause = false;
		isSpawning = true;
		gameOver = false;
		nextGame = false;
		//Sort playerlist
		for (Player p : GameplayManager.getInstance().players) {
			p.newGame();
		}
		GameplayManager.getInstance().sortPlayerList();

		// Network
		this.server = server;

		// Select the type of the Game
		if (this.server != null) {
			// If multiplayer game
			System.out.println("Game instance created by server");
			this.gameUpdater = new ServerUpdater();
			CommandListener cl = new ServerCommandListener(server);
			setCommandListener(cl);
		} else {
			if (GameplayManager.getInstance().networkClient == null) {
				System.out.println("Game instance created for a single player game");
				this.gameUpdater = new SingleUpdater();
			} else {
				System.out.println("Game instance created for a network game");
				this.gameUpdater = new ClientUpdater();
				GameplayManager.getInstance().networkClient.setGame(this);
			}
		}

		try {
			//Loads debug level
//			level = Level.LoadLevel("Assets/Levels/the-complex.lvl");

			//Loads level according to rules
			String newLevel = GameplayManager.getInstance().getLevel();
			if (newLevel.equals("the-random.lvl")) {
				MapGenerator mg = new MapGenerator();
				mg.MapAlgorithm(16, 12, GameplayManager.getInstance().players.size(), true, 0, false);
			}
			System.out.println("Game tries to load level: " + newLevel);
			level = Level.LoadLevel(newLevel);
			System.out.println("Game loaded " + newLevel + " successfully");

			//Initiate AI
			List<Player> aiPlayerList = new ArrayList<Player>();
			String dp = "Game reports these players as AI: ";
			for(Player p: GameplayManager.getInstance().players) {
				if((boolean)GameplayManager.getInstance().ai.get(p)) {
					aiPlayerList.add(p);
					dp+=(p.getName() + ", ");
				}
			}
			System.out.println(dp);

			ais = new AIAdv(level,cats,mice,aiPlayerList,arrows,Level.tileSize);
			//Checks should have been made earlier that the amount of players are the same as 
			//the number of nests
			Iterator<Player> playerIt = GameplayManager.getInstance().players.iterator();
			Iterator<Nest> nests = level.getNests().iterator();
			Nest n;
			while (playerIt.hasNext() && nests.hasNext()) {
				n = nests.next();
				n.setOwner(playerIt.next());
			}
			while (nests.hasNext()) {
				level.removeNest(nests.next());			 
			}
		
			//Make all portals get portal effects of them
			if(ConfigManager.getInstance().sfx) {
				try {
					SpecialFX sfx;
					for(Portal p: level.getPortals()) {
						sfx = new SpecialFX("Assets/Scripts/Portal.sfx", p.x*Level.tileSize+Level.tileSize/2, -p.y*Level.tileSize-Level.tileSize/2, 5.0f);
						billboards.add(sfx);
						viewables.add(sfx);
					}
				} catch(IOException e) {
					System.out.println("Game could not load the portal sfx, ignoring this");
				}
			}
			
			//Make all arrows listen
			ArrayList<Arrow> arrowlist = level.getArrows();
			arrows.addAll(arrowlist);

			//Create new skybox
			skyplane = new Skyplane(level.skyplaneTexPath, camera);

			//Debug mice
			List<Mouse> lm = new ArrayList<Mouse>();
			for (int i = 0; i < 0; i++) {
				lm.add(new Mouse(i % 15, i % 11, Direction.RIGHT, level));
			}
			viewables.addAll(lm);
			mice.addAll(lm);

			//Debug cats
			List<Cat> lc = new ArrayList<Cat>();
			for (int i = 0; i < 0; i++) {
				lc.add(new Cat((i + 2) % 15, i % 11, Direction.LEFT, level, this.mice));
			}
			viewables.addAll(lc);
			cats.addAll(lc);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		//Initialize picking
		selectionBuffer = BufferUtil.newIntBuffer(10);
		renderMode = GL.GL_RENDER;
	}

	/**
	 * This method is called when a new game is loaded and
	 * ready to start. 
	 */
	public void initializeGame() {
		System.out.println("Game is starting initialization with server: " + server);
		ui.showMessage(level.introText, 5000, false);
		camera.update();
		Timer startTimer = new Timer();
		try {
			while (startTimer.read() < 5000) {
				Thread.sleep(50);
				camera.updateZoom();
			}

			if (GameplayManager.getInstance().networkClient != null) {
				GameplayManager.getInstance().networkClient.sendReady(true);
				ui.showMessage("Waiting for players", 1000, true);
				while (!GameplayManager.getInstance().networkClient.isGameReady()) {
					Thread.sleep(50);
				}
			}
			ui.clearMessage();
			ui.showMessage("Start!", 1000, false);
			//Play sounds
			SoundManager.getInstance().playSound(new File("Assets/Sound/Start.ogg"), false);
			SoundManager.getInstance().playSound(new File("Assets/Sound/Theme.ogg"), true);
			gameTimer.setTimestamp();
			GameplayManager.getInstance().gameTimer = gameTimer;
			ui.showGUI(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}//</editor-fold>
	
	//<editor-fold defaultstate="collapsed" desc=" Update & view ">
	/**
	 * Update all game objects
	 * @return always false
	 */
	@Override
	public boolean update() {
		return gameUpdater.update();
	}

	/**
	 * Views the game (and if renderMode is GL_SELECT: performs picking)
	 * @param gl the current gl context
	 */
	@Override
	public void view(GL gl) {
		if (renderMode == gl.GL_RENDER) {
			gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
			gl.glLoadIdentity();
			skyplane.view(gl);		//View skyplane
			camera.setCamera(gl); //Set camera
			level.view(gl);       //View level
			viewLock.lock();
			for (Viewable v : viewables) {
				v.view(gl);         //View units
			}
			for(Billboard b: billboards) {
				b.view(gl);
			}
			viewLock.unlock();
			ui.view(gl);          //View UI
		} else {
			//Player has clicked on the screen, perform picking
			GLU glu = new GLU();
			int[] viewport = new int[4];
			int hits = 0;
			//Create a buffer for the clicked objects
			gl.glSelectBuffer(selectionBuffer.capacity(), selectionBuffer);
			gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
			//Start picking rendering mode
			gl.glRenderMode(GL.GL_SELECT);
			//Start clear the namelist and input dummy value for objects to overwrite
			gl.glInitNames();
			gl.glPushName(999);

			//Sets the projection options including a multiply with the PickMatrix
			gl.glMatrixMode(GL.GL_PROJECTION);
			gl.glPushMatrix();
			gl.glLoadIdentity();
			glu.gluPickMatrix(mousePos[0], viewport[3] - mousePos[1], 1, 1, viewport, 0);
			glu.gluPerspective(45.0f, (float) viewport[2] / (float) viewport[3], 1.0, 1000.0);

			//Render objects that can be picked (using the camera to get the right view matrix)
			gl.glMatrixMode(GL.GL_MODELVIEW);
			gl.glLoadIdentity();
			camera.setCamera(gl);
			level.viewPicking(gl);

			//Restore projection matrix and flush gl pipeline
			gl.glMatrixMode(GL.GL_PROJECTION);
			gl.glPopMatrix();
			gl.glFlush();

			//Restore render mode, matrixmode and registrating hits
			hits = gl.glRenderMode(GL.GL_RENDER);
			renderMode = GL.GL_RENDER;
			gl.glMatrixMode(GL.GL_MODELVIEW);

			//Handling hitted objects
			int numNames;
			int mark = 0;
			for (int i = 0; i < hits; i++) {
				numNames = selectionBuffer.get(mark); //How many hitted objects?
				mark += 3; //Skip worthless information
				for (int j = 0; j < numNames; j++) {
					//For all hitted objects, we perform something
					int x = selectionBuffer.get(mark);
					int y = x / 16;
					x = x % 16;
					if (GameplayManager.getInstance().networkClient == null) {
						placeArrow(newArrowDir, x, y, GameplayManager.getInstance().thisPlayer);
					} else {
						GameplayManager.getInstance().networkClient.sendArrowRequest(newArrowDir, x, y);
					}
					mark += 2; //Skip more worthless information
				}
			}
			//Render ordenary view to screen
			view(gl);
		}
	}//</editor-fold>
	
	//<editor-fold defaultstate="collapsed" desc=" Spawning methods ">
	/**
	 * Places an arrow in the playing field. If there are an arrow at the given tile false is returned and no arrow is placed.
	 * @param dir
	 * @param x
	 * @param y
	 * @param player
	 * @return is the arrow placed successfully?
	 */
	public boolean placeArrow(Direction dir, int x, int y, Player player) {
		if (player == null) {
			throw new IllegalArgumentException("No player given");
		}
		Tile tile = level.getTile(x, y);
		if (tile.getClass() == EmptyTile.class) {
			if (!((EmptyTile) tile).hasArrow()) {
				((EmptyTile) tile).setArrow(dir, player);
				return true;
			}
		}
		return false;
	}

	/**
	 * Spawns an entity of the given class at the given position with the given direction and id
	 * @param type The class type of the Entity
	 * @param x the x position of the object
	 * @param y the y position of the object
	 * @param dir the direction of the Entity
	 * @param id the id of the Entity, this is used for syncing the Entity
	 * @return The entity that has been spawned
	 */
	public Entity spawnEntity(Class type, float x, float y, Direction dir, int id) {
		if (type == Mouse.class) {
			Mouse m = new Mouse(x, y, dir, level, id);
			spawnedMice.add(m);
			return m;
		} else if (type == Cat.class) {
			Cat cat = new Cat(x, y, dir, level, mice);
			spawnedCats.add(cat);
			return cat;
		} else if (type == GoldenMouse.class) {
			GoldenMouse gm = new GoldenMouse(x, y, dir, level);
			spawnedMice.add(gm);
			return gm;
		} else if (type == PowerupMouse.class) {
			PowerupMouse pm = new PowerupMouse(x, y, dir, level);
			spawnedMice.add(pm);
			return pm;
		} else {
			throw new IllegalArgumentException("The given type (" + type + ") is not a Entity type.");
		}
	}

	/**
	 * Spawns a entity at the given spawn point of the given type, see type list,
	 * with the given direction
	 * 
	 * 0 - Mouse
	 * 1 - Cat
	 * 2 - Golden mouse
	 * 3 - Powerup mouse
	 * 4 - Kamikaze mouse
	 * 5 - Tactical mouse
	 * 6 - Agent mouse
	 * 
	 * @param type gives the type of entity that will be spawned
	 * @param x The x coordinate of the spawn
	 * @param y The y coordinate of the spawn
	 * @param dir the direction of the new entity
	 */
	public Entity spawnEntity(int type, int x, int y, Direction dir) {
		switch (type) {
			case 1:
				Cat cat = new Cat(x, y, dir, level, mice);
				spawnedCats.add(cat);
				return cat;
			case 2:
				GoldenMouse gm = new GoldenMouse(x, y, dir, level);
				spawnedMice.add(gm);
				return gm;
			case 3:
				PowerupMouse pm = new PowerupMouse(x, y, dir, level);
				spawnedMice.add(pm);
				return pm;
			case 4:
				KamikazeMouse km = new KamikazeMouse(x, y, dir, level, cats);
				spawnedMice.add(km);
				return km;
			case 5:
				TacticalMouse tm = new TacticalMouse(x, y, dir, level);
				spawnedMice.add(tm);
				return tm;
			case 6:
				AgentMouse am = new AgentMouse(x, y, dir, level);
				spawnedMice.add(am);
				return am;
			default:
				Mouse m = new Mouse(x, y, dir, level);
				spawnedMice.add(m);
				return m;
		}
	}

	/**
	 * Spawns a entity at the given spawn point of the given type, see type list,
	 * with a random direction
	 * 
	 * 0 - Mouse
	 * 1 - Cat
	 * 2 - Golden mouse
	 * 3 - Powerup mouse
	 * 4 - Kamikaze mouse
	 * 5 - Tactical mouse
	 * 6 - Agent mouse
	 * 
	 * @param type the type of the new entity
	 * @param x The x coordinate of the spawn
	 * @param y The y coordinate of the spawn
	 * @return the start direction of the spawned entity
	 */
	public Entity spawnEntity(int type, int x, int y) {
		int iDir = randomGenerator.nextInt(4);
		Direction[] dirs = Direction.values();
		Direction dir = dirs[iDir];
		return spawnEntity(type, x, y, dir);
	}

	/**
	 * Spawns a entity at a random spawn point of the given type, see type list
	 * 
	 * 0 - Mouse
	 * 1 - Cat
	 * 2 - Golden mouse
	 * 3 - Powerup mouse
	 * 4 - Kamikaze mouse
	 * 5 - Tactical mouse
	 * 6 - Agent mouse
	 * 
	 * @param type The type to spawn
	 * @return The Point where the spawn point is located
	 */
	public Entity spawnEntity(int type) {
		int index = randomGenerator.nextInt(level.getSpawnPoints().length);
		Point spawnPoint = level.getSpawnPoints()[index];
		return spawnEntity(type, spawnPoint.x, spawnPoint.y);
	}

	/**
	 * Spawns a billboard on the given position with a texture and a text.
	 * If text is null, assuming its a ghost and set the texturePath accordingly
	 * @param x the position on the x axis for the billboard
	 * @param y the position on the y axis for the billboard
	 * @param z the position on the y axis for the billboard
	 * @param texturePath the text that will be printed on the billboard
	 * @param text The text printed on the new billboard
	 */
	public void spawnBillboard(final float x, final float y, final float z, String texturePath, final String text) {
		if (text == null)
			texturePath = "Assets/Textures/MouseGhost.png";
		viewLock.lock();
		billboards.add(new Billboard(texturePath, text, x, -y, z));
		viewLock.unlock();
	}
	
	/** Spawns a special effect on the given position with the properties specified
	 * in the sfxfile.
	 * @param sfxfile a path to the file that will be used to create the specialfx
	 * @param x the position on the x axis for the effect
	 * @param y the position on the y axis for the effect
	 */
	public void spawnSpecialFX(final String sfxfile, final float x, final float y) {
		if(ConfigManager.getInstance().sfx) {
			System.out.println("Game: SpecialFX spawned: " + sfxfile);
			SpecialFX newSpecialFX;
			try {
				newSpecialFX = new SpecialFX(sfxfile, x, -y, 0.0f);
			} catch(IOException e) {
				System.out.println("SpecialFX could not read the effect on path: " + sfxfile);
				return;
			}

			viewLock.lock();
			billboards.add(newSpecialFX);
			viewables.add(newSpecialFX);
			viewLock.unlock();
		}
	}

	
	/**
	 * Will fire a powerup onto the game at random
	 * @param p the player that got the powerup
	 */
	private void spawnPowerup(Player p){
		Spinner spinner = new Spinner();
		ui.showPowerupSpinner(spinner);
		gameTimer.pause();
		while(!spinner.update()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		gameTimer.unPause();
		int powerupIndex = spinner.getPowerup();
		System.out.println("Powerup " + Spinner.getPowerupName(powerupIndex) + " recieved by " + p.getName());
		//ui.showMessage(Spinner.getPowerupName(powerupIndex), 1000, false);
		switch (powerupIndex) {
			case 1:
				powerups.add(new CanksAmbush(this));
				break;
			case 2:
				powerups.add(new SlowDown(commandListener));
				break;
			case 3:
				powerups.add(new SpeedUp(commandListener));
				break;
			case 4:
				powerups.add(new FavouredSpacecraft(p, this));
				break;
			case 5:
				powerups.add(new CanksAirstrike(p, spawnedCats, level, this));
				break;
			case 6:
				powerups.add(new Rotate(camera));
				break;
			case 7:
				for (Arrow a : level.getArrows()) {
					if (a.getOwner() != null) {
						a.deactivate();
					}
				}
				powerups.add(new Rethink());
				break;
			case 8:
				powerups.add(new SneakyCanks(cats));
				break;
			case 9:
				powerups.add(new BackupPlan());
				break;
			default:
				powerups.add(new MulokRetreat(this));
				break;
		}
	}//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc=" Listener handlers ">
	/**
	 * Creates all eventlisteners that the game uses
	 * @return A collection with all listeners necessary to play the game
	 */
	@Override
	public Collection<EventListener> getListeners() {
		//Create a list for listeners
		Collection<EventListener> list = new ArrayList<EventListener>();
		CommandListener cl;
		cl = new CommandListener();
		cl.setGame(this);
		GameKeyListener dkl = new GameKeyListener(this);
		GameMouseListener gml = new GameMouseListener(this);
		GameMouseMotionListener gmml = new GameMouseMotionListener(this);
		list.add(cl);
		list.add(dkl);
		list.add(gml);
		list.add(gmml);
		return list;
	}

	/**
	 * Connects a commandlistener to the game
	 * @param cl the wanted commandlistener object
	 */
	@Override
	public void setCommandListener(CommandListener cl) {
		commandListener = cl;
	}// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc=" Registers for events ">
	/**
	 * Registers keypress
	 * @param keyCode the keycode of the pressed key
	 */
	public void registerKeyState(int keyCode, boolean isDown) {
		if (isDown) {
			if (GameplayManager.getInstance().gameTimer != null) {
				if (gameOver) {
					nextGame = true;
				} else {
					if (!pause) {
						//Put arrow keys
						if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP) {
							renderMode = GL.GL_SELECT;
							newArrowDir = Direction.UP;
						} else if (keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN) {
							renderMode = GL.GL_SELECT;
							newArrowDir = Direction.DOWN;
						} else if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT) {
							renderMode = GL.GL_SELECT;
							newArrowDir = Direction.LEFT;
						} else if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) {
							renderMode = GL.GL_SELECT;
							newArrowDir = Direction.RIGHT;
						//Show scoreboard
						} else if (keyCode == KeyEvent.VK_SPACE) {
							ui.showScoreBoard(true);
						}
					}
					//Pause
					if (keyCode == KeyEvent.VK_PAUSE) {
						if (!gameTimer.isPaused()) {
							registerCommand(Command.PAUSE);
						} else {
							registerCommand(Command.UNPAUSE);
						}
					}
					//Debug
					if (ConfigManager.getInstance().debug) {
						//Control camera
						if (keyCode == KeyEvent.VK_UP) {
							camera.setDirection(Direction.UP);
						} else if (keyCode == KeyEvent.VK_DOWN) {
							camera.setDirection(Direction.DOWN);
						} else if (keyCode == KeyEvent.VK_LEFT) {
							camera.setDirection(Direction.LEFT);
						} else if (keyCode == KeyEvent.VK_RIGHT) {
							camera.setDirection(Direction.RIGHT);
						//Spawn entitys
						} else if (keyCode == KeyEvent.VK_1) {
							spawnEntity(0);
						} else if (keyCode == KeyEvent.VK_2) {
							spawnEntity(1);
						} else if (keyCode == KeyEvent.VK_3) {
							spawnEntity(2);
						} else if (keyCode == KeyEvent.VK_4) {
							spawnEntity(3);
						} else if (keyCode == KeyEvent.VK_5) {
							spawnEntity(4);
						} else if (keyCode == KeyEvent.VK_6) {
							spawnEntity(5);
						} else if (keyCode == KeyEvent.VK_7) {
							spawnEntity(6);
						//Control game state
						} else if (keyCode == KeyEvent.VK_F1) {
							commandListener.commandPerformed(Command.GOTOTITLE);
						} else if (keyCode == KeyEvent.VK_F2) {
							commandListener.commandPerformed(Command.GOTOMENU);
						} else if (keyCode == KeyEvent.VK_F3) {
							commandListener.commandPerformed(Command.GOTOLOBBY);
						} else if (keyCode == KeyEvent.VK_F4) {
							commandListener.commandPerformed(Command.ENDTOURNAMENT);
						//Simulate catch of entitys
						} else if (keyCode == KeyEvent.VK_M) {
							GameplayManager.getInstance().thisPlayer.mouseGet();
						} else if (keyCode == KeyEvent.VK_C) {
							GameplayManager.getInstance().thisPlayer.catGet();
						} else if (keyCode == KeyEvent.VK_G) {
							GameplayManager.getInstance().thisPlayer.goldenMouseGet();
						} else if (keyCode == KeyEvent.VK_P) {
							spawnPowerup(GameplayManager.getInstance().thisPlayer);
							GameplayManager.getInstance().thisPlayer.powerupMouseGet();
						}										
					}
				}
			}
			//Exit the game
			if (keyCode == KeyEvent.VK_ESCAPE) {
				if (ConfigManager.getInstance().debug) {
					System.exit(0);
				} else if (GameplayManager.getInstance().gameTimer != null) {
					if (pause) {
						registerCommand(Command.UNPAUSE);
					} else {
						registerCommand(Command.PAUSE);
					}
				}
			}

		} else {
			if (keyCode == KeyEvent.VK_SPACE) {
				ui.showScoreBoard(false);
			}
		}
	}

	/**
	 * Registers the mouse motion to the game object
	 * @param x the coordinate of the cursor on the x axis
	 * @param y the coordinate of the cursor on the y axis
	 */
	public void registerMouseMotion(int x, int y) {
		if (pause) {
			ui.registerMouseMotion(x, ConfigManager.getInstance().height - y);
		}
		mousePos[0] = x;
		mousePos[1] = y;
	}

	/**
	 * Registers the mouse click to the gameobject,
	 * used for menus and next game if the game is over
	 * @param x the coordinate of the cursor on the x axis
	 * @param y the coordinate of the cursor on the y axis
	 */
	public void registerMouseClick(int x, int y) {
		if (gameOver) {
			nextGame = true;
		} else if (pause) {
			ui.registerMouseClick(x, ConfigManager.getInstance().height - y);
		}
	}

	/**
	 * Registers the command sent to the game
	 * @param c the command that will be registered
	 */
	public void registerCommand(Command c) {
		commandListener.commandPerformed(c);
		if (c == Command.PAUSE) {
			// If single player
			if (GameplayManager.getInstance().networkClient == null) {
				ui.showMessage("The game is paused", 1000, true);
			}
			ui.showGUI(false);
			pause = true;
		} else {
			ui.clearMessage();
			ui.showGUI(true);
			pause = false;
		}
	}//</editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc=" Getters ">
	public TreeSet<Mouse> getMice() {
		return this.mice;
	}

	public HashSet<Cat> getCats() {
		return this.cats;
	}

	public HashSet<Arrow> getArrows() {
		return this.arrows;
	}
	
	public Timer getGameTimer() {
		return gameTimer;
	}// </editor-fold>
	
	//<editor-fold defaultstate="collapsed" desc=" Setters ">
	public void setGameTimer(Timer timer) {
		this.gameTimer = timer;
	}
	
	/**
	 * Turns on the regualar spawning procidure
	 * @param spawning if true, the entities spawn according to ruleset
	 */
	public void setSpawning(boolean spawning) {
		isSpawning = spawning;
	}//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc=" Common updater methods ">
	/**
	 * Is called as the update method for single and client games when the game is over
	 */
	public void gameOver() {
		level.getLeaderNest().blastUpdate();
		SpecialFX rocketFire=null;
		Iterator<Billboard> bit = billboards.iterator();
		while(bit.hasNext()) {
			Billboard b = bit.next();
			if(b.getClass()==SpecialFX.class && ((SpecialFX)b).isEffect("Assets/Scripts/RocketFire.sfx")) {
				rocketFire = (SpecialFX)b;
				break;
			}
		}
		if(rocketFire!=null) {
			rocketFire.move(0.0f, 0.0f, 0.01f);
			rocketFire.update();
		}
		//If the game shall start the next game of the tournament
		if (nextGame) {
			//Jump to next game
			SoundManager.getInstance().stopSounds();
			commandListener.commandPerformed(Command.NEXTGAME);
		}
	}

	/**
	 * Returns the caption that this state wants to put into the caption of the 
	 * window
	 * @return "Local game" or "Network game"
	 */
	@Override
	public String toString() {
		if (GameplayManager.getInstance().networkClient == null) {
			return "Local game";
		} else {
			return "Network game";
		}
	}//</editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc=" Server updater class ">
	private class ServerUpdater implements Updatable {

		@Override
		public boolean update() {
			if (gameOver) {
				if (nextGame) {
					commandListener.commandPerformed(Command.NEXTGAME);
					nextGame = false;
				}
			} else {
				if (GameplayManager.getInstance().isGameOver()) {
					gameOver = true;
					nextGame = true;
					return false;
				}
				if (isSpawning) {
					if (spawnTimer.read() > 500) {
						Point[] spawns = level.getSpawnPoints();
						SyncObject s;
						Entity e;
						for (Point spawn : spawns) {
							if (randomGenerator.nextFloat() < GameplayManager.getInstance().mouseSpawningRate &&
											mice.size() < 3) {
								e = spawnEntity(0, spawn.x, spawn.y, Direction.RIGHT);
								server.broadcastMessage(12, e.getSyncable());
							} else if (randomGenerator.nextFloat() < GameplayManager.getInstance().catSpawningRate &&
											cats.size() < GameplayManager.getInstance().maxNumberCats) {
//								e = spawnEntity(1, spawn.x, spawn.y,Direction.RIGHT);
//								server.broadcastMessage(12, e.getSyncable());
							} else if (randomGenerator.nextFloat() < GameplayManager.getInstance().goldenSpawningRate) {
//								e = spawnEntity(2, spawn.x, spawn.y,Direction.RIGHT);
//								server.broadcastMessage(12, e.getSyncable());
							} else if (randomGenerator.nextFloat() < GameplayManager.getInstance().powerupSpawningRate) {
//								e = spawnEntity(3, spawn.x, spawn.y,Direction.RIGHT);
//								server.broadcastMessage(12, e.getSyncable());
							}
						}
						spawnTimer.setTimestamp();
					}
				}

				//Updates all updatables registered to the list
				// Mouse
				Iterator<Mouse> it = mice.iterator();
				Mouse m;
				while (it.hasNext()) {
					m = it.next();
					if (m.update()) {
						// If the mouse should be removed but is not dead he has entered a nest
						if (!m.isDead()) {
							Position mousePosition = m.getPosition();
							Nest nest = (Nest) level.getTile(mousePosition.getXTilePosition(), mousePosition.getYTilePosition());

							if (m.getClass() == GoldenMouse.class) {
								nest.getOwner().goldenMouseGet();
							} else if (m.getClass() == PowerupMouse.class) {
								nest.getOwner().powerupMouseGet();
								spawnPowerup(nest.getOwner());
							} else {
								nest.getOwner().mouseGet();
							}
						}
//						System.out.println("## Server: Mouse got killed at (" + m.x + "," + m.y + ")");
//						System.out.println("## Server: Mouse killed on tile " + level.getTile((int) (m.x / Level.tileSize), (int) (m.y / Level.tileSize)).getClass());
						it.remove();
						viewLock.lock();
						viewables.remove(m);
						viewLock.unlock();
					}
				}

				// Cat
				Iterator<Cat> catIt = cats.iterator();
				Cat c;
				while (catIt.hasNext()) {
					c = catIt.next();
					if (c.update()) {
						// If the cat should be removed but is not dead he has entered a nest
						if (!c.isDead()) {
							Position catPosition = c.getPosition();
							Nest nest = (Nest) level.getTile(catPosition.getXTilePosition(), catPosition.getYTilePosition());
							nest.getOwner().catGet();
						}
						catIt.remove();
						viewLock.lock();
						viewables.remove(c);
						viewLock.unlock();
					} // c.update()
				}

				// Arrow
				for (Arrow a : arrows) {
					a.update();
				}

				// Powerups
				for (Powerup pp : powerups) {
					pp.update();
				}

			
				

				cats.addAll(spawnedCats);
				mice.addAll(spawnedMice);
				spawnedCats.clear();
				spawnedMice.clear();
				// AIs
				ais.update();
			}
			return false;
		}
	}// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc=" Client updater class ">
	private class ClientUpdater implements Updatable {

		@Override
		public boolean update() {
			if (gameOver) {
				gameOver();
			} else {
				if (GameplayManager.getInstance().isGameOver()) {
					camera.setSpin(true);
					SoundManager.getInstance().stopSounds();
					SoundManager.getInstance().playSound(new File("Assets/Sound/Winner.ogg"), true);
					ui.showMessage(GameplayManager.getInstance().players.get(0).getName() + " is the winner!", 3000, true);
					gameOver = true;
					return false;
				}

				//Updates all updatables registered to the list
				// Mouse
				Iterator<Mouse> it = mice.iterator();
				Mouse m;
				while (it.hasNext()) {
					m = it.next();
					if (m.update()) {
						// If the mouse should be removed but is not dead he has entered a nest
						// The host doesn't need to run this because the server already has
						if (!m.isDead() && !(GameplayManager.getInstance().networkClient.isHost())) {
							Position mousePosition = m.getPosition();
							Nest nest = (Nest) level.getTile(mousePosition.getXTilePosition(), mousePosition.getYTilePosition());

							if (m.getClass() == GoldenMouse.class) {
								nest.getOwner().goldenMouseGet();
							} else if (m.getClass() == PowerupMouse.class) {
								nest.getOwner().powerupMouseGet();
								spawnPowerup(nest.getOwner());
							} else {
								nest.getOwner().mouseGet();
							}
						} else {
							spawnBillboard(m.getX(), m.getY(), 5.0f, null, null);
						}

						it.remove();
						viewLock.lock();
						viewables.remove(m);
						viewLock.unlock();
					} // m.update()
				}

				// Cat
				Iterator<Cat> catIt = cats.iterator();
				Cat c;
				while (catIt.hasNext()) {
					c = catIt.next();
					if (c.update()) {
						// If the cat should be removed but is not dead he has entered a nest
						if (!c.isDead()) {
							Position catPosition = c.getPosition();
							Nest nest = (Nest) level.getTile(catPosition.getXTilePosition(), catPosition.getYTilePosition());
							nest.getOwner().catGet();
							spawnSpecialFX("Assets/Scripts/Explosion.sfx",nest.x*Level.tileSize+Level.tileSize/2, nest.y*Level.tileSize+Level.tileSize/2);
						}
						catIt.remove();
						viewLock.lock();
						viewables.remove(c);
						viewLock.unlock();
					} // c.update()
				}

				// Arrow
				for (Arrow a : arrows) {
					a.update();
				}

				// Powerups
				for (Powerup pp : powerups) {
					pp.update();
				}

				
				cats.addAll(spawnedCats);
				mice.addAll(spawnedMice);
				viewLock.lock();
				viewables.addAll(cats);
				viewables.addAll(mice);
				viewLock.unlock();
				spawnedCats.clear();
				spawnedMice.clear();
				// AIs
				ais.update();
			}

			camera.update();
			return false;
		}
	}//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc=" Single player updater class">
	private class SingleUpdater implements Updatable {

		@Override
		public boolean update() {
			if (gameOver) {
				gameOver();
			} else {
				if (GameplayManager.getInstance().isGameOver()) {
					camera.setSpin(true);
					SoundManager.getInstance().stopSounds();
					SoundManager.getInstance().playSound(new File("Assets/Sound/Winner.ogg"), true);
					ui.showMessage(GameplayManager.getInstance().players.get(0).getName() + " is the winner!", 3000, true);
					gameOver = true;
					Nest winnersNest = level.getLeaderNest();
					try {
						Billboard rocketFire = new SpecialFX("Assets/Scripts/RocketFire.sfx", winnersNest.x*Level.tileSize+Level.tileSize/2, -winnersNest.y*Level.tileSize-Level.tileSize/2, 0.0f);
						billboards.add(rocketFire);
						viewLock.lock();
						viewables.add(rocketFire);
						viewLock.unlock();
					}	catch(IOException e) {
						System.err.println("Game could not read the Assets/Scripts/RocketFire.sfx, ignoring execution of fire.");
					}
					return false;
				}
				// Spawns entities if there are any spawn points
				if (isSpawning) {
					if (spawnTimer.read() > 500) {
						Point[] spawns = level.getSpawnPoints();
						for (Point spawn : spawns) {
							if (randomGenerator.nextFloat() < GameplayManager.getInstance().mouseSpawningRate) {
								spawnEntity(0, spawn.x, spawn.y);
							} else if (randomGenerator.nextFloat() < GameplayManager.getInstance().catSpawningRate &&
											cats.size() < GameplayManager.getInstance().maxNumberCats) {
								spawnEntity(1, spawn.x, spawn.y);
							} else if (randomGenerator.nextFloat() < GameplayManager.getInstance().goldenSpawningRate) {
								spawnEntity(2, spawn.x, spawn.y);
							} else if (randomGenerator.nextFloat() < GameplayManager.getInstance().powerupSpawningRate) {
								spawnEntity(3, spawn.x, spawn.y);
							} else if (randomGenerator.nextFloat() < GameplayManager.getInstance().kamikazeSpawningRate) {
								spawnEntity(4, spawn.x, spawn.y);
							} else if (randomGenerator.nextFloat() < GameplayManager.getInstance().tacticalSpawningRate) {
								spawnEntity(5, spawn.x, spawn.y);
							} else if (randomGenerator.nextFloat() < GameplayManager.getInstance().agentSpawningRate) {
								spawnEntity(6, spawn.x, spawn.y);
							}
						}
						spawnTimer.setTimestamp();
					}
				}

				//Updates all updatables registered to the list
				// Mouse
				Iterator<Mouse> mouseIt = mice.iterator();
				Mouse m;
				while (mouseIt.hasNext()) {
					m = mouseIt.next();
					if (m.update()) {
						// If the mouse should be removed but is not dead it has entered a nest
						if (!m.isDead()) {
							Position mousePosition = m.getPosition();
							Nest nest = (Nest) level.getTile(mousePosition.getXTilePosition(), mousePosition.getYTilePosition());

							if (m.getClass() == GoldenMouse.class) {
								nest.getOwner().goldenMouseGet();
								spawnBillboard(m.getX(), m.getY(), 10.0f, "Assets/Textures/SignPositive.png", "+"+GameplayManager.getInstance().goldenValue);
							} else if (m.getClass() == PowerupMouse.class) {
								nest.getOwner().powerupMouseGet();
								spawnPowerup(nest.getOwner());
							} else if (m.getClass() == KamikazeMouse.class) {
								nest.getOwner().kamikazeMouseGet();
							} else if (m.getClass() == TacticalMouse.class) {
								nest.getOwner().tacticalMouseGet();
							} else if (m.getClass() == AgentMouse.class) {
								int loseScore = nest.getOwner().agentMouseGet();
								spawnBillboard(m.getX(), m.getY()-7.0f, 10.0f,  "Assets/Textures/SignNegative.png", "-"+String.valueOf(loseScore));
								spawnSpecialFX("Assets/Scripts/Explosion.sfx",nest.x*Level.tileSize+Level.tileSize/2, nest.y*Level.tileSize+Level.tileSize/2);
							} else {
								nest.getOwner().mouseGet();
							}
						} else {
							if(m.getClass() == KamikazeMouse.class) {
								KamikazeMouse km = (KamikazeMouse)m;
								Cat c = km.getVictim();
								if(c!=null) {
									spawnSpecialFX("Assets/Scripts/Explosion.sfx",c.getX(), c.getY());
								}
							}
							spawnBillboard(m.getX(), m.getY(), 5.0f, null, null);
						}

						mouseIt.remove();
						viewLock.lock();
						viewables.remove(m);
						viewLock.unlock();
					}
				}

				// Cat
				Iterator<Cat> catIt = cats.iterator();
				Cat c;
				while (catIt.hasNext()) {
					c = catIt.next();
					if (c.update()) {
						// If the cat should be removed but is not dead he has entered a nest
						if (!c.isDead()) {
							Position catPosition = c.getPosition();
							Nest nest = (Nest) level.getTile(catPosition.getXTilePosition(), catPosition.getYTilePosition());
							int loseScore = nest.getOwner().catGet();
							spawnBillboard(c.getX(), c.getY()-7.0f, 10.0f, "Assets/Textures/SignNegative.png", String.valueOf(loseScore));
							spawnSpecialFX("Assets/Scripts/explosion.sfx",nest.x*Level.tileSize+Level.tileSize/2, nest.y*Level.tileSize+Level.tileSize/2);
						}
						catIt.remove();
						viewLock.lock();
						viewables.remove(c);
						viewLock.unlock();
					}
				}

				// Arrow
				for (Arrow a : arrows) {
					a.update();
				}

				// Powerups
				Iterator<Powerup> pit = powerups.iterator();
				while (pit.hasNext()) {
					Powerup pp = pit.next();
					if (pp.update()) {
						pit.remove();
					}
				}

				// Billboards
				Iterator<Billboard> bit = billboards.iterator();
				while (bit.hasNext()) {
					Billboard b = bit.next();
					if (b.update()) {
						bit.remove();
						viewLock.lock();
						billboards.remove(b);
						viewLock.unlock();
					}
				}
				
				// AIs
			  ais.update();
				
				cats.addAll(spawnedCats);
				mice.addAll(spawnedMice);
				viewLock.lock();
				viewables.addAll(cats);
				viewables.addAll(mice);
				viewLock.unlock();
				spawnedCats.clear();
				spawnedMice.clear();
			}

			camera.update();
			return false;
		}
	}// </editor-fold>
}
