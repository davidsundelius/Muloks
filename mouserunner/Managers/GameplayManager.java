package mouserunner.Managers;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Stack;
import mouserunner.Game.NetworkClient;
import mouserunner.Game.Player;
import mouserunner.LevelComponents.Trap;
import mouserunner.Poweups.Powerup;
import mouserunner.System.Timer;

/**
 * This singleton class keeps track of the rules, players and statistics
 * for a whole tournament
 * @author Zorek
 */
public class GameplayManager {
	private static final GameplayManager instance = new GameplayManager(); /** the number of powerups available in the game at this moment */

	//Ruleset
	private String rulesetName;
	/** The total time of a game (-1 -> no limit)																	*/
	public int time;										
	/** If true, the its good to get as many mice as possible											*/
	public boolean mostMice;						
	/** The score limit to be eliminated/win from the game (-1 -> no limit)				*/
	public int upperLimit;							
	/** The score limit to be eliminated/win from the game (-1 -> no limit)				*/
	public int lowerLimit;							
	/** The score each player has at the beginning of a game (plus/minus handicap)*/
	public int startingScore;						
	/** The score value of a mouse																								*/
	public int mouseValue;							
	/** The score value of a golden mouse																					*/
	public int goldenValue;							
	/** The amount of score the player will loose: score=score-(score/catReducer)	*/
	public int catReducerValue;					
	/** The number of arrows each player has at its disposal											*/
	public int numArrows;								
	/** The amount of time player placed arrows stay in field											*/
	public long arrowTime;							
	/** The chance that a mouse spawns every second (1.0f is every time)					*/
	public float mouseSpawningRate;			
	/** The chance that a cat spawns every second (1.0f is every time)						*/
	public float catSpawningRate;				
	/** The chance that a golden mouse spawns every second (1.0f is every time)		*/
	public float goldenSpawningRate;		
	/** The chance that a powerup mouse spawns every second (1.0f is every time)	*/
	public float powerupSpawningRate;		
	/** The chance that a kamikase mouse spawns every second (1.0f is every time)	*/
	public float kamikazeSpawningRate;	
	/** The chance that a tactical mouse spawns every second (1.0f is every time)	*/
	public float tacticalSpawningRate;	
	/** The chance that a agent cat spawns every second (1.0f is every time)			*/
	public float agentSpawningRate;			
	/** The maximum number of mice allowed (-1 -> no limit)												*/
	public int maxNumberMice;						
	/** The maximum number of cats allowed (-1 -> no limit)												*/
	public int maxNumberCats;						
	/** The maximum number of cats allowed (-1 -> no limit)												*/
	public int maxNumberGolden;				
	/** The maximum number of cats allowed (-1 -> no limit)												*/
	public int maxNumberPowerup;				
	/** The maximum number of cats allowed (-1 -> no limit)												*/
	public int maxNumberKamikaze;
	/** The maximum number of cats allowed (-1 -> no limit)												*/																
	public int maxNumberTactical;							
	/** The maximum number of cats allowed (-1 -> no limit)												*/
	public int maxNumberAgent;					
	/** If an element in this array is true, then the powerup is enabled					*/
	public boolean[] powerupEnabled;	
	/** If an element in this array is true, then the trap is enabled							*/
	public boolean[] trapsEnabled;
	/** The time, in millisecond, to reload the mouse trap												*/
	public long mouseTrapReloadTime;
	/** The time, in millisecond, to reload the cat trap													*/
	public long catTrapReloadTime;
	/** The duration that a mouse will be slowed by glue													*/
	public long gluedDuration;
	
	//Tournament variables
	/** Keeps the player objects */
	public List<Player> players;
	/** Keeps track of which players are controlled by the AI */
	public Map<Player, Boolean> ai;
	/** Adds or subtracts from the score each new game */
	public Map<Player, Integer> handicap;				
	/** Keeps track of the levels the host choose to play */
	public List<String> levels;								
	/** The levels the player banned from being choosed at random*/
	public List<String> bannedRandomLevelPaths;	
	/** The reference to the player, playing on this computer*/
	public Player thisPlayer;										
	
	//Network
	/** The instance of the current client network handler (null if single player) */
	public NetworkClient networkClient; 
	
	//Current game variables
	/** The index of the current game */
	public int currentGameIndex;	
	/** The timer of the current game */
	public Timer gameTimer;				
	
