package year2024;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class Day6
{
	static ArrayList<ObstacleCandidate> obstacleCandidates = new ArrayList<>();
	static boolean recordObstacleCandidates = true;
	
	public static void main(String[] args) throws IOException
	{
		Grid startingGrid = new Grid("Input/2024/Day6");
		Grid grid = new Grid(startingGrid);
		System.out.println(grid);
		System.out.println();
		
		//Continue processing the guard until they leave the grid.
		//In this first run we will be recording all positions where we could potentially place a new obstacle to change
		//the guard's path.
		while (grid.guard.inBounds())
			grid.guard.step();
		recordObstacleCandidates = false;
		
		System.out.println(grid);
		System.out.println();
		System.out.println(grid.guard.traversedTiles);
		
		long start = System.currentTimeMillis();
		int total = 0;
		//Iterate through all our candidate positions to determine which one's result in a looped path.
		for (ObstacleCandidate candidate : obstacleCandidates)
		{
			grid = new Grid(startingGrid);
			grid.setTile(candidate.x, candidate.y, TileType.new_obstacle);
			
			//Continue processing the guard until they either leave the grid or get stuck in a loop.
			while (grid.guard.inBounds() && !grid.guard.inLoop())
				grid.guard.step();
			
			if (grid.guard.inLoop())
				total++;
		}
		long duration = System.currentTimeMillis() - start;
		System.out.println(total + " " + duration);
	}
	
	static class Grid
	{
		int size;
		Tile[][] grid;
		Guard guard;
		int startX;
		int startY;
		
		Grid(Grid source)
		{
			size = source.size;
			grid = new Tile[size][size];
			startX = source.startX;
			startY = source.startY;
			guard = new Guard(this, source.guard.x, source.guard.y, source.guard.direction);
			guard.traversedTiles = source.guard.traversedTiles;
			
			for (int y = 0; y < size; y++) for (int x = 0; x < size; x++)
			{
				grid[y][x] = new Tile(source.grid[y][x]);
			}
		}
		
		Grid(String sourceFile) throws IOException
		{
			BufferedReader reader = new BufferedReader(new FileReader(sourceFile));
			int y = 0;
			String line = reader.readLine();
			while (!line.isEmpty())
			{
				//If this is the first line, we use it to determine the size of the grid.
				if (grid == null)
				{
					size = line.length();
					grid = new Tile[size][size];
				}
				
				for (int x = 0; x < size; x++)
				{
					char chr = line.charAt(x);
					Direction direction = Direction.getDirection(chr);
					//If this tile specifies a direction, it is where the guard will spawn, facing in that direction.
					if (direction != null)
					{
						//The actual tile at this position will be a floor tile.
						grid[y][x] = new Tile(TileType.floor);
						
						guard = new Guard(this, x, y, direction);
						startX = x;
						startY = y;
					}
					else
					{
						//Otherwise we determine which tile type will be at this position.
						TileType tile = TileType.getTile(chr);
						if (tile != null)
							grid[y][x] = new Tile(tile);
						else
							throw new RuntimeException("Unexpected character '" + chr + "'");
					}
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
		 * Gets the type of a tile in the grid.
		 * <p>
		 *     Returns the out of bounds tile type for positions not in the grid.
		 * </p>
		 */
		TileType getTileType(int x, int y)
		{
			if (x >= 0 && x < size && y >= 0 && y < size)
				return grid[y][x].type;
			else
				return TileType.OOB;
		}
		
		/**
		 * Gets a tile in the grid.
		 * <p>
		 *     Returns an out of bounds tile for positions not in the grid.
		 * </p>
		 */
		Tile getTile(int x, int y)
		{
			if (x >= 0 && x < size && y >= 0 && y < size)
				return grid[y][x];
			else
				return new Tile(TileType.OOB);
		}
		
		void setTile(int x, int y, TileType tile)
		{
			if (x >= 0 && x < size && y >= 0 && y < size)
				grid[y][x].type = tile;
		}
		
		@Override
		public String toString()
		{
			StringBuilder builder = new StringBuilder();
			for (int y = 0; y < size; y++)
			{
				for (int x = 0; x < size; x++)
				{
					if (x == guard.x && y == guard.y)
						builder.append(guard.direction.chr);
					else
						builder.append(grid[y][x].type.chr);
				}
				if (y < size - 1)
					builder.append("\n");
			}
			return builder.toString();
		}
	}
	
	static class Guard
	{
		int x;
		int y;
		Direction direction;
		Grid grid;
		
		int traversedTiles = 0;
		
		Guard(Grid grid, int x, int y, Direction direction)
		{
			this.x = x;
			this.y = y;
			this.direction = direction;
			this.grid = grid;
		}
		
		/**
		 * Advances the guard forward one step.
		 *
		 */
		void step()
		{
			if (grid.getTileType(x, y) != TileType.traversed_floor)
			{
				//If we have not yet traversed this tile, we increment our count of traversed tiles.
				traversedTiles++;
				
				//If this isn't the guard's spawn position, then we also record this as an obstacle candidate position
				//if recording is currently enabled.
				if (recordObstacleCandidates && !(x == grid.startX && y == grid.startY))
					obstacleCandidates.add(new ObstacleCandidate(x, y));
			}
			
			Tile tile = grid.getTile(x, y);
			//Update the type of this tile, so we can know that we have already been here if we return in the future.
			tile.type = TileType.traversed_floor;
			//We also record which direction are facing in, so we can determine if we are in a looped path.
			tile.traversedDirections.add(direction);
			//Move the guard forward in the direction they are currently facing.
			x += direction.dx;
			y += direction.dy;
			
			turnAwayFromObstacle();
		}
		
		boolean facingObstacle()
		{
			TileType tile = grid.getTileType(x + direction.dx, y + direction.dy);
			return !tile.traversable;
		}
		
		/**
		 * Rotates the guard until they are not facing an obstacle.
		 * <p>
		 *     This will do nothing if the guard is already not facing an obstacle.
		 * </p>
		 */
		void turnAwayFromObstacle()
		{
			while (facingObstacle())
			{
				TileType tile = grid.getTileType(x + direction.dx, y + direction.dy);
				if (!tile.traversable)
					direction = direction.nextDirection();
			}
		}
		
		boolean inLoop()
		{
			//We are in a looped path if we have already passed through our current position in the same direction that
			//we are currently facing.
			Tile tile = grid.getTile(x, y);
			return tile.traversed() && tile.checkDirection(direction);
		}
		
		boolean inBounds()
		{
			return grid.getTileType(x, y).inBounds;
		}
	}
	
	record ObstacleCandidate(int x, int y)
	{
	
	}
	
	static class Tile
	{
		TileType type;
		HashSet<Direction> traversedDirections = new HashSet<>();
		
		Tile(TileType type)
		{
			this.type = type;
		}
		
		Tile(Tile tile)
		{
			this.type = tile.type;
			this.traversedDirections.addAll(tile.traversedDirections);
		}
		
		/**
		 * Checks if this tile has been traversed in a given direction.
		 */
		boolean checkDirection(Direction direction)
		{
			return traversedDirections.contains(direction);
		}
		
		boolean traversed()
		{
			return type == TileType.traversed_floor;
		}
	}
	
	enum TileType
	{
		floor(true, true, '.'),
		traversed_floor(true, true, 'X'),
		obstacle(false, true, '#'),
		new_obstacle(false, true, 'O'),
		OOB(true, false, '@');
		
		final boolean traversable;
		final boolean inBounds;
		final char chr;
		
		TileType(boolean traversable, boolean inBounds, char chr)
		{
			this.traversable = traversable;
			this.inBounds = inBounds;
			this.chr = chr;
		}
		
		static TileType getTile(char chr)
		{
			for (TileType tile : TileType.values())
				if (tile.chr == chr)
					return tile;
			return null;
		}
	}
	
	enum Direction
	{
		left(3, -1, 0, '<'),
		down(0, 0, 1, 'v'),
		right(1, 1, 0, '>'),
		up(2, 0, -1, '^');
		
		final int nextDirection;
		final int dx;
		final int dy;
		final char chr;
		
		Direction(int nextDirection, int dx, int dy, char chr)
		{
			this.nextDirection = nextDirection;
			this.dx = dx;
			this.dy = dy;
			this.chr = chr;
		}
		
		static Direction getDirection(char chr)
		{
			for (Direction direction : Direction.values())
				if (direction.chr == chr)
					return direction;
			return null;
		}
		
		Direction nextDirection()
		{
			return Direction.values()[nextDirection];
		}
	}
}