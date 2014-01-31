package mouserunner.Game;

import mouserunner.Managers.TextureManager;
import com.sun.opengl.util.texture.Texture;
import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;
import javax.media.opengl.GL;
import mouserunner.Managers.GameplayManager;

/**
 * SpecialFX handles the creation and animation of explosions and other effects
 * @author Zorek
 */
public class SpecialFX extends Billboard {

	private String name;
	private float particleRadius;
	private int numParts;
	private Texture texture;
	private Particle[] Parts;
	private Collection<MagneticPoint> MagPoints = new HashSet<MagneticPoint>();
	private float[] pos;
	private float[] gravity;
	private float[] maxVel;
	private String[] color;
	private float fade;
	private boolean respawn;

	/**
	 * The constructor for creating a specialfx
	 *
	 * @throws FileNotFoundException    throw if the FXfile is not found or could not be read
	 * @param FXfile  a File with path to the desired effect
	 * @param x  the x position of the effect
	 * @param y  the y position of the effect
	 * @param z  the z position of the effect
	 */
	public SpecialFX(String FXfile, float x, float y, float z) throws FileNotFoundException {
		name = FXfile;
		pos = new float[3];
		pos[0] = x;
		pos[1] = y;
		pos[2] = z;
		loadSFXFile(FXfile);
	}

	/**
	 * The constructor for cloning an effect
	 *
	 * @param sfx   A reference to the object that should be cloned
	 * @param x  the x position of the effect
	 * @param y  the y position of the effect
	 * @param z  the z position of the effect
	 */
	public SpecialFX(SpecialFX sfx, float x, float y, float z) {
		name = sfx.name;
		pos = new float[3];
		pos[0] = x;
		pos[1] = y;
		pos[2] = z;
		numParts = sfx.numParts;
		Parts = new Particle[numParts];
		particleRadius = sfx.particleRadius;
		gravity = sfx.gravity;
		maxVel = sfx.maxVel;
		fade = sfx.fade;
		respawn = sfx.respawn;

		Random rand = new Random();
		color = sfx.color;
		int[] icolor = new int[3];
		for (int i = 0; i < 3; i++) {
			if (!color[i].equals("rand")) {
				icolor[i] = Integer.parseInt(color[i]);
			}
		}
		for (int i = 0; i < numParts; i++) {
			for (int j = 0; j < 3; j++) {
				if (color[j].equals("rand")) {
					icolor[j] = rand.nextInt(255);
				}
			}
			Parts[i] = new Particle(this, new Color(icolor[0], icolor[1], icolor[2]));
		}

		texture = sfx.texture;
		for (MagneticPoint mp : sfx.MagPoints) {
			MagPoints.add(mp);
		}
	}

	/**
	 * Is this effect loaded from the given sfx-file?
	 * @param FXfile the given sfx-file
	 * @return true, if this effect is loaded from the given sfx-file
	 */
	public boolean isEffect(String FXfile) {
		return FXfile.equals(name);
	}

	/**
	 * Moves the spawningpoint of this effect with the given motion vector
	 * @param x the motion vector compostant on the x axis
	 * @param y the motion vector compostant on the y axis
	 * @param z the motion vector compostant on the z axis
	 */
	public void move(float x, float y, float z) {
		pos[0] += x;
		pos[1] += y;
		pos[2] += z;
	}

	/**
	 * Is called to draw the current state of the drawable object
	 *
	 * @param gl The object used to draw to the screen
	 */
	@Override
	public void view(GL gl) {
		if(GameplayManager.getInstance().gameTimer!=null) {
			gl.glPushMatrix();
			gl.glDepthMask(false);
			gl.glBlendFunc(gl.GL_SRC_ALPHA, gl.GL_ONE);
			texture = TextureManager.getInstance().getTexture(new File("Assets/Textures/Part.png"));
			texture.bind();
			for (Particle part : Parts) {
				part.view(gl);
			}
			gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			gl.glBlendFunc(gl.GL_SRC_ALPHA, gl.GL_ONE_MINUS_SRC_ALPHA);
			gl.glDepthMask(true);
			gl.glPopMatrix();
		}
	}

	/**
	 * Is called to update the current state of the updatable object
	 * @return If true, the effect is finished and can be removed
	 */
	@Override
	public boolean update() {
		boolean finished = true;
		for (Particle part : Parts) {
			if (!part.animate()) {
				finished = false;
			}
			for (MagneticPoint magPoint : MagPoints) {
				magPoint.updateParticle(part);
			}
		}
		return finished;
	}

	/**
	 * Gets the particle radius of the current effect
	 * 
	 * @return The particle radius
	 */
	protected float getParticleRadius() {
		return particleRadius;
	}

	/**
	 * Gets the spawn points position of the current effect
	 * 
	 * @return The spawn points position
	 */
	protected float[] getSpawnPoint() {
		return pos;
	}

	/**
	 * Gets the global gravity of the current effect
	 * 
	 * @return The global gravity
	 */
	protected float[] getGravity() {
		return gravity;
	}

	/**
	 * Gets the maximum paticle speed of the current effect
	 * 
	 * @return The maximum velocity
	 */
	protected float[] getMaxVel() {
		return maxVel;
	}

	/**
	 * Gets the fading constant of the current effect
	 * 
	 * @return The fading constant
	 */
	protected float getFade() {
		return fade;
	}

	/**
	 * Gets the respawn boolean of the given effect
	 * 
	 * @return If true, the particles will respawn when they are totally faded
	 */
	protected boolean getRespawn() {
		return respawn;
	}

