package mouserunner.Game;

import mouserunner.System.Timer;
import mouserunner.System.Viewable;
import mouserunner.Managers.GameplayManager;
import mouserunner.Managers.TextureManager;
import mouserunner.Managers.FontManager;
import com.sun.opengl.util.j2d.TextRenderer;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import mouserunner.Managers.ConfigManager;
import mouserunner.Menu.Components.Button;
import mouserunner.Menu.Components.MenuComponent;
import mouserunner.System.Command;

/**
 * Draws the UI over the gamescreen. Also includes methods for fading messages
 * and a scoreboard
 * @author Zorek
 */
public class UI implements Viewable {
	//Textrenderers
	private TextRenderer uiText;
	private TextRenderer scoreText;
	private TextRenderer messageText;	//HUD system
	private int width;
	private int height;
	private boolean showGUI;
	private boolean showScoreBoard;
	private Spinner powerupSpinner;
	private final SimpleDateFormat df = new SimpleDateFormat("mm:ss");
	private Game game;
	private Timer fadeTimer;	//Screen center message
	private String currentMessage;
	private Timer messageTimer;
	private float maxTime;
	private boolean messageLoop;	//Game menu
	private List<MenuComponent> menuComponents;

	/**
	 * Constructs new gui, initializes fontengine and creates new fonts
	 */
	public UI(Game game) {
		this.game = game;
		uiText = new TextRenderer(FontManager.getInstance().getFont("Assets/Misc/Meow.ttf", Font.PLAIN, 16));
		scoreText = new TextRenderer(FontManager.getInstance().getFont("Assets/Misc/Meow.ttf", Font.BOLD, 72));
		messageText = new TextRenderer(FontManager.getInstance().getFont("Assets/Misc/Meow.ttf", Font.BOLD, 24));
		messageText.setSmoothing(true);
		width = 800;
		height = 600;
		fadeTimer = new Timer();
		currentMessage = null;
		messageLoop = false;
		messageTimer = new Timer();
		showGUI = false;
		showScoreBoard = false;
		powerupSpinner = null;
		menuComponents = new ArrayList<MenuComponent>();
		menuComponents.add(new Button(325, 182, "Back to game"));
		menuComponents.add(new Button(325, 137, "Exit to main menu"));
	}

