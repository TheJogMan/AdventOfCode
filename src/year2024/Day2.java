package year2024;

import java.io.*;
import java.util.ArrayList;

public class Day2
{
	public static void main(String[] args) throws IOException
	{
		//read data
		ArrayList<Report> reports = new ArrayList<>();
		BufferedReader reader = new BufferedReader(new FileReader("2024Input/Day2"));
		for (String line : reader.lines().toList())
			reports.add(new Report(line));
		reader.close();
		
		System.out.println(checkUndampened(reports));
		System.out.println(checkDampened(reports));
	}
	
	public static int checkUndampened(ArrayList<Report> reports)
	{
		int total = 0;
		for (Report report : reports)
		{
			if (report.isSafe() < 0)
				total++;
		}
		return total;
	}
	
	public static int checkDampened(ArrayList<Report> reports)
	{
		int total = 0;
		for (Report report : reports)
		{
			if (report.isSafe() < 0)
				total++;
			else
			{
				for (int index = 0; index < report.levels.size(); index++)
				{
					Report newReport = new Report(report);
					newReport.removeLevel(index);
					if (newReport.isSafe() < 0)
					{
						total++;
						break;
					}
				}
			}
		}
		return total;
	}
	
	public static class Report
	{
		ArrayList<Integer> levels = new ArrayList<>();
		
		public Report(String line)
		{
			for (String levelString : line.split(" ", 0))
				levels.add(Integer.parseInt(levelString));
		}
		
		public Report(Report source)
		{
			levels.addAll(source.levels);
		}
		
		public void removeLevel(int index)
		{
			levels.remove(index);
		}
		
		public int isSafe()
		{
			Change change = Change.undetermined;
			int previousLevel = levels.getFirst();
			int failureIndex = -1;
			for (int index = 1; index < levels.size(); index++)
			{
				int level = levels.get(index);
				
				Change thisChange = Change.undetermined;
				if (level > previousLevel)
					thisChange = Change.positive;
				else if (level < previousLevel)
					thisChange = Change.negative;
				else
				{
					if (failureIndex == -1)
						failureIndex = index;
				}
				if (change == Change.undetermined)
					change = thisChange;
				
				if (change != thisChange)
				{
					if (failureIndex == -1)
						failureIndex = index;
				}
				
				if (Math.abs(level - previousLevel) > 3)
				{
					if (failureIndex == -1)
						failureIndex = index;
				}
				
				previousLevel = level;
			}
			
			return failureIndex;
		}
		
		enum Change
		{
			positive, negative, undetermined
		}
	}
}