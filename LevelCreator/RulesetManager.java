/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package LevelCreator;

import java.io.File;
import java.io.FilenameFilter;

/**
 *
 * @author David
 */
public class RulesetManager {
	private static FilenameFilter fileFilter;	
	
	public static String[] generateFileList() {
		// Filers the list to only include .lvl files
		fileFilter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.endsWith(".rls")) {
					return true;
				}
				return false;
			}
		};

		return new File("Assets/Rulesets").list(fileFilter);
	}
}
