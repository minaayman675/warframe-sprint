package net.michaelripley.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.jar.Manifest;

/**
 * Stream, File, and other I/O related utilities
 * @author Michael Ripley (<a href="mailto:michael-ripley@utulsa.edu">michael-ripley@utulsa.edu</a>) Apr 1, 2014
 */
public final class UtilFile
{	
	private static final boolean inJar = inJarTest();
	private static final String MANIFEST_RELATIVE_PATH = "META-INF/MANIFEST.MF";
	
	/**
	 * Do not allow instances of this class to be created
	 */
	private UtilFile()
	{}
	
	/**
	 * Check if this program is running from within a jar file. Note that this method's
	 * result is cached after the first call as the value never changes.
	 * @return <code>true</code> if this program is running from within a jar file
	 */
	public static boolean inJar()
	{
		return inJar;
	}
	
	/**
	 * Actively check if this program is running from within a jar file
	 * @return <code>true</code> if this program is running from within a jar file
	 */
	private static boolean inJarTest()
	{
		/*
		 * classPath is something like:
		 * jar:file:/C:/Users/michael/workspace/GameCells/export/CELLULAR_AUTOMATA_SIGNED.jar!/gameEngine/Util.class
		 */
		String className = Util.class.getName().replace('.', '/'); // gameEngine.Util
		String classPath = Util.class.getResource("/" + className + ".class").toString();
		return classPath.startsWith("jar:");
	}
	
