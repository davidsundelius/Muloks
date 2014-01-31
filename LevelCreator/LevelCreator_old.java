package LevelCreator;

import mouserunner.Game.Level;
import mouserunner.Game.SortedTileList;
import mouserunner.LevelComponents.SpawnPoint;
import mouserunner.LevelComponents.EmptyTile;
import mouserunner.LevelComponents.Nest;
import mouserunner.LevelComponents.Tile;
import java.awt.Point;
import mouserunner.LevelComponents.BlackHole;
import mouserunner.LevelComponents.Cattrap;
import mouserunner.LevelComponents.Glue;
import mouserunner.LevelComponents.Mousetrap;
import mouserunner.LevelComponents.Portal;
import mouserunner.LevelComponents.Trap;
import mouserunner.System.Direction;

/**
 * This class is only ment for creating a few static levels for testing and
 * debugging. When the real LevelCreator / LevelGenerator is implemented
 * it will replace this class, probably
 * @author Erik
 */
public class LevelCreator_old {

	private SortedTileList level;
	private String introText;
	private Point[] spawns;

	public LevelCreator_old() {
		level = new SortedTileList();
	}

	public void saveLevel(String filename) {
		Level.SaveLevel(filename, level, introText, "Assets/Textures/SkyplaneSky.png");
	}

