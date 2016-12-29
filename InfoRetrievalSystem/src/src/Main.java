package src;

import java.io.Console;
import java.io.IOException;

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
	
	
	public static void main() {
		QueryRetrievalSystemConfig config = new QueryRetrievalSystemConfig();
		
		try {
			config.initialize();
			runSystem(config);
		}
		// System failed to boot (to index files and thus create the database)
		catch(IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	private static void runSystem(QueryRetrievalSystemConfig config) 
			throws IOException 
	{
		QueryRetrievalSystem machine = new QueryRetrievalSystem(config.getIndex(), config.getAnalyzer());
		
		
		Console cmd = System.console();
		String query = cmd.readLine("Please enter a query: ");	
	
		cmd.printf("Results:\n--------\n{0}\n",
				machine.getTopResultsForQuery(query, 10));
		
		while(true) {
			String goodChoices = cmd.readLine("Enter the document numbers you liked: ");
			if(goodChoices == "stop")
				break;
	
			int[] goodChoiceIndexes = _listToInt(goodChoices.split(" "));
			String badChoices = cmd.readLine("Enter the document numbers you hated: ");
			int[] badChoiceIndexes = _listToInt(badChoices.split(" "));
			
			cmd.printf("Refined results:\n--------\n{0}\n",
					machine.getTopResultsRankRefined(goodChoiceIndexes, badChoiceIndexes));
		}
		
		cmd.printf("\n\n");
		
		// Clean machine and launch second query
		runSystem(config);
	}
	
	private static int[] _listToInt(String[] split) {
		int[] values = new int[split.length];
		for(int i = 0; i < split.length; i++) {
			values[i] = Integer.decode(split[i]);
		}
		
		return values;
	}
}
