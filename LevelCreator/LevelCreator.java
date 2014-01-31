package LevelCreator;

// <editor-fold defaultstate="collapsed" desc=" Imports ">
import java.awt.Component;
import java.awt.Graphics;
import java.util.logging.Logger;
import javax.swing.UnsupportedLookAndFeelException;
import mouserunner.Game.Level;
import mouserunner.Game.SortedTileList;
import mouserunner.LevelComponents.Tile;
import mouserunner.LevelComponents.SpawnPoint;
import mouserunner.LevelComponents.EmptyTile;
import mouserunner.LevelComponents.Nest;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import mouserunner.LevelComponents.BlackHole;
import mouserunner.LevelComponents.Cattrap;
import mouserunner.LevelComponents.Glue;
import mouserunner.LevelComponents.Mousetrap;
import mouserunner.LevelComponents.Portal;
import mouserunner.System.Direction;
// </editor-fold>

/**
 * This is the Level editor or level creator.
 * @author Erik
 */
public class LevelCreator extends JFrame implements ActionListener, MouseListener, KeyListener {
	/** The number of portals allowed on the map. */
	private static final int maxNumberOfPortals = 16;
	
	// <editor-fold defaultstate="collapsed" desc=" Definitions ">
	// All JLabel arrays are defined here because they will be used with action performed and such
	// They array which names end with Tiles are used in the panel, trapTiles are the trap icons and so on
	// Remember to change the length of the array if you change the number of tiles
	private JLabel[] panelTiles = new JLabel[3];
	private JLabel[] trapTiles = new JLabel[5];
	private JLabel[] permanentArrowTiles = new JLabel[4];
	private JLabel[] portalBindingTiles = new JLabel[1];
	// Used with the current tile type indicator, see setCurrentTile(tiletype).
	private TileType currentTileType;
	// This is the actual level.
	private JLabel[][] labelGrid;
	// This probably doesn't need to be defined here (could be done in the constructor)
	private JLevelPanel levelPanel;
	// The size of the level. Note that other parts of the code asumes a 16x12 level size, so don't change this
	private int cols = 16,  rows = 12;
	private boolean levelIsModified;
	// Used with save, to see if the level has been saved already
	private String savedName;
	private String introText;	
	
	// Portal binding
	// True if we are in portal binding mode
	private boolean portalBindingMode;
	private int numberOfPortals;
	private JLabel selectedPortal;
	private TreeMap<Integer, JLabel[]> bindedPortals;
	private static LinkedList<Integer> portalIDPool;
	
	// Scaled images
	Image emptyTile, nest, spawn, upArrow, downArrow, leftArrow, rightArrow, blackHole, catTrap, mouseTrap, glue, portal;
	private JLabel currentTileLabel;
	// </editor-fold>

