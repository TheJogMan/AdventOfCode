package year2024;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Day4
{
	static int size;
	static String[] puzzle;
	
	public static void main(String[] args) throws IOException
	{
		//Read Data.
		BufferedReader reader = new BufferedReader(new FileReader("Input/2024/Day4"));
		//We get the size of the grid from the first line
		String firstLine = reader.readLine();
		size = firstLine.length();
		puzzle = new String[size];
		puzzle[0] = firstLine;
		//Now we read the remaining lines.
		for (int index = 1; index < size; index++)
			puzzle[index] = reader.readLine();
		reader.close();
		
		int count = 0;
		//Check each template against each position in the grid and count the number of matches.
		for (int x = 0; x < size; x++) for (int y = 0; y < size; y++)
			for (int template = 0; template < xmasTemplates.length; template++)
				if (checkXMASTemplate(template, x, y))
					count++;
		System.out.println(count);
		
		count = 0;
		//Repeat for the second set of templates.
		for (int x = 0; x < size; x++) for (int y = 0; y < size; y++)
			for (int template = 0; template < x_masTemplates.length; template++)
				if (checkX_MASTemplate(template, x, y))
					count++;
		System.out.println(count);
	}
	
	/**
	 * Gets a character from the grid.
	 * <p>
	 *     Returns a space character for any positions not contained in the grid.
	 * </p>
	 */
	public static char getChar(int x, int y)
	{
		if (x >= 0 && x < size && y >= 0 && y < size)
			return puzzle[y].charAt(x);
		else
			return ' ';
	}
	
	public static boolean checkXMASTemplate(int template, int x, int y)
	{
		//Check each character in the template to see if it matches the relative character in the grid.
		//A '.' in the template is a wildcard and will match any character in the grid.
		for (int tx = 0; tx < 4; tx++) for (int ty = 0; ty < 4; ty++)
		{
			char templateChar = xmasTemplates[template][ty].charAt(tx);
			if (templateChar != '.')
			{
				char puzzleChar = getChar(tx + x, ty + y);
				if (puzzleChar != templateChar)
					return false;
			}
		}
		return true;
	}
	
	public static boolean checkX_MASTemplate(int template, int x, int y)
	{
		//Check each character in the template to see if it matches the relative character in the grid.
		//A '.' in the template is a wildcard and will match any character in the grid.
		for (int tx = 0; tx < 3; tx++) for (int ty = 0; ty < 3; ty++)
		{
			char templateChar = x_masTemplates[template][ty].charAt(tx);
			if (templateChar != '.')
			{
				char puzzleChar = getChar(tx + x, ty + y);
				if (puzzleChar != templateChar)
					return false;
			}
		}
		return true;
	}
	
	static final String[][] x_masTemplates = {
			{
					"M.S",
					".A.",
					"M.S"
			},
			{
					"M.M",
					".A.",
					"S.S"
			},
			{
					"S.S",
					".A.",
					"M.M"
			},
			{
					"S.M",
					".A.",
					"S.M"
			},
	};
	
	static final String[][] xmasTemplates = {
			{
					"X...",
					".M..",
					"..A.",
					"...S"
			},
			{
					"...X",
					"..M.",
					".A..",
					"S..."
			},
			{
					"...S",
					"..A.",
					".M..",
					"X..."
			},
			{
					"S...",
					".A..",
					"..M.",
					"...X"
			},
			{
					"X...",
					"M...",
					"A...",
					"S..."
			},
			{
					"S...",
					"A...",
					"M...",
					"X..."
			},
			{
					"XMAS",
					"....",
					"....",
					"...."
			},
			{
					"SAMX",
					"....",
					"....",
					"...."
			},
	};
}