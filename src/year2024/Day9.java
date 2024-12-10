package year2024;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public class Day9
{
	static final String example = "2333133121414131402";
	static final String example2 = "12345";
	
	static ArrayList<Integer> drive = new ArrayList<>();
	
	public static void main(String[] args) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader("2024Input/Day9"));
		String driveMap = reader.readLine();
		reader.close();
		
		load2(driveMap);
		long start = System.currentTimeMillis();
		while (!allMoved())
			shift2();
		long duration = System.currentTimeMillis() - start;
		System.out.println(checkSum2() + " in " + duration + "ms");
	}
	
	static long checkSum()
	{
		long total = 0;
		for (int index = 0; index < drive.size(); index++)
			if (drive.get(index) != -1)
				total += (long)index * drive.get(index);
		return total;
	}
	
	static void shift()
	{
		int emptyIndex = getFirstEmpty();
		int occupiedIndex = getLastOccupied();
		if (emptyIndex != -1 && occupiedIndex != -1)
		{
			int fileID = drive.get(occupiedIndex);
			drive.set(occupiedIndex, -1);
			drive.set(emptyIndex, fileID);
		}
	}
	
	static int getLastOccupied()
	{
		for (int index = drive.size() - 1; index >= 0; index--)
			if (drive.get(index) != -1)
				return index;
		return -1;
	}
	
	static int getFirstEmpty()
	{
		for (int index = 0; index < drive.size(); index++)
			if (drive.get(index) == -1)
				return index;
		return -1;
	}
	
	static boolean isCompact()
	{
		boolean foundEmpty = false;
		for (int block : drive)
		{
			if (!foundEmpty && block == -1)
				foundEmpty = true;
			if (foundEmpty && block != -1)
				return false;
		}
		return true;
	}
	
	static String driveToString()
	{
		StringBuilder builder = new StringBuilder();
		for (int block : drive)
			builder.append(block == -1 ? "." : block);
		return builder.toString();
	}
	
	static void load(String map)
	{
		int fileID = 0;
		boolean isFile = true;
		drive.clear();
		for (int index = 0; index < map.length(); index++)
		{
			int size = Integer.parseInt(map.charAt(index) + "");
			for (int blockNum = 0; blockNum < size; blockNum++)
				drive.add(isFile ? fileID : -1);
			if (isFile)
				fileID++;
			isFile = !isFile;
		}
	}
	
	static LinkedList<Block> drive2 = new LinkedList<>();
	
	static boolean shift2()
	{
		Block block = getLastUnmovedBlock();
		Block space = firstFreeBlock(block.size, block.index);
		if (block == null)
			return false;
		
		if (space == null)
		{
			block.moved = true;
			return false;
		}
		
		int mapIndex = drive2.indexOf(space);
		Block newBlock = new Block(block.fileID, block.size, space.index);
		newBlock.moved = true;
		drive2.add(mapIndex, newBlock);
		space.size -= block.size;
		space.index += block.size;
		block.fileID = -1;
		if (space.size == 0)
			drive2.remove(space);
		return true;
	}
	
	static Block getLastUnmovedBlock()
	{
		for (Iterator<Block> iterator = drive2.descendingIterator(); iterator.hasNext();)
		{
			Block block = iterator.next();
			if (block.fileID != -1 && !block.moved)
				return block;
		}
		return null;
	}
	
	static Block firstFreeBlock(int minimumSize, int indexLimit)
	{
		for (Block block : drive2)
		{
			if (block.fileID == -1 && block.index < indexLimit && block.size >= minimumSize)
				return block;
		}
		return null;
	}
	
	static boolean allMoved()
	{
		for (Block block : drive2)
			if (block.fileID != -1 && !block.moved)
				return false;
		return true;
	}
	
	static long checkSum2()
	{
		long total = 0;
		for (Block block : drive2)
			total += block.checkSum();
		return total;
	}
	
	static String drive2ToString()
	{
		StringBuilder builder = new StringBuilder();
		for (Block block : drive2)
			builder.append(block);
		return builder.toString();
	}
	
	static void load2(String map)
	{
		int fileID = 0;
		boolean isFile = true;
		drive2.clear();
		int driveIndex = 0;
		for (int index = 0; index < map.length(); index++)
		{
			int size = Integer.parseInt(map.charAt(index) + "");
			drive2.add(new Block(isFile ? fileID : -1, size, driveIndex));
			driveIndex += size;
			if (isFile)
				fileID++;
			isFile = !isFile;
		}
	}
	
	static class Block
	{
		int fileID;
		int size;
		int index;
		
		boolean moved = false;
		
		Block(int fileID, int size, int index)
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