package queries;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;

public class QueryPerformer {
	private Directory _index;
	private Analyzer _analyzer;
	
	private Query _query;
	
	private IndexSearcher _indexSearcher;
	
	public QueryPerformer(Directory index, Analyzer analyzer)
	{
		this._index = index;
		this._analyzer = analyzer;
	}
	
	public void initialize(String queryStr)
			throws IOException, ParseException 
	{
		_query = new QueryParser("contents", _analyzer).parse(queryStr);
		
		IndexReader reader = DirectoryReader.open(_index);
		_indexSearcher = new IndexSearcher(reader);
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
