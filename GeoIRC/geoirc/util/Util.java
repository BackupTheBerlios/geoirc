/*
 * Util.java
 *
 * Created on June 27, 2003, 9:36 AM
 */

package geoirc.util;

import java.util.*;

/**
 *
 * @author  Pistos
 */
public class Util
{
    
    // No default constructor
    private Util() { }
    
    public static String stringArrayToString( String [] array )
    {
        return stringArrayToString( array, 0 );
    }
    
    public static String stringArrayToString( String [] array, int from_element )
    {
        if( ( array == null ) || ( from_element >= array.length ) )
        {
            return null;
        }
        if( from_element < 0 )
        {
            from_element = 0;
        }
        
        String retval = array[ from_element ];
        for( int i = from_element + 1; i < array.length; i++ )
        {
            retval = retval + " " + array[ i ];
        }
        
        return retval;
    }
    
    public static String [] tokensToArray( String string )
    {
        if( string == null ) { return null; }
        StringTokenizer st = new StringTokenizer( string );
        Vector tokens = new Vector();

        while( st.hasMoreTokens() )
        {
            tokens.add( st.nextToken() );
        }

        int num_tokens = tokens.size();
        if( num_tokens > 0 )
        {
            String [] array = new String[ tokens.size() ];
            return (String []) tokens.toArray( array );
        }
        else
        {
            return null;
        }
    }
    
}
