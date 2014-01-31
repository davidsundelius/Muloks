package mouserunner.AI;

import java.util.ArrayList;
import mouserunner.Game.*;
import mouserunner.System.Timer;

/**
 * Used for methods and data storage about the AIs.
 * @author Pierre Andersson
 */
public class AIMind {

	private Player owner; //Stores which player is the owner of this mind.
	private ArrayList<EntityRow> weightedRows = new ArrayList<EntityRow>();
	int NestX = -1;
	int NestY = -1;
	//Next action, 0 = attract mice, 1 = defend, 2 = attack, -1 is no action
	int nextAction = -1;
	//Defense X,Y and Direction
	int defenseX;
	int defenseY;
	int defenseDir;
	//The difference in score between now and last update cycle
	protected int scoreChange = 0;
	protected int lastScore = 0;
	//Next Path to place arrows on
	AIPath nextPath;
	//This timer counts if it is time to rethink for the AI.
	Timer rethinkClock = new Timer();
	//How many MS the AI needs to wait before replaing its arrows
	int rethinkTime = 3000;
	ArrayList<AIPath> currentPaths = new ArrayList<AIPath>();

	//Constructor
	public AIMind(Player pOwner) {
		owner = pOwner;
	}

	//Forget the the current weighted stacks
	public void forgetRows() {
		weightedRows.clear();
		nextAction = -1;
	}

	//Sets the nest coordinates
	public void updateNestPosition(int X, int Y) {
		NestX = X;
		NestY = Y;
	}

	//Add a stack to its weighted stacks
	public void addWeightedRow(EntityRow rowToAdd) {
		weightedRows.add(rowToAdd);
	}

	//Sets the stack list
	public void setWeightedRowList(ArrayList<EntityRow> listToAdd) {
		weightedRows = listToAdd;
	}

	//Getter fir the stack list
	public ArrayList<EntityRow> getRows() {
		return weightedRows;
	}

	//Getter for the owner
	public Player getOwner() {
		return owner;
	}

	//Setter for the owner
	public void setOwner(Player newOwner) {
		owner = newOwner;
	}
}
