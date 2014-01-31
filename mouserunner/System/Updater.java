package mouserunner.System;

import mouserunner.Managers.StateManager;
import mouserunner.Game.Game;
import java.awt.Frame;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.EventListener;
import java.util.HashSet;
import javax.media.opengl.GLCanvas;
import mouserunner.EventListeners.CommandListener;

/**
 * A updater class - updates all the active objects at the current state
 * also keeps track of the gamewindow and eventlisteners
 * @author Zorek
 */
public class Updater {

	private Frame frame;
	private GLCanvas canvas;
	private Collection<EventListener> eventListeners = new HashSet<EventListener>();
	private boolean running;
	private float interval;

	/**
	 * Creates a new updater
	 * @param frame a initialized frame
	 * @param canvas a initialized GLcanvas that will be used to view the states
	 * @param tps the amount of ticks per second that will update the state
	 */
	public Updater(Frame frame, GLCanvas canvas, int tps) {
		this.frame = frame;
		this.canvas = canvas;
		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						System.out.println("---MouseRunner exited, user closed the main window---");
						System.exit(0);
					}
				}).start();
			}
		});
		setTps(tps);
		running = true;
	}

	/**
	 * Starts the update loop (infinite)
	 */
	public void update() {
		while (running) {
			int start = (int) (System.nanoTime() / 1000000);

			StateManager.getInstance().getState().update();

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

	/**
	 * Sets a new state
	 */
	public void setState(State newState) {
		System.out.println("Updater recieved order to change state to: " + newState.getClass().getName());
		destroyListeners();
		frame.setTitle("Muloks - " + newState.toString());
		setTps(1000);
		setRunning(true);
		StateManager.getInstance().setState(newState);
		createListeners();
		if (newState instanceof Game) {
			((Game) newState).initializeGame();
		}
		System.out.println("Updater completed state change without any trouble");
	}

	/**
	 * Creates all listeners requested from the current state and adds them to
	 * the Listenerlist
	 */
	private void createListeners() {
		eventListeners.addAll(StateManager.getInstance().getState().getListeners());
		for (EventListener e : eventListeners) {
			if (e instanceof KeyListener) {
				canvas.addKeyListener((KeyListener) e);
			} else if (e instanceof MouseMotionListener) {
				canvas.addMouseMotionListener((MouseMotionListener) e);
			} else if (e instanceof MouseListener) {
				canvas.addMouseListener((MouseListener) e);
			} else if (e instanceof CommandListener) {
				CommandListener cl = (CommandListener) e;
				cl.setUpdater(this);
				StateManager.getInstance().getState().setCommandListener(cl);
			} else {
				System.err.println("Warning: Unimplemented listener listed in Updater");
			}
		}
	}

	/**
	 * Destroys all listeners of the current state (use before creating a new state)
	 */
	private void destroyListeners() {
		for (EventListener e : eventListeners) {
			if (e instanceof KeyListener) {
				canvas.removeKeyListener((KeyListener) e);
			} else if (e instanceof MouseMotionListener) {
				canvas.removeMouseMotionListener((MouseMotionListener) e);
			} else if (e instanceof MouseListener) {
				canvas.removeMouseListener((MouseListener) e);
			} else if (e instanceof CommandListener); //No need to do anything here yet
			else {
				System.err.println("Warning: Unimplemented listener removed from Updater");
			}
		}
		eventListeners.clear();
	}

	/**
	 * Makes it possible to pause the updater
	 */
	public void setRunning(boolean running) {
		this.running = running;
	}

	/**
	 * Reassigns the "ticks per second" variable to the new value
	 * @param tps the new tps
	 */
	public void setTps(int tps) {
		interval = 1000 / tps;
	}
}

