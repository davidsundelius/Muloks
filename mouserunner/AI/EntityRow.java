/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mouserunner.AI;

import java.util.ArrayList;
import java.util.LinkedList;
import mouserunner.Game.Arrow;
import mouserunner.Game.Level;
import mouserunner.LevelComponents.BlackHole;
import mouserunner.LevelComponents.EmptyTile;
import mouserunner.LevelComponents.Nest;
import mouserunner.System.Direction;

/**
 *
 * @author Pierre Andersson
 */
//Used to list stacks 
class EntityRow {

	LinkedList<Integer> partX = new LinkedList<Integer>();
	LinkedList<Integer> partY = new LinkedList<Integer>();
	LinkedList<Boolean> emptyPart = new LinkedList<Boolean>();
	//Banned parts are used in pathfinding to flag parts of a row as invalid
	//start coordinates, the list will be as long as there are coordinates in
	//a row.
	LinkedList<Boolean> bannedPart = new LinkedList<Boolean>();
	LinkedList<Integer> partFacing = new LinkedList<Integer>();
	LinkedList<Integer> turnX = new LinkedList<Integer>();
	LinkedList<Integer> turnY = new LinkedList<Integer>();
	//How many empty tiles this row has at its end
	int endEmpty = 0;
	//How many empty tiles this row has at its beginning
	int beginningEmpty = 0;
	//Data about the final tile
	int endX;
	int endY;
	int endFacing;
	int rowLength = 1;
	int CatValue = 0;
	int MouseValue = 0; //Default mouse value, if lower may cause weird behaviour
	int ModifyValue = 0; //The value of the entityrow modified by path to target
	//Only used when stored in an AIMind
	AIPath rowPath = new AIPath(); //Storage for the path to the target
	//Only used when stored in an AIMind

	//Returns the shortest estimated distance to x,y
	public int estimateDistance(int targetX, int targetY, boolean returnIndex) {
		int bestIndex = 0; //Stores the index to the shortes position one can use
		int bestDistance = 0; //Stores the estimated distance of the best index
		//Run this for each tile in
		for (int i = 0; i < partFacing.size(); i++) {
			int workX = partX.get(i);
			int workY = partY.get(i);
			int totalDistance;
			int workDistance;
			//Estimate X distance
			workDistance = workX - targetX;
			if (workDistance < 0) {
				workDistance = workDistance * -1;
			}
			totalDistance = workDistance;
			//Esitmate Y distasnce
			workDistance = workY - targetY;
			if (workDistance < 0) {
				workDistance = workDistance * -1;
			}
			totalDistance = totalDistance + workDistance;
			//Check if this index is better then the prev best
			if (bestDistance < totalDistance && bannedPart.get(i) == false) {
				//It is, set this index as bestIndex
				bestIndex = i;
			}
			if (bestIndex == 0 && bannedPart.get(0) == true) {
				bestIndex = -1;
				break;
			}
		}
		//Send back the index
		if (returnIndex) {
			return bestIndex;
		} else {
			return bestDistance;
		}
	}

	/**
	 * Modifies the value of an entityRow depedning on distance and turns.
	 * @param modifyPath The path the entityRow will take.
	 */
	public void modifyValue(AIPath modifyPath) {
		rowPath = modifyPath;
		//Values equals 10 times the amount of mice / the amount of arrows that is needed.
		ModifyValue = (10 * MouseValue) / (modifyPath.nTurnsTotal + 1);
		//Substract 250 for each cat/mulok agent in a given row.
		ModifyValue = ModifyValue + (CatValue * -250);
		//For each tile longer then 4, 2 is substracted from the total value
		if (modifyPath.nTotalLength > 4 && ModifyValue > modifyPath.nTotalLength) {
			ModifyValue = ModifyValue - 2 * (modifyPath.nTotalLength - 4);
		}

	}

	/**
	 * Modifies the value of an entityRow that is to be used offensivly
	 * depedning on distance and turns .
	 * @param modifyPath The path the entityRow will take.
	 */
	public void modifyAttackValue(AIPath modifyPath) {
		rowPath = modifyPath;
		//Values equals 10 times the amount of cats / the amount of arrows that is needed.
		ModifyValue = (10 * CatValue) / (modifyPath.nTurnsTotal + 1);
		//Substract 2 for each mouse agent in the given row.
		ModifyValue = ModifyValue + (MouseValue * -2);
		//For each tile longer then 4, 2 is substracted from the total value
		if (modifyPath.nTotalLength > 4 && ModifyValue > modifyPath.nTotalLength) {
			ModifyValue = ModifyValue - 2 * (modifyPath.nTotalLength - 4);
		}

	}
	//Add a cooridnate to the entity row

	public void addCoordinate(int X, int Y, int facing) {
		partX.add(X);
		partY.add(Y);
		partFacing.add(facing);

	}
	//Add a turn to the entity row

	public void addTurn(int X, int Y) {
		turnX.add(X);
		turnY.add(Y);

	}

