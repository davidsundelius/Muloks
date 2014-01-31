package mouserunner.System;

import mouserunner.Managers.StateManager;
import com.sun.opengl.util.FPSAnimator;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;

/**
 * A view class - views everything to the screen through opengl
 * @author Zorek
 */
public class Viewer implements GLEventListener{
  private GLCanvas canvas;
  private FPSAnimator animator;

  /**
   * Creates a new updater
   * @param canvas a initialized GLcanvas that will be used to view the states
   * @param fps the amount of frames per second that will view the state
   */
  public Viewer(GLCanvas canvas, int fps) {
    this.canvas=canvas;
    animator = new FPSAnimator(canvas,fps);
    animator.start();
  }

  /**
   * Initializes opengl and sets up the current enviroment according to 
   * settings and requirements
   * @param drawable the current drawable object sent from JOGL
   */
	@Override
  public void init(GLAutoDrawable drawable) {
		System.out.println("Viewer started initialization of OpenGL");
    GL gl = drawable.getGL();
		
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		gl.glShadeModel(GL.GL_SMOOTH);
    gl.glEnable(gl.GL_TEXTURE_2D);
    gl.glColor4f(1.0f, 1.0f, 1.0f, 0.5f);
    gl.glBlendFunc(gl.GL_SRC_ALPHA, gl.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(gl.GL_BLEND);
    gl.glClearDepth(1.0f);
    gl.glEnable(gl.GL_DEPTH_TEST);
    gl.glDepthFunc(gl.GL_LEQUAL);
    gl.glHint(GL.GL_LINE_SMOOTH_HINT, gl.GL_NICEST);
    gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, gl.GL_NICEST);
		gl.glEnable (gl.GL_LINE_SMOOTH);
		System.out.println("Viewer completed setup of OpenGL");
  }

  /**
   * Draws everything from the current state
   * @param drawable the current drawable object sent from JOGL
   */
	@Override
  public void display(GLAutoDrawable drawable) {
    GL gl = drawable.getGL();
    gl.glLoadIdentity();
    StateManager.getInstance().viewState(gl);
		gl.glFlush();
  }

  /**
   * If the window is changed, reshaped or moved this will be called
   * and resets the variables that is affected to fit the new
   * screen size
   * @param drawable the current drawable object sent from JOGL
   * @param x the new x value sent from JOGL
   * @param y the new y value sent from JOGL
   * @param width the new width value sent from JOGL
   * @param height the new height value sent from JOGL
   */
	@Override
  public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		System.out.println("Viewer recieved order to reshape canvas to: x="+x+", y="+y+", w="+width+", h="+height);
    final GL gl = drawable.getGL();
    final GLU glu = new GLU();
    if (height <= 0) height = 1;
    final float ratio = (float)width / (float)height;
    gl.glViewport(0, 0, width, height);
    gl.glMatrixMode(GL.GL_PROJECTION);
    gl.glLoadIdentity();
    glu.gluPerspective(45.0f, ratio, 1.0, 1000.0);
    gl.glMatrixMode(GL.GL_MODELVIEW);
    gl.glLoadIdentity();
		System.out.println("Viewer completed reshape without any trouble");
  }
  
  /**
   * Not yet implemented in JOGL
   * @param arg1 Don't know
   * @param arg2 Don't know
   */
	@Override
  public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2) {
  }

}

