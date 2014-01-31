package mouserunner.Model3D;

public class KeyFrameRotation {
  private float time;
  private float[] rotation;
  
  public KeyFrameRotation(float time, float[] rotation) {
    this.time=time;
    this.rotation=rotation;
  }
  
  public float getTime() {
    return time;
  }
  
  public float[] getRotation() {
    return rotation;
  }
}
