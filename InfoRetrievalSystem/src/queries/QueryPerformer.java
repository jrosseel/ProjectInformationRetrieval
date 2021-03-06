package queries;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;

public class QueryPerformer {
	private Analyzer _analyzer;
	
	private Query _query;
	
	private IndexSearcher _indexSearcher;
	
	public QueryPerformer(Analyzer analyzer, IndexSearcher indexSearcher)
	{
		this._analyzer = analyzer;
		this._indexSearcher = indexSearcher;
	}
	
	public void setQuery(String queryStr)
			throws IOException, ParseException 
	{
		_query = new QueryParser("contents", _analyzer).parse(queryStr);
	}
	
	public TopDocs getTopK(int k) 
			throws IOException
	{
		
        return _indexSearcher.search(_query, k);
	}

	public Document findDoc(int docId) 
			throws IOException 
	{
		return _indexSearcher.doc(docId);
	}
}
