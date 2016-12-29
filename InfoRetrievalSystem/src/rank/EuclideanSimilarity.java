package rank;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.util.BytesRef;

public class EuclideanSimilarity extends ClassicSimilarity 
{
	private IndexSearcher _searcher;
	private IndexReader _reader;
	private Query _query;
	
 	public EuclideanSimilarity(IndexSearcher searcher, Query query) {
 		_searcher = searcher;
 		_reader = _searcher.getIndexReader();
 		_query = query;
 	}
 	
 	@Override
 	public float scorePayload(int doc, int start, int end, BytesRef payload) {
 		
 		System.out.println(payload);
 		return super.scorePayload(doc, start, end, payload);
 	} 
}
