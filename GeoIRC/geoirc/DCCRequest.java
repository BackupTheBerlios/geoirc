/*
 * DCCRequest.java
 *
 * Created on August 19, 2003, 11:49 AM
 */

package geoirc;

import geoirc.util.Util;

/**
 *
 * @author  Pistos
 */
public class DCCRequest implements GeoIRCConstants
{
    protected int type;
    protected String arg1;
    protected String arg2;
    protected String ip_str;
    protected String port_str;
    protected String remote_nick;
    protected int filesize;
    protected DCCAgent dcc_agent;
    protected SettingsManager settings_manager;
    
    private DCCRequest() { }
    
    /**
     * @param args the arguments given to a DCC command sent from a remote client
     */
    public DCCRequest( String [] args, DCCAgent agent, String remote_nick, SettingsManager settings_manager )
        throws ArrayIndexOutOfBoundsException,
            NumberFormatException,
            IllegalArgumentException
    {
        this.settings_manager = settings_manager;
        
        if( args[ 0 ].toUpperCase().equals( "CHAT" ) )
        {
            type = DCC_CHAT;
        }
        else if( args[ 0 ].toUpperCase().equals( "SEND" ) )
        {
            type = DCC_SEND;
        }
        else
        {
            throw new IllegalArgumentException();
        }
        
        arg1 = args[ 1 ];
        String address_str = args[ 2 ];
        ip_str = Util.getIPAddressString( address_str );
        port_str = args[ 3 ];
        // Parse the port string for the sake of checking data correctness.
        Integer.parseInt( port_str );
        if( args.length > 4 )
        {
            filesize = Integer.parseInt( args[ 4 ] );
            int max_size = settings_manager.getInt(
                "/dcc/file transfers/maximum received file size",
                DEFAULT_MAX_DCC_SEND_FILESIZE
            );
            if( ( filesize < 1 ) || ( filesize > max_size ) )
            {
                throw new IllegalArgumentException();
            }
        }
        else if( type == DCC_SEND )
        {
            throw new IllegalArgumentException();
        }
        else
        {
            filesize = -1;
        }
        
        if( agent != null )
        {
            dcc_agent = agent;
        }
        else
        {
            throw new IllegalArgumentException();
        }
        this.remote_nick = remote_nick;
    }
    
    public void accept( String user_nick )
    {
        DCCClient dcc_client = dcc_agent.addDCCClient(
            ip_str, port_str, type, user_nick, remote_nick, arg1, filesize
        );
        dcc_client.connect();
    }
    
    public int getType()
    {
        return type;
    }
    
    public String toString()
    {
        String retval = "";
        switch( type )
        {
            case DCC_CHAT:
                retval += "Chat: ";
                break;
            case DCC_SEND:
                retval += "Send: ";
                break;
        }
        retval += remote_nick + " @ " + ip_str + ":" + port_str + " (" + arg1 + ")";
        
        return retval;
    }
}
