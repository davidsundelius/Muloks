package mouserunner.Model3D;

public class Polygon {
  private int flags;
	private int[] vertexIndices;
	private float[][] vertexNormals;
	private float[] s;
  private float[] t;
	private byte smoothingGroup;
	private byte groupIndex;
  
  public Polygon(int flags, int[] vertexIndices, float[][] vertexNormals, float[] s, float[] t, byte smoothingGroup, byte groupIndex) {
    this.flags=flags;
    this.vertexIndices=vertexIndices;
    this.vertexNormals=vertexNormals;
    this.s=s;
    this.t=t;
    this.smoothingGroup=smoothingGroup;
    this.groupIndex=groupIndex;
  }
  public final int getFlags() {
    return flags;
  }
  public final int[] getVertexIndices() {
    return vertexIndices;
  }
  public final float[][] getVertexNormals() {
    return vertexNormals;
  }
  public final float[] getS() {
    return s;
  }
  public final float[] getT() {
    return t;
  }
  public final byte getSmoothingGroup() {
    return smoothingGroup;
  }
  public final byte getGroupIndex() {
    return groupIndex;
  }
}