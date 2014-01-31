package mouserunner.LevelComponents;

import mouserunner.Game.Level;
import com.sun.opengl.util.texture.Texture;
import javax.media.opengl.GL;
import java.awt.Color;
import java.io.File;
import mouserunner.Game.Player;
import mouserunner.Managers.ModelManager;
import mouserunner.Managers.TextureManager;

/**
 *
 * @author Erik
 */
public class Nest extends Tile {

	private Player owner;
	private float z, rot;

	public Nest(int x, int y, boolean leftWall, boolean rightWall, boolean topWall, boolean bottomWall) {
		super(x, y, leftWall, rightWall, topWall, bottomWall);
		this.owner = null;
		z=0.0f;
		rot=0.0f;
	}

	public Player getOwner() {
		if (owner == null) {
			throw new IllegalStateException("No owner set for this nest");
		}
		return owner;
	}

	public void setOwner(Player owner) {
		this.owner = owner;
	}
	
	public void blastUpdate() {
		if(z<150.0f) {
			rot+=0.4f;
			z+=0.01f;
		}
	}

	@Override
	public void view(GL gl) {
		if (owner == null) {
			color = Color.LIGHT_GRAY;
		} else {
			color = owner.getColor();
		}
		Texture texture = TextureManager.getInstance().getTexture(new File("Assets/Textures/Tile.png"));
    texture.bind();
		super.viewTile(gl);
		if(!owner.isAlive())
			z=-0.5f;
		gl.glPushMatrix();
			gl.glTranslatef(Level.tileSize / 2, -Level.tileSize / 2, z);
			gl.glRotatef(rot, 0.0f, 0.0f, 1.0f);
			gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			ModelManager.getInstance().getModel(new File("Assets/Models/Rocket.ms3d"), false).generateDisplayList(gl);
		gl.glPopMatrix();

		viewWalls(gl);
	}
}
