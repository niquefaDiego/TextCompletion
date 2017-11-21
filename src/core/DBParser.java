package core;


import java.util.*;
import java.util.Map.Entry;

import javafx.util.Pair;

import java.io.*;

public class DBParser
{
	private static DBParser instance;
	private static final Object lock = new Object();
	
	public static DBParser getInstance() {
		synchronized(lock) {
			if ( instance == null ) {
				instance = new DBParser();
			}
			return instance;
		}
	}
	public final double A = 0.5;
	public final double B = 0.5;
	
	Map<String, Set<String> > words = new TreeMap<String, Set<String>>();
	Map<String, Set<String> > types = new TreeMap<String, Set<String>>();
	Map<String, Integer> freq = new TreeMap<String, Integer>();
	Map<String, Map<String, Double>> markovChain = new TreeMap<String, Map<String, Double>>();
	
	public void foundWord ( String word, String type ) {
		word = word.toLowerCase();
//		System.out.println("word = " + word + ", type = " + type);
		if ( !words.containsKey(word) )
			words.put ( word, new TreeSet<String>() );
		if ( !types.containsKey(type) )
			types.put( type, new TreeSet<String>() );
		if (!freq.containsKey(word)) 
			freq.put(word, 0);
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
	
	private void calcProbabilities() {
		for (Map<String, Double> chainRow: markovChain.values()) {
			int total = 0;
			for(double value: chainRow.values()) {
				total += value;
			}
			for(String key: chainRow.keySet()) {
					chainRow.put(key, chainRow.get(key)/total);
			}
		}
	}
	
	private DBParser() {
		BufferedReader in;
		
		try { in = new BufferedReader ( new FileReader("esp.train.txt") ); }
		catch ( IOException e ) { throw new RuntimeException(); }
		
		String line;
		String longWord = null;
		String longWordType = null;
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
			
			String word = tokens[0];

			if ( tokens[2].charAt(0) == 'O' ) {
				if ( longWord != null ) {
					foundWord(longWord, longWordType);
					addFreqToChain(previousType, longWordType);
					previousType = longWordType;
					longWord = longWordType = null;
				}
				foundWord ( word, tokens[1] );
				addFreqToChain(previousType, tokens[1]);
				previousType = tokens[1];
			}
			else if ( tokens[2].charAt(0) == 'I' ) {
				longWord += " " + word;
			}
			else if ( tokens[2].charAt(0) == 'B' ){
				longWord = word;
				longWordType = tokens[2];
			} else {
				throw new RuntimeException ( "Invalid 3rd token (Line " + lineIndex + ")" );
			}
		}
		
		if ( longWord != null ) {
			foundWord ( longWord, longWordType );
			addFreqToChain(previousType, longWordType);
			longWord = longWordType = null;
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
//		for ( String type : types.keySet() ) {
//			System.out.print( type + ":" );
//			for ( String word : types.get(type) )
//				System.out.print ( " " + word );
//			System.out.println();
//		}
		
		try {in.close();}
		catch ( IOException e ) { e.printStackTrace(); }
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
	
	public synchronized List<String> getPredictions(String prevWord, String current, int maxPredictions) {
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

}
