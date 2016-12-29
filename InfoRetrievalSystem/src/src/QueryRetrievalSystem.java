package src;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;

import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;

import queries.QueryPerformer;

/**
 * Core of the system. Has the lifecycle of one query and its possible refinements
 */
public class QueryRetrievalSystem {

	private Directory _index;
	private Analyzer _analyzer;
	
	private QueryPerformer _qPerformer;
	
	
	public QueryRetrievalSystem(Directory index, Analyzer analyzer) {
		_index = index;
		_analyzer = analyzer;
	}
	
	/**
	 * Main query execution specialist. Handles aspect 1 - 5
	 */
	public String getTopResultsForQuery(String query, int k) 
			throws IOException 
	{
		_qPerformer = new QueryPerformer(_index, _analyzer, query);
		
		TopDocs matches = _qPerformer.getTopK(k);
        
        ScoreDoc[] hits = matches.scoreDocs;
        
		return printResults(hits);
	}
	
	/**
	 * Executes the Rochio algorithm to refine results
	 * */
	public String getTopResultsRankRefined(int[] goodChoiceIndexes, int[] badChoiceIndexes) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	/**
	 * Creates a string that prints the passed results
	 */
	private String printResults(ScoreDoc[] hits) 
				throws IOException
	{
		StringBuilder strBuilder = new StringBuilder();
		
		strBuilder.append("Found " + hits.length + " hits.\n");
		
        for(int i=0;i<hits.length;++i) 
        {
            int docId = hits[i].doc;
            Document d = _qPerformer.findDoc(docId);
            
            strBuilder.append((i + 1) + ". Document:" + d.get("filename"));
        }
        
        return strBuilder.toString();
	}
}