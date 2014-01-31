package LevelCreator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FilenameFilter;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 * This class is a GUI frame that contains a list of the Levels in the levels folder and
 * provide buttons for renaming and deleting the levels
 * @author Erik
 */
public class LevelManager extends JFrame implements ActionListener, MouseListener {
	/** Starts a Level Manager in OPEN mode, this means that levels can be opens from the level manager */
	public final static int OPEN = 0;
	/** Starts a Level Manager in EDIT mode, this means that levels can be renamed and deletet */
	public final static int EDIT = 1;
	
	// A reference to the Level creator. This will be used when a Level should be opened,
	// the name of the level will then be passed to the level creator (often the one that spawned the level manager)
	private LevelCreator levelCreator;
	// The list of all levels
	private JList fileList;
	// Buttons used in open mode
	private JButton openButton;
	// buttons used in edit mode
	private JButton renameButton,  deleteButton;
	// A file name filer that filters all files that does not have the file extension .lvl
	private static FilenameFilter fileFilter = null;

	/**
	 * Creates a Level manager. The level manager has two modes; edit and open.<br>
	 * In edit mode levels can be renamed and deleted and in open mode level can be opened.
	 * When in open mode the levels are opened in the given {@link LevelCreator}
	 * @param levelCreator The level creator used to open files in
	 * @param mode The mode of the level manager, OPEN or EDIT
	 */
	public LevelManager(LevelCreator levelCreator, int mode) {
		super();
		if (mode == OPEN) {
			setTitle("Open Level");
		} else if(mode == EDIT) {
			setTitle("Level manager");
		} else {
			System.out.println("An unknown mode was given. mode = " + mode);
			JOptionPane.showMessageDialog(this, "An unknown mode was given, could not open", "Error", JOptionPane.WARNING_MESSAGE);
			dispose();
		}
		this.levelCreator = levelCreator;
		
		fileList = new JList(generateFileList());
		fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		JPanel panel = new JPanel(new BorderLayout());
		// If OPEN mode, opening level can be done by dubble clicking, hence the mouse listener
		if (mode == OPEN) {
			fileList.addMouseListener(this);
		}
		JScrollPane scrollPane = new JScrollPane(fileList);

		// Stuff that are only for OPEN mode
		if (mode == OPEN) {
			JPanel buttonPanel = new JPanel(new BorderLayout());
			openButton = new JButton("Open level");
			openButton.addActionListener(this);

			buttonPanel.add(openButton, BorderLayout.CENTER);

			panel.add(scrollPane);
			panel.add(buttonPanel, BorderLayout.SOUTH);

			setSize(200, 300);
		} else { // File handle mode
			JPanel buttonPanel = new JPanel();
			renameButton = new JButton("Rename file");
			deleteButton = new JButton("Delete file");
			renameButton.addActionListener(this);
			deleteButton.addActionListener(this);

			buttonPanel.add(renameButton);
			buttonPanel.add(deleteButton);

			panel.add(scrollPane);
			panel.add(buttonPanel, BorderLayout.SOUTH);

			setSize(200, 350);
		}

		add(panel);
		// Closing this window should not close the program, but only the window
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		centerWindow();
		setVisible(true);
	}

	/**
	 * This method generates a list of all the levels in the Assets/Levels folder
	 * @return an String array of file names.
	 */
	public static String[] generateFileList() {
		if(fileFilter == null) {
			// Filers the list to only include .lvl files
			fileFilter = new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					if (name.endsWith(".lvl")) {
						return true;
					}
					return false;
				}
			};
		}
		
		return new File("Assets/Levels").list(fileFilter);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == openButton) {
			// If the open button was pressed the currently selected level will be opened.
			// More precisly, the level creator class is told to open the level and this window is disposed
			// If no level is selected a warning i shown
			if (!fileList.isSelectionEmpty()) {
				levelCreator.openLevel((String) fileList.getSelectedValue());
				dispose();
			} else {
				JOptionPane.showMessageDialog(this, "Please choose a level to open", "No level selected", JOptionPane.WARNING_MESSAGE);
			}
		} else if (e.getSource() == deleteButton) {
			String fileName = (String) fileList.getSelectedValue();
			int response = JOptionPane.showConfirmDialog(this, "Do you really want to delete " + fileName + "?", "Delete level", JOptionPane.YES_NO_OPTION);
			// If the use want to delete the file
			if (response == 0) {
				if (new File("Assets/Levels/" + fileName).delete()) {
					// Removes the ascii version of the level, if this fails the user is not notified
					// because it's not something he sees. And this will probably not be left in the final release anyway.
					if (!new File("Assets/AsciiLevels/" + fileName.replace(".lvl", ".mrlvl")).delete()) {
						System.out.println("LevelManager: Failed to remove ascii version of level");
					}
					JOptionPane.showMessageDialog(this, "The level " + fileName + " has been deleted!");
					// Updates the list of files
					fileList.setListData(new File("Assets/Levels").list(fileFilter));
				} else {
					JOptionPane.showMessageDialog(this, "The level could not be deleted!");
				}
			}
		} else if (e.getSource() == renameButton) {
			String fileName = (String) fileList.getSelectedValue();
			String response = "";
			while(response.equals("")) {
				response = JOptionPane.showInputDialog(this, "Please enter the new name", "Rename level", JOptionPane.PLAIN_MESSAGE);
				// if the user hit cancel the method does nothing
				if(response == null) {
					return;
				}
				
				// The file name should not contains slash characters.
				if(response.contains("/") || response.contains("\\")) {
					JOptionPane.showMessageDialog(this, "The file name can't contain the characters '/' and '\\'", "Bad file name", JOptionPane.WARNING_MESSAGE);
					response = "";
				}
			}
			// If the given file does not end with .lvl the file extension is added
			if (!response.endsWith(".lvl")) {
				response += ".lvl";
			}
			// If the file path is not in the file name it is added
			if (!response.startsWith("Assets/Levels/"));
			response = "Assets/Levels/" + response;

			// Trys to rename the file.
			if (!new File("Assets/Levels/" + fileName).renameTo(new File(response))) {
				JOptionPane.showMessageDialog(this, "The file could not be renamed", "Rename faild", JOptionPane.WARNING_MESSAGE);
			} else {
				fileList.setListData(new File("Assets/Levels").list(fileFilter));
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// if a level was dubble clicked in the file list that level is opened.
		// This only works in OPEN mode, because a mouse listener is only added in OPEN mode
		if (e.getClickCount() > 1) {
			levelCreator.openLevel((String) fileList.getSelectedValue());
			dispose();
		}
	}
	// <editor-fold defaultstate="collapsed" desc="Unused mouse listeners">
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
	 * This method centers the frame
	 */
	public void centerWindow() {
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
