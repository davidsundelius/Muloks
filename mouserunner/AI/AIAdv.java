package mouserunner.AI;

import mouserunner.Game.Entity.PowerupMouse;
import mouserunner.Game.Entity.GoldenMouse;
import mouserunner.Game.Entity.Mouse;
import mouserunner.Game.Entity.AgentMouse;
import mouserunner.Game.Entity.Cat;
import mouserunner.Game.Entity.KamikazeMouse;
import mouserunner.LevelComponents.*;
import java.util.ArrayList;
import java.util.Collection;

import mouserunner.Game.*;
import mouserunner.System.Direction;
import mouserunner.System.Timer;

/**
 * This is the masterclass for the more expanded AI system.
 * @author Pierre Andersson
 */
public class AIAdv// implements Updatable 
{

	private int nTick = 0; //The Current Tick, see AI DesignPlan.txt for details
	private int nTicksTotal = 4; //The Total amount of ticks.
	//Level state
	//The two first dimensions in the array represents the maps
	//x and y cvooridnates.
	//The third represents type of data that needs to be retrieved
	//about the tile.
	//0 = GroundType (0 = OpenFloor,  5 = Nest, 6 = Spawn, 7 = Blackhole, 8= Glue, 
	//9 = Mousetrap, 10 = Portal, 11 = Cattrap)
	//1 = North Wall (0 = No Wall, 1 = Wall)
	//2 = East Wall
	//3 = South Wall
	//4 = West Wall
	private int[][][] nCurLevel; //This int is there to store the current
	//level so its state can be accessed quickly with CPU intensive 
	//algorithms.
	private float tileSize; //The size of the tiles
	private int nMapX; //Map width 
	private int nMapY; //Map heigth
	private int totalCPUs = 0; //The total amount of AI players.
	private int lastActor = 0; //The index of the last AIMinds that acted
	private ArrayList<EntityRow> lEntityRows = new ArrayList<EntityRow>(); //Stacks of mice
	//The AIMinds are used to store AI related information for each player
	private ArrayList<AIMind> lAIMinds = new ArrayList<AIMind>();
	//Timer stuff
	Timer updateClock = new Timer();
	int updateCycle;
	Timer milestoneClock = new Timer();
	int milestoneCycle;
	//Player data
	private Collection<Player> Players;
	//Mouse data
	private Collection<Mouse> mice;
	//Cat data
	private Collection<Cat> cats;
	private Collection<Arrow> arrows;
	//Level data
	private Level currentLevel;

	//Constructor
	public AIAdv(Level lMap,
					Collection<Cat> cCats, Collection<Mouse> cMice,
					Collection<Player> cPlayers, Collection<Arrow> cArrows, float fTileSize) {
		nMapX = 16;
		nMapY = 12;

		//Set the level size, note that 
		nCurLevel = new int[nMapX][nMapY][5];

		//Link Collections
		Players = cPlayers;
		mice = cMice;
		cats = cCats;
		arrows = cArrows;
		//Link map
		currentLevel = lMap;
		//Store the current map

		//Store tilesize
		tileSize = fTileSize;
		//Initate the AIMinds
		for (Player currentPlayer : Players) {
			{
				AIMind currentMind = new AIMind(currentPlayer);
				lAIMinds.add(currentMind);
				if (currentPlayer.isAI() && currentPlayer.isAlive()) //Increase amount of AI players
				{
					totalCPUs++;
				}

			}
			//Convert map to a fast to read format
			updateMap();


		}
		//Timer initiation
		updateCycle = 20; //The basic update cycle for one player,
		//The lower the better they are at defending against cats.
		updateClock.setTimestamp();

		milestoneCycle = 2000; //How often the AI will recount ones score
		milestoneClock.setTimestamp(); //Zero milestone clock
		//Calculate total amount of ticks
		nTicksTotal = totalCPUs + 1;
		//
		//Calculate real update cycle
		if (totalCPUs > 0) {
			updateCycle = updateCycle / (totalCPUs);
		}

	}
	//@Override

