package mouserunner.Managers;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import mouserunner.System.*;

/**
 * A class used to keep track of the current state
 * @author Zorek
 */
public class StateManager {

  private final static StateManager instance = new StateManager();
	private Timer fadeTimer;
	private final float maxTime = 1000.0f;
  private State currentState;

  /**
   * Empty constructor
   */
  private StateManager() {
		fadeTimer=new Timer();
		fadeTimer.pause();
		currentState=null;
  }

   /**
   * Gets the singleton
   * @return the single instance
   */ 
  public static StateManager getInstance() {
    return instance;
  }
  
  /**
   * Sets a new state
   * @param newState the new state
   */
  public void setState(State newState) {
		fadeTimer.unPause();
		fadeTimer.setTimestamp();
		while (fadeTimer.read() < maxTime/2) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
    currentState = newState;
  }

  /**
   * Returns the current state
   * @param gl the current gl context
   */
  public void viewState(GL gl) {
		currentState.view(gl);
		
		if(!fadeTimer.isPaused()) {
			gl.glDisable(gl.GL_TEXTURE_2D);
			GLU glu = new GLU();
			gl.glMatrixMode(gl.GL_PROJECTION);
			gl.glPushMatrix();
			gl.glLoadIdentity();
			glu.gluOrtho2D(0.0, ConfigManager.getInstance().width, 0.0, ConfigManager.getInstance().height);
			gl.glMatrixMode(gl.GL_MODELVIEW);
			gl.glLoadIdentity();
			//Draw alpha mask
			setAlpha((float)fadeTimer.read(),gl);
			gl.glBegin(gl.GL_QUADS);
				gl.glVertex2i(0																	,   0																	);
				gl.glVertex2i(ConfigManager.getInstance().width ,   0																	);
				gl.glVertex2i(ConfigManager.getInstance().width , ConfigManager.getInstance().height	);
				gl.glVertex2i(0																	, ConfigManager.getInstance().height	);
			gl.glEnd();
			if(fadeTimer.read()>maxTime) {
				fadeTimer.pause();
			}
			gl.glMatrixMode(gl.GL_PROJECTION);
			gl.glPopMatrix();
			gl.glMatrixMode(gl.GL_MODELVIEW);
			gl.glEnable(gl.GL_TEXTURE_2D);
		}
  }
	
	public State getState() {
    return currentState;
	}
	
  /**
   * Internal method for calculating the alpha value depending on time
   * @param time the time since last picture
   * @param maxtime the time when the picture will be alpha 1.0f
   * @param gl the current gl context
   */
  private void setAlpha(float time, GL gl) {
		float alpha=1.0f;
		if(time<maxTime/2){
			alpha = time/(maxTime/2);
		}else if(time>maxTime/2)
			alpha = (maxTime-time)/(maxTime/2);
		else
			alpha = 1.0f;
		gl.glColor4f(0.0f, 0.0f, 0.0f, alpha);
	}
}

