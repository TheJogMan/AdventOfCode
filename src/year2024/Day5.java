package year2024;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Day5
{
	public static void main(String[] args) throws IOException
	{
		Data data = load("2024Input/Day5");
		
		int total = 0;
		for (Update update : data.getValidUpdates())
			total += update.middlePage();
		System.out.println(total);
		total = 0;
		ArrayList<Update> invalidUpdates = data.getInvalidUpdates();
		for (Update update : invalidUpdates)
		{
			update.fix(data.rules);
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
				if (update.validate(rules))
					validUpdates.add(update);
			return validUpdates;
		}
		
		ArrayList<Update> getInvalidUpdates()
		{
			ArrayList<Update> invalidUpdates = new ArrayList<>();
			for (Update update : updates)
				if (!update.validate(rules))
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
		
		void fix(Update update)
		{
			int leaderIndex = -1;
			int followerIndex = -1;
			for (int index = 0; index < update.pages.length; index++)
			{
				if (update.pages[index] == leader)
					leaderIndex = index;
				else if (update.pages[index] == follower)
					followerIndex = index;
			}
			if (leaderIndex == -1 || followerIndex == -1 || followerIndex > leaderIndex)
				return;
			
			int leader = update.pages[leaderIndex];
			update.pages[leaderIndex] = update.pages[followerIndex];
			update.pages[followerIndex] = leader;
		}
		
		boolean check(Update update)
		{
			boolean foundFollower = false;
			for (int index = 0; index < update.pages.length; index++)
			{
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
		
		void fix(ArrayList<Rule> rules)
		{
			while (!validate(rules))
				for (Rule rule : rules)
					rule.fix(this);
		}
		
		boolean validate(ArrayList<Rule> rules)
		{
			for (Rule rule : rules)
				if (!rule.check(this))
				{
					return false;
				}
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