	public void update() {
		//Store nest position
		updateNests();
		//Check if an update cycle happens.
		if (updateClock.read() < updateCycle) {
			return;// No update cycle happens this frame.
		}
		//Run the update cycle
		updateClock.setTimestamp();
		//Tick 0
		if (nTick == 0) {
			//Calculate entity rows for later collective use.
			calculateEntityRows();
			//Check if milestone needs to be updated
			if (milestoneClock.read() > milestoneCycle) {
				//Update AI minds and reset milestone clock
				updateAIminds();
				milestoneClock.setTimestamp();
			}
		}
		//Other ticks
		if (nTick != 0 && nTick < nTicksTotal) {
			//Loop through each AIMind to se which one acts next
			for (int i = lastActor; i < lAIMinds.size(); i++) {
				//If AI player, act
				if (lAIMinds.get(i).getOwner().isAI()) {
					//Only do stuff if the player has a nest
					if (lAIMinds.get(i).NestX != -1) {
						//Clear previous calculations about entity rows
						//to make room for new ones.
						lAIMinds.get(i).forgetRows();
						//Weight options, involved weighting entity rows.
						lAIMinds.set(i, weightOptions(lAIMinds.get(i)));
						//Finally, place any arrows that are needed.
						placeArrows(lAIMinds.get(i));
					}
					lastActor = i + 1;
					i = lAIMinds.size();
				}
			}
		}
		//Final tick
		if (nTick == nTicksTotal) {
			//AI Cycle complete, start over
			//Reset lastActor so it starts back at player 0 again
			lastActor = 0;
			nTick = 0;
		} else //Ready for next tick
		{
			nTick++;
		}
		return;// true;
	}

	/**
	 * This function makes each AIMind relearn the position of their own nests.
	 */
	public void updateNests() {
		//Loop thourgh each AIMind
		for (int i = 0; i < lAIMinds.size(); i++) {
			//Loop though the list of nest to find out which the AImind in question owns
			for (int j = 0; j < currentLevel.getNests().size(); j++) {
				//Owner found, set coordinates
				if (lAIMinds.get(i).getOwner() == currentLevel.getNests().get(j).getOwner()) {
					lAIMinds.get(i).updateNestPosition(currentLevel.getNests().get(j).x, currentLevel.getNests().get(j).y);
					//System.out.println("Nest found");
					break; //No need to look for another nest
				}
			}
		}
	}

	/**
	 * Updates the AIMinds to be sure that such things as the tournament score
	 * is up to date.
	 */
	public void updateAIminds() {
		//Loop though each AI minds
		for (AIMind currentMind : lAIMinds) {
			for (Player currentPlayer : Players) {
				//Owner found, update AIMinds
				if (currentMind.getOwner().getName().equals(currentPlayer.getName())) {
					//Check up score change
					currentMind.scoreChange = currentPlayer.getScore() - currentMind.lastScore;
					currentMind.lastScore = currentPlayer.getScore();
					//  System.out.println("Score Change: " + currentMind.scoreChange
					//   + " Score: " +  currentPlayer.getScore() +" Prev Score: "
					//   + currentMind.getOwner().getScore());
					//Update all data
					currentMind.setOwner(currentPlayer);

				}

			}
		}
	}

	/**
	 * This functin places arrows as determined by the weightOptions function.
	 * @param currentMind The AIMind that the place arrows system will set arrows for.
	 */
	private void placeArrows(AIMind currentMind) {
		//Check first if an action is to be taken
		if (currentMind.nextAction != -1) {
			// System.out.println("Placing arrows for : " +currentMind.getOwner().getName()
			//         + " Action: " + currentMind.nextAction);
			//Check if defense
			if (currentMind.nextAction == 1) {
				//Place defense arrow
				//System.out.println("Defensive action by: "+ currentMind.getOwner().getName()
				//        + " X: " + currentMind.defenseX + " Y: " + currentMind.defenseY);
				placeArrow(currentMind.defenseX, currentMind.defenseY, currentMind.defenseDir, currentMind.getOwner());
			} //Check if attraction or attack
			else if (currentMind.nextAction == 0 || currentMind.nextAction == 2) {
				AIPath workPath = currentMind.nextPath;
				for (int i = 0; i < workPath.nTurnsTotal; i++) {
					placeArrow(workPath.nTurnX.get(i), workPath.nTurnY.get(i), workPath.nTurnDir.get(i), currentMind.getOwner());
				}
			}
			currentMind.nextAction = -1;
		}
	/*  if (currentMind.getRows().size() > 0)
	{
	//Find the best mouse stack
	int bestIndex = 0;

	for (int i = 0; i < currentMind.getRows().size(); i++)
	{
	if (currentMind.getRows().get(i).ModifyValue > currentMind.getRows().get(bestIndex).ModifyValue
	&& currentMind.getRows().get(i).rowPath.nTurnsTotal < 4 && currentMind.getRows().get(i).rowPath.nTurnsTotal > 0 && currentMind.getRows().get(i).rowPath.nTotalLength != -1)
	bestIndex = i;
	}
	AIPath workPath = new AIPath();
	workPath = currentMind.getRows().get(bestIndex).rowPath;
	System.out.println("Placing arrows for : " +currentMind.getOwner().getName());
	System.out.println("Row Value RAW: " + currentMind.getRows().get(bestIndex).MouseValue + "Row Value MODDED: " + currentMind.getRows().get(bestIndex).ModifyValue + "Path Turns: " + currentMind.getRows().get(bestIndex).rowPath.nTurnsTotal
	+ "Row Length: " + currentMind.getRows().get(bestIndex).rowLength);
	//if (currentMind.currentPath == null || weightPath(currentMind.currentPath, true) < weightPath(workPath, false))
	//{
	for (int i = 0; i < workPath.nTurnsTotal; i++)
	{
	currentMind.currentPath = workPath;
	placeArrow(workPath.nTurnX.get(i),workPath.nTurnY.get(i),workPath.nTurnDir.get(i), currentMind.getOwner());
	}
	// }
	}*/
	}

