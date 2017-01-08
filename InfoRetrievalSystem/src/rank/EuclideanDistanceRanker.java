package rank;

import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.util.BytesRef;

public class EuclideanDistanceRanker extends ClassicSimilarity {
	
	
	
	@Override
	public float scorePayload(int doc, int start, int end, BytesRef payload) {
		// TODO Auto-generated method stub
		return super.scorePayload(doc, start, end, payload);
	}
}
