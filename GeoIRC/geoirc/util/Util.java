/*
 * Util.java
 *
 * Created on June 27, 2003, 9:36 AM
 */

package geoirc.util;

import geoirc.DisplayManager;
import geoirc.GeoIRCConstants;

import java.awt.Color;
import java.util.*;

/**
 *
 * @author  Pistos
 */
public class Util implements GeoIRCConstants
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
    
    public static int [] getRGB( String rgb_string ) throws NumberFormatException
    {
        int [] retval = new int[ 3 ];
        
        retval[ 0 ] = Integer.parseInt( rgb_string.substring( 0, 2 ), 16 );
        retval[ 1 ] = Integer.parseInt( rgb_string.substring( 2, 4 ), 16 );
        retval[ 2 ] = Integer.parseInt( rgb_string.substring( 4, 6 ), 16 );
        
        for( int i = 0; i < 3; i++ )
        {
            if( retval[ i ] < 0 )
            {
                retval[ i ] = 0;
            }
            else if( retval[ i ] > 255 )
            {
                retval[ i ] = 255;
            }
        }
        
        return retval;
    }
    
    public static void printException(
        DisplayManager display_manager,
        Throwable t,
        String user_message
    )
    {
        if( user_message != null )
        {
            display_manager.printlnDebug( user_message );
        }
        
        display_manager.printlnDebug( t.getMessage() );
        
        StackTraceElement [] stes = t.getStackTrace();
        for( int i = 0, n = stes.length; i < n; i++ )
        {
            display_manager.printlnDebug( stes[ i ].toString() );
        }
    }
    
    public static boolean isValidSortOrder( int sort_order )
    {
        boolean is_valid = false;
        
        switch( sort_order )
        {
            case SORT_ALPHABETICAL_ASCENDING:
            case SORT_TIME_SINCE_LAST_ASCENDING:
                is_valid = true;
                break;
        }
        
        return is_valid;
    }
    
    public static String getPadding( String character, int reps )
    {
        String retval = "";
        
        for( int i = 0; i < reps; i++ )
        {
            retval += character;
        }
        
        return retval;
    }
    
	/**
     * @param color
     * @return
     */
    public static String colorToHexString(Color color)
	{
		int[] col = { color.getRed(), color.getGreen(), color.getBlue() };
		StringBuffer hex = new StringBuffer();
		
		for(int i = 0; i < col.length; i++)
		{
			if(col[i] == 0)
				hex.append("00");
			else
				hex.append(Integer.toHexString(col[i]));			
		}
		
		return hex.toString();
	}
    
    /**
     * @param object
     * @param def
     * @return
     */
    public static Object getDefaultIfNull(Object object, Object def)
    {
        if(object != null)
            return object;
        
        return def;    
    }
    
    /**
     * @param object
     * @param def
     * @return
     */
    public static String getDefaultIfNull(Object object, String def)
    {
        return getDefaultIfNull(object, (Object)def).toString();
    }
}