	/**
	 * Creates a new Level creator
	 */
	public LevelCreator() {
		super("Level Creator - unsaved");
		
		// <editor-fold defaultstate="collapsed" desc=" Random initialization stuff ">
		// The portal-pairs have an index (1, 2, etc) that represents their binding
		// The portal ID pool is a linked list with int between 1 and 8.
		resetPortalIDPool();
		// This map keep track of which portal is binded to which portal.
		bindedPortals = new TreeMap<Integer, JLabel[]>();
		this.numberOfPortals = 0;

		// This will be set to true if the uses does anything. Uses when determining if the level
		// should be saved
		levelIsModified = false;
		// The intro text to the level. Later this variable is tested if it is empty
		// so it shouldn't be null!
		introText = "";

		// True if the editor is in portal binding mode. In binding mode no tiles can be placed
		// only portal can be binded to other portals
		portalBindingMode = false;

		// Used for keyboard shortcuts
		addKeyListener(this);
		// </editor-fold>
		
		// <editor-fold defaultstate="collapsed" desc=" Look-and-feel ">
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			javax.swing.SwingUtilities.updateComponentTreeUI(this);
			pack();
		} catch (ClassNotFoundException ex) {
			Logger.getLogger(LevelCreator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			Logger.getLogger(LevelCreator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			Logger.getLogger(LevelCreator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (UnsupportedLookAndFeelException ex) {
			System.out.println("The system default look-and-feel was not found :o");
			JOptionPane.showMessageDialog(this, "The look-and-feel was not found. Are you running a weir OS? Please contact the developers", "ERROR!", JOptionPane.WARNING_MESSAGE);
			System.exit(1);
		}
		// </editor-fold>
		
		// <editor-fold defaultstate="collapsed" desc=" Filler icon ">
		// The filler icon is used to fill the holes between the tiles,
		// the small 3 times 3 holes surronded by walls
		Icon fillerIcon = new Icon() {

			@Override
			public void paintIcon(Component c, Graphics g, int x, int y) {
				g.setColor(java.awt.Color.WHITE);
				g.fillRect(0, 0, getIconWidth(), getIconHeight());
			}

			@Override
			public int getIconWidth() {
				return 3;
			}

			@Override
			public int getIconHeight() {
				return 3;
			}
		};

// </editor-fold>

		// <editor-fold defaultstate="collapsed" desc="Label grid">
		/* The label grid is the actual level. It contains a field for every wall, tile and
		 * hole in the level. In the game the walls are stored as values in the tiles
		 * but in the level creator they are field of their own.
		 * Here in the constructor the label grid is initialized with empty tiles,
		 * walls on the outer boarders and filler tiles in the holes
		 */
		// Init labelGrid
		labelGrid = new JLabel[2 * rows + 1][2 * cols + 1];

		// Sets the corners
		labelGrid[0][0] = new JLabel();   // top-left
		labelGrid[0][2 * cols] = new JLabel(); // top-right
		labelGrid[2 * rows][0] = new JLabel(); // bottom-left
		labelGrid[2 * rows][2 * cols] = new JLabel(); // bottom-right

		// Sets the vertical borders
		for (int r = 1; r < 2 * rows; r++) {
			if (r % 2 == 1) {
				labelGrid[r][0] = new JLabel(new WallIcon(WallIcon.VERTICAL, true));
				labelGrid[r][2 * cols] = new JLabel(new WallIcon(WallIcon.VERTICAL, true));
			} else {
				labelGrid[r][0] = new JLabel(fillerIcon);
				labelGrid[r][2 * cols] = new JLabel(fillerIcon);
			}
		}
		// Sets the horizontal borders
		for (int c = 1; c < 2 * cols; c++) {
			if (c % 2 == 1) {
				labelGrid[0][c] = new JLabel(new WallIcon(WallIcon.HORIZONTAL, true));
				labelGrid[2 * rows][c] = new JLabel(new WallIcon(WallIcon.HORIZONTAL, true));
			} else {
				labelGrid[0][c] = new JLabel(fillerIcon);
				labelGrid[2 * rows][c] = new JLabel(fillerIcon);
			}
		}

		// Sets all tiles except the borders to empty tiles
		for (int r = 1; r < 2 * rows; r++) {
			for (int c = 1; c < 2 * cols; c++) {
				if (r % 2 == 1) {
					if (c % 2 == 0) {
						//System.err.println("Creating Vertical wall at (" + r + "," + c + ")");
						labelGrid[r][c] = new JLabel(new WallIcon(WallIcon.VERTICAL, false));
					} else {
						//System.err.println("Creating JLabel at (" + r + "," + c + ")");
						labelGrid[r][c] = new JLabel(new TileIcon(TileType.EMPTYTILE, c, r));
					}
				} else if (c % 2 == 1) {
					labelGrid[r][c] = new JLabel(new WallIcon(WallIcon.HORIZONTAL, false));
				} else {
					labelGrid[r][c] = new JLabel(fillerIcon);


				}
			}
		}
		// </editor-fold>
		
		// <editor-fold defaultstate="collapsed" desc="GUI">
		
		// <editor-fold defaultstate="collapsed" desc="Menu">
		//Create menu

		JMenuBar menuBar = new JMenuBar();
		JMenu file = new JMenu();

		// Sets the name, adds the action listener and the shortcut to each menu item and also adds them to the menu
		JMenuItem itemNew = new JMenuItem("New");
		itemNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
		itemNew.addActionListener(this);
		file.add(itemNew);

		JMenuItem itemOpen = new JMenuItem("Open");
		itemOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		itemOpen.addActionListener(this);
		file.add(itemOpen);

		JMenuItem itemSave = new JMenuItem("Save");
		itemSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		itemSave.addActionListener(this);
		file.add(itemSave);

		JMenuItem itemSaveAs = new JMenuItem("Save As");
		itemSaveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK));
		itemSaveAs.addActionListener(this);
		file.add(itemSaveAs);

		file.addSeparator();
		
		JMenuItem itemLM = new JMenuItem("Level Manager");
		itemLM.addActionListener(this);
		file.add(itemLM);
		
		file.addSeparator();

		JMenuItem itemClose = new JMenuItem("Quit");
		itemNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
		itemClose.addActionListener(this);
		file.add(itemClose);

		file.setText("File");

		menuBar.add(file);
		// </editor-fold>

		// <editor-fold defaultstate="collapsed" desc="Panel layout">
		// Inits the left panel and sets its background
		JLevelPanel panel = new JLevelPanel(new GridBagLayout(), "Assets/Textures/SkyplaneSpace.png");
		// This constrains will be used with the panel (to the left)
		GridBagConstraints panelConstraint = new GridBagConstraints();
		
		// Creates the toolboxes that will contain the different tiles
		// <editor-fold defaultstate="collapsed" desc=" Toolboxes ">
		JToolBar stdToolbar = new JToolBar("Standard tiles");
		stdToolbar.setToolTipText("Standard tiles");
		stdToolbar.setFloatable(false);

		JToolBar trapToolbar = new JToolBar("Trap tiles");
		trapToolbar.setToolTipText("Trap tiles");
		trapToolbar.setFloatable(false);

		JToolBar permanentArrowToolBar = new JToolBar("Arrow Tiles");
		permanentArrowToolBar.setToolTipText("Arrow Tiles");
		permanentArrowToolBar.setFloatable(false);
		
		JToolBar portalBindingToolBar = new JToolBar("Portal Binding Tiles");
		portalBindingToolBar.setToolTipText("Portal Binding Tiles");
		portalBindingToolBar.setFloatable(false);

		// Stores an object of each tile type. This is stored in a public variable because it will
		// be used in the action listener and such.
		panelTiles[0] = new JLabel(new ImageIcon("Assets/LevelCreator/empty.png"));
		panelTiles[1] = new JLabel(new ImageIcon("Assets/LevelCreator/nest.png"));
		panelTiles[2] = new JLabel(new ImageIcon("Assets/LevelCreator/spawn.png"));

		trapTiles[0] = new JLabel(new ImageIcon("Assets/LevelCreator/BlackHole.png"));
		trapTiles[1] = new JLabel(new ImageIcon("Assets/LevelCreator/Cattrap.png"));
		trapTiles[2] = new JLabel(new ImageIcon("Assets/LevelCreator/Mousetrap.png"));
		trapTiles[3] = new JLabel(new ImageIcon("Assets/LevelCreator/Glue.png"));
		trapTiles[4] = new JLabel(new ImageIcon("Assets/LevelCreator/Portal.png"));

		permanentArrowTiles[0] = new JLabel(new ImageIcon("Assets/LevelCreator/arrowLEFT.png"));
		permanentArrowTiles[1] = new JLabel(new ImageIcon("Assets/LevelCreator/arrowRIGHT.png"));
		permanentArrowTiles[2] = new JLabel(new ImageIcon("Assets/LevelCreator/arrowUP.png"));
		permanentArrowTiles[3] = new JLabel(new ImageIcon("Assets/LevelCreator/arrowDOWN.png"));
		
		portalBindingTiles[0] = new JLabel(new ImageIcon("Assets/LevelCreator/NoTileTile.png"));

		// To make thing a bit less messy there is a private class that listens to the panels
		// actions. I.e. seperates the level and the panel actions.
		MouseListener panelMouseListener = new PanelMouseListener();
		// All previoulsy created tile icons are added to the toolboxes and are assign a action listener
		// The separators are added to minimize the padding between the icons
		for (JLabel jl : panelTiles) {
			jl.addMouseListener(panelMouseListener);
			stdToolbar.add(jl);
			stdToolbar.addSeparator(new Dimension(1, 25));
		}

		for (JLabel jl : trapTiles) {
			jl.addMouseListener(panelMouseListener);
			trapToolbar.add(jl);
			trapToolbar.addSeparator(new Dimension(1, 25));
		}

		for (JLabel jl : permanentArrowTiles) {
			jl.addMouseListener(panelMouseListener);
			permanentArrowToolBar.add(jl);
			permanentArrowToolBar.addSeparator(new Dimension(1, 25));
		}
		
		for (JLabel jl : portalBindingTiles) {
			jl.addMouseListener(panelMouseListener);
			portalBindingToolBar.add(jl);
			portalBindingToolBar.addSeparator(new Dimension(1, 25));
		}

		// Each toolbox should be added in the north west corner and only one toolbox
		// per line should be added
		panelConstraint.gridwidth = GridBagConstraints.REMAINDER;
		panelConstraint.anchor = GridBagConstraints.NORTHWEST;

		panel.add(stdToolbar, panelConstraint);
		panel.add(trapToolbar, panelConstraint);
		panel.add(permanentArrowToolBar, panelConstraint);
		panel.add(portalBindingToolBar, panelConstraint);
		// </editor-fold>
		
		// <editor-fold defaultstate="collapsed" desc=" Current tile image ">
		// Current tile image
		// When a tile is selected a larger version of it should be displayed in a box.
		// This code creates objects of every such type and stores them. This code is for saving proccessor power
		emptyTile = Toolkit.getDefaultToolkit().createImage("Assets/LevelCreator/empty.png").getScaledInstance(75, -1, Image.SCALE_DEFAULT);
		nest = Toolkit.getDefaultToolkit().createImage("Assets/LevelCreator/nest.png").getScaledInstance(75, -1, Image.SCALE_DEFAULT);
		spawn = Toolkit.getDefaultToolkit().createImage("Assets/LevelCreator/spawn.png").getScaledInstance(75, -1, Image.SCALE_DEFAULT);
		upArrow = Toolkit.getDefaultToolkit().createImage("Assets/LevelCreator/arrowUP.png").getScaledInstance(75, -1, Image.SCALE_DEFAULT);
		downArrow = Toolkit.getDefaultToolkit().createImage("Assets/LevelCreator/arrowDOWN.png").getScaledInstance(75, -1, Image.SCALE_DEFAULT);
		leftArrow = Toolkit.getDefaultToolkit().createImage("Assets/LevelCreator/arrowLEFT.png").getScaledInstance(75, -1, Image.SCALE_DEFAULT);
		rightArrow = Toolkit.getDefaultToolkit().createImage("Assets/LevelCreator/arrowRIGHT.png").getScaledInstance(75, -1, Image.SCALE_DEFAULT);
		blackHole = Toolkit.getDefaultToolkit().createImage("Assets/LevelCreator/BlackHole.png").getScaledInstance(75, -1, Image.SCALE_DEFAULT);
		catTrap = Toolkit.getDefaultToolkit().createImage("Assets/LevelCreator/Cattrap.png").getScaledInstance(75, -1, Image.SCALE_DEFAULT);
		mouseTrap = Toolkit.getDefaultToolkit().createImage("Assets/LevelCreator/Mousetrap.png").getScaledInstance(75, -1, Image.SCALE_DEFAULT);
		glue = Toolkit.getDefaultToolkit().createImage("Assets/LevelCreator/Glue.png").getScaledInstance(75, -1, Image.SCALE_DEFAULT);
		portal = Toolkit.getDefaultToolkit().createImage("Assets/LevelCreator/Portal.png").getScaledInstance(75, -1, Image.SCALE_DEFAULT);

		// The currect tile type is set to empty tile
		currentTileLabel = new JLabel();
		setCurrentTile(TileType.EMPTYTILE);

		// Centers the up-scaled tile icon
		panelConstraint.ipady = 20;
		panelConstraint.ipadx = 25;
		
		panel.add(currentTileLabel, panelConstraint);
		// </editor-fold>
		
		// <editor-fold defaultstate="collapsed" desc=" Sky plane ">
		// Skyplane
		// Creates the list of sky planes
		String[] skyPlaneNames = new File("Assets/Textures/").list(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				if (name.startsWith("Skyplane")) {
					return true;
				}
				return false;
			}
		});

