package year2024;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Day12
{
	public static void main(String[] args) throws IOException
	{
		Farm farm = new Farm("Input/2024/Day12");
		farm.createRegions();
		farm.calculatePerimeters();
		
		System.out.println(farm.plantTypes.size() + " plant types in " + farm.regions.size() + " regions.");
		System.out.print("Plants:");
		for (Character plantType : farm.plantTypes)
			System.out.print(" " + plantType);
		System.out.println();
		for (Farm.Region region : farm.regions)
			System.out.println(region.origin.plantType + " plants with an area of " + region.area() + " and perimeter of " + region.perimeter + " with " + region.sides() + " sides. Total cost is " + region.totalCost() + " or a discount cost of " + region.discountCost());
		System.out.println("Total cost of the farm is " + farm.totalCost() + " or a discount cost of " + farm.discountCost());
	}
	
	static class Farm
	{
		int size;
		Plot[][] plots;
		HashSet<Character> plantTypes = new HashSet<>();
		ArrayList<Region> regions = new ArrayList<>();
		
		Farm(String input) throws IOException
		{
			BufferedReader reader = new BufferedReader(new FileReader(input));
			int y = 0;
			String line = reader.readLine();
			while (!line.isEmpty())
			{
				//If this is the first line, we use it to determine the size of the grid.
				if (plots == null)
				{
					size = line.length();
					plots = new Plot[size][size];
				}
				
				for (int x = 0; x < size; x++)
				{
					char plantType = line.charAt(x);
					plantTypes.add(plantType);
					plots[y][x] = new Plot(plantType, x, y);
				}
				
				y++;
				if (reader.ready())
					line = reader.readLine();
				else
					line = "";
			}
			reader.close();
		}
		
		int totalCost()
		{
			int total = 0;
			for (Region region : regions)
				total += region.totalCost();
			return total;
		}
		
		int discountCost()
		{
			int total = 0;
			for (Region region : regions)
				total += region.discountCost();
			return total;
		}
		
		void calculateSides()
		{
			regions.forEach(Region::sides);
		}
		
		void calculatePerimeters()
		{
			regions.forEach(Region::perimeter);
		}
		
		void createRegions()
		{
			for (int y = 0; y < size; y++) for (int x = 0; x < size; x++)
			{
				Plot plot = plots[y][x];
				//If this plot is not part of a region, we create a new region.
				//We then expand that region to contain all adjacent plots of the same plant type.
				if (plot.region == null)
				{
					Region region = new Region(plot);
					regions.add(region);
					region.expand(plot);
				}
			}
		}
		
		/**
		 * Gets a plot in this grid.
		 * <p>
		 *     Returns an empty plot for all positions not inside the grid.
		 * </p>
		 */
		Plot plot(int x, int y)
		{
			if (x >= 0 && x < size && y >= 0 && y < size)
				return plots[y][x];
			else
				return new Plot(' ', x, y);
		}
		
		class Plot
		{
			final char plantType;
			final int x, y;
			Region region;
			
			Plot(char plantType, int x, int y)
			{
				this.plantType = plantType;
				this.x = x;
				this.y = y;
			}
			
			@Override
			public boolean equals(Object object)
			{
				//Two plots are equal if they have the same plant type at the same grid position.
				if (object instanceof Plot otherPlot)
					return otherPlot.x == x && otherPlot.y == y && otherPlot.plantType == plantType;
				else
					return false;
			}
		}
		
		class Region
		{
			final Plot origin;
			ArrayList<Plot> plots = new ArrayList<>();
			HashMap<Direction, ArrayList<Plot>> sidePlots = new HashMap<>();
			int perimeter = -1;
			int sides = -1;
			
			Region(Plot origin)
			{
				this.origin = origin;
				origin.region = this;
				plots.add(origin);
			}
			
			void expand(Plot origin)
			{
				//Recursively expand in all 4 cardinal directions to claim all adjacent unclaimed plots of the same
				//plant type.
				for (Direction direction : Direction.values())
				{
					Plot plot = plot(origin.x + direction.dx, origin.y + direction.dy);
					if (plot.plantType == origin.plantType && plot.region == null)
					{
						plot.region = this;
						plots.add(plot);
						expand(plot);
					}
				}
			}
			
			int discountCost()
			{
				return area() * sides();
			}
			
			int totalCost()
			{
				return area() * perimeter();
			}
			
			int area()
			{
				return plots.size();
			}
			
			int sides()
			{
				if (sides == -1)
				{
					sides = 0;
					sidePlots.clear();
					//We iterate over all plots in the region looking for plots along the perimeter
					for (Plot plot : plots)
						for (Direction direction : Direction.values())
						{
							Plot otherPlot = plot(plot.x + direction.dx, plot.y + direction.dy);
							ArrayList<Plot> sidePlots = this.sidePlots.computeIfAbsent(direction, sideDirection -> new ArrayList<>());
							//This plot is on the perimeter if the other plot is outside the region
							//This is also a new side that we need to check if that other plot was not already found to
							//be part of a side in this direction.
							if (!this.equals(otherPlot.region) && !sidePlots.contains(otherPlot))
							{
								sides++;
								sidePlots.add(otherPlot);
								exploreSide(otherPlot, direction);
							}
						}
				}
				return sides;
			}
			
			/**
			 * Walk along the length of a side to determine which plots it runs along.
			 */
			void exploreSide(Plot start, Direction side)
			{
				ArrayList<Plot> sidePlots = this.sidePlots.computeIfAbsent(side, sideDirection -> new ArrayList<>());
				Direction oppositeDirection = side.oppositeDirection();
				//We will walk in both of the two directions that run along this side.
				for (Direction direction : side.perpendicularDirections())
				{
					int x = start.x;
					int y = start.y;
					boolean proceed = true;
					while (proceed)
					{
						x += direction.dx;
						y += direction.dy;
						Plot exterior = plot(x, y);
						Plot interior = plot(x + oppositeDirection.dx, y + oppositeDirection.dy);
						//We continue walking until either the interior plot is no longer inside the region or the
						//exterior plot is no longer outside the region.
						if (!this.equals(exterior.region) && this.equals(interior.region))
							sidePlots.add(exterior);
						else
							proceed = false;
					}
				}
			}
			
			int perimeter()
			{
				if (perimeter == -1)
				{
					//We calculate the perimeter by counting the number of plots not part of this region that are
					//adjacent to the plots in this region.
					perimeter = 0;
					for (Plot plot : plots)
						for (Direction direction : Direction.values())
						{
							Plot otherPlot = plot(plot.x + direction.dx, plot.y + direction.dy);
							if (!this.equals(otherPlot.region))
								perimeter++;
						}
				}
				return perimeter;
			}
		}
	}
	
	enum Direction
	{
		UP(0, -1, new int[] {2, 3}, 1),
		DOWN(0, 1, new int[] {2, 3}, 0),
		LEFT(-1, 0, new int[] {0, 1}, 3),
		RIGHT(1, 0, new int[] {0, 1}, 2);
		
		final int dx;
		final int dy;
		final int[] perpendicularDirectionOrdinals;
		final int oppositeDirectionOrdinal;
		
		Direction(int dx, int dy, int[] perpendicularDirectionOrdinals, int oppositeDirectionOrdinal)
		{
			this.dx = dx;
			this.dy = dy;
			this.perpendicularDirectionOrdinals = perpendicularDirectionOrdinals;
			this.oppositeDirectionOrdinal = oppositeDirectionOrdinal;
		}
		
		Direction oppositeDirection()
		{
			return Direction.values()[oppositeDirectionOrdinal];
		}
		
		Direction[] perpendicularDirections()
		{
			Direction[] perpendicularDirections = new Direction[perpendicularDirectionOrdinals.length];
			Direction[] directions = Direction.values();
			for (int index = 0; index < perpendicularDirections.length; index++)
			{
				perpendicularDirections[index] = directions[perpendicularDirectionOrdinals[index]];
			}
			return perpendicularDirections;
		}
	}
}