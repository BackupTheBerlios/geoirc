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
import java.util.regex.Pattern;

/**
 *
 * @author  Pistos
 */
public class Util implements GeoIRCConstants
{
    protected static int last_error_code;
    
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
            case SORT_MODE_ALPHABETICAL_ASCENDING:
            case SORT_MODE_TIME_SINCE_LAST_ASCENDING:
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
    
    public static String get32BitAddressString( String ip_address )
    {
        String retval = null;
        
        if( Pattern.matches( "\\d+\\.\\d+\\.\\d+\\.\\d+", ip_address ) )
        {
            try
            {
                // Get the byte for each of the four parts of the IP address.
                
                String [] ip_parts = ip_address.split( "\\." );
                int [] bytes = new int[ 4 ];
                for( int i = 0; i < 4; i++ )
                {
                    bytes[ i ] = Integer.parseInt( ip_parts[ i ] );
                }
                
                long net_addr =
                    bytes[ 0 ] * 0x1000000
                    + bytes[ 1 ] * 0x10000
                    + bytes[ 2 ] * 0x100
                    + bytes[ 3 ];
                
                retval = Long.toString( net_addr );
            }
            catch( NumberFormatException e ) { }
        }
        
        return retval;
    }
    
    public static String getIPAddressString( String _32bit_address_string )
    {
        // Example: DCC CHAT chat 3655733111 4453
        // 3655733111 == 0xD9E60F77
        // 0xD9 == 217
        // 0xE6 == 230
        // 0x0F == 15
        // 0x77 == 119
        // 217.230.15.119
        
        long ip = Long.parseLong( _32bit_address_string );
        return Long.toString( ( ip & 0xFF000000 ) / 0x1000000 ) + "."
            + Long.toString( ( ip & 0x00FF0000 ) / 0x10000 ) + "."
            + Long.toString( ( ip & 0x0000FF00 ) / 0x100 ) + "."
            + Long.toString( ip & 0x000000FF );
    }
    
    public static int getLastErrorCode()
    {
        return last_error_code;
    }
    
    /**
     * @return a String containing a space-separated list of possible matches,
     * or the empty string if no words from among possible_words are a
     * completion for word.  Util.getLastErrorCode() gives ...
     */
    public static String completeFrom( String word, String [] possible_words, DisplayManager display_manager )
    {
        Vector words_found = new Vector();
        String retval = "";

        for( int i = 0, n = possible_words.length; i < n; i++ )
        {
            if( possible_words[ i ].startsWith( word ) )
            {
                words_found.add( possible_words[ i ] );
            }
        }

        if( words_found.size() == 0 )
        {
            last_error_code = COMPLETE_NONE_FOUND;
        }
        else if( words_found.size() == 1 )
        {
            last_error_code = COMPLETE_ONE_FOUND;
            retval = (String) words_found.elementAt( 0 );
        }
        else
        {
            int word_len = word.length();
            int matches_up_to = ((String) words_found.elementAt( 0 )).length();
            String wrd;
            String prev_wrd;
            String multiple_matches = "";
            
            for( int i = 0, n = words_found.size(); i < n; i++ )
            {
                wrd = ((String) words_found.elementAt( i ));
                if( i > 0 )
                {
                    prev_wrd = ((String) words_found.elementAt( i - 1 ));
                    for( int j = matches_up_to; j >= word_len; j-- )
                    {
                        if( j > prev_wrd.length() )
                        {
                            j = prev_wrd.length();
                        }
                        if( j > wrd.length() )
                        {
                            j = wrd.length();
                        }
                        if( wrd.substring( 0, j ).equals( prev_wrd.substring( 0, j ) ) )
                        {
                            matches_up_to = j;
                            break;
                        }
                    }
                }
                multiple_matches += wrd + " ";
            }

            display_manager.printlnDebug( multiple_matches );
            retval = ((String) words_found.elementAt( 0 )).substring( 0, matches_up_to );
            last_error_code = COMPLETE_MORE_THAN_ONE_FOUND;
        }

        return retval;
    }

    public static String[] getStringsCuttedToEqualLength(String string1, String string2)
    {
        int dif = string1.length() - string2.length();                
                
        if( dif > 0)
        {
            string1 = string1.substring(0, string2.length());
        }
        else if ( dif < 0)
        {
            string2 = string2.substring(0, string1.length());
        }
                
        return new String[] { string1, string2 };
    }

    public static int fitInt( int number, int min, int max )
    {
        int retval = number;
        if( retval < min )
        {
            retval = min;
        }
        else if( retval > max )
        {
            retval = max;
        }
        
        return retval;
    }
    
    public static String getQueryWindowFilter( String remote_nick )
    {
        return
            "from=" + remote_nick + " and "
            + FILTER_SPECIAL_CHAR + "self "
            + "or "
            + remote_nick + " and "
            + "from=" + FILTER_SPECIAL_CHAR + "self";
    }
}
