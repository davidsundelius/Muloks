package mouserunner.Game;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import mouserunner.System.Viewable;
import javax.media.opengl.GL;
import java.util.ArrayList;
import java.util.LinkedList;
import mouserunner.Managers.GameplayManager;
import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;
import mouserunner.LevelComponents.BlackHole;
import mouserunner.LevelComponents.Cattrap;
import mouserunner.LevelComponents.EmptyTile;
import mouserunner.LevelComponents.Glue;
import mouserunner.LevelComponents.Mousetrap;
import mouserunner.LevelComponents.Nest;
import mouserunner.LevelComponents.Portal;
import mouserunner.LevelComponents.SpawnPoint;
import mouserunner.LevelComponents.Tile;
import mouserunner.System.Direction;

/**
 * A playfield or level for the game. The level contains tiles of different sorts.
 * Everything is draw automaticliy by calling view()
 * @author Erik
 */
public class Level implements Viewable, Serializable {

	public static final float tileSize = 10.0f;
	/** The width of the field (counted in number of tiles) */
	public final int width;
	/** The height of the field (counted in number of tiles) */
	public final int height;	//Keeps track of the tiles
	private SortedTileList tiles;
	//The introtext
	public final String introText;
	//The skyplane texture path
	public final String skyplaneTexPath;
	// A list of spawn points
	private Point[] spawnPoints;

	public Level(SortedTileList tiles, String introText, String skyplaneTexPath) {
		this.tiles = tiles;
		this.introText = introText;
		this.skyplaneTexPath = skyplaneTexPath;

		width = tiles.width;
		height = tiles.height;

		LinkedList<Point> spawns = new LinkedList<Point>();
		// Loop through the tiles and search for spawn points
		for (Tile tile : tiles) {
			if (tile.getClass() == SpawnPoint.class) {
				spawns.add(new Point(tile.x, tile.y));
			}
		}
		this.spawnPoints = spawns.toArray(new Point[0]);
	}

