package core;


import java.util.*;
import java.util.Map.Entry;

import javafx.util.Pair;

import java.io.*;

public class DBParser
{
	private static DBParser instance;
	private static final Object lock = new Object();
	private static Map<String,String> typeConversions = new HashMap<String,String>();
	
	static {
		typeConversions.put("P0","PN");
	}
	
	public static DBParser getInstance() {
		synchronized(lock) {
			if ( instance == null ) {
				instance = new DBParser();
			}
			return instance;
		}
	}
	public final double A = 0.5;
	public final double B = 1;
	
	Map<String, Set<String> > words = new TreeMap<String, Set<String>>();
	Map<String, Set<String> > types = new TreeMap<String, Set<String>>();
	Map<String, Double> freq = new TreeMap<String, Double>();
	Map<String, Map<String, Double>> markovChain = new TreeMap<String, Map<String, Double>>();
	
	public void foundWord ( String word, String type ) {
		word = word.toLowerCase();
//		System.out.println("word = " + word + ", type = " + type);
		if ( !words.containsKey(word) )
			words.put ( word, new TreeSet<String>() );
		if ( !types.containsKey(type) )
			types.put( type, new TreeSet<String>() );
		if (!freq.containsKey(word)) 
			freq.put(word, 0.0);
		words.get(word).add(type);
		types.get(type).add(word);
		freq.put(word, freq.get(word) + 1);
	}
	
	public void addFreqToChain(String previous, String current) {
		if (previous == null || current == null) return;
		if (!markovChain.containsKey(previous))
			markovChain.put(previous, new TreeMap<String, Double>());
		Map<String, Double> chainRow = markovChain.get(previous);
		if (!chainRow.containsKey(current))
			chainRow.put(current, 0.0);
		chainRow.put(current, chainRow.get(current) + 1);
	}
	
