package year2024;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Day5
{
	public static void main(String[] args) throws IOException
	{
		Data data = load("Input/2024/Day5");
		
		int total = 0;
		//Sum up the middle pages of all valid updates.
		for (Update update : data.getValidUpdates())
			total += update.middlePage();
		System.out.println(total);
		
		total = 0;
		ArrayList<Update> invalidUpdates = data.getInvalidUpdates();
		//Sum up the middle pages of all invalid updates after fixing them.
		for (Update update : invalidUpdates)
		{
			update.makeCompliantWithAll(data.rules);
			total += update.middlePage();
		}
		System.out.println(total);
	}
	
	public static Data load(String file) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		boolean readingRules = true;
		ArrayList<Rule> rules = new ArrayList<>();
		ArrayList<Update> updates = new ArrayList<>();
		while (reader.ready())
		{
			String line = reader.readLine();
			if (readingRules)
			{
				//While reading the rules, when we encounter an empty line that means we have reached the end of the
				//rules and must begin reading updates.
				if (line.isEmpty())
					readingRules = false;
				else
					rules.add(new Rule(line));
			}
			else
				updates.add(new Update(line));
		}
		reader.close();
		return new Data(rules, updates);
	}
	
	public record Data(ArrayList<Rule> rules, ArrayList<Update> updates)
	{
		ArrayList<Update> getValidUpdates()
		{
			ArrayList<Update> validUpdates = new ArrayList<>();
			for (Update update : updates)
				if (update.isCompliantWithAll(rules))
					validUpdates.add(update);
			return validUpdates;
		}
		
		ArrayList<Update> getInvalidUpdates()
		{
			ArrayList<Update> invalidUpdates = new ArrayList<>();
			for (Update update : updates)
				if (!update.isCompliantWithAll(rules))
					invalidUpdates.add(update);
			return invalidUpdates;
		}
	}
	
	public static class Rule
	{
		final int leader;
		final int follower;
		
		Rule(String input)
		{
			int separator = input.indexOf('|');
			leader = Integer.parseInt(input.substring(0, separator));
			follower = Integer.parseInt(input.substring(separator + 1));
		}
		
		void makeCompliant(Update update)
		{
			int leaderIndex = -1;
			int followerIndex = -1;
			//Find the indecies of the two pages relevant to this rule.
			for (int index = 0; index < update.pages.length; index++)
			{
				if (update.pages[index] == leader)
					leaderIndex = index;
				else if (update.pages[index] == follower)
					followerIndex = index;
			}
			//If we did not find indecies for both pages, or if they are already in proper order, we can stop here.
			if (leaderIndex == -1 || followerIndex == -1 || followerIndex > leaderIndex)
				return;
			
			//If the pages are not in proper order, swap them.
			int leader = update.pages[leaderIndex];
			update.pages[leaderIndex] = update.pages[followerIndex];
			update.pages[followerIndex] = leader;
		}
		
		boolean isCompliant(Update update)
		{
			boolean foundFollower = false;
			//Iterate through this update looking for the pages relevant to this update.
			for (int index = 0; index < update.pages.length; index++)
			{
				//If we find the leading page after finding the following page, the update is not in compliance.
				if (update.pages[index] == follower)
					foundFollower = true;
				else if (update.pages[index] == leader && foundFollower)
					return false;
			}
			return true;
		}
		
		@Override
		public String toString()
		{
			return leader + "|" + follower;
		}
	}
	
	public static class Update
	{
		final int[] pages;
		
		Update(String input)
		{
			String[] pageNumbers = input.split(",");
			pages = new int[pageNumbers.length];
			for (int index = 0; index < pageNumbers.length; index++)
				pages[index] = Integer.parseInt(pageNumbers[index]);
		}
		
		void makeCompliantWithAll(ArrayList<Rule> rules)
		{
			//Since this uses a simple approach, making this update compliant with one rule could inadvertently break
			//compliance with another, so we just continue iterating as many times as we need to reach full compliance.
			while (!isCompliantWithAll(rules))
				for (Rule rule : rules)
					rule.makeCompliant(this);
		}
		
		boolean isCompliantWithAll(ArrayList<Rule> rules)
		{
			for (Rule rule : rules)
				if (!rule.isCompliant(this))
					return false;
			return true;
		}
		
		int middlePage()
		{
			return pages[pages.length / 2];
		}
		
		@Override
		public String toString()
		{
			StringBuilder builder = new StringBuilder();
			for (int index = 0; index < pages.length; index++)
			{
				builder.append(pages[index]);
				if (index < pages.length - 1)
					builder.append(",");
			}
			return builder.toString();
		}
	}
}