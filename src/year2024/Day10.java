package year2024;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Day10
{
	public static void main(String[] args) throws IOException
	{
		Map map = new Map("Input/2024/Day10");
		int[] data = map.trailData();
		for (int index = 0; index < map.trailHeads.size(); index++)
		{
			int[] trailHead = map.trailHeads.get(index );
			System.out.println(trailHead[0] + "," + trailHead[1] + ": " + data[3 + index * 2] + ":" + data[3 + index * 2 + 1]);
		}
		System.out.println(data[0] + " trail heads reaching " + data[1] + " peaks via " + data[2] + " distinct paths");
	}
	
	static class Map
	{
		int size;
		int[][] heights;
		
		ArrayList<int[]> trailHeads;
		
		Map(String file) throws IOException
		{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			trailHeads = new ArrayList<>();
			int y = 0;
			String line = reader.readLine();
			while (!line.isEmpty())
			{
				//If this is the first line then we use it to determine the grid size.
				if (heights == null)
				{
					size = line.length();
					heights = new int[size][size];
				}
				
				for (int x = 0; x < size; x++)
				{
					heights[y][x] = Integer.parseInt(line.charAt(x) + "");
					if (heights[y][x] == 0)
						trailHeads.add(new int[] {x, y});
				}
				
				y++;
				if (reader.ready())
					line = reader.readLine();
				else
					line = "";
			}
			reader.close();
		}
		
		/**
		 * Gets the height at a position in the map.
		 * <p>
		 *     Returns -1 if the given position is not on the map.
		 * </p>
		 */
		int getHeight(int x, int y)
		{
			if (x >= 0 && x < size && y >= 0 && y < size)
				return heights[y][x];
			else
				return -1;
		}
		
		/**
		 * Explores a trail up to it's highest point.
		 * <p>
		 *     0: total number of max height end points that could be reached.
		 *     1: total number of distinct trails leading to those end points.
		 * </p>
		 */
		int[] exploreTrail(int startX, int startY)
		{
			ArrayList<int[]> maxHeightEndings = new ArrayList<>();
			trailStep(startX, startY, maxHeightEndings);
			int trailCount = 0;
			for (int[] ending : maxHeightEndings)
				trailCount += ending[2];
			return new int[] {maxHeightEndings.size(), trailCount};
		}
		
		/**
		 * Gathers data about this trail.
		 * <p>
		 *     Bulk data from index 3 and on is grouped in pairs for each trail head.
		 *     <br><br>
		 *     0: Trail head count.<br>
		 *     1: Map score.<br>
		 *     2: Map rating.<br>
		 *     3-[trail head count]: trail head score, trail head rating.
		 * </p>
		 */
		int[] trailData()
		{
			int totalScore = 0;
			int totalRating = 0;
			int[] data = new int[3 + trailHeads.size() * 2];
			data[0] = trailHeads.size();
			for (int index = 0; index < trailHeads.size(); index++)
			{
				int[] trailHead = trailHeads.get(index);
				int[] result = exploreTrail(trailHead[0], trailHead[1]);
				totalScore += result[0];
				data[3 + index * 2] = result[0];
				totalRating += result[1];
				data[3 + index * 2 + 1] = result[1];
			}
			data[1] = totalScore;
			data[2] = totalRating;
			return data;
		}
		
		/**
		 * Steps through a trail, as part of trail exploration.
		 * @see #exploreTrail(int, int)
		 */
		void trailStep(int startX, int startY, ArrayList<int[]> gatheredPositions)
		{
			int startingHeight = getHeight(startX, startY);
			
			//No trail to explore if we aren't even starting in the map.
			if (startingHeight < 0)
				return;
			
			//This is guaranteed to be the end point if we are starting at max height.
			if (startingHeight == 9)
			{
				//If we have already found this particular peak, increment its trail count.
				//Otherwise, add it to our list as a new peak position.
				int index = getPositionIndex(startX, startY, gatheredPositions);
				if (index == -1)
					gatheredPositions.add(new int[] {startX, startY, 1});
				else
					gatheredPositions.get(index)[2]++;
			}
			
			for (Direction direction : Direction.values())
			{
				int x = startX + direction.dx;
				int y = startY + direction.dy;
				int height = getHeight(x, y);
				
				//The trail only ever goes up, only in increments of one, and doesn't leave the map.
				if (height >= 0 && height - startingHeight == 1)
					trailStep(x, y, gatheredPositions);
			}
		}
		
		/**
		 * Gets the index of a position from a list of positions.
		 * <p>
		 *     Returns -1 if the position is not contained, or it's index if it is.
		 * </p>
		 */
		int getPositionIndex(int positionX, int positionY, ArrayList<int[]> positionList)
		{
			for (int index = 0; index < positionList.size(); index++)
			{
				int[] position = positionList.get(index);
				if (positionX == position[0] && positionY == position[1])
					return index;
			}
			return -1;
		}
	}
	
	enum Direction
	{
		up(0, -1),
		down(0, 1),
		left(-1, 0),
		right(1, 0);
		
		final int dx;
		final int dy;
		
		Direction(int dx, int dy)
		{
			this.dx = dx;
			this.dy = dy;
		}
	}
}