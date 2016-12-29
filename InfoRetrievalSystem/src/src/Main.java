package src;

import java.awt.List;
import java.io.Console;
import java.util.ArrayList;

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
	
		Console cmd = System.console();
		
		String query = cmd.readLine("Please enter a query: ");	
		
		QueryRetrievalSystem machine = new QueryRetrievalSystem();
		cmd.printf("Results:\n--------\n{0}\n",
				_printResults(machine.getTopResultsForQuery(query, 10)));
		
		while(true) {
			String goodChoices = cmd.readLine("Enter the document numbers you liked: ");
			if(goodChoices == "stop")
				break;

			int[] goodChoiceIndexes = _listToInt(goodChoices.split(" "));
			String badChoices = cmd.readLine("Enter the document numbers you hated: ");
			int[] badChoiceIndexes = _listToInt(badChoices.split(" "));
			
			cmd.printf("Refined results:\n--------\n{0}\n",
					_printResults(machine.getTopResultsRankRefined(goodChoiceIndexes, badChoiceIndexes)));
		}
		
		cmd.printf("\n\n");
		main();
	}
	
	private static int[] _listToInt(String[] split) {
		int[] values = new int[split.length];
		for(int i = 0; i < split.length; i++) {
			values[i] = Integer.decode(split[i]);
		}
		
		return values;
	}

	private static String _printResults(String[] queryResults) {
		return String.join("\n", queryResults);
	}
}
