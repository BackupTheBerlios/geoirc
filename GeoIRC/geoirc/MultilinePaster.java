/*
 * MultilinePaster.java
 *
 * Created on September 10, 2003, 2:49 PM
 */

package geoirc;

import javax.swing.JTextField;

/**
 *
 * @author  Pistos
 */
public class MultilinePaster extends Thread implements GeoIRCConstants
{
    protected InputFieldOwner ifo;
    protected String text;
    
    private MultilinePaster() { }
    
    public MultilinePaster(
        InputFieldOwner ifo,
        String text
    )
    {
        this.ifo = ifo;
        this.text = text;
    }
    
    public void run()
    {
        String one_line;
        int index = text.indexOf( '\n' );
        int lines_pasted = 0;
        int paste_flood_allowance = ifo.getFloodAllowance();
        int paste_flood_delay = ifo.getFloodDelay();

        while( index > -1 )
        {
            one_line = text.substring( 0, index );
            ifo.regularPaste( one_line );
            ifo.useInputField();
            lines_pasted++;
            
            if( lines_pasted > paste_flood_allowance )
            {
                try
                {
                    Thread.sleep( paste_flood_delay );
                } catch( InterruptedException e ) { }
            }

            text = text.substring( index + 1 );
            index = text.indexOf( '\n' );
        }
        
        ifo.regularPaste( text );
    }
}
