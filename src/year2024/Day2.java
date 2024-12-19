package year2024;

import java.io.*;
import java.util.ArrayList;

public class Day2
{
	public static void main(String[] args) throws IOException
	{
		//Read data.
		ArrayList<Report> reports = new ArrayList<>();
		BufferedReader reader = new BufferedReader(new FileReader("Input/2024/Day2"));
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
			if (report.isSafe())
				total++;
		return total;
	}
	
	public static int checkDampened(ArrayList<Report> reports)
	{
		int total = 0;
		for (Report report : reports)
		{
			if (report.isSafe())
				total++;
			else
			{
				//One by one, try removing each level from the report to see if that makes it safe.
				for (int index = 0; index < report.levels.size(); index++)
				{
					Report newReport = new Report(report);
					newReport.removeLevel(index);
					if (newReport.isSafe())
					{
						total++;
						
						//Once we find a safe state, we don't need to continue checking the rest.
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
		
		public boolean isSafe()
		{
			//Take the first level as our initial previous level and begin iterating from the second level.
			Change change = Change.undetermined;
			int previousLevel = levels.getFirst();
			for (int index = 1; index < levels.size(); index++)
			{
				int level = levels.get(index);
				
				//Determine which direction this level changed in.
				Change thisChange;
				if (level > previousLevel)
					thisChange = Change.positive;
				else if (level < previousLevel)
					thisChange = Change.negative;
				else//If there was no change, this level is unsafe.
					return false;
				
				//If direction of change for this record was not yet known, we assume it is the direction this level
				//changed in.
				if (change == Change.undetermined)
					change = thisChange;
				
				//If this level's direction of change does not match that of the record, this level is unsafe.
				if (change != thisChange)
					return false;
				
				//If the level changed by more than 3, this level is unsafe.
				if (Math.abs(level - previousLevel) > 3)
					return false;
				
				previousLevel = level;
			}
			
			//If we have made it to this point, then all levels in this record are safe.
			return true;
		}
		
		enum Change
		{
			positive, negative, undetermined
		}
	}
}