package mouserunner.Game;

import mouserunner.System.SyncObject;
import mouserunner.System.Viewable;
import mouserunner.System.Updatable;
import mouserunner.Managers.TextureManager;
import javax.media.opengl.GL;
import java.awt.Color;
import com.sun.opengl.util.texture.Texture;
import java.io.File;
import java.io.Serializable;
import mouserunner.Managers.GameplayManager;
import mouserunner.System.Direction;
import mouserunner.System.Syncable;

/**
 * A class that represents the directing arrows, that can be placed on the {@link mouserunner.LevelComponents.EmptyTile} by
 * players. Every EmptyTile has a Arrow instance. To place an arrow
 * call {@link mouserunner.LevelComponents.EmptyTile#setArrow(Direction, Player)}
 * @author Erik
 * @see mouserunner.LevelComponents.EmptyTile
 */
public class Arrow implements Viewable, Updatable, Serializable, Syncable {
	private Color color;
	private Direction dir;
	
	// Blinking
	private long startTime, expireTime, blink, nextBlink;
	/** The duration if a "blink" (the time the arrow is not shown) */
	private static long blinkDuration = 75;

	// The Player that placed the arrow, null if it is a permanent arrow
	private Player owner;
	// Used to determin if the arrow should be displayed
	private boolean active;
	// Used with the about-to-disappear indication in the view method
	private boolean stoppedBlinking;
	
	/** The x coordinate of the arrow */
	public final int x;
	/** The y coordinate of the arrow */
	public final int y;

	/**
	 * Cretes an arrow. The parameters should be the position of the tile the arrow
	 * belongs to. This is not check howevery so it is up to the programmer to get it right.
	 * @param x the X position of the arrow
	 * @param y the Y position of the arrow
	 */
	public Arrow(int x, int y) {
		this.x = x;
		this.y = y;
		this.color = null;
		this.dir = null;
		this.owner = null;
		this.startTime = 0;
		this.expireTime = 0;
		this.blink = 0;
		this.nextBlink = 0;
		this.active = false;
		this.stoppedBlinking = false;
	}

	/**
	 * Updates the arrow, checks if its lifespan is over and deactivats it if so.
	 * @return always false (not used)
	 */
	@Override
	public boolean update() {
		if(!isPermanent()) {
			if (isActive()) {
				long currentTime = GameplayManager.getInstance().gameTimer.read() - startTime;
				if (currentTime > expireTime) {
					owner.removeArrow().deactivate();
				}
			}
		}
		return false;
	}
	
