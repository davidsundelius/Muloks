/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mouserunner.LevelComponents;

import java.awt.Color;
import javax.media.opengl.GL;
import mouserunner.Game.Entity.Entity;
/**
 *
 * @author Erik
 */
public class BlackHole extends Trap {

	public BlackHole(int x, int y, boolean leftWall, boolean rightWall, boolean topWall, boolean bottomWall) {
		super(x, y, leftWall, rightWall, topWall, bottomWall);
		this.color = Color.BLACK;
	}
	
	@Override
	public void trigger(Entity entity) {
		entity.kill(true);
	}

	@Override
	public void view(GL gl) {
		viewTile(gl);
		viewWalls(gl);
	}

}
