package mouserunner.Managers;
				
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;


/**
 * SoundManager (singleton) is in charge of the sound. It starts and stops playback when told to.
 * @author Zorek
 */
public class SoundManager {

  /** Singleton */
  private static SoundManager instance = new SoundManager();
  /** Set of soundclips for each sound loaded */
  private static List<Clip> playingClipList = new ArrayList<Clip>();
  

  /**
   * Empty private constructor
   */
  private SoundManager() {
  }

  public static SoundManager getInstance() {
    return instance;
  }

  /**
   * The method playSound is used to start playback of a desired file.
   *
   * @param file  a File with path to the desired soundfile, preferably ogg-format
   * @param loop  if true, the sound will be looped until stopSounds() is called
   */
  public void playSound(final File file, final boolean loop) {
    if(ConfigManager.getInstance().sound) {
      Clip newClip = new Clip(file, loop);
      newClip.start();
      playingClipList.add(newClip);
    }
  }

	/**
   * This method checks if a soundfile is playing right now
   *
   * @param file is this sound file playing?
   * @return if true, the sound file is currently playing
   */
	public boolean isPlaying(final File file) {
		boolean result=false;
		for(Clip c: playingClipList)
			if(file.getPath().equals(c.getClipFilePath()))
				result=true;
		return result;
	}
	
  /**
   * Method to stop all playback from SoundManager
   */
  public void stopSounds() {
    for (Clip cl : playingClipList)
      cl.stopClip();
  }
}

/**
 * Clip is a internal class representing each sound, using threads these clips can
 * be played at any time during gameplay
 * @author Zorek
 */
class Clip extends Thread {

  private AudioFormat af;
  private AudioInputStream ais;
  private final File file;
  private final boolean loop;
  boolean playing;

  /**
   * Creating a new Clip
   * @param file is a valid soundfile to start playing
   * @param loop if true, the clip don't stop playing until stop is called
   */
  public Clip(final File file, final boolean loop) {
    this.file = file;
    this.loop = loop;
  }

  /**
   * This method is called by the threading system when start() is called.
   * Starts the playback of clip.
   */
  @Override
  public void run() {
		playing=true;
    play();
  }

  /**
   * Loads the requested soundclip from HDD
   */
  private void loadClip() {
    try {
      AudioInputStream in = AudioSystem.getAudioInputStream(file);
      ais = null;
      if (in != null) {
        AudioFormat baseFormat = in.getFormat();
        af = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(),
                16,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getSampleRate(),
                false);
        ais = AudioSystem.getAudioInputStream(af, in);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Plays the requested clip (loops if loop=true)
   */
  private void play() {
    try {
      byte[] data = new byte[4096];
      while (playing) {
        loadClip();
        SourceDataLine line = getLine(af);
        if (line != null) {
          line.start();
          int nBytesRead = 0;
          while (nBytesRead != -1) {
            nBytesRead = ais.read(data, 0, data.length);
            if (nBytesRead != -1) {
              line.write(data, 0, nBytesRead);
            }
						if(!playing) {
							line.stop();
							line.close();
							return;
						}
          }
          line.drain();
          line.stop();
          line.close();
          playing=loop;
          ais.close();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public void stopClip() {
    this.playing = false;
  }

  /**
   * Method to read a new binary line from audio file using available codecs
	 * @param audioFormat a reference to the AudioFormat object used for playback
	 * @throws LineUnavailableException thrown if the next line cannot be read
	 * @return the next line information from the current audiofile
   */
  private SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException {
    SourceDataLine res = null;
    DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
    res = (SourceDataLine) AudioSystem.getLine(info);
    res.open(audioFormat);
    return res;
  }
	
	/**
	 * Gets this clips file path
	 * @return the current clips file path
	 */
	public String getClipFilePath() {
		return file.getPath();
	}
}