	/**
	 * This functin places an arrow in the current active level.
	 * @param nX The X coordinate of the arrow.
	 * @param nY The Y coordinate of the arrow.
	 * @param nDirection The facing of the arrow.
	 * @param owner The Player that owns the arrow.
	 */
	private void placeArrow(int nX, int nY, int nDirection, Player owner) {
		//Set direction
		Direction dir;
		if (nDirection == 0) {
			dir = Direction.UP;
		} else if (nDirection == 1) {
			dir = Direction.RIGHT;
		} else if (nDirection == 3) {
			dir = Direction.LEFT;
		} else {
			dir = Direction.DOWN;
		}
		//Place the arrow
		((EmptyTile) currentLevel.getTile(nX, nY)).setArrow(dir, owner);

	}

	/**
	 * Determines how many arrows a particular AIMind has left to place
	 * @param maxArrows The max amount of arrows the player can place.
	 * @param currentMind The AIMind whose arrows will be checked.
	 * @return Returns the amount of arrows the mind can place.
	 */
	private int determineArrows(int maxArrows, AIMind currentMind) {
		int arrowsLeft = maxArrows;
		//Loop through every arrow
		for (Arrow currentArrow : arrows) {
			if (currentArrow.getOwner() == currentMind.getOwner() && currentArrow.isActive()) {
				//Arrow is placed, reduce arrows left
				arrowsLeft = arrowsLeft - 1;
			}
		}

		//Return how many arrows remain
		return arrowsLeft;
	}

	/**
	 * Checks if a path has been disrupted by anyone placing arrows on it
	 * that destroys the path.
	 * @param testPath The path to be tested.
	 * @return Returns false if the path has been compromised.
	 */
	private boolean checkPathIntregity(AIPath testPath) {
		boolean pathWhole = true; //Set to false if path has been compromised
		//Loop through each step in the path
		for (int i = 0; i < testPath.nStepFacing.size(); i++) {
			//Store the tile that is to be tested
			Tile tempTile = currentLevel.getTile(testPath.nStepX.get(i), testPath.nStepY.get(i));
			if (tempTile instanceof EmptyTile && ((EmptyTile) tempTile).hasArrow()) {
				//Store the direction of the arrow
				Direction tempDir = ((EmptyTile) tempTile).getArrow().getDir();
				//Convert direction
				int inDir = 0;
				if (tempDir == Direction.UP) {
					inDir = 0;
				} else if (tempDir == Direction.RIGHT) {
					inDir = 1;
				} else if (tempDir == Direction.DOWN) {
					inDir = 2;
				} else {
					inDir = 3;
				}
				//Check if arrow disrupts path
				if (inDir != testPath.nStepFacing.get(i)) {
					//Path dirupted! Record it and end loop
					pathWhole = false;
					i = testPath.nStepFacing.size();
				}

			}
		}
		return pathWhole;

	}

