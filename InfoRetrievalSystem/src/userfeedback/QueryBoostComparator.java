package userfeedback;
/*
 * QueryBoostComparator.java 
 * 
 * Created on February 23, 2005, 5:28 PM 
 * 
 * @author Neil O. Rouben 
 */
 
import java.util.*; 
 
import org.apache.lucene.search.*; 
 
public class QueryBoostComparator implements Comparator 
{ 
     
    /** Creates a new instance of QueryBoostComparator */ 
    public QueryBoostComparator() 
    { 
    } 
     
    /**
     * Compares queries based on their boost 
     * Since want to be sorted in decending order; comparison will be reversed 
     */ 
    public int compare(Object obj1, Object obj2) 
    { 
    	BoostableQuery q1 = (BoostableQuery) obj1; 
    	BoostableQuery q2 = (BoostableQuery) obj2; 
         
        if ( q1.getBoost() > q2.getBoost() ) 
            return -1; 
        else if ( q1.getBoost() < q2.getBoost() )  
         return 1; 
        else 
         return 0; 
    } 
     
}