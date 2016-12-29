package src;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import docindex.IndexBuilder;
import soundex.PhoneticAnalyzer;

public class QueryRetrievalSystemConfig {
	
	private Directory _index;
	private Analyzer _analyzer;
	
	public void initialize() 
			throws IOException 
	{
		_analyzer = new PhoneticAnalyzer();
		
		System.out.println("Initializing query system...");
		_index = FSDirectory.open(Paths.get(QuerySystemConsts.INDEX_DIRECTORY));
		
		IndexBuilder indexBuilder = new IndexBuilder(_index, _analyzer);
	
		System.out.println("Loading database.");
		if(indexBuilder.tryCreateIndex()) 
			System.out.println("Database did not yet exists. Created index from file database.");	
	}

	public Directory getIndex() {
		return _index;
	}

	public Analyzer getAnalyzer() {
		return _analyzer;
	}


}