	//Color stack
	/** The pool from which new players gets their colors */
	public Stack<Color> colorPool;

	/**
	 * Constructs the RuleManager internally and loads default rules
	 */
	private GameplayManager() {
		reset();
		loadDefaultRuleset();
	}

	/**
	 * Return the single instance of RuleManager
	 * @return the instance
	 */
	public static GameplayManager getInstance() {
		return instance;
	}

	/**
	 * Prepares all affected components for a start of a new tournament
	 * @param rulesetName the path to the ruleset that will be used in the tournament
	 */
	public void newGame(String rulesetName) {
		System.out.println("GameplayManager is starting a new tournament with the ruleset: " + rulesetName);
		this.rulesetName = rulesetName;
		try {
			loadRuleset(new File("Assets/Rulesets/" + rulesetName));
			System.out.println("GameplayManager loaded " + rulesetName + " without any problems");
		} catch (IOException e) {
			System.out.println("GameplayManager could not read rulesfile: " + rulesetName + ", loading default ruleset");
			loadDefaultRuleset();
		}
		currentGameIndex = 0;
		gameTimer = null;
	}

	/**
	 * Resets the settings to standard for a new GameplayerManager
	 */
	public void reset() {
		System.out.println("GameplayManagers tournament settings is resetted");
		fillColorPool();
		players = new ArrayList<Player>();
		ai = new HashMap<Player, Boolean>();
		handicap = new HashMap<Player, Integer>();
		bannedRandomLevelPaths = new ArrayList<String>();
		levels = new ArrayList<String>();
	}

	/**
	 * Prepares debug settings for a new tournament,
	 * sets the playerlist up and 
	 */
	public void loadDebugTournament() {
		reset();
		//Set players
		Player p;
		p = new Player("Lego");
		players.add(p);
		ai.put(p, false);
		handicap.put(p, 0);
		p = new Player("Zorek");
		players.add(p);
		ai.put(p, true);
		handicap.put(p, 0);
		p = new Player("Erik");
		players.add(p);
		ai.put(p, true);
		handicap.put(p, 0);
		p = new Player("puKKa");
		players.add(p);
		ai.put(p, true);
		handicap.put(p, 0);
		p = new Player("farkost");
		players.add(p);
		ai.put(p, true);
		handicap.put(p, 0);
		p = new Player("wilsonx");
		players.add(p);
		ai.put(p, true);
		handicap.put(p, 0);
		p = new Player("myDM");
		players.add(p);
		ai.put(p, true);
		handicap.put(p, 0);
		p = new Player("Rot");
		players.add(p);
		ai.put(p, true);
		handicap.put(p, -1);
		thisPlayer = players.get(1);

		//Set level list for tournaments
		int numGames = 3;
		for (int i = 0; i < numGames; i++) {
			levels.add(pickRandomLevel());		//Sets this game to a local game
		}
		networkClient = null;
	}

	/**
	 * Set the standard rules for a classic game.
	 * This method is loaded at gamestart and if the game
	 * requests a broken or not found file to load the ruleset from
	 */
	public void loadDefaultRuleset() {
		time = 180000;
		mostMice = true;
		upperLimit = -1;
		lowerLimit = -1;
		startingScore = 0;
		mouseValue = 1;
		catReducerValue = 3;
		goldenValue = 50;
		numArrows = 3;
		arrowTime = 10000;
		mouseSpawningRate = 0.8f;
		catSpawningRate = 0.28f;
		goldenSpawningRate = 0.05f;
		powerupSpawningRate = 0.05f;
		kamikazeSpawningRate = 0.0f;
		tacticalSpawningRate = 0.0f;
		agentSpawningRate = 0.0f;
		maxNumberMice = -1;
		maxNumberCats = 8;
		maxNumberGolden = 1;
		maxNumberPowerup = 1;
		maxNumberKamikaze = 0;
		maxNumberTactical = 0;
		maxNumberAgent = 0;
		powerupEnabled = new boolean[Powerup.numPowerups];
		for(int i=0;i<powerupEnabled.length;i++)
			powerupEnabled[i]=true;
		trapsEnabled = new boolean[Trap.numTraps];
		for(int i=0;i<trapsEnabled.length;i++)
			trapsEnabled[i]=true;
		mouseTrapReloadTime = 4000;
		catTrapReloadTime = 4000;
		gluedDuration = 2000;
		rulesetName = "Classic.rls";
		saveRuleset();
	}

