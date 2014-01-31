package mouserunner.Managers;

import java.util.*;
import java.io.*;
import static javax.swing.JOptionPane.*;

/**
 * Config (singleton) keeps track of the userdefined preferences of the game
 * loads/saves this information to a textfile and also loads the standard
 * settings if no file is available at runtime
 * @author Zorek
 */
public class ConfigManager {
  private final String confFile="Assets/Misc/config.dat";
  
  //System settings loaded from file
	/** The set up width of the MouseRunner window			*/
	public int width;					
	/** The set up height of the MouseRunner window			*/
	public int height;		
	/** True if special effects should be enabled in the game			*/
  public boolean sfx;	
	/** True if sound should be enabled in the game			*/
  public boolean sound;	
	/** True if the intro should be autoskipped					*/
	public boolean skipIntro; 
	
	//Lobby settings loaded from file
	/** The name the user used last in the lobby				*/
	public String name;	
	/** The favorite servers of the user								*/
	public List<String> favorites = new LinkedList<String>(); 
	
	//Flags sent to MouseRunner via commandline
	/** If true, the program is running in debug mode		*/
	public boolean debug;
			
	private static ConfigManager instance = new ConfigManager();

  /**
   * Creates a new configmanager (internal) and loads the information from file
   */
  private ConfigManager() {
    try {
      loadSettings();
    }
    catch(Exception e) {
      System.out.println("ConfigManager could not load config file, loading default");
      loadStandard();
    }
    saveSettings();
  }
	
  /**
   * Loading standard settings for the game
   */
  private void loadStandard() {
		width=800;
		height=600;
		sfx=true;
    sound=true;
		skipIntro=false;
		name="Unknown";
		favorites.add("Loopback");
		favorites.add("127.0.0.1");
  }

  /**
   * Prints settings to file
   */
  public void saveSettings() {
    try {
      PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(confFile)));
      out.print("{System}\n");
      out.print(width+" "+height+"\n");
			out.print(sfx+"\n");
			out.print(sound+"\n");
			out.print(skipIntro+"\n");
			out.print("{Lobby}\n");
			out.print(name+"\n");
			for(String f: favorites)
				out.print(f + " ");
			out.print("\n");
      out.flush();
      out.close();
    }
    catch(IOException e){
      showMessageDialog(null, "Couldn't save settings to disk, contact the system administrator");
    }
  }
  
  /**
   * Loading settings from file
   * @throws IOException if experiencing problem with reading
   */
  private void loadSettings() throws IOException {
    Scanner sc = new Scanner(new File(confFile));
    sc.useLocale(new Locale("en-US"));
    sc.nextLine();
		width=sc.nextInt();
    height=sc.nextInt();
		sfx=sc.nextBoolean();
    sound=sc.nextBoolean();
		skipIntro=sc.nextBoolean();
		sc.nextLine();
		sc.nextLine();
		name=sc.nextLine();
		Scanner favScan = new Scanner(sc.nextLine());
		while(favScan.hasNext())
			favorites.add(favScan.next());
  }
	
  /**
   * Retreave the singleton reference
   * @return the single instance
   */
	public static ConfigManager getInstance() {
		return instance;
	}
}
