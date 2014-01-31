package mouserunner.Game;

import mouserunner.LevelComponents.Tile;
import java.io.Serializable;
import java.util.*;
import mouserunner.LevelComponents.EmptyTile;
import mouserunner.LevelComponents.Nest;


/**
 * This class is a collection of tiles structured as an two dimensional array.
 * The iterator of this class assumes that the Collection is filled, i.e. it will return
 * null references if iterating before filling the entire Collection
 * @author Erik
 */
public class SortedTileList extends AbstractCollection<Tile> implements Serializable {

	private Tile[][] array;
	private int size;
	
	public final int width,height;

	public SortedTileList() {
		this(16,12);
	}
	
	/**
	 * Creates a tile collection with the given dimensions
	 * @param cols The given number of columns
	 * @param rows The given number of rows
	 */
	public SortedTileList(int cols, int rows) {
		this.array = new Tile[rows][cols];
		this.width = cols;
		this.height = rows;
	}

	@Override
	public boolean add(Tile tile) {
		// If there already are a tile at the given position
		if(array[tile.y][tile.x] != null) {
			return false;
		}
		
		array[tile.y][tile.x] = tile;
		size++;

		return true;
	}

	public Tile get(int c, int r) {
		return array[r][c];
	}
	
	/**
	 * Replaceds the tile at the given coordinates with the given {@link mouserunner.LevelComponents.Tile}
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @param tile the Tile that replaces the old tile
	 */
	public void replace(int x, int y, Tile tile) {
		array[y][x] = tile;
	}

	@Override
	public Iterator<Tile> iterator() {
		return new SortedTileListIterator();
	}

	@Override
	public void clear() {
		array = new Tile[height][width];
		size = 0;
	}

	@Override
	public int size() {
		return this.size;
	}
	
	class SortedTileListIterator implements Iterator<Tile> {

		private int atCol, atRow, iteratedItems;

		public SortedTileListIterator() {
			this.atCol = 0;
			this.atRow = 0;
			this.iteratedItems = 0;
		}

		@Override
		public boolean hasNext() {
			if(atRow == (height - 1) && atCol >= width) {
				return false;
			}
			
			return true;
		}

		@Override
		public Tile next() {
			if(!(atCol < width)) {
				atCol = 0;
				atRow++;
			}
			
			iteratedItems++;
			return array[atRow][atCol++];
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("A tile can not be removed from the playing field");
		}
	}
	
	
	public static void main(String[] args) {
		SortedTileList stl = new SortedTileList(3, 2);
		System.out.println("Starts STL test");
		System.out.println("Fills the first row");
		stl.add(new EmptyTile(0, 0, false, false, false, false));
		stl.add(new EmptyTile(1, 0, false, false, false, false));
		stl.add(new EmptyTile(2, 0, false, false, false, false));
		
		System.out.println("Tries to add at an existing tile");
		if(stl.add(new EmptyTile(2, 0, false, false, false, false))) {
			System.out.println("[Error] Could still add");
		} else {
			System.out.println("[Correct] Was not added");
		}
		
		stl.add(new Nest(0, 1, false, false, false, false));
		stl.add(new EmptyTile(1, 1, false, false, false, false));
		stl.add(new EmptyTile(2, 1, false, false, false, false));
		
		System.out.println("The size should be 6, it is " + stl.size());
		
		System.out.println("Iterating and printing all class types");
		Iterator<Tile> it = stl.iterator();
		Tile t;
		while(it.hasNext()) {
			t = it.next();
			System.out.println("Class = " + t.getClass());
		}
	}
}