	/**
	 * Weights the options an AI has and determines how the AI will spend
	 * its arrows.
	 * @param currentMind The AI mind whose options will be weighted.
	 * @return Returns an AIMind that has decided its next cource of action.
	 */
	public AIMind weightOptions(AIMind currentMind) {

		int catDefenseRange = 9; //How many tiles a cat must walk at most to
		//reach players nest to be considered a threat.

		//Check how many arrows are left
		int remainingArrows = determineArrows(3, currentMind);
		//If remainaing arrows is zero or no rows exist, skip this
		if (lEntityRows.size() <= 0) {
			currentMind.nextAction = -1;
			return currentMind;
		}
		boolean rethink = false; //If this is true, an AI may replace its arrows
		//Only allow rethinks if no score has been gained and the rethink time
		//has passed

		if (currentMind.rethinkClock.read() > currentMind.rethinkTime && currentMind.scoreChange <= 0) {
			//Allow rethink and zero rethink timer.
			rethink = true;

			currentMind.rethinkClock.setTimestamp();
		}

		AIPath mousePath = new AIPath(); //The path for the mouse
		int mouseValue = 0; //The weighted value for attracting mice
		AIPath attackPath = new AIPath(); //The path for the cats
		int attackValue = 0; //The weighted value for attacking other players.
		AIPath defensePath = new AIPath(); //The path of the EntityRow to guard against
		int defenseValue = 0; //The weighted value for defending against cats.
		//Calculate attract, attacn and defend values
		//Loop through all rows and perform neccassery calculations
		for (EntityRow sCurRow : lEntityRows) {

			EntityRow workRow = sCurRow;
			//Temporary path
			AIPath pCurPath = new AIPath();
			//////////////////////////
			//DEFENSE
			//////////////////////////
			//Calculate defense information, if needed
			if (workRow.CatValue > defenseValue) {

				//See if cats will reach target
				pCurPath = calculatePath(-1, workRow.endX, workRow.endY, currentMind.NestX, currentMind.NestY, workRow.endFacing, currentMind.getOwner());
				//If no it can reach the lair and is within the cat defense range
				if (pCurPath.nTotalLength != -1 && pCurPath.nTotalLength < catDefenseRange) {
					//  System.out.println("Cat Threat for " +currentMind.getOwner().getName() + "! Length: " + pCurPath.nTotalLength + " Turns: "+ pCurPath.nTurnsTotal);
					//Store information about defense row
					defensePath = pCurPath;
					defenseValue = workRow.CatValue;

				}
			}
			//////////////////////////
			//ATTACK
			//////////////////////////
			//Calculate total attack value
			int totalDistance = -1;
			//Run this loop while total distance is equal to negative one
			while (totalDistance == -1) {
				//Only run this code for rows with cats in it
				if (workRow.CatValue > 0) {
					//This piece of code needs to check for each opposing AI mind
					for (AIMind currentTarget : lAIMinds) {
						//Only attempt to attack active enemies, not onself
						if (currentTarget.NestX != -1 && currentTarget.NestY != -1 && currentTarget.getOwner() != currentMind.getOwner()) {
							//System.out.println("Check attack for " + currentTarget.getOwner().getName());

							int bestIndex = workRow.estimateDistance(currentTarget.NestX, currentTarget.NestY, true);
							//Whole row is blocked, end this loop
							if (bestIndex == -1) //End loop
							{
								totalDistance = 0;
							} else {
								pCurPath = calculatePath(6, workRow.partX.get(bestIndex), workRow.partY.get(bestIndex), currentTarget.NestX, currentTarget.NestY, workRow.partFacing.get(bestIndex), currentMind.getOwner());
								//Check total distance, if it is -1 the path was blocked
								totalDistance = pCurPath.nTotalLength;
								//Path is invalid
								if (totalDistance == -1) {
									workRow.bannedPart.set(bestIndex, true);// = true;
								} else {

									//Save modified value
									workRow.modifyAttackValue(pCurPath);
									//Final modification of value
									int finalValue = workRow.ModifyValue * (currentTarget.getOwner().getScore() / 3);
									//Check if current values are the best yet and the path
									//does not need more arrows then avalible, or if rethink is
									//possible. Also make sure at least one arrow actually needs to be placed
									if (finalValue > attackValue &&
													pCurPath.nTurnsTotal <= remainingArrows && pCurPath.nTurnsTotal > 0 || rethink && finalValue > attackValue && pCurPath.nTurnsTotal > 0) {
										//They are, record all needed data.
										attackValue = finalValue;
										attackPath = pCurPath;
									//System.out.println("Attack Value: " + attackValue);
									}

								}
							}
						}
					}
				} else {
					//End loop
					totalDistance = 0;
				}
			}
			//////////////////////////
			//ATTRACTION
			//////////////////////////
			//If total distance is an invalid path, run the loop
			totalDistance = -1;
			//The value of the best path for mice
			while (totalDistance == -1) {
				int bestIndex = workRow.estimateDistance(currentMind.NestX, currentMind.NestY, true);
				//Whole row is blocked, end this loop
				if (bestIndex == -1) //End loop
				{
					totalDistance = 6;
				} else {
					pCurPath = calculatePath(6, workRow.partX.get(bestIndex), workRow.partY.get(bestIndex), currentMind.NestX, currentMind.NestY, workRow.partFacing.get(bestIndex), currentMind.getOwner());
					//Check total distance, if it is -1 the path was blocked
					totalDistance = pCurPath.nTotalLength;
					//Path is invalid
					if (totalDistance == -1) {
						workRow.bannedPart.set(bestIndex, true);// = true;
					} else {
						//Save modified value
						workRow.modifyValue(pCurPath);
						//Check if current values are the best yet and the path
						//does not need more arrows then avalible, or if rethink is
						//possible. Also make sure at least one arrow actually needs to be placed
						if (workRow.ModifyValue > mouseValue &&
										pCurPath.nTurnsTotal <= remainingArrows && pCurPath.nTurnsTotal > 0 || rethink && workRow.ModifyValue > mouseValue && pCurPath.nTurnsTotal > 0) {
							//They are, record all needed data.
							mouseValue = workRow.ModifyValue;
							mousePath = pCurPath;
						}

					}
				}

			}
		}
		//Calc done



		//Store attact data
		currentMind.nextPath = mousePath;

		//Finish defense value, its equal to one third of players score times cats
		defenseValue = 10 * defenseValue * (currentMind.getOwner().getScore() / 3);
		//Defend, attack or attack, that is the question
		if (defenseValue > mouseValue || defenseValue > 0 && remainingArrows != 0 && defenseValue > attackValue || defenseValue > 0 && remainingArrows != 0 || attackValue == 0) {
			//System.out.println("Mouse Value: " + mouseValue + " Defense Value: " + defenseValue);
			//Attract if no valid arrowplacement is found
			currentMind.nextAction = 0;
			//Defense data
			//Calculate defense arrow placement
			for (int i = defensePath.nStepFacing.size() - 1; i >= 0; i = i - 1) {
				//Check if arrow placement is possible
				if (nCurLevel[defensePath.nStepX.get(i)][defensePath.nStepY.get(i)][0] == 0 && !((EmptyTile) currentLevel.getTile(defensePath.nStepX.get(i), defensePath.nStepY.get(i))).hasArrow()) {
					//Arrowplacement possible, place arrow.
					currentMind.defenseX = defensePath.nStepX.get(i);
					currentMind.defenseY = defensePath.nStepY.get(i);
					//Determine defense facing
					if (defensePath.nStepFacing.get(i) == 0) {
						currentMind.defenseDir = 2; //South
					} else if (defensePath.nStepFacing.get(i) == 1) {
						currentMind.defenseDir = 3; //West
					} else if (defensePath.nStepFacing.get(i) == 2) {
						currentMind.defenseDir = 0; //North
					} else {
						currentMind.defenseDir = 1; //East
					}                       //Valid placement found, break loop and defend
					currentMind.nextAction = 1;
					i = -1;
				}
			}


		} else if (attackValue > mouseValue) {
			//Attack
			//Next path will be the attack path
			currentMind.nextPath = attackPath;
			System.out.println("AIAdv: " + currentMind.getOwner().getName() + " attacks! Grrr!");
			currentMind.nextAction = 2;
		} else if (mouseValue > 0) {
			//Attract
			currentMind.nextAction = 0;
		} else {
			//Do nothing
			currentMind.nextAction = -1;
		}

		return currentMind;
	}

