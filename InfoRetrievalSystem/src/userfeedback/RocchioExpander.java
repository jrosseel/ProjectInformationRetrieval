package userfeedback;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.PatternSyntaxException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.TFIDFSimilarity;

import src.QuerySystemConsts;

/**
 * @source: https://github.com/eric-cho/LuceneQueryExpansion/blob/master/src/main/java/in/student/project/queryexpansion/QueryExpansion.java
 */
public class RocchioExpander 
{ 
	private Analyzer analyzer; 
	private IndexSearcher searcher; 
	private TFIDFSimilarity similarity; 
	private Vector<BoostableQuery> expandedTerms;

	/**
	 * Creates a new instance of QueryExpansion 
	 * 
	 * @param similarity 
	 * @param analyzer - used to parse documents to extract terms 
	 * @param searcher - used to obtain idf 
	 */ 
	public RocchioExpander( Analyzer analyzer, IndexSearcher searcher, TFIDFSimilarity similarity ) 
	{ 
		this.analyzer = analyzer; 
		this.searcher = searcher; 
		this.similarity = similarity;
	} 

	/**
	 * Performs Rocchio's query expansion with pseudo feedback qm = alpha * 
	 * query + ( beta / relevanDocsCount ) * Sum ( rel docs vector ) 
	 *  
	 * @param queryStr - 
	 *            that will be expanded 
	 * @param hits - 
	 *            from the original query to use for expansion 
	 * @param prop - properties that contain necessary values to perform query;  
	 *               see constants for field names and values 
	 *  
	 * @return expandedQuery 
	 *  
	 * @throws IOException 
	 * @throws ParseException 
	 */ 
	public Query expandQuery( String queryStr, TopDocs hits, int k ) 
			throws IOException 
	{ 
		// Get Docs to be used in query expansion 
		Vector<Document> vHits = getDocs( queryStr, hits, k ); 

		return expandQuery( queryStr, vHits, k ); 
	} 


	/**
	 * Gets documents that will be used in query expansion. 
	 * number of docs indicated by <code>QueryExpansion.DOC_NUM_FLD</code> from <code> QueryExpansion.DOC_SOURCE_FLD </code> 
	 *  
	 * @param query - for which expansion is being performed 
	 * @param hits - to use in case <code> QueryExpansion.DOC_SOURCE_FLD </code> is not specified 
	 * @param prop - uses <code> QueryExpansion.DOC_SOURCE_FLD </code> to determine where to get docs 
	 *  
	 * @return number of docs indicated by <code>QueryExpansion.DOC_NUM_FLD</code> from <code> QueryExpansion.DOC_SOURCE_FLD </code>  
	 * @throws IOException  
	 * @throws GoogleSearchFault  
	 */ 
	private Vector<Document> getDocs( String query, TopDocs hits, int k ) throws IOException 
	{ 
		Vector<Document> vHits = new Vector<Document>();         

		// Convert Hits -> Vector 
		int hits_len = hits.scoreDocs.length; 
		for ( int i = 0; ( ( i < k ) && ( i < hits_len ) ); i++ ) 
		{ 
			vHits.add( searcher.doc(hits.scoreDocs[i].doc) ); 
		}        

		return vHits; 
	} 


	/**
	 * Performs Rocchio's query expansion with pseudo feedback 
	 * qm = alpha * query + ( beta / relevanDocsCount ) * Sum ( rel docs vector ) 
	 *  
	 * @param queryStr - that will be expanded 
	 * @param hits - from the original query to use for expansion 
	 * @param prop - properties that contain necessary values to perform query;  
	 *               see constants for field names and values 
	 *  
	 * @return 
	 * @throws IOException 
	 * @throws ParseException 
	 */ 
	public Query expandQuery( String queryStr, Vector<Document> hits, int k) 
			throws IOException 
	{ 
		// Load Necessary Values from Properties 
		float alpha = QuerySystemConsts.ROCHIO_ALPHA; 
		float beta = QuerySystemConsts.ROCCHIO_BETA; 
		float decay = QuerySystemConsts.DECAY_FLD; 
		int termNum = QuerySystemConsts.MAX_ROCCHIO_TERM_EXPANSION;                        

		// Create combine documents term vectors - sum ( rel term vectors ) 
		Vector<QueryTermVector> docsTermVector = getDocsTerms( hits, k, analyzer ); 

		// Adjust term features of the docs with alpha * query; and beta; and assign weights/boost to terms (tf*idf) 
		Query expandedQuery = adjust( docsTermVector, queryStr, alpha, beta, decay, k, termNum ); 

		return expandedQuery; 
	} 

