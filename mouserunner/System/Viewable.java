package mouserunner.System;

import javax.media.opengl.GL;

/**
 * Viewable is a common interface for all objects that can be shown on the screen
 * @author Zorek
 */
public interface Viewable {
  /**
   * Method called to view the object on the screen
   * @param gl The current gl context
   */
  public void view(GL gl);
}