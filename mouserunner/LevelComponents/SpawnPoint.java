package mouserunner.LevelComponents;

import mouserunner.Game.Level;
import java.io.File;
import javax.media.opengl.GL;
import mouserunner.Managers.ModelManager;

/**
 *
 * @author Erik
 */
public class SpawnPoint extends Tile {

	public SpawnPoint(int x, int y, boolean leftWall, boolean rightWall, boolean topWall, boolean bottomWall) {
		super(x, y, leftWall, rightWall, topWall, bottomWall);
	}

	@Override
	public void view(GL gl) {
		super.viewTile(gl);
		
		gl.glPushMatrix();
		gl.glTranslatef(Level.tileSize / 1.5f, -Level.tileSize / 4, 0.5f * Level.tileSize);
		gl.glRotatef(120.0f, 1, 0, 0);
		gl.glRotatef(-30.0f, 0, 1, 0);
		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		ModelManager.getInstance().getModel(new File("Assets/Models/Rocket.ms3d"), false).generateDisplayList(gl);
		gl.glPopMatrix();

		viewWalls(gl);
	}
}
