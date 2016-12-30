package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.lucene.queryparser.classic.ParseException;

/**
 * Entry point for the Group 2 Information Retrieval System. 
 * Contains the main logic for the search system.
 * 
 * Works in a piped way.
 * 
 * The system takes in a query, sanitizes and expands it, and finally searches
 * 	a set of documents that correspond to the query.
 * 
 * That set of documents is ranked and returned to the user.
 * 
 * The user can give feedback as to which documents interest him/her, after which the results will be refined.
 */
public class Main {
	
	
	public static void main(String[] args) {
		QueryRetrievalSystemConfig config = new QueryRetrievalSystemConfig();
		
		try {
			config.initialize();
			runSystem(config);
		}
		// System failed to boot (to index files and thus create the database)
		catch(IOException | ParseException e) 
		{
			e.printStackTrace();
		}
	}
	
	private static void runSystem(QueryRetrievalSystemConfig config) 
			throws IOException, ParseException 
	{
		QueryRetrievalSystem machine = new QueryRetrievalSystem(config.getIndex(), config.getAnalyzer());
		machine.initialize();
		
		String query = _readLine("Please enter a query: ");	
	
		_printf("Results:\n--------\n%s\n",
				machine.getTopResultsForQuery(query, 10));
		
		while(true) {
			String goodChoices = _readLine("Enter the document numbers you liked: ");
			if(goodChoices.equals("stop"))
				break;
	
			int[] goodChoiceIndexes = _listToInt(goodChoices.split(" "));
			String badChoices = _readLine("Enter the document numbers you hated: ");
			int[] badChoiceIndexes = _listToInt(badChoices.split(" "));
			
			_printf("Refined results:\n--------\n%s\n",
					machine.getTopResultsRankRefined(goodChoiceIndexes, badChoiceIndexes));
		}
		
		_printf("\n\n");
		
		// Clean machine and launch second query
		runSystem(config);
	}
	
	private static String _readLine(String format, Object... args) throws IOException {
	    if (System.console() != null) {
	        return System.console().readLine(format, args);
	    }
	    _printf(format, args);
	    BufferedReader reader = new BufferedReader(new InputStreamReader(
	            System.in));
	    return reader.readLine();
	}
	
	private static void _printf(String format, Object... args) {
		 System.out.print(String.format(format, args));
	}

	private static int[] _listToInt(String[] split) {
		if(split.length > 0 && !split[0].equals("")) {
			int[] values = new int[split.length];
			
			for(int i = 0; i < split.length; i++) {
				values[i] = Integer.decode(split[i]);
			}
			
			return values;
		}
		else 
			return new int[0];
	}
}