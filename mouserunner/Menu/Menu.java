package mouserunner.Menu;

import mouserunner.System.State;
import mouserunner.EventListeners.CommandListener;
import mouserunner.EventListeners.MenuKeyListener;
import mouserunner.Managers.ConfigManager;
import mouserunner.Managers.TextureManager;
import mouserunner.System.Command;
import mouserunner.EventListeners.MenuMouseListener;
import mouserunner.Menu.Components.Button;
import mouserunner.Menu.Components.MenuComponent;
import java.util.Collection;
import java.util.EventListener;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import mouserunner.EventListeners.MenuMouseMotionListener;
import mouserunner.Game.Camera;
import mouserunner.Game.Level;
import mouserunner.Game.Model;
import mouserunner.Managers.GameplayManager;
import mouserunner.Managers.ModelManager;

/**
 * A state that shows the main menu, also a superclass for the lobby
 * @author Zorek
 */
public class Menu implements State {

	protected GLU glu = new GLU();
	protected CommandListener commandListener;
	protected Command command;
	protected int menuPointer;
	private MenuState state;
	protected String backTexture;
	protected List<MenuComponent> menuComponents;
	public Lock menuComponentLock = new ReentrantLock();
	public Camera camera = new Camera(true);
	protected Model mulokModel = ModelManager.getInstance().getModel(new File("Assets/Models/AAMouse.ms3d"), false);
	protected Model canksModel = ModelManager.getInstance().getModel(new File("Assets/Models/AAMouse.ms3d"), false);


	/**
	 * Create the new menu state and resets menu pointer
	 */
	public Menu() {
		command = null;
		menuPointer = 0;
		menuComponents = new ArrayList<MenuComponent>();
		setState(MenuState.MAIN);
	}

	/**
	 * Moves the pointer down in the menu
	 */
	public void addMenuPointer() {
		if (menuPointer == menuComponents.size() - 1) {
			menuPointer = 0;
		} else {
			menuPointer++;
		}
		changeFocus();
	}

	/**
	 * Moves the pointer up in the menu
	 */
	public void subMenuPointer() {
		if (menuPointer == 0) {
			menuPointer = menuComponents.size() - 1;
		} else {
			menuPointer--;
		}
		changeFocus();
	}

	/**
	 * Gives the currently marked component the focus
	 */
	protected void changeFocus() {
		for (MenuComponent m : menuComponents) {
			m.setFocus(false);
		}
		menuComponents.get(menuPointer).setFocus(true);
	}

	/**
	 * Method used to register a mouse click to the menu state
	 * @param x the click position on the x axis
	 * @param y the click position on the inverted y axis
	 */
	public void registerMouseClick(int x, int y) {
		//Convert x and y axis from screen coords
		x=(int)(x*(800.0f/(float)ConfigManager.getInstance().width));
		y=(int)(y*(600.0f/(float)ConfigManager.getInstance().height));
		//Register click
		MenuComponent m = getComponentFromCoord(x, y);
		if (m != null) {
			changeFocus();
			m.activateComponent(x, y);
			executeEvent();
		}
	}

	/**
	 * Method used to register a mouse motion to the menu state
	 * @param x the mouse position on the x axis
	 * @param y the mouse position on the inverted y axis
	 */
	public void registerMouseMotion(int x, int y) {
		//Convert x and y axis from screen coords
		x=(int)(x*(800.0f/(float)ConfigManager.getInstance().width));
		y=(int)(y*(600.0f/(float)ConfigManager.getInstance().height));
		//Register mouse motion
		MenuComponent m = getComponentFromCoord(x, y);
		if (m != null) {
			menuPointer = menuComponents.indexOf(m);
			changeFocus();
		} else {
			for (MenuComponent mc : menuComponents) {
				mc.setFocus(false);
			}
		}
	}

	/**
	 * Recieves a component from the menuComponents-list based upon
	 * its position
	 * @param x the check position on the x axis
	 * @param y the check position on the inverted y axis
	 * @return the checked menu component (null if none is hit)
	 */
	protected MenuComponent getComponentFromCoord(int x, int y) {
		for (MenuComponent m : menuComponents) {
			if (x > m.x && x < m.x + m.width && y > m.y && y < m.y + m.height) {
				return m;
			}
		}
		return null;
	}

	/**
	 * Method used to register a key stroke of a typeable character from
	 * the keyboard.
	 * @param c the typed char
	 */
	public void registerKeyType(char c) {
		menuComponents.get(menuPointer).setCharInput(c);
	}

