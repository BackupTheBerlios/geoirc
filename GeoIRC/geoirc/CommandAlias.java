/*
 * CommandAlias.java
 *
 * Created on July 9, 2003, 5:35 PM
 */

package geoirc;

import geoirc.util.Util;

/**
 *
 * @author  Pistos
 */
public class CommandAlias implements GeoIRCConstants
{
    protected String alias;
    protected String expansion;
    
    private CommandAlias() { }

    public CommandAlias( String alias, String expansion )
    {
        this.alias = alias;
        this.expansion = expansion;
    }
    
    public String getAlias()
    {
        return alias;
    }
    
    public String expand( String arg_string )
    {
        String retval = null;
        String [] expansion_array = Util.tokensToArray( expansion );
        String [] args = Util.tokensToArray( arg_string );
        String token;
        String token_remainder;
        
        for( int i = 0; i < expansion_array.length; i++ )
        {
            token = expansion_array[ i ];
            token_remainder = token.substring( 1 );
            switch( token.charAt( 0 ) )
            {
                case ALIAS_ARG_CHAR:
                    try
                    {
                    }
                    catch( NumberFormatException e ) { }
                    break;
                case ALIAS_ARG_REST_CHAR:
                    break;
                default:
                    break;
            }
        }
        
        retval = Util.stringArrayToString( expansion_array );
        
        return retval;
    }
}
