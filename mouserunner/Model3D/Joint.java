package mouserunner.Model3D;

public class Joint {
  private byte flags;
  private String name;
  private String parentName;
  private int parent;
  private float[] rotation;
  private float[] position;
  private KeyFrameRotation[] keyFramesRot;
  private KeyFramePosition[] keyFramesPos;
  private Matrix relative;
  private Matrix absolute;
  
  public Joint(final byte flags, final String name, final String parentName, final float[] rotation, final float[] position, final KeyFrameRotation[] keyFramesRot, final KeyFramePosition[] keyFramesPos) {
    this.flags=flags;
    this.name=name;
    this.parentName=parentName;
    this.rotation=rotation;
    this.position=position;
    this.keyFramesRot=keyFramesRot;
    this.keyFramesPos=keyFramesPos;
    parent=-1;
    relative=new Matrix();
    absolute=new Matrix();
  }
  
  public int getFlags() {
    return flags;
  }
  
  public String getName() {
    return name;
  }
  public String getParentName() {
    return parentName;
  }
  public int getParent() {
    return parent;
  }
  public float[] getRotation() {
    return rotation;
  }
  public float[] getPosition() {
    return position;
  }
  public KeyFrameRotation[] getKeyFramesRot() {
    return keyFramesRot;
  }
  public KeyFramePosition[] getKeyFramesPos() {
    return keyFramesPos;
  }
  public Matrix getRelative() {
    return relative;
  }
  public Matrix getAbsolute() {
    return absolute;
  }
  public void setParent(int parent){
    this.parent=parent;
  }    
}
