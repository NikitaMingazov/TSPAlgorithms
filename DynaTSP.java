import java.io.*;
import java.util.*;
import java.math.*;
import java.time.*;

public class DynaTSP { // global variables for fewer recursion variables
	public static boolean[] nodes; // true = visited, false = open
	public static double[][] matrix; // distance between two points
	public static byte size; // number of open nodes
	public static HashMap<Integer, Pair<Double, Byte>> memo = new HashMap<>(); // memoisation hash table

	public static void main(String[] args) {
		String[] tsp = readFile(args[0]);
		int n = Integer.parseInt(tsp[0]);
		
		matrix = computeMatrix(tsp); // initialising global variables
		nodes = new boolean[n]; 
		size = (byte)(n-1); 

		Instant before = Instant.now(); // time recording begin

		double length = tsp((byte)0);

		Byte step = memo.get(hash(nodes,0)).second; // fetch solution from the memoisation map
		Stack<Byte> steps = new Stack<>();
		steps.push(step);
		for (int i = 1; i < n - 1; i++) {
			nodes[step] = true;
			step = memo.get(hash(nodes,step)).second;
			steps.push(step);
		}

		Instant after = Instant.now(); // time recording end
		long duration = Duration.between(before, after).toMillis();

		String solution = "[0, "; // moving solution from Stack to String
		while (!steps.empty()) {
			solution += steps.pop() + ", ";
		}
		solution += "0]";

		System.out.println("Solution: " + solution);
		System.out.println("Solution length: " + roundTo(length,3)+" (3dp)");
		System.out.println("Time taken: " + duration + " (ms)");
	}
	// recursive TSP algorithm for f(I,j) [I is omitted from tsp() but is static and works the same way on tsp(bool[]* I, int j)]
	public static double tsp(byte dest) {
		Integer key = hash(nodes,dest); // I and j from the problem definition
		if (memo.containsKey(key)) { // returning memoised solution
			return memo.get(key).first;
		}
		if (size == 0) { // base case, used to use Vector<int>, later moved to bool[] hence this name
			memo.put(key, new Pair<Double,Byte>(matrix[0][dest],(byte)0));
			return matrix[0][dest];
		}
		byte prev = -1; // last node in the shortest path to dest
		double minimum = Double.MAX_VALUE; 
		for (byte i = 1; i < matrix.length; i++) { // iterate through each open node in nodes, removing it and calling tsp with j = item
			if (!nodes[i]) {
				nodes[i] = true;
				size--;
				double cur = tsp(i) + matrix[i][dest]; // recursion call
				if (cur < minimum) { // values to memoise
					minimum = cur;
					prev = i;
				}
				nodes[i] = false;
				size++;
			}
		}
		Pair<Double, Byte> result = new Pair<>(minimum,prev);
		memo.put(key, result);
		return minimum;
	}
	// returns a unique hash for the hashmap key
	public static Integer hash(boolean[] nodes, int j) {
		Integer hash = 0;
		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i]) {
				hash |= (1 << i); // [set bit at i to 1] x = x | (2^i)
			}
	    }
		hash += j * (int)Math.pow(10,8);
		return hash;
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
		int[] out = new int[2]; // I'd have used a pair for this but int[2] is more convenient
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
	public static class Pair<X, Y> { // copied from StackOverflow because Java doesn't have this functionality
		public final X first; 
		public final Y second; 
		public Pair(X x, Y y) { 
			this.first = x; 
			this.second = y; 
		}
	} 
}