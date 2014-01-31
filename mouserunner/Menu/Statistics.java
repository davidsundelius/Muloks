package mouserunner.Menu;

import com.sun.opengl.util.j2d.TextRenderer;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import javax.media.opengl.GL;
import mouserunner.Game.Player;
import mouserunner.Managers.ConfigManager;
import mouserunner.Managers.FontManager;
import mouserunner.Managers.GameplayManager;
import mouserunner.Menu.Components.Button;
import mouserunner.Menu.Components.MenuComponent;
import mouserunner.System.Command;


/**
 * The class presenting the statistics screen, after a tournament.
 * This state shows statistics and game details for all the players.
 * 
 * @author Zorek
 */
public class Statistics extends Menu {
	private TextRenderer text;
	
	/**
	 * Constructs a new statistics screen
	 */
	public Statistics() {
		command=null;
		menuPointer = 0;
		menuComponents=new ArrayList<MenuComponent>();
		backTexture="Statistics";
		menuComponents.add(new Button(630, 15, "Back to menu"));
		text = new TextRenderer(FontManager.getInstance().getFont("Assets/Misc/Meow.ttf", Font.PLAIN, 16));
	}
	
	@Override
	public void view(GL gl) {
		super.view(gl);

		text.beginRendering(ConfigManager.getInstance().width, ConfigManager.getInstance().height);
			text.setColor(Color.BLACK);
			for(int i=0;i<GameplayManager.getInstance().players.size();i++) {
				Player p=GameplayManager.getInstance().players.get(i);
				text.draw(p.getName(), 40, 360-30*i);
			}
		text.endRendering();
	}
	
	@Override
	public void executeEvent() {
		commandListener.commandPerformed(Command.GOTOMENU);
		changeFocus();
	}
	
	@Override
	public String toString() {
		return "Game over";
	}
}