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
import mouserunner.Managers.TextureManager;
import mouserunner.Managers.GameplayManager;

/**
 *
 * @author Erik
 */
public class Mousetrap extends Trap {
	private long reloaded;

	public Mousetrap(int x, int y, boolean leftWall, boolean rightWall, boolean topWall, boolean bottomWall) {
		super(x, y, leftWall, rightWall, topWall, bottomWall);
		this.reloaded = 0;
	}

	@Override
	public void trigger(Entity entity) {
		if (entity instanceof Mouse) {
			if (isLoaded()) {
				entity.kill(false);
				reloaded = GameplayManager.getInstance().gameTimer.read() + GameplayManager.getInstance().mouseTrapReloadTime;
			}
		}
	}
	
	public boolean isLoaded() {
		return GameplayManager.getInstance().gameTimer.read() >= reloaded;
	}

	public void view(GL gl) {
		Texture texture = TextureManager.getInstance().getTexture(new File("Assets/Textures/TrapMouseTrap.png"));
		texture.bind();
		
		viewTile(gl);
		viewWalls(gl);
	}

}
