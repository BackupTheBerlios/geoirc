/*
 * ScriptInterface.java
 *
 * Created on October 10, 2003, 11:49 AM
 */

package geoirc;

/**
 *
 * @author  Pistos
 */
public interface ScriptInterface
{
    int execute( String command );
    String get( String variable, String default_ );
    String getString( String variable, String default_ );
    int getInt( String variable, int default_ );
    boolean getBoolean( String variable, boolean default_ );
    void registerRawListener( Object listener );
    void registerInputListener( Object listener );
    String [] onRaw( String line_, String qualities_ );
    String onInput( String line_ );
    void evalFile( String file );
}
