/*
 * BoolExpTester.java
 *
 * Created on June 25, 2003, 12:14 PM
 */

package geoirc.util;

/**
 *
 * @author  Pistos
 */
public class BoolExpTester {
    
    /** Creates a new instance of BoolExpTester */
    public BoolExpTester() {
    }
    
    public void test( String expr, String qualities )
    {
        boolean result;
        try
        {
            result = BoolExpEvaluator.evaluate( expr, qualities );
            System.out.println( expr );
            System.out.println( qualities );
            System.out.println( result ? "T" : "F" );
        }
        catch( BadExpressionException e )
        {
            System.out.println( "Bad expression: " + expr );
            System.out.println( e.getMessage() );
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        BoolExpTester bet = new BoolExpTester();
        bet.test( "not(a or b or c)", "a" );
    }
    
}
