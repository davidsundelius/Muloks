package mouserunner.System;

/**
 * Updatable is a common interface for all dynamic the updater can
 * call to update its setting, for instance used for animation and physics.
 * @author Zorek
 */
public interface Updatable {

  /**
   * The method called to update the settings of an object
   * @return does the object want to be destroyed?
   */
  public boolean update();
}

