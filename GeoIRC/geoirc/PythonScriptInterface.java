/*
 * PythonScriptInterface.java
 *
 * Created on October 10, 2003, 12:00 PM
 */

package geoirc;

/**
 *
 * @author  Pistos
 */
public interface PythonScriptInterface extends ScriptInterface
{
    void registerMethod( Object object, Object method );
    void execMethod( String [] args );
}
