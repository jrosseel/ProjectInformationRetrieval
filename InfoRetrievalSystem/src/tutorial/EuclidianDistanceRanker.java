package tutorial;

import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.util.BytesRef;

public class EuclidianDistanceRanker extends TFIDFSimilarity {

	@Override
	public float coord(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float decodeNormValue(long arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long encodeNormValue(float arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float idf(long arg0, long arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float lengthNorm(FieldInvertState arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float queryNorm(float arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float scorePayload(int arg0, int arg1, int arg2, BytesRef arg3) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float sloppyFreq(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float tf(float arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
		  
		 
}