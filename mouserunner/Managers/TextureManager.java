package mouserunner.Managers;

import com.sun.opengl.util.texture.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.media.opengl.*;

/**
 * TextureManager (singleton) reads textures from files and returns them as Textures.
 * Also controlls that only one texture is loaded of the same type.
 * @author Zorek
 */
public class TextureManager {

  private static TextureManager instance = new TextureManager();
  private Map<String, Texture> textures = new HashMap<String, Texture>();
  private File noTextureFile = new File("Assets/Textures/NoTexture.png");

  private TextureManager() {
  }

  /**
   * Method that returns a texture from given file, if not already in memory
   * it's loaded from  given file.
   *
   * @param file A File which contains a path to a valid texture
   * @return Returns the ordered texture
   */
  public Texture getTexture(File file) {
    final Texture newTexture;
    if (!textures.containsKey(file.getPath())) {
      newTexture = loadTexture(file);
      textures.put(file.getPath(), newTexture);
    } else {
      newTexture = textures.get(file.getPath());
    }
    if (newTexture == null) {
      //System.err.println(file.getPath()+" do not exist or could not be read, loading default texture");
      if (textures.containsKey(noTextureFile.getPath())) {
        return textures.get(noTextureFile.getPath());
      } else {
        final Texture noTextureTexture = loadTexture(noTextureFile);
        textures.put(noTextureFile.getPath(), noTextureTexture);
        return noTextureTexture;
      }
    } else {
      return newTexture;
    }
  }

  /**
   * Gets the instance of the TextureManager
   *
   * @return Returns the singleton instance
   */
  public static TextureManager getInstance() {
    return instance;
  }

  private Texture loadTexture(final File file) {
    Texture t = null;

    try {
      t = TextureIO.newTexture(file, false);
    } catch (IOException ex) {
      return null;
    }

    t.setTexParameteri(GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
    t.setTexParameteri(GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
		t.setTexParameteri(GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
		t.setTexParameteri(GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
    return t;
  }
}
