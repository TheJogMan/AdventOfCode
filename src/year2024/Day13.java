package year2024;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Day13
{
	public static void main(String[] args) throws IOException, InterruptedException
	{
		ArrayList<Machine> machines = loadMachines("Input/2024/Day13", true);
		
		long totalCost = 0;
		for (Machine machine : machines)
		{
			System.out.println(machine);
			long[] buttonPresses = machine.determineButtonPresses(false);
			if (buttonPresses == null)
				System.out.println("Can't reach prize!");
			else
			{
				long cost = Machine.determineTokenCost(buttonPresses);
				System.out.println("A: " + buttonPresses[0] + ", B: " + buttonPresses[1] + " - Tokens: " + cost);
				totalCost += cost;
			}
			System.out.println();
		}
		System.out.println("Cost of all obtainable prizes: " + totalCost);
	}
	
	static ArrayList<Machine> loadMachines(String file, boolean adjustForError) throws IOException
	{
		ArrayList<Machine> machines = new ArrayList<>();
		BufferedReader reader = new BufferedReader(new FileReader(file));
		while (reader.ready())
		{
			machines.add(new Machine(reader, adjustForError));
			reader.readLine();
		}
		reader.close();
		return machines;
	}
	
	static class Button
	{
		final long xDelta;
		final long yDelta;
		
		Button(String line)
		{
			int start = line.indexOf('X');
			int end = line.indexOf(',');
			boolean negative = line.charAt(start + 1) == '-';
			xDelta = Long.parseLong(line.substring(start + 2, end)) * (negative ? -1 : 1);
			line = line.substring(end);
			start = line.indexOf('Y');
			negative = line.charAt(start + 1) == '-';
			yDelta = Long.parseLong(line.substring(start + 2)) * (negative ? -1 : 1);
		}
		
		void press(Claw claw)
		{
			bulkPress(claw, 1);
		}
		
		void bulkPress(Claw claw, long pressCount)
		{
			claw.x += xDelta * pressCount;
			claw.y += yDelta * pressCount;
		}
		
		/**
		 * Checks if pressing this button will move the claw towards the location.
		 */
		boolean canReach(Claw claw, long x, long y)
		{
			return ((x < claw.x && xDelta < 0) || (x > claw.x && xDelta > 0))
					&& ((y < claw.y && yDelta < 0) || (y > claw.y && yDelta > 0));
		}
		
		/**
		 * Checks if the claw can be moved to the prize location by only pressing this button.
		 * <p>
		 *     Returns the number of required presses, or -1 if this button alone can not reach the location.
		 * </p>
		 */
		long straitShot(Claw claw, long prizeX, long prizeY)
		{
			long xDiff = prizeX - claw.x;
			long yDiff = prizeY - claw.y;
			
			//Check to make sure this button is even moving in the right direction.
			if (!((xDiff < 0 && xDelta < 0) || (xDiff >= 0 && xDelta >= 0)))
				return -1;
			if (!((yDiff < 0 && yDelta < 0) || (yDiff >= 0 && yDelta >= 0)))
				return -1;
			
			long xPressCount = xDiff / xDelta;
			long yPressCount = yDiff / yDelta;
			
			//Make sure the number of presses will reach the destination on both axis.
			if (xPressCount != yPressCount)
				return -1;
			
			//Make sure we are actually reaching the destination.
			if (xDiff % xDelta != 0 && yDiff % yDelta != 0)
				return -1;
			
			//If all checks have passed, then we have a strait shot!
			return xPressCount;
		}
		
		@Override
		public String toString()
		{
			return "X" + (xDelta < 0 ? '-' : '+') + xDelta + ", Y" + (yDelta < 0 ? '-' : '+') + yDelta;
		}
	}
	
	static class Machine
	{
		final long prizeX;
		final long prizeY;
		final Button buttonA;
		final Button buttonB;
		
		Machine(BufferedReader reader, boolean adjustForError) throws IOException
		{
			buttonA = new Button(reader.readLine());
			buttonB = new Button(reader.readLine());
			String line = reader.readLine();
			int start = line.indexOf('=');
			int end = line.indexOf(',');
			prizeX = Integer.parseInt(line.substring(start + 1, end)) + (adjustForError ? 10000000000000L : 0);
			line = line.substring(end);
			start = line.indexOf('=');
			prizeY = Integer.parseInt(line.substring(start + 1)) + (adjustForError ? 10000000000000L : 0);
		}
		
		/**
		 * Determines the number of times each button must be pressed to reach the prize.
		 * <p>
		 *     {A Presses, B Presses}<br>
		 *     Returns null if the prize can not be reached.
		 * </p>
		 */
		long[] determineButtonPresses(boolean limitTo100Presses)
		{
			Claw claw = new Claw();
			
			/*
			 * Was completely lost and had to get this formula from someone else.
			 * Might be time for me to refresh my knowledge of algebra.
			 */
			long bPressCount = (prizeY * buttonA.xDelta - prizeX * buttonA.yDelta)/ (buttonB.yDelta * buttonA.xDelta - buttonB.xDelta * buttonA.yDelta);
			long aPressCount = (prizeX - bPressCount * buttonB.xDelta) / buttonA.xDelta;
			
			//Make sure our button press counts actually reach the prize
			if (buttonA.xDelta * aPressCount + buttonB.xDelta * bPressCount != prizeX)
				return null;
			if (buttonA.yDelta * aPressCount + buttonB.yDelta * bPressCount != prizeY)
				return null;
			
			//If we are enforcing a limit, make sure we don't exceed it.
			if (limitTo100Presses && (aPressCount > 100 || bPressCount > 100))
				return null;
			
			return new long[] {aPressCount, bPressCount};
		}
		
		/**
		 * Determines the token cost of the button presses to reach the prize.
		 * <p>
		 *     Returns -1 if the prize can't be reached.
		 * </p>
		 */
		long determineMinimumCost(boolean limitTo100Presses)
		{
			long[] buttonPresses = determineButtonPresses(limitTo100Presses);
			if (buttonPresses == null)
				return -1;
			else
				return determineTokenCost(buttonPresses);
		}
		
		/**
		 * Determines the token cost of a number of button presses.
		 * <p>
		 *     {A Presses, B Presses}
		 * </p>
		 */
		static long determineTokenCost(long[] buttonPresses)
		{
			return buttonPresses[1] + 3 * buttonPresses[0];
		}
		
		@Override
		public String toString()
		{
			return "Button A: " + buttonA + "\nButton B: " + buttonB + "\nPrize: X=" + prizeX + ", Y=" + prizeY;
		}
	}
	
	static class Claw
	{
		long x = 0;
		long y = 0;
		
		@Override
		public String toString()
		{
			return x + "," + y;
		}
	}
}