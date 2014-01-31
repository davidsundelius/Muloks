package mouserunner.Model3D;

public class Header
{
  private byte id[];
  private int version;
  
  public Header(final byte id[], final int version) {
    setID(id);
    setVersion(version);
  }
  public final byte[] getID() {
    return id;
  }
  public final int getVersion() {
    return version;
  }
  private final void setID(final byte id[]) {
    if (id.length != 10)
      throw new IllegalArgumentException("MS3DHeader id length should be 10, is " + id.length);
    if (!"MS3D000000".equals(new String(id)))
      throw new IllegalArgumentException("MS3DHeader id \"" + id + "\" invalid");
    this.id = id;
  }
  private final void setVersion(final int version) {
    if (version < 3 | version > 4)
      throw new IllegalArgumentException("MS3DHeader version " + version + " unsupported");
    this.version = version;
  }
}
