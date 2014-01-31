package mouserunner.System;

import java.util.Collection;
import java.util.EventListener;
import mouserunner.EventListeners.CommandListener;

/**
 * An interface for generilizing states, used in the StateManager
 * Known states:
 * Game
 * Title
 * Menu
 * @author Zorek
 */

public interface State extends Updatable, Viewable {
  /**
   * The updater calls this to get all eventlisteners of the current state
   * @return a list of eventlisteners
   */
  public Collection<EventListener> getListeners();
  /**
   * The updater calls this to let the current commandlistener get information
   * of the current state
   */
  public void setCommandListener(CommandListener cl);
}

