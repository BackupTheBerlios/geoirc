/*
 * CommandAlias.java
 *
 * Created on July 9, 2003, 5:35 PM
 */

package geoirc;

import geoirc.util.Util;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.regex.*;

/**
 * Argument indices are 1-based, not 0-based.
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
    
    public String expand( String command_line, Hashtable variables )
    {
        String [] tokens = Util.tokensToArray( command_line );
        String retval = command_line;
        
        if( tokens[ 0 ].equals( alias ) )
        {
            retval = expansion;
            String i_str;
            for( int i = 1; i < tokens.length; i++ )
            {
                i_str = Integer.toString( i );
                
                // Single argument substitutions.
                
                retval = retval.replaceAll(
                    ALIAS_ARG_CHAR + i_str,
                    tokens[ i ]
                );
                
                // "Rest of line" substitutions.
                
                retval = retval.replaceAll(
                    ALIAS_ARG_REST_CHAR + i_str,
                    Util.stringArrayToString( tokens, i )
                );
                
            }
            
            // Missing arguments are just replaced with empty strings.
            retval = retval.replaceAll(
                ALIAS_ARG_CHAR + "\\d+",
                ""
            );
            retval = retval.replaceAll(
                ALIAS_ARG_REST_CHAR + "\\d+",
                ""
            );
            
            // Variable substitution

            for( Enumeration enum = variables.keys(); enum.hasMoreElements(); )
            {
                String variable = (String) enum.nextElement();
                String value = (String) variables.get( variable );
                if( value == null )
                {
                    value = "";
                }
                
                retval = retval.replaceAll(
                    ALIAS_ARG_CHAR + variable,
                    value
                );
            }
        }
        
        return retval;
    }
    
    public String getAlias()
    {
        return alias;
    }
    
    public String getExpansion()
    {
        return expansion;
    }
}
