/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package LevelCreator;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import javax.swing.JPanel;

/**
 * This is a JPanel with a background image. It works just like a normal JPanel
 * except that the background image can be set and retrieved. 
 * @author Erik
 */
public class JLevelPanel extends JPanel {
    private Image image;
	private String imageName;

	/**
	 * Creates a JPanel with the given layout manager and background image
	 * @param layout The layoutmanager of the JLevelPanel
	 * @param imageName The path to the background image
	 */
    public JLevelPanel(LayoutManager layout, String imageName) {
        super(layout);
        setImage(imageName);
    }

	/**
	 * Creates a JPanel with the given background image.
	 * @param imageName The path to the background image
	 */
    public JLevelPanel(String imageName) {
		super();
        setImage(imageName);
    }

	/**
	 * Sets the background image of the JLevelPanel. Setting the image repaints the
	 * panel. 
	 * @param imageName The path to the background image.
	 */
    public void setImage(String imageName) {
        this.imageName = imageName;
		this.image = Toolkit.getDefaultToolkit().createImage(imageName);
        repaint();
    }
    
	/**
	 * Returns the path to the current background image.
	 * @return The path to the current background image.
	 */
    public String getImageName() {
		return imageName;
	}

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, this);
    }

}
