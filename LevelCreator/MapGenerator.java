package LevelCreator;

import java.util.*;
import javax.swing.JOptionPane;
import java.io.*;
import mouserunner.Game.Level;
import mouserunner.Game.SortedTileList;
import mouserunner.LevelComponents.*;

public class MapGenerator {
	//Map Storage variables

	int[][][] nMap;
	int[][][] nSplice;
	int nSpliceX;
	int nSpliceY;

	///////////////////////////////Tile Value Key////////////////
	//0 = Open Floor
	//1 = Reserved Path
	//2 = Spawn
	//3 = Nest
	//4 = North Arrow
	//5 = East Arrow
	//6 = South Arrow
	//7 = West Arrow
	//8+ = Trap
	////////////////////////////////////////////////  MAP ALGORITHM //////////////////////////
	//This function generates a new random map.
	/*Parameter key
	nWidth = The width of the map (min 5)
	nHeight = The height of the map (min 5)
	nPlayers = How many nests and spawns the algorithm will place. Min 2 max 4
	bSymmetry = If this is True, the map is symmetrical otherwise its asymetrical. Note that the function currently only
	supports symmetrical maps.
	bTraps = If true, the algortithm will place traps
	nMapType = Determines the general layout of the map. 
	Map Types
	-1 = Random
	0 = The nests will be in the middle of the map, the spawn points in the corners.
	1 = Nests in corners, spawns in the middle.
	2 = Nests along top edge, spawns along bottom edge. 
	3 = Nests along right edge, spawns along left edge.
	4 = Nests bottom, spawns top.
	5 = Nests left, spawns right.
	 */
	public boolean MapAlgorithm(int nWidth, int nHeight, int nPlayers, boolean bSymmetry, int nMapType, boolean bTraps) {
		//Define the map size, note that the third dimension is there for walls. With value 0 being the tile type (open, spawn, nest
		//or any kind of trap. Value 1, if that is set to one then the tile in question has a wall along its southern edge.
		//If Value 2 is one, then the tile has a wall along its eastern edge.   
		nMap = new int[nWidth][nHeight][3];

		//If the map is syemmtrical, launch the splice function.
		if (bSymmetry == true) {
			//If there is an error, report it and end function.
			if (!Splice(nWidth, nHeight, nMapType, nPlayers)) {
				JOptionPane.showMessageDialog(null, "Error: Bad map size");
				return false;
			}
		}
		//Call the function that places spawns and nests
		PlaceSpawnsNests(nMapType, nPlayers);
		//Create Paths depending on maptype
		//This randomizer is used to determine random edge cooridnates
		Random rRandom = new Random(System.currentTimeMillis());
		//Map Type 0
		if (nMapType == 0) {
			//Spawn to Nest
			CreatePath(1, 1, nSpliceX - 2, nSpliceY - 2, 1);
			CreatePath(1, 1, nSpliceX - 2, nSpliceY - 2, 2);
			//Edges
			int nCoord = -1; //Variable for storing random value
			//Make sure coord is reachible, by being greater then 1
			//South Edge
			nCoord = rRandom.nextInt(nSpliceX - 1) + 1;
			CreatePath(1, 1, nCoord, nSpliceY - 1, 2);
			//East Edge
			nCoord = rRandom.nextInt(nSpliceY - 1) + 1;
			CreatePath(1, 1, nSpliceX - 1, nCoord, 1);
		}
		//Map Type 1
		if (nMapType == 1) {
			//Spawn to Nest
			CreatePath(1, 1, nSpliceX - 2, nSpliceY - 2, 1);
			CreatePath(1, 1, nSpliceX - 2, nSpliceY - 2, 2);
			//Edges
			int nCoord = -1; //Variable for storing random value
			//Make sure coord is reachible, by being greater then 1
			//South Edge
			nCoord = rRandom.nextInt(nSpliceX - 1) + 1;
			CreatePath(nSpliceX - 2, nSpliceY - 2, nCoord, nSpliceY - 1, 2);
			//East Edge
			nCoord = rRandom.nextInt(nSpliceY - 1) + 1;
			CreatePath(nSpliceX - 2, nSpliceY - 2, nSpliceX - 1, nCoord, 1);
		}
		//Map Type 2 or 4
		if (nMapType == 2 || nMapType == 4) {
			//Spawn to Nest
			CreatePath(nSpliceX / 2, 1, nSpliceX / 2, nSpliceY - 2, 1);
			CreatePath(nSpliceX / 2, 1, nSpliceX / 2, nSpliceY - 2, 3);
			//Edges
			int nCoord = -1; //Variable for storing random value
			//Make sure coord is reachible, by being greater then 1
			//West Edge
			nCoord = rRandom.nextInt(nSpliceY - 1) + 1;
			CreatePath(nSpliceX / 2, 1, 0, nCoord, 3);
			//East Edge
			nCoord = rRandom.nextInt(nSpliceY - 1) + 1;
			CreatePath(nSpliceX / 2, 1, nSpliceX - 1, nCoord, 3);
		}
		//Map Type 3 or 5
		if (nMapType == 3 || nMapType == 5) {
			//Spawn to Nest
			CreatePath(1, nSpliceY / 2, nSpliceX - 2, nSpliceY / 2, 0);
			CreatePath(1, nSpliceY / 2, nSpliceX - 2, nSpliceY / 2, 2);
			//Edges
			int nCoord = -1; //Variable for storing random value
			//Make sure coord is reachible, by being greater then 1
			//West Edge
			nCoord = rRandom.nextInt(nSpliceX - 1) + 1;
			CreatePath(1, nSpliceX / 2, nCoord, 0, 0);
			//East Edge
			nCoord = rRandom.nextInt(nSpliceX - 1) + 1;
			CreatePath(1, nSpliceX / 2, nCoord, nSpliceY - 1, 2);
		}

		//Place extra walls on the map
		PlaceWalls();
		//Place traps on the map
		//PlaceTraps();
		//Last operation before map is done
		Mirror(nMapType, nWidth, nHeight, nPlayers);
		//Print the map to a file
		PrintMap(false, nWidth, nHeight);
		//Report that the map was successfully generated.
		return true;
	}

