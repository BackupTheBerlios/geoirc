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
    static final String GEOIRC_VERSION = "0.2.0a";
    
    static final String BASE_GEOIRC_TITLE = "GeoIRC";
    static final String DEFAULT_NEW_CONTENT_TITLE_PREFIX = "*";
    
    static final boolean GOD_IS_GOOD = true;
    
    static final int DEFAULT_IDENT_PORT = 113;
    static final int DEFAULT_PORT = 6667;
    
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
        "disconnect",
        "listconnections",
        "part",
        "listchannels",
        "set",
        "setfilter",
        "settitle",
        "privmsg",
        "msg",
        "changewindow",
        "switchwindow",
        "help",
        "completenick",
        "print",
        "focus_on_input_field",
        "listwindows",
        "dockwindow",
        "newinfowindow",
        "newtextwindow",
        "exit",
        "listdockedwindows",
        "floatwindow",
        "undockwindow",
        "pageup",
        "pagedown",
        "nudgeup",
        "nudgedown",
        "connect",
        "exec",
        "listmembers",
        "showqualities",
        "exec2",
        "play",
        "quote",
        "raw",
        "println"
    };
    public static final int UNKNOWN_COMMAND = -1;
    public static final int CMD_ACTION = 3;
    public static final int CMD_CHANGE_WINDOW = 22;
    public static final int CMD_COMPLETE_NICK = 24;
    public static final int CMD_CONNECT = 39;
    public static final int CMD_DISCONNECT = 12;
    public static final int CMD_DOCK_WINDOW = 28;
    public static final int CMD_EXEC = 40;
    public static final int CMD_EXEC2 = 43;
    public static final int CMD_EXIT = 31;
    public static final int CMD_FLOAT_WINDOW = 33;
    public static final int CMD_FOCUS_ON_INPUT_FIELD = 26;
    public static final int CMD_HELP = 23;
    public static final int CMD_JOIN = 2;
    public static final int CMD_LIST_CHANNELS = 15;
    public static final int CMD_LIST_CONNECTIONS = 13;
    public static final int CMD_LIST_DOCKED_WINDOWS = 32;
    public static final int CMD_LIST_FONTS = 4;
    public static final int CMD_LIST_MEMBERS = 41;
    public static final int CMD_LIST_WINDOWS = 27;
    public static final int CMD_MSG = 20;
    public static final int CMD_NEW_INFO_WINDOW = 29;
    public static final int CMD_NEW_SERVER = 5;
    public static final int CMD_NEW_TEXT_WINDOW = 30;
    public static final int CMD_NEW_WINDOW = 1;
    public static final int CMD_NEXT_HISTORY_ENTRY = 9;
    public static final int CMD_NEXT_WINDOW = 7;
    public static final int CMD_NICK = 6;
    public static final int CMD_NUDGE_DOWN = 38;
    public static final int CMD_NUDGE_UP = 37;
    public static final int CMD_PAGE_DOWN = 36;
    public static final int CMD_PAGE_UP = 35;
    public static final int CMD_PART = 14;
    public static final int CMD_PLAY = 44;
    public static final int CMD_PREVIOUS_WINDOW = 8;
    public static final int CMD_PREVIOUS_HISTORY_ENTRY = 10;
    public static final int CMD_PRINT = 25;
    public static final int CMD_PRINTLN = 47;
    public static final int CMD_PRIVMSG = 19;
    public static final int CMD_QUOTE = 45;
    public static final int CMD_RAW = 46;
    public static final int CMD_SEND_RAW = 0;
    public static final int CMD_SERVER = 11;
    public static final int CMD_SET = 16;
    public static final int CMD_SET_FILTER = 17;
    public static final int CMD_SET_TITLE = 18;
    public static final int CMD_SHOW_QUALITIES = 42;
    public static final int CMD_SWITCH_WINDOW = 21;
    public static final int CMD_UNDOCK_WINDOW = 34;
    
    static final String [] IRCMSGS =
    {
        "PRIVMSG",
        "JOIN",
        "PART",
        "QUIT",
        "PING",
        "NICK",
        "NOTICE",
        "332",
        "333",
        "353",     
        "366",
        "311",
        "312",
        "317",
        "318",
        "319",
        "001"
    };
    static final int IRCMSG_PRIVMSG = 0;
    static final int IRCMSG_JOIN = 1;
    static final int IRCMSG_PART = 2;
    static final int IRCMSG_QUIT = 3;
    static final int IRCMSG_PING = 4;
    static final int IRCMSG_NICK = 5;
    static final int IRCMSG_NOTICE = 6;
    static final int IRCMSG_RPL_TOPIC = 7;
    static final int IRCMSG_RPL_TOPIC_SETTER = 8;
    static final int IRCMSG_RPL_NAMREPLY = 9;
    static final int IRCMSG_RPL_ENDOFNAMES = 10;
    static final int IRCMSG_RPL_WHOISUSER = 11;
    static final int IRCMSG_RPL_WHOISSERVER = 12;
    static final int IRCMSG_RPL_WHOISIDLE = 13;
    static final int IRCMSG_RPL_ENDOFWHOIS = 14;
    static final int IRCMSG_RPL_WHOISCHANNELS = 15;
    static final int IRCMSG_WELCOME = 16;
    
    static final String [] CTCP_CMDS =
    {
        "VERSION",
        "SOURCE",
        "USERINFO",
        "PAGE"
    };
    static final int UNKNOWN_CTCP_CMD = -1;
    static final int CTCP_CMD_VERSION = 0;
    static final int CTCP_CMD_SOURCE = 1;
    static final int CTCP_CMD_USERINFO = 2;
    static final int CTCP_CMD_PAGE = 3;
    
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
    static final java.awt.Dimension GI_WINDOW_MINIMUM_SIZE = new java.awt.Dimension( 30, 30 );
    
    static final String STYLE_ESCAPE_SEQUENCE = Character.toString( (char) 255 );
    static final String STYLE_TERMINATION_SEQUENCE = ";";
    static final String STYLE_FOREGROUND = "fg";
    static final String STYLE_BACKGROUND = "bg";
    static final String STYLE_BOLD = "bo";
    static final String STYLE_ITALIC = "it";
    static final String STYLE_UNDERLINE = "un";
    
    static final String ALIAS_ARG_CHAR = "%";
    static final String ALIAS_ARG_REST_CHAR = "&";
    static final int MAX_ALIAS_ARGS = 20;
    static final String VARIABLE_CHAR = "%";
    
    static final char FILTER_SPECIAL_CHAR = '%';
    static final String PRINTLN_SEPARATOR_CHAR = ";";
    
    static final char CTCP_MARKER = (char) 1;
    
    static final char NAMLIST_OP_CHAR = '@';
    static final char NAMLIST_VOICE_CHAR = '+';
    
    static final String MODE_OP = "o";
    static final String MODE_VOICE = "v";
    
    static final int DOCK_NOWHERE = -1;
    static final int DOCK_TOP = 0;
    static final int DOCK_RIGHT = 1;
    static final int DOCK_BOTTOM = 2;
    static final int DOCK_LEFT = 3;
    static final String [] DOCK_STR = { "t", "r", "b", "l" };
    static final double DEFAULT_DOCK_WEIGHT = 0.25;
    static final int DEFAULT_DIVIDER_LOCATION = 100;  // pixels
    
    static final int NO_PANE = -1;
    static final int INFO_PANE = 0;
    static final int TEXT_PANE = 1;

    static final int DEFAULT_NUDGE_AMOUNT = 20;
    
    static final int DELAY_FOR_SERVER_READER_DEATH = 100;  // milliseconds
    
    static final String [] VARS =
    {
        "lasturl",
        "linesunread"
    };
    static final int VAR_LAST_URL = 0;
    static final int VAR_LINES_UNREAD = 1;
    
    static final String POSITION_TOP = "top";
    static final String POSITION_RIGHT = "right";
    static final String POSITION_BOTTOM = "bottom";
    static final String POSITION_LEFT = "left";
    static final String DEFAULT_WINDOWBAR_POSITION = POSITION_TOP;
    static final java.awt.Color DEFAULT_WINDOW_BUTTON_FOREGROUND_COLOUR = new java.awt.Color( 0, 0, 0 );
    static final String DEFAULT_INPUT_FIELD_FOREGROUND = "000000";
    static final String DEFAULT_INPUT_FIELD_BACKGROUND = "ffffff";
    
    static final int SORT_UNSORTED = -1;
    static final int SORT_ALPHABETICAL_ASCENDING = 0;
    static final int SORT_TIME_SINCE_LAST_ASCENDING = 1;
    static final int DEFAULT_SORT_ORDER = SORT_ALPHABETICAL_ASCENDING;
}
