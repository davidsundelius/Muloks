package mouserunner;

import LevelCreator.LevelCreator;
import Server.Server;
import Server.ServerDebugger;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.IOException;
import javax.media.opengl.GLCanvas;
import mouserunner.Menu.Title;
import mouserunner.System.Viewer;
import mouserunner.System.Updater;
import mouserunner.Managers.ConfigManager;
import mouserunner.System.Logger;
import mouserunner.System.State;
import static javax.swing.JOptionPane.showMessageDialog;

/**
 * MouseRunners mainclass initializes opengl, starts the first state
 * and puts it into the state manager. Also creates the controller
 * and viewer classes
 * @author Zorek
 */
public class MouseRunner {

	private boolean runGame;
	private Viewer viewer;
	private Updater updater;

	/**
	 * Internal constructor called from static context to initialize and run
	 * the game
	 * @param args the arguments passed on by the user from terminal
	 */
	private MouseRunner(String[] args) {
		System.out.println("---Starting initialization of project MouseRunner---");
		runGame = true;
		parseFlags(args);
		if (runGame) {
			initialize();
			run();
		}
	}

	/**
	 * Parses the flags sent to program by command line
	 * @param args the arguments passed on by the user from terminal
	 */
	private void parseFlags(String[] args) {
		for (String flag : args) {
			//Enter debug mode
			if (flag.equals("-debug") || flag.equals("-d")) {
				ConfigManager.getInstance().debug = true;
			} //Start the editor
			else if ((flag.equals("-editor") || flag.equals("-e")) && runGame) {
				System.out.println("Mouserunner is starting program in level editor mode");
				runGame = false;
				new LevelCreator();
			} //Start the server
			else if ((flag.equals("-server") || flag.equals("-s")) && runGame) {
				System.out.println("Mouserunner is starting program in dedicated server mode");
				runGame = false;
				try {
					new Server("Dedicated Server");
				} catch (IOException e) {
					System.out.println("Dedicated server could not be started, port is blocked. Exiting game.");
				}
			} //Start client for server debugging
			else if ((flag.equals("-serverd") || flag.equals("-sd")) && runGame) {
				System.out.println("Mouserunner is starting program server debugging mode");
				runGame = false;
				new ServerDebugger();
			} // Recreates all current levels
			else if ((flag.equals("-recreateLevel") || flag.equals("-rcl")) && runGame) {
				runGame = false;
				mouserunner.Game.Level.recreateLevels();
			}
		}
	}

	/**
	 * Initializes the game (at title screen)
	 */
	private void initialize() {
		//Starts up the logger (if not debug mode, then use the ordanary System.out and System.err)
		if (!ConfigManager.getInstance().debug) {
			try {
				Logger log = new Logger();
				System.setOut(log);
				System.setErr(log);
			} catch (IOException e) {
				showMessageDialog(null, "Cannot create log-file, set right permissions. Deactivates logging.");
				System.setOut(null);
				System.setErr(null);
			}
		}
		//Creates a awt frame with a glcanvas attached, sets size and cursor
		if (ConfigManager.getInstance().debug) {
			System.out.println("Mouserunner is starting program in debug mode");
		} else {
			System.out.println("---Starting initialization of project MouseRunner---");
			System.out.println("Mouserunner is starting program in release mode");
		}
		Frame frame = new Frame("Muloks");


		try {
			GLCanvas canvas = new GLCanvas();
			canvas.setSize(ConfigManager.getInstance().width - 2, ConfigManager.getInstance().height - 2);
			changeCursor("Assets/Textures/cursor.png", canvas);
			frame.setIconImage(Toolkit.getDefaultToolkit().getImage("Assets/Textures/Icon.png"));
			frame.add(canvas);
			frame.pack();
			frame.setResizable(false);
			//Creates initial state
			State startState = new Title();
			updater = new Updater(frame, canvas, 1000);
			updater.setState(startState);
			viewer = new Viewer(canvas, 100);
			canvas.addGLEventListener(viewer);
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
			canvas.requestFocus();
			((Title)startState).startIntro();
		} catch (UnsatisfiedLinkError e) {
			System.out.println("Mouserunner is missing neccecary libraries for JOGL (Java OpenGL). Exiting");
			showMessageDialog(null,"Muloks is missing required libraries. Exiting.");
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("Mouserunner has created window and initialized state successfully");
	}

	/**
	 * Sets the AWT cursor to a custom cursor
	 */
	private void changeCursor(String path, Component c) {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Image image = toolkit.getImage(path);
		// Sets the point of the cursor
		Point hotSpot = new Point(0, 7);
		Cursor cursor = toolkit.createCustomCursor(image, hotSpot, "catHand");
		c.setCursor(cursor);
	}

	/**
	 * Starts the updaters main loop
	 */
	private void run() {
		while (true) {
			updater.update();
		}

	}

	/**
	 * Called from JRE when program is started
	 */
	public static void main(String[] args) {
		new MouseRunner(args);
	}
}