		// Fix the names
		for (int i = 0; i < skyPlaneNames.length; i++) {
			skyPlaneNames[i] = skyPlaneNames[i].replace("Skyplane", "").replace(".png", "");
		}
		
		JComboBox skyPlaneBox = new JComboBox(skyPlaneNames);
		skyPlaneBox.setSelectedIndex(0);
		skyPlaneBox.addActionListener(this);
		
		panelConstraint.ipadx = 50;
		panelConstraint.ipady = 0;
		
		panel.add(skyPlaneBox, panelConstraint);
		// </editor-fold>

		// Addes a filler JLabel to make the other stuff keep to the north
		panelConstraint.gridheight = GridBagConstraints.REMAINDER;
		panelConstraint.weighty = 1;
		panel.add(new JLabel(), panelConstraint);
		// </editor-fold>

		// <editor-fold defaultstate="collapsed" desc="Level layout">
		//Create level layout
		// Creates the graphical part of the actual level.
		levelPanel = new JLevelPanel(new BorderLayout(), "Assets/Textures/Skyplane" + ((String)skyPlaneBox.getSelectedItem()) + ".png");
		// levelGrid holds the actual tiles from the labelGrid
		JPanel levelGrid = new JPanel(new GridBagLayout());
		// Very importans if the skyplane should be visible
		levelGrid.setOpaque(false);
		GridBagConstraints levelConstrinat = new GridBagConstraints();

		// Adds the tile icons from the labelGrid to the levelGrid
		for (int r = 0; r <= 2 * rows; r++) {
			for (int c = 0; c <= 2 * cols; c++) {
				levelConstrinat.gridx = c;
				levelConstrinat.gridy = r;
				labelGrid[r][c].addMouseListener(this);
				levelGrid.add(labelGrid[r][c], levelConstrinat);
			}
		}
		levelPanel.add(levelGrid, BorderLayout.CENTER);
		// </editor-fold>

		// <editor-fold defaultstate="collapsed" desc="Main layout">
		//Create main layout
		// Adds all the above created stuff
		JPanel mainPanel = new JPanel();
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(panel, BorderLayout.WEST);
		mainPanel.add(levelPanel);

		setJMenuBar(menuBar);
		add(mainPanel);

