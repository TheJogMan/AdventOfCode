package year2024;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/*
 * Comparing the performance of my approach against that used by CS Jackie
 * https://www.youtube.com/watch?v=q--CgC1yUO4
 */

public class Day5HashMap
{
	static final String inputFile = "Input/2024/Day5";
	
	public static void main(String[] args) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		HashMap<Integer, HashMap<Integer, Character>> rules = readRules(reader);
		ArrayList<int[]> sequences = readSequences(reader);
		reader.close();
		
		long start = System.currentTimeMillis();
		checkSequences(sequences, rules);
		long duration = System.currentTimeMillis() - start;
		System.out.println("HashMap approach takes " + duration + "ms");
		
		Day5.Data data = Day5.load(inputFile);
		start = System.currentTimeMillis();
		int total = 0;
		for (Day5.Update update : data.getValidUpdates())
			total += update.middlePage();
		duration = System.currentTimeMillis() - start;
		System.out.println("My approach takes " + duration + "ms");
	}
	
	static boolean checkSequence(int[] sequence, HashMap<Integer, HashMap<Integer, Character>> rules)
	{
		for (int pageIndex = 0; pageIndex < sequence.length; pageIndex++)
		{
			int page = sequence[pageIndex];
			for (int index = 0; index < pageIndex; index++)
			{
				int beforePage = sequence[index];
				if (rules.computeIfAbsent(page, pageNumber -> new HashMap<>()).get(beforePage) != 'b')
					return false;
			}
			for (int index = pageIndex + 1; index < sequence.length; index++)
			{
				int afterPage = sequence[index];
				if (rules.computeIfAbsent(page, pageNumber -> new HashMap<>()).get(afterPage) != 'a')
					return false;
			}
		}
		return true;
	}
	
	static int getMiddlePage(int[] sequence)
	{
		return sequence[sequence.length / 2];
	}
	
	static int checkSequences(ArrayList<int[]> sequences, HashMap<Integer, HashMap<Integer, Character>> rules)
	{
		int middleSum = 0;
		for (int[] sequence : sequences)
			if (checkSequence(sequence, rules))
				middleSum += getMiddlePage(sequence);
		return middleSum;
	}
	
	static HashMap<Integer, HashMap<Integer, Character>> readRules(BufferedReader reader) throws IOException
	{
		HashMap<Integer, HashMap<Integer, Character>> rules = new HashMap<>();
		
		String line = reader.readLine();
		while (!line.isEmpty())
		{
			int separator = line.indexOf('|');
			int before = Integer.parseInt(line.substring(0, separator));
			int after = Integer.parseInt(line.substring(separator + 1));
			
			rules.computeIfAbsent(before, page -> new HashMap<>()).put(after, 'a');
			rules.computeIfAbsent(after, page -> new HashMap<>()).put(before, 'b');
			
			line = reader.readLine();
		}
		
		return rules;
	}
	
	static ArrayList<int[]> readSequences(BufferedReader reader) throws IOException
	{
		ArrayList<int[]> sequences = new ArrayList<>();
		while (reader.ready())
		{
			String line = reader.readLine();
			String[] pageNumbers = line.split(",");
			int[] pages = new int[pageNumbers.length];
			for (int index = 0; index < pageNumbers.length; index++)
				pages[index] = Integer.parseInt(pageNumbers[index]);
			sequences.add(pages);
		}
		return sequences;
	}
}