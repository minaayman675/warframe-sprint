package net.michaelripley.util;


import java.util.Random;

/**
 * General utilities
 * @author Michael Ripley (<a href="mailto:michael-ripley@utulsa.edu">michael-ripley@utulsa.edu</a>) Apr 1, 2014
 */
public final class Util
{	
	public static final Random rand = new Random();
	
	/**
	 * Do not allow instances of this class to be created
	 */
	private Util()
	{}
	
	/**
	 * Print all currently running thread names
	 */
	public static void debugThreads()
	{
		Thread[] threads = new Thread[Thread.activeCount()];
		Thread.enumerate(threads);
		for (Thread thread : threads)
		{
			System.out.println("> " + thread.getName());
		}
		System.out.println();
	}
	
	/**
	 * Check if assertions are enabled
	 * @return <code>true</code> if assertions are enabled
	 */
	public static boolean assertionsEnabled()
	{
		boolean assertionsEnabled = false;
		assert assertionsEnabled = true; // Intentional side-effect
		// Now assertionsEnabled is set to the correct value
		return assertionsEnabled;
	}
	
	/**
	 * Print an integer array in the form "{0, 1, 2}"
	 * @param array The array to print
	 */
	public static void printIntArray(int[] array)
	{
		System.out.print('{');
		for (int index=0; index<array.length; index++)
		{
			System.out.print(array[index]);
			if (index != array.length - 1)
				System.out.print(", ");
		}
		System.out.println('}');
	}
	
	/**
	 * Print an integer array in the form "{0.00, 1.00, 2.00}"
	 * @param array The array to print
	 */
	public static void printFloatArray(float[] array)
	{
		System.out.print('{');
		for (int index=0; index<array.length; index++)
		{
			System.out.printf("%.2f", array[index]);
			if (index != array.length - 1)
				System.out.print(", ");
		}
		System.out.println('}');
	}
	
	/**
	 * Used to determine whether the user making this call is subject to
	 * teleportations.
	 * @return whether the user making this call is a goat 
	 */
	public static boolean isUserAGoat()
	{
		return false;
	}
	
	/**
	 * Crashes the program if an expression is false
	 * @param assertion expression to evaluate
	 * @throws AssertionError if the condition is false
	 */
	public static void assertTrue(boolean assertion)
	{
		assert assertion;
	}
	
	/**
	 * Crashes the program if an expression is false
	 * @param assertion expression to evaluate
	 * @param message optional message to display when the crash occurs
	 * @throws AssertionError if the condition is false
	 */
	public static void assertTrue(boolean assertion, String message)
	{
		assert assertion : message;
	}
}