	/**
	 * This method loads the level creator with a level. These levels are hand written
	 * inside of createLevel and are select by there internal index x
	 * 0 - Level from hasselmus
	 * 1 - Empty field
	 * 2 - A new level!
	 * 3 - A level with permanent arrows
	 * 4 - A level with spawn points
	 * 5 - A level with 2 spawns and 1 nest
	 * 6 - A very simple level for debugging gameplay with 4 players
	 * 
	 * @param x the internat level index
	 */
	public void createLevel(int x) {
		level.clear();
		switch (x) {
			case 0:
				introText = "The First - One map to rule them all";
				spawns = new Point[0];
				for (int i = 0; i < 16; i++) {
					for (int j = 0; j < 12; j++) {
						if (i == 0 && j == 0) {
							level.add(new EmptyTile(i, j, true, false, true, false));
						} else if (i == 0 && j == 11) {
							level.add(new EmptyTile(i, j, true, false, false, true));
						} else if (i == 0 && j != 0) {
							level.add(new EmptyTile(i, j, true, false, false, false));
						} else if (i == 15 && j == 0) {
							level.add(new EmptyTile(i, j, false, true, true, false));
						} else if (i != 0 && j == 0) {
							level.add(new EmptyTile(i, j, false, false, true, false));
						} else if (i == 15 && j == 11) {
							level.add(new EmptyTile(i, j, false, true, false, true));
						} else if (i == 15 && j != 11) {
							level.add(new EmptyTile(i, j, false, true, false, false));
						} else if (i != 0 && j == 11) {
							level.add(new EmptyTile(i, j, false, false, false, true));
						// Walls in the map
						} else if (i == 8 && j == 2) {
							level.add(new EmptyTile(i, j, false, true, false, false));
						} else if (i == 9 && j == 2) {
							level.add(new EmptyTile(i, j, true, false, false, false));
						} else if (i == 8 && j == 4) {
							level.add(new EmptyTile(i, j, false, false, false, true));
						} else if (i == 8 && j == 5) {
							level.add(new EmptyTile(i, j, false, false, true, false));
						} else if (i == 6 && j == 4) {
							level.add(new EmptyTile(i, j, true, false, false, false));
						} else if (i == 5 && j == 4) {
							level.add(new EmptyTile(i, j, false, true, false, false));
						} else {
							level.add(new EmptyTile(i, j, false, false, false, false));
						}
					}
				}
				break;
			case 1:
				introText = "The Empty - Nothing here...Wait is that? MICE AAAH!";
				spawns = new Point[0];
				for (int i = 0; i < 16; i++) {
					for (int j = 0; j < 12; j++) {
						if (i == 0 && j == 0) {
							level.add(new EmptyTile(i, j, true, false, true, false));
						} else if (i == 0 && j == 11) {
							level.add(new EmptyTile(i, j, true, false, false, true));
						} else if (i == 0 && j != 0) {
							level.add(new EmptyTile(i, j, true, false, false, false));
						} else if (i == 15 && j == 0) {
							level.add(new EmptyTile(i, j, false, true, true, false));
						} else if (i != 0 && j == 0) {
							level.add(new EmptyTile(i, j, false, false, true, false));
						} else if (i == 15 && j == 11) {
							level.add(new EmptyTile(i, j, false, true, false, true));
						} else if (i == 15 && j != 11) {
							level.add(new EmptyTile(i, j, false, true, false, false));
						} else if (i != 0 && j == 11) {
							level.add(new EmptyTile(i, j, false, false, false, true));
						} else {
							level.add(new EmptyTile(i, j, false, false, false, false));
						}
					}
				}
				break;
			case 2:
				introText = "The New - Hasselmus called and it wants its maps back...";
				spawns = new Point[0];
				for (int i = 0; i < 16; i++) {
					for (int j = 0; j < 12; j++) {
						if (i == 0 && j == 0) {
							level.add(new EmptyTile(i, j, true, false, true, false));
						} else if (i == 0 && j == 11) {
							level.add(new EmptyTile(i, j, true, false, false, true));
						} else if (i == 0 && j != 0) {
							level.add(new EmptyTile(i, j, true, false, false, false));
						} else if (i == 15 && j == 0) {
							level.add(new EmptyTile(i, j, false, true, true, false));
						} else if (i != 0 && j == 0) {
							level.add(new EmptyTile(i, j, false, false, true, false));
						} else if (i == 15 && j == 11) {
							level.add(new EmptyTile(i, j, false, true, false, true));
						} else if (i == 15 && j != 11) {
							level.add(new EmptyTile(i, j, false, true, false, false));
						} else if (i != 0 && j == 11) {
							level.add(new EmptyTile(i, j, false, false, false, true));
						} else if (i == 3 && j <= 5) {
							level.add(new EmptyTile(i, j, true, false, false, false));
						} else if (i == 2 && j <= 5) {
							level.add(new EmptyTile(i, j, false, true, false, false));
						} else if (i == 6 && j >= 4) {
							level.add(new EmptyTile(i, j, false, true, false, false));
						} else if (i == 7 && j >= 4) {
							level.add(new EmptyTile(i, j, true, false, false, false));
						} else if (i == 12 && j <= 5) {
							level.add(new EmptyTile(i, j, false, true, false, false));
						} else if (i == 13 && j <= 5) {
							level.add(new EmptyTile(i, j, true, false, false, false));
						} else {
							level.add(new EmptyTile(i, j, false, false, false, false));
						}
					}
				}
				level.get(6, 11).setWall(Direction.RIGHT);
				level.get(7, 11).setWall(Direction.LEFT);
				level.get(2, 0).setWall(Direction.RIGHT);
				level.get(3, 0).setWall(Direction.LEFT);
				level.get(12, 0).setWall(Direction.RIGHT);
				level.get(13, 0).setWall(Direction.LEFT);
                                
				break;
			case 3:
				introText = "The Permanent - An arrow is so much more than an almond?";
				spawns = new Point[0];
				for (int i = 0; i < 16; i++) {
					for (int j = 0; j < 12; j++) {
						if (i == 0 && j == 0) {
							level.add(new EmptyTile(i, j, true, false, true, false));
						} else if (i == 0 && j == 11) {
							level.add(new EmptyTile(i, j, true, false, false, true));
						} else if (i == 0 && j != 0) {
							level.add(new EmptyTile(i, j, true, false, false, false));
						} else if (i == 15 && j == 0) {
							level.add(new EmptyTile(i, j, false, true, true, false));
						} else if (i != 0 && j == 0) {
							level.add(new EmptyTile(i, j, false, false, true, false));
						} else if (i == 15 && j == 11) {
							level.add(new EmptyTile(i, j, false, true, false, true));
						} else if (i == 15 && j != 11) {
							level.add(new EmptyTile(i, j, false, true, false, false));
						} else if (i != 0 && j == 11) {
							level.add(new EmptyTile(i, j, false, false, false, true));
						} else {
							level.add(new EmptyTile(i, j, false, false, false, false));
						}
					}
				}
				((EmptyTile) level.get(3, 0)).setArrow(Direction.DOWN);
				((EmptyTile) level.get(3, 5)).setArrow(Direction.RIGHT);
				((EmptyTile) level.get(5, 5)).setArrow(Direction.UP);
				((EmptyTile) level.get(10, 0)).setArrow(Direction.DOWN);
				break;
			case 4:
				introText = "The Spawn - 99 spawning rockets FLOATing in the summer sky...?";
				spawns = new Point[1];
				spawns[0] = new Point(2, 7);
				for (int i = 0; i < 16; i++) {
					for (int j = 0; j < 12; j++) {
						if (i == 0 && j == 0) {
							level.add(new EmptyTile(i, j, true, false, true, false));
						} else if (i == 0 && j == 11) {
							level.add(new EmptyTile(i, j, true, false, false, true));
						} else if (i == 2 && j == 7) {
							level.add(new SpawnPoint(i, j, false, false, false, false));
						} else if (i == 0 && j != 0) {
							level.add(new EmptyTile(i, j, true, false, false, false));
						} else if (i == 15 && j == 0) {
							level.add(new EmptyTile(i, j, false, true, true, false));
						} else if (i != 0 && j == 0) {
							level.add(new EmptyTile(i, j, false, false, true, false));
						} else if (i == 15 && j == 11) {
							level.add(new EmptyTile(i, j, false, true, false, true));
						} else if (i == 15 && j != 11) {
							level.add(new EmptyTile(i, j, false, true, false, false));
						} else if (i != 0 && j == 11) {
							level.add(new EmptyTile(i, j, false, false, false, true));
						} else {
							level.add(new EmptyTile(i, j, false, false, false, false));
						}
					}
				}
				break;
			case 5:
				introText = "The Nest - Home sweet cat infested home!";
				spawns = new Point[2];
				spawns[0] = new Point(2, 7);
				spawns[1] = new Point(2, 2);
				for (int i = 0; i < 16; i++) {
					for (int j = 0; j < 12; j++) {
						if (i == 0 && j == 0) {
							level.add(new EmptyTile(i, j, true, false, true, false));
						} else if (i == 0 && j == 11) {
							level.add(new EmptyTile(i, j, true, false, false, true));
						} else if (i == 8 && j == 7) {
							level.add(new Nest(i, j, false, false, false, false));
						} else if (i == 2 && j == 7) {
							level.add(new SpawnPoint(i, j, false, false, false, false));
						} else if (i == 2 && j == 2) {
							level.add(new SpawnPoint(i, j, false, false, false, false));
						} else if (i == 0 && j != 0) {
							level.add(new EmptyTile(i, j, true, false, false, false));
						} else if (i == 15 && j == 0) {
							level.add(new EmptyTile(i, j, false, true, true, false));
						} else if (i != 0 && j == 0) {
							level.add(new EmptyTile(i, j, false, false, true, false));
						} else if (i == 15 && j == 11) {
							level.add(new EmptyTile(i, j, false, true, false, true));
						} else if (i == 15 && j != 11) {
							level.add(new EmptyTile(i, j, false, true, false, false));
						} else if (i != 0 && j == 11) {
							level.add(new EmptyTile(i, j, false, false, false, true));
						} else {
							level.add(new EmptyTile(i, j, false, false, false, false));
						}
					}
				}
				break;
			case 6:
				introText = "The Simple - IIS (It's So Simple) Mouse trap";
				spawns = new Point[2];
				spawns[0] = new Point(6, 6);
				spawns[1] = new Point(9, 6);
				for (int i = 0; i < 16; i++) {
					for (int j = 0; j < 12; j++) {
						if (i == 0 && j == 0) {
							level.add(new Nest(i, j, true, false, true, false));
						} else if (i == 0 && j == 11) {
							level.add(new Nest(i, j, true, false, false, true));
						} else if (i == 15 && j == 11) {
							level.add(new Nest(i, j, false, true, false, true));
						} else if (i == 15 && j == 0) {
							level.add(new Nest(i, j, false, true, true, false));
						} else if (i == 6 && j == 6) {
							level.add(new SpawnPoint(i, j, false, false, false, false));
						} else if (i == 9 && j == 6) {
							level.add(new SpawnPoint(i, j, false, false, false, false));
						} else if (i == 0 && j != 0) {
							level.add(new EmptyTile(i, j, true, false, false, false));
						} else if (i != 0 && j == 0) {
							level.add(new EmptyTile(i, j, false, false, true, false));
						} else if (i == 15 && j != 11) {
							level.add(new EmptyTile(i, j, false, true, false, false));
						} else if (i != 0 && j == 11) {
							level.add(new EmptyTile(i, j, false, false, false, true));
						} else {
							level.add(new EmptyTile(i, j, false, false, false, false));
						}
					}
				}
				break;
			case 7:
				introText = "The create test - testing create";
				for (int i = 0; i < 16; i++) {
					for (int j = 0; j < 12; j++) {
						if (i == 0 && j == 0) {
							level.add(Tile.createTile(i, j, true, false, true, false, 3));
						} else if (i == 0 && j == 11) {
							level.add(Tile.createTile(i, j, true, false, false, true, 3));
						} else if (i == 15 && j == 11) {
							level.add(Tile.createTile(i, j, false, true, false, true, 3));
						} else if (i == 15 && j == 0) {
							level.add(Tile.createTile(i, j, false, true, true, false, 3));
						} else if (i == 6 && j == 6) {
							level.add(Tile.createTile(i, j, false, false, false, false, 2));
						} else if (i == 9 && j == 6) {
							level.add(Tile.createTile(i, j, false, false, false, false, 2));
						} else if (i == 9 && j == 8) {
							level.add(Tile.createTile(i, j, false, false, false, false, 6));
						} else if (i == 12 && j == 6) {
							level.add(Tile.createTile(i, j, false, false, false, false, 7));
						} else if (i == 0 && j != 0) {
							level.add(new EmptyTile(i, j, true, false, false, false));
						} else if (i != 0 && j == 0) {
							level.add(new EmptyTile(i, j, false, false, true, false));
						} else if (i == 15 && j != 11) {
							level.add(new EmptyTile(i, j, false, true, false, false));
						} else if (i != 0 && j == 11) {
							level.add(new EmptyTile(i, j, false, false, false, true));
						} else {
							level.add(new EmptyTile(i, j, false, false, false, false));
						}
					}
				}
				break;
			case 8:
				introText = "The traps";
				for (int i = 0; i < 16; i++) {
					for (int j = 0; j < 12; j++) {
						if (i == 0 && j == 0) {
							level.add(Tile.createTile(i, j, true, false, true, false, 3));
						} else if (i == 0 && j == 11) {
							level.add(Tile.createTile(i, j, true, false, false, true, 3));
						} else if (i == 15 && j == 11) {
							level.add(Tile.createTile(i, j, false, true, false, true, 3));
						} else if (i == 15 && j == 0) {
							level.add(Tile.createTile(i, j, false, true, true, false, 3));
						} else if (i == 6 && j == 6) {
							level.add(Tile.createTile(i, j, false, false, false, false, 2));
						} else if (i == 9 && j == 6) {
							level.add(Tile.createTile(i, j, false, false, false, false, 2));
						} else if (i == 9 && j == 8) {
							level.add(Tile.createTile(i, j, false, false, false, false, 6));
						} else if (i == 12 && j == 6) {
							level.add(Tile.createTile(i, j, false, false, false, false, 7));
						} else if (i == 0 && j != 0) {
							level.add(new EmptyTile(i, j, true, false, false, false));
						} else if (i != 0 && j == 0) {
							level.add(new EmptyTile(i, j, false, false, true, false));
						} else if (i == 15 && j != 11) {
							level.add(new EmptyTile(i, j, false, true, false, false));
						} else if (i != 0 && j == 11) {
							level.add(new EmptyTile(i, j, false, false, false, true));
						} else if(i == 5 && j == 5) {
							level.add(new BlackHole(i, j, false, false, false, false));
						} else if(i == 8 && j == 8) {
							level.add(new Cattrap(i, j, false, false, false, false));
						} else if(i == 1 && j == 1) {
							level.add(new Portal(i, j, false, false, false, false));
						} else if(i == 14 && j == 10) {
							level.add(new Portal(i, j, false, false, false, false));
						} else if(i == 1 && j == 10) {
							level.add(new Glue(i, j, false, false, false, false));
						}else {
							level.add(new EmptyTile(i, j, false, false, false, false));
						}						
					}
				}
				((Portal)level.get(1, 1)).connect((Portal)level.get(14, 10));
				break;
			default:
				throw new IndexOutOfBoundsException("There is no predefined level with this index");
		}

	}

