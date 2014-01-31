package mouserunner.Game;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.media.opengl.GL;
import mouserunner.System.Updatable;
import mouserunner.System.Viewable;
import mouserunner.System.Timer;
import mouserunner.Managers.GameplayManager;
import mouserunner.Managers.TextureManager;
import mouserunner.Poweups.Powerup;

/**
 * Spinner is a class that represents a powerups spinner that
 * shows up and randomizes a powerup to use when a powerup mouse
 * enters a nest
 * @author Zorek
 */
public class Spinner implements Updatable, Viewable {

	private final int numScrolls = 8;
	private final int totalTime = 3000;
	private final int width = 250;
	private final int height = 150;
	private Random randomGenerator;
	private final Timer timer = new Timer();
	private int[] powerupIndeces;
	private List<Sign> signs;
	private Lock viewLock = new ReentrantLock();
	private boolean done;
	private int centerX;
	private int centerY;

	/**
	 * Constructs a new spinner, also randomizes {numScrolls} amount
	 * of powerups (that are not blocked in the GameplayManager) and
	 * saves them in powerupIndeces
	 */
	public Spinner() {
		done = false;
		powerupIndeces = new int[numScrolls];
		signs = new LinkedList<Sign>();
		randomGenerator = new Random();
		for (int i = 0; i < numScrolls; i++) {
			//Loop a new random number until we get a powerup that is enabled
			powerupIndeces[i]=-1;
			while(powerupIndeces[i]==-1) {
				powerupIndeces[i] = randomGenerator.nextInt(Powerup.numPowerups);
				if(!GameplayManager.getInstance().powerupEnabled[powerupIndeces[i]])
					powerupIndeces[i]=-1;
			}
			signs.add(new Sign(powerupIndeces[i], i));
		}

		centerX = 400;
		centerY = 330;
	}

	/**
	 * Updates the Spinner
	 * @return is the spinner done with the animation?
	 */
	@Override
	public boolean update() {
		if (timer.read() > totalTime) {
			done = true;
		}
		Iterator<Sign> it = signs.iterator();
		while (it.hasNext()) {
			Sign s = it.next();
			if (s.update()) {
				viewLock.lock();
				it.remove();
				viewLock.unlock();
			}
		}
		return done;
	}

	/**
	 * Views the Spinner
	 * @param gl the current gl context
	 */
	@Override
	public void view(GL gl) {
		//Draw the Spinner
		gl.glPushMatrix();
		gl.glTranslatef(centerX, centerY, 0.0f);

		//Draw each sign except the front one
		Sign front = null;
		viewLock.lock();
		for (Sign s : signs) {
			if (s.inFront) {
				front = s;
			} else {
				s.view(gl);
			}
		}
		viewLock.unlock();
		//Draw the sign thats in front right now
		if (front != null) {
			front.view(gl);
		}
		gl.glPopMatrix();
	}

	/**
	 * Is the spinner done with its work?
	 * @return if true, the spinner is done
	 */
	public boolean isDone() {
		return done;
	}

	/**
	 * Returns the chosen powerup, used by game to spawn the correct powerup.
	 * To translate this integer into the name of the powerup, use getPowerUpName(int)
	 * in this class
	 * @return a int representing the chosen powerupIndex
	 */
	public int getPowerup() {
		return powerupIndeces[0];
	}

	/**
	 * Returns the name of the powerup with the given index
	 * @param index the number used to map a powerup name
	 * @return the name of the powerup
	 */
	public static String getPowerupName(int index) {
		switch (index) {
			case 1:
				return "Canks Ambush!";
			case 2:
				return "Slow Down!";
			case 3:
				return "Speed Up!";
			case 4:
				return "Favoured Spacecraft!";
			case 5:
				return "Canks Airstrike!";
			case 6:
				return "Rotate!";
			case 7:
				return "Rethink!";
			case 8:
				return "Sneaky Canks!";
			case 9:
				return "Backup Plan!";
			default:
				return "Mulok Retreat!";
		}
	}

	private class Sign implements Updatable, Viewable {

		private int index;
		private int powerup;
		
		private float scale;
		private int offset;
		private float fade;
		
		private boolean inFront;
		private int animationTime;
		private int startTime;
		private int lastRead;
		private int timeSinceLastRead;
		
		private Texture t;
		private TextureCoords tc;

