/*
 * AliasManager.java
 *
 * Created on July 10, 2003, 9:39 AM
 */

package geoirc;

import java.util.Vector;

/**
 * Note that the order of the aliases in the settings is the order of
 * expansion.  That is, the expansion of lower-numbered aliases are
 * also expanded by higher-numbered aliases.
 *
 * @author  Pistos
 */
public class AliasManager implements GeoIRCConstants
{
    protected SettingsManager settings_manager;
    protected DisplayManager display_manager;
    protected Vector aliases;
    
    // No default constructor
    private AliasManager() { }
    
    public AliasManager( SettingsManager settings_manager, DisplayManager display_manager )
    {
        this.settings_manager = settings_manager;
        this.display_manager = display_manager;
        aliases = new Vector();
        
        int i = 0;
        String i_str;
        String alias;
        String expansion;
        
        while( GOD_IS_GOOD )
        {
            i_str = Integer.toString( i );
            
            alias = settings_manager.getString(
                "/command aliases/" + i_str + "/alias",
                ""
            );
            if( alias.equals( "" ) )
            {
                // No more command aliases stored in the settings.
                break;
            }
            
            expansion = settings_manager.getString(
                "/command aliases/" + i_str + "/expansion",
                ""
            );
            
            addAlias( alias, expansion );
            
            i++;
        }
    }
    
    public String expand( String command )
    {
        String expansion = command;
        int n = aliases.size();
        CommandAlias ca;
        for( int i = 0; i < n; i++ )
        {
            ca = (CommandAlias) aliases.elementAt( i );
            expansion = ca.expand( expansion );
        }
        
        return expansion;
    }
    
    protected void addAlias( String alias, String expansion )
    {
        CommandAlias ca = new CommandAlias( alias, expansion );
        aliases.add( ca );
    }
    
    public String [] getAliases()
    {
        CommandAlias [] alias_array = new CommandAlias[ 0 ];
        alias_array = (CommandAlias []) aliases.toArray( alias_array );
        String [] retval = new String[ alias_array.length ];
        for( int i = 0; i < alias_array.length; i++ )
        {
            retval[ i ] = alias_array[ i ].getAlias() + " "
                + alias_array[ i ].getExpansion();
        }
        return retval;
    }
}