	/**
	 * Adjust term features of the docs with alpha * query; and beta; 
	 * and assign weights/boost to terms (tf*idf). 
	 * 
	 * @param docsTermsVector of the terms of the top 
	 *        <code> docsRelevantCount </code> 
	 *        documents returned by original query 
	 * @param queryStr - that will be expanded 
	 * @param alpha - factor of the equation 
	 * @param beta - factor of the equation 
	 * @param docsRelevantCount - number of the top documents to assume to be relevant 
	 * @param maxExpandedQueryTerms - maximum number of terms in expanded query 
	 * 
	 * @return expandedQuery with boost factors adjusted using Rocchio's algorithm 
	 * 
	 * @throws IOException 
	 * @throws ParseException 
	 */ 
	public Query adjust( Vector<QueryTermVector> docsTermsVector, String queryStr,  
			float alpha, float beta, float decay, int docsRelevantCount,  
			int maxExpandedQueryTerms ) 
					throws IOException 
	{ 
		Query expandedQuery; 

		// setBoost of docs terms 
		Vector<BoostableQuery> docsTerms = setBoost( docsTermsVector, beta, decay );

		// setBoost of query terms 
		// Get queryTerms from the query 
		QueryTermVector queryTermsVector = new QueryTermVector( queryStr, analyzer );         
		Vector<BoostableQuery> queryTerms = setBoost( queryTermsVector, alpha );         

		// combine weights according to expansion formula 
		Vector<BoostableQuery> expandedQueryTerms = combine( queryTerms, docsTerms ); 
		setExpandedTerms( expandedQueryTerms );  

		Comparator comparator = new QueryBoostComparator(); 
		Collections.sort( expandedQueryTerms, comparator ); 

		// Create Expanded Query 
		expandedQuery = null; 
		try { 
			expandedQuery = mergeQueries( expandedQueryTerms, maxExpandedQueryTerms ); 
		} catch (QueryNodeException e) { 
			e.printStackTrace(); 
		} 

		return expandedQuery; 
	} 



	/**
	 * Merges <code>termQueries</code> into a single query. 
	 * In the future this method should probably be in <code>Query</code> class. 
	 * This is akward way of doing it; but only merge queries method that is 
	 * available is mergeBooleanQueries; so actually have to make a string 
	 * term1^boost1, term2^boost and then parse it into a query 
	 *      
	 * @param termQueries - to merge 
	 * 
	 * @return query created from termQueries including boost parameters 
	 * @throws QueryNodeException  
	 */     
	public Query mergeQueries( Vector<BoostableQuery> termQueries, int maxTerms ) throws QueryNodeException 
	{ 
		Query query = null; 

		// Select only the maxTerms number of terms 
		int termCount = Math.min( termQueries.size(), maxTerms ); 

		// Create Query String 
		StringBuffer qBuf = new StringBuffer(); 
		for ( int i = 0; i < termCount; i++ ) 
		{ 
			BoostableQuery termQuery = termQueries.elementAt(i);  
			Term term = termQuery.getTerm(); 

			qBuf.append( QueryParser.escape(term.text()).toLowerCase() + "^" + termQuery.getBoost() + " " );
		}    

		String targetStr = qBuf.toString(); 
		// TODO: Evt integreren met Tim zijn query parser
		try { 
			query = new QueryParser(QuerySystemConsts.FIELD_DOC_INDEXEDCONTENTS, analyzer ).parse(targetStr); 
		} catch (ParseException e) { 
			e.printStackTrace(); 
		}        

		return query; 
	} 


