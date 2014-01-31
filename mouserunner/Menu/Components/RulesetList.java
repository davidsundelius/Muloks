package mouserunner.Menu.Components;

import mouserunner.Managers.GameplayManager;

/**
 * A component for representing a ruleset list in a menu
 * @author Zorek
 */
public class RulesetList extends List {
	/**
	 * Creates a new list, sets its position, dimensions and texturepath
	 * @param x the position on the x-axis for the new list
	 * @param y the position on the y-axis for the new list
	 */
	public RulesetList(final int x, final int y) {
		super(x,y);
		width = 120;
		texturePath = "MenuCRulesetList";
	}
	
	/**
	 * This will occur when the ruleset list is clicked
	 */
	@Override
	public void activateComponent(int x, int y) {
		super.activateComponent(x, y);
		GameplayManager.getInstance().newGame(list.get(cursor));
		return;
	}
}