
import java.util.*;
import java.io.*;

public class DBParser {
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
	
	public void doit() throws IOException {
		BufferedReader in = new BufferedReader ( new FileReader("esp.testa.txt") );
		
		String line;
		String longWord = null;
		String longWordType = null;
		int lineIndex = 0;
		String previous = null;
		while ( ( line = in.readLine() ) != null ) {
			lineIndex++;
			line = line.trim();
			if ( line.isEmpty() ) continue;
			String tokens[] = line.split(" ");
			if ( tokens.length != 3 )
				throw new RuntimeException("Found line with 3 tokens: '" + line + "' (Line " + lineIndex + ")");
			
			String word = tokens[0];

			if ( tokens[2].charAt(0) == 'O' ) {
				if ( longWord != null ) {
					foundWord(longWord, longWordType);
					addFreqToChain(previous, longWord);
					previous = longWord;
					longWord = longWordType = null;
				}
				foundWord ( word, tokens[1] );
				addFreqToChain(previous, word);
				previous = word;
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
			addFreqToChain(previous, longWord);
			longWord = longWordType = null;
		}
		
		calcProbabilities();
		
//		System.out.println( "-------- WORDS --------" );
//		System.out.println( "There are " + words.size() + " words" );
//		for ( String word : words.keySet() ) {
//			System.out.print( word + ":" );
//			for ( String type : words.get(word) )
//				System.out.print ( " " + type );
////			System.out.println(freq.get(word));
//			System.out.println();
//		}
		
//		System.out.println( "-------- TYPES --------" );
//		System.out.println( "There are " + types.size() + " types" );
//		for ( String type : types.keySet() ) {
//			System.out.print( type + ":" );
//			for ( String word : types.get(type) )
//				System.out.print ( " " + word );
//			System.out.println();
//		}
		
		in.close();
	}
	
	public static void main(String args[]) throws IOException {
		new DBParser().doit();
	}
}
