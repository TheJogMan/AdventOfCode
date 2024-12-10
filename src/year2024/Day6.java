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
		Grid startingGrid = new Grid("2024Input/Day6");
		Grid grid = new Grid(startingGrid);
		System.out.println(grid);
		System.out.println();
		
		while (grid.guard.inBounds())
		{
			grid.guard.step();
		}
		recordObstacleCandidates = false;
		
		System.out.println(grid);
		System.out.println();
		System.out.println(grid.guard.traversedTiles);
		
		long start = System.currentTimeMillis();
		int total = 0;
		for (ObstacleCandidate candidate : obstacleCandidates)
		{
			grid = new Grid(startingGrid);
			grid.setTile(candidate.x, candidate.y, TileType.new_obstacle);
			
			while (grid.guard.inBounds() && !grid.guard.inLoop())
				grid.guard.step();
			if (grid.guard.inLoop())
			{
				total++;
			}
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
			size = Integer.parseInt(reader.readLine());
			grid = new Tile[size][size];
			for (int y = 0; y < size; y++)
			{
				String line = reader.readLine();
				for (int x = 0; x < size; x++)
				{
					char chr = line.charAt(x);
					Direction direction = Direction.getDirection(chr);
					if (direction != null)
					{
						guard = new Guard(this, x, y, direction);
						grid[y][x] = new Tile(TileType.floor);
						startX = x;
						startY = y;
					}
					else
					{
						TileType tile = TileType.getTile(chr);
						if (tile != null)
							grid[y][x] = new Tile(tile);
						else
							throw new RuntimeException("Unexpected character '" + chr + "'");
					}
				}
			}
			reader.close();
		}
		
		TileType getTileType(int x, int y)
		{
			if (x >= 0 && x < size && y >= 0 && y < size)
				return grid[y][x].type;
			else
				return TileType.OOB;
		}
		
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
		
		void step()
		{
			if (grid.getTileType(x, y) != TileType.traversed_floor)
			{
				traversedTiles++;
				if (recordObstacleCandidates && !(x == grid.startX && y == grid.startY))
					obstacleCandidates.add(new ObstacleCandidate(x, y));
			}
			
			Tile tile = grid.getTile(x, y);
			tile.type = TileType.traversed_floor;
			tile.traversedDirections.add(direction);
			x += direction.dx;
			y += direction.dy;
			checkForObstacle();
		}
		
		boolean facingObstacle()
		{
			TileType tile = grid.getTileType(x + direction.dx, y + direction.dy);
			return !tile.traversable;
		}
		
		void checkForObstacle()
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