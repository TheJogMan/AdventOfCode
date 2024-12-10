package year2024;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Day1
{
	static ArrayList<Integer> list1 = new ArrayList<>();
	static ArrayList<Integer> list2 = new ArrayList<>();
	static HashMap<Integer, Integer> counts = new HashMap<>();
	
	public static void main(String[] args) throws IOException
	{
		readLists();
		part1();
		part2();
	}
	
	public static void part1()
	{
		sortLists();
		int totalDistance = 0;
		for (int index = 0; index < list1.size(); index++)
		{
			int num1 = list1.get(index);
			int num2 = list2.get(index);
			int distance = Math.abs(num1 - num2);
			totalDistance += distance;
		}
		System.out.println(totalDistance);
	}
	
	public static void part2()
	{
		countOccurrences();
		
		int total = 0;
		for (Integer value : list1)
		{
			if (counts.containsKey(value))
				total += value * counts.get(value);
		}
		System.out.println(total);
	}
	
	public static void countOccurrences()
	{
		for (Integer value : list2)
		{
			if (counts.containsKey(value))
				counts.put(value, counts.get(value) + 1);
			else
				counts.put(value, 1);
		}
	}
	
	public static void sortLists()
	{
		list1.sort(Integer::compareTo);
		list2.sort(Integer::compareTo);
	}
	
	public static void readLists() throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader("2024Input/year2024.Day1"));
		
		while (reader.ready())
		{
			String line = reader.readLine();
			list1.add(Integer.parseInt(line.substring(0, 5)));
			list2.add(Integer.parseInt(line.substring(8)));
		}
		reader.close();
	}
}