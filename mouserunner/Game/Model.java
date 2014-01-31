package mouserunner.Game;

import mouserunner.System.Viewable;
import mouserunner.System.Updatable;
import mouserunner.Managers.ModelManager;
import com.sun.opengl.util.texture.Texture;
import javax.media.opengl.GL;
import mouserunner.Model3D.Group;
import mouserunner.Model3D.Header;
import mouserunner.Model3D.Joint;
import mouserunner.Model3D.Material;
import mouserunner.Model3D.Matrix;
import mouserunner.Model3D.Polygon;
import mouserunner.Model3D.Vertex;

/**
 * A class that represents a viewable (may be animated) 3dmodel in the game
 * @author Zorek
 */
public class Model implements Updatable, Viewable {
	private Header header;
	private Vertex[] vertices;
	private Polygon[] polygons;
	private Group[] groups;
	private Material[] materials;
	private boolean animated;
	private float time;
	private Joint[] joints;
	private Object[] keyframes;
	
	private int displayListName;

	/**
	 * Constructs a new static model (not animated)
	 */
	public Model(Header header, Vertex[] vertices, Polygon[] polygons, Group[] groups, Material[] materials) {
		this.header = header;
		this.vertices = vertices;
		this.polygons = polygons;
		this.groups = groups;
		this.materials = materials;
		animated = false;
		time = 0.0f;
		this.joints = null;
		this.keyframes = null;
		displayListName = -1;
	}

	/**
	 * Assigns a skeleton to the model and puts it in animationmode
	 * @param joints the skeleton that is being applyed
	 */
	public void applySkeleton(Joint[] joints) {
		animated = true;
		this.joints = joints;
	}

	/**
	 * Method called to view the gui on the screen
	 * @param gl The current gl context
	 */
	@Override
	public void view(GL gl) {
		if (displayListName == -1) {
			displayListName = ModelManager.getInstance().getDisplayList(this, gl);
		}
		gl.glCallList(displayListName);
	}

	/**
	 * Builds the displaylists for opengl to later draw in the view method
	 * Is currently used to draw the model since DisplayLists buggs with
	 * ATIcards
	 * @param gl The current gl context
	 */
	public void generateDisplayList(GL gl) {
		//View model
		gl.glEnable(gl.GL_BLEND);
		for (Group group : groups) {
			Texture texture = materials[group.getMaterialIndex()].getTexture();
			if(texture!=null) {
				texture.enable();
				texture.bind();
			} else {
				gl.glMaterialfv(gl.GL_FRONT, gl.GL_AMBIENT, materials[group.getMaterialIndex()].getAmbient(), 0);
				gl.glMaterialfv(gl.GL_FRONT, gl.GL_DIFFUSE, materials[group.getMaterialIndex()].getDiffuse(), 0);
				gl.glMaterialfv(gl.GL_FRONT, gl.GL_SPECULAR, materials[group.getMaterialIndex()].getSpecular(), 0);
				gl.glMaterialfv(gl.GL_FRONT, gl.GL_EMISSION, materials[group.getMaterialIndex()].getEmissive(), 0);
				gl.glMaterialf(gl.GL_FRONT, gl.GL_SHININESS, materials[group.getMaterialIndex()].getShininess());
				gl.glDisable(gl.GL_TEXTURE_2D);
			}
			for (int i : group.getPolygonIndices()) {
				gl.glBegin(gl.GL_TRIANGLES);
				for (int j = 0; j < 3; j++) {
					int vertexIndex = polygons[i].getVertexIndices()[j];
					gl.glNormal3fv(polygons[i].getVertexNormals()[j], 0);
					gl.glTexCoord2f(polygons[i].getS()[j], polygons[i].getT()[j]);
					gl.glVertex3f(vertices[vertexIndex].getLocation()[0], vertices[vertexIndex].getLocation()[1], vertices[vertexIndex].getLocation()[2]);
				}
				gl.glEnd();
			}
		}
		
		//View outline
		gl.glDisable(gl.GL_TEXTURE_2D);
		float[] colors = new float[4];
		gl.glGetFloatv(gl.GL_CURRENT_COLOR, colors, 0);
		gl.glColor4f(0.0f, 0.0f, 0.0f, colors[3]);
		gl.glLineWidth(1.5f);
		gl.glPolygonMode(gl.GL_BACK, gl.GL_LINE);
		gl.glCullFace(gl.GL_FRONT);
		gl.glEnable(gl.GL_CULL_FACE);
		for (Group groupOutline : groups) {
			for (int i : groupOutline.getPolygonIndices()) {
				gl.glBegin(gl.GL_TRIANGLES);
				for (int j = 0; j < 3; j++) {
					int vertexIndex = polygons[i].getVertexIndices()[j];
					gl.glVertex3f(vertices[vertexIndex].getLocation()[0], vertices[vertexIndex].getLocation()[1], vertices[vertexIndex].getLocation()[2]);
				}
				gl.glEnd();
			}
		}
		
		//Restore GL settings
		gl.glPolygonMode(gl.GL_FRONT_AND_BACK, gl.GL_FILL);
		gl.glCullFace(gl.GL_BACK);
		gl.glDisable(gl.GL_CULL_FACE);
		gl.glEnable(gl.GL_TEXTURE_2D);
	}

	/**
	 * Check to see if the model is animated
	 * @return is this model animated?
	 */
	public boolean isAnimated() {
		return animated;
	}

	/**
	 * Gets a reference to the array of all the vertices for this model
	 * used for setting up animation in ModelManager
	 * @return list of the vertices for the model
	 */
	public Vertex[] getVertices() {
		return vertices;
	}

	/**
	 * Gets a reference to the array of all the joints for this model
	 * used for setting up animation in ModelManager
	 * @return list of the joints for the model
	 */
	public Joint[] getJoints() {
		return joints;
	}

	/**
	 * Updates animation for a object (Not supported yet)
	 * @return Is this object done with the current animation?
	 */
	@Override
	public boolean update() {
		//Animate does not work for now
		for (int i = 0; i < joints.length; i++) {
			Matrix animationMatrix = new Matrix();
			//Calculate keyframe for translation
			if (joints[i].getKeyFramesPos().length > 0) {
				animationMatrix.translate(joints[i].getKeyFramesPos()[0].getPosition());
			}
			//Calculate keyframe for rotation
			if (joints[i].getKeyFramesRot().length > 0) {
				animationMatrix.rotate(joints[i].getKeyFramesRot()[0].getRotation());
			}
			joints[i].getAbsolute().multiply(animationMatrix);
		}

		//Transform all vertices relative to the corresponding joint
		for (int i = 0; i < vertices.length; i++) {
			if (vertices[i].getBoneID() != -1) {
				vertices[i].setLocation(joints[vertices[i].getBoneID()].getAbsolute().transform(vertices[i].getLocation()));
			}
		}
		return false;
	}
}