	/**
	 * Method called to view the gui on the screen
	 * @param gl The current gl context
	 */
	@Override
	public void view(GL gl) {
		GLU glu = new GLU();

		//Set viewing mode to GUI rendering
		gl.glMatrixMode(gl.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		glu.gluOrtho2D(0.0, 800.0, 0.0, 600.0);
		gl.glMatrixMode(gl.GL_MODELVIEW);
		gl.glLoadIdentity();

		//Calculate alphamask for paused mode and intro
		float alpha = 1.0f;
		if (!showGUI) {
			if (fadeTimer.isPaused()) {
				alpha = 0.6f;
			} else {
				alpha = (float) fadeTimer.read() / 1000.0f;
				if (alpha > 0.6f) {
					fadeTimer.pause();
				}
			}
		} else {
			if (fadeTimer.isPaused()) {
				alpha = 0.0f;
			} else {
				alpha = 0.6f - (float) fadeTimer.read() / 1000.0f;
				if (alpha < 0.0f) {
					fadeTimer.pause();
				}
			}
		}
		//Draw alpha mask
		gl.glColor4f(0.0f, 0.0f, 0.0f, alpha);
		gl.glBegin(gl.GL_QUADS);
		gl.glVertex2i(0, 0);
		gl.glVertex2i(width, 0);
		gl.glVertex2i(width, height);
		gl.glVertex2i(0, height);
		gl.glEnd();


		if (showGUI) {
			viewTopUI(gl);
			viewBottomLeftUI(gl, true);
			viewBottomRightUI(gl);
			viewBottomLeftUI(gl, false);
			if(powerupSpinner!=null) {
				powerupSpinner.view(gl);
				if(powerupSpinner.isDone())
					powerupSpinner=null;
			}else if (showScoreBoard) {
				viewScoreboard(gl);
			}
			
		} else {
			if (GameplayManager.getInstance().gameTimer != null) {
				viewMenu(gl);
			}
		}
		gl.glMatrixMode(gl.GL_PROJECTION);
		gl.glPopMatrix();
		gl.glMatrixMode(gl.GL_MODELVIEW);

		//Center text message display
		if (currentMessage != null) {
			float time = messageTimer.read();
			if (time < maxTime) {
				printMessage(time);
			} else {
				if (messageLoop) {
					messageTimer.setTimestamp();
					time = 0.0f;
					printMessage(time);
				} else {
					clearMessage();
				}
			}
		}
	}
	
	//<editor-fold defaultstate="collapsed" desc=" Viewers ">
	private void viewTopUI(GL gl) {
		//The background of the top UI
		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		Texture t = TextureManager.getInstance().getTexture(new File("Assets/Textures/GuiTop.png"));
		TextureCoords tc = t.getSubImageTexCoords(0, 0, 800, 31);
		t.enable();
		t.bind();
		gl.glBegin(gl.GL_QUADS);
		gl.glTexCoord2d(tc.left(), tc.bottom());
		gl.glVertex2i(0, height - 31);
		gl.glTexCoord2d(tc.right(), tc.bottom());
		gl.glVertex2i(width, height - 31);
		gl.glTexCoord2d(tc.right(), tc.top());
		gl.glVertex2i(width, height);
		gl.glTexCoord2d(tc.left(), tc.top());
		gl.glVertex2i(0, height);
		gl.glEnd();

		//Cat and mouse time counter hider
		int offset = 500;
		if (GameplayManager.getInstance().gameTimer != null && GameplayManager.getInstance().time != -1 && GameplayManager.getInstance().gameTimer.read() < GameplayManager.getInstance().time - 1000) {
			offset = (int) (500.0f * ((float) GameplayManager.getInstance().gameTimer.read() / (float) (GameplayManager.getInstance().time - 1000)));
		}
		t = TextureManager.getInstance().getTexture(new File("Assets/Textures/GuiHide.png"));
		t.bind();
		gl.glBegin(gl.GL_QUADS);
		gl.glTexCoord2f(0.0f, 0.0f);
		gl.glVertex2i(width - 165 - offset, height);
		gl.glTexCoord2f(1.0f, 0.0f);
		gl.glVertex2i(width - 120, height);
		gl.glTexCoord2f(1.0f, 1.0f);
		gl.glVertex2i(width - 120, height - 32);
		gl.glTexCoord2f(0.0f, 1.0f);
		gl.glVertex2i(width - 165 - offset, height - 32);
		gl.glEnd();

		//Cat and mouse time counter
		t = TextureManager.getInstance().getTexture(new File("Assets/Textures/GuiCatAndMouse.png"));
		tc = t.getSubImageTexCoords(0, 0, 45, 20);
		t.bind();
		gl.glBegin(gl.GL_QUADS);
		gl.glTexCoord2d(tc.left(), tc.bottom());
		gl.glVertex2i(width - 165 - offset, height - 25);
		gl.glTexCoord2d(tc.right(), tc.bottom());
		gl.glVertex2i(width - 120 - offset, height - 25);
		gl.glTexCoord2d(tc.right(), tc.top());
		gl.glVertex2i(width - 120 - offset, height - 5);
		gl.glTexCoord2d(tc.left(), tc.top());
		gl.glVertex2i(width - 165 - offset, height - 5);
		gl.glEnd();
		t.disable();

		//Text rendering on the top ui
		uiText.beginRendering(width,height);
		uiText.setColor(Color.BLACK);
		if (GameplayManager.getInstance().time == -1) {
			uiText.draw("∞", 52, height - 20);
		} else if (GameplayManager.getInstance().gameTimer != null && GameplayManager.getInstance().time - GameplayManager.getInstance().gameTimer.read() > 1000) {
			uiText.draw(df.format(new Date(GameplayManager.getInstance().time - GameplayManager.getInstance().gameTimer.read())), 40, height - 20);
		} else {
			uiText.draw("Out!", 45, height - 20);
		}
		uiText.endRendering();
	}

	private void viewBottomLeftUI(GL gl, boolean firstPart) {
		Player player = GameplayManager.getInstance().thisPlayer;
		if (firstPart) {
			gl.glColor4f(player.getColor().getRed() / 255, player.getColor().getGreen() / 255, player.getColor().getBlue() / 255, 1.0f);
			Texture t = TextureManager.getInstance().getTexture(new File("Assets/Textures/GuiBack.png"));
			TextureCoords tc = t.getSubImageTexCoords(0, 0, 533, 101);
			t.enable();
			t.bind();
			gl.glBegin(gl.GL_QUADS);
			gl.glTexCoord2d(tc.left(), tc.bottom());
			gl.glVertex2i(0, 0);
			gl.glTexCoord2d(tc.right(), tc.bottom());
			gl.glVertex2i(533, 0);
			gl.glTexCoord2d(tc.right(), tc.top());
			gl.glVertex2i(533, 101);
			gl.glTexCoord2d(tc.left(), tc.top());
			gl.glVertex2i(0, 101);
			gl.glEnd();
			t.disable();
			gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

			//Draw lower GUI text, starting with playername
			uiText.beginRendering(width, height);
			uiText.draw(player.getName(), 157, 83);
			//Mouse counter
			String score = String.format("%04d", player.getMouseCount());
			uiText.draw(score, 460, 67);
			//Cat counter
			score = String.format("%04d", player.getCatCount());
			uiText.draw(score, 460, 24);
			uiText.endRendering();
		} else {
			//Draw player score display
			scoreText.beginRendering(width, height);
			scoreText.setColor(Color.BLACK);
			int firstDigit = (player.getScore() / 100) % 10;
			int secondDigit = (player.getScore() / 10) % 10;
			int thirdDigit = (player.getScore()) % 10;
			if (firstDigit == 0) {
				scoreText.draw(String.valueOf(firstDigit), 108, 23);
			} else {
				scoreText.draw(String.valueOf(firstDigit), 112, 23);
			}
			if (secondDigit == 0) {
				scoreText.draw(String.valueOf(secondDigit), 156, 23);
			} else {
				scoreText.draw(String.valueOf(secondDigit), 160, 23);
			}
			if (thirdDigit == 0) {
				scoreText.draw(String.valueOf(thirdDigit), 204, 23);
			} else {
				scoreText.draw(String.valueOf(thirdDigit), 208, 23);
			}
			scoreText.endRendering();
		}
	}

	private void viewBottomRightUI(GL gl) {
		//Render the background
		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		Texture t = TextureManager.getInstance().getTexture(new File("Assets/Textures/GuiBottom.png"));
		TextureCoords tc = t.getSubImageTexCoords(0, 0, 800, 101);
		t.enable();
		t.bind();
		gl.glBegin(gl.GL_QUADS);
		gl.glTexCoord2d(tc.left(), tc.bottom());
		gl.glVertex2i(0, 0);
		gl.glTexCoord2d(tc.right(), tc.bottom());
		gl.glVertex2i(width, 0);
		gl.glTexCoord2d(tc.right(), tc.top());
		gl.glVertex2i(width, 101);
		gl.glTexCoord2d(tc.left(), tc.top());
		gl.glVertex2i(0, 101);
		gl.glEnd();
		t.disable();

		//Draw lower right scoreboard
		uiText.beginRendering(width, height);
		List<Player> players = GameplayManager.getInstance().players;
		for (int i = 0; i < players.size(); i++) {
			Player tmpPlayer = players.get(i);
			uiText.setColor(tmpPlayer.getColor());
			int xoffset;
			int yoffset;
			if (i < 4) {
				xoffset = width - 235;
				yoffset = 65 - (16 * i);
			} else {
				xoffset = width - 112;
				yoffset = 65 - (16 * (i - 4));
			}
			uiText.draw(String.format("%1$-7s", tmpPlayer.getName()) + "(" + String.format("%03d", tmpPlayer.getScore()) + ")", xoffset, yoffset);
		}
		uiText.endRendering();
	}

	private void viewScoreboard(GL gl) {
		//Draw the background of the scoreboard
		Texture t = TextureManager.getInstance().getTexture(new File("Assets/Textures/GuiScoreboard.png"));
		TextureCoords tc = t.getSubImageTexCoords(0, 0, 300, 350);
		t.enable();
		t.bind();
		gl.glBegin(gl.GL_QUADS);
		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		gl.glTexCoord2d(tc.left(), tc.bottom());
		gl.glVertex2i(width / 2 - 150, height / 2 - 150);
		gl.glTexCoord2d(tc.right(), tc.bottom());
		gl.glVertex2i(width / 2 + 150, height / 2 - 150);
		gl.glTexCoord2d(tc.right(), tc.top());
		gl.glVertex2i(width / 2 + 150, height / 2 + 200);
		gl.glTexCoord2d(tc.left(), tc.top());
		gl.glVertex2i(width / 2 - 150, height / 2 + 200);
		gl.glEnd();
		t.disable();

		//Draw the scoreboard text
		uiText.beginRendering(width, height);
		uiText.setColor(Color.WHITE);
		List<Player> players = GameplayManager.getInstance().getSortedPlayerListByTournamentScore();
		for (int i = 0; i < players.size(); i++) {
			Player tmpPlayer = players.get(i);
			uiText.setColor(tmpPlayer.getColor());
			uiText.draw((i + 1) + " - " + String.format("%1$-7s", tmpPlayer.getName()) + " - " + tmpPlayer.getTournamentScore(), width / 2 - 130, height / 2 + 150 - (25 * i));
		}
		uiText.endRendering();
	}
	
	private void viewMenu(GL gl) {
		//Draw menu alternatives
		Texture t = TextureManager.getInstance().getTexture(new File("Assets/Textures/GuiMenu.png"));
		TextureCoords tc = t.getSubImageTexCoords(0, 0, 180, 160);
		t.enable();
		t.bind();
		gl.glBegin(gl.GL_QUADS);
		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		gl.glTexCoord2f(tc.left(), tc.bottom());
		gl.glVertex2i(width / 2 - 90, height / 2 - 200);
		gl.glTexCoord2f(tc.right(), tc.bottom());
		gl.glVertex2i(width / 2 + 90, height / 2 - 200);
		gl.glTexCoord2f(tc.right(), tc.top());
		gl.glVertex2i(width / 2 + 90, height / 2 - 40);
		gl.glTexCoord2f(tc.left(), tc.top());
		gl.glVertex2i(width / 2 - 90, height / 2 - 40);
		gl.glEnd();
		t.disable();
		for (MenuComponent m : menuComponents) {
			m.view(gl);
		}
	}//</editor-fold>
	
	//<editor-fold defaultstate="collapsed" desc=" Ingame menu ">
	/**
	 * Used to register mouse movement when the ingame menu is activated
	 * @param x the cursor position on the x axis
	 * @param y the cursor position on the y axis
	 */
	public void registerMouseMotion(int x, int y) {
		//Convert x and y axis from screen coords
		x=(int)(x*(800.0f/(float)ConfigManager.getInstance().width));
		y=(int)(y*(600.0f/(float)ConfigManager.getInstance().height));
		//Register mouse motion
		MenuComponent m = getComponentFromCoord(x, y);
		if (m != null) {
			setFocus(m);
		} else {
			for (MenuComponent mc : menuComponents) {
				mc.setFocus(false);
			}
		}
	}
	
	/**
	 * Used to register mouse clicks when the ingame menu is activated
	 * @param x the cursor position on the x axis
	 * @param y the cursor position on the y axis
	 */
	public void registerMouseClick(int x, int y) {
		//Convert x and y axis from screen coords
		x=(int)(x*(800.0f/(float)ConfigManager.getInstance().width));
		y=(int)(y*(600.0f/(float)ConfigManager.getInstance().height));
		//Register mouse click
		MenuComponent m = getComponentFromCoord(x, y);
		switch (menuComponents.indexOf(m)) {
			case 0:
				game.registerCommand(Command.UNPAUSE);
				break;
			case 1:
				game.registerCommand(Command.GOTOMENU);
				break;
		}
	}

	/**
	 * Recieves a component from the menuComponents-list based upon
	 * its position
	 * @param x the check position on the x axis
	 * @param y the check position on the inverted y axis
	 * @return the checked menu component (null if none is hit)
	 */
	private MenuComponent getComponentFromCoord(int x, int y) {
		for (MenuComponent m : menuComponents) {
			if (x > m.x && x < m.x + m.width && y > m.y && y < m.y + m.height) {
				return m;
			}
		}
		return null;
	}

	private void setFocus(MenuComponent c) {
		for (MenuComponent m : menuComponents) {
			m.setFocus(false);
		}
		c.setFocus(true);
	}//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc=" Message handlers ">
	/**
	 * Set a message that is shown faded onto the screen for a certain amount
	 * of time or looping with a certain interval
	 * @param message the message that is shown on the center of the screen
	 * @param interval the interval in which the message will blink
	 * @param loop do the blink once or until something calls clearMessage()?
	 */
	public void showMessage(String message, float interval, boolean loop) {
		messageTimer.setTimestamp();
		currentMessage = message;
		maxTime = interval;
		messageLoop = loop;
	}

	/**
	 * Clears the center message, set by showMessage
	 */
	public void clearMessage() {
		currentMessage = null;
	}

	/**
	 * Returns the current message
	 * @return the message
	 */
	public String getMessage() {
		return currentMessage;
	}
	
		/**
	 * Internal method for printing the message and calculate alpha
	 * depending on time
	 * @param time the time passed since the last interval
	 */
	private void printMessage(float time) {
		messageText.beginRendering(800, 600);
		float alpha = 0.0f;
		if (time <= maxTime / 2) {
			alpha = time / (maxTime / 2);
		} else {
			alpha = (maxTime - time) / (maxTime / 2);
		}
		messageText.setColor(1.0f, 1.0f, 1.0f, alpha);
		messageText.draw(currentMessage, width / 2 - (int) messageText.getBounds(currentMessage).getWidth() / 2, height / 2);
		messageText.endRendering();
	}//</editor-fold>
	
	//<editor-fold defaultstate="collapsed" desc=" Show methods ">
	/**
	 * Show gui on screen or hide everything but the message?
	 * @param show if true -> show gui
	 */
	public void showGUI(boolean show) {
		fadeTimer.unPause();
		fadeTimer.setTimestamp();
		showGUI = show;
	}

	/**
	 * Show scoreboard at the center of the screen
	 * @param show if true, show scoreboard
	 */
	public void showScoreBoard(boolean show) {
		showScoreBoard = show;
	}

	/**
	 * Show the Powerup Spinner at the center of the screen
	 * @param powerupSpinner the spinner that shall be viewed by the UI
	 */
	public void showPowerupSpinner(Spinner powerupSpinner) {
		this.powerupSpinner=powerupSpinner;
	}//</editor-fold>
}
