package mouserunner.Managers;

import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;

/**
 * FontManager (singleton) loads and returns fonts (ttf-files) from disc
 * @author Zorek
 */
public class FontManager {

	private static FontManager instance = new FontManager();

	private FontManager() {
	}

	public static FontManager getInstance() {
		return instance;
	}

	public Font getFont(final String fontName, final int style, final int size) {
		return loadFont(fontName, style, size);
	}

	private Font loadFont(final String fontName, final int style, final int size) {
		try {
			File fontFile = new File(fontName);
			FileInputStream fis = new FileInputStream(fontFile);
			Font font = Font.createFont(Font.TRUETYPE_FONT, fis).deriveFont(style, size);
			return font;
		} catch (Exception ex) {
			System.err.println("Can't locate font: " + fontName + ", using default system font");
			return new Font("Monospaced", style, size);
		}
	}
}