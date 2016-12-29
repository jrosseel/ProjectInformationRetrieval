package docindex;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;

import src.QuerySystemConsts;

/**
 * Class responsible for the creation and management of the file index.
 */
public class IndexBuilder {
	
	private Directory _index;
	private IndexWriterConfig _config;
	private static IndexWriter w;
	
	private static ArrayList<File> _queue;
	
	private Analyzer _analyzer;
	
	public IndexBuilder(Directory index, Analyzer analyzer) {
		this._analyzer = analyzer;
		this._index = index;
	}

	/**
	 * If no index exists, create an index from the to-index-files location.
	 * @return
	 */
	public boolean tryCreateIndex()
		throws IOException
	{
		if(!DirectoryReader.indexExists(_index))
        {
        	_config = new IndexWriterConfig(_analyzer);
            w = new IndexWriter(_index, _config);
            
            // read all html files and add them to the index
            indexDirectory(QuerySystemConsts.DATASET_DIRECTORY);
            _queue = new ArrayList<File>();
            w.close();
            
            return true;
        }
        else
        {
        	return false;
        }			
	}
	
	private void indexDirectory(String folder) throws IOException
	{
		addFilesToIndex(new File(folder));
		
        int originalNumDocs = w.numDocs();
        for (File f : _queue) {
        	FileReader fr = null;
        	try 
        	{
        		fr = new FileReader(f);
        		Document doc = new Document();
        		// TextField: tokenize text
        		// StringField: don't tokenize text
        		doc.add(new TextField("contents", fr));
        		doc.add(new StringField("filename", f.getName(), Field.Store.YES));
        		w.addDocument(doc);
        		System.out.println("Added: " + f);
        	} 
        	catch (Exception e) 
        	{
        		System.out.println("Could not add: " + f);
        	} 
        	finally 
        	{
        		fr.close();
        	}
        }
        
        int newNumDocs = w.numDocs();
        System.out.println("");
        System.out.println("************************");
        System.out.println((newNumDocs - originalNumDocs) + " documents added.");
        System.out.println("************************");

        _queue.clear();
	}
	
	private void addFilesToIndex(File file) {

        if (!file.exists()) 
        	System.out.println(file + " does not exist.");
        
        else if (file.isDirectory()) 
        	// search recursive for files
        	for (File f : file.listFiles())
        		addFilesToIndex(f);
        
        else 
        {
        	String filename = file.getName().toLowerCase();
        	if (filename.endsWith(".htm") || filename.endsWith(".html") || filename.endsWith(".xml") || filename.endsWith(".txt")) 
        	{
        		_queue.add(file);
        	} 
        	else
        		System.out.println("Skipped " + filename + " due to incompatible file type.");
        }
	}
}