	//This functions calculates the total value of mouse stacks, with
	//getting them to move allready calculated.
	public ArrayList<EntityRow> weightEntityRows(AIMind currentMind) {
		ArrayList<EntityRow> tempList = new ArrayList<EntityRow>();
		int index = 1;
		//Weight values for each stack
		Timer pathTimer = new Timer();

		for (EntityRow sCurRow : lEntityRows) {

			EntityRow workRow = sCurRow;
			AIPath pCurPath = new AIPath();
			int totalDistance = -1;
			while (totalDistance == -1) {
				//Estimate best location to start from in a row
				int bestIndex = workRow.estimateDistance(currentMind.NestX, currentMind.NestY, true);
				//store type of tile in row
				Tile testTile;
				testTile = currentLevel.getTile(workRow.partX.get(bestIndex), workRow.partY.get(bestIndex));
				//All tiles are tested, no more suitable can be found. End loop
				if (bestIndex == -1) {
					break;
				} //Make sure tiletype is emtpy
				else if (testTile instanceof EmptyTile) {
					pCurPath = calculatePath(6, workRow.partX.get(bestIndex), workRow.partY.get(bestIndex), currentMind.NestX, currentMind.NestY, workRow.partFacing.get(bestIndex), currentMind.getOwner());
					totalDistance = pCurPath.nTotalLength;
				} else //Mark tiles of non-empty class as invalid start tiles
				{
					totalDistance = -1;
				}

				if (totalDistance == -1) {
					workRow.bannedPart.set(bestIndex, true);// = true;
				} else {
					workRow.modifyValue(pCurPath);
					tempList.add(workRow);
					break;
				}


			}

		//System.out.println("Time taken: "+ pathTimer.read());
		//EntityStack tempStack = new EntityStack();
		//tempStack = sCurStack;
		//tempStack.modifyValue(pCurPath);
		//tempList.add(tempStack);*/

		}
		// System.out.println("Time taken: "+ pathTimer.read());
		// System.out.println("Rows: "+tempList.size());
		return tempList;
	//Return the mind with the weighted stacks

	}

