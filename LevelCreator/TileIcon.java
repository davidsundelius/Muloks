package LevelCreator;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Color;
import javax.swing.ImageIcon;
import java.awt.Toolkit;

/**
 * An icon used in a tile. It contains the same information as an ImageIcon as well as
 * information about which type of tile is shows. The displayed image is set by
 * setting the tile type.
 * @author Erik
 */
public class TileIcon extends ImageIcon {

    private TileType type;
    private Image image;
    private Color color;
	/** The x coordinate of the Tile */
    public final int x;
	/** The y coordinate of the Tile */
    public final int y;
	
	// This is only used when the tile is a portal
	private int portalID;

	/**
	 * Creates a TileIcon of the given type at the given position
	 * @param type the {@link TileType} of the tile.
	 * @param x the x index of the tile icon
	 * @param y the y index og the tile icon
	 */
    public TileIcon(TileType type, int x, int y) {
        super();
        this.x = x;
        this.y = y;
        setType(type);
		// Invalid portal ID
		this.portalID = -1;
    }

    /**
     * Returns the current tile type
     * @return Returns the current tile type
     */
    public TileType getType() {
        return type;
    }
	
	/**
	 * Sets the portal ID of the tile icon this should only be set if the tile is a portal type, but
	 * nothing bad will happen if the ID is set on any other tile
	 * @param portalID
	 */
	public void setPortalID(int portalID) {
		this.portalID = portalID;
	}
	
	/**
	 * Returns the portal ID of the tile icon
	 * @return returns the portal ID of the tile icon
	 */
	public int getPortalID() {
		return portalID;
	}

    /**
     * Sets the tile type. The corresponding image is set when the type is set.
     * @param type the type that should be set
     */
    public void setType(TileType type) {
        if(type == null)
            return;
        
        this.type = type;
        if(type == TileType.EMPTYTILE) {
            setImage(Toolkit.getDefaultToolkit().createImage("Assets/LevelCreator/nest.png"));
            if ((x + y) % 4 == 0)
                color = new Color(255, 150, 0);
            else
                color = new Color(100, 200, 255);
        } else if(type == TileType.NEST){
            setImage(Toolkit.getDefaultToolkit().createImage("Assets/LevelCreator/nest.png"));
        } else if(type == TileType.SPAWN){
            setImage(Toolkit.getDefaultToolkit().createImage("Assets/LevelCreator/spawn.png"));
        } else if(type == TileType.ARROWLEFT){
            setImage(Toolkit.getDefaultToolkit().createImage("Assets/LevelCreator/arrowLEFT.png"));
        } else if(type == TileType.ARROWRIGHT){
            setImage(Toolkit.getDefaultToolkit().createImage("Assets/LevelCreator/arrowRIGHT.png"));
        } else if(type == TileType.ARROWUP){
            setImage(Toolkit.getDefaultToolkit().createImage("Assets/LevelCreator/arrowUP.png"));
        } else if(type == TileType.ARROWDOWN){
            setImage(Toolkit.getDefaultToolkit().createImage("Assets/LevelCreator/arrowDOWN.png"));
        } else if(type == TileType.BLACKHOLE){
            setImage(Toolkit.getDefaultToolkit().createImage("Assets/LevelCreator/BlackHole.png"));
        } else if(type == TileType.CATTRAP){
            setImage(Toolkit.getDefaultToolkit().createImage("Assets/LevelCreator/Cattrap.png"));
        } else if(type == TileType.MOUSETRAP){
            setImage(Toolkit.getDefaultToolkit().createImage("Assets/LevelCreator/Mousetrap.png"));
        } else if(type == TileType.GLUE){
            setImage(Toolkit.getDefaultToolkit().createImage("Assets/LevelCreator/Glue.png"));
        } else if(type == TileType.PORTAL){
            setImage(Toolkit.getDefaultToolkit().createImage("Assets/LevelCreator/Portal.png"));
        }
    }

    @Override
    public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
        super.paintIcon(c, g, x, y);
        if(type == TileType.EMPTYTILE) {
            g.setColor(color);
            g.fillRect(x, y, 25, 25);
        } else {
            g.drawImage(image, x, y, c);
			if(type == TileType.PORTAL && portalID != -1) {
				g.drawString(String.valueOf(portalID), x + 14, y + 14);
			}
		}
    }
}
