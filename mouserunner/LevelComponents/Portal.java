package mouserunner.LevelComponents;

import com.sun.opengl.util.texture.Texture;
import java.awt.Color;
import java.io.File;
import java.util.LinkedList;
import javax.media.opengl.GL;
import mouserunner.Game.Entity.Entity;
import mouserunner.Managers.TextureManager;

/**
 * <p>This is a Portal trap in the Mouserunner game. It should be used as a tile on
 * the playing field. A Portal has a partner, another portal that is it connected
 * to. All entities that enter the portal will be moved to the portals partner.
 * A portal has to be connected to another portal through the {@link #connect(Portal)}
 * method otherwise an excpetion will be thrown when the Portal is triggered.</p>
 * 
 * <p>Note that a portal-pair only needs to be connected "one-way", i.e. calling
 * connect from portal A with parameter B is enough to connect portal A and B.
 * Calling connect from B with A as parameter after this will cause an 
 * IllegalStateException. A portal can be tested to see if it is connected through
 * the {@link #isConnected()} method.</p>
 * @author Erik
 */
public class Portal extends Trap {
	/** This is a pool of different colors that the portal can use for identifing
	 portal-pairs. It will probablt be replaced in a later version (so don't use it
	 * anywhere else then in the Portal class)*/
	private static LinkedList<Color> texturePool;
	
	// The portal that this portal is connected to
	private Portal partner;

	/**
	 * Creates a Portal with the given position and walls. This portal needs to
	 * be connected to another portal before it can be used.
	 * @param x the X coordinate of the Portal on the playing field
	 * @param y the Y coordinate of the Portal on the playing field
	 * @param leftWall true if there should be a wall to the left
	 * @param rightWall true if there should be a wall to the right
	 * @param topWall true if there should be a wall on the top
	 * @param bottomWall true if there should be a wall on the bottom
	 */
	public Portal(int x, int y, boolean leftWall, boolean rightWall, boolean topWall, boolean bottomWall) {
		super(x, y, leftWall, rightWall, topWall, bottomWall);
		this.partner = null;
	}
	
	/**
	 * This method returns a Color from the Color pool. If the color pool is not
	 * initiated this method initiates it first.
	 * 
	 * This method will be replaced in a later version.
	 * @return A Color from the Portal color pool. Null if the color pool is empty
	 */
	private Color getPortalColor() {
		if(texturePool == null) {
			texturePool = new LinkedList<Color>();
			texturePool.add(Color.RED);
			texturePool.add(Color.BLUE);
			texturePool.add(Color.BLACK);
			texturePool.add(Color.GREEN);
			texturePool.add(Color.YELLOW);
			texturePool.add(Color.PINK);
			texturePool.add(Color.MAGENTA);
			texturePool.add(Color.CYAN);
		}
		
		if(texturePool.isEmpty()) {
			// if there are no more portals avalialbe something should happen
			return null;
		} else {
			return texturePool.poll();
		}
	}
	
	/**
	 * This method connects the portal to another portal. When a Entity enters this
	 * portal they will reappear at the portal provided by this method.
	 * If the portal is not connected to another portal nothing will happen when
	 * an Entity enters it.
	 * 
	 * It's only neccesary to connect the portals from one end, trying to connect
	 * a portal pair from both sides will generate an exception on the second call.
	 * Note that when a portal has been connected it cannot be "disconnected".
	 * @param partner the Portal this portal should connect to
	 * @throws IllegalStateException if trying to connect an already connected portal
	 * @see #isConnected() 
	 */
	public void connect(Portal partner) {
		// A portal can only connect once and so trying to connect to a already connected
		// portal will caouse an exception
		if(isConnected()) {
			throw new IllegalStateException("This Portal already has a connection");
		}
		this.color = getPortalColor();
		setPartner(partner);
		partner.setPartner(this);
	}
	
	/**
	 * Indicates whether this portal is connected with another portal or not.
	 * @return true if this portal is connected to another portal
	 */
	public boolean isConnected() {
		return this.partner != null;
	}
	
	/**
	 * Returns the {@link mouserunner.LevelComponents.Portal} that this Portal is connected to.
	 * @return the Portal this portal is connected to
	 */
	public Portal getParnter() {
		return this.partner;
	}
	
	/**
	 * Setups this portals connection with its partner. Sets the parnet variable
	 * and the color they both should use. The portal that invokes the connect method
	 * should set its color before calling this method
	 * @param portal the portal to set as partner
	 * @see #connect(mouserunner.LevelComponents.Portal) 
	 * @see #getParnter() 
	 */
	private void setPartner(Portal portal) {
		this.partner = portal;
		this.color = portal.color;
	}

	/**
	 * This method "triggers" the Portal moving the Entity from its current position to
	 * the position of the portals partner.
	 * @param entity the Entity that triggered the trap
	 */
	@Override
	public void trigger(Entity entity) {
		if(!isConnected()) {
			throw new IllegalStateException("The portal at (" + this.x + "," + this.y + ") has not been conncted to anoter portal");
		}
		
		entity.teleport(partner);
	}

	/**
	 * Displays the Portal
	 * @param gl
	 */
	@Override
	public void view(GL gl) {
		Texture texture = TextureManager.getInstance().getTexture(new File("Assets/Textures/TrapPortalBlue.png"));
		texture.bind();
		viewTile(gl);
		viewWalls(gl);
	}

}