		setSize(800, 600);
		centerWindow();
		setVisible(true);
		// The program should check if the level has been changed and if so save the
		// level therefore nothing is done when the close button is press, or rather
		// the close-operation is not handled here.
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setResizable(false);

		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				if (checkAndSaveLevel()) {
					System.exit(0);
				}
			}
		});

	// </editor-fold>
	// </editor-fold>
	}
	
	

	// <editor-fold defaultstate="collapsed" desc=" Open / Save / Reset ">
	/**
	 * This method opens (loads) the level with the given file name. The parameter
	 * can be the name of the file or the reletive path.
	 * @param fileName the file name of the level that should be loaded
	 * @throws IllegalArgumentException if the given file has the wrong file extension
	 */
	public void openLevel(String fileName) {
		// Load the level
		try {
			// Checks the file extension of the given level
			if (!fileName.endsWith(".lvl")) {
				throw new IllegalArgumentException("The given file is not a level file. Should be a .lvl file");
			}
			Level level;
			Tile[][] tiles;
			
			if(!fileName.contains("Assets/Levels/")) {
				fileName = "Assets/Levels/" + fileName;
			}
			
			// Reads the level from the file
			level = Level.LoadLevel(fileName);

			// For faster indexing the tile list is saved in a multi-array
			// instead of reading directly from the SortedTileList
			tiles = new Tile[rows][cols];
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					tiles[r][c] = level.getTile(c, r);				// Set the tiles
				}
			}
			// Vertical borders
			for (int r = 1; r < 2 * rows; r += 2) {
				labelGrid[r][0].setIcon(new WallIcon(WallIcon.VERTICAL, tiles[(int) (r / 2)][0].hasWall(Direction.LEFT)));
				labelGrid[r][2 * cols].setIcon(new WallIcon(WallIcon.VERTICAL, tiles[(int) (r / 2)][cols - 1].hasWall(Direction.RIGHT)));
			}
			// Horizontal borders
			for (int c = 1; c < 2 * cols; c += 2) {
				labelGrid[0][c].setIcon(new WallIcon(WallIcon.HORIZONTAL, tiles[0][(int) (c / 2)].hasWall(Direction.UP)));
				labelGrid[2 * rows][c].setIcon(new WallIcon(WallIcon.HORIZONTAL, tiles[rows - 1][(int) (c / 2)].hasWall(Direction.DOWN)));
			}
			// All tiles except the borders
			// The labelGrid holds all tiles, walls and empty slots between walls
			// this loop fills the array with the correct values
			for (int r = 1; r < 2 * rows; r++) {
				for (int c = 1; c < 2 * cols; c++) {
					if (r % 2 == 1) {
						if (c % 2 == 0) { // If the current r,c coord is a vertical wall
							labelGrid[r][c].setIcon(new WallIcon(WallIcon.VERTICAL, tiles[(int) (r / 2)][(int) ((c - 1) / 2)].hasWall(Direction.RIGHT)));
						} else { // If the current r,c coord is a tile
							TileType tt = null;
							// Converts the 2 * width + 1 size loop to 0 to 15 size, same for height
							int y = (int) (r / 2);
							int x = (int) (c / 2);
							if (tiles[y][x].getClass() == EmptyTile.class) {
								EmptyTile et = (EmptyTile) tiles[y][x];
								if (et.hasArrow()) {
									if (et.getArrowDirection() == Direction.LEFT) {
										tt = TileType.ARROWLEFT;
									} else if (et.getArrowDirection() == Direction.RIGHT) {
										tt = TileType.ARROWRIGHT;
									} else if (et.getArrowDirection() == Direction.UP) {
										tt = TileType.ARROWUP;
									} else if (et.getArrowDirection() == Direction.DOWN) {
										tt = TileType.ARROWDOWN;
									}
								} else {
									tt = TileType.EMPTYTILE;
								}
							} else if (tiles[y][x].getClass() == Nest.class) {
								tt = TileType.NEST;
							} else if (tiles[y][x].getClass() == SpawnPoint.class) {
								tt = TileType.SPAWN;
							} else if (tiles[y][x].getClass() == BlackHole.class) {
								tt = TileType.BLACKHOLE;
							} else if (tiles[y][x].getClass() == Cattrap.class) {
								tt = TileType.CATTRAP;
							} else if (tiles[y][x].getClass() == Mousetrap.class) {
								tt = TileType.MOUSETRAP;
							} else if (tiles[y][x].getClass() == Glue.class) {
								tt = TileType.GLUE;
							} else if (tiles[y][x].getClass() == Portal.class) {
								tt = TileType.PORTAL;
							}
							labelGrid[r][c].setIcon(new TileIcon(tt, c, r));
						}
					} else if (c % 2 == 1) { // If the current r,c coord is a horizontal wall
						labelGrid[r][c].setIcon(new WallIcon(WallIcon.HORIZONTAL, tiles[(int) ((r - 1) / 2)][(int) (c / 2)].hasWall(Direction.DOWN)));
					}
				}
			}
			
			// Resets the portal binding variables
			resetPortalIDPool();
			selectedPortal = null;
			this.numberOfPortals = 0;
			bindedPortals.clear();
			
			// Connects all portals
			for (int r = 1; r < 2 * rows; r += 2) {
				for (int c = 1; c < 2 * cols; c += 2) {
					if(((TileIcon)labelGrid[r][c].getIcon()).getType() == TileType.PORTAL) {
						numberOfPortals++;
						TileIcon ti = (TileIcon)labelGrid[r][c].getIcon();
						// if the portals partner has not been assigned an id yet
						if(ti.getPortalID() == -1) {
							int y = (int) (r / 2);
							int x = (int) (c / 2);
							int id = portalIDPool.pop();
							ti.setPortalID(id);
							Portal p = ((Portal)tiles[y][x]).getParnter();
							int r2 = 2 * p.y + 1;
							int c2 = 2 * p.x + 1;
							((TileIcon)labelGrid[r2][c2].getIcon()).setPortalID(id);
							JLabel[] jl = {labelGrid[r][c],labelGrid[r2][c2]};
							bindedPortals.put(id, jl);
						}
					}
				}
			}
			
			setTitle("Level Creator - " + fileName.substring(0, fileName.length() - 4));
			savedName = fileName;
			
			this.introText = level.introText;
			levelPanel.setImage(level.getSkyplaneTexPath());
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * This method resets the current level to its initial state.
	 * Due to the multithreading of swing the field might be displayed with parts
	 * of the level still showing. I.e. the tiles are reset one by one, so some tiles
	 * might still show even though resetLevel has been called, they will be reseted
	 * in a few millisecond so it sohuld be no problem.
	 * 
	 * This method does not check if the current level has been saved! Any unsaved
	 * changes will be lost.
	 */
	private void resetLevel() {
		savedName = null;
		setTitle("Level Creator - unsaved");

		// Resets the vertical borders
		for (int r = 1; r < 2 * rows; r++) {
			if (r % 2 == 1) {
				labelGrid[r][0].setIcon(new WallIcon(WallIcon.VERTICAL, true));
				labelGrid[r][2 * cols].setIcon(new WallIcon(WallIcon.VERTICAL, true));
			}
		}
		// Resets the horizontal borders
		for (int c = 1; c < 2 * cols; c++) {
			if (c % 2 == 1) {
				labelGrid[0][c].setIcon(new WallIcon(WallIcon.HORIZONTAL, true));
				labelGrid[2 * rows][c].setIcon(new WallIcon(WallIcon.HORIZONTAL, true));
			}
		}
		// Resets all tiles except the borders to empty tiles
		for (int r = 1; r < 2 * rows; r++) {
			for (int c = 1; c < 2 * cols; c++) {
				if (r % 2 == 1) {
					if (c % 2 == 0) {
						labelGrid[r][c].setIcon(new WallIcon(WallIcon.VERTICAL, false));
					} else {
						labelGrid[r][c].setIcon(new TileIcon(TileType.EMPTYTILE, c, r));
					}
				} else if (c % 2 == 1) {
					labelGrid[r][c].setIcon(new WallIcon(WallIcon.HORIZONTAL, false));
				}
			}
		}
		this.introText = "";
		this.levelIsModified = false;
		
		// Portal binding stuff
		selectedPortal = null;
		this.numberOfPortals = 0;
		resetPortalIDPool();
		this.bindedPortals.clear();
	}

	/**
	 * Saves the current level to a file. The file is defined from within the method 
	 * (if an file name has not been given before). The method can be forced to ask for an file name, by
	 * setting the parameter to true.
	 * @param saveInNewFile true if the method should ask for a new file name regardless of the level has been save before. I.e. true = Save as
	 */
	private void saveLevel(boolean saveInNewFile) {
		// File name stuff
		String fileName = null;
		// If the level has not been saved before or a new file name is being forced
		if (savedName == null || saveInNewFile) {
			// Checks if an intro string has been setup
			String temp = "";
			 do {
				temp = (String) JOptionPane.showInputDialog(this, "Please enter an intro text", "Intro text", JOptionPane.PLAIN_MESSAGE, null, null, this.introText);
				// if the user aborts the save
				if (temp == null) {
					return;
				}
			}while (temp.equals(""));
			// If a non-empty and non-null intro text has been given it is stored
			this.introText = temp;
			
			temp = "";
			while(temp.equals("")) {
				temp = JOptionPane.showInputDialog("Please enter a file name:");
				// If the user pressed cancel the saving operation is cancelled
				if (temp == null) {
					return;
				}
				
				if(temp.contains("/") || temp.contains("\\")) {
					JOptionPane.showMessageDialog(this, "Illegal file name. The file name can not contain / or \\", "Illegal file name", JOptionPane.WARNING_MESSAGE);
					temp = "";
				} 
			}
			// This loops checks the given file name. fileName is set when the indata
			// is correct.
			while (fileName == null) {
				// If the user didn't append the file extension it is appended
				if (!temp.contains(".lvl")) {
					temp += ".lvl";				// Check if the file already exists
				}
				File levelDir = new File("Assets/Levels");
				String[] fileNames = levelDir.list();
				boolean fileExists = false;
				for (String s : fileNames) {
					if (s.equals(temp)) {
						fileExists = true;
						break;
					}
				}
				if (fileExists) {
					// If the file exists the user is asked if he want to overwrite the file
					int response = JOptionPane.showConfirmDialog(this, "A level with that name already exists. Do you want to overwrite it?", "Overwrite level?", JOptionPane.YES_NO_CANCEL_OPTION);
					// If overwrite file
					if (response == 0) {
						fileName = temp;
						// If rename new level
					} else if (response == 1) {
						temp = JOptionPane.showInputDialog("Please enter a different name:");
						// if abort save
						if (temp == null) {
							return;
							// If abort save
						}
						
						if(temp.contains("/") || temp.contains("\\")) {
							JOptionPane.showMessageDialog(this, "Illegal file name. The file name can not contain / or \\", "Illegal file name", JOptionPane.WARNING_MESSAGE);
							temp = "";
						}
					} else {
						return;
					}
				} else { // if a file with the given name don't exists
					fileName = temp;
				}
			}
		} else // If the level has been saved before and a new file name is not being forced
		{
			fileName = savedName;		// The objects in the LevelCreator is converted to Level objects
		}
		SortedTileList tiles = new SortedTileList();

		// Loops though the tiles in the labelGrid. Checks the adjacent positions
		// to see if there are any walls, these are added to the (level)tile
		boolean leftWall, rightWall, topWall, bottomWall;
		int x = 0, y = 0;
		for (int r = 1; r <= 2 * rows; r += 2) {
			for (int c = 1; c <= 2 * cols; c += 2) {
				// Stores booleans about the existans of walls on the tile. These are later used when creating the tile
				leftWall = ((WallIcon) labelGrid[r][c - 1].getIcon()).hasWall();
				rightWall = ((WallIcon) labelGrid[r][c + 1].getIcon()).hasWall();
				topWall = ((WallIcon) labelGrid[r - 1][c].getIcon()).hasWall();
				bottomWall = ((WallIcon) labelGrid[r + 1][c].getIcon()).hasWall();
				// Gets the icon at the current position in the array
				TileIcon tileIcon = (TileIcon) labelGrid[r][c].getIcon();
				// Adds a tile of the type given by the TileIcon to the current position of the tile list
				if (tileIcon.getType() == TileType.EMPTYTILE) {
					tiles.add(new EmptyTile(x, y, leftWall, rightWall, topWall, bottomWall));
				} else if (tileIcon.getType() == TileType.NEST) {
					tiles.add(new Nest(x, y, leftWall, rightWall, topWall, bottomWall));
				} else if (tileIcon.getType() == TileType.SPAWN) {
					tiles.add(new SpawnPoint(x, y, leftWall, rightWall, topWall, bottomWall));
				} else if (tileIcon.getType() == TileType.ARROWLEFT ||
						tileIcon.getType() == TileType.ARROWRIGHT ||
						tileIcon.getType() == TileType.ARROWUP ||
						tileIcon.getType() == TileType.ARROWDOWN) {
					EmptyTile t = new EmptyTile(x, y, leftWall, rightWall, topWall, bottomWall);
					Direction dir;
					if (tileIcon.getType() == TileType.ARROWLEFT) {
						dir = Direction.LEFT;
					} else if (tileIcon.getType() == TileType.ARROWRIGHT) {
						dir = Direction.RIGHT;
					} else if (tileIcon.getType() == TileType.ARROWUP) {
						dir = Direction.UP;
					} else {
						dir = Direction.DOWN;
					}
					t.setArrow(dir);
					tiles.add(t);
				} else if (tileIcon.getType() == TileType.BLACKHOLE) {
					tiles.add(new BlackHole(x, y, leftWall, rightWall, topWall, bottomWall));
				} else if (tileIcon.getType() == TileType.CATTRAP) {
					tiles.add(new Cattrap(x, y, leftWall, rightWall, topWall, bottomWall));
				} else if (tileIcon.getType() == TileType.MOUSETRAP) {
					tiles.add(new Mousetrap(x, y, leftWall, rightWall, topWall, bottomWall));
				} else if (tileIcon.getType() == TileType.GLUE) {
					tiles.add(new Glue(x, y, leftWall, rightWall, topWall, bottomWall));
				} else if (tileIcon.getType() == TileType.PORTAL) {
					tiles.add(new Portal(x, y, leftWall, rightWall, topWall, bottomWall));
				}
				// x and y is used to index the position in the Level objects (0 to 15 and 0 to 11)
				if (++x > 15) {
					x = 0;
					y++;
				}
			}
		}
		
		//Connect the portals
		Iterator<Integer> itP = bindedPortals.keySet().iterator();
		JLabel[] jla;
		// Loops through all portal IDs and connected the two portals with the same ID.
		while(itP.hasNext()) {
			jla = bindedPortals.get(itP.next());
			TileIcon ti1 = (TileIcon)jla[0].getIcon();
			TileIcon ti2 = (TileIcon)jla[1].getIcon();
			
			// Connects the portal at ti1 with the one at ti2
			((Portal)tiles.get(((int)(ti1.x / 2)), ((int)(ti1.y / 2)))).connect((Portal)tiles.get(((int)(ti2.x / 2)), ((int)(ti2.y / 2))));
		}

		//Creates and saves a level object with the newly created objects.
		if (Level.SaveLevel("Assets/Levels/" + fileName, tiles, introText, levelPanel.getImageName())) {
			savedName = fileName;
			levelIsModified = false;
			setTitle("Level Creator - " + fileName.substring(0, fileName.length() - 4));
		} else {
			JOptionPane.showMessageDialog(this, "The level could not be saved!", "Error saving level", JOptionPane.WARNING_MESSAGE);
		}
	}

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc=" Other ">
	/**
	 * Resets the portal ID pool. This method does not check if there are any portals
	 * still using the IDs. I.e. it asumse that all IDs are free.
	 */
	private static void resetPortalIDPool() {
		portalIDPool = new LinkedList<Integer>();
		for (int i = 1; i <= (maxNumberOfPortals / 2); i++) {
			portalIDPool.add(i);
		}
	}

	/**
	 * A level needs at leaste one spawn point and two nests. This method checks if the current 
	 * level has these requirements. All portals need to connected to another portal this is
	 * also checked. If any of the requirements are not met this method returns false
	 * @return true if the level has atleaste two nest, one spawn point and alla portals are connected
	 */
	private boolean validateLevel() {
		boolean nest = false, spawn = false, portal = true;
		int nestCount = 0, portalCount = 0;

		for (int r = 1; r <= 2 * rows; r += 2) {
			for (int c = 1; c <= 2 * cols; c += 2) {
				TileIcon tileIcon = (TileIcon) labelGrid[r][c].getIcon();
				if (tileIcon.getType() == TileType.NEST) {
					nestCount++;
					if (nestCount >= 2) {
						nest = true;
					}
				} else if (tileIcon.getType() == TileType.SPAWN) {
					spawn = true;
				} else if (tileIcon.getType() == TileType.PORTAL) {
					portalCount++;
					// If a portal does not have an ID the level is not valid.
					// this check assumes that if a portal has an ID it has a partner
					if (tileIcon.getPortalID() == -1) {
						portal = false;
					}
				}
			}
		}

		/* When in binding mode it is possible to select one portal and then save.
		 * In this case the above test will fail. The following code catches this error.
		 */
		if (selectedPortal != null) {
			portal = false;
		}
		// If everything was correct true is returned
		return nest && spawn && portal;
	}

	/**
	 * Checks if the level has been modified, if the level is modified the user is
	 * asked to save the level. The method returns a boolean which says if the level
	 * is in a "safe state", which means that the program can do whatever action
	 * without worrying about the level. I.e. the level is either saved or the user
	 * do not want it saved.
	 * @return true if the level is in safe state.
	 */
	private boolean checkAndSaveLevel() {
		if (levelIsModified) {
			int response = JOptionPane.showConfirmDialog(this, "The level has been modified, do you want to save it?",
					"Save changed?", JOptionPane.YES_NO_CANCEL_OPTION);
			if (response == 0) { // If the user want to save the level
				if (validateLevel()) {
					saveLevel(false);
					return true;
				} else {
					JOptionPane.showMessageDialog(this, "The level does not have the required" +
							" components (atleaste two nest, one spawn point and all Portals needs to" +
							"be connected)!", "Missing components", JOptionPane.WARNING_MESSAGE);
					// if the level is missing components it won't be saved
					return false;
				}
			} else if (response == 1) {
				return true;
			} else {
				return false;
			}
		}
		return true;
	}

	/**
	 * Sets the selected tile type. The selected tile type is used when adding new tiles to the board.
	 * @param tiletype the {@link TileType } that is selected
	 * @see TileType
	 */
	private void setCurrentTile(TileType tiletype) {
		this.currentTileType = tiletype;
		ImageIcon ii;
		if (tiletype == TileType.EMPTYTILE) {
			ii = new ImageIcon(emptyTile);
		} else if (tiletype == TileType.NEST) {
			ii = new ImageIcon(nest);
		} else if (tiletype == TileType.SPAWN) {
			ii = new ImageIcon(spawn);
		} else if (tiletype == TileType.ARROWLEFT) {
			ii = new ImageIcon(leftArrow);
		} else if (tiletype == TileType.ARROWRIGHT) {
			ii = new ImageIcon(rightArrow);
		} else if (tiletype == TileType.ARROWUP) {
			ii = new ImageIcon(upArrow);
		} else if (tiletype == TileType.ARROWDOWN) {
			ii = new ImageIcon(downArrow);
		} else if (tiletype == TileType.BLACKHOLE) {
			ii = new ImageIcon(blackHole);
		} else if (tiletype == TileType.CATTRAP) {
			ii = new ImageIcon(catTrap);
		} else if (tiletype == TileType.MOUSETRAP) {
			ii = new ImageIcon(mouseTrap);
		} else if (tiletype == TileType.GLUE) {
			ii = new ImageIcon(glue);
		} else if (tiletype == TileType.PORTAL) {
			ii = new ImageIcon(portal);
		} else {
			ii = new ImageIcon();
		}
		currentTileLabel.setIcon(ii);		
	}
	// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc=" Listeners ">
	/**
	 * Handles the menu items
	 * @param e
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("New")) {
			if (checkAndSaveLevel()) {
				resetLevel();
			}
		} else if (e.getActionCommand().equals("Open")) {
			if (checkAndSaveLevel()) {
				new LevelManager(this, LevelManager.OPEN);
			}
		} else if (e.getActionCommand().equals("Save")) {
			if (validateLevel()) {
				saveLevel(false);
			} else {
				JOptionPane.showMessageDialog(this, "The level does not have the required" +
						" components (atleaste two nest, one spawn point and all Portals needs to" +
						"be connected)!", "Missing components", JOptionPane.WARNING_MESSAGE);
			}
		} else if (e.getActionCommand().equals("Save As")) {
			if (validateLevel()) {
				saveLevel(true);
			} else {
				JOptionPane.showMessageDialog(this, "The level does not have the required" +
						" components (atleaste two nest, one spawn point and all Portals needs to" +
						"be connected)!", "Missing components", JOptionPane.WARNING_MESSAGE);
			}
		} else if (e.getActionCommand().equals("Quit")) {
			if (checkAndSaveLevel()) {
				System.exit(0);
			}
		} else if (e.getActionCommand().equals("Level Manager")) {
			if (checkAndSaveLevel()) {
				new LevelManager(this, LevelManager.EDIT);
			}
		} else { // Sky planes
			if (e.getSource().getClass() == JComboBox.class) {
				String select = (String) ((JComboBox) e.getSource()).getSelectedItem();
				levelPanel.setImage("Assets/Textures/Skyplane" + select + ".png");
			}
		}
	}

	//<editor-fold defaultstate="collapsed" desc="Unused mouse listener methods">  
	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}
	// </editor-fold>  
	/**
	 * Keyboard shortcuts for changing the current tile type
	 * @param e
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_N:
				setCurrentTile(TileType.NEST);
				break;
			case KeyEvent.VK_S:
				setCurrentTile(TileType.SPAWN);
				break;
			case KeyEvent.VK_E:
				setCurrentTile(TileType.EMPTYTILE);
				break;
			case KeyEvent.VK_D: // Arrow down
				setCurrentTile(TileType.ARROWDOWN);
				break;
			case KeyEvent.VK_U: // Arrow up
				setCurrentTile(TileType.ARROWUP);
				break;
			case KeyEvent.VK_L: // Arrow left
				setCurrentTile(TileType.ARROWLEFT);
				break;
			case KeyEvent.VK_R: // Arrow right
				setCurrentTile(TileType.ARROWRIGHT);
				break;
		}
	}

	// <editor-fold defaultstate="collapsed" desc=" Unused keylisteners ">
	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc=" Portal connction help methods ">
	// These methods are just code used in more than one place in the portal binding code
	// they should not be treated at methods that can be used whenever.

	/**
	 * Returns true if the given TileIcon has an portal ID. If true the method shows
	 * a warining message about that the TileIcon already having a ID
	 * 
	 * This methods is just code used in more than one place in the portal binding code
	 * it should not be treated at a method that can be used whenever.
	 * @param ti
	 * @return
	 */
	private boolean checkPortalID(TileIcon ti) {
		if (ti.getPortalID() != -1) {
			JOptionPane.showMessageDialog(this, "This portal already has a connection. You have to remove it before adding a new one." +
					" You can do this by right clicking the Portal", "Already connection", JOptionPane.WARNING_MESSAGE);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * This method removes the given JLabels portal ID. If the JLabel has no
	 * portal ID nothing happens
	 * 
	 * This methods is just code used in more than one place in the portal binding code
	 * it should not be treated at a method that can be used whenever.
	 * @param jl
	 */
	private void removePortalID(JLabel jl) {
		TileIcon ti = (TileIcon) jl.getIcon();
		if (ti.getPortalID() != -1) {
			if (bindedPortals.containsKey(ti.getPortalID())) {
				JLabel[] tia = bindedPortals.remove(ti.getPortalID());
				// Returns the ID to the pool and removes the IDs from the portals
				portalIDPool.push(((TileIcon) tia[0].getIcon()).getPortalID());
				((TileIcon) tia[0].getIcon()).setPortalID(-1);
				((TileIcon) tia[1].getIcon()).setPortalID(-1);
				tia[0].repaint();
				tia[1].repaint();
			} else if (selectedPortal == jl) {
				portalIDPool.push(((TileIcon) selectedPortal.getIcon()).getPortalID());
				((TileIcon) selectedPortal.getIcon()).setPortalID(-1);
				selectedPortal = null;
			} else {
				// This should never happen but just in case some error data is printed
				System.out.println("Error in LevelCreator removePortalID. ti was " + ti.getClass());
			}
		}
	}

	// </editor-fold>
	/**
	 * Handles the placement of tiles in the playingfield
	 * @param e
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		try {
			// Easy enough to understand right?
			javax.swing.Icon icon = ((JLabel) e.getSource()).getIcon();
			if (icon.getClass() == TileIcon.class) {
				// If the tile is a portal and we're in binding mode
				if (portalBindingMode && ((TileIcon) icon).getType() == TileType.PORTAL) {
					if (e.getButton() == MouseEvent.BUTTON1) {
						if (selectedPortal == null) {
							TileIcon ti = ((TileIcon) (((JLabel) (e.getSource())).getIcon()));
							// if the clicked TileIcon don't have an ID it is given one.
							if (!checkPortalID(ti)) {
								selectedPortal = (JLabel) e.getSource();
								((TileIcon) selectedPortal.getIcon()).setPortalID(portalIDPool.pop());
							}
						} else {
							// If the same Portal was clicked twice the ID is returend and the Portal is deselected
							if (selectedPortal == e.getSource()) {
								// Gives the ID back to the pool
								portalIDPool.push(((TileIcon) selectedPortal.getIcon()).getPortalID());
								((TileIcon) selectedPortal.getIcon()).setPortalID(-1);
								selectedPortal = null;
							} else {
								// Check if the clicked Portal already has a connection,
								// otherwise the portal is added with its partner to the portal array
								if (!checkPortalID((TileIcon) icon)) {
									JLabel[] ti = {selectedPortal, (JLabel) e.getSource()};
									((TileIcon) icon).setPortalID(((TileIcon) selectedPortal.getIcon()).getPortalID());
									bindedPortals.put(((TileIcon) selectedPortal.getIcon()).getPortalID(), ti);
									selectedPortal = null;
								}
							}
						}
					} else { // if right click
						removePortalID((JLabel) e.getSource());
					}
				} else { // if not portal binding mode
					// If left click
					if (e.getButton() == MouseEvent.BUTTON1) {
						if (currentTileType == TileType.PORTAL) {
							// If the maximum number of tiles has been reached nothing is added
							if (numberOfPortals >= maxNumberOfPortals) {
								JOptionPane.showMessageDialog(this, "You can only have " + maxNumberOfPortals + " portals", "Maximum number of portals reached", JOptionPane.WARNING_MESSAGE);
								return;
							}
							
							numberOfPortals++;
						} else { // if current type is not PORTAL
							if (((TileIcon) icon).getType() == TileType.PORTAL) {
								removePortalID((JLabel) e.getSource());
								numberOfPortals--;
							}
						}
						((TileIcon) icon).setType(currentTileType);
						// if right click
					} else {
						if (((TileIcon) icon).getType() == TileType.PORTAL) {
							removePortalID((JLabel) e.getSource());
							numberOfPortals--;
						}
						((TileIcon) icon).setType(TileType.EMPTYTILE);
					}
				}
				levelIsModified = true;
			} else if (icon.getClass() == WallIcon.class) {
				// You can only add/remove walls when not in portal binding mode
				if (!portalBindingMode) {
					WallIcon wallIcon = (WallIcon) icon;
					// if the clicked wall tile has a wall, that wall is removed
					// otherwise a wall is added
					if (wallIcon.hasWall()) {
						wallIcon.setWall(false);
					} else {
						wallIcon.setWall(true);
					}
					levelIsModified = true;
				}
			}

			// If the level is modified a '*' should be appended to the title
			// to show that the level is not saved with the lates modifications.
			// Only one '*' should be added, this is also tested for.
			if (levelIsModified && !getTitle().endsWith("*")) {
				setTitle(getTitle() + "*");
			}
			((JLabel) e.getSource()).repaint();
		} catch (ClassCastException ex) {
			System.out.println("WTF LOL");
			ex.printStackTrace();
		}
	}

	/**
	 * This class handles the mouse events of the panel (toolbox)
	 */
	private class PanelMouseListener implements MouseListener {

		//<editor-fold defaultstate="collapsed" desc="Unused mouse listener methods">  
		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}
		//</editor-fold>  
		/**
		 * Handles the selection of tile to place on the plaaying field
		 * @param e
		 */
		@Override
		public void mousePressed(MouseEvent e) {
			portalBindingMode = false;
			if (e.getSource() == panelTiles[0]) // If empty tile
			{
				setCurrentTile(TileType.EMPTYTILE);
			} else if (e.getSource() == panelTiles[1]) // If nest
			{
				setCurrentTile(TileType.NEST);
			} else if (e.getSource() == panelTiles[2]) // If spawn
			{
				setCurrentTile(TileType.SPAWN);
			} else if (e.getSource() == trapTiles[0]) { // If trap Black hole
				setCurrentTile(TileType.BLACKHOLE);
			} else if (e.getSource() == trapTiles[1]) { // If trap Cattrap
				setCurrentTile(TileType.CATTRAP);
			} else if (e.getSource() == trapTiles[2]) { // If trap Mousetrap
				setCurrentTile(TileType.MOUSETRAP);
			} else if (e.getSource() == trapTiles[3]) { // If trap Glue
				setCurrentTile(TileType.GLUE);
			} else if (e.getSource() == trapTiles[4]) { // If trap Portal
				setCurrentTile(TileType.PORTAL);
			} else if (e.getSource() == permanentArrowTiles[0]) // If permanante arrow left
			{
				setCurrentTile(TileType.ARROWLEFT);
			} else if (e.getSource() == permanentArrowTiles[1]) // If permanante arrow right
			{
				setCurrentTile(TileType.ARROWRIGHT);
			} else if (e.getSource() == permanentArrowTiles[2]) // If permanante arrow up
			{
				setCurrentTile(TileType.ARROWUP);
			} else if (e.getSource() == permanentArrowTiles[3]) { // If permanante arrow down
				setCurrentTile(TileType.ARROWDOWN);
			} else if (e.getSource() == portalBindingTiles[0]) { // if portal binding
				setCurrentTile(null);
				portalBindingMode = true;
			} else if (e.getSource() == portalBindingTiles[1]) { // if portal unbinding
				setCurrentTile(null);
			}
		}
	}
	// </editor-fold>
	
	/**
	 * Centers this Frame on the screen.
	 */
	private void centerWindow() {
		Dimension screenSize =
				java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = getSize();


		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}
		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		setLocation(
				(screenSize.width - frameSize.width) >> 1,
				(screenSize.height - frameSize.height) >> 1);
	}
}


