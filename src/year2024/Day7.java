package year2024;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class Day7
{
	public static void main(String[] args) throws IOException
	{
		//Read Data.
		BufferedReader reader = new BufferedReader(new FileReader("Input/2024/Day7"));
		ArrayList<Equation> equations = new ArrayList<>();
		for (String line : reader.lines().toList())
			equations.add(new Equation(line));
		reader.close();
		
		long start = System.currentTimeMillis();
		//Attempt to solve all equations.
		equations.forEach(Equation::solve);
		
		AtomicLong total = new AtomicLong();
		//Sum up the answers of all the valid equations.
		equations.forEach(
		(equation) ->
		{
			if (equation.validity == Validity.valid)
				total.addAndGet(equation.answer);
		});
		long duration = System.currentTimeMillis() - start;
		System.out.println();
		System.out.println(total.get() + " in " + duration + "ms");
	}
	
	static class Equation
	{
		long answer;
		long[] values;
		Operator[] operators;
		Validity validity;
		
		Equation(String line)
		{
			int separator = line.indexOf(':');
			answer = Long.parseLong(line.substring(0, separator));
			String[] valueStrings = line.substring(separator + 2).split(" ");
			values = new long[valueStrings.length];
			operators = new Operator[valueStrings.length - 1];
			values[0] = Long.parseLong(valueStrings[0]);
			Operator initialOperator = Operator.values()[0];
			for (int index = 0; index < operators.length; index++)
			{
				values[index + 1] = Long.parseLong(valueStrings[index + 1]);
				operators[index] = initialOperator;
			}
			validity = Validity.unchecked;
		}
		
		/**
		 * Determine what the answer to this equation would be with the currently set operators.
		 */
		long evaluate()
		{
			long result = values[0];
			for (int index = 0; index < operators.length; index++)
				result = operators[index].evaluator.evaluate(result, values[index + 1]);
			return result;
		}
		
		/**
		 * Returns whether the equation could be solved into a valid state
		 */
		boolean solve()
		{
			System.out.println("Solving: " + toStringWithoutOperators());
			validity = Validity.valid;
			//Attempt all possible operator combinations looking for one that produces the desired answer.
			while (evaluate() != answer)
			{
				if (incrementOperators())
				{
					validity = Validity.invalid;
					break;
				}
			}
			
			if (validity == Validity.valid)
			{
				System.out.println("         " + this);
				return true;
			}
			else
			{
				System.out.println("         Invalid");
				return false;
			}
		}
		
		/**
		 * Returns true if operator set overflowed back to the initial state
		 */
		boolean incrementOperators()
		{
			boolean carry = true;
			int index = 0;
			while (carry && index < operators.length)
			{
				carry = false;
				int ordinal = operators[index].ordinal();
				ordinal++;
				if (ordinal >= Operator.values().length)
				{
					ordinal = 0;
					carry = true;
				}
				operators[index] = Operator.values()[ordinal];
				index++;
			}
			return carry;
		}
		
		@Override
		public String toString()
		{
			StringBuilder builder = new StringBuilder();
			builder.append(answer).append(" = ").append(values[0]);
			for (int index = 0; index < operators.length; index++)
				builder.append(' ').append(operators[index].chr).append(' ').append(values[index + 1]);
			return builder.toString();
		}
		
		public String toStringWithoutOperators()
		{
			StringBuilder builder = new StringBuilder();
			builder.append(answer).append(" = ").append(values[0]);
			for (int index = 0; index < operators.length; index++)
				builder.append(" ? ").append(values[index + 1]);
			return builder.toString();
		}
	}
	
	enum Validity
	{
		valid,
		invalid,
		unchecked
	}
	
	enum Operator
	{
		add('+', Long::sum),
		concatenate('|', (a, b) -> Long.parseLong("" + a + b)),
		multiply('*', (a, b) -> a * b);
		
		final char chr;
		final Evaluator evaluator;
		
		Operator(char chr, Evaluator evaluator)
		{
			this.chr = chr;
			this.evaluator = evaluator;
		}
		
		interface Evaluator
		{
			long evaluate(long a, long b);
		}
	}
}