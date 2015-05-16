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
	private final int KEY_CODE = KeyEvent.VK_CLOSE_BRACKET;
	private final long REPEAT_DELAY  = 200;
	private final long POLLING_DELAY =  20;
	
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
	
	/** <code>true</code> if we want to be holding the key */
	private volatile boolean desiredState;
	
	private volatile boolean aiming;
	
	private volatile boolean firing;
	
	private volatile boolean running;
	
	private Object lock;
	private Robot robot;
	
	private Controller keyboard;
	private Controller mouse;
	
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
	
	public ToggleShift() throws AWTException
	{
		desiredState = false;
		aiming = false;
		firing = false;
		running = true;
		
		lock = new Object();
		robot = new Robot();
		
		initializeJInput();
	}
	
	public void run()
	{
		
		Thread spamThread = new Thread("spam thread"){

			@Override
			public void run()
			{
				boolean pressed = false;
				
				while (running)
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
						
					while (running && desiredState && !aiming && !firing)
					{
						robot.keyRelease(KEY_CODE);
						robot.keyPress(KEY_CODE);
						pressed = true;
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
					
					if (pressed)
					{
						robot.keyRelease(KEY_CODE);
						pressed = false;
					}
				}
			}
		};
		spamThread.start();
		
		final EventQueue keyboardEventQueue = keyboard.getEventQueue();
		final Event keyboardEvent = new Event();
		
		final EventQueue mouseEventQueue = mouse.getEventQueue();
		final Event mouseEvent = new Event();
		
		//final Component mouseLeft = mouse.getComponent(Component.Identifier.Button.LEFT);
		//final Component mouseRight = mouse.getComponent(Component.Identifier.Button.RIGHT);
		
		Thread keyboadThread = new Thread("keyboard thread") {
			@Override
			public void run()
			{
				while (running && keyboard.poll())
				{
					while (keyboardEventQueue.getNextEvent(keyboardEvent))
					{
						// if the toggle key has been pressed
						if ( keyboardEvent.getComponent().getIdentifier().equals(Component.Identifier.Key.LSHIFT)
								&& keyboardEvent.getValue() == 1.0f )
						{
							
							if (desiredState) // we want to be holding the key currently
							{
								desiredState = false; // signal our thread to release the key
								
							}
							else // we are not holding shift currently
							{
								synchronized (lock)
								{
									desiredState = true; // signal our thread to hold the key
									lock.notify(); // wake the thread
								}
							}
						}
					}
					
					try
					{
						Thread.sleep(POLLING_DELAY);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
						return;
					}
				}
			}
		};
		keyboadThread.start();
		
		Thread mouseThread = new Thread("mouse thread") {
			@Override
			public void run()
			{
				while (running && mouse.poll())
				{
					while (mouseEventQueue.getNextEvent(mouseEvent))
					{
						// if the aim button has been pressed
						if ( mouseEvent.getComponent().getIdentifier().equals(Component.Identifier.Button.RIGHT))
						{
							
							if (mouseEvent.getValue() == 1.0f) // m2 was just pressed
							{
								aiming = true;
								
							}
							else // m2 was just released
							{
								synchronized (lock)
								{
									aiming = false;
									lock.notify(); // wake the thread
								}
							}
						}
						// if the fire button has been pressed
						else if ( mouseEvent.getComponent().getIdentifier().equals(Component.Identifier.Button.LEFT))
						{
							
							if (mouseEvent.getValue() == 1.0f) // m1 was just pressed
							{
								firing = true;
							}
							else // m1 was just released
							{
								synchronized (lock)
								{
									firing = false;
									lock.notify(); // wake the thread
								}
							}
						}
					}
					
					try
					{
						Thread.sleep(POLLING_DELAY);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
						return;
					}
				}
			}
		};
		mouseThread.start();
		
		
//		Runtime.getRuntime().addShutdownHook(new Thread() {
//			@Override
//			public void run()
//			{
//				running = false;
//				
//				synchronized (lock)
//				{
//					lock.notifyAll();
//				}
//			}
//		});
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
			else if (controllers[i].getType().equals(Controller.Type.MOUSE))
			{
				if (mouse == null)
					mouse = controllers[i];
				else
				{
					System.err.println("Multiple mice detected");
					break;
				}
			}
		}
		
		if (keyboard == null)
		{
			System.err.println("No keyboards detected");
		}
	}
	
	/**
	 * The action to take after this iteration
	 */
	enum Action {
		/** */
		PRESS,
		RELEASE
	}
}
