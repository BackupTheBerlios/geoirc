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
    protected SettingsManager settings_manager;
    protected JTextField input_field;
    protected String text;
    
    private MultilinePaster() { }
    
    public MultilinePaster(
        InputFieldOwner ifo,
        SettingsManager settings_manager,
        JTextField input_field,
        String text
    )
    {
        this.ifo = ifo;
        this.settings_manager = settings_manager;
        this.input_field = input_field;
        this.text = text;
    }
    
    public void run()
    {
        String one_line;
        int index = text.indexOf( '\n' );
        int caret_pos = input_field.getCaretPosition();
        String field_text = input_field.getText();
        boolean used = false;
        int lines_pasted = 0;
        int paste_flood_allowance = settings_manager.getInt(
            "/misc/paste flood/allowance",
            DEFAULT_PASTE_FLOOD_ALLOWANCE
        );
        int paste_flood_delay = settings_manager.getInt(
            "/misc/paste flood/delay",
            DEFAULT_PASTE_FLOOD_DELAY
        );

        while( index > -1 )
        {
            one_line = text.substring( 0, index );
            if( ! used )
            {
                input_field.setText(
                    field_text.substring( 0, caret_pos )
                    + one_line
                    + field_text.substring( caret_pos )
                );

                used = true;
            }
            else
            {
                input_field.setText( one_line );
            }
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
        
        caret_pos = input_field.getCaretPosition();
        field_text = input_field.getText();
        input_field.setText(
            field_text.substring( 0, caret_pos )
            + text
            + field_text.substring( caret_pos )
        );
        input_field.setCaretPosition( caret_pos + text.length() );
    }
}