	/*
	 * Run this class as standalone to generate 4 maps
	 */
	public static void main(String[] args) {
		LevelCreator_old lc = new LevelCreator_old();

		lc.createLevel(0);
		lc.saveLevel("Assets/Levels/the-first.lvl");
		System.out.println("Added map!");
		lc.createLevel(1);
		lc.saveLevel("Assets/Levels/the-empty.lvl");
		System.out.println("Added map!");
		lc.createLevel(2);
		lc.saveLevel("Assets/Levels/the-new.lvl");
		System.out.println("Added map!");
		lc.createLevel(3);
		lc.saveLevel("Assets/Levels/the-permanent.lvl");
		System.out.println("Added map!");
		lc.createLevel(4);
		lc.saveLevel("Assets/Levels/the-spawn.lvl");
		System.out.println("Added map!");
		lc.createLevel(5);
		lc.saveLevel("Assets/Levels/the-nest.lvl");
		System.out.println("Added map!");
		lc.createLevel(6);
		lc.saveLevel("Assets/Levels/the-simple.lvl");
		System.out.println("Added map!");
		lc.createLevel(7);
		lc.saveLevel("Assets/Levels/the-tilecreate.lvl");
		System.out.println("Added map!");
		lc.createLevel(8);
		lc.saveLevel("Assets/Levels/the-trap.lvl");
		System.out.println("Added map!");
	}
}