	////////////////////////////////////CREATE PATH////////////////////////////////////
	//Create a reserved path between two points. This path will never be blocked     //
	//nStartX/nStartY: The X and Y coordinates on the slice where the path will start//
	//nEndX/nEndY: The X and Y coordinates on the slice where path will end          //
	//nStartDir/nEndDir: The Direction the path will leave the start position through//
	//and the direction the path will enter the end position through                 //
	//A value of 0 in these means north, 1 east, 2 south and 3 west                  //
	///////////////////////////////////////////////////////////////////////////////////
	protected void CreatePath(int nStartX, int nStartY, int nEndX, int nEndY, int nStartDir) {
		//Variables
		int nChance = (nSpliceX + nSpliceY) / 2; //The odds of the path turning times the length it has been travelling without making a turn.
		int nOpenTurns = 3; //An open turn is a turn that the player needs to place an arrow for mice to take. No
		//more then three of these turns per path.
		int nTurns = 6; //The total amount of turns a given path may take.
		int nHeading = nStartDir; //The Direction the path is taking at the moment. 0=North 1=East 2=South 3=West
		int[] nCurrent = new int[2]; //The current X and Y coordinates the path is at.
		nCurrent[0] = nStartX;
		nCurrent[1] = nStartY; //Set the current position to the start coordinates.
		int[] nDest = new int[2]; //The next tile the path will end up at
		int nLength = 0; //How long the path is, determines the chance of a curve. 
		int nForbDir = -1;//Forbidden direction, the path will never take this.
		boolean bLocked = false; //If set to true, it will not turn away from its locked direction.  
		int nLockDir = -1; //The direction the path is locked in
		int nArrowTurns = 2; //How many arrow turns that the algorithm might make
		//Initiate the random number generator with system time as seed
		Random rRandom = new Random(System.currentTimeMillis());
		//Loop until the path has found its desitnation.
		//Calculate forbidden direction - Lest use it
		//   JOptionPane.showMessageDialog(null, "X" + nEndX + "Y" + nEndY);
		nForbDir = CalcDir(nStartX, nStartY, nEndX, nEndY);
		//Reverse forbidden direction
		switch (nForbDir) {
			case 0:
				nForbDir = 2;
				break;
			case 1:
				nForbDir = 3;
				break;
			case 2:
				nForbDir = 0;
				break;
			case 3:
				nForbDir = 1;
				break;
		}
		int inter = 0; //Debug variable
		while (nCurrent[0] != nEndX || nCurrent[1] != nEndY) //	for (int i = 0; i < 30; i++) //Debugloop
		{
			//To prevent craziness and infinite loops
			inter++;
			if (inter >= 60) {
				// Debug code, do not uncomment unless you know what you are doing!
				//	Mirror(0, 16, 12, 4);
				//	PrintMap(true,16, 12);
				//	JOptionPane.showMessageDialog(null, "Infinite loop error. Debug data: X: " + nEndX + " Y: " + nEndY + " CX: " + nCurrent[0] + " CY: "+nCurrent[1] + "SPLICE DATA: " + nSpliceX + " , "+ nSpliceY);
				return; //End function
			}
			//Check if the path needs to head directly to the end.
			//It will head directly to the end if its X cooridnate is equal to the ends if either
			//west or east is forbidden. If north or south is forbidden, it will head directly
			//to the end if the paths Y coordinate equals the ends.

			//Note, previously it could not be locked for this section to run, but it worked
			//better if it ran without being locked.

			if (nForbDir == 3 && nCurrent[0] == nEndX || nForbDir == 1 && nCurrent[0] == nEndX || nForbDir == 0 && nCurrent[1] == nEndY || nForbDir == 2 && nCurrent[1] == nEndY) {
				//Check Direction it needs to head
				//Locked is initally west/east
				if (nForbDir == 1 || nForbDir == 3) {
					if (nCurrent[1] < nEndY) {
						nLockDir = 2;
						nForbDir = 0;
					} //If it is above the end, head south. Forbidnorth
					else {
						nLockDir = 0;
						nForbDir = 2;
					} //If it is below the end, head north. Forbid south
				} else {
					if (nCurrent[0] < nEndX) {
						nLockDir = 1;
						nForbDir = 3;
					} //If it is west of the end, head east. Forbidwest
					else {
						nLockDir = 3;
						nForbDir = 1;
					} //If it is east of the end, head west. Forbid east
				}
				//Lock the path
				bLocked = true;
			}
			//Check possible turning directions and test if the path is blocked.
			int[] nBlocked = new int[4]; //Keeps track of blocked directions, 0 = north, 1 = east and so on.

			try {
				//Check if north is blocked
				if (nCurrent[1] - 1 == -1 || nSplice[nCurrent[0]][nCurrent[1] - 1][1] == 1 || nForbDir == 0) {
					nBlocked[0] = 1;
				}
				//Check if east is blocked
				if (nCurrent[0] + 1 == nSpliceX || nSplice[nCurrent[0]][nCurrent[1]][2] == 1 || nForbDir == 1) {
					nBlocked[1] = 1;
				}
				//Check if south is blocked
				if (nCurrent[1] + 1 == nSpliceY || nSplice[nCurrent[0]][nCurrent[1]][1] == 1 || nForbDir == 2) {
					nBlocked[2] = 1;
				}
				//Check if west is blocked
				if (nCurrent[0] - 1 == -1 || nSplice[nCurrent[0] - 1][nCurrent[1]][1] == 1 || nForbDir == 3) {
					nBlocked[3] = 1;
				}
//JOptionPane.showMessageDialog(null, "Turns" + nTurns + "Current X:" + nCurrent[0] + "Current Y:" + nCurrent[1] + "Heading" + nHeading + "Blocks:" + nBlocked[0] + "," + nBlocked[1] + "," + nBlocked[2] + "," + nBlocked[3]);	

			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "ERROR!");
				JOptionPane.showMessageDialog(null, "Turns" + nTurns + "Current X:" + nCurrent[0] + "Current Y:" + nCurrent[1] + "Heading" + nHeading + "Blocks:" + nBlocked[0] + "," + nBlocked[1] + "," + nBlocked[2] + "," + nBlocked[3]);
			} finally {
			}
			//Determine if its time for the path to curve or not





			//First, ensure that it has gotten enough curves left to do a regular curve
			//Turn if it is locked

			if (nBlocked[nHeading] == 1 || nTurns > 3 || bLocked == true && nHeading != nLockDir) {
				int nOrigDir = nHeading; //Save the original heading, for use with closed turns.
				//Randomze if it turns or not, if the path straightahead is blocked. Make a turn regardless of
				//the random test..
				int nRes = rRandom.nextInt(100);
				//Previusly the chance of a turn was fixed at 5% times the length the path had been
				//travelling without turning. Now, the chance is (SpliceX+SpliceY)/2.
				if (nBlocked[nHeading] == 1 || (nRes < nLength * nChance && nRes < 80) || bLocked == true && nHeading != nLockDir) {
					//Randomize the direction it takes, left or right.
					nRes = rRandom.nextInt(2);

					//Determine the direction the path will take
					//If all Possible directions are blocked, make a 360 degree turn
					if (nHeading == 0 && nBlocked[0] == 1 && nBlocked[1] == 1 && nBlocked[3] == 1) {
						nHeading = 2;
					} else if (nHeading == 1 && nBlocked[0] == 1 && nBlocked[1] == 1 && nBlocked[2] == 1) {
						nHeading = 3;
					} else if (nHeading == 2 && nBlocked[1] == 1 && nBlocked[2] == 1 && nBlocked[3] == 1) {
						nHeading = 0;
					} else if (nHeading == 3 && nBlocked[0] == 1 && nBlocked[2] == 1 && nBlocked[3] == 1) {
						nHeading = 1;
					} //If it is blocked, then no special measures are needed, just turn around. Otherwise, make
					//other turning checks. 
					else {
						//Determine which direction to head
						//If turning left or right is not possible, continue in initial direction
						//Path is originally heading north/south
						if (nHeading == 0 || nHeading == 2) {
							if (nRes == 0 && nBlocked[3] != 1) {
								nHeading = 3;
							} else if (nBlocked[1] != 1) {
								nHeading = 1;
							} else {
								nHeading = 3;
							}
						//	JOptionPane.showMessageDialog(null, "Turning East/West Original Direction:" + nOrigDir + "Heading:" + nHeading);
						//	JOptionPane.showMessageDialog(null, "Current X:" + nCurrent[0] + "Current Y:" + nCurrent[1] + "Heading:" + nHeading);	

						} //Path is originally heading east/west
						else if (nHeading == 1 || nHeading == 3) {
							if (nRes == 0 && nBlocked[0] != 1) {
								nHeading = 0;
							} else if (nBlocked[2] != 1) {
								nHeading = 2;
							} else {
								nHeading = 0;
							}
						//	JOptionPane.showMessageDialog(null, "Current X:" + nCurrent[0] + "Current Y:" + nCurrent[1] + "Heading:" + nHeading);	

						}
						//If it is headed in the initial direction, no future checks are needed
						if (nOrigDir != nHeading) {
							//Check if it can make a closed turn
							boolean bCanCTurn = false;
							//Check if a closed turn north is possible, its not possible if it needs to place
							//a wall that crosses a path.
							if (nHeading == 0) {
								if (nCurrent[1] + 1 == nSpliceY || nSplice[nCurrent[0]][nCurrent[1] + 1][0] != 1 && nSplice[nCurrent[0]][nCurrent[1] + 1][0] != 2 && nSplice[nCurrent[0]][nCurrent[1] + 1][0] != 3) {
									if (nCurrent[0] + 1 != nSpliceX && nSplice[nCurrent[0] + 1][nCurrent[1]][0] != 1 && nSplice[nCurrent[0] + 1][nCurrent[1]][0] != 2 && nSplice[nCurrent[0] + 1][nCurrent[1]][0] != 3 || nCurrent[0] - 1 != -1 && nSplice[nCurrent[0] - 1][nCurrent[1]][0] != 1 && nSplice[nCurrent[0] - 1][nCurrent[1]][0] != 2 && nSplice[nCurrent[0] - 1][nCurrent[1]][0] != 3) {
										bCanCTurn = true;
									}
								}
							}
							//Check if a closed turn east is possible, its not possible if it needs to place
							//a wall that crosses a path.
							if (nHeading == 1) {
								if (nCurrent[0] - 1 == -1 || nSplice[nCurrent[0] - 1][nCurrent[1]][0] != 1 && nSplice[nCurrent[0] - 1][nCurrent[1]][0] != 2 && nSplice[nCurrent[0] - 1][nCurrent[1]][0] != 3) {
									if (nCurrent[1] + 1 != nSpliceY && nSplice[nCurrent[0]][nCurrent[1] + 1][0] != 1 && nSplice[nCurrent[0]][nCurrent[1] + 1][0] != 2 && nSplice[nCurrent[0]][nCurrent[1] + 1][0] != 3 || nCurrent[1] - 1 != -1 && nSplice[nCurrent[0]][nCurrent[1] - 1][0] != 1 && nSplice[nCurrent[0]][nCurrent[1] - 1][0] != 2 && nSplice[nCurrent[0]][nCurrent[1] - 1][0] != 3) {
										bCanCTurn = true;
									}
								}
							}
							//Check if a closed turn south is possible, its not possible if it needs to place
							//a wall that crosses a path.
							if (nHeading == 2) {
								if (nCurrent[1] - 1 == -1 || nSplice[nCurrent[0]][nCurrent[1] - 1][0] != 1 && nSplice[nCurrent[0]][nCurrent[1] - 1][0] != 2 && nSplice[nCurrent[0]][nCurrent[1] - 1][0] != 3) {
									if (nCurrent[0] + 1 != nSpliceX && nSplice[nCurrent[0] + 1][nCurrent[1]][0] != 1 && nSplice[nCurrent[0] + 1][nCurrent[1]][0] != 2 && nSplice[nCurrent[0] + 1][nCurrent[1]][0] != 3 || nCurrent[0] - 1 != -1 && nSplice[nCurrent[0] - 1][nCurrent[1]][0] != 1 && nSplice[nCurrent[0] - 1][nCurrent[1]][0] != 2 && nSplice[nCurrent[0] - 1][nCurrent[1]][0] != 3) {
										bCanCTurn = true;
									}
								}
							}
							//Check if a closed turn west is possible, its not possible if it needs to place
							//a wall that crosses a path.
							if (nHeading == 3) {
								if (nCurrent[0] + 1 == nSpliceX || nSplice[nCurrent[0] + 1][nCurrent[1]][0] != 1 && nSplice[nCurrent[0] + 1][nCurrent[1]][0] != 2 && nSplice[nCurrent[0] + 1][nCurrent[1]][0] != 3) {
									if (nCurrent[1] + 1 != nSpliceY && nSplice[nCurrent[0]][nCurrent[1] + 1][0] != 1 && nSplice[nCurrent[0]][nCurrent[1] + 1][0] != 2 && nSplice[nCurrent[0]][nCurrent[1] + 1][0] != 3 || nCurrent[1] - 1 != -1 && nSplice[nCurrent[0]][nCurrent[1] - 1][0] != 1 && nSplice[nCurrent[0]][nCurrent[1] - 1][0] != 2 && nSplice[nCurrent[0]][nCurrent[1] - 1][0] != 3) {
										bCanCTurn = true;
									}
								}
							}
							//Randomize if it makes an open, arrow or closed turn.
							nRes = rRandom.nextInt(5);
							//If heading is blocked, make a closed turn if possible
							if (nBlocked[nOrigDir] == 1 && bCanCTurn == true) {
								nRes = 0;
							}
							//Closed Turn
							if (nRes <= 1 && bCanCTurn == true) {
								//JOptionPane.showMessageDialog(null, "Closed Turn!");

								//Create the closed turns
								//Prev heading east, now heading south.
								if (nOrigDir == 1 && nHeading == 2) {
									//Wall Above
									if (nCurrent[1] - 1 != -1) {
										nSplice[nCurrent[0]][nCurrent[1] - 1][1] = 1;
									}
									//Wall right
									if (nCurrent[0] + 1 != nSpliceX) {
										nSplice[nCurrent[0]][nCurrent[1]][2] = 1;
									}
								} //Prev heading east, now heading north.
								else if (nOrigDir == 1 && nHeading == 0) {
									//Wall Below
									if (nCurrent[1] + 1 != nSpliceY) {
										nSplice[nCurrent[0]][nCurrent[1]][1] = 1;
									}
									//Wall right
									if (nCurrent[0] + 1 != nSpliceX) {
										nSplice[nCurrent[0]][nCurrent[1]][2] = 1;
									}
								} //Prev heading south, now heading east.
								else if (nOrigDir == 2 && nHeading == 1) {
									//Wall Below
									if (nCurrent[1] + 1 != nSpliceY) {
										nSplice[nCurrent[0]][nCurrent[1]][1] = 1;
									}
									//Wall Left
									if (nCurrent[0] - 1 != -1) {
										nSplice[nCurrent[0] - 1][nCurrent[1]][2] = 1;
									}
								} //Prev heading south, now heading west.
								else if (nOrigDir == 2 && nHeading == 3) {
									//Wall Below
									if (nCurrent[1] + 1 != nSpliceY) {
										nSplice[nCurrent[0]][nCurrent[1]][1] = 1;
									}
									//Wall Right
									if (nCurrent[0] + 1 != nSpliceX) {
										nSplice[nCurrent[0]][nCurrent[1]][2] = 1;
									}
								} //Prev heading west, now heading south.
								else if (nOrigDir == 3 && nHeading == 2) {
									//Wall Above
									if (nCurrent[1] - 1 != -1) {
										nSplice[nCurrent[0]][nCurrent[1] - 1][1] = 1;
									}
									//Wall left
									if (nCurrent[0] - 1 != -1) {
										nSplice[nCurrent[0] - 1][nCurrent[1]][2] = 1;
									}
								} //Prev heading west, now heading north.
								else if (nOrigDir == 3 && nHeading == 0) {
									//Wall Below
									if (nCurrent[1] + 1 != nSpliceY) {
										nSplice[nCurrent[0]][nCurrent[1]][1] = 1;
									}
									//Wall left
									if (nCurrent[0] - 1 != -1) {
										nSplice[nCurrent[0] - 1][nCurrent[1]][2] = 1;
									}
								} //Prev heading north, now heading east.
								else if (nOrigDir == 0 && nHeading == 1) {
									//Wall Above
									if (nCurrent[1] - 1 != -1) {
										nSplice[nCurrent[0]][nCurrent[1] - 1][1] = 1;
									}
									//Wall Left
									if (nCurrent[0] - 1 != -1) {
										nSplice[nCurrent[0] - 1][nCurrent[1]][2] = 1;
									}
								} //Prev heading south, now heading west.
								else if (nOrigDir == 0 && nHeading == 3) {
									//Wall Above
									if (nCurrent[1] - 1 != -1) {
										nSplice[nCurrent[0]][nCurrent[1] - 1][1] = 1;
									}
									//Wall Right
									if (nCurrent[0] + 1 != nSpliceX) {
										nSplice[nCurrent[0]][nCurrent[1]][2] = 1;
									}
								}
								//Substract total turns.
								nTurns = nTurns - 1;
								nLength = 0;
							} //Arrow Turn
							else if (nArrowTurns != 0 && nCurrent[0] != 0 && nCurrent[1] != 0 && nCurrent[0] != nSpliceX - 1 && nCurrent[1] != nSpliceY - 1) //Do not place arrows along edges, that might lead to inaccessible areas
							{
								if (nRes == 2 && nSplice[nCurrent[0]][nCurrent[1]][0] == 0 || nOpenTurns == 0 && nSplice[nCurrent[0]][nCurrent[1]][0] == 0) {
									//Spawn arrow and substract total turns
									//North
									if (nHeading == 0) {
										nSplice[nCurrent[0]][nCurrent[1]][0] = 4;
									}
									//East
									if (nHeading == 1) {
										nSplice[nCurrent[0]][nCurrent[1]][0] = 5;
									}
									//South
									if (nHeading == 2) {
										nSplice[nCurrent[0]][nCurrent[1]][0] = 6;
									}
									//West
									if (nHeading == 3) {
										nSplice[nCurrent[0]][nCurrent[1]][0] = 7;
									}
									nTurns = nTurns - 1;
									nLength = 0;
									nArrowTurns = nArrowTurns - 1;
								}
							} //Open Turn
							else if (nBlocked[nOrigDir] == 1 || nOpenTurns > 0) {
								//Nothing special needed, just substract open and total turns.
								nOpenTurns = nOpenTurns - 1;
								nTurns = nTurns - 1;
								nLength = 0;
							} //Neither turn can be done, continue ahead
							else {
								//		JOptionPane.showMessageDialog(null, "Failed Turn!");

								nHeading = nOrigDir;
							}

						}
					}
				}
			}
			//Determine the next tile the path will end up at
			nLength++;
			//		 JOptionPane.showMessageDialog(null, "MOVE!");
			//Reserve the tile, if no special feature is on it.
			if (nSplice[nCurrent[0]][nCurrent[1]][0] == 0) {
				nSplice[nCurrent[0]][nCurrent[1]][0] = 1;
			}
			//Move North
			if (nHeading == 0) {
				nCurrent[1] = nCurrent[1] - 1;
			}
			//Move East 
			if (nHeading == 1) {
				nCurrent[0]++;
			}
			//Move South
			if (nHeading == 2) {
				nCurrent[1]++;
			}
			//Move West
			if (nHeading == 3) {
				nCurrent[0] = nCurrent[0] - 1;
			}
		}
	}

	///////////////////////////////////////////////// SPLICE MAP //////////////////////////////////////////////////////
	//This function divides the map into smaller parts, so it can be made symmetrical. See documentation for details.//
	//																																					  //
	//																																					  //
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	protected boolean Splice(int nWidth, int nHeight, int nMapType, int nPlayers) {
		//This function splices the map in two different ways depending on which map type, and how many players there are.
		//Six or more players get special treatment
		if (nPlayers >= 6) {
			nPlayers = 4;

		}

		//If map type is 0 or 1, the division happens in the middle of the map.
		if (nMapType == 0 || nMapType == 1) {
			//If the map is for only two players, it needs to be generated for four players.
			if (nPlayers == 2) {
				nPlayers = 4;
			} //If the map is for an odd amount of players, generete it as if it was for an even amount anyway.
			else if (nPlayers == 3 || nPlayers == 5) {
				nPlayers++;
			}

			//Divide the X dimension of the map by half the amount of players, and the Y dimension by 2.
			nSpliceX = nWidth / (nPlayers / 2);
			nSpliceY = nHeight / 2;
			nSplice = new int[nSpliceX][nSpliceY][3];

		} //If map Type is 2 or 4, slice the map into vertical rectangles.
		else if (nMapType == 2 || nMapType == 4) {
			nSpliceX = nWidth / (nPlayers);
			nSpliceY = nHeight;
			nSplice = new int[nSpliceX][nSpliceY][3];

		} //If map Type is 3 or 5, slice the map into horizontal rectangles.
		else if (nMapType == 3 || nMapType == 5) {
			nSpliceX = nWidth;
			nSpliceY = nHeight / (nPlayers);
			nSplice = new int[nSpliceX][nSpliceY][3];

		}

		return true;
	}
	////////////////////////////////////////////////
	//Place Spawns and Nests depending on map type//
	////////////////////////////////////////////////

	protected void PlaceSpawnsNests(int nMapType, int players) {
		//Place SPAWNS
		//Place Spawn in corner
		if (nMapType == 0) {
			nSplice[1][1][0] = 2;
		}
		//Place Spawn in middle
		if (nMapType == 1) {
			nSplice[nSpliceX - 2][nSpliceY - 2][0] = 2;
		}
		//Place Spawn along bottom
		if (nMapType == 2) {
			nSplice[nSpliceX / 2][nSpliceY - 2][0] = 2;
		}
		//Place Spawn along right edge
		if (nMapType == 3) {
			nSplice[nSpliceX - 2][nSpliceY / 2][0] = 2;
		}
		//Place Spawn along top edge
		if (nMapType == 4) {
			nSplice[nSpliceX / 2][11][0] = 2;
		}
		//Place Spawn along right edge
		if (nMapType == 5) {
			nSplice[nSpliceX - 2][nSpliceY / 2][0] = 2;
		}

		if (players < 6) {

			//Place NESTS
			//Place Nest in Middle
			if (nMapType == 0) {
				nSplice[nSpliceX - 2][nSpliceY - 2][0] = 3;
			}
			//Place Nest in Corner
			if (nMapType == 1) {
				nSplice[1][1][0] = 3;
			}
			//Place Nest along top edge
			if (nMapType == 2) {
				nSplice[nSpliceX / 2][1][0] = 3;
			}
			//Place Nest along left edge
			if (nMapType == 3) {
				nSplice[1][nSpliceY / 2][0] = 3;
			}
			//Place Nest along bottom edge
			if (nMapType == 4) {
				nSplice[nSpliceX / 2][nSpliceY][0] = 3;
			}
			//Place Nest along right edge
			if (nMapType == 5) {
				nSplice[nSpliceX - 2][nSpliceY / 2][0] = 3;
			}
		} else //More then 6 players
		{

			//Place NESTS
			//Place Nest in Middle and left
			if (nMapType == 0) {
				nSplice[nSpliceX - 2][nSpliceY - 2][0] = 3;
				nSplice[1][nSpliceY - 2][0] = 3;
			}
			//Place Nest in Corner and right
			if (nMapType == 1) {
				nSplice[1][1][0] = 3;
				nSplice[nSpliceX - 2][1][0] = 3;
			}
			//Place Nests along top edges
			if (nMapType == 2) {
				nSplice[nSpliceX / 3][1][0] = 3;
				nSplice[2 * (nSpliceX / 3)][1][0] = 3;
			}
			//Place Nests along left edge
			if (nMapType == 3) {
				nSplice[1][nSpliceY / 3][0] = 3;
				nSplice[1][2 * (nSpliceY / 3)][0] = 3;
			}
			//Place Nest along bottom edge
			if (nMapType == 4) {
				nSplice[nSpliceX / 3][nSpliceY - 2][0] = 3;
				nSplice[2 * (nSpliceX / 3)][nSpliceY - 2][0] = 3;
			}
			//Place Nest along right edge
			if (nMapType == 5) {
				nSplice[nSpliceX - 2][nSpliceY / 3][0] = 3;
				nSplice[nSpliceX - 2][2 * (nSpliceY / 3)][0] = 3;
			}
		}
	}
	////////////////////////////////////////////////////////////////////////////	
	//This function determines if a specific spot is valid for wall placement.//
	//To simplyfy other functions, nDir checks in all four directions and not //
	//just the two directions that each tile stores its walls in.             //  
	//If bWallBlocks is true, it will return false if a wall allready is in	  //	    
	//place at the location in question.													  //	
	////////////////////////////////////////////////////////////////////////////

	protected boolean CheckWall(int nX, int nY, int nDir, boolean bWallBlocks) {
		//Check northern wall
		if (nDir == 0) {
			//Check if positions is valid
			if (nY - 1 == -1 && bWallBlocks != true || nSplice[nX][nY][0] == 0 && nY - 1 != -1 && nSplice[nX][nY - 1][0] == 0 || nY - 1 != -1 && nSplice[nX][nY - 1][1] == 1 && bWallBlocks == false) {
				return true;
			}
		}
		//Check western wall
		if (nDir == 3) {
			//Check if positions is valid
			if (nX - 1 == -1 && bWallBlocks != true || nSplice[nX][nY][0] == 0 && nX - 1 != -1 && nSplice[nX - 1][nY][0] == 0 || nX - 1 != -1 && nSplice[nX - 1][nY][2] == 1 && bWallBlocks == false) {
				return true;
			}
		}
		//Check southern wall
		if (nDir == 2) {
			//Check if positions is valid
			if (nY + 1 == nSpliceY && bWallBlocks != true || nSplice[nX][nY][0] == 0 && nY + 1 != nSpliceY && nSplice[nX][nY + 1][0] == 0 || nY + 1 != nSpliceY && nSplice[nX][nY][1] == 1 && bWallBlocks == false) {
				return true;
			}
		}
		//Check eastern wall
		if (nDir == 1) {
			//Check if positions is valid
			if (nX + 1 == nSpliceX && bWallBlocks != true || nSplice[nX][nY][0] == 0 && nX + 1 != nSpliceX && nSplice[nX + 1][nY][0] == 0 || nX + 1 != nSpliceX && nSplice[nX][nY][2] == 1 && bWallBlocks == false) {
				return true;
			}
		}
		return false;
	}

	///////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////	
	//This function randomly places walls, see documentation for details.//
	///////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////
	protected void PlaceWalls() {
		//Variables
		Random rRandom = new Random(System.currentTimeMillis()); //Randomizer
		int nWalls = (nSpliceX + nSpliceY) / 2; //Amount of walls to be placed.
		int[] nCurrent = new int[2]; //Array containing current X and why coordinates
		int nDirection = -1; //Current Direction of wall. 0 is north, 1 east and so on.
		int nFacing = -1; //The walls facing
		//Randomize wall start position until you get a valid one
		while (nWalls != 0) {
			boolean bValid = false; //This one is set to true if a valid wall position is randomized.
			int nInt = 0; //Used to prevent infinite liips
			while (bValid == false) {

				if (nInt >= 50) {
					return; //To prevent ifinite loops
				}				//Select a tile randomly
				nCurrent[0] = rRandom.nextInt(nSpliceX); //Start X
				nCurrent[1] = rRandom.nextInt(nSpliceY); //Start Y
				nFacing = rRandom.nextInt(4); //Initial Facing
				bValid = CheckWall(nCurrent[0], nCurrent[1], nFacing, true);
				nInt++;
			}
			nWalls = nWalls - 1;
			//Set the current direction
			//Randomize direction
			nDirection = rRandom.nextInt(2);
			//Set the direction depending on wall facing

			//Wall facing is North/South
			if (nFacing == 0 || nFacing == 2) {
				if (nDirection == 0) {
					nDirection = 3; //Direction is west
				} else {
					nDirection = 1; //Direction is east
				}
			} //Wall facing is West/East
			else {
				if (nDirection == 0) {
					nDirection = 0; //Direction is noth
				} else {
					nDirection = 2; //Direction is south
				}
			}
			boolean bBlocked = false; //Sets to true if the wall gets blocked.
			//Loop until wall gets blocked
			while (bBlocked != true) {
				//Place wall
				//Wall faces north
				if (nFacing == 0) {
					nSplice[nCurrent[0]][nCurrent[1] - 1][1] = 1;
				}
				//Wall faces east
				if (nFacing == 1) {
					nSplice[nCurrent[0]][nCurrent[1]][2] = 1;
				}
				//Wall faces south
				if (nFacing == 2) {
					nSplice[nCurrent[0]][nCurrent[1]][1] = 1;
				}
				//Wall faces west
				if (nFacing == 3) {
					nSplice[nCurrent[0] - 1][nCurrent[1]][2] = 1;
				}

				//Wall turning and premature ending code here. Might not be needed

				//////
				//Move wall depending on direction
				//Move north
				if (nDirection == 0) {
					nCurrent[1] = nCurrent[1] - 1;
				}
				//Move east
				if (nDirection == 1) {
					nCurrent[0] = nCurrent[0] + 1;
				}
				//Move South
				if (nDirection == 2) {
					nCurrent[1] = nCurrent[1] + 1;
				}
				//Move west
				if (nDirection == 3) {
					nCurrent[0] = nCurrent[0] - 1;
				}
				//Check if new position is blocked, if it is blocked the loop shall end 
				//			JOptionPane.showMessageDialog(null, nCurrent[0] + " "+ nCurrent[1]);
				if (nCurrent[0] < 0 || nCurrent[1] < 0 || nCurrent[0] >= nSpliceX || nCurrent[1] >= nSpliceY || CheckWall(nCurrent[0], nCurrent[1], nFacing, true) == false) {
					bBlocked = true;
				}
			}
		}
	}
	///////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////	
	//This function randomly places traps, see documentation for details.//
	///////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////

	protected void PlaceTraps() {
		Random rRandom = new Random(System.currentTimeMillis()); //Randomizer
		//Go through each tile
		//Y Axis
		for (int i = 0; i < nSpliceY; i++) {
			//X axis
			for (int j = 0; j < nSpliceX; j++) {
				//Make sure its not reserved and open
				if (nSplice[j][i][0] == 0) {
					//Generate a random number between 0-99
					int nRandom = rRandom.nextInt(100);
					//Determine chance of there being a trap there
					int nChance = 8; //Base chance is 2%
					//Check for adjecant traps. No traps can be found to the east and south
					//due to the method the algorithm places traps
					if (i - 1 != -1 && nSplice[j][i - 1][0] > 3 //North
									|| i - 1 != -1 && j + 1 != nSpliceX && nSplice[j + 1][i - 1][0] > 3 //Northeast
									|| j - 1 != -1 && nSplice[j - 1][i][0] > 3 //West
									|| i - 1 != -1 && j - 1 != -1 && nSplice[j - 1][i - 1][0] > 3) //Northwest
					{
						nChance = nChance + 18; //Increase trapchance by 18
					}					//Check for adjecant walls
					boolean[] bWalled = new boolean[4]; //Stores if each direction is walled.
					//Check if Directions is walled
					if (i - 1 == -1 || nSplice[j][i - 1][1] == 1) {
						bWalled[0] = true; //North
					}
					if (i + 1 == nSpliceY || nSplice[j][i][1] == 1) {
						bWalled[2] = true; //South
					}
					if (j - 1 == -1 || nSplice[j - 1][i][2] == 2) {
						bWalled[3] = true; //West
					}
					if (j + 1 == nSpliceX || nSplice[j][i][2] == 2) {
						bWalled[1] = true; //East
					}					//If walls are on opposing sides, increase odds of trap placement.
					if (bWalled[0] == true && bWalled[2] == true || bWalled[1] == true && bWalled[3] == true) //Increase Chance
					{
						nChance = nChance + 18;
					}
					//Place trap if random chance is low enough
					if (nRandom < nChance) {
						//Algorithm for placing differnt types of traps here, curent only supports one type.
						//Blackhole
						nSplice[j][i][0] = 8;
					//				JOptionPane.showMessageDialog(null, "Trap placed"); //Debugmessage
					}
				}
			}
		}
	}
	////////////////////////////////////////////////
	//Calculate Dominant direction towards a point//
	//Only used in the Create Path function.      //
	//Returns: 0=North,1=East,2=South,3=West      //
	////////////////////////////////////////////////

	protected int CalcDir(int nStartX, int nStartY, int nEndX, int nEndY) {
		//Calculate the dominant Directon
		int nXvalue = nStartX - nEndX;//Calculate the horizontal distance to target
		int nYvalue = nStartY - nEndY;//Calculate the vertical distance to target
		int nXPos = 0;
		if (nXvalue > 0) {
			nXPos = 1;//Is Xvalue positive?
		}
		int nYPos = 0;
		if (nYvalue > 0) {
			nYPos = 1;//Is Yvalue positive?
		}
		if (nXvalue < 0) {
			nXvalue = nXvalue * -1; //Make Xvalue positive
		}
		if (nYvalue < 0) {
			nYvalue = nYvalue * -1; //Make Xvalue positive
		}		//Determine dominant direction
		if (nXvalue >= nYvalue && nXPos == 1) {
			return 3; //West is dominant
		} else if (nXvalue >= nYvalue && nXPos == 0) {
			return 1; //East is dominant
		} else if (nXvalue < nYvalue && nYPos == 0) {
			return 2; //South is dominant
		} else {
			return 0; //North is dominant.
		}
	}
	////////////////////////////////////////////////
	//Finalize the map by mirroring each splice   //
	//This function exists to make sure that      //
	//the map is symetrical.                      //
	////////////////////////////////////////////////

	protected void Mirror(int nMapType, int nMapWidth, int nMapHeight, int nPlayers) {
		//If 6 or more players
		if (nPlayers >= 6) {
			nPlayers = 4;
		}
		//If map type is 0 or 1, we might have to fudge the nPlayers integer a little.
		if (nMapType == 0 || nMapType == 1) {
			//If the map is for only two players, it needs to be mirrored for four players.
			if (nPlayers == 2) {
				nPlayers = 4;
			} //If the map is for an odd amount of players, mirror it as if it was for an even amount anyway.
			else if (nPlayers == 3 || nPlayers == 5) {
				nPlayers++;
			}

			//This Variable determines how many players there are that are not inverted on the X axis, this is one. Two if there
			//are six or eight players.
			int nUninvertedX = 1;
			if (nPlayers == 6 || nPlayers == 8) {
				nUninvertedX = 2;
			}
			//This Variable determines how many players there are that are inverted on the X axis, this is one. Two if there
			//are eight players.
			int nInvertedX = 1;
			if (nPlayers == 8) {
				nInvertedX = 2;
			}
			//Place the slices univerted along the the Y AND X axis
			for (int a = 0; a < nUninvertedX; a++) {
				//Place the SPLICE!
				for (int i = 0; i < nSpliceY; i++) {
					for (int j = 0; j < nSpliceX; j++) {
						nMap[j + (a * nSpliceX)][i][0] = nSplice[j][i][0];
						nMap[j + (a * nSpliceX)][i][1] = nSplice[j][i][1];
						nMap[j + (a * nSpliceX)][i][2] = nSplice[j][i][2];
					}
				}
			}
			//Place the slices Inverted along the the X axis
			for (int a = 0 + nUninvertedX; a < (nInvertedX + nUninvertedX); a++) {
				//Place the SPLICE!
				for (int i = 0; i < nSpliceY; i++) {
					for (int j = 0; j < nSpliceX; j++) {
						//Invert arrows
						if (nSplice[(nSpliceX - j) - 1][i][0] == 5) {
							nMap[j + (a * nSpliceX)][i][0] = 7;
						} else if (nSplice[(nSpliceX - j) - 1][i][0] == 7) {
							nMap[j + (a * nSpliceX)][i][0] = 5;
						} //No arrows
						else {
							nMap[j + (a * nSpliceX)][i][0] = nSplice[(nSpliceX - j) - 1][i][0];
						}
						nMap[j + (a * nSpliceX)][i][1] = nSplice[(nSpliceX - j) - 1][i][1];
						nMap[j + (a * nSpliceX) - 1][i][2] = nSplice[(nSpliceX - j) - 1][i][2];
					}
				}
			}
			//Copy the top of the map to the bottom, and invert it.
			for (int i = 0; i < nSpliceY; i++) {
				for (int j = 0; j < nMapWidth; j++) {
					//Invert arrows
					if (nMap[j][(nSpliceY - i) - 1][0] == 4) {
						nMap[j][i + nSpliceY][0] = 6;
					} else if (nMap[j][(nSpliceY - i) - 1][0] == 6) {
						nMap[j][i + nSpliceY][0] = 4;
					} //No arrows
					else {
						nMap[j][i + nSpliceY][0] = nMap[j][(nSpliceY - i) - 1][0];
					}
					nMap[j][i + nSpliceY - 1][1] = nMap[j][(nSpliceY - i) - 1][1];
					nMap[j][i + nSpliceY][2] = nMap[j][(nSpliceY - i) - 1][2];
				}
			}

		} //If the maptype is 2,3,4 or 5. Its a simple matter of copying each splice.
		//If the maptype is 2 or 4, it needs to be handled vertically.
		else if (nMapType == 2 || nMapType == 4) {
			//Each splice might needs to be copied once for each player.
			for (int p = 0; p < nPlayers; p++) {
				//Copy the splice 
				for (int i = 0; i < nSpliceY; i++) {
					for (int j = 0; j < nSpliceX; j++) {
						nMap[j + (nSpliceX * p)][i][0] = nSplice[j][i][0];
						nMap[j + (nSpliceX * p)][i][1] = nSplice[j][i][1];
						nMap[j + (nSpliceX * p)][i][2] = nSplice[j][i][2];
					}
				}
			}
		} //If the maptype is 3 or 5, it needs to be handled horizontially.
		else if (nMapType == 3 || nMapType == 5) {
			//Each splice might needs to be copied once for each player.
			for (int p = 0; p < nPlayers; p++) {
				//Copy the splice
				for (int i = 0; i < nSpliceY; i++) {
					for (int j = 0; j < nSpliceX; j++) {
						nMap[j][i + (nSpliceY * p)][0] = nSplice[j][i][0];
						nMap[j][i + (nSpliceY * p)][1] = nSplice[j][i][1];
						nMap[j][i + (nSpliceY * p)][2] = nSplice[j][i][2];
					}
				}
			}
		}
	}
	////////////////////////////////////////////////
	//Print the map to file                       //
	////////////////////////////////////////////////

	protected void PrintMap(boolean bDebug, int nMapWidth, int nMapHeight) {
		String[] sOutput = new String[nMapHeight * 2];
		//If the bDebug is true, print the map in an easy to read format
		if (bDebug == true) {
			//For each Y of the map
			for (int i = 0; i < nMapHeight; i++) {
				//Double the printing integer
				int y = i * 2;
				//Set previous values to nothing, to prevent printing of NULL
				sOutput[y] = "";
				sOutput[y + 1] = "";
				for (int j = 0; j < nMapWidth; j++) {

					//Analyze each tile and print it to a string
					int nTileType = nMap[j][i][0];
					switch (nTileType) {
						case 0:
							sOutput[y] = sOutput[y] + ",";
							break;
						case 1:
							sOutput[y] = sOutput[y] + ",";
							break;
						case 2:
							sOutput[y] = sOutput[y] + "S";
							break;
						case 3:
							sOutput[y] = sOutput[y] + "N";
							break;
						default:
							sOutput[y] = sOutput[y] + "T";
							break;
					}
					//Print Vertical Walls
					if (nMap[j][i][2] == 1) {
						sOutput[y] = sOutput[y] + "@";
					} else {
						sOutput[y] = sOutput[y] + " ";
					}
					//Print Horizontal Walls
					if (nMap[j][i][1] == 1) {
						sOutput[y + 1] = sOutput[y + 1] + "@ ";
					} else {
						sOutput[y + 1] = sOutput[y + 1] + "  ";
					}

				}
			}
			//Print the Actual map to a file.
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter("DebugMap.txt"));
				//Print each variable in the sOutput array into its own line.
				for (int i = 0; i < nMapHeight * 2; i++) {
					out.write(sOutput[i]);
					out.write("\r\n");
				}
				out.close();
			} catch (IOException e) {
			}

		} //
		// Store the map in a format usable by the game.
		else {
			//Create the sorted tile list to be used.
			SortedTileList stlMap = new SortedTileList();
			//Create and save each tile
			for (int i = 0; i < nMapHeight; i++) {
				for (int j = 0; j < nMapWidth; j++) {
					//Store walls
					boolean[] bWalls = new boolean[4];
					//Check walls
					//North
					if (i == 0 || nMap[j][i - 1][1] == 1) {
						bWalls[0] = true;
					}
					//East
					if (j == nMapWidth - 1 || nMap[j][i][2] == 1) {
						bWalls[1] = true;
					}
					//South
					if (i == nMapHeight - 1 || nMap[j][i][1] == 1) {
						bWalls[2] = true;
					}
					//West
					if (j == 0 || nMap[j - 1][i][2] == 1) {
						bWalls[3] = true;
					}
					//Create the tile
					//The difference between reserved paths and open
					//floors is none when the game is running
					int nTileType = nMap[j][i][0];
					if (nTileType == 0) {
						nTileType++;
					}
					//Add the tile to the tile list
					stlMap.add(Tile.createTile(j, i, bWalls[3], bWalls[1], bWalls[0], bWalls[2], nTileType));
				}
			}
			//Save the level
			Level.SaveLevel("the-random", stlMap, RandomString(), "Assets/Textures/SkyplaneSky.png"); //Define the level class.
		}
	}

	protected String RandomString() {
		try {

			FileReader fr = new FileReader("Assets/Misc/Strings.ran");
			BufferedReader br = new BufferedReader(fr);
			String sFinalLine = ""; //The final line that will be returned.
			ArrayList<String> lStrings = new ArrayList<String>(); //Used to store strngs for the current stage, used for
			//final randomization of the stirngs.
			String sLine = br.readLine(); //Currentline
			Random rRandom = new Random(System.currentTimeMillis());
			for (int nStage = 0; nStage < 3; nStage++)//0 = Mulox, 1 = Verb, 2 = Canx
			{
				//System.out.println("Random Stage " + nStage);
				lStrings.clear(); //Empty the list
				while ((sLine = br.readLine()) != null && !sLine.substring(0, 1).equals("#")) {
					//System.out.println("Line Read: " + sLine);
					lStrings.add(sLine); //Add the current line to the list
				}
				sFinalLine = sFinalLine + lStrings.get(rRandom.nextInt(lStrings.size())) + " ";
			}
			return sFinalLine;
		} catch (IOException e) {
			// catch possible io errors from readLine()
			e.printStackTrace();
		}
		return "Error";
	}
}