/*
 * VariableManager.java
 *
 * Created on July 23, 2003, 5:22 PM
 */

package geoirc;

import java.util.Hashtable;

/**
 *
 * @author  Pistos
 */
public class VariableManager
{
    protected DisplayManager display_manager;
    protected Hashtable variables;
    
    private VariableManager() { }
 
    public VariableManager( DisplayManager display_manager )
    {
        this.display_manager = display_manager;
        variables = new Hashtable();
    }
    
    protected String getValue( String variable )
    {
        return (String) variables.get( variable );
    }
    
    public String get( String variable, String default_ )
    {
        return getString( variable, default_ );
    }
    public String getString( String variable, String default_ )
    {
        String value = getValue( variable );
        if( value == null )
        {
            value = default_;
        }
        return value;
    }
    
    public int getInt( String variable, int default_ )
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
    
    public boolean getBoolean( String variable, boolean default_ )
    {
        String value_str = getValue( variable );
        boolean value = default_;
        if( value_str != null )
        {
            value = Boolean.valueOf( value_str ).booleanValue();
        }
        
        return value;
    }
    
    public void set( String variable, String value )
    {
        setString( variable, value );
    }
    public void setString( String variable, String value )
    {
        variables.put( variable, value );
    }
    
    public void setInt( String variable, int value )
    {
        String value_str = Integer.toString( value );
        variables.put( variable, value_str );
    }
    
    public void setBoolean( String variable, boolean value )
    {
        String value_str = Boolean.toString( value );
        variables.put( variable, value_str );
    }
    
    public String replaceAll( String input )
    {
        
    }
}
