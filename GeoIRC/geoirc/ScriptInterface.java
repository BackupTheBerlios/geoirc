/*
 * ScriptInterface.java
 *
 * Created on August 8, 2003, 3:54 PM
 */

package geoirc;

import java.util.Vector;
import org.python.core.*;
import org.python.util.PythonInterpreter;

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
    protected PythonInterpreter python_interpreter;
    
    protected PyObject py_object;
    
    protected Vector raw_listeners;
    protected Vector print_listeners;
    
    private ScriptInterface() { }
    
    public ScriptInterface(
        CommandExecutor executor,
        SettingsManager settings_manager,
        DisplayManager display_manager,
        VariableManager variable_manager,
        PythonInterpreter python_interpreter
    )
    {
        this.executor = executor;
        this.settings_manager = settings_manager;
        this.display_manager = display_manager;
        this.variable_manager = variable_manager;
        this.python_interpreter = python_interpreter;
        
        raw_listeners = new Vector();
        print_listeners = new Vector();
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
    
    public void registerRawListener( PyObject listener )
    {
        raw_listeners.add( listener );
    }
    
    public void onRaw( String line )
    {
        PyObject py_object;
        PyMethod method;
        for( int i = 0, n = raw_listeners.size(); i < n; i++ )
        {
            py_object = (PyObject) raw_listeners.elementAt( i );
            method = (PyMethod) py_object.__findattr__( new PyString( "onRaw" ) );
            if( method != null )
            {
                method.__call__( new PyString( line ) );
            }
        }
    }
}