	// <editor-fold defaultstate="collapsed" desc=" Loading and Saving ">
	/**
	 * This method loads the level with the given file name. The file name should be the level of the file or the path to
	 * the Levels in the Assets folder (Assets/Levels/). The file should have the file extension .lvl or no file extension
	 * 
	 * The returned level is not initiated. Before using the level is has to be initiated by
	 * calling {@link #initiate(Timer)} with the timer used by the rest of the game (what Timer is given is not checked
	 * but for the game to work properly it has to be the gameTimer).
	 * 
	 * P.S. Wrong file extension should probably be blocked, I'll implementit some time.
	 * @param mapFile The file name
	 * @return The uninitiated loaded level
	 * @throws java.io.FileNotFoundException
	 */
	public static Level LoadLevel(String mapFile) throws FileNotFoundException {
		try {
			if (!mapFile.startsWith("Assets/Levels/")) {
				mapFile = "Assets/Levels/" + mapFile;
			}
			if (!mapFile.endsWith(".lvl")) {
				mapFile += ".lvl";
			}
			FileInputStream fis = new FileInputStream(new File(mapFile));
			ObjectInputStream in = new ObjectInputStream(fis);

			Level level = (Level) in.readObject();
			in.close();
			fis.close();

			return level;
		} catch (InvalidClassException ex) {
			// If the level was of an incompatible type the ascii version is loaded and stored
			String asciiName = mapFile.substring(14, mapFile.length() - 4);
			try {
				System.out.println("Level " + mapFile + " has incompatible level version. Reconstructing from ascii version");
				Level lv = Level.LoadAsciiLevel("Assets/AsciiLevels/" + asciiName + ".mrlvl");
				Level.SaveLevel(mapFile, lv.tiles, lv.introText, lv.skyplaneTexPath);
				return lv;
			} catch (FileNotFoundException ex2) {
				System.out.println("Level tried to recreate " + mapFile + " from an ascii copy but no copy was found");
				System.exit(1);
			}
		} catch (IOException ex) {
			// If the level was of an incompatible type the ascii version is loaded and stored
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			// If the level was of an incompatible type the ascii version is loaded and stored
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * Saves a level to the given file.
	 * @param fileName The name if the level. Can be only level name or the reletive path.
	 * @param tiles The level
	 * @param introText The intro text
	 * @param skyplaneTexPath the path to the sky plane texture
	 * @return did the file save properly?
	 */
	public static boolean SaveLevel(String fileName, SortedTileList tiles, String introText, String skyplaneTexPath) {
		if (fileName.equals("")) {
			throw new IllegalArgumentException("Bad file name");
		}

		Level level = new Level(tiles, introText, skyplaneTexPath);

		// If the class attempting to save the level only passed the file name, the reletive URL is added
		if (!fileName.startsWith("Assets/Levels/")) {
			fileName = "Assets/Levels/" + fileName;		// If the level has no file extension, one is added
		}
		if (!fileName.endsWith(".lvl")) {
			fileName += ".lvl";		// Saves an ascii copy of the Level.
		}
		String asciiName = fileName.substring(14, fileName.length() - 4);
		Level.SaveAsciiLevel(level, "Assets/AsciiLevels/" + asciiName + ".mrlvl");

		try {
			FileOutputStream fos = new FileOutputStream(new File(fileName));
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(level);
			out.close();
			fos.close();
			return true;
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
			return false;
		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}
	}

	/**
	 * This method saves the given Level in a text based format. This method assums a 16x12 Level
	 * @param level The Level to be saved
	 * @param fileName The name of the file where the level goes
	 */
	public static void SaveAsciiLevel(Level level, String fileName) {
		// <editor-fold defaultstate="collapsed" desc=" Setup the tile array ">
		int rows = 12, cols = 16;
		int[][] asciiLevel = new int[2 * rows + 1][2 * cols + 1];

		// init the array
		for (int r = 0; r <= 2 * rows; r++) {
			for (int c = 0; c <= 2 * cols; c++) {
				asciiLevel[r][c] = -1;
			}
		}
		Tile currentTile;
		char tileType;
		/*
		 * Tile types (ints) [not in use]:
		 *  0 - EmptyTile
		 *  1 - EmptyTile w/ left arrow
		 *  2 - EmptyTile w/ right arrow
		 *  3 - EmptyTile w/ up arrow
		 *  4 - EmptyTile w/ down arrow
		 *  5 - Nest
		 *  6 - Spawn Point
		 *  7 - no wall
		 *  8 - wall
		 *  9 - Black hole
		 * 10 - Cattrap
		 * 11 - Mousetrap
		 * 12 - Glue
		 * 13 - Portal
		 */
		int x = 0, y = 0;
		for (int r = 1; r <= 2 * rows; r += 2) {
			for (int c = 1; c <= 2 * cols; c += 2) {
				currentTile = level.getTile(x++, y);
				// <editor-fold defaultstate="collapsed" desc="Det. level type">
				Class tileClass = currentTile.getClass();
				if (tileClass == EmptyTile.class) {
					if (((EmptyTile) currentTile).hasArrow()) {
						Direction dir = ((EmptyTile) currentTile).getArrowDirection();
						if (dir == Direction.LEFT) {
							tileType = 1;
						} else if (dir == Direction.RIGHT) {
							tileType = 2;
						} else if (dir == Direction.UP) {
							tileType = 3;
						} else {
							tileType = 4;
						}
					} else { // if the tile has no arrow
						tileType = 0;
					}
				} else if (tileClass == Nest.class) {
					tileType = 5;
				} else if (tileClass == SpawnPoint.class) {
					tileType = 6;
				} else if (tileClass == BlackHole.class) {
					tileType = 9;
				} else if (tileClass == Cattrap.class) {
					tileType = 10;
				} else if (tileClass == Mousetrap.class) {
					tileType = 11;
				} else if (tileClass == Glue.class) {
					tileType = 12;
				} else if (tileClass == Portal.class) {
					tileType = 13;
				} else { // if the tile is a spawn point
					throw new IllegalArgumentException("That tile clas was not found " + tileClass);
				}
				// </editor-fold>
				asciiLevel[r][c] = tileType;
				// <editor-fold defaultstate="collapsed" desc="Sets current tile walls">
				// Left wall
				if (currentTile.hasWall(Direction.LEFT)) {
					asciiLevel[r][c - 1] = 8;
				} else {
					asciiLevel[r][c - 1] = 7;				// Right wall
				}
				if (currentTile.hasWall(Direction.RIGHT)) {
					asciiLevel[r][c + 1] = 8;
				} else {
					asciiLevel[r][c + 1] = 7;				// Up wall
				}
				if (currentTile.hasWall(Direction.UP)) {
					asciiLevel[r - 1][c] = 8;
				} else {
					asciiLevel[r - 1][c] = 7;				// Down wall
				}
				if (currentTile.hasWall(Direction.DOWN)) {
					asciiLevel[r + 1][c] = 8;
				} else {
					asciiLevel[r + 1][c] = 7;
					// </editor-fold>
				}
			} // cols loop
			x = 0;
			y++;
		} // rows loop
		// </editor-fold>
		
		// <editor-fold defaultstate="collapsed" desc=" Converts portal bindings to ascii version ">
		// Convert the Portal bindings to an ascii friendly version
		String portalBindings = "";

		Iterator<Tile> portalIt = level.tiles.iterator();
		Tile t;
		Portal p, partner;
		LinkedList<Portal> addedPortals = new LinkedList<Portal>();
		while (portalIt.hasNext()) {
			t = portalIt.next();
			if (t.getClass() == Portal.class) {
				p = (Portal) t;
				if (!addedPortals.contains(p)) {
					partner = p.getParnter();
					// The format is x1 y1 x2 y2
					portalBindings += p.x + " " + p.y + " " + partner.x + " " + partner.y + " ";
					addedPortals.add(p);
					addedPortals.add(partner);
				}
			}
		}

		// </editor-fold>
		
		// <editor-fold defaultstate="collapsed" desc=" Writes the ascii level to file ">
		try {
			if (!fileName.startsWith("Assets/AsciiLevels/")) {
				fileName = "Assets/AsciiLevels/" + fileName;
			}
			if (!fileName.endsWith(".mrlvl")) {
				fileName += ".mrlvl";
			}
			FileWriter fw = new FileWriter(new File(fileName));
			PrintWriter pw = new PrintWriter(fw);
			pw.println(level.introText);
			pw.println(level.skyplaneTexPath);
			pw.println(portalBindings);
			for (int r = 0; r <= 2 * rows; r++) {
				for (int c = 0; c <= 2 * cols; c++) {
					if (asciiLevel[r][c] == -1) {
						pw.print("-1");
					} else {
						pw.print(asciiLevel[r][c]);
					}
					pw.print(" ");
					pw.flush();
				}
				pw.println();
			}
			pw.close();
			fw.close();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		// </editor-fold>
	}

	/**
	 * Loads a level from an file. The method is currently not protected agains badly formated
	 * files
	 * @param fileName the name if the file. The name should be only the name of the file or the name including 
	 * the path Assets/AsciiLevels/ and/or the file xtension .mrlvl. If another path is given terrible things till happen
	 * @return the loaded level
	 * @throws java.io.FileNotFoundException
	 */
	public static Level LoadAsciiLevel(String fileName) throws FileNotFoundException {
		int rows = 12;
		int cols = 16;

		if (!fileName.startsWith("Assets/AsciiLevels/")) {
			fileName = "Assets/AsciiLevels/" + fileName;
		}
		if (!fileName.endsWith(".mrlvl")) {
			fileName += ".mrlvl";
		}
		// <editor-fold defaultstate="collapsed" desc=" Ascii array ">
		Scanner sc = new Scanner(new File(fileName));
		int[][] asciiLevel = new int[2 * rows + 1][2 * cols + 1];
		String introText = sc.nextLine();
		String skyPlane = sc.nextLine();
		String poralBindingsString = sc.nextLine();
		Scanner lineSc;
		int r = 0;
		int c = 0;
		// Loops through the level, line-by-line
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			lineSc = new Scanner(line);
			//System.out.println(line);
			while (lineSc.hasNext()) {
				asciiLevel[r][c] = lineSc.nextInt();
				c++;
			}
			c = 0;
			r++;
			lineSc.close();
		}
		sc.close();

		// </editor-fold>

		// <editor-fold defaultstate="collapsed" desc=" Set up the tiles ">
		SortedTileList tiles = new SortedTileList();
		Tile currentTile = null;
		int x = 0, y = 0;
		for (r = 1; r < 2 * rows; r++) {
			for (c = 1; c < 2 * cols; c++) {
				// <editor-fold defaultstate="collapsed" desc=" Select tile type ">
				if (asciiLevel[r][c] != -1 && asciiLevel[r][c] != 7 && asciiLevel[r][c] != 8) {
					if (asciiLevel[r][c] == 0) { // if empty tile
						currentTile = new EmptyTile(x, y, false, false, false, false);
					} else if (asciiLevel[r][c] == 1) { // if empty tile with left arrow
						currentTile = new EmptyTile(x, y, false, false, false, false);
						((EmptyTile) currentTile).setArrow(Direction.LEFT);
					} else if (asciiLevel[r][c] == 2) { // if empty tile with right arrow
						currentTile = new EmptyTile(x, y, false, false, false, false);
						((EmptyTile) currentTile).setArrow(Direction.RIGHT);
					} else if (asciiLevel[r][c] == 3) { // if empty tile with up arrow
						currentTile = new EmptyTile(x, y, false, false, false, false);
						((EmptyTile) currentTile).setArrow(Direction.UP);
					} else if (asciiLevel[r][c] == 4) { // if empty tile with down arrow
						currentTile = new EmptyTile(x, y, false, false, false, false);
						((EmptyTile) currentTile).setArrow(Direction.DOWN);
					} else if (asciiLevel[r][c] == 5) { // if nest
						currentTile = new Nest(x, y, false, false, false, false);
					} else if (asciiLevel[r][c] == 6) { // if spawn point
						currentTile = new SpawnPoint(x, y, false, false, false, false);
					} else if (asciiLevel[r][c] == 9) { // if black hole
						currentTile = new BlackHole(x, y, false, false, false, false);
					} else if (asciiLevel[r][c] == 10) { // if Cattrap
						currentTile = new Cattrap(x, y, false, false, false, false);
					} else if (asciiLevel[r][c] == 11) { // if Mousetrap
						currentTile = new Mousetrap(x, y, false, false, false, false);
					} else if (asciiLevel[r][c] == 12) { // if Glue
						currentTile = new Glue(x, y, false, false, false, false);
					} else if (asciiLevel[r][c] == 13) { // if Portal
						currentTile = new Portal(x, y, false, false, false, false);
					}
					// </editor-fold>

					// <editor-fold defaultstate="collapsed" desc="Set walls">
					if (asciiLevel[r][c] != -1) {
						// Check left wall
						if (asciiLevel[r][c - 1] == 8) {
							currentTile.setWall(Direction.LEFT);
						}
						// Check right wall
						if (asciiLevel[r][c + 1] == 8) {
							currentTile.setWall(Direction.RIGHT);
						}
						// Check up wall
						if (asciiLevel[r - 1][c] == 8) {
							currentTile.setWall(Direction.UP);
						}
						// Check down wall
						if (asciiLevel[r + 1][c] == 8) {
							currentTile.setWall(Direction.DOWN);
						}
					}
					// </editor-fold>

					if (currentTile != null) {
						tiles.add(currentTile);
					}
					x++;
					if (x > 15) {
						x = 0;
						y++;
					}
				}
			}
		}
		
		// Portal bindings
		Scanner portalSc = new Scanner(poralBindingsString);
		int x1,x2,y1,y2;
		try {
			while(portalSc.hasNext()) {
				x1 = portalSc.nextInt();
				y1 = portalSc.nextInt();
				x2 = portalSc.nextInt();
				y2 = portalSc.nextInt();
				
				if(tiles.get(x1, y1).getClass() == Portal.class) {
					if(tiles.get(x1, y1).getClass() == Portal.class) {
						// if the portal is not already connected
						if(!((Portal)tiles.get(x1, y1)).isConnected()) {
							((Portal)tiles.get(x1, y1)).connect((Portal)tiles.get(x2, y2));
						}
					} else {
						System.out.println("Level: Error while loading ascii level. The given point (" + x1 + "," + y1 + ") was not a Portal");
					} // if the second position is not a portal
				} else { // if the first position is not a portal
					System.out.println("Level: Error while loading ascii level. The given point (" + x1 + "," + y1 + ") was not a Portal");
				}
			}
		} catch(NoSuchElementException ex) {
			System.out.println("Level: Error while loading ascii level. The number of portal binding variables was wrong. All unconnected portals are converted to EmptyTiles");
			Iterator<Tile> portalIt = tiles.iterator();
			Tile t;
			while(portalIt.hasNext()) {
				t = portalIt.next();
				if(t.getClass() == Portal.class) {
					if(!((Portal)t).isConnected()) {
						tiles.replace(t.x, t.y, new EmptyTile(t.x, t.y, t.hasWall(Direction.LEFT), t.hasWall(Direction.RIGHT), t.hasWall(Direction.UP), t.hasWall(Direction.DOWN)));
					}
				}
			}
		}

// </editor-fold>

		return new Level(tiles, introText, skyPlane);
	}

// </editor-fold>
	public String getSkyplaneTexPath() {
		return skyplaneTexPath;
	}

	public ArrayList<Arrow> getArrows() {
		ArrayList<Arrow> al = new ArrayList<Arrow>();

		for (Tile t : tiles) {
			if (t instanceof EmptyTile) {
				al.add(((EmptyTile) t).getArrow());
			}
		}
		return al;
	}

	/**
	 * Returns an array of the spawn point in the Level.
	 * @return An array of Spawn Points
	 */
	public Point[] getSpawnPoints() {
		return spawnPoints;
	}

	/**
	 * Returns a list of all portals on the level (used to spawn portal effects)
	 * @return an ArrayList of portals
	 */
	public ArrayList<Portal> getPortals() {
		ArrayList<Portal> portals = new ArrayList<Portal>();
		for (Tile t : tiles) {
			if (t.getClass()== Portal.class) {
				portals.add((Portal)t);
			}
		}
		return portals;
	}

	/**
	 * Returns a list of all nests on the level
	 * @return an ArrayList of nests
	 */
	public ArrayList<Nest> getNests() {
		ArrayList<Nest> al = new ArrayList<Nest>();

		for (Tile t : tiles) {
			if (t.getClass() == Nest.class) {
				al.add((Nest) t);
			}
		}
		return al;
	}

	public Nest getLeaderNest() {
		for (Nest n : getNests()) {
			if (n.getOwner() == GameplayManager.getInstance().players.get(0)) {
				return n;
			}
		}
		return null;
	}

	public void removeNest(Nest n) {
		tiles.replace(n.x, n.y, new EmptyTile(n.x, n.y, n.hasWall(Direction.LEFT), n.hasWall(Direction.RIGHT), n.hasWall(Direction.UP), n.hasWall(Direction.DOWN)));
	}

	/**
	 * This method finds the tile at a specific position (x,y)
	 * and return a refrence to it.
	 * @return a refrence to a tile on the playfield
	 */
	public Tile getTile(int x, int y) {
		return tiles.get(x, y);
	}

	@Override
	public void view(GL gl) {
		gl.glEnable(GL.GL_TEXTURE_2D);
		for (Tile t : tiles) {
			gl.glPushMatrix();
			gl.glTranslatef(t.x * Level.tileSize, -t.y * Level.tileSize, 0.0f);
			t.view(gl);
			gl.glPopMatrix();
		}
	}

	public void viewPicking(GL gl) {
		for (Tile t : tiles) {
			int x = t.x;
			int y = t.y;
			gl.glLoadName(x + (y * 16));
			gl.glPushMatrix();
			gl.glTranslatef(x * Level.tileSize, -y * Level.tileSize, 0.0f);
			t.viewTile(gl);
			gl.glPopMatrix();
		}
	}

	/**
	 * Recreates all levels from their ascii copys. Only levels that have ascii copys is recreated
	 */
	public static void recreateLevels() {
		String[] list = new File("Assets/AsciiLevels").list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".mrlvl");
			}
		});

		for (String n : list) {
			try {
				System.out.println("Level recreated: " + n);
				Level l = Level.LoadAsciiLevel("Assets/AsciiLevels/" + n);
				Level.SaveLevel("Assets/Levels/" + n.replace(".mrlvl", ".lvl"), l.tiles, l.introText, l.skyplaneTexPath);
			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
			}
		}
	}
}
