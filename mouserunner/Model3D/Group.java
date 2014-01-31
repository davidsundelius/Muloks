package mouserunner.Model3D;

public class Group {
  private byte flags;
  private String name;
  private int[] polygonIndices;
  private byte materialIndex;
  
  public Group(byte flags, String name, int[] polygonIndices, byte materialIndex) {
    this.flags=flags;
    this.name=name;
    this.polygonIndices=polygonIndices;
    this.materialIndex=materialIndex;
  }
  public final byte getFlags() {
    return flags;
  }
  public final String getName() {
    return name;
  }
  public final int[] getPolygonIndices() {
    return polygonIndices;
  }
  public final byte getMaterialIndex() {
    return materialIndex;
  }
}