	private void calcProbabilities()
	{
		BufferedWriter out = null;
		try { out = new BufferedWriter ( new FileWriter("MarkovChain.txt") ); }
		catch (IOException e) { e.printStackTrace(); }

		for (Map.Entry<String,Map<String,Double>> entry: markovChain.entrySet()) {
			
			boolean debug = Tags.getName(entry.getKey()).equals("Determinante Exclamativo");
			Map<String, Double> chainRow = entry.getValue();
			double total = 0;
			for(double value: chainRow.values()) {
				total += value;
			}
			
			double outProb = 0;
			for(Map.Entry<String,Double> rowEntry: chainRow.entrySet()) {
				//if ( debug ) System.out.println( "debug -> " + rowEntry );
				rowEntry.setValue( rowEntry.getValue()/total );
				outProb += rowEntry.getValue();
				try {
					out.write( Tags.getName( entry.getKey() ) + " -> "
							+ Tags.getName(rowEntry.getKey()) + " (" + (rowEntry.getValue()) + ")\n" );
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			System.out.println ( "Out prob of " + entry.getKey() + " = " + outProb );
		}
		
		if ( out != null )
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		for ( Map.Entry<String,Double> entry : freq.entrySet() )
			entry.setValue( entry.getValue() / (double) freq.size() );
	}
	
	private DBParser()
	{
		String fileNames[] = new String[] { "esp.testa.txt", "esp.testb.txt", "esp.train.txt" };
		
		for ( String file : fileNames ) { 
			BufferedReader in;
			try { in = new BufferedReader ( new FileReader(file) ); }
			catch ( IOException e ) { throw new RuntimeException(); }
			
			String line;
			int lineIndex = 0;
			String previousType = null;
			while ( true ) {
				lineIndex++;
				
				try { line = in.readLine(); }
				catch ( IOException e ) { throw new RuntimeException("Failed to read line " + lineIndex); }
				if ( line == null ) break;
				
				line = line.trim();
				if ( line.isEmpty() ) continue;
				String tokens[] = line.split(" ");
				if ( tokens.length != 3 )
					throw new RuntimeException("Found line with 3 tokens: '" + line + "' (Line " + lineIndex + ")");
				
				String word = tokens[0].toLowerCase();
				
				tokens[1] = typeConversions.getOrDefault(tokens[1], tokens[1]);
				
				foundWord ( word, tokens[1] );
				addFreqToChain(previousType, tokens[1]);
				previousType = tokens[1];
			}
						
			try {in.close();}
			catch ( IOException e ) { e.printStackTrace(); }
		}
		
		calcProbabilities();
		
//		System.out.println( "-------- WORDS --------" );
		System.out.println( "There are " + words.size() + " words" );
//		for ( String word : words.keySet() ) {
//			System.out.print( word + ":" );
//			for ( String type : words.get(word) )
//				System.out.print ( " " + type );
////			System.out.println(freq.get(word));
//			System.out.println();
//		}
		
//		System.out.println( "-------- TYPES --------" );
		System.out.println( "There are " + types.size() + " types" );
		int unknownCount = 0;
		for ( String type : types.keySet() ) {
			System.out.print( type );
			if ( Tags.getName(type) == null ) unknownCount++;
			else System.out.print( " (" + Tags.getName(type) + ")");
			System.out.print(":");
			int counter = 0;
			for ( String word : types.get(type) ) {
				System.out.print ( " " + word );
				if ( counter++ > 20 ) break;
			}
			System.out.println();
		}
		System.out.println("There are " + unknownCount + " unknown types");

	}
	
	private double getHighestMarkovChainValue(String prevWord, String word) {
		double ans = 0.0;
		if ( prevWord == null || word == null ) return 0.0;
		if (!words.containsKey(prevWord) || !words.containsKey(word)) return 0.0;
		for(String typePrevWord: words.get(prevWord)) {
			for(String typeWord: words.get(word)) {
				if (markovChain.containsKey(typePrevWord) && markovChain.get(typePrevWord).containsKey(typeWord)) {
					ans = Math.max(ans, markovChain.get(typePrevWord).get(typeWord));
				}
			}
		}
		return ans;
	}
	
	private double calcUtility(String prevWord, String word) {
		return A*freq.get(word) + B*getHighestMarkovChainValue(prevWord, word);
	}
	
	public String getType(String word) {
		if ( word == null ) return null;
		word = word.toLowerCase();
		if ( word.matches("[0-9]+") ) return Tags.NUMBER_TYPE;
		Set<String> types = words.getOrDefault(word, null);;
		if ( types == null ) return null;
		return types.iterator().next();
	}
	
	public synchronized List<String> getPredictions(String prevWord, String current, int maxPredictions) {
		prevWord = prevWord == null ? null : prevWord.toLowerCase();
		current = current == null ? null : current.toLowerCase();
		List<String> predictions = new ArrayList<String>();
		List<Pair<Double, String>> possible = new ArrayList<Pair<Double, String>>();
		for(Entry<String, Set<String>> entry: words.entrySet()) {
			String word = entry.getKey();
			if (word.startsWith(current)) {
				double utility = calcUtility(prevWord, word);
				possible.add(new Pair(utility, word));
			}
		}
		possible.sort(new Comparator<Pair>() {
			   @Override
			   public int compare(Pair p1, Pair p2) {
			       return ((Double) (p1.getKey())).compareTo((Double)p2.getKey());
			   }
			});
		if (possible.size() > 0) {
			int id = possible.size()-1;
			while(maxPredictions > 0 && id >= 0) {
				//System.out.println(possible.get(id).getValue());
				predictions.add(possible.get(id).getValue());
				maxPredictions --;
				id --;
			}
		}
		return predictions;
	}

	public String getExpectedType(String previousWordType, String currentWord) {
		System.out.println( "getExpected(" + previousWordType + ","+ currentWord + ")");
		if ( previousWordType == null ) return null;
		Map<String, Double> edges = markovChain.getOrDefault(previousWordType, null);
		if ( edges == null ) return null;
		String best = null;
		double bestProb = -1;
		for ( Map.Entry<String, Double> entry : edges.entrySet() )
			if ( entry.getValue() > bestProb ) {
				bestProb = entry.getValue();
				best = entry.getKey();
			}
		return best;
	}

}
