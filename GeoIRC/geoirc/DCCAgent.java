/*
 * DCCAgent.java
 *
 * Created on August 19, 2003, 12:10 PM
 */

package geoirc;

/**
 *
 * @author  Pistos
 */
public interface DCCAgent
{
    public DCCClient addDCCClient(
        String hostname,
        String port,
        int type,
        String user_nick,
        String remote_nick,
        String arg1,
        int filesize
    );
}
