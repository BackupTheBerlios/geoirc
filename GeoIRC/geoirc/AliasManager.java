/*
 * AliasManager.java
 *
 * Created on July 10, 2003, 9:39 AM
 */

package geoirc;

import java.util.Vector;

/**
 *
 * @author  Pistos
 */
public class AliasManager
{
    protected SettingsManager settings_manager;
    protected Vector aliases;
    
    // No default constructor
    private AliasManager() { }
    
    public AliasManager( SettingsManager settings_manager )
    {
        this.settings_manager = settings_manager;
        
        
    }
    
}
