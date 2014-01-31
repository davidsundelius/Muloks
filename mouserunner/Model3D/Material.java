package mouserunner.Model3D;

import com.sun.opengl.util.texture.Texture;
import java.io.File;
import mouserunner.Managers.TextureManager;

public class Material {
  private String name;
  private float[] ambient;
  private float[] diffuse;
  private float[] specular;
  private float[] emissive;
  private float shininess;
  private float transparency;
  private byte mode;
  private String stexture;
  private Texture texture;
  private String salphaMap;
  private Texture alphaMap;
  
  public Material(String name, float[] ambient, float[] diffuse, float[] specular, float[] emissive, float shininess, float transparency, byte mode, String texture, String alphaMap) {
    this.name=name;
    this.ambient=ambient;
    this.diffuse=diffuse;
    this.specular=specular;
    this.emissive=emissive;
    this.shininess=shininess;
    this.transparency=transparency;
    this.mode=mode;
    this.stexture=texture;
    this.texture=null;
    this.salphaMap=alphaMap;
    this.alphaMap=null;
  }
  
  public final String getName() {
    return name;
  }
  public final float[] getAmbient() {
    return ambient;
  }
  public final float[] getDiffuse() {
    return diffuse;
  }
  public final float[] getSpecular() {
    return specular;
  }
  public final float[] getEmissive() {
    return emissive;
  }
  public final float getShininess() {
    return shininess;
  }
  public final float getTransparency() {
    return transparency;
  }
  public final byte getMode() {
    return mode;
  }
  public final Texture getTexture() {
		if(texture==null&&stexture!=null)
			loadTexture();
    return texture;
  }
  public final Texture getAlphaMap() {
    return alphaMap;
  }
  public final void loadTexture() {
		texture = TextureManager.getInstance().getTexture(new File("Assets"+stexture));
  }
}