	/**
	 * The user wants to activate the object currently marked by the menu pointer
	 */
	public void executeEvent() {
		switch (state) {
			case MAIN:
				switch (menuPointer) {
					case 0: //New party game
						command = Command.GOTOLOBBY;
						break;
					case 1: //Tutorial
						GameplayManager.getInstance().reset();
						GameplayManager.getInstance().newGame("Classic.rls");
						GameplayManager.getInstance().loadDebugTournament();
						command = Command.NEWGAME;
						break;
					case 2: //Challange
						setState(MenuState.CHALLANGE);
						break;
					case 3: //Settings
						setState(MenuState.SETTINGS);
						break;
					case 4: //Intro
						ConfigManager.getInstance().skipIntro = false;
						command = Command.GOTOTITLE;
						break;
					case 5: //Credits
						command = Command.GOTOCREDITS;
						break;
					case 6: //Exit game
						System.out.println("---MouseRunner exited, user exited through the menu---");
						System.exit(0);
						break;
				}
				break;
			case CHALLANGE:
				setState(MenuState.MAIN);
				break;
			case SETTINGS:
				setState(MenuState.MAIN);
				break;
		}
		changeFocus();
	}

	/**
	 * Changes the menus state
	 * @param newState enum that tells the method what state to spawn
	 */
	private void setState(MenuState newState) {
		menuComponentLock.lock();
		menuPointer = 0;
		state = newState;
		menuComponents.clear();
		switch (newState) {
			case MAIN:
				backTexture = "MenuMain";
				menuComponents.add(new Button(25, 405, "Play"));
				menuComponents.add(new Button(25, 345, "Tutorial"));
				menuComponents.add(new Button(25, 285, "Challange mode"));
				menuComponents.add(new Button(25, 225, "Settings"));
				menuComponents.add(new Button(25, 165, "Intro"));
				menuComponents.add(new Button(25, 105, "Credits"));
				menuComponents.add(new Button(25, 45, "Exit game"));
				break;
			case CHALLANGE:
				backTexture = "MenuChallange";
				menuComponents.add(new Button(300, 310, "Back"));
				break;
			case SETTINGS:
				backTexture = "MenuSettings";
				menuComponents.add(new Button(300, 310, "Back"));
				break;
		}
		menuComponentLock.unlock();
		changeFocus();
	}

	/**
	 * Updates possible animation or motion on the menu
	 * @return always false
	 */
	@Override
	public boolean update() {
		if (command != null) {
			commandListener.commandPerformed(command);
		}
		return false;
	}

	/**
	 * Views the menu
	 * @param gl the current gl context
	 */
	@Override
	public void view(GL gl) {
		gl.glDisable(GL.GL_DEPTH_TEST);
		//Prepare menu to be drawn
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		glu.gluOrtho2D(0.0, 800, 0.0, 600);

		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();

		//Draw background
		Texture t = null;
		TextureCoords tc = null;
		t = TextureManager.getInstance().getTexture(new File("Assets/Textures/" + backTexture + ".png"));
		tc = t.getSubImageTexCoords(0, 0, 800, 600);
		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		t.enable();
		t.bind();
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(tc.left(), tc.bottom());
		gl.glVertex2i(0, 0);
		gl.glTexCoord2f(tc.right(), tc.bottom());
		gl.glVertex2i(800, 0);
		gl.glTexCoord2f(tc.right(), tc.top());
		gl.glVertex2i(800, 600);
		gl.glTexCoord2f(tc.left(), tc.top());
		gl.glVertex2i(0, 600);
		gl.glEnd();
		
		//Draw menu alternatives
		menuComponentLock.lock();
		for (MenuComponent m : menuComponents) {
			m.view(gl);
		}
		menuComponentLock.unlock();

		t.disable();
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPopMatrix();
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
		camera.setCamera(gl);
		gl.glTranslatef(Level.tileSize / 2 + Level.tileSize*8, -Level.tileSize / 2 - Level.tileSize*8,0.0f);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		mulokModel.generateDisplayList(gl);
	}

	/**
	 * Creates all event listeners that the menu uses
	 * @return A collection with all listeners used by Menu
	 */
	@Override
	public Collection<EventListener> getListeners() {
		Collection<EventListener> list = new ArrayList<EventListener>();
		CommandListener cl = new CommandListener();
		MenuKeyListener mkl = new MenuKeyListener(this);
		MenuMouseListener mml = new MenuMouseListener(this);
		MenuMouseMotionListener mmml = new MenuMouseMotionListener(this);
		list.add(cl);
		list.add(mkl);
		list.add(mml);
		list.add(mmml);
		return list;
	}

	/**
	 * Gives the commandlistener information of this session of menu
	 * @param cl the commandlistener that need the information
	 */
	@Override
	public void setCommandListener(CommandListener cl) {
		commandListener = cl;
	}

	@Override
	public String toString() {
		return "Menu";
	}
}

/**
 * Internal enum to control in which state the menu is currently in
 * @author Zorek
 */
enum MenuState {

	MAIN,
	CHALLANGE,
	SETTINGS
}