	/**
	 * Loads a ruleset from a rls-file
	 * @param rulesetFile the file used to create the ruleset
	 * @throws IOException if the file could not be read
	 */
	private void loadRuleset(File rulesetFile) throws IOException {
		Scanner sc = new Scanner(rulesetFile);
		sc.useLocale(new Locale("en-US"));
		//Read general game settings
		time = sc.nextInt();
		mostMice = sc.nextBoolean();
		upperLimit = sc.nextInt();
		lowerLimit = sc.nextInt();
		startingScore = sc.nextInt();
		mouseValue = sc.nextInt();
		catReducerValue = sc.nextInt();
		goldenValue = sc.nextInt();
		//Read arrow settings
		sc.nextLine();
		numArrows = sc.nextInt();
		arrowTime = sc.nextLong();
		//Read the spawning rates of entities
		sc.nextLine();
		mouseSpawningRate = sc.nextFloat();
		catSpawningRate = sc.nextFloat();
		goldenSpawningRate = sc.nextFloat();
		powerupSpawningRate = sc.nextFloat();
		kamikazeSpawningRate = sc.nextFloat();
		tacticalSpawningRate = sc.nextFloat();
		agentSpawningRate = sc.nextFloat();
		//Read the maximum number of entities of a specific type
		sc.nextLine();
		maxNumberMice = sc.nextInt();
		maxNumberCats = sc.nextInt();
		maxNumberGolden = sc.nextInt();
		maxNumberPowerup = sc.nextInt();
		maxNumberKamikaze = sc.nextInt();
		maxNumberTactical = sc.nextInt();
		maxNumberAgent = sc.nextInt();
		//Read powerups, if all powerups are turned off, turn off powerup spawning
		sc.nextLine();
		powerupEnabled = new boolean[Powerup.numPowerups];
		boolean allFalse=true;
		for(int i=0;i<powerupEnabled.length;i++) {
			powerupEnabled[i] = sc.nextBoolean();
			if(powerupEnabled[i])
				allFalse=false;
		}
		if(allFalse) {
			maxNumberPowerup=0;
		}
		//Read traps enabling
		sc.nextLine();
		trapsEnabled = new boolean[Trap.numTraps];
		for(int i=0;i<trapsEnabled.length;i++)
			trapsEnabled[i] = sc.nextBoolean();
		//Read traps settings
		sc.nextLine();
		mouseTrapReloadTime = sc.nextLong();
		catTrapReloadTime = sc.nextLong();
		gluedDuration = sc.nextLong();
		sc.close();
	}