	/**
	 * Creates a list of entity stacks that is part of a new entity row. An entity row
	 * is a list of entities that will each walk the same path if nothing disturbs them.
	 * Pathinding is abstracted and done only once per entityRow and nest in order to
	 * reduce CPU load of the AI. Each entity row is treated as its own object for the
	 * purpose of AI, the result may be imperfect but it is light. Perfect for a fast
	 * paced game.
	 * @param initialX The X coordinate that the row will start from.
	 * @param initialY The Y coordinate that the row will start from.
	 * @param initialFacing The Initial Facing of the entity row.
	 * @param currentMap The current map as a standard 3d array of integers.
	 * @param currentStacks The current avalible and valid entity stacks.
	 * @param currentRows The currently generated entityRows, so no two rows cross each other.
	 * @param currentLevel The current level.
	 * @return A list of EntityStacks as part of the new row.
	 */
	public ArrayList<EntityStack> generateRow(int initialX, int initialY, int initialFacing, int[][][] currentMap, ArrayList<EntityStack> currentStacks, ArrayList<EntityRow> currentRows, Level currentLevel) {
		int maxLength = 16; //A row may not be longer then this
		int maxEmptyLength = 2; //A row may not be emtpy for more tiles then this
		//These variables are used to store information about the row being
		//generated.
		int currentX = initialX;
		int currentY = initialY;
		int currentFacing = initialFacing;
		boolean reversed = false; //If this is true, check backwards from start instead of
		//forwards
		boolean blocked = false; //If this is true, then the row cannot be calculated anymore
		//This methods mainloop
		while (!blocked && rowLength < maxLength) {
			int nextX; //The X coordinate of next tile
			int nextY; //The Y coordinate of next tile
			int nextFacing; //The facing of next tile
			boolean endBlocked = false; //If this end is blocked, set to true
			boolean isEmpty = true; //Set to false if a stack exists at point
			//First, check for entitystacks that occipies current location
			for (EntityStack workStack : currentStacks) {
				//Check if a stack occupies this location
				if (workStack.X == currentX && workStack.Y == currentY && workStack.Heading == currentFacing) {
					//There is, flag stack as in a row and add its values to the row
					workStack.inARow = true;
					isEmpty = false;
					//If its a mouse stack, add the mice value to the row, else add the catvalue
					if (!workStack.CatStack) {
						MouseValue = MouseValue + workStack.StackValue;
					} else {
						CatValue = CatValue + workStack.StackValue;
					}
				}
			}
			//Entity stack checking done

			//If not empty, zero relevant empty counter
			if (!isEmpty && !reversed) {
				//Store end values, in case this tile is the last one
				endX = currentX;
				endY = currentY;
				endFacing = currentFacing;
				endEmpty = 0;
			} else if (!isEmpty && reversed) {
				beginningEmpty = 0;
			} //If empty, increase relevant counter
			else if (!reversed) {
				endEmpty++;
			} else {
				beginningEmpty++;
			}
			//Add current coordinats to row
			addCoordinate(currentX, currentY, currentFacing);
			emptyPart.add(isEmpty);
			bannedPart.add(false);

			//First, determine the direction for checking the next tile
			int currentDirection; //The direction this will head in
			//if not reversed, direction equals this tiles facing, if reversed, its the oppsite
			if (!reversed) {
				currentDirection = currentFacing;
			} else if (currentFacing == 0) {
				currentDirection = 2; //North, becomes south
			} else if (currentFacing == 1) {
				currentDirection = 3; //East west
			} else if (currentFacing == 2) {
				currentDirection = 0; //South north
			} else {
				currentDirection = 1; //west east
			}                //Next, check for walls
			//If not reversed, check which direction any mice will head
			if (!reversed && currentMap[currentX][currentY][currentDirection + 1] == 1) {
				//Wall is ahead, check right
				int nextCheck = currentDirection;
				//Determine direction that is to the right
				if (nextCheck == 0) {
					nextCheck = 1; //North east
				} else if (nextCheck == 1) {
					nextCheck = 2; //East south
				} else if (nextCheck == 2) {
					nextCheck = 3; //South west
				} else {
					nextCheck = 0; //west north
				}                    //Check collition
				if (currentMap[currentX][currentY][nextCheck + 1] == 0) {
					currentDirection = nextCheck; //No colition, turn right
				} else {
					//Colition, turn left
					//Determine direction that is to the Left
					if (nextCheck == 0) {
						nextCheck = 3; //North west
					} else if (nextCheck == 1) {
						nextCheck = 0; //East north
					} else if (nextCheck == 2) {
						nextCheck = 1; //South east
					} else {
						nextCheck = 2; //west south
					}                       //Check collition again
					if (currentMap[currentX][currentY][nextCheck + 1] == 0) {
						currentDirection = nextCheck; //No colition, turn left
					} else //Collition, block this end
					{
						endBlocked = true;
					}

				}
			} //Check walls when reversed
			else if (reversed && currentMap[currentX][currentY][currentDirection + 1] == 1) {
				//Wall is behind, check left
				int nextCheck = currentDirection;
				//Determine direction that is to the Left
				if (nextCheck == 0) {
					nextCheck = 3; //North west
				} else if (nextCheck == 1) {
					nextCheck = 0; //East north
				} else if (nextCheck == 2) {
					nextCheck = 1; //South east
				} else {
					nextCheck = 2; //west south
				}                    //Check collition
				if (currentMap[currentX][currentY][nextCheck + 1] == 0) {
					currentDirection = nextCheck; //No colition, turn left
				} else {
					//Colition, turn right
					//Determine direction that is to the right
					if (nextCheck == 0) {
						nextCheck = 1; //North east
					} else if (nextCheck == 1) {
						nextCheck = 2; //East south
					} else if (nextCheck == 2) {
						nextCheck = 3; //South west
					} else {
						nextCheck = 0; //west north
					}                       //Check collition again
					if (currentMap[currentX][currentY][nextCheck + 1] == 0) {
						currentDirection = nextCheck; //No colition, turn right
					} else //Collition, block this end
					{
						endBlocked = true;
					}

				}
			}
			//End wall checking, determine next tile based on direction
			if (currentDirection == 0) {
				nextX = currentX;
				nextY = currentY - 1;
			}//North
			else if (currentDirection == 1) {
				nextX = currentX + 1;
				nextY = currentY;
			}//East
			else if (currentDirection == 2) {
				nextX = currentX;
				nextY = currentY + 1;
			}//South
			else {
				nextX = currentX - 1;
				nextY = currentY;
			} //West
			//Check if terrain blocks, or if it is outside map
			if (nextX == -1 || nextY == -1 || nextX >= currentLevel.width || nextY >= currentLevel.height || currentLevel.getTile(nextX, nextY) instanceof Nest || currentLevel.getTile(nextX, nextY) instanceof BlackHole) {
				//It blocks, flag as blocked
				endBlocked = true;
			}
			//Check for arrows to dermine facing of next tile
			if (!endBlocked && currentLevel.getTile(nextX, nextY) instanceof EmptyTile && ((EmptyTile) currentLevel.getTile(nextX, nextY)).hasArrow()) {
				//Has arrow, set facing of arrow tile equal to the direction
				//the arrow points
				Arrow currentArrow = ((EmptyTile) currentLevel.getTile(nextX, nextY)).getArrow();
				if (currentArrow.getDir() == Direction.UP) {
					nextFacing = 0;
				} else if (currentArrow.getDir() == Direction.RIGHT) {
					nextFacing = 1;
				} else if (currentArrow.getDir() == Direction.DOWN) {
					nextFacing = 2;
				} else {
					nextFacing = 3;
				}
			} else {
				//No arrow, facing equals direction if not reversed. Else
				//Facing equals opposite direction
				if (!reversed) {
					nextFacing = currentDirection;
				} else if (currentDirection == 0) {
					nextFacing = 2;//North becomes south
				} else if (currentDirection == 1) {
					nextFacing = 3;//East becomes west
				} else if (currentDirection == 2) {
					nextFacing = 0;//South becomes north
				} else {
					nextFacing = 1;//West becomes east
				}
			}


			//Check self collition
			for (int i = 0; i < rowLength; i++) {
				if (partX.get(i) == nextX && partY.get(i) == nextY && partFacing.get(i) == nextFacing) {
					//Self collition! Block this end
					endBlocked = true;
				}
			}
			//Check colition with other rows
			for (EntityRow testRow : currentRows) {
				//Check all coordinates for collition
				for (int i = 0; i < testRow.partX.size(); i++) {
					if (testRow.partX.get(i) == nextX && testRow.partY.get(i) == nextY && testRow.partFacing.get(i) == nextFacing) {
						//Row collition! Block this end
						endBlocked = true;
					//Check if row merging is possible
                       /*     if (testRow.rowLength - testRow.beginningEmpty - testRow.endEmpty < maxLength)
					{
					//Merging possible!
					MergeRows(testRow);
					}*/
					}
				}
			}
			//Make sure row length is valid
			if (!reversed && endEmpty > maxEmptyLength) {
				//Invalid row length, block
				endBlocked = true;
			} else if (reversed && beginningEmpty > maxEmptyLength) {
				//Invalid row length, block
				endBlocked = true;
			}

			//Tests are done, prepere for next tile, reverse or complete row
			if (endBlocked && reversed) {
				blocked = true; //End loop
			} else if (endBlocked) {


				//Start from tile 1
				reversed = true; //Reverse loop
				currentX = initialX;
				currentY = initialY;
				currentFacing = initialFacing;

			} else {
				//Continue normally
				currentX = nextX;
				currentY = nextY;
				currentFacing = nextFacing;
				rowLength++;
			}

		}
		//  System.out.println("Length: "+ rowLength);
		return currentStacks;
	}
}
//Used to list stacks 

class EntityStack {

	int X;
	int Y;
	int Heading;
	boolean CatStack;
	int StackValue;
	boolean inARow = false; //If this is true, the stack is allready a part of a row

	public void initStack(int nX, int nY, int nHeading, boolean bIsCatStack, int nValue) {
		X = nX;
		Y = nY;
		Heading = nHeading;
		CatStack = bIsCatStack;
		StackValue = nValue;
	}
}
