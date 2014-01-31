package mouserunner.LevelComponents;

import mouserunner.Game.Level;
import java.awt.Color;

// Tiles are Serializable to allow saving them in level files
import java.io.File;
import java.io.Serializable;
import javax.media.opengl.GL;
import mouserunner.System.Direction;
import mouserunner.Managers.ModelManager;
import mouserunner.System.Viewable;

/**
 * This is a class that represents the tiles on the playfield.
 * @author Erik & Zorek (typ)
 */
public abstract class Tile implements Viewable, Serializable {

	public final int x,  y;
	protected Color color;
	// These booleans are true if there is a wall at the given side
	protected boolean leftWall,  rightWall,  topWall,  bottomWall;

	public Tile(int x, int y, boolean leftWall, boolean rightWall, boolean topWall, boolean bottomWall) {
		this.x = x;
		this.y = y;
		this.leftWall = leftWall;
		this.rightWall = rightWall;
		this.topWall = topWall;
		this.bottomWall = bottomWall;
		if ((x + y) % 2 == 0) {
			color = new Color(255, 150, 0);
		} else {
			color = new Color(100, 200, 255);
		}
	}

	/**
	 * Check whether or not the tile has a wall in the given direction
	 * @param dir the direction that will be tested
	 * @return true if there is a wall in the given direction
	 */
	public boolean hasWall(Direction dir) {
		// Checks if the tile has a wall in the given direction
		// and returns true if so
		if (dir == Direction.RIGHT && rightWall) {
			return true;
		}
		if (dir == Direction.LEFT && leftWall) {
			return true;
		}
		if (dir == Direction.UP && topWall) {
			return true;
		}
		if (dir == Direction.DOWN && bottomWall) {
			return true;
		}
		return false;
	}

	/**
	 * Change the status of the wall at the given direction. If there is a
	 * wall it is removed and vice versa
	 * @param dir Which wall that should be modified
	 */
	public void setWall(Direction dir) {
		if (dir == Direction.LEFT) {
			this.leftWall = !this.leftWall;
		} else if (dir == Direction.RIGHT) {
			this.rightWall = !this.rightWall;
		} else if (dir == Direction.UP) {
			this.topWall = !this.topWall;
		} else {
			this.bottomWall = !this.bottomWall;
		}
	}

	protected void viewWall(GL gl) {
		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		ModelManager.getInstance().getModel(new File("Assets/Models/Wall.ms3d"), false).view(gl);
	}
	
	protected void viewWalls(GL gl) {
		if (rightWall) {
			gl.glPushMatrix();
			gl.glTranslatef(Level.tileSize, -Level.tileSize / 2, 0.0f);
			viewWall(gl);
			gl.glPopMatrix();
		}
		if (leftWall) {
			gl.glPushMatrix();
			gl.glTranslatef(0.0f, -Level.tileSize / 2, 0.0f);
			viewWall(gl);
			gl.glPopMatrix();
		}
		if (topWall) {
			gl.glPushMatrix();
			gl.glRotatef(90.0f, 0.0f, 0.0f, 1.0f);
			gl.glTranslatef(0.0f, -Level.tileSize / 2, 0.0f);
			viewWall(gl);
			gl.glPopMatrix();
		}
		if (bottomWall) {
			gl.glPushMatrix();
			gl.glRotatef(90.0f, 0.0f, 0.0f, 1.0f);
			gl.glTranslatef(-Level.tileSize, -Level.tileSize / 2, 0.0f);
			viewWall(gl);
			gl.glPopMatrix();
		}
	}

	/**
	 * Displays only the tile (used for picking)
	 * @param gl
	 */
	public void viewTile(GL gl) {
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
	 * This method creates a Tile if the given tile type.
	 * 
	 * Tile type list:
	 * 0 - Not used, will cast an exception
	 * 1 = Reserved Path
	 * 2 = Spawn
	 * 3 = Nest
	 * 4 = North Arrow
	 * 5 = East Arrow
	 * 6 = South Arrow
	 * 7 = West Arrow
	 * 8 = Trap
	 * 
	 * @param x
	 * @param y
	 * @param leftWall
	 * @param rightWall
	 * @param topWall
	 * @param bottomWall
	 * @param type the type of the tile. See type list above.
	 * @return the newly created tile
	 */
	public static Tile createTile(int x, int y, boolean leftWall, boolean rightWall, boolean topWall, boolean bottomWall, int type) {
		// Used with permanent arrow
		EmptyTile t;
		switch (type) {
			case 1: // Empty tile
				return new EmptyTile(x, y, leftWall, rightWall, topWall, bottomWall);
			case 2: // Spawn
				return new SpawnPoint(x, y, leftWall, rightWall, topWall, bottomWall);
			case 3: // Nest
				return new Nest(x, y, leftWall, rightWall, topWall, bottomWall);
			case 4: // North arrow
				t = new EmptyTile(x, y, leftWall, rightWall, topWall, bottomWall);
				t.setArrow(Direction.UP);
				return t;
			case 5: // East arrow
				t = new EmptyTile(x, y, leftWall, rightWall, topWall, bottomWall);
				t.setArrow(Direction.RIGHT);
				return t;
			case 6: // South arrow
				t = new EmptyTile(x, y, leftWall, rightWall, topWall, bottomWall);
				t.setArrow(Direction.DOWN);
				return t;
			case 7: // West arrow
				t = new EmptyTile(x, y, leftWall, rightWall, topWall, bottomWall);
				t.setArrow(Direction.LEFT);
				return t;
			case 8: // Traps
				throw new IllegalArgumentException("Traps are not implemented");
			default: // Empty tile
				throw new IllegalArgumentException("Bad tile type");
		}
	}
}
