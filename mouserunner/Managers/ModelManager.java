package mouserunner.Managers;

import mouserunner.Game.Model;
import mouserunner.Model3D.*;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.media.opengl.GL;

/**
 * ModelManager (singleton) reads models from ms3d-files (milkshape) and returns them as Model.
 * Also controls that only one model is loaded of the same type.
 * @author Zorek
 */
public class ModelManager {

  private static ModelManager instance = new ModelManager();
  private Map<String, Model> models = new HashMap<String, Model>();
  private Map<Model, Integer> displayLists = new HashMap<Model, Integer>();
  private DataReader instream;

  /**
   * Empty internal constructor for the singleton
   */
  private ModelManager() {
  }

  /**
   * Retreave the singleton reference
   * @return the single instance
   */
  public static ModelManager getInstance() {
    return instance;
  }

  /**
   * Gets a model from a file, if this model has already been loaded
   * load the same from the cache list with loaded models
   * @param file the file which contains the model (*.ms3d)
   * @param animated is the model animated?
   * @return the loaded model
   */
  public Model getModel(File file, boolean animated) {
    final Model newModel;
    if (!models.containsKey(file.getPath())) {
      newModel = loadModel(file, animated);
      models.put(file.getPath(), newModel);
    } else {
      newModel = models.get(file.getPath());
    }
    return newModel;
  }

  /**
   * Retreave the display list identifier for a chosen model.
   * If it hasn't been generated yet, generate now
   * @param model the model that we want to get a displaylist for
   * @param gl referense to the current GL context
   * @return the identifier for the display list
   */
  public int getDisplayList(Model model, GL gl) {
    final Integer newInt;
    if (!(displayLists.containsKey(model))) {
      newInt = buildDisplayLists(model, gl);
      displayLists.put(model, newInt);
    } else {
      newInt = displayLists.get(model);
    }
    return newInt;
  }

  /**
   * Generates a display list for a chosen model
   * @param model the model that we want to get a displaylist for
   * @param gl referense to the current GL context
   * @return the identifier for the new display list
   */
  private int buildDisplayLists(Model model, GL gl) {
    int listName = gl.glGenLists(1);
    gl.glNewList(listName, gl.GL_COMPILE);
    model.generateDisplayList(gl);
    gl.glEndList();
    return listName;
  }

  /**
   * Loads a new MS3D model
   * @param file the file which contains the model (*.ms3d)
   * @param animated is the model animated?
   * @return the loaded model
   */
  private Model loadModel(File file, boolean animated) {
    try {
      instream = new DataReader(new FileInputStream(file));
      Model newModel = new Model(loadHeader(), loadVertices(), loadPolygons(), loadGroups(), loadMaterials());
      if (animated) {
        final float fps = instream.readFloat();
        final float time = instream.readFloat();
        final int totalFrames = instream.readInt();
        final Joint[] joints = loadJoints();
        newModel.applySkeleton(joints);
        setupAnimation(newModel);
      }
      return newModel;
    } catch (FileNotFoundException e) {
      System.err.println("Could not find the model:" + file.getPath());
    } catch (IOException e) {
      System.err.println("Could not read from modelfile: " + file.getPath());
    }
    return null;
  }
  
  /**
   * Loads the header of a MS3D file
   * @throws IOException if the file could not be read properly
   * @return the loaded header
   */
  private Header loadHeader() throws IOException {
    final byte id[] = new byte[10];
    instream.read(id, 0, id.length);
    return new Header(id, instream.readInt());
  }

  /**
   * Loads the vertices of a MS3D file
   * @throws IOException if the file could not be read properly
   * @return the loaded vertices
   */
  private Vertex[] loadVertices() throws IOException {
    final int numVerts = this.instream.readUnsignedShort();
    final Vertex verteces[] = new Vertex[numVerts];
    for (int i = 0; i < numVerts; i++) {
      final byte flags = instream.readByte();
      final float[] position = {instream.readFloat(), instream.readFloat(), instream.readFloat()};
      final byte boneID = instream.readByte();
      final byte index = instream.readByte();
      verteces[i] = new Vertex(flags, position, boneID, index);
    }
    return verteces;
  }

