package mouserunner.Game;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import mouserunner.System.Direction;

/**
 * Controls the camera ingame
 * @author Zorek
 */
public class Camera {
  private GLU glu = new GLU();
  
  private Direction direction;
  private float[] position=new float[3];
  protected double rot; /** the rotation value for the camera */
  private boolean lock;
  private boolean spin;
  
  /**
   * Creates a new camera and sets its initial values
	 * @param inMenu True, if the camera is used for a menu
   */
  public Camera(boolean inMenu) {
    direction=Direction.DOWN;
    lock=false;
    spin=false;
    rot=Math.PI*1.5;
		if(inMenu)
			position[2]=150.0f;
		else
			position[2]=10.0f;
		update();
  }

  /**
   * Updates the camers position, moves it according to the current destination
   * direction
   * @return always false
   */
  public boolean update() {
    if(!lock) {
      if(spin)
        rot+=0.001f;
      else
        moveCamera();
      position[0] = ((float)Math.cos(rot) * 16.0f * Level.tileSize) + 8.0f * Level.tileSize;
      position[1] = ((float)Math.sin(rot) * 12.0f * Level.tileSize) - 6.0f * Level.tileSize;
    }
    return false;
  }

  /**
   * Method to multiply the cameramatrix with the modelview matrix
   * to get a camera effect
   * @param gl The current gl context
   */
  public void setCamera(GL gl) {
		gl.glTranslatef(0.0f, 10.0f, 0.0f);
    glu.gluLookAt(position[0],          position[1],          position[2],  // Camera position
                   8.0f*Level.tileSize, -6.0f*Level.tileSize,  -5.0f,       // Lookat position
                   0.0f,                0.0f,                 1.0f);        // Up vector
  }
  
  /**
   * Gets the direction of which the camera is now, from the boards view
   * @return the current relative direction of the camera, from the board
   */
  public Direction getDirection() {
    return direction;
  }
  
  /**
   * Sets a new destination of the camera. It will move towards this every
   * update frame.
   * @param direction the new direction
   */
  public void setDirection(Direction direction) {
    lock=false;
    this.direction = direction;
  }

  /**
   * Calculates and moves the camera towards the current destination
   */
  private void moveCamera() {
    //Make the rotation angle fit in the interval [0.0 , 2pi]
    if(rot>Math.PI*2) rot-=Math.PI*2;
    if(rot<0.0) rot+=Math.PI*2;
    
    //Calculate new camera angle
    if(direction==Direction.LEFT) {
      if(rot>Math.PI-0.1&&rot<Math.PI+0.1) {
        rot=Math.PI;
        lock=true;
      }
      else if(rot<Math.PI)
        rot+=0.01;
      else
        rot-=0.01;
    }
    else if(direction==Direction.RIGHT)
      if(rot>2*Math.PI-0.1||rot<0.1) {
        rot=0.0;
        lock=true;
      }
      else if(rot<Math.PI)
        rot-=0.01;
      else
        rot+=0.01;
    else if(direction==Direction.UP)
      if(rot>Math.PI/2-0.1&&rot<Math.PI/2+0.1){
        rot=Math.PI/2;
        lock=true;
      }
      else if(rot<Math.PI*1.5 && rot>Math.PI/2)
        rot-=0.01;
      else
        rot+=0.01;
    else if(direction==Direction.DOWN)
      if(rot>Math.PI*1.5-0.1&&rot<Math.PI*1.5+0.1) {
        rot=Math.PI*1.5;
        lock=true;
      }
      else if(rot>0.0 && rot<Math.PI*1.5)
        rot+=0.01;
      else
        rot-=0.01;
  }

  /**
   * Zooms the camera out if it's under the distance of 20.0f
   */
  public void updateZoom() {
    if(position[2]<150.0)
      position[2]+=2.0;
  }

  /**
   * Set the spinning of the camera, if its set to true the camera circles
   * around the board
   * @param spin if true, the camera spins
   */
  public void setSpin(boolean spin) {
    this.spin=spin;
    lock=!spin;
  }
}