	/**
	 * <p>Draws the arrow.</p>
	 * 
	 * This method only exisits because theses lines of code will be used serveral times in
	 * view and therefore it is good to have them in one place (don't write the same code many times)
	 * @param gl
	 */
	private void drawArrow(GL gl, boolean blink) {
		Texture texture;
		// If the arrow is blinking no arrow should be shown
		if(blink) {
			texture = TextureManager.getInstance().getTexture(new File("Assets/Textures/Tile.png"));
		} else {
			texture = TextureManager.getInstance().getTexture(new File("Assets/Textures/arrow" + dir + ".png"));
		}
		texture.bind();
		gl.glColor3f((float) color.getRed() / 255.0f, (float) color.getGreen() / 255.0f, (float) color.getBlue() / 255.0f);
		
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0.0f, 1.0f);
		gl.glVertex3f(0.0f, -Level.tileSize, 0.0f);
		gl.glTexCoord2f(1.0f, 1.0f);
		gl.glVertex3f(Level.tileSize, -Level.tileSize, 0.0f);
		gl.glTexCoord2f(1.0f, 0.0f);
		gl.glVertex3f(Level.tileSize, 0.0f, 0.0f);
		gl.glTexCoord2f(0.0f, 0.0f);
		gl.glVertex3f(0.0f, 0.0f, 0.0f);
		gl.glEnd();
	}

	/**
	 * Displays the arrow. If the arrow is not active nothing is drawn.
	 * @param gl
	 */
	@Override
	public void view(GL gl) {
		if (active) {
			// If the arrow is not permanent
			if(!isPermanent()) {
				long currentTime = GameplayManager.getInstance().gameTimer.read() - startTime;
				 //if there are less than 5 secs left of the timer
				if(currentTime > expireTime - 5000) {
					// if it's not time for the next blink
					if(currentTime < nextBlink) {
						drawArrow(gl, false);
					} else {
						// If the arrow should be hidden (aka blink)
						if(currentTime < blink) {
							drawArrow(gl, true);
							stoppedBlinking = true;
						} else {
							blink = currentTime + blinkDuration;
							if(stoppedBlinking) {
								nextBlink = currentTime + (expireTime - currentTime) / 2;
								stoppedBlinking = false;
							}
							drawArrow(gl, false);
						}
					}
				}else { // if no blinking
					drawArrow(gl, false);
				}
			} else { // if the arrow is permanent
				drawArrow(gl, false);
			}
		}
	}
	
	/**
	 * Activates the arrow, i.e. showing it on the playing field for the 
	 * {@link mouserunner.Managers.GameplayManager#arrowTime} duration
	 * or until it is deactivated {@link #deactivate()}
	 * @param dir the direction of the arrow
	 * @param owner the over of the arrow, i.e the player that placed the arrow
	 */
	public void activate(Direction dir, Player owner) {
		this.dir = dir;
		this.color = owner.getColor();
		this.owner = owner;
		owner.addArrow(this);
		this.startTime = GameplayManager.getInstance().gameTimer.read();
		this.expireTime = GameplayManager.getInstance().arrowTime;
		this.active = true;
	}

	/**
	 * Activates a permanent arrow. An permanent arrow will never be deactivated
	 * by it self, a call to {@link #deactivate()} is needed.
	 * @param dir The direction of the arrow
	 */
	public void activate(Direction dir) {
		this.dir = dir;
		this.color = Color.LIGHT_GRAY;
		this.active = true;
	}

	/**
	 * Deactivates the arrow, removing it from the playing field and reseting
	 * the internal state of the arrow.
	 */
	public void deactivate() {
		active = false;
		color = null;
		dir = null;
		owner = null;
		this.startTime = 0;
		this.expireTime = 0;
		this.blink = 0;
		this.nextBlink = 0;
		this.stoppedBlinking = false;
	}
	
	/**
	 * This method "damages" the arrow, shortening its duration. The duration is decreased by
	 * 1/3 if the total time it is damaged.
	 */
	public void damage() {
		this.expireTime -= (1/3.0) * GameplayManager.getInstance().arrowTime;
	}
	
	/**
	 * This method turns the arrow clockwise one step
	 */
	public void turn() {
		dir = dir.turn(Direction.RIGHT);
	}

	/**
	 * Returns the direction of the arrow, if the arrow is not active null is 
	 * returned.
	 * @return the Direction of the arrow
	 */
	public Direction getDir() {
		return dir;
	}

	/**
	 * Returns true if the the arrow is currently displaying an arrow on the
	 * playing field.
	 * @return true if the arrow is active
	 */
	public boolean isActive() {
		return active;
	}
	
	/**
	 * Indicates if the arrow is a permanent arrow or not
	 * @return true if the arrow is permanent
	 */
	public boolean isPermanent() {
		return this.owner == null;
	}
	

	/**
	 * Returns the owner (a Player) of the arrow. If it's a permenent arrow or if the
	 * arrow is not active null is returned
	 * @return The Player that owns the arrow, null if there is no owner
	 */
	public Player getOwner() {
		return owner;
	}
       
	/**
	 * Syncronizes the arrow with the given SyncObject. If the SyncObject does not
	 * have the right type an {@link IllegalArgumentException} is thrown
	 * @param s the SyncObject the arrow should be synced with
	 */
	@Override
	public void synchronize(SyncObject s) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public SyncObject getSyncable() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
