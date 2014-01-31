package mouserunner.System;

/**
 * A enum to use for keeping track system commands used in the CommandListener<BR>
 * Known commands:<BR>
 * NEWGAME - Starts a new game<BR>
 * NEXTGAME - Jumps and starts the next game in a tournament <BR>
 * ENDTOURNAMENT - Exits the tournament and goes to the result screen<BR>
 * GOTOTITLE - Exits the current state and returns to the title screen<BR>
 * GOTOMENU - Exits the current state and returns to the main menu<BR>
 * GOTOLOBBY - Exits the current state and enters the game lobby<BR>
 * * GOTOLOBBY - Exits the current state and shows the credits<BR>
 * PAUSE - Pauses the game<BR>
 * UNPAUSE - Unpauses the game<BR>
 * SLOWDOWN - Lower the updaters tps<BR>
 * SPEEDUP - Higher the updaters tps<BR>
 * RESTORESPEED - Restore the updaters tps<BR>
 * @author Zorek
 */
public enum Command {
  NEWGAME,
  NEXTGAME,
  ENDTOURNAMENT,
	GOTOTITLE,
  GOTOMENU,
	GOTOLOBBY,
	GOTOCREDITS,
  PAUSE,
  UNPAUSE,
  SLOWDOWN,
  SPEEDUP,
	RESTORESPEED
}