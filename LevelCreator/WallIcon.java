package LevelCreator;

import java.awt.Toolkit;
import javax.swing.ImageIcon;

/**
 * An icon that shows a wall. A wall is either horizontal or vertical, this is set
 * when the wall is constructed and cannot be changed later on.
 * @author Erik
 */
public class WallIcon extends ImageIcon {
	/** Tells the WallIcon constructor that the direction of the wall is vertical */
    public final static int VERTICAL = -23;
	/** Tells the WallIcon constructor that the direction of the wall is horizontal */
    public final static int HORIZONTAL = -24;
    
    private boolean wall;
    private final int direction;
    
    public WallIcon(int direction, boolean wall) {
        if(direction != VERTICAL && direction != HORIZONTAL)
            throw new IllegalArgumentException("An invalid direction was given");
        
        this.direction = direction;
        setWall(wall);
    }

    /**
     * Indicates whether there is an active wall or not.
     * @return true if there is an active wall
     */
    public boolean hasWall() {
        return wall;
    }

    /**
     * Sets the wall. and also the corresponding image.
     * @param wall the state of the wall, true if there should be a wall.
     */
    public void setWall(boolean wall) {
        this.wall = wall;
        if(direction == VERTICAL)
            if(wall)
                setImage(Toolkit.getDefaultToolkit().createImage("Assets/LevelCreator/vWall.png"));
            else
                setImage(Toolkit.getDefaultToolkit().createImage("Assets/LevelCreator/vNoWall.png"));
        else
            if(wall)
                setImage(Toolkit.getDefaultToolkit().createImage("Assets/LevelCreator/hWall.png"));
            else
                setImage(Toolkit.getDefaultToolkit().createImage("Assets/LevelCreator/hNoWall.png"));
    }
}