	/**
	 * Extracts terms of the documents; Adds them to vector in the same order 
	 * 
	 * @param doc - from which to extract terms 
	 * @param docsRelevantCount - number of the top documents to assume to be relevant 
	 * @param analyzer - to extract terms 
	 * 
	 * @return docsTerms docs must be in order 
	 */ 
	public Vector<QueryTermVector> getDocsTerms( Vector<Document> hits, int docsRelevantCount, Analyzer analyzer ) 
			throws IOException 
	{      
		Vector<QueryTermVector> docsTerms = new Vector<QueryTermVector>(); 

		// Process each of the documents 
		for ( int i = 0; ( (i < docsRelevantCount) && (i < hits.size()) ); i++ ) 
		{ 
			Document doc = hits.elementAt( i ); 
			// Get text of the document and append it 
			StringBuffer docTxtBuffer = new StringBuffer();

			String[] docTxtFlds = doc.getValues( QuerySystemConsts.FIELD_DOC_TITLE ); 

			if(docTxtFlds.length > 0)
				for(int j = 0; j < docTxtFlds.length; j++)
					docTxtBuffer.append( docTxtFlds[j] + " " );   

			// Create termVector and add it to vector 
			QueryTermVector docTerms = new QueryTermVector( docTxtBuffer.toString(), analyzer ); 
			docsTerms.add(docTerms ); 
		}         

		return docsTerms; 
	} 


	/**
	 * Sets boost of terms.  boost = weight = factor(tf*idf) 
	 * 
	 * @param termVector 
	 * @param beta - adjustment factor ( ex. alpha or beta ) 
	 */  
	public Vector<BoostableQuery> setBoost( QueryTermVector termVector, float factor ) 
			throws IOException 
	{ 
		Vector<QueryTermVector> v = new Vector<QueryTermVector>(); 
		v.add( termVector ); 

		return setBoost( v, factor, 0 ); 
	} 


	/**
	 * Sets boost of terms.  boost = weight = factor(tf*idf) 
	 * 
	 * @param docsTerms 
	 * @param factor - adjustment factor ( ex. alpha or beta ) 
	 */ 
	public Vector<BoostableQuery> setBoost( Vector<QueryTermVector> docsTerms, float factor, float decayFactor ) 
			throws IOException 
	{ 
		Vector<BoostableQuery> terms = new Vector<BoostableQuery>(); 

		// setBoost for each of the terms of each of the docs 
		for ( int g = 0; g < docsTerms.size(); g++ ) 
		{ 
			QueryTermVector docTerms = docsTerms.elementAt( g ); 
			String[] termsTxt = docTerms.getTerms(); 
			int[] termFrequencies = docTerms.getTermFrequencies(); 

			// Increase decay 
			float decay = decayFactor * g; 

			// Populate terms: with TermQuries and set boost 
			for ( int i = 0; i < docTerms.size(); i++ ) 
			{ 
				// Create Term 
				String termTxt = termsTxt[i]; 
				Term term = new Term( QuerySystemConsts.FIELD_DOC_INDEXEDCONTENTS, termTxt ); 

				// Calculate weight 
				float tf = termFrequencies[i]; 
				float idf = similarity.idf( (long)tf, docTerms.size() ); 
				float weight = tf * idf; 

				// Adjust weight by decay factor 
				weight = weight - (weight * decay);

				// Create BoostableQuery and add it to the collection 
				BoostableQuery termQuery = new BoostableQuery( term ); 
				// Calculate and set boost 
				termQuery.setBoost( factor * weight ); 
				terms.add( termQuery ); 
			} 
		} 

		// Get rid of duplicates by merging termQueries with equal terms 
		merge( terms );   

		return terms; 
	} 


