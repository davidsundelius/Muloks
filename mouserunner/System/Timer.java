package mouserunner.System;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Timer is used for keeping track of the time in game
 * @author Zorek
 */
public class Timer implements Serializable {
	private long timestamp;
  private int pauseTime;
  public static long serverOffset;

  /**
   * Constructs new timer and sets its timestamp
   */
	public Timer() {
		pauseTime=-1;
		setTimestamp();
	}

  /**
   * Sets new timestamp
   */
	public void setTimestamp() {
		timestamp=Calendar.getInstance().getTimeInMillis();
	}
	
  /**
   * Reads the time since last timestamp
   * @return the past time
   */
	public int read() {
		if(pauseTime==-1)
			return (int)(Calendar.getInstance().getTimeInMillis()-timestamp);
		else
			return (int)pauseTime;
	}
	
	public void sync(long serverTime) {
		serverOffset = Calendar.getInstance().getTimeInMillis() - serverTime;
	}

  /**
   * Reads the time since last timestamp and then sets a new timestamp
   * @return the past time
   */
	public int tick() {
		int tmp = read();
		setTimestamp();
		return tmp;
	}

  /**
   * Pauses the timer
   */
  public void pause() {
		pauseTime=(int)(Calendar.getInstance().getTimeInMillis()-timestamp);
	}
  
  /**
   * Unpauses the timer
   */  
  public void unPause() {
		timestamp=(Calendar.getInstance().getTimeInMillis()-pauseTime);
		pauseTime=-1;
	}
	
	public boolean isPaused() {
		return (pauseTime!=-1);
	}
}
