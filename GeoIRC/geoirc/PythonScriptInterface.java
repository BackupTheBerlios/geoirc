/*
 * PythonScriptInterface.java
 *
 * Created on August 8, 2003, 3:54 PM
 */

package geoirc;

import geoirc.util.Util;
import java.util.Hashtable;
import java.util.Vector;
import org.python.core.*;
import org.python.util.PythonInterpreter;

/**
 * This object is accessed in python scripts as an object by the name of "geoirc".
 * @author  Pistos
 */
public class PythonScriptInterface
{
    
    protected CommandExecutor executor;
    protected DisplayManager display_manager;
    protected SettingsManager settings_manager;
    protected VariableManager variable_manager;
    protected I18nManager i18n_manager;
    protected PythonInterpreter python_interpreter;
    protected Hashtable python_methods;
    
    protected Vector raw_listeners;
    protected Vector input_listeners;
    protected Vector print_listeners;
    
    private PythonScriptInterface() { }
    
    public PythonScriptInterface(
        CommandExecutor executor,
        SettingsManager settings_manager,
        DisplayManager display_manager,
        VariableManager variable_manager,
        I18nManager i18n_manager,
        PythonInterpreter python_interpreter,
        Hashtable python_methods
    )
    {
        this.executor = executor;
        this.settings_manager = settings_manager;
        this.display_manager = display_manager;
        this.variable_manager = variable_manager;
        this.i18n_manager = i18n_manager;
        this.python_interpreter = python_interpreter;
        this.python_methods = python_methods;
        
        raw_listeners = new Vector();
        input_listeners = new Vector();
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
    
    public void registerInputListener( PyObject listener )
    {
        input_listeners.add( listener );
    }
    
    public void registerMethod( PyString object, PyString method )
    {
        String object_name = object.toString();
        String reference = object_name + "." + method.toString();
        PyObject py_object = python_interpreter.get( object_name );
        PyMethod py_method = (PyMethod) py_object.__findattr__( method );
        python_methods.put( reference, py_method );
    }
    
    public String [] onRaw( String line_, String qualities_ )
    {
        PyObject py_object;
        PyMethod method;
        PyObject transformed_message = null;
        
        String line = line_;
        String qualities = qualities_;
        
        for( int i = 0, n = raw_listeners.size(); i < n; i++ )
        {
            py_object = (PyObject) raw_listeners.elementAt( i );
            method = (PyMethod) py_object.__findattr__( new PyString( "onRaw" ) );
            if( method != null )
            {
                try
                {
                    transformed_message = method.__call__( new PyString( line ), new PyString( qualities ) );
                }
                catch( Exception e )
                {
                    Util.printException(
                        display_manager,
                        e,
                        i18n_manager.getString( "python raw exception" )
                    );
                }
                if( transformed_message != null )
                {
                    line = ( transformed_message.__findattr__( new PyString( "text" ) ) ).toString();
                    qualities = ( transformed_message.__findattr__( new PyString( "qualities" ) ) ).toString();
                }
            }
        }
        
        String [] retval = new String[ 2 ];
        retval[ 0 ] = line;
        retval[ 1 ] = qualities;
        
        return retval;
    }

    public String onInput( String line_ )
    {
        PyObject py_object;
        PyMethod method;
        PyObject transformed_line = null;
        
        String line = line_;
        
        for( int i = 0, n = input_listeners.size(); i < n; i++ )
        {
            py_object = (PyObject) input_listeners.elementAt( i );
            method = (PyMethod) py_object.__findattr__( new PyString( "onInput" ) );
            if( method != null )
            {
                try
                {
                    transformed_line = method.__call__( new PyString( line ) );
                }
                catch( Exception e )
                {
                    Util.printException(
                        display_manager,
                        e,
                        i18n_manager.getString( "python input exception" )
                    );
                }
                if( transformed_line != null )
                {
                    line = transformed_line.toString();
                }
            }
        }
        
        return line;
    }
    
}
