package mouserunner.AI;

import mouserunner.Game.Arrow;
import mouserunner.Game.Level;
import mouserunner.Game.Player;
import mouserunner.LevelComponents.EmptyTile;
import mouserunner.LevelComponents.Portal;
import mouserunner.System.Direction;

/**
 *
 * @author Pierre Andersson
 */
public class AICostCalc {
	//Data storage

	int[] directionCost = new int[4]; //The cost to move in each cardinal direction
	//if set to -1 or less for any reason that direction i blocked.
	int[] turnType = new int[4]; //The type of turn needed to move to this open square
	//0 = No turn, 1 = free turn,
	//2 = placed arrow turn

	//This Method calculates the move from one tile to each adjecant tile
	//@return Returns the move cost from nX,nY to each adjecant tile. If it
	//returns -1 that direction is blocked.
	protected void calculateMoveCost(int nOpenCost, int nX, int nY, int nTargetX, int nTargetY, int nDirection, Player currentPlayer, int nClosedList[][], int nCurLevel[][][], Level currentLevel, int nMapX, int nMapY) {

		int nTurnDir = -1; //The direction this tile will force a turn, is set
		//later in code
		//Cost settings
		int nArrowCost = 2; //The movement cost of an unstable arrow.
		int nGlueCost = 5; //The cost of crossing glue
		int nMousetrap = 4; //The cost of crossing a mousetrap
		int nCattrap = -4; //The cost of crossing a cattrap
		//by use of an arrow. If -1 if keep current direction
		//First, check if there is an arrow and set force turn.

		//Check if current square has an arrow
		if (currentLevel.getTile(nX, nY) instanceof EmptyTile && ((EmptyTile) currentLevel.getTile(nX, nY)).hasArrow()) {
			//It has an arrow, store it
			Arrow currentArrow = ((EmptyTile) currentLevel.getTile(nX, nY)).getArrow();
			//Permanent arrows cost no extra
			if (currentArrow.getOwner() == null) {
				nArrowCost = 0;
			} //Self-owned arrows cost full
			else if (currentArrow.getOwner() == currentPlayer) {
				nArrowCost = 0;
			}
			//Check current direction and any forced directions
			if (currentArrow.getDir() == Direction.UP) {
				nTurnDir = 0;
			} else if (currentArrow.getDir() == Direction.RIGHT) {
				nTurnDir = 1;
			} else if (currentArrow.getDir() == Direction.DOWN) {
				nTurnDir = 2;
			} else if (currentArrow.getDir() == Direction.LEFT) {
				nTurnDir = 3;
			}
		//Set direction to turn direction
		//nDirection = nTurnDir;
		}

		//End of arrow loop


		//Run four times, once for each direction
		for (int i = 0; i < 4; i++) {
			int nNextX; //The target X coordinate
			int nNextY; //The target Y coordinate

			int nCost = 1; //Each square has a move cost of at least one
			//This method may increase nCost
			int[] nTargetCords = new int[2];
			nTargetCords = calculateNextCoordinate(nX, nY, i, nMapX, nMapY);
			//Set the coordinates
			nNextX = nTargetCords[0];
			nNextY = nTargetCords[1];

			//Check Walls
			if (nCurLevel[nX][nY][i + 1] == 1) {
				directionCost[i] = -1;
			} //Tile type is unimportant for taget tile
			else if (nNextX != nTargetX || nNextY != nTargetY) {
				//Check tile type
				//Check if it is closed
				if (nClosedList[nNextX][nNextY] == 1) {
					directionCost[i] = -1;
				} //Nest
				else if (nCurLevel[nNextX][nNextY][0] == 5) {
					directionCost[i] = -1;
				} //Blackhole
				else if (nCurLevel[nNextX][nNextY][0] == 7) {
					directionCost[i] = -1;
				} //Glue
				else if (nCurLevel[nNextX][nNextY][0] == 8) {
					nCost = nCost + nGlueCost;
				} //Mousetrap
				else if (nCurLevel[nNextX][nNextY][0] == 9) {
					nCost = nCost + nMousetrap;
				} //Cattrap
				else if (nCurLevel[nNextX][nNextY][0] == 11) {
					nCost = nCost + nCattrap;
				} //Portal
				else if (nCurLevel[nNextX][nNextY][0] == 10) {
					//Set next tile to portal exit
					//No special cost calc portal code is needed
                     /*   Portal tempPortal = ((Portal)currentLevel.getTile(nNextX, nNextY)).getParnter();
					nNextX = tempPortal.x;
					nNextY = tempPortal.y;*/
				}
			}
			//If direction is not straitght ahead and not blocked
			if (nDirection != i && directionCost[i] != -1) {
				boolean freeTurn = false; //Set to true if turning in this direction
				//does not require an arrow placement
				//Arrowturn in this direction, turning is free no matter what.
				if (nTurnDir == i) {
					freeTurn = true;
				} //Make sure there is a wall ahead
				else if (nCurLevel[nX][nY][nDirection + 1] == 1) {
					int nTurnwall = nDirection + 1; //The direction clockwise from this position
					if (nTurnwall == 4) {
						nTurnwall = 0; //West becomes north
					}                        //Clockwise when wall is ahead is free
					if (i == nTurnwall) {
						freeTurn = true;
					} //Counter clockwise when wall is ahead and to right is free
					else if (nCurLevel[nX][nY][nTurnwall + 1] == 1) {
						nTurnwall = nDirection - 1; //Counterclockwise
						if (nTurnwall == -1) {
							nTurnwall = 3; //North becomes West
						}
						if (i == nTurnwall) {
							freeTurn = true;
						}
					}
				}
				//If turn is free
				if (freeTurn) {
					//Records as free turn
					turnType[i] = 1;
				} else {
					//If arrow turn or if an arrow cannot be placed on this tile type.
					if (nTurnDir != -1 && nTurnDir != i || nCurLevel[nX][nY][0] != 0 || nOpenCost == -1 && nTurnDir != i) {
						//If blocked of due to arrow,
						//or an arrow cannot be placed on this tile
						//or no open turns allowed.
						directionCost[i] = -1;
					} else {
						//Record as an open turn
						nCost = nCost + nOpenCost;
						turnType[i] = 2;
					}
				}
			/*int nTurnWall = i -1; //The wall counterclockwise from current pos
			if (nTurnWall == -1) nTurnWall = 3; //North becomes west
			//Check
			if (nCurLevel[nX][nY][nTurnWall+1] == 0 || nCurLevel[nX][nY][nDirection+1] == 0 ) {
			if (nTurnDir != -1)
			{
			//If blocked of due to arrow, close this open turn
			directionCost[i]=-1;
			}
			else
			{
			//Record as an open turn
			nCost = nCost + nOpenCost;
			turnType[i] = 2;
			}
			}
			else
			//Revords as free turn
			turnType[i] = 1; */
			} //If direction is straightahead, but there is an arrow that points another way!
			else if (nDirection == i && nTurnDir != i && nTurnDir != -1) {
				directionCost[i] = -1;
			} //Record that this direction is not a turn
			else {
				turnType[i] = 0;
			}
			//If not blocked, store Direction cost
			//Arrow turn
			if (directionCost[i] != -1 && nTurnDir == -1) {
				directionCost[i] = nCost + nArrowCost;
			} //Nonarrow turn
			else if (directionCost[i] != -1) {
				directionCost[i] = nCost;
			}



		}
	//End of direction loop

	//Block of any directions not enabled due to arrows
           /* if (nTurnDir != -1)
	{
	int nFinalDirection = -1; //The final direction after making an arrow turn
	//Four step process
	for (int i = 0; i < 4; i++)
	{
	int nCheckDir = nTurnDir; //The Direction to check
	if (i ==1 ) nCheckDir=+1; //Check to the right
	else if (i ==2 ) nCheckDir=-1; //Check to the left
	else if (i ==3 ) nCheckDir=-2; //Check behind
	//Convert errat nCheckDir values to correct ones
	if (nCheckDir == 4) nCheckDir = 0; //4 is north
	else if (nCheckDir == -1) nCheckDir = 3; //-1 is west
	else if (nCheckDir == -2) nCheckDir = 2; //-2 is south
	//Check if the direction is walled off
	if (nCurLevel[nX][nY][nCheckDir+1] != 1)
	{
	nFinalDirection = nCheckDir;
	//Set final direction to an open turn
	turnType[nFinalDirection] = 1;
	break;
	}
	}
	if (nFinalDirection == -1) System.out.println("Pathfinding error, closed location encircled by walls!");
	//Block off non-final directions.
	if (nFinalDirection != 0) directionCost[0] =-1;
	else if (directionCost[0] !=-1) directionCost[0] =+ nArrowCost;
	if (nFinalDirection != 1) directionCost[1] =-1;
	else if (directionCost[1] !=-1)directionCost[1] =+ nArrowCost;
	if (nFinalDirection != 2) directionCost[2] =-1;
	else if (directionCost[2] !=-1)directionCost[2] =+ nArrowCost;
	if (nFinalDirection != 3) directionCost[3] =-1;
	else if (directionCost[3] !=-1)directionCost[3] =+ nArrowCost;
	}
	//End of method

	 */
	//<editor-fold defaultstate="collapsed" desc=" Old Code ">
                /* OLD CODE, preserved just in case
	//Check North
	if (nCurLevel[nWorkX][nWorkY][1] != 1 && nCurLevel[nWorkX][nWorkY - 1][0] == 1 && nClosedList[nWorkX][nWorkY - 1] < 1 || nCurLevel[nWorkX][nWorkY][1] != 1 && nCurLevel[nWorkX][nWorkY - 1][0] == 6 && nClosedList[nWorkX][nWorkY - 1] < 1 || nCurLevel[nWorkX][nWorkY][1] != 1 && nWorkX == nEndX && nWorkY == nEndY) {
	Position pTemp = new Position();
	pTemp.X = nWorkX;
	pTemp.Y = nWorkY - 1;
	pTemp.ParentX = nWorkX;
	pTemp.ParentX = nWorkY;
	//Check if a turn has to be made
	if (nFacing[nWorkX][nWorkY] == 0 || nFacing[nWorkX][nWorkY] == 3 && nCurLevel[nWorkX][nWorkY][4] == 1) {
	pTemp.calcCost(nMovVal[nWorkX][nWorkY], nEndX, nEndY);
	} else {
	pTemp.calcCost(nMovVal[nWorkX][nWorkY] + 10, nEndX, nEndY);
	nArrow[nWorkX][nWorkY] = 0;
	} //Arrow pointing north has to be placed
	lOpenList.add(pTemp);
	}


	//Check East
	if (nCurLevel[nWorkX][nWorkY][2] != 1 && nCurLevel[nWorkX + 1][nWorkY][0] == 1 && nClosedList[nWorkX + 1][nWorkY] < 1 || nCurLevel[nWorkX][nWorkY][2] != 1 && nCurLevel[nWorkX + 1][nWorkY][0] == 6 && nClosedList[nWorkX + 1][nWorkY] < 1 || nCurLevel[nWorkX][nWorkY][2] != 1 && nWorkX == nEndX && nWorkY == nEndY) {
	Position pTemp = new Position();
	pTemp.X = nWorkX + 1;
	pTemp.Y = nWorkY;
	pTemp.ParentX = nWorkX;
	pTemp.ParentX = nWorkY;
	//Check if a turn has to be made
	if (nFacing[nWorkX][nWorkY] == 1 || nFacing[nWorkX][nWorkY] == 0 && nCurLevel[nWorkX][nWorkY][1] == 1) {
	pTemp.calcCost(nMovVal[nWorkX][nWorkY], nEndX, nEndY);
	} else {
	pTemp.calcCost(nMovVal[nWorkX][nWorkY] + 10, nEndX, nEndY);
	nArrow[nWorkX][nWorkY] = 1;
	}//Arrow pointing east has to be placed
	lOpenList.add(pTemp);
	}

	//Check South
	if (nCurLevel[nWorkX][nWorkY][3] != 1 && nCurLevel[nWorkX][nWorkY + 1][0] == 1 && nClosedList[nWorkX][nWorkY + 1] < 1 || nCurLevel[nWorkX][nWorkY][3] != 1 && nCurLevel[nWorkX][nWorkY + 1][0] == 6 && nClosedList[nWorkX][nWorkY + 1] < 1 || nCurLevel[nWorkX][nWorkY][3] != 1 && nWorkX == nEndX && nWorkY == nEndY) {
	Position pTemp = new Position();
	pTemp.X = nWorkX;
	pTemp.Y = nWorkY + 1;
	pTemp.ParentX = nWorkX;
	pTemp.ParentX = nWorkY;
	//Check if a turn has to be made
	if (nFacing[nWorkX][nWorkY] == 2 || nFacing[nWorkX][nWorkY] == 1 && nCurLevel[nWorkX][nWorkY][2] == 1) {
	pTemp.calcCost(nMovVal[nWorkX][nWorkY], nEndX, nEndY);
	} else {
	pTemp.calcCost(nMovVal[nWorkX][nWorkY] + 10, nEndX, nEndY);
	nArrow[nWorkX][nWorkY] = 2;
	}//Arrow pointing south has to be placed
	lOpenList.add(pTemp);
	}

	//Check West
	if (nCurLevel[nWorkX][nWorkY][4] != 1 && nCurLevel[nWorkX - 1][nWorkY][0] == 1 && nClosedList[nWorkX - 1][nWorkY] < 1 || nCurLevel[nWorkX][nWorkY][4] != 1 && nCurLevel[nWorkX - 1][nWorkY][0] == 6 && nClosedList[nWorkX - 1][nWorkY] < 1 || nCurLevel[nWorkX][nWorkY][4] != 1 && nWorkX == nEndX && nWorkY == nEndY) {
	Position pTemp = new Position();
	pTemp.X = nWorkX - 1;
	pTemp.Y = nWorkY;
	pTemp.ParentX = nWorkX;
	pTemp.ParentX = nWorkY;
	//Check if a turn has to be made
	if (nFacing[nWorkX][nWorkY] == 3 || nFacing[nWorkX][nWorkY] == 2 && nCurLevel[nWorkX][nWorkY][3] == 1) {
	pTemp.calcCost(nMovVal[nWorkX][nWorkY], nEndX, nEndY);
	} else {
	pTemp.calcCost(nMovVal[nWorkX][nWorkY] + 10, nEndX, nEndY);
	nArrow[nWorkX][nWorkY] = 0;
	} //Arrow pointing west has to be placed
	lOpenList.add(pTemp);
	}
	 */
	//</editor-fold>

	}

	protected int[] calculateNextCoordinate(int nX, int nY, int nDirection, int nMapX, int nMapY) {
		int[] nCords = new int[2]; //The target coordinates
		//Set the next coordinate based on which direction is being checked
		switch (nDirection) {
			case 0:
				nCords[0] = nX;
				nCords[1] = nY - 1;
				break; //North
			case 1:
				nCords[0] = nX + 1;
				nCords[1] = nY;
				break; //East
			case 2:
				nCords[0] = nX;
				nCords[1] = nY + 1;
				break; //South
			default:
				nCords[0] = nX - 1;
				nCords[1] = nY;
				break; //West
		}
		//Check if target is outside map, then move target coordinates to
		//the opposite side of the map
		if (nCords[0] == -1) {
			nCords[0] = nMapX - 1;
		} else if (nCords[0] == nMapX) {
			nCords[0] = 0;
		} else if (nCords[1] == -1) {
			nCords[1] = nMapY - 1;
		} else if (nCords[1] == nMapY) {
			nCords[1] = 0;
		}
		//Return target coordinates
		return nCords;
	}
}