	/**
	 * Used to fill the lEntityRows list with up to date entity rows.
	 */
	public void calculateEntityRows() {
		//Clear away old stacks
		lEntityRows.clear();
		Timer rowTimer = new Timer();
		//These are used to temporarily store stacks
		int[][][] nMouseStacks = new int[nMapX][nMapY][4];
		int[][][] nCatStacks = new int[nMapX][nMapY][4];
		//Temporary coordinates
		int nPosX;
		int nPosY;
		int nFacing;
		int nCatsTotal = 0;
		//Calculate Micestacks
		for (Mouse mouse : mice) {
			//Make sure entity is not dead
			if (!mouse.isDead()) {
				//Get position

				nPosX = mouse.getPosition().getXTilePosition();
				nPosY = mouse.getPosition().getYTilePosition();
				switch (mouse.getDirection()) {
					case UP:
						nFacing = 0;
					case RIGHT:
						nFacing = 1;
					case DOWN:
						nFacing = 2;
					default:
						nFacing = 3;
				}
				//Add +1 to stack value, Vanillamice
				if (mouse instanceof Mouse) {
					nMouseStacks[nPosX][nPosY][nFacing]++;
				}
				//Add +5 to stack value, Powermice
				if (mouse instanceof PowerupMouse) {
					nMouseStacks[nPosX][nPosY][nFacing] = nMouseStacks[nPosX][nPosY][nFacing] + 5;
				}
				//Add +15 to stack value, Kamizase
				if (mouse instanceof KamikazeMouse) {
					nMouseStacks[nPosX][nPosY][nFacing] = nMouseStacks[nPosX][nPosY][nFacing] + 15;
				}
				//Add +50 to stack value, Gold
				if (mouse instanceof GoldenMouse) {
					nMouseStacks[nPosX][nPosY][nFacing] = nMouseStacks[nPosX][nPosY][nFacing] + 50;
				}
				//Att to catstack instead of mouse stack, Agent
				if (mouse instanceof AgentMouse) {
					nCatStacks[nPosX][nPosY][nFacing]++;
				}
			}
		}
		//Calculate Catstacks
		for (Cat cat : cats) {
			//Make sure entity is not dead
			if (!cat.isDead()) {
				//Get position
				nPosX = cat.getPosition().getXTilePosition();
				nPosY = cat.getPosition().getYTilePosition();
				switch (cat.getDirection()) {
					case UP:
						nFacing = 0;
					case RIGHT:
						nFacing = 1;
					case DOWN:
						nFacing = 2;
					default:
						nFacing = 3;
				}
				//Add +1 to stack value, Vanilla cats
				if (cat instanceof Cat) {
					//System.out.println("AIADV: " + nPosX + " " + nPosY + " " + nFacing);
					nCatStacks[nPosX][nPosY][nFacing]++;
					nCatsTotal++;
				}

			}
		}
		//Calculate stack positions, used to calculate rows
		ArrayList<EntityStack> lEntityStacks = new ArrayList<EntityStack>();
		for (int i = 0; i < nMapY; i++) {
			for (int j = 0; j < nMapX; j++) {
				for (int k = 0; k < 4; k++) {
					//Micestacks
					if (nMouseStacks[j][i][k] > 0) {
						EntityStack sMouseStack = new EntityStack();
						sMouseStack.initStack(j, i, k, false, nMouseStacks[j][i][k]);
						lEntityStacks.add(sMouseStack);

					}
					//Catstacks
					if (nCatStacks[j][i][k] > 0) {
						EntityStack sCatStack = new EntityStack();
						sCatStack.initStack(j, i, k, true, nCatStacks[j][i][k]);
						lEntityStacks.add(sCatStack);

					}
				}
			}
		}
		//Calculate EntityRows
		for (int i = 0; i < lEntityStacks.size(); i++) {
			EntityStack currentStack = lEntityStacks.get(i);
			//Only rows not yet in a row may start a new one
			if (!currentStack.inARow) {
				EntityRow currentRow = new EntityRow();
				lEntityStacks = currentRow.generateRow(currentStack.X, currentStack.Y, currentStack.Heading, nCurLevel, lEntityStacks, lEntityRows, currentLevel);
				lEntityRows.add(currentRow);

			}
		}
	// System.out.println("Row calc Time: "+ rowTimer.read());
	//  if (nCatsTotal != 0)
	//         System.out.println("Cats Out: " + nCatsTotal);
	}
	//This method sets the nCurLevel to be consistent with the map

