package core;

import java.util.Map;
import java.util.TreeMap;

public class Tags {

	private static Map<Character,String> adjectives;
	private static Map<Character,String> adverbs;
	private static Map<Character, String> blocks;
	private static Map<Character,String> conjunctions;
	private static Map<Character,String> determiners;
	private static Map<Character,String> names;
	private static Map<String,String> punctuation;
	private static Map<Character,String> pronouns;
	private static Map<Character,String> verbs1, verbs2;

	public static String getName ( String tag ) {
		switch ( tag.charAt(0) ) {
			case 'A':
				return "Adjetivo " + adjectives.get(tag.charAt(1));
			case 'B':
				return "Bloque: " + blocks.get(tag.charAt(2));
			case 'C':
				return "Conjunciones " + conjunctions.get(tag.charAt(1));
			case 'D':
				if ( tag.charAt(1) == 'A' )
					return "Artículo";
				return "Determinante " + determiners.get(tag.charAt(1));
			case 'I':
				return "Interjerción";
			case 'F':
				return "Puntuación: " + punctuation.get(tag.substring(1));
			case 'N':
				return "Nombre " + names.get(tag.charAt(1));
			case 'P':
				return "Pronombre " + pronouns.get(tag.charAt(1));
			case 'R':
				return "Adverbio " + adverbs.get(tag.charAt(1));
			case 'S':
				if ( tag.charAt(1) == 'P' )
					return "Preposición";
				break;
			case 'V':
				return "Verbo " + verbs1.get(tag.charAt(1)) + " " + verbs2.get(tag.charAt(2));
			case 'Y':
				return "Abreviación";
			case 'Z':
				return "Numeral";
		}
		return null;
	}
	
	static {
		adjectives = new TreeMap<Character, String>();
		adjectives.put('Q', "Calificativo");
		adjectives.put('O', "Ordinal");
		
		adverbs = new TreeMap<Character, String>();
		adverbs.put('G', "General");
		adverbs.put('N', "Negativo");
		
		blocks = new TreeMap<Character, String>();
		blocks.put('L', "Ubicación");
		blocks.put('M', "Otra");
		blocks.put('O', "Organización");
		blocks.put('P', "Persona");
		
		conjunctions = new TreeMap<Character, String>();
		conjunctions.put('C', "Coordinada");
		conjunctions.put('S', "Subordinada");
		
		determiners = new TreeMap<Character, String>();
		determiners.put('D', "Demostrativo");
		determiners.put('P', "Posesivo");
		determiners.put('T', "Interrogativo");
		determiners.put('E', "Exclamativo");
		determiners.put('I', "Indefinido");
		determiners.put('N', "Numeral");
		determiners.put('A', "Artículo");
		
		names = new TreeMap<Character, String>();
		names.put('C', "Comun");
		names.put('P', "Propio");
		
		pronouns = new TreeMap<Character, String>();
		pronouns.put( 'P', "Personal" );
		pronouns.put( 'D', "Demostrativo" );
		pronouns.put( 'X', "Posesivo" );
		pronouns.put( 'I', "Indefinido" );
		pronouns.put( 'T', "Interrogativo" );
		pronouns.put( 'R', "Relativo" );
		pronouns.put( 'E', "Exclamativo");
		pronouns.put( 'N', "Numeral");
		
		punctuation = new TreeMap<String, String>(); 
		punctuation.put("c", "Coma");
		punctuation.put("d", "Dos puntos");
		punctuation.put("e", "Comillas");
		punctuation.put("g", "Raya");
		punctuation.put("h", "Slash");
		punctuation.put("ia", "Abrir interrogación");
		punctuation.put("it", "Cerrar interrogación");
		punctuation.put("p", "Punto");
		punctuation.put("pa", "Abrir Paréntesis");
		punctuation.put("pt", "Cerrar Paréntesis");
		punctuation.put("s", "Etcétera");
		punctuation.put("t", "Porcentaje");
		punctuation.put("x", "Punto y coma");
		punctuation.put("z", "Operador Aritmético");
		
		verbs1 = new TreeMap<Character, String>();
		verbs1.put('M', "Principal");
		verbs1.put('A', "Auxiliar");
		verbs1.put('S', "Semiauxiliar");
		
		verbs2 = new TreeMap<Character, String>();
		verbs2.put('I', "Indicativo");
		verbs2.put('S', "Subjuntivo");
		verbs2.put('M', "Imperativo");
		verbs2.put('N', "Infinitivo");
		verbs2.put('G', "Gerundio");
		verbs2.put('P', "Participio");

	}
}