	/**
	 * Gets the respawn boolean of the given effect
	 * 
	 * @param FXpath File path to effectscript
	 * @throws FileNotFoundException if file does not exist
	 * @return If true, the particles will respawn when they are totally faded
	 */
	private void loadSFXFile(String FXpath) throws FileNotFoundException {
		Scanner sc = new Scanner(new File(FXpath));
		sc.useLocale(new Locale("en-US"));
		numParts = sc.nextInt();
		Parts = new Particle[numParts];
		particleRadius = sc.nextFloat();
		gravity = new float[3];
		for (int i = 0; i < 3; i++) {
			gravity[i] = sc.nextFloat();
		}
		maxVel = new float[3];
		for (int i = 0; i < 3; i++) {
			maxVel[i] = sc.nextFloat();
		}
		fade = sc.nextFloat();
		respawn = sc.nextBoolean();

		Random rand = new Random();
		color = new String[3];
		int[] icolor = new int[3];
		for (int i = 0; i < 3; i++) {
			color[i] = sc.next();
			if (!color[i].equals("rand")) {
				icolor[i] = Integer.parseInt(color[i]);
			}
		}

		for (int i = 0; i < numParts; i++) {
			for (int j = 0; j < 3; j++) {
				if (color[j].equals("rand")) {
					icolor[j] = rand.nextInt(255);
				}
			}
			Parts[i] = new Particle(this, new Color(icolor[0], icolor[1], icolor[2]));
		}

		sc.nextLine();
		while (sc.hasNextLine()) {
			Scanner lineReader = new Scanner(sc.nextLine());
			lineReader.useLocale(new Locale("en-US"));
			MagPoints.add(new MagneticPoint(lineReader.nextFloat(), lineReader.nextFloat(), lineReader.nextFloat(), lineReader.nextFloat()));
		}
	}
}

class MagneticPoint {

	SpecialFX belongsTo;
	private float[] pos;
	private float gravity;

	public MagneticPoint(final float x, final float y, final float z, final float gravity) {
		pos = new float[3];
		pos[0] = x;
		pos[1] = y;
		pos[2] = z;
		this.gravity = gravity;
	}

	protected void updateParticle(Particle part) {
		float[] vec = new float[3];
		for (int i = 0; i < 3; i++) {
			vec[i] = part.pos[i] - pos[i];
		}
		float abs = (float) Math.abs(Math.sqrt(vec[0] * vec[0] + vec[1] * vec[1] + vec[2] * vec[2]));
		if (abs != 0) {
			vec[0] /= abs;
			vec[1] /= abs;
			vec[2] /= abs;
			part.vel[0] -= vec[0] * gravity;
			part.vel[1] -= vec[1] * gravity;
			part.vel[2] -= vec[2] * gravity;
		}
	}
}

class Particle {

	SpecialFX belongsTo;
	protected float[] pos;
	protected float[] vel;
	private Color color;
	private float life;

	public Particle(final SpecialFX sfx, final Color color) {
		belongsTo = sfx;
		this.color = color;
		spawn();
	}

	protected boolean animate() {
		vel[0] += belongsTo.getGravity()[0];
		vel[1] += belongsTo.getGravity()[1];
		vel[2] += belongsTo.getGravity()[2];
		pos[0] += vel[0];
		pos[1] += vel[1];
		pos[2] += vel[2];
		life -= belongsTo.getFade();
		if (life < 0.0f) {
			if (belongsTo.getRespawn()) {
				spawn();
			} else {
				return true;
			}
		}
		return false;
	}

	protected void view(final GL gl) {
		gl.glPushMatrix();
		gl.glTranslatef(pos[0] + belongsTo.getSpawnPoint()[0], pos[1] + belongsTo.getSpawnPoint()[1], pos[2] + belongsTo.getSpawnPoint()[2]);
		belongsTo.setBillboardMatrix(gl);
		gl.glColor4f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, life);
		gl.glBegin(gl.GL_QUADS);
		gl.glTexCoord2f(0.0f, 1.0f);
		gl.glVertex3f(-belongsTo.getParticleRadius(), belongsTo.getParticleRadius(), 0.0f);
		gl.glTexCoord2f(1.0f, 1.0f);
		gl.glVertex3f(belongsTo.getParticleRadius(), belongsTo.getParticleRadius(), 0.0f);
		gl.glTexCoord2f(1.0f, 0.0f);
		gl.glVertex3f(belongsTo.getParticleRadius(), -belongsTo.getParticleRadius(), 0.0f);
		gl.glTexCoord2f(0.0f, 0.0f);
		gl.glVertex3f(-belongsTo.getParticleRadius(), -belongsTo.getParticleRadius(), 0.0f);
		gl.glEnd();
		gl.glPopMatrix();
	}

	private void spawn() {
		Random rand = new Random();
		float[] newPos = {0, 0, 0};
		pos = newPos;
		float xyAngle = (float) (rand.nextFloat() * 2 * Math.PI);
		float zAngle = (float) (rand.nextFloat() * 2 * Math.PI);
		float[] newVel = {(float) Math.cos(xyAngle) * belongsTo.getMaxVel()[0] * rand.nextFloat(), (float) Math.sin(xyAngle) * belongsTo.getMaxVel()[1] * rand.nextFloat(), (float) Math.sin(zAngle) * belongsTo.getMaxVel()[2] * rand.nextFloat()};
		vel = newVel;
		life = rand.nextFloat();
	}
}