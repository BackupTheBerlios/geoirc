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
    protected DCCAgent dcc_agent;
    
    private DCCRequest() { }
    
    /**
     * @param args the arguments given to a DCC command sent from a remote client
     */
    public DCCRequest( String [] args, DCCAgent agent, String remote_nick )
        throws ArrayIndexOutOfBoundsException,
            NumberFormatException,
            IllegalArgumentException
    {
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
        port_str = args[ 3 ];

        ip_str = Util.getIPAddressString( address_str );

        // Parse for the sake of checking data correctness.
        Integer.parseInt( port_str );
        
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
            ip_str, port_str, type, user_nick, remote_nick
        );
        dcc_client.connect();
    }
    
    public int getType()
    {
        return type;
    }
    
    public String toString()
    {
        return remote_nick + " @ " + ip_str + ":" + port_str + " (" + arg1 + ")";
    }
}
