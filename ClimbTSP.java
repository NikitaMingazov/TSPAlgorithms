import java.io.*;
import java.util.*;
import java.math.*;
import java.time.*;

public class ClimbTSP {
	public static double[][] matrix; // declaring as static because there's only one and every function uses it

	public static void main(String[] args) {
		/*String[] tsp = readFile(args[0]);
		// calculating distances between nodes
		matrix = computeMatrix(tsp);

		for (int i = 300; i <= 1800; i += 300) {
			int plateau = i;
			for (int j = 300; j <= 1800; j += 300) {
				int shuffleMax = j;

				double lengthSum = 0;
				long durationSum = 0;
				int experiment = Integer.parseInt(args[1]);
				for (int k = 0; k < experiment; k++) {
					Instant before = Instant.now(); // time recording begin

					Solution sol = heuristicSolution(); // generate initial solution
					Solution bestSol = tsp(plateau,shuffleMax,sol);

					Instant after = Instant.now(); // time recording end
					long duration = Duration.between(before, after).toMillis();

					lengthSum += bestSol.getLength();
					durationSum += duration;
				}
				System.out.println("P: "+plateau);
				System.out.println("K: "+shuffleMax);
				double ratioP = ((lengthSum/experiment)/278.437);
				System.out.println("Ratio of average: "+roundTo(ratioP,5));
				System.out.println("Average time taken: "+(durationSum/experiment)+" (ms)");
			}		
		}*/
		String[] tsp = readFile(args[0]);
		int plateau = Integer.parseInt(args[1]); // after this many iterations without local improvement, shuffle
		int shuffleMax = Integer.parseInt(args[2]); // after this many shuffles in a row without global improvement, search will end

		// calculating distances between nodes
		matrix = computeMatrix(tsp);

		Instant before = Instant.now(); // time recording begin

		Solution sol = heuristicSolution(); // generate initial solution
		Solution bestSol = tsp(plateau,shuffleMax,sol);

		Instant after = Instant.now(); // time recording end
		long duration = Duration.between(before, after).toMillis();

		System.out.println("Solution: " + Arrays.toString(bestSol.getPath()));
		System.out.println("Solution length: " + roundTo(bestSol.getLength(),3) + " (3dp)");
		System.out.println("Time taken: " + duration + " (ms)");
	}
	// performs hill climbing on a given solution to a selectedd depth until improvement ceases
	public static Solution tsp(int plateau, int shuffleMax, Solution initial) {
		Solution sol = new Solution(initial);
		Solution bestSol = new Solution(sol);
		int p = 0;
		int shuffling = (int)Math.ceil(matrix.length/5)+2; // how many neighbour operations to perform to look for a new candidate solution upon reaching a plateau
		// this variable's proper value is uncertain and likely depends on the specific TSP instance

		while (sol.getCounter() < shuffleMax) { // Improvement *6: Instead of running for N iterations, consider stopping after seeing no improvement 
			Solution newSol = neighbourOf(sol);
			if (newSol.getLength() < sol.getLength() && decimalDifference(sol,newSol)) { // local maximum
				sol = new Solution(newSol); 
				p = 0;
				if (sol.getLength() < bestSol.getLength() && decimalDifference(sol,bestSol)) { // global maximum
					sol.setCounter(0);
					bestSol = new Solution(sol);
				}
			}
			else {
				p++;
				if (p == plateau) { // no progress in the search
					p = 0;
					int temp = sol.getCounter(); // Improvement *4: rather than randomise, perform random segment reversing on global maximum for a new solution
					sol = new Solution(bestSol);
					sol.setCounter(temp);
					sol.shuffle(shuffling); // this function increments counter, global best resets it, so terminate upon K plateaus without improvement
				} // 1-7
			}
		}
		return bestSol;
	}
	// returns a neighbour of the given Solution. x = new(y).neighbour() isn't syntactically correct, so this moves it to form x = neighbourOf(y)
	public static Solution neighbourOf(Solution sol) {
		Solution newSol = new Solution(sol);
		newSol.neighbour();
		return newSol;
	}
	// checks if two solutions have a length difference >= 0.001
	public static boolean decimalDifference(Solution first, Solution second) {
		int firstInt = (int)(first.getLength()*1000);
		int secondInt = (int)(second.getLength()*1000);
		if (firstInt == secondInt) {
			return false;
		}
		else {
			return true;
		}
	}
	// returns a solution according to the closest node heuristic given in spec
	public static Solution heuristicSolution() {
		int n = matrix.length;
		Vector<Integer> unvisited = new Vector<>(); // list of nodes that need to be visited
		for (int i = 1; i < n; i++) {
			unvisited.add(i);
		}
		int[] heurPath = new int[n+1]; // the path the heuristic finds
		heurPath[0] = 0;
		double heurLength = 0; // the distance of the path
		for (int i = 0; i < n-1; i++) {
			double lowest = Double.POSITIVE_INFINITY;
			int next = -1;
			for (int j = 0; j < unvisited.size(); j++) { // finding the closest node
				double dist = matrix[heurPath[i]][unvisited.get(j)];
				if (dist < lowest) {
					lowest = dist;
					next = unvisited.get(j);
				}
			}
			unvisited.remove((Integer)next); // remove closest node and update solution
			heurPath[i+1] = next;
			heurLength += lowest;
		}
		heurPath[n] = 0;
		heurLength += matrix[heurPath[n-1]][heurPath[n]];
		
		return new Solution(heurPath, heurLength);
	}
	public static class Solution {
		private int[] path;
		private double length;
		private int shuffles = 0;
		// initialise a solution with defined values (used for inital solution)
		public Solution (int[] path, double length) {
			this.path = path;
			this.length = length;
		}
		// create a deep copy of another solution
		public Solution (Solution other) {
			this.path = Arrays.copyOf(other.path, other.path.length);
			this.length = other.length;
			this.shuffles = other.shuffles;
		}
		// (Improvement *3: Rather than reverse a segment, swap two randomly selected nodes)
		// morphs this solution into a neighbour by swapping any two nodes
		public void neighbour() {
			int n = matrix.length;
			int first = (int)(Math.random()*(n-2)) + 1; // 1 to n-2
			int second = (int)(Math.random()*(n-2)) + 2; // 2 to n-1
			if (first == second) {
				second++; // if the two nodes are the same, the second is incremented
			}
			
			length -= matrix[path[first - 1]][path[first]]; // subtracting current connections
			length -= matrix[path[first]][path[first + 1]];
			length -= matrix[path[second - 1]][path[second]];
			length -= matrix[path[second]][path[second + 1]];

			int temp = path[second]; // swapping first and second
			path[second] = path[first];
			path[first] = temp;

			length += matrix[path[second - 1]][path[second]]; // adding new connections
			length += matrix[path[second]][path[second + 1]];
			length += matrix[path[first - 1]][path[first]];
			length += matrix[path[first]][path[first + 1]];
		}
		// (Improvement *4: instead of a fully random solution)
		// perform n neighbour operations
		public void shuffle(int n) {
			shuffles++;
			for (int i = 0; i < n; i++) {
				neighbour();
			}
		}
		// (Improvement *6: stopping after seeing no improvement for a large number of consecutive iterations)
		// set shuffle counter 
		public void setCounter(int counter) {
			this.shuffles = counter;
		}
		// return shuffle counter (compared against K for search end)
		public int getCounter() {
			return shuffles;
		}
		public double getLength() {
			return length;
		}
		public int[] getPath() {
			return path;
		}
	}
	// converts tsp instance to distance matrix
	public static double[][] computeMatrix(String[] tsp) { 
		int n = Integer.parseInt(tsp[0]);
		// converting "x y" to [x,y]
		int[][] coords = new int[n][2];
		for (int i = 0; i < n; i++) {
			coords[i] = readLine(tsp, i);
		}
		
		double[][] matrix = new double[n][n];
		for (int i = 0; i < n; i++) {
			int[] iXY = coords[i]; 
			for (int j = 0; j < n; j++) {
				if (i == j) { // 0 when a node is going to itself
					matrix[i][j] = 0;
				}
				else {
					int[] jXY = coords[j];
					int xDiff = iXY[0] - jXY[0];
					int yDiff = iXY[1] - jXY[1];
					matrix[i][j] = Math.sqrt(Math.pow(xDiff, 2) + Math.pow(yDiff, 2)); // Pythagoras' theorem for diagonal distance
				}
			}
		}
		return matrix;
	}
	// reads the given file and returns an array of strings, one for each line
	public static String[] readFile(String filename) {
		LinkedList<String> lines = new LinkedList<String>(); // linked list used for efficient addition and removal (equivalent to a queue in this usage)
		String[] content = new String[0];
		try {
		  File myObj = new File(filename);
		  Scanner myReader = new Scanner(myObj);
		  while (myReader.hasNextLine()) {
		  	lines.add(myReader.nextLine());
		  }
		  int len = lines.size();
		  content = new String[len];
		  for (int i = 0; i < len; i++) {
		  	content[i] = lines.getFirst();
		  	lines.removeFirst();
		  }
		  myReader.close();
		} 
		catch (FileNotFoundException e) {
		  e.printStackTrace();
		}
		return content;
	}
	// reads the given line from the TSP and converts it to int[2], starting from the 2nd line (skips the n# first line)
	public static int[] readLine(String[] tsp, int index) {
		int[] out = new int[2]; // pair of x and y integers
			int i = 0;
			for (String s : tsp[index + 1].split(" ", 0)) {
				out[i] = Integer.parseInt(s);
				i++;
			}
		return out;	
	}
	// rounds number to x decimal points
	public static double roundTo(double input, int points) {
		return ((int)Math.round(input*Math.pow(10,points)))/Math.pow(10,points);
	}
}