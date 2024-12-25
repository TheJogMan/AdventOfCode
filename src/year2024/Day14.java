package year2024;

import java.io.*;
import java.util.ArrayList;

public class Day14
{
	public static void main(String[] args) throws IOException
	{
		Room room = new Room("Input/2024/Day14");
		//getSafetyFactor(room, 100);
		
		//We must first identify the pattern we are looking for.
		//identifyPatterns(room);
		
		/*
		 * Finding the pattern based on lowest entropy does not guarantee that we found it at it's earliest emergence.
		 * However, now that we know the pattern we can use it to find it's absolute earliest emergence.
		 */
		searchForPattern(room, "Input/2024/Day14Template");
	}
	
	static void getSafetyFactor(Room room, int seconds)
	{
		room.simulateFor(seconds);
		System.out.println("Safety factor at " + seconds + " seconds: " + room.calculateSafetyFactor());
	}
	
	static void searchForPattern(Room room, String templateFile) throws IOException
	{
		Template template = new Template(templateFile);
		
		//Room width multiplied by room height should result in the amount of time after which the room loops back to
		//it's initial state. Simulating up to that time guarantees we see all there is to be seen, and simulating any
		//further would be redundant.
		int duration = room.width * room.height;
		for (int second = 0; second <= duration; second++)
		{
			if (template.containsPattern(room))
				System.out.println("Template found at " + second + " seconds.");
			room.stepAll();
		}
	}
	
	static class Template
	{
		final int width;
		final int height;
		final boolean[][] cells;
		
		Template(String file) throws IOException
		{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			//Read the template size from the first line.
			int[] dimensions = parseIntegerPair(reader.readLine());
			width = dimensions[0];
			height = dimensions[1];
			cells = new boolean[height][width];
			
			//Read the actual template
			int y = 0;
			String line = reader.readLine();
			while (!line.isEmpty())
			{
				for (int x = 0; x < width; x++)
					cells[y][x] = line.charAt(x) == 'X';
				y++;
				if (reader.ready())
					line = reader.readLine();
				else
					line = "";
			}
		}
		
		boolean containsPattern(Room room)
		{
			for (int y = 0; y < room.height; y++) for (int x = 0; x < room.width; x++)
				if (templateAtPosition(room, x, y))
					return true;
			return false;
		}
		
		boolean templateAtPosition(Room room, int x, int y)
		{
			for (int templateY = 0; templateY < width; templateY++) for (int templateX = 0; templateX < width; templateX++)
			{
				boolean hasBot = room.countRobotsAtPosition(x + templateX, y + templateY) != 0;
				if (hasBot != cells[templateY][templateX])
					return false;
			}
			return true;
		}
	}
	
	static void identifyPatterns(Room room) throws IOException
	{
		/*
		 * I once again needed to look for help to figure out how to solve part 2 on this one.
		 * Essentially we will try simulating every possible configuration the room could be in, and manually inspect
		 * them to look for the pattern. They will also be sorted by their entropy score so that we can inspect the
		 * most likely candidates first. The entropy score will be calculated with a simple flood fill to measure the
		 * size of a contiguous empty space, because the presence of a structured pattern should significantly reduce
		 * the size of that contiguous empty space.
		 *
		 * We may have instances where the flood fill gets cut off early by random chance despite having not structured
		 * pattern, meaning we will have some configurations with a very low entropy score despite having no pattern,
		 * and this is why we will ultimately rely on manual inspection of the output.
		 *
		 * However, for any instances where the entropy score was below a minimum value, we will retry the flood fill at
		 * a new position.
		 */
		
		ArrayList<Map> maps = new ArrayList<>();
		//Room width multiplied by room height should result in the amount of time after which the room loops back to
		//it's initial state. Simulating up to that time guarantees we see all there is to be seen, and simulating any
		//further would be redundant.
		int duration = room.width * room.height;
		//Step forward through time fur the full duration, capturing a snapshot of the room for every second.
		System.out.println("Simulating for " + duration + " seconds.");
		maps.add(new Map(room, 0));
		for (int second = 1; second <= duration; second++)
		{
			room.stepAll();
			maps.add(new Map(room, second));
		}
		
		System.out.println("Sorting...");
		//Sort the snapshots based on their entropy score, because rooms with lower entropy are more likely to have our
		//pattern.
		maps.sort(null);
		
		System.out.println("Regurgitating...");
		//Regurgitate our snapshots to an output file for manual inspection.
		BufferedWriter writer = new BufferedWriter(new FileWriter("Robots.txt"));
		for (Map map : maps)
		{
			writer.write(map.toString());
			writer.newLine();
			writer.newLine();
		}
		writer.close();
		System.out.println("Complete!");
	}
	
	/**
	 * Represents a map of locations where at least one robot could be found in a room at a given timestamp
	 */
	static class Map implements Comparable<Map>
	{
		final int width;
		final int height;
		final int timestamp;
		final byte[][] cells;
		
		int entropyScore = -1;
		
		Map(Room room, int timestamp)
		{
			this.timestamp = timestamp;
			this.width = room.width;
			this.height = room.height;
			cells = new byte[height][width];
			
			for (int y = 0; y < height; y++) for (int x = 0; x < width; x++)
				cells[y][x] = room.countRobotsAtPosition(x, y) != 0 ? (byte)1 : 0;
		}
		
		boolean botAt(int x, int y)
		{
			if (inMap(x, y))
				return cells[y][x] == 1;
			else
				return false;
		}
		
		boolean emptyAt(int x, int y)
		{
			if (inMap(x, y))
				return cells[y][x] == 0;
			else
				return false;
		}
		
		boolean inMap(int x, int y)
		{
			return x >= 0 && x < width && y >= 0 && y < height;
		}
		
