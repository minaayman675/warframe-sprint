import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import util.UtilFile;
import net.java.games.input.Component.Identifier;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;
import net.java.games.input.EventQueue; 

/* TODO:
 * crouch fix (see below)
 * hotkey to temporarily pause operation (so i can type)
 * Do I only need to sprint while WASD is being pressed?
 * hotkey to exit the program
 */

public class ToggleShift
{
	private final static Identifier IDENTIFIER_SPAMFIRE = Identifier.Button._4;
	private final static Identifier IDENTIFIER_TOGGLESPRINT = Identifier.Key.LCONTROL;
	
	private final static int KEYCODE_SPRINT = KeyEvent.VK_CLOSE_BRACKET;
	private final static int KEYCODE_FIRE = InputEvent.BUTTON1_DOWN_MASK;
	private final static long SPRINT_REPEAT_DELAY  = 200;
	private final static long FIRE_REPEAT_DELAY  = 20;
	private final static long POLLING_DELAY =  20;
	
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
	private volatile boolean desiredState = false;
	
	private volatile boolean aiming = false;
	private volatile boolean firing = false;
	private volatile boolean firingSpam = false;
	private volatile boolean running = true;
	private volatile boolean crouching = false;
	
	private Object sprintSpamLock = new Object();
	private Object firingSpamLock = new Object();
	private Robot robot; // Must be initialized in constructor due to Exceptions
	
	private Controller keyboard;
	private Controller mouse;
	
	private static int threadID = 0;
	
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
		robot = new Robot();
		initializeJInput();
	}
	
	public void run()
	{
		new SprintSpamThread("sprint spam thread " + threadID++).start();
		new FiringSpamThread("firing spam thread " + threadID++).start();
		new   KeyboardThread("keyboard thread "    + threadID++).start();
		new      MouseThread("mouse thread "       + threadID++).start();
		
		// Print mouse components:
		//Arrays.stream(mouse.getComponents()).forEach(c -> System.out.println(c.getIdentifier().getName()));
		
//		Runtime.getRuntime().addShutdownHook(new Thread() {
//			@Override
//			public void run()
//			{
//				running = false;
//				
//				synchronized (sprintSpamLock)
//				{
//					sprintSpamLock.notifyAll();
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
	
	private class SprintSpamThread extends Thread
	{
		SprintSpamThread(String name)
		{
			super(name);
		}
		
		@Override
		public void run()
		{
			boolean pressed = false;
			
			while (running)
			{
				synchronized(sprintSpamLock)
				{
					// if we need to not be sprinting
					if (running && (!desiredState || aiming || firing || crouching))
					{
						try
						{
							sprintSpamLock.wait();
						}
						catch (InterruptedException e)
						{
							e.printStackTrace();
							System.exit(1);
						}
					}
				}
				
				// while we need to be sprinting
				while (running && desiredState && !aiming && !firing && !crouching)
				{
					robot.keyRelease(KEYCODE_SPRINT);
					robot.keyPress(KEYCODE_SPRINT);
					pressed = true;
					try
					{
						Thread.sleep(SPRINT_REPEAT_DELAY);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
						System.exit(1);
					}
				}
				
				// if we simulated a press at some point, release the button
				if (pressed)
				{
					robot.keyRelease(KEYCODE_SPRINT);
					pressed = false;
				}
			}
		}
	}
	
	private class FiringSpamThread extends Thread
	{
		FiringSpamThread(String name)
		{
			super(name);
		}
		
		@Override
		public void run()
		{
			boolean pressed = false;
			
			while (running)
			{
				synchronized(firingSpamLock)
				{
					// if we need to not be spamming fire
					if (running && !firingSpam)
					{
						try
						{
							synchronized (sprintSpamLock)
							{
								firing = false;
								sprintSpamLock.notify(); // wake the thread
							}
							
							firingSpamLock.wait();
						}
						catch (InterruptedException e)
						{
							e.printStackTrace();
							System.exit(1);
						}
					}
				}
					
				// while we need to be spamming fire
				while (running && firingSpam)
				{
					firing = true;
					robot.mousePress(KEYCODE_FIRE);
					robot.mouseRelease(KEYCODE_FIRE);
					pressed = true;
					try
					{
						Thread.sleep(FIRE_REPEAT_DELAY);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
						System.exit(1);
					}
				}
				
				// if we simulated a press at some point, release the button
				if (pressed)
				{
					robot.mouseRelease(KEYCODE_FIRE);
					pressed = false;
				}
			}
		}
	}
	
	private class KeyboardThread extends Thread
	{
		KeyboardThread(String name)
		{
			super(name);
		}
		
		@Override
		public void run()
		{
			final EventQueue keyboardEventQueue = keyboard.getEventQueue();
			final Event keyboardEvent = new Event();
			
			while (running && keyboard.poll())
			{
				while (keyboardEventQueue.getNextEvent(keyboardEvent))
				{
					// if the toggle key has been pressed
					if ( keyboardEvent.getComponent().getIdentifier().equals(IDENTIFIER_TOGGLESPRINT)
							&& keyboardEvent.getValue() == 1.0f )
					{
						
						if (desiredState) // we want to be holding the key currently
						{
							desiredState = false; // signal our thread to release the key
							
						}
						else // we are not holding shift currently
						{
							synchronized (sprintSpamLock)
							{
								desiredState = true; // signal our thread to hold the key
								sprintSpamLock.notify(); // wake the thread
							}
						}
					}
					else if (keyboardEvent.getComponent().getIdentifier().equals(Identifier.Key.LSHIFT))
					{
						// if we just pressed crouch
						if (keyboardEvent.getValue() == 1.0f)
						{
							crouching = true;
						}
						else
						{
							crouching = false;
							sprintSpamLock.notify(); // wake the thread
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
					System.exit(1);
				}
			}
		}
	}
	
	private class MouseThread extends Thread
	{
		MouseThread(String name)
		{
			super(name);
		}
		
		@Override
		public void run()
		{
			final EventQueue mouseEventQueue = mouse.getEventQueue();
			final Event mouseEvent = new Event();
			
			while (running && mouse.poll())
			{
				while (mouseEventQueue.getNextEvent(mouseEvent))
				{
					// if the aim button has been pressed
					if ( mouseEvent.getComponent().getIdentifier().equals(Identifier.Button.RIGHT))
					{
						
						if (mouseEvent.getValue() == 1.0f) // m2 was just pressed
						{
							aiming = true;
							
						}
						else // m2 was just released
						{
							synchronized (sprintSpamLock)
							{
								aiming = false;
								sprintSpamLock.notify(); // wake the thread
							}
						}
					}
					// if the fire button has been pressed
					else if (!firingSpam && mouseEvent.getComponent().getIdentifier().equals(Identifier.Button.LEFT))
					{
						
						if (mouseEvent.getValue() == 1.0f) // m1 was just pressed
						{
							firing = true;
						}
						else // m1 was just released
						{
							synchronized (sprintSpamLock)
							{
								firing = false;
								sprintSpamLock.notify(); // wake the thread
							}
						}
					}
					// if the spam fire button has been pressed
					else if (mouseEvent.getComponent().getIdentifier().equals(IDENTIFIER_SPAMFIRE))
					{
						if (mouseEvent.getValue() == 1.0f) // m1 was just pressed
						{	
							synchronized (firingSpamLock)
							{
								firingSpam = true;
								firingSpamLock.notify(); // wake the thread
							}
						}
						else // m4 was just released
						{
							firingSpam = false;
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
					System.exit(1);
				}
			}
		}
	}
}
