/*
 * VariableManager.java
 *
 * Created on July 23, 2003, 5:22 PM
 */

package geoirc;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 *
 * @author  Pistos
 */
public class VariableManager implements GeoIRCConstants
{
    protected Hashtable variables;
    
    public VariableManager()
    {
        variables = new Hashtable();
    }
    
    protected synchronized String getValue( String variable )
    {
        return (String) variables.get( variable );
    }
    
    public synchronized String get( String variable, String default_ )
    {
        return getString( variable, default_ );
    }
    public synchronized String getString( String variable, String default_ )
    {
        String value = getValue( variable );
        if( value == null )
        {
            value = default_;
        }
        return value;
    }
    
    public synchronized int getInt( String variable, int default_ )
    {
        String value_str = getValue( variable );
        int value = default_;
        try
        {
            value = Integer.parseInt( value_str );
        }
        catch( NumberFormatException e ) { }
        
        return value;
    }
    
    public synchronized boolean getBoolean( String variable, boolean default_ )
    {
        String value_str = getValue( variable );
        boolean value = default_;
        if( value_str != null )
        {
            value = Boolean.valueOf( value_str ).booleanValue();
        }
        
        return value;
    }
    
    public synchronized void set( String variable, String value )
    {
        setString( variable, value );
    }
    public synchronized void setString( String variable, String value )
    {
        variables.put( variable, value );
    }
    
    public synchronized void setInt( String variable, int value )
    {
        String value_str = Integer.toString( value );
        variables.put( variable, value_str );
    }
    
    public synchronized void setBoolean( String variable, boolean value )
    {
        String value_str = Boolean.toString( value );
        variables.put( variable, value_str );
    }
    
    public synchronized int incrementInt( String variable )
    {
        int value = getInt( variable, 0 ) + 1;
        setInt( variable, value );
        return value;
    }
    
    /**
     * Replaces all instances of VARIABLE_CHAR + variablename in the
     * input string with the value of the variable.
     */
    public String replaceAll( String input, String variable )
    {
        String retval = null;
        if( input != null )
        {
            retval = input.replaceAll(
                VARIABLE_CHAR + variable,
                getValue( variable )
            );
        }
        return retval;
    }
    
    public String replaceAll( String input )
    {
        String variable;
        String retval = input;
        
        for( Enumeration enum = variables.keys(); enum.hasMoreElements(); )
        {
            variable = (String) enum.nextElement();
            retval = replaceAll( retval, variable );
        }
        
        return retval;
    }
}
