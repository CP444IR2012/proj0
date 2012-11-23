/* 
 * ProjectO
 * GROUP3: 306, 308, 905
 */

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

public class Search {
	
	//Data Member --Porter--
	Class stemClass = null;
	SnowballStemmer stemmer = null;
	
	//Data Member --JDBM--
	RecordManager recman;
    long          recid;
    Tuple         tuple = new Tuple();
    TupleBrowser  browser;
    BTree         tree = null;
    Properties    props;
	
	public static void main(String[] args) {
		
		Search search = new Search();
		
		//-------------------------------------------------------
		System.out.println("Project 0");
		try {
			Scanner in = new Scanner(System.in);
			// input 1 (one or more tokens separated by space)
			String input1 = in.nextLine();
			//System.out.println(input1);
			
			// TODO: Connect with Stemmer (org.tartarus.snowball) and display
			// stemmed input on console
			// init 
			search.initPorter("english");
			search.initJDBM();
			//change string to lowercase
			input1=input1.toLowerCase();
			//System.out.println(input1);
			
			//-----------------------------------------------------
			//StringTokenizer: spilt token into term
			StringTokenizer st=new StringTokenizer(input1," ");
	        while(st.hasMoreTokens()){
	            String term =st.nextToken();
	            //System.out.println("Before: "+term);
	            String stemTerm = search.stemTerm(term);
	            //System.out.println("Afrer: "+stemTerm);
	            System.out.print(stemTerm+" ");
				//-----------------------------------------------------
	            // TODO: Store unstemmed token as key, the stemmed token as value in
		        // B+tree (jdbm.btree)
	            search.tree.insert(term, stemTerm, false );
	        }        
	        
            // make the data persistent in the database
	        search.recman.commit();
	        System.out.println();
	        
			//listTree();
	        search.listTree();
            
            // get search key
    		String input2 = in.nextLine();
    		//change string to lowercase
			input2=input2.toLowerCase();
    		//StringTokenizer: spilt token into term
			st=new StringTokenizer(input2," ");
	        while(st.hasMoreTokens()){
	            String term =st.nextToken(); 
				//-----------------------------------------------------
	            // TODO: Retrieve from B+tree given the input 2 key
	            String key = search.searchTree(term);
	            //System.out.println(key);
	            if (key.isEmpty()){
	            	System.out.println("Term: not found");
	            }else{
	            	System.out.println("Term: "+key +" is found");
	            }
	        }   
            in.close();
		} catch (Exception e) {
			e.getMessage();
			e.printStackTrace();
		} 
	}
	private void initPorter(String language) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		stemClass = Class.forName("org.tartarus.snowball.ext." +language+ "Stemmer");
		//Create a instance of stemmer 
		stemmer = (SnowballStemmer) stemClass.newInstance();
	}
	
	private String stemTerm(String input){
		stemmer.setCurrent(input); //input before stem >> compressed  
        stemmer.stem(); //stem term
        String stemTerm = stemmer.getCurrent(); //output affer stem >> compress 
		return stemTerm;
	}
	
	private void initJDBM() throws Exception{
		//initDatabase ---JDBM---
		props = new Properties();
		// open database and setup an object cache
        recman = RecordManagerFactory.createRecordManager("term", props );

        // if DB exist then try to reload an existing B+Tree
        recid = recman.getNamedObject("Tokenizer");
        if ( recid != 0 ) {
            tree = BTree.load( recman, recid );
            System.out.println( "Reloaded existing BTree with " + tree.size()+" term." );
        } else {
            // create a new B+Tree data structure and use a StringComparator
            tree = BTree.createInstance( recman, new StringComparator() );
            recman.setNamedObject( "Tokenizer", tree.getRecid() );
            System.out.println( "Created a new empty BTree" );
        }
	}
	
	//Note Fix return type to String instead boolean
	private String searchTree(String key) throws IOException{
		 String result = null;
		 browser = tree.browse(key);
         while ( browser.getNext( tuple ) ) {
             String key1 = (String) tuple.getKey();
             if ( key1.matches(key)) {
            	//print 
            	result = key1;
                System.out.println(key1+" Found");
                break;
             } else {
                //System.out.println("Not Found");
            	result = "";
             }
         }
		return result;
	}
	/* 
	 * ProjectO
	 * GROUP3: 306, 308, 905
	 */
	
	private void listTree() throws IOException{
        // show list of index key & value
        System.out.println();
        System.out.println( "Key                   Value       " );
        System.out.println( "------------------       ------------------" );
        browser = tree.browse();
        while ( browser.getNext( tuple ) ) {
            print( tuple );
        }
	}
	
    private static void print( Tuple tuple ) {
        String key = (String) tuple.getKey();
        String value = (String) tuple.getValue();
        System.out.println( pad( key, 25) + value );
    }

    private static String pad( String str, int width ) {
        StringBuffer buf = new StringBuffer( str );
        int space = width-buf.length();
        while ( space-- > 0 ) {
            buf.append( ' ' );
        }
        return buf.toString();
    }

}
