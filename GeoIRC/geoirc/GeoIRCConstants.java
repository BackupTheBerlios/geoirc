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
    static final boolean GOD_IS_GOOD = true;
    
    public static final String [] CMDS =
    {
        "sendraw",
        "newwindow",
        "join",
        "me", // emote/action
        "listfonts",
        "newserver",
        "nick",
        "nextwindow",
        "previouswindow",
        "next_history_entry",
        "previous_history_entry",
        "server",
        "changeserver",
        "listservers",
        "part",
        "listchannels",
        "set",
        "setfilter",
        "settitle",
        "privmsg",
        "msg"
    };
    public static final int UNKNOWN_COMMAND = -1;
    public static final int CMD_ACTION = 3;
    public static final int CMD_CHANGE_SERVER = 12;
    public static final int CMD_JOIN = 2;
    public static final int CMD_LIST_CHANNELS = 15;
    public static final int CMD_LIST_FONTS = 4;
    public static final int CMD_LIST_SERVERS = 13;
    public static final int CMD_MSG = 20;
    public static final int CMD_NEW_SERVER = 5;
    public static final int CMD_NEW_TEXT_WINDOW = 1;
    public static final int CMD_NEXT_HISTORY_ENTRY = 9;
    public static final int CMD_NEXT_WINDOW = 7;
    public static final int CMD_NICK = 6;
    public static final int CMD_PART = 14;
    public static final int CMD_PREVIOUS_WINDOW = 8;
    public static final int CMD_PREVIOUS_HISTORY_ENTRY = 10;
    public static final int CMD_PRIVMSG = 19;
    public static final int CMD_SEND_RAW = 0;
    public static final int CMD_SERVER = 11;
    public static final int CMD_SET = 16;
    public static final int CMD_SET_FILTER = 17;
    public static final int CMD_SET_TITLE = 18;
    
    static final String [] IRCMSGS =
    {
        "PRIVMSG",
        "JOIN",
        "PART",
        "QUIT"
    };
    static final int IRCMSG_PRIVMSG = 0;
    static final int IRCMSG_JOIN = 1;
    static final int IRCMSG_PART = 2;
    static final int IRCMSG_QUIT = 3;
    
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
    
    static final String DEFAULT_SETTINGS_FILEPATH = "./settings.xml";
    
    static final int GI_NORMAL = 0;
    static final int GI_MAXIMIZED = 1;
    static final int GI_MINIMIZED = 2;
    
    static final String STYLE_ESCAPE_SEQUENCE = Character.toString( (char) 255 );
    static final String STYLE_TERMINATION_SEQUENCE = ";";
    static final String STYLE_FOREGROUND = "fg";
    static final String STYLE_BACKGROUND = "bg";
    static final String STYLE_BOLD = "bo";
    static final String STYLE_ITALIC = "it";
    static final String STYLE_UNDERLINE = "un";
}