	/**
	 * A helper method used by {@link #getWorkingDirectory()} to determine what type of OS
	 * the program is running from
	 * @return An integer representing the OS: <ol><li>*nix</li><li>Solaris / SunOS</li>
	 * <li>Windows</li><li>Macintosh</li></ol>
	 */
	private static int getPlatform()
	{
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.contains("linux"))	return 1;
		if (osName.contains("unix"))	return 1;
		if (osName.contains("solaris"))	return 2;
		if (osName.contains("sunos"))	return 2;
		if (osName.contains("win"))		return 3;
		if (osName.contains("mac"))		return 4;
		return 5;
	}
	
	/** The name of the folder that {@link #getWorkingDirectory()} will return */
	private static final String folderName = "zkxsgames";
	
	/**
	 * Get a working directory on the computer's file system that is appropriate for
	 * persisting files in
	 * @return the working directory
	 */
	public static File getWorkingDirectory()
	{
		String userHome = System.getProperty("user.home", ".");
		File workingDirectory;
		switch (getPlatform())
		{
			case 1:
				// fall through
			case 2:
				workingDirectory = new File(userHome, "." + folderName +"/");
				break;
			case 3:
				String applicationData = System.getenv("APPDATA");
				if (applicationData != null)
				{
					workingDirectory = new File(applicationData, "." + folderName + "/");
				}
				else
				{
					workingDirectory = new File(userHome, "." + folderName + "/");
				}
				break;
			case 4:
				workingDirectory = new File(userHome, "Library/Application Support/" + folderName);
				break;
			default:
				workingDirectory = new File(userHome, "." + folderName + "/");
		}
		
		return workingDirectory;
	}
	
	/**
	 * Opens a URL and streams its contents to a file
	 * @param inputName The URL to open
	 * @param outFile The file to stream to
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static void streamToFile(String inputName, File outFile) throws IOException, URISyntaxException
	{
		streamToFile(getFileInputStream(inputName), outFile);
	}
	
	/**
	 * Streams an InputStream to a file. Note that this closes the InputStream when done.
	 * This implementation requires Java 7 or newer to compile
	 * @param in The input stream
	 * @param outFile The file to stream to
	 * @throws IOException
	 */
	public static void streamToFile(InputStream in, File outFile) throws IOException
	{
		Util.assertTrue(in != null);
		
		try
		{
			Files.copy(in, outFile.toPath());
		}
		finally
		{
			in.close();
		}
	}
	
	/**
	 * Streams an InputStream to a file. Note that this closes the InputStream when done.
	 * This implementation will work on most JRE versions
	 * @param in The input stream
	 * @param outFile The file to stream to
	 * @throws IOException
	 */
	public static void streamToFileLegacy(InputStream in, File outFile) throws IOException
	{
		OutputStream out = null;
		
		try
		{
			Util.assertTrue(in != null);
			
			out = new FileOutputStream(outFile);
			byte[] buffer = new byte[1024];
			int bytesRead;
			
			while ((bytesRead = in.read(buffer)) != -1)
			{
				out.write(buffer, 0, bytesRead);
			}
		}
		finally // close streams
		{
			if (in != null)
			{
				try
				{
					in.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			if (out != null)
			{
				try
				{
					out.flush();
					out.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Copy one file to another. Note that if the destination file exists, it will
	 * be overwritten.
	 * @param sourceFile The source file
	 * @param destFile The destination file
	 * @throws IOException
	 */
	public static void copyFile(File sourceFile, File destFile) throws IOException
	{
		if (!destFile.exists())
		{
			destFile.createNewFile();
		}
		
		FileChannel source = null;
		FileChannel destination = null;
		
		try
		{
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		}
		finally
		{
			try
			{
				if (source != null)
				{
					source.close();
				}
			}
			finally // because someone thought it would be a good idea to only allow one finally per try
			{
				if (destination != null)
				{
					destination.close();
				}
			}
		}
	}
	
	/**
	 * Get the manifest of this jar file
	 * @return The manifest object
	 * @throws URISyntaxException
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static Manifest getManifest() throws URISyntaxException, MalformedURLException, IOException
	{
		if (inJar())
		{
			// these next two lines are exactly the same in #inJar()
			String className = Util.class.getName().replace('.', '/');
			String classPath = Util.class.getResource("/" + className + ".class").toString();
			
			String manifestPath = classPath.substring(0, classPath.lastIndexOf("!/") + 2) + MANIFEST_RELATIVE_PATH;
			
			InputStream in = new URL(manifestPath).openStream();
			Manifest manifest = new Manifest(in);
			try
			{
				in.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
			return manifest;
		}
		else // if we are NOT in a jar file
		{
			/*
			 * This is an interesting case: we are trying to get the manifest but we are
			 * not in a jar file. One appropriate result would be to simply return null.
			 * However, it is useful in development to have the ability to run the program
			 * normally while it is not in a jar file. To do so, we must be able to attempt
			 * to open the "META-INF/MANIFEST.MF" file from the root of the project. 
			 * Therefore, we will attempt to do this below.
			 */
			
			InputStream in = getFileInputStream(MANIFEST_RELATIVE_PATH);
			if (in == null) return null;
			
			Manifest manifest = new Manifest(in);
			
			try
			{
				in.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
			return manifest;
		}	
	}
	
	/**
	 * Gets the InputStream of a file given its relative path.
	 * This expects the resources to be relative to the root of the src folder.
	 * HOLY CRAP. Finding out how to do this was a tremendous pain in the ass.
	 * @param url Path to file
	 * @return The InputStream of a file
	 */
	public static InputStream getFileInputStream(String url)
	{
		InputStream in = Util.class.getResourceAsStream("/" + url);
		return in;
	}
	
	/**
	 * Get a URL object given a URL relative to the project's root
	 * @param relativePath
	 * @return a URL object representing the given string
	 */
	public static URL getFileURL(String relativePath)
	{
		URL url = Util.class.getResource("/" + relativePath);
		Util.assertTrue(url != null, "Could not find file " + url);
		return url;
	}
	
	/**
	 * Computes the SHA-256 checksum of the file at the given path
	 * @param filepath A path to the file to checksum
	 * @return the SHA-256 checksum of the file
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws NoSuchAlgorithmException 
	 */
	public static byte[] checksum(String filepath) throws IOException, URISyntaxException, NoSuchAlgorithmException
	{
		InputStream in = getFileInputStream(filepath);
		byte[] result;
		
		try
		{
			result = checksum(in);
		}
		finally
		{
			in.close();
		}
		
		return result;
	}
	
	/**
	 * Computes the SHA-256 checksum of the stream. This does NOT close the
	 * @param in the stream to checksum
	 * @return the SHA-256 checksum of the stream
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public static byte[] checksum(InputStream in) throws IOException, NoSuchAlgorithmException
	{
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		
		byte[] buffer = new byte[1024];
		int bytesRead;
		
		while ((bytesRead = in.read(buffer)) != -1)
		{
			digest.update(buffer, 0, bytesRead);
		}
		
		return digest.digest();
	}
	
	/**
	 * Computes the SHA-256 checksum of the given file
	 * @param file The file to checksum
	 * @return the SHA-256 checksum of the file
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws NoSuchAlgorithmException 
	 */
	public static byte[] checksum(File file) throws IOException, NoSuchAlgorithmException
	{
		FileInputStream in = new FileInputStream(file);
		
		byte[] result;
		
		try
		{
			result = checksum(in);
		}
		finally
		{
			in.close();
		}
		
		return result;
	}
}