  /**
   * Loads the polygons of a MS3D file
   * @throws IOException if the file could not be read properly
   * @return the loaded polygons
   */
  private Polygon[] loadPolygons() throws IOException {
    final int numPoly = instream.readUnsignedShort();
    final Polygon[] polygons = new Polygon[numPoly];
    for (int i = 0; i < numPoly; i++) {
      final int flags = instream.readUnsignedShort();
      final int vertIndices[] = {instream.readUnsignedShort(), instream.readUnsignedShort(), instream.readUnsignedShort()};
      final float[][] normals = new float[3][];
      for (int j = 0; j < 3; j++) {
        normals[j] = new float[3];
        for (int k = 0; k < 3; k++) {
          normals[j][k] = instream.readFloat();
        }
      }
      final float s[] = {instream.readFloat(), instream.readFloat(), instream.readFloat()};
      final float t[] = {instream.readFloat(), instream.readFloat(), instream.readFloat()};
      final byte smoothingGroup = instream.readByte();
      final byte groupIndex = instream.readByte();
      polygons[i] = new Polygon(flags, vertIndices, normals, s, t, smoothingGroup, groupIndex);
    }
    return polygons;
  }

  /**
   * Loads the groups of a MS3D file
   * @throws IOException if the file could not be read properly
   * @return the loaded groups
   */
  private Group[] loadGroups() throws IOException {
    final int numGroups = instream.readUnsignedShort();
    final Group groups[] = new Group[numGroups];
    for (int i = 0; i < numGroups; i++) {
      final byte flags = instream.readByte();
      final byte bName[] = new byte[32];
      instream.read(bName, 0, bName.length);
      final String name = instream.makeSafeString(bName);
      final int numIndices = instream.readUnsignedShort();
      final int indices[] = new int[numIndices];
      for (int j = 0; j < numIndices; j++) {
        indices[j] = instream.readUnsignedShort();
      }
      final byte materialIndex = instream.readByte();
      groups[i] = new Group(flags, name, indices, materialIndex);
    }
    return groups;
  }

  /**
   * Loads the materials of a MS3D file
   * @throws IOException if the file could not be read properly
   * @return the loaded materials
   */
  private Material[] loadMaterials() throws IOException {
    final int numMaterials = instream.readUnsignedShort();
    final Material materials[] = new Material[numMaterials];
    for (int i = 0; i < numMaterials; i++) {
      final byte[] bname = new byte[32];
      instream.read(bname, 0, bname.length);
      final String name = instream.makeSafeString(bname);
      final float[] ambient = {instream.readFloat(), instream.readFloat(), instream.readFloat(), instream.readFloat()};
      final float[] diffuse = {instream.readFloat(), instream.readFloat(), instream.readFloat(), instream.readFloat()};
      final float[] specular = {instream.readFloat(), instream.readFloat(), instream.readFloat(), instream.readFloat()};
      final float[] emissive = {instream.readFloat(), instream.readFloat(), instream.readFloat(), instream.readFloat()};
      final float shininess = instream.readFloat();
      final float transparency = instream.readFloat();

      final byte mode = instream.readByte();
      final byte btexture[] = new byte[128];
      instream.read(btexture, 0, btexture.length);
      String texture = instream.makeSafeString(btexture);
      if (!texture.equals("")) {
        texture = texture.substring(2, texture.length());
				texture=texture.replace('\\', '/'); //Fixes wierd windows-filepaths
      } else {
        texture = null;
      }
      final byte balphaMap[] = new byte[128];
      instream.read(balphaMap, 0, balphaMap.length);
      String alphaMap = instream.makeSafeString(balphaMap);
      materials[i] = new Material(name, ambient, diffuse, specular, emissive, shininess, transparency, mode, texture, alphaMap);
    }
    return materials;
  }

  /**
   * Loads the joints of a MS3D file
   * @throws IOException if the file could not be read properly
   * @return the loaded jointss
   */
  private Joint[] loadJoints() throws IOException {
    //Load joints from file
    final int numJoints = this.instream.readUnsignedShort();
    final Joint[] joints = new Joint[numJoints];
    for (int i = 0; i < numJoints; i++) {
      final byte flags = instream.readByte();

      final byte bname[] = new byte[32];
      instream.read(bname, 0, bname.length);
      final String name = instream.makeSafeString(bname);

      final byte bparent[] = new byte[32];
      this.instream.read(bparent, 0, bparent.length);
      final String parent = instream.makeSafeString(bparent);

      final float[] jRotation = {instream.readFloat(), instream.readFloat(), instream.readFloat()};
      final float[] jPosition = {instream.readFloat(), instream.readFloat(), instream.readFloat()};

      final int numKeyFramesRot = instream.readUnsignedShort();
      final int numKeyFramesPos = instream.readUnsignedShort();
      
      final KeyFrameRotation keyFramesRot[] = new KeyFrameRotation[numKeyFramesRot];
      final KeyFramePosition keyFramesPos[] = new KeyFramePosition[numKeyFramesPos];

      for (int j = 0; j < numKeyFramesRot; j++) {
        final float time = instream.readFloat();
        final float[] kRotation = {instream.readFloat(), instream.readFloat(), instream.readFloat()};
        keyFramesRot[j] = new KeyFrameRotation(time, kRotation);
      }
      for (int j = 0; j < numKeyFramesPos; j++) {
        final float time = instream.readFloat();
        final float[] kPosition = {instream.readFloat(), instream.readFloat(), instream.readFloat()};
        keyFramesPos[j] = new KeyFramePosition(time, kPosition);
        
      }
      joints[i] = new Joint(flags, name, parent, jRotation, jPosition, keyFramesRot, keyFramesPos);
    }

    //Generate a parent tree
    for(int i=0;i<joints.length;i++) {
      if(joints[i].getParentName().equals(""))
        joints[i].setParent(-1);
      else
        for(int j=0;j<joints.length;j++)
          if(joints[i].getParentName().equals(joints[j].getName())) {
            joints[i].setParent(j);
            break;
          }
    }
    return joints;
  }

