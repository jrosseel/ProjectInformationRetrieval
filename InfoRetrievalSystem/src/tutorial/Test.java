package tutorial;

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
import org.apache.lucene.store.RAMDirectory;

public class Test {
	private static IndexWriter w;
	private static ArrayList<File> queue = new ArrayList<File>();
	
    public static void main(String[] args) throws IOException, ParseException {
        // Specify the analyzer for tokenizing text (use the same for indexing and searching).
        StandardAnalyzer analyzer = new StandardAnalyzer();

        // Create the index (if not already exists)
        String indexDirectory = "index";
        Directory index = FSDirectory.open(Paths.get(indexDirectory));
        if(!DirectoryReader.indexExists(index))
        {
        	// create index
        	IndexWriterConfig config = new IndexWriterConfig(analyzer);
            w = new IndexWriter(index, config);
            // read all html files and add them to the index
            indexFileOrDirectory("dataset");
            w.close();
        }
        else
        {
        	System.out.println("Index already created, let's search!");
        }
        
        // perform query MEER UITLEG VOLGT        
        String querystr = args.length > 0 ? args[0] : "Preliminary interpretation";

        // contents argument => field where we should search for matches with our query (the whole web page)
        Query q = new QueryParser("contents", analyzer).parse(querystr);

        // search
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
    }
    
    public static void indexFileOrDirectory(String folder) throws IOException {
    	// get all the files from a folder and add them to the index
        addFiles(new File(folder));        
        int originalNumDocs = w.numDocs();
        for (File f : queue) {
          FileReader fr = null;
          try {
            fr = new FileReader(f);
            Document doc = new Document();
            doc.add(new TextField("contents", fr));
            doc.add(new StringField("filename", f.getName(), Field.Store.YES));

            w.addDocument(doc);
            System.out.println("Added: " + f);
          } catch (Exception e) {
            System.out.println("Could not add: " + f);
          } finally {
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

      private static void addFiles(File file) {

        if (!file.exists()) {
          System.out.println(file + " does not exist.");
        }
        if (file.isDirectory()) {
          for (File f : file.listFiles()) {
            addFiles(f);
          }
        } else {
          String filename = file.getName().toLowerCase();
          //===================================================
          // Only index text files
          //===================================================
          if (filename.endsWith(".htm") || filename.endsWith(".html") || 
                  filename.endsWith(".xml") || filename.endsWith(".txt")) {
            queue.add(file);
          } else {
            System.out.println("Skipped " + filename);
          }
        }
      }
}