/*
 * GeoIRCConstants.java
 *
 * Created on June 27, 2003, 11:44 AM
 */

package geoirc;

import java.awt.event.InputEvent;

/**
 *
 * @author  Pistos
 */
public interface GeoIRCConstants
{
    static final String [] IRCMSGS =
    {
        "PRIVMSG"
    };
    static final int IRCMSG_PRIVMSG = 0;
    
    static final boolean NEXT_WINDOW = false;
    static final boolean PREVIOUS_WINDOW = true;
    
    static final int SHIFT_ = InputEvent.SHIFT_DOWN_MASK;
    static final int CTRL_ = InputEvent.CTRL_DOWN_MASK;
    static final int ALT_ = InputEvent.ALT_DOWN_MASK;
    static final int CTRL_ALT_ = InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK;
    static final int CTRL_SHIFT_ = InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK;
    static final int ALT_SHIFT_ = InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK;
    static final int CTRL_ALT_SHIFT_
        = InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK;
    
    static final int NO_MODIFIER_KEYS = 0;
    static final int SHIFT = 1;
    static final int CTRL = 2;
    static final int ALT = 4;
    
    /* It follows, then, that:
     *
     * CTRL+SHIFT = 3;
     * ALT+SHIFT = 5;
     * CTRL+ALT = 6;
     * CTRL+ALT+SHIFT = 7;
     *
     */
    
}
