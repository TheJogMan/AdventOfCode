package year2024;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day3
{
	public static void main(String[] args) throws IOException
	{
		//Read data.
		BufferedReader reader = new BufferedReader(new FileReader("Input/2024/Day3"));
		StringBuilder input = new StringBuilder();
		while (reader.ready())
			input.append(reader.readLine()).append("\n");
		reader.close();
		
		//Regex pattern: ((mul\(\d{1,3},\d{1,3}\))|(do\(\)))|(don't\(\))
		//We will need to insert a bunch of escape characters to be able to represent the pattern in a string to pass it
		//to the pattern compiler.
		//Regex separates out any valid "mul(#,#)" or "do()" or "don't()"
		Matcher matcher = Pattern.compile("((mul\\(\\d{1,3},\\d{1,3}\\))|(do\\(\\)))|(don't\\(\\))").matcher(input);
		int total = 0;
		//We begin with our mul instructions enabled.
		boolean enabled = true;
		//Continue stepping through the input data until our regex matcher can no longer find more valid instructions.
		while (matcher.find())
		{
			String instruction = matcher.group();
			
			if (instruction.equals("do()"))
				enabled = true;
			else if (instruction.equals("don't()"))
				enabled = false;
			else if (enabled)
			{
				//If this instruction wasn't do or don't then it must be mul.
				int separator = instruction.indexOf(',');
				int num1 = Integer.parseInt(instruction.substring(4, separator));
				int num2 = Integer.parseInt(instruction.substring(separator + 1, instruction.length() - 1));
				total += num1 * num2;
			}
		}
		System.out.println(total);
	}
}