	/**
	 * Gets rid of duplicates by merging termQueries with equal terms 
	 *  
	 * @param terms 
	 */ 
	private void merge(Vector<BoostableQuery> terms)  
	{ 
		for ( int i = 0; i < terms.size(); i++ ) 
		{ 
			BoostableQuery term = terms.elementAt( i ); 
			// Itterate through terms and if term is equal then merge: add the boost; and delete the term 
			for ( int j = i + 1; j < terms.size(); j++ ) 
			{ 
				BoostableQuery tmpTerm = terms.elementAt( j ); 

				// If equal then merge 
				if ( tmpTerm.getTerm().text().equals( term.getTerm().text() ) ) 
				{ 
					// Add boost factors of terms 
					term.setBoost( term.getBoost() + tmpTerm.getBoost() ); 
					// delete uncessary term 
					terms.remove( j );      
					// decrement j so that term is not skipped 
					j--; 
				} 
			} 
		} 
	} 


	/**
	 * combine weights according to expansion formula 
	 */ 
	public Vector<BoostableQuery> combine( Vector<BoostableQuery> queryTerms, Vector<BoostableQuery> docsTerms ) 
	{ 
		Vector<BoostableQuery> terms = new Vector<BoostableQuery>(); 
		// Add Terms from the docsTerms 
		terms.addAll( docsTerms ); 
		// Add Terms from queryTerms: if term already exists just increment its boost 
		for ( int i = 0; i < queryTerms.size(); i++ ) 
		{ 
			BoostableQuery qTerm = queryTerms.elementAt(i); 
			BoostableQuery term = find( qTerm, terms ); 
			// Term already exists update its boost 
			if ( term != null ) 
			{ 
				float weight = qTerm.getBoost() + term.getBoost(); 
				term.setBoost( weight ); 
			} 
			// Term does not exist; add it 
			else 
			{ 
				terms.add( qTerm ); 
			} 
		} 

		return terms; 
	} 


	/**
	 * Finds term that is equal 
	 * 
	 * @return term; if not found -> null 
	 */ 
	public BoostableQuery find( BoostableQuery term, Vector<BoostableQuery> terms ) 
	{ 
		BoostableQuery termF = null; 

		Iterator<BoostableQuery> iterator = terms.iterator(); 
		while ( iterator.hasNext() ) 
		{ 
			BoostableQuery currentTerm = iterator.next(); 
			if ( term.getTerm().equals( currentTerm.getTerm() ) ) 
			{ 
				termF = currentTerm;
			} 
		} 

		return termF; 
	} 



	/**
	 * Returns <code> QueryExpansion.TERM_NUM_FLD </code> expanded terms from the most recent query 
	 *  
	 * @return 
	 */ 
	public Vector<BoostableQuery> getExpandedTerms() 
	{ 
		int termNum = 1000; 
		if (termNum > this.expandedTerms.size()) termNum = this.expandedTerms.size(); 
		Vector<BoostableQuery> terms = new Vector<BoostableQuery>(); 

		// Return only necessary number of terms 
		List<BoostableQuery> list = this.expandedTerms.subList( 0, termNum ); 
		terms.addAll( list ); 

		return terms; 
	} 


	private void setExpandedTerms( String str ) 
	{ 
		Vector<BoostableQuery> terms = new Vector<BoostableQuery>(); 

		String[] splitArray = null; 
		try { 
			splitArray = str.split("\\s+"); 
		} catch (PatternSyntaxException ex) { 
			//  
		} 

		// setBoost for each of the terms of each of the docs 
		for ( int i = 0; i < splitArray.length; i++ ) 
		{ 
			String termTxt = splitArray[i]; 
			Term term = new Term( QuerySystemConsts.FIELD_DOC_INDEXEDCONTENTS, termTxt ); 

			// Create BoostableQuery and add it to the collection 
			BoostableQuery termQuery = new BoostableQuery( term ); 
			terms.add( termQuery ); 
		} 

		// Get rid of duplicates by merging termQueries with equal terms 
		merge( terms );   

		setExpandedTerms ( terms ); 
	} 

	private void setExpandedTerms( Vector<BoostableQuery> expandedTerms ) 
	{    
		this.expandedTerms = expandedTerms; 
	} 



}