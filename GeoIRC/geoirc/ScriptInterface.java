/*
 * ScriptInterface.java
 *
 * Created on August 8, 2003, 3:54 PM
 */

package geoirc;

/**
 *
 * @author  Pistos
 */
public class ScriptInterface
{
    
    protected CommandExecutor executor;
    protected DisplayManager display_manager;
    protected SettingsManager settings_manager;
    protected VariableManager variable_manager;
    
    private ScriptInterface() { }
    
    public ScriptInterface(
        CommandExecutor executor,
        SettingsManager settings_manager,
        DisplayManager display_manager,
        VariableManager variable_manager
    )
    {
        this.executor = executor;
        this.settings_manager = settings_manager;
        this.display_manager = display_manager;
        this.variable_manager = variable_manager;
    }
    
    public int execute( String command )
    {
        return executor.execute( command );
    }
    
    public String get( String variable, String default_ )
    {
        return getString( variable, default_ );
    }
    public String getString( String variable, String default_ )
    {
        return variable_manager.getString( variable, default_ );
    }
    
    public int getInt( String variable, int default_ )
    {
        return variable_manager.getInt( variable, default_ );
    }
    
    public boolean getBoolean( String variable, boolean default_ )
    {
        return variable_manager.getBoolean( variable, default_ );
    }
}