	/**
	 * Prints ruleset to working file
	 */
	public void saveRuleset() {
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("Assets/Rulesets/" + rulesetName)));
			out.print(time + " " + mostMice + " " + upperLimit + " " + lowerLimit + " " + startingScore + " " + mouseValue + " " + catReducerValue + " " + goldenValue + "\n");
			out.print(numArrows + " " + arrowTime + "\n");
			out.print(mouseSpawningRate + " " + catSpawningRate + " " + goldenSpawningRate + " " + powerupSpawningRate + " " + kamikazeSpawningRate + " " + tacticalSpawningRate + " " + agentSpawningRate + "\n");
			out.print(maxNumberMice + " " + maxNumberCats + " " + maxNumberGolden + " " + maxNumberPowerup + " " + maxNumberKamikaze + " " + maxNumberTactical + " " + maxNumberAgent + "\n");
			for(int i=0;i<powerupEnabled.length;i++) out.print(powerupEnabled[i]+" ");
			out.print('\n');
			for(int i=0;i<trapsEnabled.length;i++) out.print(trapsEnabled[i]+" ");
			out.print('\n');
			out.print(mouseTrapReloadTime + " " + catTrapReloadTime + " " + gluedDuration + '\n');
			out.flush();
			out.close();
		} catch (IOException e) {
			System.out.println("GameplayManager: Couldn't save ruleset to disk, check for permission limits");
		}
	}

	/**
	 * Choose a random level among all the created levels (except banned ones)
	 * @return The path to the randomly chosen level
	 */
	public String pickRandomLevel() {
		String result = null;
		List<String> levelList = new ArrayList<String>();
		File dir = new File("Assets/Levels/");
		//Create a list of available levels
		for (File level : dir.listFiles()) {
			if (!level.isDirectory()) {
				levelList.add(level.getName());
			}
		}
		Random rand = new Random();
		while (result == null) {
			//Randomize a level index
			int levelIndex = rand.nextInt(levelList.size());
			result = levelList.get(levelIndex); //Set random level path
			//Check that the user did not ban the random level
			for (String brlp : bannedRandomLevelPaths) //Needs equals (can't use "contains")
			{
				if (brlp.equals(levelList.get(levelIndex))) { //If level is in bann list, randomize another 
					result = null;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Tells the GameplayManager to prepare the next game in the tournament
	 * @return if true, the tournament continues
	 */
	public boolean nextGame() {
		gameTimer = null;
		currentGameIndex++;
		if (levels.size() <= currentGameIndex) {
			return false;
		}
		for (Player p : players) {
			p.gameOver();
		}
		return true;
	}

	/**
	 * Sorts the player list after high score (or low score)
	 */
	public void sortPlayerList() {
		if (mostMice) {
			for (int i = 0; i < players.size() - 1; i++) {
				for (int j = 1; j < players.size() - i; j++) {
					if (players.get(j - 1).getScore() < players.get(j).getScore()) {
						Player tmpPlayer = players.get(j - 1);
						players.set(j - 1, players.get(j));
						players.set(j, tmpPlayer);
					}
				}
			}
		} else {
			for (int i = 0; i < players.size() - 1; i++) {
				for (int j = 1; j < players.size() - i; j++) {
					if (players.get(j - 1).getScore() > players.get(j).getScore()) {
						Player tmpPlayer = players.get(j - 1);
						players.set(j - 1, players.get(j));
						players.set(j, tmpPlayer);
					}
				}
			}
		}
	}

	/**
	 * Gets a sorted player list after high tournament score
	 * @return the sorted list
	 */
	public List<Player> getSortedPlayerListByTournamentScore() {
		List<Player> newList = new ArrayList<Player>();
		newList.addAll(players);
		for (int i = 0; i < newList.size() - 1; i++) {
			for (int j = 1; j < newList.size() - i; j++) {
				if (newList.get(j - 1).getTournamentScore() < newList.get(j).getTournamentScore()) {
					Player tmpPlayer = newList.get(j - 1);
					newList.set(j - 1, newList.get(j));
					newList.set(j, tmpPlayer);
				}
			}
		}
		return newList;
	}

	/**
	 * Makes some checks to see if any player has won according to the
	 * current ruleset.
	 * @return true, if game is over
	 */
	public boolean isGameOver() {
		int numAlive = 0;
		if (time != -1) {
			if (gameTimer.read() > time - 1000) {
				return true;
			}
		}
		for (Player p : players) {
			if (mostMice) {
				if(upperLimit!=-1) {
					if (p.getScore() >= upperLimit) {
						return true;
					}
				}
				if(lowerLimit!=-1) {
					if(p.getScore() <= lowerLimit) {
						p.eliminate();
					}
				}
			} else {
				if(upperLimit!=-1) {
					if (p.getScore() >= upperLimit) {
						p.eliminate();
					}
				}
				if(lowerLimit!=-1) {
					if(p.getScore() <= lowerLimit) {
						return true;
					}
				}
			}
			if (p.isAlive()) {
				numAlive++;
			}
		}
		if (numAlive < 2) {
			return true;
		}
		return false;
	}

	/**
	 * Returns the path to the next level file
	 * @return the path to the next level file
	 */
	public String getLevel() {
		return levels.get(currentGameIndex);
	}
	
	/**
	 * Makes some checks to see if any player has won according to the
	 * current ruleset.
	 * @return the name of the currently loaded ruleset
	 */
	public String getRulesetName() {
		return rulesetName;
	}

	/**
	 * Fills up the color list to hand out colors to the newly created
	 * Player-objects later
	 */
	private void fillColorPool() {
		colorPool = new Stack<Color>();
		colorPool.add(Color.ORANGE);
		colorPool.add(Color.PINK);
		colorPool.add(Color.WHITE);
		colorPool.add(Color.DARK_GRAY);
		colorPool.add(Color.YELLOW);
		colorPool.add(Color.MAGENTA);
		colorPool.add(Color.BLUE);
		colorPool.add(Color.CYAN);
		colorPool.add(Color.RED);
	}
}
