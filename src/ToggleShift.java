import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import util.UtilFile;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;


public class ToggleShift
{
	
	private static final String[] LIBRARIES = {
		"jinput-dx8.dll",
		"jinput-dx8_64.dll",
		"jinput-raw.dll",
		"jinput-raw_64.dll",
		"jinput-wintab.dll",
		"libjinput-linux.so",
		"libjinput-linux64.so",
		"libjinput-osx.jnilib"
	};
	
	private final int KEY_CODE = KeyEvent.VK_CLOSE_BRACKET;
	private final long REPEAT_DELAY = 100;
	
	/** <code>true</code> if we are holding shift */
	private volatile boolean state;
	private Object lock;
	private Robot robot;
	
	public static void main(String[] args)
	{
		try
		{
			new ToggleShift().run();
		}
		catch (AWTException e)
		{
			e.printStackTrace();
		}
	}
	
	private Controller keyboard;
	
	public ToggleShift() throws AWTException
	{
		state = false;
		lock = new Object();
		robot = new Robot();
		initializeJInput();
	}
	
	public void run()
	{
		
		Thread spamThread = new Thread(){

			@Override
			public void run()
			{
				while (true)
				{
					synchronized(lock)
					{
						try
						{
							lock.wait();
						}
						catch (InterruptedException e)
						{
							e.printStackTrace();
							return;
						}
					}
					
					while (state)
					{
						robot.keyPress(KEY_CODE);
						try
						{
							Thread.sleep(REPEAT_DELAY);
						}
						catch (InterruptedException e)
						{
							e.printStackTrace();
							return;
						}
					}
					
					robot.keyRelease(KEY_CODE);
				}
			}
		};
		spamThread.start();
		
		EventQueue eventQueue = keyboard.getEventQueue();
		Event event = new Event();
		
		while (keyboard.poll())
		{
			while (eventQueue.getNextEvent(event))
			{
				if (event.getValue() == 1.0)
				{
					if (event.getComponent().getIdentifier().equals(Component.Identifier.Key.LSHIFT))
					{
						if (state) // we are holding shift currently
						{
							state = false; // signal our thread to release the key
						}
						else // we are not holding shift currently
						{
							synchronized (lock)
							{
								state = true; // signal our thread to hold the key
								lock.notify(); // wake the thread
							}
						}
						
						robot.keyPress(KeyEvent.VK_INSERT);
					}
				}
			}
			
			try
			{
				Thread.sleep(200);
			}
			catch (InterruptedException e)
			{}
		}
	}
	
	private void initializeJInput()
	{
		// copy libraries into workingdir/lib
		File workingDir = UtilFile.getWorkingDirectory();
		File libDir = new File(workingDir, "lib");
		try
		{
			if (!libDir.exists()) libDir.mkdirs();
			
			for (String library : LIBRARIES)
			{
				File newFile = new File(libDir, library);
				if (!newFile.exists()) UtilFile.streamToFile(UtilFile.getFileInputStream("lib/" + library), newFile);
			}
			
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		
		// set workingdir/lib/ as jinput library path
		System.setProperty("net.java.games.input.librarypath", libDir.getAbsolutePath());
		
		// if windows, then use directinput explicitly (it doesn't require focus)
		String osName = System.getProperty("os.name", "").trim().toLowerCase();
		if (osName.startsWith("windows"))
		{
			System.setProperty("net.java.games.input.useDefaultPlugin", "false");
			System.setProperty("net.java.games.input.plugins", "net.java.games.input.DirectInputEnvironmentPlugin");
		}
		
		
		// get all keyboards
		ControllerEnvironment controllerEnviornment = ControllerEnvironment.getDefaultEnvironment();
		Controller[] controllers = controllerEnviornment.getControllers();
		
		for (int i = 0; i < controllers.length; i++)
		{
			if (controllers[i].getType().equals(Controller.Type.KEYBOARD))
			{
				if (keyboard == null)
					keyboard = controllers[i];
				else
				{
					System.err.println("Multiple keyboards detected");
					break;
				}
			}
		}
		
		if (keyboard == null)
		{
			System.err.println("No keyboards detected");
		}
	}
}