		int entropyScore()
		{
			final int lowerLimit = 5;
			//Entropy scores below a minimum limit imply a premature cutoff and should be retried.
			//This also inherently ensures we actually calculate the score the first time it is requested.
			while (entropyScore < lowerLimit)
			{
				//Find our first empty cell to start the flood fill from.
				int startX = -1;
				int startY = 0;
				for (int y = 0; y < height; y++)
				{
					for (int x = 0; x < width; x++)
					{
						if (cells[y][x] == 0)
						{
							startX = x;
							startY = y;
							break;
						}
					}
					if (startX != -1)
						break;
				}
				
				//Perform our flood fill to measure the size of the contiguous empty space.
				entropyScore = floodFill(startX, startY);
				
				//If we don't pass our lower limit, we need to prepare to try again
				if (entropyScore < lowerLimit)
				{
					//Reset our flood fill.
					//Resetting to 5 instead of 0 will ensure we don't attempt to flood fill the same region again.
					for (int y = 0; y < height; y++)
						for (int x = 0; x < width; x++)
							if (cells[y][x] == 2)
								cells[y][x] = 5;
				}
			}
			
			//The empty space in the map will be fully contiguous more often than not, so less contiguous space means
			//lower entropy.
			return entropyScore;
		}
		
		/**
		 * Perform a flood fill from a given position, counting the number of filled cells.
		 */
		int floodFill(int startX, int startY)
		{
			int found = 0;
			if (cells[startY][startX] == 0)
			{
				cells[startY][startX] = 3;
				found = 1;
			}
			
			int total = 0;
			while (found > 0)
			{
				total += found;
				found = 0;
				for (int y = 0; y < height; y++) for (int x = 0; x < width; x++)
				{
					if (cells[y][x] == 3)
					{
						cells[y][x] = 2;
						
						if (emptyAt(x - 1, y))
						{
							cells[y][x - 1] = 3;
							found++;
						}
						if (emptyAt(x + 1, y))
						{
							cells[y][x + 1] = 3;
							found++;
						}
						if (emptyAt(x, y - 1))
						{
							cells[y - 1][x] = 3;
							found++;
						}
						if (emptyAt(x, y + 1))
						{
							cells[y + 1][x] = 3;
							found++;
						}
					}
				}
			}
			return total;
		}
		
		@Override
		public String toString()
		{
			StringBuilder builder = new StringBuilder();
			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{
					if (botAt(x, y))
						builder.append('X');
					else if (emptyAt(x, y))
						builder.append('.');
					else
						builder.append('*');
				}
				if (y < height - 1)
					builder.append("\n");
			}
			return builder.toString();
		}
		
		@Override
		public int compareTo(Map otherMap)
		{
			return entropyScore() - otherMap.entropyScore();
		}
	}
	
	static class Room
	{
		final int width;
		final int height;
		ArrayList<Robot> robots = new ArrayList<>();
		
		Room(String file) throws IOException
		{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			//Read the room size from the first line.
			int[] dimensions = parseIntegerPair(reader.readLine());
			width = dimensions[0];
			height = dimensions[1];
			
			//Read in robots.
			String line = reader.readLine();
			while (line != null && !line.isEmpty())
			{
				robots.add(new Robot(this, line));
				line = reader.readLine();
			}
			reader.close();
		}
		
		int calculateSafetyFactor()
		{
			int[] quadrantCounts = new int[5];
			
			for (Robot robot : robots)
				quadrantCounts[robot.quadrant()]++;
			
			return quadrantCounts[1] * quadrantCounts[2] * quadrantCounts[3] * quadrantCounts[4];
		}
		
		void simulateFor(int seconds)
		{
			for (int second = 0; second < seconds; second++)
				stepAll();
		}
		
		void stepAll()
		{
			robots.forEach(Robot::step);
		}
		
		int countRobotsAtPosition(int x, int y)
		{
			int total = 0;
			for (Robot robot : robots)
				if (robot.x == x && robot.y == y)
					total++;
			return total;
		}
	}
	
	static int[] parseIntegerPair(String pair)
	{
		int separator = pair.indexOf(',');
		int x = Integer.parseInt(pair.substring(0, separator));
		int y = Integer.parseInt(pair.substring(separator + 1));
		return new int[] {x, y};
	}
	
	static class Robot
	{
		final Room room;
		int x;
		int y;
		final int velocityX;
		final int velocityY;
		
		Robot(Room room, String line)
		{
			this.room = room;
			
			int start = line.indexOf('=');
			int end = line.indexOf(' ');
			int[] pair = parseIntegerPair(line.substring(start + 1, end));
			x = pair[0];
			y = pair[1];
			
			line = line.substring(end);
			start = line.indexOf('=');
			pair = parseIntegerPair(line.substring(start + 1));
			velocityX = pair[0];
			velocityY = pair[1];
		}
		
		/**
		 * Determines which quadrant of the room the robot is in.
		 * <p>
		 *     Returns 0 if the robot is on a middle axis.
		 * </p>
		 */
		int quadrant()
		{
			int middleX = room.width / 2;
			int middleY = room.height / 2;
			
			if (x < middleX)
			{
				if (y < middleY)
					return 1;
				else if (y > middleY)
					return 3;
				else
					return 0;
			}
			else if (x > middleX)
			{
				if (y < middleY)
					return 2;
				else if (y > middleY)
					return 4;
				else
					return 0;
			}
			else
				return 0;
		}
		
		void step()
		{
			x += velocityX;
			y += velocityY;
			
			if (x < 0)
				x += room.width;
			else if (x >= room.width)
				x -= room.width;
			
			if (y < 0)
				y += room.height;
			else if (y >= room.height)
				y -= room.height;
		}
	}
}