		public Sign(int powerup, int index) {
			this.index = index;
			this.powerup=powerup;
			inFront = (index == numScrolls - 1);
			scale = 0.8f;
			if(index!=0)
				offset = -height/2;
			else
				offset = 0;
			fade = 0.0f;
			//Calculates the time the sign can use to complete its animation
			animationTime = (totalTime / (numScrolls+2))*3;
			/* Calculates the time when this sign can start to animate
			starting with the one with highest index cause of the ease of having the
			final element as 0*/
			startTime = (animationTime/3) * (numScrolls - index - 1);
			lastRead=0;
			timeSinceLastRead=1;
		}

		private void move(int goalOffset, float goalScale, float goalFade, int timeLeft) {
			//Calculate the stepping speed of the movement depending on how much time is left
			//until the goal changes
			int offsetAnimationStep=1;
			float scaleAnimationStep = 0.01f;
			float fadeAnimationStep = 0.01f;
			float timePart=0.0f;
			if(timeLeft!=0) {
				timePart = (float)timeSinceLastRead/(float)timeLeft;
				offsetAnimationStep = (int)((goalOffset-offset)*timePart);
				scaleAnimationStep = (goalScale-scale)*timePart;
				fadeAnimationStep = (goalFade-fade)*timePart;
			}
			
			//Animate the offset of this sign
			if (goalOffset - 1 < offset + offsetAnimationStep && goalOffset + 1 > offset + offsetAnimationStep) {
				offset = goalOffset;
			} else {
				offset += offsetAnimationStep;
			}
			
			//Animate the scale of this sign
			if (goalScale - 0.01f < scale + scaleAnimationStep && goalScale + 0.01f > scale + scaleAnimationStep) {
				scale = goalScale;
			} else {
				scale += scaleAnimationStep;
			}
			
			//Animate the fade of this sign
			if (goalFade - 0.01f < fade + fadeAnimationStep && goalFade + 0.01f > fade + fadeAnimationStep) {
				fade = goalFade;
			} else {
				fade += fadeAnimationStep;
			}
		}

		@Override
		public boolean update() {
			int currentTime = timer.read();
			if (currentTime > startTime) {
				if (currentTime < startTime + animationTime) {
					//Calculate the frame (from 0-300) for the animation
					int animationFrame = (int) (300.0f * ((float) (currentTime - startTime) / (float) animationTime));
					int goalOffset = 0;
					float goalScale = 0.0f;
					float goalFade = 0.0f;
					//Animate
					if (animationFrame < 100) {
						goalOffset = 0;
						goalScale = 1.0f;
						goalFade = 1.0f;
					} else if(animationFrame < 200) {
						if (index != 0) {
							goalOffset = height/2;
							goalScale = 0.5f;
							goalFade = 0.7f;
						} else {
							goalOffset = 0;
							goalScale = 1.2f;
							goalFade = 1.0f;
						}
					} else {
						if (index != 0) {
							signs.get(index - 1).inFront = true;
							inFront = false;
							goalOffset = 0;
							goalScale = 0.0f;
							goalFade = 0.0f;
						} else {
							goalOffset = 0;
							goalScale = 2.0f;
							goalFade = 0.0f;
						}
					}
					//Calculate the time until the current animation ends on this sign
					int framesLeft = 100 - (animationFrame%100);
					int timeLeft = (framesLeft*animationTime)/300;
					move(goalOffset, goalScale, goalFade, timeLeft);
				} else {
					return true;
				}
			}
			//Calculates the time since the last call to update on this sign
			timeSinceLastRead=currentTime-lastRead;
			lastRead=currentTime;
			return false;
		}

		@Override
		public void view(GL gl) {
			gl.glColor4f(1.0f, 1.0f, 1.0f, fade);
			t = TextureManager.getInstance().getTexture(new File("Assets/Textures/GuiSpinner"+powerup+".png"));
			tc = t.getSubImageTexCoords(0, 0, width, height);

			t.enable();
			t.bind();
			int[] diff = {(int) ((width/2) * scale), (int) ((height/2) * scale)};
			gl.glBegin(gl.GL_QUADS);
			gl.glTexCoord2d(tc.right(), tc.top());
			gl.glVertex2i(diff[0], diff[1] + offset);
			gl.glTexCoord2d(tc.left(), tc.top());
			gl.glVertex2i(-diff[0], diff[1] + offset);
			gl.glTexCoord2d(tc.left(), tc.bottom());
			gl.glVertex2i(-diff[0], -diff[1] + offset);
			gl.glTexCoord2d(tc.right(), tc.bottom());
			gl.glVertex2i(diff[0], -diff[1] + offset);
			gl.glEnd();
			t.disable();
		}
	}
}