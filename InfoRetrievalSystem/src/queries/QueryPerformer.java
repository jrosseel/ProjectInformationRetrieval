package queries;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class QueryPerformer {
	private String query;
	private StandardAnalyzer analyzer;
	private final String INDEX_DIRECTORY = "index";
	private final String DATASET_DIRECTORY = "dataset";
	private Directory index;
	private IndexWriterConfig config;
	private static IndexWriter w;
	private static ArrayList<File> queue;
	
	public QueryPerformer()
	{
		analyzer = new StandardAnalyzer();
		try {
			index = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
			if(!DirectoryReader.indexExists(index))
	        {
	        	config = new IndexWriterConfig(analyzer);
	            w = new IndexWriter(index, config);
	            // read all html files and add them to the index
	            indexDirectory(DATASET_DIRECTORY);
	            queue = new ArrayList<File>();
	            w.close();
	        }
	        else
	        {
	        	System.out.println("Index already created, let's search!");
	        }			
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public void setQuery(String query)
	{
		this.query = query;
	}
	
	public void results() throws IOException
	{
		//String querystr = args.length > 0 ? args[0] : "\"22 CA581203\"~4";
		try {
			// Get results
			String querystr = query;
			Query q = new QueryParser("contents", analyzer).parse(querystr);
			int hitsPerPage = 100000;
	        IndexReader reader = DirectoryReader.open(index);
	        IndexSearcher searcher = new IndexSearcher(reader);
	        TopDocs docs = searcher.search(q, hitsPerPage);
	        ScoreDoc[] hits = docs.scoreDocs;

	        // Display results
	        System.out.println("Found " + hits.length + " hits.");
	        for(int i=0;i<hits.length;++i) {
	            int docId = hits[i].doc;
	            Document d = searcher.doc(docId);
	            System.out.println((i + 1) + ". Document:" + d.get("filename"));
	        }
	        reader.close();
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	private void indexDirectory(String folder) throws IOException
	{
		addFilesToIndex(new File(folder));        
        int originalNumDocs = w.numDocs();
        for (File f : queue) {
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

        queue.clear();
	}
	
	private void addFilesToIndex(File file) {

        if (!file.exists()) 
        {
          System.out.println(file + " does not exist.");
        }
        if (file.isDirectory()) 
        {
          // search recursive for files
          for (File f : file.listFiles()) {
        	
            addFilesToIndex(f);
          }
        } 
        else 
        {
	      String filename = file.getName().toLowerCase();
	      if (filename.endsWith(".htm") || filename.endsWith(".html") || filename.endsWith(".xml") || filename.endsWith(".txt")) 
	      {
	        queue.add(file);
	      } 
	      else 
	      {
	        System.out.println("Skipped " + filename);
	      }
        }
      }
	
	
}
