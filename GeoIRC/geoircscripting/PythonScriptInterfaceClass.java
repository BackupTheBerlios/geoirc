/*
 * PythonScriptInterface.java
 *
 * Created on August 8, 2003, 3:54 PM
 */

package geoircscripting;

import geoirc.*;
import geoirc.gui.DisplayManager;
import geoirc.util.Util;
import java.util.Hashtable;
import java.util.Vector;
import org.python.core.PyJavaInstance;
import org.python.core.PyMethod;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;

/**
 * This object is accessed in python scripts as an object by the name of "geoirc".
 * @author  Pistos
 */
public class PythonScriptInterfaceClass implements geoirc.PythonScriptInterface
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
    
    private PythonScriptInterfaceClass() { }
    
    public PythonScriptInterfaceClass(
        CommandExecutor executor,
        SettingsManager settings_manager,
        DisplayManager display_manager,
        VariableManager variable_manager,
        I18nManager i18n_manager
    )
    {
        this.executor = executor;
        this.settings_manager = settings_manager;
        this.display_manager = display_manager;
        this.variable_manager = variable_manager;
        this.i18n_manager = i18n_manager;
        
        python_interpreter = new PythonInterpreter();
        python_interpreter.set( "geoirc", new PyJavaInstance( this ) );
        
        raw_listeners = new Vector();
        input_listeners = new Vector();
        print_listeners = new Vector();
        python_methods = new Hashtable();
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
    
    public void registerRawListener( Object listener_ )
    {
        try
        {
            PyObject listener = (PyObject) listener_;
            raw_listeners.add( listener );
        } catch( ClassCastException e ) { }
    }
    
    public void registerInputListener( Object listener_ )
    {
        try
        {
            PyObject listener = (PyObject) listener_;
            input_listeners.add( listener );
        } catch( ClassCastException e ) { }
    }
    
    public void registerMethod( Object object_, Object method_ )
    {
        try
        {
            PyString object = (PyString) object_;
            PyString method = (PyString) method_;
            String object_name = object.toString();
            String reference = object_name + "." + method.toString();
            PyObject py_object = python_interpreter.get( object_name );
            PyMethod py_method = (PyMethod) py_object.__findattr__( method );
            python_methods.put( reference, py_method );
        } catch( ClassCastException e ) { }
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
    
    public void evalFile( String python_file )
    {
        python_interpreter.execfile( python_file );
    }
    
    public void execMethod( String [] args )
    {
        PyMethod method = (PyMethod) python_methods.get( args[ 0 ] );
        if( method != null )
        {
            try
            {
                if( args.length > 1 )
                {
                    method.__call__( new PyString( Util.stringArrayToString( args, 1 ) ) );
                }
                else
                {
                    method.__call__();
                }
            }
            catch( Exception e )
            {
                Util.printException(
                    display_manager,
                    e,
                    i18n_manager.getString( "py method failure" ) );
            }
        }
        
    }
    
}
