package swu.cp.ir;

import java.io.*;
import java.util.*;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.btree.BTree;
import jdbm.helper.StringComparator;
import jdbm.helper.Tuple;
import jdbm.helper.TupleBrowser;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;



public class Search1 {
	
	//Data Member --JDBM--
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArrayList<String> key = new ArrayList<String>();
		ArrayList<String> value = new ArrayList<String>();
		
		RecordManager recman;
        long          recid;
        Tuple         tuple = new Tuple();
        TupleBrowser  browser;
        BTree         tree = null;
        Properties    props;
        
		//-------------------------------------------------------

		System.out.println("Project 0");

		Scanner in = new Scanner(System.in);
		// input 1 (one or more tokens separated by space)
		String input1 = in.nextLine();
		//System.out.println(input1);
		
		try {
			// TODO: Connect with Stemmer (org.tartarus.snowball) and display
			// stemmed input on console
			Class stemClass;
			//locate path of Class 
			stemClass = Class.forName("org.tartarus.snowball.ext." +
					"english" + "Stemmer");
			//stemClass = new englishStemmer();
			//Create a instance of stemmer 
			//SnowballStemmer stemmer = (SnowballStemmer) stemClass.newInstance();
			
			englishStemmer stemmer = new englishStemmer();
			//-------------------------------------------------------
			//change string to lowercase
			input1=input1.toLowerCase();
			System.out.println(input1);
			
			//StringTokenizer: spilt token into term
			StringTokenizer st=new StringTokenizer(input1," ");
	        while(st.hasMoreTokens()){
	            String term =st.nextToken();
	            //String q = "Compressed";
	            //System.out.println("Before: "+q);
	            //stemmer.setCurrent(); = input before stem >> compressed
	            stemmer.setCurrent(term);
	            //Call stemmer to use porter alogorithm	            
	            stemmer.stem();
	            //stemmer.getCurrent(); = output affer stem >> compress 
	            String stemTerm = stemmer.getCurrent();
	            //System.out.println("Afrer: "+stemTerm);
	            //System.out.println("Next Token "+temp);
	            
	            //add term in key & value list
	            key.add(term);
	            value.add(stemTerm);
	        }        
		
		
		
	        System.out.println();
	        // TODO: Store unstemmed token as key, the stemmed token as value in
	        // B+tree (jdbm.btree)
	        //construct Btree
			props = new Properties();
			// open database and setup an object cache
            recman = RecordManagerFactory.createRecordManager("term", props );

            // try to reload an existing B+Tree
            recid = recman.getNamedObject("Tokenizer");
            if ( recid != 0 ) {
                tree = BTree.load( recman, recid );
                System.out.println( "Reloaded existing BTree with " + tree.size()
                                    + " famous people." );
            } else {
                // create a new B+Tree data structure and use a StringComparator
                // to order the records based on people's name.
                tree = BTree.createInstance( recman, new StringComparator() );
                recman.setNamedObject( "Tokenizer", tree.getRecid() );
                System.out.println( "Created a new empty BTree" );
            }
            
            //insert
            for ( int i=0; i<key.size(); i++ ) {
                tree.insert(key.get(i),value.get(i) , false );
            }
            // make the data persistent in the database
            recman.commit();
            
            // show list of term with their occupation
            System.out.println();
            System.out.println( "Key                   Value       " );
            System.out.println( "------------------       ------------------" );

            // traverse term in order
            browser = tree.browse();
            while ( browser.getNext( tuple ) ) {
                print( tuple );
            }
            
            // get search key
    		String input2 = in.nextLine();
    		// TODO: Retrieve from B+tree given the input 2 key
			stemmer.setCurrent(input2);
            //Call stemmer to use porter alogorithm	            
            stemmer.stem();
            //stemmer.getCurrent(); = output affer stem >> compress 
            String stemInput2 = stemmer.getCurrent();
            browser = tree.browse();
            while ( browser.getNext( tuple ) ) {
                String key1 = (String) tuple.getKey();
                if ( key1.matches(stemInput2)) {
                   System.out.println("Found");
                } else {
                   System.out.println("Not Found");
                }
            }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		in.close();

	}
	 /**
     * Print a Tuple containing a ( Person, Occupation ) pair.
     */
    static void print( Tuple tuple ) {
        String person = (String) tuple.getKey();
        String occupation = (String) tuple.getValue();
        System.out.println( pad( person, 25) + occupation );
    }


    /**
     * Pad a string with spaces on the right.
     *
     * @param str String to add spaces
     * @param width Width of string after padding
     */
    static String pad( String str, int width ) {
        StringBuffer buf = new StringBuffer( str );
        int space = width-buf.length();
        while ( space-- > 0 ) {
            buf.append( ' ' );
        }
        return buf.toString();
    }

}
