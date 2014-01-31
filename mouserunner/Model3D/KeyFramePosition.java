package mouserunner.Model3D;

public class KeyFramePosition {
  private float time;
  private float[] position;
  
  public KeyFramePosition(float time, float[] position) {
    this.time=time;
    this.position=position;
  }
  
  public float getTime() {
    return time;
  }
  
  public float[] getPosition() {
    return position;
  }
}
