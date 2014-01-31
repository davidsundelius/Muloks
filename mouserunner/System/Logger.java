package mouserunner.System;

import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * A logger utility to be used in release mode to create a 
 * easy-to-read logfile
 * @author David
 */
public class Logger extends PrintStream {
	
	private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss | ");
	
	public Logger() throws IOException {
		super("Assets/Misc/Muloks.log");
	}
	
	@Override
	public void println(String s) {
		s=df.format(Calendar.getInstance().getTime())+s;
		super.println(s);
	}
}
