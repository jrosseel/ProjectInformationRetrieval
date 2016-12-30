package src;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;

import queries.QueryPerformer;
import userfeedback.RocchioExpander;

/**
 * Core of the system. Has the lifecycle of one query and its possible refinements
 */
public class QueryRetrievalSystem {

	private Directory _index;
	private Analyzer _analyzer;
	private IndexReader _IndexReader;
	private IndexSearcher _indexSearcher;

	private QueryPerformer _qPerformer;
	
	private RocchioExpander _rExpander;
	
	public QueryRetrievalSystem(Directory index, Analyzer analyzer) {
		_index = index;
		_analyzer = analyzer;
	}
	
	public void initialize() 
			throws IOException 
	{
		_IndexReader = DirectoryReader.open(_index);
		_indexSearcher = new IndexSearcher(_IndexReader);
		_rExpander = new RocchioExpander(_analyzer, _indexSearcher, new ClassicSimilarity());
	}
	
	/**
	 * Main query execution specialist. Handles aspect 1 - 5
	 */
	public String getTopResultsForQuery(String query, int k) 
			throws IOException, ParseException 
	{
		_qPerformer = new QueryPerformer(_analyzer, _indexSearcher);
		_qPerformer.setQuery(query);
		
		TopDocs matches = _qPerformer.getTopK(k);
        
        ScoreDoc[] hits = matches.scoreDocs;
        
        // Cache for feedback loop
        _query = query;
        _hits = hits;
        _topK = k;
        
		return printResults(hits);
	}
	
	private String _query;
	private ScoreDoc[] _hits;
	private int _topK;
	
	/**
	 * Executes the Rochio algorithm to refine results
	 * @throws IOException 
	 * */
	public String getTopResultsRankRefined(int[] goodChoiceIndexes, int[] badChoiceIndexes) throws IOException {
		Query q = _rExpander.expandQuery(_query, _getGoodHits(goodChoiceIndexes), _topK);
		TopDocs matches = _indexSearcher.search(q, _topK);
		// Todo: hier weer cachen voor feedback loop zodat we meer dan een keer rochio kunnen doen en steeds slimmer wordt
		
		return printResults(matches.scoreDocs);
	}
	
	
	private TopDocs _getGoodHits(int[] goodChoiceIndexes) {
		List<ScoreDoc> approvedHits = new ArrayList<ScoreDoc>();
		
		for(int i = 0; i < goodChoiceIndexes.length; i++)
													// - 1 want zie printResults
			approvedHits.add(_hits[goodChoiceIndexes[i] - 1]);
		
		
		return new TopDocs(approvedHits.size(), approvedHits.toArray(new ScoreDoc[approvedHits.size()]), 1000f);
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
            
            strBuilder.append((i + 1) + ". Document " + d.get(QuerySystemConsts.FIELD_DOC_FILENAME) + ": " + d.get(QuerySystemConsts.FIELD_DOC_TITLE));
            strBuilder.append('\n');
        }
        
        return strBuilder.toString();
	}
}