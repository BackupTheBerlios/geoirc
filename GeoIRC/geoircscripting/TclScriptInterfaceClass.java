/*
 * TclScriptInterface.java
 *
 * Created on September 16, 2003, 2:37 PM
 */

package geoircscripting;

import geoirc.*;
import geoirc.gui.DisplayManager;
import geoirc.util.Util;
import java.util.Hashtable;
import java.util.Vector;
import tcl.lang.*;

/**
 *
 * @author  Pistos
 */
public class TclScriptInterfaceClass implements geoirc.TclScriptInterface
{
    protected CommandExecutor executor;
    protected DisplayManager display_manager;
    protected SettingsManager settings_manager;
    protected VariableManager variable_manager;
    protected I18nManager i18n_manager;
    protected Interp tcl_interpreter;
    protected Vector tcl_procs;
    
    protected Vector raw_listeners;
    protected Vector input_listeners;
    protected Vector print_listeners;
    
    private TclScriptInterfaceClass() { }
    
    public TclScriptInterfaceClass(
        CommandExecutor executor,
        SettingsManager settings_manager,
        DisplayManager display_manager,
        VariableManager variable_manager,
        I18nManager i18n_manager,
        Vector tcl_procs
    ) throws TclException
    {
        this.executor = executor;
        this.settings_manager = settings_manager;
        this.display_manager = display_manager;
        this.variable_manager = variable_manager;
        this.i18n_manager = i18n_manager;
        this.tcl_procs = tcl_procs;
        
        tcl_interpreter = new Interp();
        raw_listeners = new Vector();
        input_listeners = new Vector();
        print_listeners = new Vector();
        
        tcl_interpreter.setVar(
            "geoirc",
            ReflectObject.newInstance( tcl_interpreter, TclScriptInterface.class, this ),
            0
        );
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
    
    public void registerRawListener( Object proc_ )
    {
        try
        {
            String proc = (String) proc_;
            raw_listeners.add( proc );
        } catch( ClassCastException e ) { }
    }
    
    public void registerInputListener( Object proc_ )
    {
        try
        {
            String proc = (String) proc_;
            input_listeners.add( proc );
        } catch( ClassCastException e ) { }
    }
    
    public void registerProc( String proc_name )
    {
        tcl_procs.add( proc_name );
    }
    
    public String [] onRaw( String line_, String qualities_ )
    {
        String tcl_proc;
        TclObject transformed_message = null;
        
        String line = line_;
        String qualities = qualities_;
        
        for( int i = 0, n = raw_listeners.size(); i < n; i++ )
        {
            tcl_proc = (String) raw_listeners.elementAt( i );
            if( tcl_proc != null )
            {
                try
                {
                    line = escapeTclChars( line );
                    qualities = escapeTclChars( qualities );
                    tcl_interpreter.eval(
                        tcl_proc + " "
                        + "\"" + line
                        + "\" \"" + qualities + "\""
                    );
                    transformed_message = tcl_interpreter.getResult();
                    if( transformed_message != null )
                    {
                        line = (TclList.index( tcl_interpreter, transformed_message, 0 )).toString();
                        qualities = (TclList.index( tcl_interpreter, transformed_message, 1 )).toString();
                    }
                }
                catch( TclException e )
                {
                    display_manager.printlnDebug( i18n_manager.getString( "tcl raw exception" ) );
                    if( e.getCompletionCode() == TCL.ERROR )
                    {
                        display_manager.printlnDebug( tcl_interpreter.getResult().toString() );
                    }
                }
            }
        }
        
        String [] retval = new String[ 2 ];
        retval[ 0 ] = line;
        retval[ 1 ] = qualities;
        
        return retval;
    }
    
    public String onInput( String input_line )
    {
        String tcl_proc;
        TclObject transformed_line = null;
        
        String line = input_line;
        
        for( int i = 0, n = input_listeners.size(); i < n; i++ )
        {
            tcl_proc = (String) input_listeners.elementAt( i );
            if( tcl_proc != null )
            {
                try
                {
                    line = escapeTclChars( line );
                    tcl_interpreter.eval( tcl_proc + " \"" + line + "\"" );
                    transformed_line = tcl_interpreter.getResult();
                    if( transformed_line != null )
                    {
                        line = transformed_line.toString();
                    }
                }
                catch( TclException e )
                {
                    display_manager.printlnDebug( i18n_manager.getString( "tcl input exception" ) );
                    if( e.getCompletionCode() == TCL.ERROR )
                    {
                        display_manager.printlnDebug( tcl_interpreter.getResult().toString() );
                    }
                }
            }
        }
        
        return line;
    }
    
    protected String escapeTclChars( String text )
    {
        String retval = text;
        retval = retval.replaceAll( "\\\\", "\\\\\\\\" );  // gee that's a lot of backslashes.  :)
        retval = retval.replaceAll( "\\{", "\\\\\\{" );
        retval = retval.replaceAll( "\\}", "\\\\\\}" );
        retval = retval.replaceAll( "\"", "\\\\\"" );
        retval = retval.replaceAll( "\\[", "\\\\\\[" );
        retval = retval.replaceAll( "\\]", "\\\\\\]" );
        retval = retval.replaceAll( "\\$", "\\\\\\$" );
        return retval;
    }
    
    public void eval( String tcl_code )
    {
        try
        {
            tcl_interpreter.eval( tcl_code );
        }
        catch( TclException e )
        {
            Util.printException( display_manager, e, i18n_manager.getString( "tcl error" ) );
        }
    }
    
    public void evalFile( String tcl_file )
    {
        try
        {
            tcl_interpreter.evalFile( tcl_file );
        }
        catch( TclException e )
        {
            Util.printException(
                display_manager, e,
                i18n_manager.getString( "load failure", new Object [] { tcl_file } )
            );
        }
    }
}