  /**
   * Sets up the animation for a given model and activates its animation properties
   * @param the given model
   */
  private void setupAnimation(Model model) {
    Joint[] joints = model.getJoints();
    
    for(Joint j: joints){
      //Rotate/translate all joints locally
      j.getRelative().translate(j.getPosition());
      j.getRelative().rotate(j.getRotation());
      //Rotate/translate all joints relative to parents
      if(j.getParent()==-1) {
        j.getAbsolute().setMatrix(j.getRelative());
      } else {
        j.getAbsolute().setMatrix(joints[j.getParent()].getAbsolute());
        j.getAbsolute().multiply(j.getRelative());
      }
    }
    
    //Transform all vertices relative to the corresponding joint
    Vertex[] vertices = model.getVertices();
    for(int i=0;i<vertices.length;i++)
      if(vertices[i].getBoneID()!=-1)
        joints[vertices[i].getBoneID()].getAbsolute().transform(vertices[i].getLocation());
    //(Rotate all corresponding normals) not implemented since we are not using light in this game
    return;
  }
}

//Datareader for reading binary files
class DataReader extends FilterInputStream implements DataInput {

  private DataInputStream dis;

  public DataReader(InputStream in) {
    super(in);
    dis = new DataInputStream(in);
  }

	@Override
  public boolean readBoolean() throws IOException {
    return dis.readBoolean();
  }

	@Override
  public byte readByte() throws IOException {
    return dis.readByte();
  }

	@Override
  public char readChar() throws IOException, EOFException {
    return dis.readChar();
  }

	@Override
  public double readDouble() throws IOException, EOFException {
    return Double.longBitsToDouble(readLong());
  }

	@Override
  public float readFloat() throws IOException, EOFException {
    return Float.intBitsToFloat(readInt());
  }

	@Override
  public void readFully(byte[] b) throws IOException, EOFException {
    dis.readFully(b);
  }

	@Override
  public void readFully(byte[] b, int off, int len) throws IOException, EOFException {
    dis.readFully(b, off, len);
  }

	@Override
  public int readInt() throws IOException, EOFException {
    int res = 0;
    for (int shiftBy = 0; shiftBy < 32; shiftBy += 8) {
      res |= (dis.readByte() & 0xff) << shiftBy;
    }
    return res;
  }

	@Override
  public String readLine() throws IOException {
    return null;
  }

	@Override
  public long readLong() throws IOException, EOFException {
    long res = 0;
    for (int shiftBy = 0; shiftBy < 64; shiftBy += 8) {
      res |= (dis.readByte() & 0xff) << shiftBy;
    }
    return res;
  }

	@Override
  public short readShort() throws IOException, EOFException {
    final int low = readByte() & 0xff;
    final int high = readByte() & 0xff;
    return (short) (high << 8 | low);
  }

	@Override
  public String readUTF() throws IOException {
    return dis.readUTF();
  }

	@Override
  public int readUnsignedByte() throws IOException, EOFException {
    return dis.readUnsignedByte();
  }

	@Override
  public int readUnsignedShort() throws IOException {
    final int low = readByte() & 0xff;
    final int high = readByte() & 0xff;
    return (high << 8 | low);
  }

	@Override
  public int skipBytes(int n) throws IOException {
    return dis.skipBytes(n);
  }

  public final String makeSafeString(final byte buffer[]) {
    final int len = buffer.length;
    for (int i = 0; i < len; i++) {
      if (buffer[i] == (byte) 0) {
        return new String(buffer, 0, i);
      }
    }
    return new String(buffer);
  }
}