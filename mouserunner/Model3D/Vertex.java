package mouserunner.Model3D;

public class Vertex {
  private byte flags;
	private float[] position;
	private byte boneID;
  private byte index;

  
  public Vertex(final byte flags, final float[] position, final byte boneID, final byte index) {
    this.flags=flags;
    this.position=position;
    this.boneID=boneID;
    this.index=index;
  }
  public final byte getFlags() {
    return flags;
  }
  public final float[] getLocation(){
    return position;
  }
	public void setLocation(float[] pos){
    position=pos;
  }
  public final byte getBoneID() {
    return boneID;
  }
  public final byte getRefCount(){
    return index;
  }
}