	public void updateMap() {
		//This variable is used to store the current tile being worked with
		Tile tCurrentTile;
		//Loop through Y
		for (int i = 0; i < nMapY; i++) {
			//Loop through X
			for (int j = 0; j < nMapX; j++) {
				//Get the current tile
				tCurrentTile = currentLevel.getTile(j, i);
				//Check if tile is emtpy.
				if (tCurrentTile instanceof EmptyTile) {
					nCurLevel[j][i][0] = 0;
				} else if (tCurrentTile instanceof Nest) {
					nCurLevel[j][i][0] = 5;
				} else if (tCurrentTile instanceof SpawnPoint) {
					nCurLevel[j][i][0] = 6;
				} else if (tCurrentTile instanceof BlackHole) {
					nCurLevel[j][i][0] = 7;
				} else if (tCurrentTile instanceof Glue) {
					nCurLevel[j][i][0] = 8;
				} else if (tCurrentTile instanceof Mousetrap) {
					nCurLevel[j][i][0] = 9;
				} else if (tCurrentTile instanceof Portal) {
					nCurLevel[j][i][0] = 10;
				} else if (tCurrentTile instanceof Cattrap) {
					nCurLevel[j][i][0] = 11;
				//Insert Traps here at a later date
				//Check for walls
				}
				if (tCurrentTile.hasWall(Direction.UP)) {
					nCurLevel[j][i][1] = 1;
				}
				if (tCurrentTile.hasWall(Direction.RIGHT)) {
					nCurLevel[j][i][2] = 1;
				}
				if (tCurrentTile.hasWall(Direction.DOWN)) {
					nCurLevel[j][i][3] = 1;
				}
				if (tCurrentTile.hasWall(Direction.LEFT)) {
					nCurLevel[j][i][4] = 1;
				}
			}

		}

	}

