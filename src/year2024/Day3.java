package year2024;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day3
{
	public static String test = "xmul(2,4)%&mul[3,7]!@^do_not_mul(5,5)+mul(32,64]then(mul(11,8)mul(8,5))";
	public static String test2 = "xmul(2,4)&mul[3,7]!^don't()_mul(5,5)+mul(32,64](mul(11,8)undo()?mul(8,5))";
	
	public static void main(String[] args) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader("2024Input/Day3"));
		StringBuilder input = new StringBuilder();
		while (reader.ready())
			input.append(reader.readLine()).append("\n");
		reader.close();
		
		//regex pattern: ((mul\(\d{1,3},\d{1,3}\))|(do\(\)))|(don't\(\))
		//regex separates out any valid "mul(#,#)" or "do()" or "don't()"
		Matcher matcher = Pattern.compile("((mul\\(\\d{1,3},\\d{1,3}\\))|(do\\(\\)))|(don't\\(\\))").matcher(input);
		int total = 0;
		boolean enabled = true;
		while (matcher.find())
		{
			String instruction = matcher.group();
			
			if (instruction.equals("do()"))
			{
				System.out.println("enabled");
				enabled = true;
			}
			else if (instruction.equals("don't()"))
			{
				System.out.println("disabled");
				enabled = false;
			}
			else if (enabled)
			{
				System.out.println(instruction);
				int separator = instruction.indexOf(',');
				int num1 = Integer.parseInt(instruction.substring(4, separator));
				int num2 = Integer.parseInt(instruction.substring(separator + 1, instruction.length() - 1));
				System.out.println(" -> " + num1 + " * " + num2);
				total += num1 * num2;
			}
		}
		System.out.println(total);
	}
}