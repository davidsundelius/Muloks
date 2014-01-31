/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mouserunner.LevelComponents;

import com.sun.opengl.util.texture.Texture;
import java.io.File;
import javax.media.opengl.GL;
import mouserunner.Game.Entity.Entity;
import mouserunner.Game.Entity.Mouse;
import mouserunner.Managers.GameplayManager;
import mouserunner.Managers.TextureManager;

/**
 *
 * @author Erik
 */
public class Glue extends Trap {
	public static double gluedSpeedRatio = 0.5;

	public Glue(int x, int y, boolean leftWall, boolean rightWall, boolean topWall, boolean bottomWall) {
		super(x, y, leftWall, rightWall, topWall, bottomWall);
	}
	
	@Override
	public void trigger(Entity entity) {
		if(entity instanceof Mouse) {
			entity.slowDown(GameplayManager.getInstance().gluedDuration, gluedSpeedRatio);
		}
	}

	public void view(GL gl) {
		Texture texture = TextureManager.getInstance().getTexture(new File("Assets/Textures/TrapGlue.png"));
		texture.bind();
		
		viewTile(gl);
		viewWalls(gl);
	}

}
