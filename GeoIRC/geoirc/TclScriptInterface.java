/*
 * TclScriptInterface.java
 *
 * Created on October 10, 2003, 12:01 PM
 */

package geoirc;

/**
 *
 * @author  Pistos
 */
public interface TclScriptInterface extends ScriptInterface
{
    void registerProc( String proc_name );
    void eval( String tcl_code );
}
