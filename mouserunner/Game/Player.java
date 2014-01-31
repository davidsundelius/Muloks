package mouserunner.Game;

import java.awt.Color;
import java.util.LinkedList;
import mouserunner.Managers.GameplayManager;

/**
 * Controls all the data for a player
 * @author Erik (& Zorek)
 */
public class Player {

	private String name;
	private int score;
	private int totScore;
	private int tournamentScore;
	private int totMice;
	private int totCats;
	private int totGoldenMice;
	private int totPowerupMice;
	private int totKamikazeMice;
	private int totTacticalMice;
	private int totAgentMice;
	private boolean alive;
	private Color color;
	private LinkedList<Arrow> arrows;
	private boolean ready;
	private boolean AI = true;

	/**
	 * Constructs a new player with a name
	 * @param name the name of the player
	 */
	public Player(String name) {
		this.name = name;
		color = GameplayManager.getInstance().colorPool.pop();
		tournamentScore = 0;
		totMice = 0;
		totCats = 0;
		totGoldenMice = 0;
		totPowerupMice = 0;
		totKamikazeMice = 0;
		totTacticalMice = 0;
		totAgentMice = 0;
		ready = false;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * Getter for the players name
	 * @return the players name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Getter for the players score
	 * @return the players score
	 */
	public int getScore() {
		return score;
	}

	/**
	 * Setter for the players  
	 * @param playerScore The score which the player will get.
	 */
	public void setScore(int playerScore) {
		score = playerScore;
	}

	/**
	 * Getter for the players tournament score
	 * @return the players tournament score
	 */
	public int getTournamentScore() {
		return tournamentScore;
	}

	/**
	 * Getter for the players color
	 * @return the players color
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Getter for the total mouse count
	 * @return the players total mouse count
	 */
	public int getMouseCount() {
		return totMice;
	}

	/**
	 * Getter for wheter the player is AI controlled or not
	 * @return wheter the player is AI controlled or not.
	 */
	public boolean isAI() {
		return AI;
	}

	/**
	 * Getter for the total cat count
	 * @return the players total cat count
	 */
	public int getCatCount() {
		return totCats;
	}

	/**
	 * Is player alive?
	 * @return true, if player is alive
	 */
	public boolean isAlive() {
		return alive;
	}

	/**
	 * Is player ready?
	 * @return true, if player is alive
	 */
	public boolean isReady() {
		return ready;
	}

	/**
	 * Set player ready status
	 * @param ready the new status
	 */
	public void setReady(final boolean ready) {
		this.ready = ready;
	}

	/**
	 * Adds an arrow to the game with this players color and control
	 * @param arrow the arrow that will be placed
	 */
	public void addArrow(Arrow arrow) {
		arrows.add(arrow);
	}

	/**
	 * Remove an arrow from the field
	 * @return a reference to the removed arrow
	 */
	public Arrow removeArrow() {
		return arrows.removeFirst();
	}

	/**
	 * Remove all players arrows from field
	 */
	public void clearArrows() {
		arrows.clear();
	}

	/**
	 * Returns the number of arrows this player controls at the moment
	 * @return the arrow count
	 */
	public int getNumberArrows() {
		return arrows.size();
	}

	/**
	 * The player has cought a mouse
	 */
	public void mouseGet() {
		if (score + GameplayManager.getInstance().mouseValue < 999) {
			if (score + GameplayManager.getInstance().mouseValue > 0) {
				score += GameplayManager.getInstance().mouseValue;
			} else {
				score = 0;
			}
		} else {
			score = 999;
		}
		totMice++;
		GameplayManager.getInstance().sortPlayerList();
	}

	/**
	 * The player has cought a cat
	 */
	public int catGet() {
		int bScore = score;
		if (GameplayManager.getInstance().catReducerValue != 0) {
			if (score - (score / GameplayManager.getInstance().catReducerValue) > 999) {
				score = 999;
			} else {
				score -= score / GameplayManager.getInstance().catReducerValue;
			}
			if (score > 0) {
				score--;
			}
		}
		totCats++;
		GameplayManager.getInstance().sortPlayerList();
		return bScore - score;
	}

	/**
	 * The player has cought a golden mouse
	 */
	public void goldenMouseGet() {
		if (score + GameplayManager.getInstance().goldenValue < 999) {
			if (score + GameplayManager.getInstance().goldenValue > 0) {
				score += GameplayManager.getInstance().goldenValue;
			} else {
				score = 0;
			}
		} else {
			score = 999;
		}
		totGoldenMice++;
		GameplayManager.getInstance().sortPlayerList();
	}

	/**
	 * The player has cought a golden mouse
	 */
	public void powerupMouseGet() {
		if (score < 999) {
			score++;
		}
		totPowerupMice++;
		GameplayManager.getInstance().sortPlayerList();
	}

	/**
	 * The player has cought a kamikaze mouse
	 */
	public void kamikazeMouseGet() {
		if (score < 999) {
			score++;
		}
		totKamikazeMice++;
		GameplayManager.getInstance().sortPlayerList();
	}

	/**
	 * The player has cought a tactical mouse
	 */
	public void tacticalMouseGet() {
		if (score < 999) {
			score++;
		}
		totTacticalMice++;
		GameplayManager.getInstance().sortPlayerList();
	}

	/**
	 * The player has cought a agent mouse
	 */
	public int agentMouseGet() {
		int bScore = score;
		score -= score / 3;
		if (score > 0) {
			score--;
		}
		totAgentMice++;
		GameplayManager.getInstance().sortPlayerList();
		return bScore - score;
	}

	/**
	 * Eliminates this player
	 */
	public void eliminate() {
		alive = false;
		clearArrows();
	}

	/**
	 * Called if game is over, prepares a new game and deals score to the player
	 */
	public void gameOver() {
		tournamentScore += GameplayManager.getInstance().players.size() - GameplayManager.getInstance().players.indexOf(this);
		newGame();
	}

	/**
	 * Resets the player for a new game
	 */
	public void newGame() {
		ready = false;
		score = GameplayManager.getInstance().handicap.get(this);
		alive = true;
		arrows = new LinkedList<Arrow>();
	}

	/**
	 * Returns true if the given player has the same name that this player
	 * @param o given player
	 * @return true, if its the same player
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Player)) {
			return false;
		}
		Player player = (Player) o;
		return player.name.equals(name);
	}
}
