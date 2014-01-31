package mouserunner.LevelComponents;

import mouserunner.Game.Level;
import mouserunner.System.Direction;
import mouserunner.Managers.TextureManager;
import com.sun.opengl.util.texture.Texture;
import java.io.File;
import javax.media.opengl.GL;
import mouserunner.Game.Arrow;
import mouserunner.Game.Player;

/**
 * An empty tile. The standard tile of the playfield.
 * @author Zorek (& Erik)
 */
public class EmptyTile extends Tile {

	private Texture texture;
	private Arrow arrow;

	public EmptyTile(int x, int y, boolean leftWall, boolean rightWall, boolean topWall, boolean bottomWall) {
		super(x, y, leftWall, rightWall, topWall, bottomWall);
		arrow = new Arrow(x, y);
	}

	/**
	 * Adds a permanent arrow to the tile, with the given direction and the Color light gray
	 * @param dir the direction of the arrow
	 */
	public void setArrow(Direction dir) {
		arrow.activate(dir);
	}

	/**
	 * Adds an arrow to the tile with the given direcion. The color of the arrow will be the players color
	 * @param dir the direction of the tile
	 * @param player the player that owns the arrow
	 */
	public void setArrow(Direction dir, Player player) {
		// If there is no arrow on the tile the arrow is added
		if (!arrow.isActive()) {
			if (player.getNumberArrows() >= 3) {
				player.removeArrow().deactivate();
				arrow.activate(dir, player);
			} else {
				arrow.activate(dir, player);
			}
		}

	}

	/**
	 * Checks if the tiles has en active arrow.
	 * @return true if the tile has an active arrow.
	 */
	public boolean hasArrow() {
		return arrow.isActive();
	}

	/**
	 * Returns the direction of the arrow on this tile. If the arrow is not active
	 * null is returned
	 * @return the direction of the arrow, null if the arrow is not active
	 */
	public Direction getArrowDirection() {
		return arrow.getDir();
	}

	public Arrow getArrow() {
		return this.arrow;
	}

	/**
	 * Displays the tile and its walls
	 * @param gl
	 */
	@Override
	public void view(GL gl) {
		// If there are no arrow on the tile use the default texture else use the arrow texture
		if (!arrow.isActive()) {
			texture = TextureManager.getInstance().getTexture(new File("Assets/Textures/Tile.png"));
			//float[] material = {(float)color.getRed()/255.0f,(float)color.getGreen()/255.0f,(float)color.getBlue()/255.0f};
			//gl.glMaterialfv(gl.GL_FRONT, gl.GL_AMBIENT, material,0);
			texture.bind();
			viewTile(gl);
		} else {
			arrow.view(gl);
		}

		viewWalls(gl);
	}
}
