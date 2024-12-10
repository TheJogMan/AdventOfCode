package year2024;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Day8
{
	public static void main(String[] args) throws IOException
	{
		Map map = new Map("2024Input/Day8");
		
		int total = map.calculateAntinodes(true);
		
		System.out.println(map);
		System.out.println();
		System.out.println(total);
	}
	
	static class Map
	{
		int size;
		boolean[][] antinodes;
		HashMap<Character, ArrayList<Antenna>> antennae = new HashMap<>();
		
		Map(String file) throws IOException
		{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			size = Integer.parseInt(reader.readLine());
			antinodes = new boolean[size][size];
			for (int y = 0; y < size; y++)
			{
				String line = reader.readLine();
				for (int x = 0; x < size; x++)
				{
					antinodes[y][x] = false;
					char frequency = line.charAt(x);
					if (frequency != '.')
						antennae.computeIfAbsent(frequency, (AntennaFrequency) -> new ArrayList<>()).add(new Antenna(frequency, x, y));
				}
			}
			reader.close();
		}
		
		Antenna getAntennaAt(int x, int y)
		{
			for (ArrayList<Antenna> antennaGroup : antennae.values())
				for (Antenna antenna : antennaGroup)
					if (antenna.x == x && antenna.y == y)
						return antenna;
			return null;
		}
		
		record Antenna(char frequency, int x, int y)
		{
			ArrayList<int[]> calculateAntinodes(Map map, Antenna otherAntenna, boolean useResonantHarmonics)
			{
				ArrayList<int[]> antinodes = new ArrayList<>();
				int dx = (otherAntenna.x - x) * 2;
				int dy = (otherAntenna.y - y) * 2;
				
				int walkX = x + dx;
				int walkY = y + dy;
				int stepCount = 0;
				while (map.inBounds(walkX, walkY) && (stepCount == 0 || useResonantHarmonics))
				{
					antinodes.add(new int[] {walkX, walkY});
					walkX += dx;
					walkY += dy;
					stepCount++;
				}
				if (useResonantHarmonics)
				{
					walkX = x - dx;
					walkY = y - dy;
					while (map.inBounds(walkX, walkY))
					{
						antinodes.add(new int[]{walkX, walkY});
						walkX -= dx;
						walkY -= dy;
					}
				}
				
				return antinodes;
			}
		}
		
		int calculateAntinodes(boolean useResonantHarmonics)
		{
			int total = 0;
			for (ArrayList<Antenna> antennaGroup : antennae.values())
			{
				for (Antenna antenna : antennaGroup) for (Antenna otherAntenna : antennaGroup)
					if (!antenna.equals(otherAntenna))
					{
						if (!this.antinodes[antenna.y][antenna.x])
						{
							total++;
							this.antinodes[antenna.y][antenna.x] = true;
						}
						
						ArrayList<int[]> antinodes = antenna.calculateAntinodes(this, otherAntenna, useResonantHarmonics);
						for (int[] position : antinodes)
							if (inBounds(position[0], position[1]))
								if (!this.antinodes[position[1]][position[0]])
								{
									total++;
									this.antinodes[position[1]][position[0]] = true;
								}
					}
			}
			return total;
		}
		
		int countAntinodes()
		{
			int total = 0;
			for (int y = 0; y < size; y++) for (int x = 0; x < size; x++)
				if (antinodes[y][x])
					total++;
			return total;
		}
		
		boolean inBounds(int x, int y)
		{
			return x >= 0 && x < size && y >= 0 && y < size;
		}
		
		@Override
		public String toString()
		{
			StringBuilder builder = new StringBuilder();
			for (int y = 0; y < size; y++)
			{
				for (int x = 0; x < size; x++)
				{
					Antenna antenna = getAntennaAt(x, y);
					if (antenna != null)
						builder.append(antenna.frequency);
					else if (antinodes[y][x])
						builder.append('#');
					else
						builder.append('.');
				}
				if (y < size - 1)
					builder.append('\n');
			}
			builder.append("\nFrequencies: ");
			Character[] frequencies = antennae.keySet().toArray(new Character[0]);
			for (int index = 0; index < frequencies.length; index++)
			{
				builder.append(frequencies[index]);
				if (index < frequencies.length - 1)
					builder.append(", ");
			}
			return builder.toString();
		}
	}
	
	enum Direction
	{
		north(0, -1),
		south(0, 1),
		east(1, 0),
		west(-1, 0),
		north_west(north, west),
		south_west(south, west),
		north_east(north, east),
		south_east(south, east);
		
		final int dx;
		final int dy;
		
		Direction(int dx, int dy)
		{
			this.dx = dx;
			this.dy = dy;
		}
		
		Direction(Direction dir1, Direction dir2)
		{
			this(dir1.dx + dir2.dx, dir1.dy + dir2.dy);
		}
	}
}