package year2024;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public class Day9
{
	static ArrayList<Integer> blocks = new ArrayList<>();
	
	public static void main(String[] args) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader("Input/2024/Day9"));
		String driveMap = reader.readLine();
		reader.close();
		
		/*
		loadBlocks(driveMap);
		//Continue shifting blocks until they are maximally compact.
		while (!areBlocksCompact())
			shiftBlocks();
		 */
		
		loadFiles(driveMap);
		long start = System.currentTimeMillis();
		//Continue shifting files until they have all been moved.
		while (!allFilesMoved())
			shiftFile();
		long duration = System.currentTimeMillis() - start;
		System.out.println(filesChecksum() + " in " + duration + "ms");
	}
	
	static long blocksCheckSum()
	{
		long total = 0;
		for (int index = 0; index < blocks.size(); index++)
			if (blocks.get(index) != -1)
				total += (long)index * blocks.get(index);
		return total;
	}
	
	/**
	 * Moves the right most occupied block into the leftmost unoccupied block.
	 */
	static void shiftBlocks()
	{
		int emptyIndex = getLeftMostUnoccupiedBlock();
		int occupiedIndex = getRightMostOccupiedBlock();
		if (emptyIndex != -1 && occupiedIndex != -1)
		{
			int fileID = blocks.get(occupiedIndex);
			blocks.set(occupiedIndex, -1);
			blocks.set(emptyIndex, fileID);
		}
	}
	
	static int getRightMostOccupiedBlock()
	{
		for (int index = blocks.size() - 1; index >= 0; index--)
			if (blocks.get(index) != -1)
				return index;
		return -1;
	}
	
	static int getLeftMostUnoccupiedBlock()
	{
		for (int index = 0; index < blocks.size(); index++)
			if (blocks.get(index) == -1)
				return index;
		return -1;
	}
	
	/**
	 * Checks if there are any unoccupied blocks between occupied blocks.
	 */
	static boolean areBlocksCompact()
	{
		boolean foundEmpty = false;
		for (int block : blocks)
		{
			if (!foundEmpty && block == -1)
				foundEmpty = true;
			if (foundEmpty && block != -1)
				return false;
		}
		return true;
	}
	
	static String blocksToString()
	{
		StringBuilder builder = new StringBuilder();
		for (int block : blocks)
			builder.append(block == -1 ? "." : block);
		return builder.toString();
	}
	
	static void loadBlocks(String map)
	{
		int fileID = 0;
		boolean isFile = true;
		blocks.clear();
		//Read in files, alternating between empty files and filled files.
		for (int index = 0; index < map.length(); index++)
		{
			int size = Integer.parseInt(map.charAt(index) + "");
			for (int blockNum = 0; blockNum < size; blockNum++)
				blocks.add(isFile ? fileID : -1);
			if (isFile)
				fileID++;
			isFile = !isFile;
		}
	}
	
	static LinkedList<File> files = new LinkedList<>();
	
	/**
	 * Moves the right most unmoved file into the left most empty file that is large enough to contain it.
	 * <p>
	 *     If there are no empty files to the left of the unmoved file that are large enough to contain it then the file
	 *     is not moved but still marked as having been moved.
	 * </p>
	 */
	static boolean shiftFile()
	{
		File file = getRightMostUnmovedFile();
		File space = leftMostEmptyFile(file.size, file.index);
		
		//If we have no unmoved files, then we have nothing to move.
		if (file == null)
			return false;
		
		//If there were no empty files to the left of the unmoved file that were large enough to contain it, we can't
		//move it.
		if (space == null)
		{
			file.moved = true;
			return false;
		}
		
		int mapIndex = files.indexOf(space);
		//Create a new file to be the moved file.
		File newFile = new File(file.fileID, file.size, space.index);
		newFile.moved = true;
		files.add(mapIndex, newFile);
		
		//Shrink and shift the empty file to accommodate the new file.
		space.size -= file.size;
		space.index += file.size;
		//If the empty file now has a size of 0, it should be removed from the drive.
		if (space.size == 0)
			files.remove(space);
		
		//Set the old file to be an empty file.
		file.fileID = -1;
		return true;
	}
	
	static File getRightMostUnmovedFile()
	{
		for (Iterator<File> iterator = files.descendingIterator(); iterator.hasNext();)
		{
			File file = iterator.next();
			if (file.fileID != -1 && !file.moved)
				return file;
		}
		return null;
	}
	
	/**
	 * Gets the left most empty file that meets the given requirements.
	 */
	static File leftMostEmptyFile(int minimumSize, int indexLimit)
	{
		for (File file : files)
		{
			if (file.fileID == -1 && file.index < indexLimit && file.size >= minimumSize)
				return file;
		}
		return null;
	}
	
	static boolean allFilesMoved()
	{
		for (File file : files)
			if (file.fileID != -1 && !file.moved)
				return false;
		return true;
	}
	
	static long filesChecksum()
	{
		long total = 0;
		for (File file : files)
			total += file.checkSum();
		return total;
	}
	
	static String filesToString()
	{
		StringBuilder builder = new StringBuilder();
		for (File file : files)
			builder.append(file);
		return builder.toString();
	}
	
	static void loadFiles(String map)
	{
		int fileID = 0;
		boolean isFile = true;
		files.clear();
		int driveIndex = 0;
		//Read in files, alternating between empty files and filled files.
		for (int index = 0; index < map.length(); index++)
		{
			int size = Integer.parseInt(map.charAt(index) + "");
			files.add(new File(isFile ? fileID : -1, size, driveIndex));
			driveIndex += size;
			if (isFile)
				fileID++;
			isFile = !isFile;
		}
	}
	
	static class File
	{
		int fileID;
		int size;
		int index;
		
		boolean moved = false;
		
		File(int fileID, int size, int index)
		{
			this.fileID = fileID;
			this.size = size;
			this.index = index;
		}
		
		@Override
		public String toString()
		{
			return String.valueOf(fileID == -1 ? "." : fileID).repeat(size);
		}
		
		long checkSum()
		{
			if (fileID == -1)
				return 0;
			
			long total = 0;
			for (int blockNum = index; blockNum < index + size; blockNum++)
				total += (long)blockNum * fileID;
			return total;
		}
	}
}