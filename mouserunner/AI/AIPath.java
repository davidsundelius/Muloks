package mouserunner.AI;

import java.util.LinkedList;

/**
 * This class is used to store a specifc path, as well as perform calculations
 * on it.
 * @author Pierre
 */
public class AIPath {
	//The Cooridnates each arrow needs to be placed at

	LinkedList<Integer> nTurnX = new LinkedList<Integer>();
	LinkedList<Integer> nTurnY = new LinkedList<Integer>();
	LinkedList<Integer> nTurnDir = new LinkedList<Integer>(); //The direction the turn takes.
	//X and Y cooridnates of each step along a path. Step 0 is the origin, step 1
	//is the first tile that the path crosses after it leave the origin and so on
	LinkedList<Integer> nStepX = new LinkedList<Integer>();
	LinkedList<Integer> nStepY = new LinkedList<Integer>();
	LinkedList<Integer> nStepFacing = new LinkedList<Integer>();
	int nTurnsTotal;
	//Total length of path
	int nTotalLength;
}
//Used to open square for pathfinding

class Position {

	int X;
	int Y;
	int ParentX;
	int ParentY;
	int ParentArrowDir = -1; //The direction a parent arrow points, is -1 if
	//no ParentArrow exists
	int Heading;
	int Cost; //Actual cost to reach this point
	int EstDist; //Estimated cost to reach the end point
	//Estimate distance to end square, used for pathfinding only

	protected void calcCost(int nBase, int nEndX, int nEndY) {
		//The actual cost
		Cost = nBase;
		//This changes the estimated distance to the target
		int nTempVal = nEndX - X;
		if (nTempVal < 0) {
			nTempVal = nTempVal * -1;
		}
		EstDist = nTempVal;
		nTempVal = nEndY - Y;
		if (nTempVal < 0) {
			nTempVal = nTempVal * -1;
		}
		EstDist = EstDist + nTempVal + nBase;
	}
}

        
   