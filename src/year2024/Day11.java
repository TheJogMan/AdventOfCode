package year2024;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Day11
{
	public static void main(String[] args) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader("Input/2024/Day11"));
		String input = reader.readLine();
		reader.close();
		String[] initialStones = input.split(" ");
		long[] initialState = new long[initialStones.length];
		for (int index = 0; index < initialState.length; index++)
			initialState[index] = Long.parseLong(initialStones[index]);
		
		//aoc(initialState, 500);
		graph(initialState, 1000);
	}
	
	static void graph(long[] initialStones, int maxBlinkCount) throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter("executionTimes.csv"));
		writer.write("iterationCount,solution1,solution2");
		writer.newLine();
		
		for (int blinkCount = 1; blinkCount <= maxBlinkCount; blinkCount++)
		{
			cache.clear();
			
			System.out.print(blinkCount + ",");
			writer.write(blinkCount + ",");
			long start = System.currentTimeMillis();
			calculateStoneCount(initialStones, blinkCount);
			long duration = System.currentTimeMillis() - start;
			writer.write(duration + ",");
			System.out.print(duration + ",");
			
			start = System.currentTimeMillis();
			calculateStoneCount2(initialStones, blinkCount);
			duration = System.currentTimeMillis() - start;
			writer.write(duration + "");
			System.out.println(duration);
			if (blinkCount < maxBlinkCount)
				writer.newLine();
		}
		
		writer.close();
	}
	
	static void aoc(long[] initialStones, int blinkCount)
	{
		long start = System.currentTimeMillis();
		long total = calculateStoneCount(initialStones, blinkCount);
		long duration = System.currentTimeMillis() - start;
		System.out.println(total + " " + duration + "ms");
		
		start = System.currentTimeMillis();
		total = calculateStoneCount2(initialStones, blinkCount);
		duration = System.currentTimeMillis() - start;
		System.out.println(total + " " + duration + "ms");
	}
	
	static long calculateStoneCount(long[] initialStones, int blinkCount)
	{
		HashMap<Long, Long> stoneCounts = new HashMap<>();
		for (long stone : initialStones)
			stoneCounts.put(stone, 1L);
		for (int blink = 0; blink < blinkCount; blink++)
		{
			HashMap<Long, Long> newCounts = new HashMap<>();
			for (Map.Entry<Long, Long> stoneCount : stoneCounts.entrySet())
			{
				long stone = stoneCount.getKey();
				long count = stoneCount.getValue();
				String stringValue = String.valueOf(stone);
				
				if (stone == 0)
					newCounts.put(1L, newCounts.computeIfAbsent(1L, (newStone) -> 0L) + count);
				else if (stringValue.length() % 2 == 0)
				{
					int middle = stringValue.length() / 2;
					long left = Long.parseLong(stringValue.substring(0, middle));
					long right = Long.parseLong(stringValue.substring(middle));
					newCounts.put(left, newCounts.computeIfAbsent(left, (newStone) -> 0L) + count);
					newCounts.put(right, newCounts.computeIfAbsent(right, (newStone) -> 0L) + count);
				}
				else
				{
					long newStone = stone * 2024;
					newCounts.put(newStone, newCounts.computeIfAbsent(newStone, (newStoneNumber) -> 0L) + count);
				}
				stoneCounts = newCounts;
			}
		}
		
		long total = 0;
		for (Long count : stoneCounts.values())
			total += count;
		return total;
	}
	
	static HashMap<Integer, HashMap<Long, Long>> cache = new HashMap<>();
	
	static long calculateStoneCount2(long[] initialStones, int blinkCount)
	{
		long total = 0;
		for (long stone : initialStones)
			total += calculateStoneCount(stone, blinkCount);
		return total;
	}
	
	static long calculateStoneCount(long start, int t)
	{
		HashMap<Long, Long> layer = cache.computeIfAbsent(t, blinkNum -> new HashMap());
		return layer.computeIfAbsent(start, s -> calculateValue(s, t));
	}
	
	static long calculateValue(long start, int t)
	{
		if (t == 0)
			return 1;
		String stringValue = String.valueOf(start);
		if (start == 0)
			return calculateStoneCount(1, t - 1);
		else if (stringValue.length() % 2 == 0)
		{
			int middle = stringValue.length() / 2;
			long left = Long.parseLong(stringValue.substring(0, middle));
			long right = Long.parseLong(stringValue.substring(middle));
			return calculateStoneCount(left, t - 1) + calculateStoneCount(right, t - 1);
		}
		else
			return calculateStoneCount(start * 2024, t - 1);
	}
}