	//The pathfinding algorithm, used to find the best route
	//between two points and calculate where arrows need to be
	//placed.
	public AIPath calculatePath(int nOpenCost, int nOriginX, int nOriginY, int nEndX, int nEndY, int nInitialFacing, Player currentPlayer) {
		AIPath pFinalPath = new AIPath();
		//These 2d arrays are used for the A* algorithm
		ArrayList<Position> lOpenList = new ArrayList<Position>(); //The Open List
		int[][] nClosedList = new int[nMapX][nMapY]; //The Closed List
		//Note, 0 = Not Closed 1 = Closed 2 = Closed with all adjecant square allready
		//Tagged
		int[][] nParentX = new int[nMapX][nMapY]; //X cooridnate of Parent Square
		int[][] nParentY = new int[nMapX][nMapY]; //X cooridnate of Parent Square
		int[][] nFacing = new int[nMapX][nMapY]; //Objects facing in Square
		int[][] nMovVal = new int[nMapX][nMapY]; //The Movement Value of the current square
		int[][] nArrow = new int[nMapX][nMapY]; //Stores any arrows that needs
		//to be palced at the closed tile in question and which direction 
		//they have to point. Is -1 if no turn has to be made.
		//Initialising the search.
		nClosedList[nOriginX][nOriginY] = 1;
		nParentX[nOriginX][nOriginY] = -1;
		nParentY[nOriginX][nOriginY] = -1;
		nFacing[nOriginX][nOriginY] = nInitialFacing;
		nMovVal[nOriginX][nOriginY] = 0;
		nArrow[nOriginX][nOriginY] = -1;


		//WorkX and WorkY are cooridnates of the tile most recently
		//added to the closed list
		int nWorkX = nOriginX;
		int nWorkY = nOriginY;
		int nTest = 0;
		//The main search loop.
		while (nClosedList[nEndX][nEndY] != 1) {

			//Updating the Open List
			//Update Open List
                    /*
			Check each non-diagonal adjecant tile to see if one can add
			it to the open list. The center tile of this is the work
			coordinates, or the last tile added to the closed list.
			 */
			AICostCalc turnData = new AICostCalc();//This variable is used to
			//to store the cost for each direction, and any types of turns needed
			turnData.calculateMoveCost(nOpenCost, nWorkX, nWorkY, nEndX, nEndY, nFacing[nWorkX][nWorkY], currentPlayer, nClosedList, nCurLevel, currentLevel, nMapX, nMapY);
			int[] nDirCost = turnData.directionCost;
			//Add each direction to the open list of it is not blocked
			for (int i = 0; i < 4; i++) {

				if (nDirCost[i] != -1) {
					//Calculate next coordinates
					int[] nTargetCords = new int[2];
					nTargetCords = turnData.calculateNextCoordinate(nWorkX, nWorkY, i, nMapX, nMapY);
					//Create position and add to open list
					Position currentPosition = new Position();
					//Special stuff for portals
					if (nCurLevel[nTargetCords[0]][nTargetCords[1]][0] != 10) {
						currentPosition.X = nTargetCords[0];
						currentPosition.Y = nTargetCords[1];
					} else {
						//If portal, add target square to open list instead.
						Portal tempPortal = ((Portal) currentLevel.getTile(nTargetCords[0], nTargetCords[1])).getParnter();
						currentPosition.X = tempPortal.x;
						currentPosition.Y = tempPortal.y;
					}
					currentPosition.ParentX = nWorkX;
					currentPosition.ParentY = nWorkY;
					currentPosition.Heading = i;
					//If an arrow is needed, store it
					if (turnData.turnType[i] == 2) {
						currentPosition.ParentArrowDir = i;
					} else {
						currentPosition.ParentArrowDir = -1;
					}
					currentPosition.calcCost(nDirCost[i], nEndX, nEndY);
					lOpenList.add(currentPosition);
				}
			}

			//Find the tile in the openlist and add the one with the 
			//lowest movement cost to the closed list. This is the center
			//of finding the best path


			int nOpen = 0; //Selects the best open index in the open list.
			//Make sure the end point can be reached.
			if (lOpenList.size() > 0) {
				for (int i = 0; i < lOpenList.size(); i++) {
					if (lOpenList.get(i).EstDist <= lOpenList.get(nOpen).EstDist) {
						nOpen = i;
					}
				}
			} //The end point cannot be rached.
			else {
				//System.out.println("Warning! " + nEndX + "," + nEndY + " is" +
				//				"inaccessible from " + nOriginX + "," + nOriginY);
				pFinalPath.nTotalLength = -1;
				return pFinalPath;
			}
			//Add the open position found to the closed list
			int nAddX = lOpenList.get(nOpen).X;
			int nAddY = lOpenList.get(nOpen).Y;
			int nWorkParentX = lOpenList.get(nOpen).ParentX;
			int nWorkParentY = lOpenList.get(nOpen).ParentY;
			//ClosedList point data
			nClosedList[nAddX][nAddY] = 1;
			nParentX[nAddX][nAddY] = nWorkParentX;
			nParentY[nAddX][nAddY] = nWorkParentY;
			nMovVal[nAddX][nAddY] = lOpenList.get(nOpen).Cost;
			//Set Facing
			nFacing[nAddX][nAddY] = lOpenList.get(nOpen).Heading;
			//Set needed parent arrows
			nArrow[nAddX][nAddY] = lOpenList.get(nOpen).ParentArrowDir;
			//Update the work cooridnates, as a new tile has been
			//added to the closed list.
			nWorkX = nAddX;
			nWorkY = nAddY;
			//Remove the added object from the opnelist.
			lOpenList.remove(nOpen);
			nTest++;
		}
		//Now, we should have a complete path to the target, 
		//Here we calculate important data about the path and saves it
		int nTurnsTotal = 0;
		int nLengthTotal = 0;
		int nCurrentX = nEndX;//nParentX[nEndX][nEndY];
		int nCurrentY = nEndY;//nParentY[nEndX][nEndY];



		//Loop and calculate all relevant values
		while (nCurrentX != nOriginX || nCurrentY != nOriginY) {
			pFinalPath.nStepX.add(nCurrentX);
			pFinalPath.nStepY.add(nCurrentY);
			pFinalPath.nStepFacing.add(nFacing[nCurrentX][nCurrentY]);
			//Get Parent

			int nTempX = nParentX[nCurrentX][nCurrentY];
			int nTempY = nParentY[nCurrentX][nCurrentY];


			//Check if arrow is place there
			//Check for turns
			Tile tempTile = currentLevel.getTile(nTempX, nTempY);
			if (tempTile instanceof EmptyTile && !((EmptyTile) tempTile).hasArrow() && nArrow[nCurrentX][nCurrentY] != -1) {
				//Arrow needs to be placed at this point, store its
				//cooridnates
				//System.out.println("Turn X: " + nCurrentX + "Turn Y:" + nCurrentY + " Dir: " + nArrow[nCurrentX][nCurrentY]);
				pFinalPath.nTurnX.add(nTempX);
				pFinalPath.nTurnY.add(nTempY);

				pFinalPath.nTurnDir.add(nArrow[nCurrentX][nCurrentY]);
				nTurnsTotal++;
			}

			//Make ready for next square

			//End loop if next square would be invalid, just to be safe

			nCurrentX = nTempX;
			nCurrentY = nTempY;
			nLengthTotal++;
		}

		//Store length and turns, used when AI is weighting different options
		pFinalPath.nTotalLength = nLengthTotal;
		pFinalPath.nTurnsTotal = nTurnsTotal;
		return pFinalPath;
	}
}

//  //Initiate AI
//                        ais = new AIAdv(level,cats,mice,GameplayManager.getInstance().players
//                        ,arrows,level.tileSize);
