package userfeedback;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.search.TermQuery;

public class BoostableQuery extends TermQuery {

	private float _boost = 1;
	
	public BoostableQuery(Term t) {
		super(t);
	}

	public BoostableQuery(Term t, TermContext c) {
		super(t, c);
	}
	
	public float getBoost() { return _boost; }
	public void  setBoost(float b) { _boost = b; }
	
}
