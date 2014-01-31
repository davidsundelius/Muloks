/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mouserunner.LevelComponents;


import mouserunner.Game.Entity.Entity;


/**
 * All traps have a method called trigger which entities call when they enter a
 * trap tile. The convetion is that the Trap should modify the Entity but not
 * keep any information in itself about the events of the trigger
 * @author Erik
 */
public abstract class Trap extends Tile {
	public static int numTraps = 4;

	public Trap(int x, int y, boolean leftWall, boolean rightWall, boolean topWall, boolean bottomWall) {
		super(x, y, leftWall, rightWall, topWall, bottomWall);
	}

	public abstract void trigger(Entity entity);
}
