package mouserunner.System;

/**
 * A enum to use for keeping track of directions
 * @author Erik (& Zorek)
 */

public enum Direction {
    RIGHT(1,0),
    LEFT(-1,0),
    UP(0,-1),
    DOWN(0,1);
    
	/** Used by entities to modifiy their movement.
	 * Should be -1, 0 or 1. */
    public final int moveX, moveY;
    
    /**
	 * Creates a new Direcion. The parameters is used by entites when moving.
	 * The x or y is multiplyed to the speed if the mouse to alter its direction.
	 * A zero value will make the speed zero in that direction and minus one will
	 * invert the direction of the speed.
	 * The values should be -1, 0 or 1 (unless you want a direction like VERYRIGHT, then
	 * you could use 2 or something, it is not recommended though).
	 * Example. Dirction(1, 0) will make an entity move nowhere on the Y-axis
	 * and normaly on the X-axis
	 * @param x the X-axis modifier
	 * @param y the Y-axis modifier
	 */
    Direction(int x, int y) {
        moveX = x;
        moveY = y;
    }
    
    /**
     * Used for finding out what direction you will be heading after you turn.
	 * You turn by giving the method the parameter LEFT or RIGHT and the method
	 * returns the direction you are now facing. 
	 * E.g. if you're heading UP and turn LEFT you would be heading LEFT and that is what
	 * is returned
	 * 
     * @param dir The direction to turn (LEFT or RIGHT).
     * @return The calculated direction
	 * @throws IllegalArgumentException if the parameter given is not LEFT or RIGHT
     */
    public Direction turn(Direction dir) {
        if(dir == LEFT) {
            if(this == Direction.LEFT)       return Direction.DOWN;
            else if(this == Direction.UP)    return Direction.LEFT;
            else if(this == Direction.RIGHT) return Direction.UP;
            else   /* if dir == DOWN */     return Direction.RIGHT; 
        } else if(dir == RIGHT) {
            if(this == Direction.LEFT)       return Direction.UP;
            else if(this == Direction.UP)    return Direction.RIGHT;
            else if(this == Direction.RIGHT) return Direction.DOWN;
            else   /* if dir == DOWN */     return Direction.LEFT;
        } else {
            throw new IllegalArgumentException("Impossible to turn " + dir + "!");
        }
    }
	
	
	/**
	 * This method converts an int to a Direcion. This conversion should be valid
	 * throughout the entire game.
	 * 
	 * Conversion table:
	 * <ul>
	 * <li>0 - Left</li>
	 * <li>1 - Right</li>
	 * <li>2 - Up</li>
	 * <li>3 - Down</li>
	 * </ul>
	 * @param dir the int to convert to a Direcion
	 * @return A Direction given by the parameter value
	 * @throws IllegalArgumentException if the given int is not a valid conversion value (should be 0-3)
	 */
	public static Direction intToDir(int dir) {
		switch(dir) {
			case 0:
				return LEFT;
			case 1:
				return RIGHT;
			case 2:
				return UP;
			case 3:
				return DOWN;
			default:
				throw new IllegalArgumentException("The provided int (" + dir + ") is not a valid direction");
		}
	}
	
	/**
	 * This method converts an Direcion to a int. This convertsion should be valid
	 * throughout the entire game.
	 * 
	 * Conversion table:
	 * <ul>
	 * <li>0 - Left</li>
	 * <li>1 - Right</li>
	 * <li>2 - Up</li>
	 * <li>3 - Down</li>
	 * </ul>
	 * @param dir the Direcion to convert to a int
	 * @return An int given by the parameter value
	 * @throws IllegalArgumentException if the given Direction is not LEFT,RIGHT,UP or DOWN
	 */
	public static int dirTiInt(Direction dir) {
		switch(dir) {
			case LEFT:
				return 0;
			case RIGHT:
				return 1;
			case UP:
				return 2;
			case DOWN:
				return 3;
			default:
				throw new IllegalArgumentException("The provided direction (" + dir + ") is not a valid direction");
		}
	}
}
