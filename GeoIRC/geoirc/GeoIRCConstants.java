/*
 * GeoIRCConstants.java
 *
 * Created on June 27, 2003, 11:44 AM
 */

package geoirc;

import java.awt.event.InputEvent;
import java.awt.Dimension;
import java.util.Locale;
import javax.swing.JSplitPane;

/**
 *
 * @author  Pistos
 */
public interface GeoIRCConstants
{
    static final String GEOIRC_VERSION = "0.3.6a";
    
    static final String BASE_GEOIRC_TITLE = "GeoIRC";
    static final String GEOIRC_HOMEPAGE = "http://purepistos.net/geoirc";
    static final String GEOIRC_DEVPAGE = "http://developer.berlios.de/projects/geoirc";
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
        "nextpane",
        "previouspane",
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
        "changepane",
        "switchpane",
        "help",
        "completenick",
        "printdebug",
        "focus_on_input_field",
        "listwindows",
        "dockpane",
        "newinfowindow",
        "newtextwindow",
        "exit",
        "nextwindow",
        "floatpane",
        "undockpane",
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
        "print",
        "log",
        "removelog",
        "listlogs",
        "test",
        "loadpy",
        "execpymethod",
        "listprocesses",
        "killprocess",
        "execwithwindow",
        "topic",
        "list_dcc_requests",
        "accept_dcc_request",
        "reject_dcc_request",
        "resetscriptenvironment",
        "hidequalities",
        "charbold",
        "charitalic",
        "charunderline",
        "charcolour",
        "charnormal",
        "extendedpaste",
        "dccchat",
        "list_dcc_chat_offers",
        "enablecolourcodes",
        "disablecolourcodes",
        "loadtcl",
        "exectcl",
        "printactive",
        "clearinputfield",
        "clearpane",
        "quit",
        "closewindow",
        "maximizewindow",
        "minimizewindow",
        "restorewindow",
        "sizewindow",
        "positionwindow",
        "opensettings",
        "clearinputhistory",
        "dccsend",
        "listpanes",
        "activatepane",
        "activatetextpane",
        "activatepanebyindex",
        "newexternalwindow",
        "previouswindow",
        "find",
        "findagain",
        "findcasesensitive",
        "closepane",
        "ctcp",
        "printconsole",
        "printconsoleat",
        "newconsolewindow"
    };
    
    public static final String[] IRC_CMDS =
    {
        "chanserv",
        "join",
        "listchannels",        
        "me",
        "memoserv",        
        "msg",        
        "nick",
        "nickserv",        
        "part",      
        "privmsg",
        "seenserv",        
        "set",        
        "topic"
    };
    
    public static final int UNKNOWN_COMMAND = -1;
    public static final int CMD_ACCEPT_DCC_REQUEST = 59;
    public static final int CMD_ACTION = 3;
    public static final int CMD_ACTIVATE_PANE = 89;
    public static final int CMD_ACTIVATE_PANE_BY_INDEX = 91;
    public static final int CMD_ACTIVATE_TEXT_PANE = 90;
    public static final int CMD_CHANGE_PANE = 22;
    public static final int CMD_CHAR_BOLD = 63;
    public static final int CMD_CHAR_ITALIC = 64;
    public static final int CMD_CHAR_UNDERLINE = 65;
    public static final int CMD_CHAR_COLOUR = 66;
    public static final int CMD_CHAR_NORMAL = 67;
    public static final int CMD_CLEAR_INPUT_FIELD = 76;
    public static final int CMD_CLEAR_INPUT_HISTORY = 86;
    public static final int CMD_CLEAR_PANE = 77;
    public static final int CMD_CLOSE_PANE = 97;
    public static final int CMD_CLOSE_WINDOW = 79;
    public static final int CMD_COMPLETE_NICK = 24;
    public static final int CMD_CONNECT = 39;
    public static final int CMD_CTCP = 98;
    public static final int CMD_DCC_CHAT = 69;
    public static final int CMD_DCC_SEND = 87;
    public static final int CMD_DISABLE_COLOUR_CODES = 72;
    public static final int CMD_DISCONNECT = 12;
    public static final int CMD_DOCK_PANE = 28;
    public static final int CMD_ENABLE_COLOUR_CODES = 71;
    public static final int CMD_EXEC = 40;
    public static final int CMD_EXEC2 = 43;
    public static final int CMD_EXEC_PY_METHOD = 53;
    public static final int CMD_EXEC_TCL = 74;
    public static final int CMD_EXEC_WITH_WINDOW = 56;
    public static final int CMD_EXIT = 31;
    public static final int CMD_EXTENDED_PASTE = 68;
    public static final int CMD_FIND = 94;
    public static final int CMD_FIND_AGAIN = 95;
    public static final int CMD_FIND_CASE_SENSITIVE = 96;
    public static final int CMD_FLOAT_PANE = 33;
    public static final int CMD_FOCUS_ON_INPUT_FIELD = 26;
    public static final int CMD_HELP = 23;
    public static final int CMD_HIDE_QUALITIES = 62;
    public static final int CMD_JOIN = 2;
    public static final int CMD_KILL_PROCESS = 55;
    public static final int CMD_LIST_CHANNELS = 15;
    public static final int CMD_LIST_CONNECTIONS = 13;
    public static final int CMD_LIST_DCC_REQUESTS = 58;
    public static final int CMD_LIST_DCC_OFFERS = 70;
    public static final int CMD_LIST_FONTS = 4;
    public static final int CMD_LIST_WINDOWS = 27;
    public static final int CMD_LIST_LOGS = 50;
    public static final int CMD_LIST_MEMBERS = 41;
    public static final int CMD_LIST_PANES = 88;
    public static final int CMD_LIST_PROCESSES = 54;
    public static final int CMD_LOAD_PY = 52;
    public static final int CMD_LOAD_TCL = 73;
    public static final int CMD_LOG = 48;
    public static final int CMD_MAXIMIZE_WINDOW = 80;
    public static final int CMD_MINIMIZE_WINDOW = 81;
    public static final int CMD_MSG = 20;
    public static final int CMD_NEW_CONSOLE_WINDOW = 101;
    public static final int CMD_NEW_EXTERNAL_WINDOW = 92;
    public static final int CMD_NEW_INFO_WINDOW = 29;
    public static final int CMD_NEW_SERVER = 5;
    public static final int CMD_NEW_TEXT_WINDOW = 30;
    public static final int CMD_NEW_WINDOW = 1;
    public static final int CMD_NEXT_HISTORY_ENTRY = 9;
    public static final int CMD_NEXT_PANE = 7;
    public static final int CMD_NEXT_WINDOW = 32;
    public static final int CMD_NICK = 6;
    public static final int CMD_NUDGE_DOWN = 38;
    public static final int CMD_NUDGE_UP = 37;
    public static final int CMD_OPEN_SETTINGS = 85;
    public static final int CMD_PAGE_DOWN = 36;
    public static final int CMD_PAGE_UP = 35;
    public static final int CMD_PART = 14;
    public static final int CMD_PLAY = 44;
    public static final int CMD_POSITION_WINDOW = 84;
    public static final int CMD_PREVIOUS_PANE = 8;
    public static final int CMD_PREVIOUS_HISTORY_ENTRY = 10;
    public static final int CMD_PREVIOUS_WINDOW = 93;
    public static final int CMD_PRINT = 47;
    public static final int CMD_PRINT_ACTIVE = 75;
    public static final int CMD_PRINT_CONSOLE = 99;
    public static final int CMD_PRINT_CONSOLE_AT = 100;
    public static final int CMD_PRINT_DEBUG = 25;
    public static final int CMD_PRIVMSG = 19;
    public static final int CMD_QUIT = 78;
    public static final int CMD_QUOTE = 45;
    public static final int CMD_RAW = 46;
    public static final int CMD_REJECT_DCC_REQUEST = 60;
    public static final int CMD_REMOVE_LOG = 49;
    public static final int CMD_RESET_SCRIPT_ENVIRONMENT = 61;
    public static final int CMD_RESTORE_WINDOW = 82;
    public static final int CMD_SEND_RAW = 0;
    public static final int CMD_SERVER = 11;
    public static final int CMD_SET = 16;
    public static final int CMD_SET_FILTER = 17;
    public static final int CMD_SET_TITLE = 18;
    public static final int CMD_SHOW_QUALITIES = 42;
    public static final int CMD_SIZE_WINDOW = 83;
    public static final int CMD_SWITCH_PANE = 21;
    public static final int CMD_TEST = 51;
    public static final int CMD_TOPIC = 57;
    public static final int CMD_UNDOCK_PANE = 34;
    
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
        "001",
        "KICK",
        "331",
        "TOPIC",
        "MODE",
        "433",
        "PONG"
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
    static final int IRCMSG_KICK = 17;
    static final int IRCMSG_RPL_NOTOPIC = 18;
    static final int IRCMSG_TOPIC = 19;
    static final int IRCMSG_MODE = 20;
    static final int IRCMSG_ERR_NICKNAMEINUSE = 21;
    static final int IRCMSG_PONG = 22;
    
    static final String [] CTCP_CMDS =
    {
        "VERSION",
        "SOURCE",
        "USERINFO",
        "PAGE",
        "DCC",
        "PING"
    };
    static final int UNKNOWN_CTCP_CMD = -1;
    static final int CTCP_CMD_VERSION = 0;
    static final int CTCP_CMD_SOURCE = 1;
    static final int CTCP_CMD_USERINFO = 2;
    static final int CTCP_CMD_PAGE = 3;
    static final int CTCP_CMD_DCC = 4;
    static final int CTCP_CMD_PING = 5;
    
    static final boolean NEXT_PANE = false;
    static final boolean PREVIOUS_PANE = true;
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
    static final String DEFAULT_LOG_PATH = "./logs/";
    
    static final String DEFAULT_LOG_START_MESSAGE = "**** Logging begun at y MM dd HH:mm:ss ****";
    
    static final int GI_NORMAL = 0;
    static final int GI_MAXIMIZED = 1;
    static final int GI_MINIMIZED = 2;
    static final int WINDOW_MINIMUM_WIDTH = 20;
    static final int WINDOW_MAXIMUM_WIDTH = 5000;
    static final int WINDOW_MINIMUM_HEIGHT = 20;
    static final int WINDOW_MAXIMUM_HEIGHT = 5000;
    static final Dimension GI_WINDOW_MINIMUM_SIZE = new Dimension( WINDOW_MINIMUM_WIDTH, WINDOW_MINIMUM_HEIGHT );
    static final Dimension DEFAULT_DESKTOP_PANE_SIZE = new Dimension( 500, 300 );
    static final int MINIMUM_PANE_BAR_BUTTON_WIDTH = 20;  // pixels
    static final int MAXIMUM_PANE_BAR_BUTTON_WIDTH = 200;  // pixels
    
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
    static final String COMMAND_ARGUMENT_SEPARATOR_CHAR = ";";
    
    static final char CTCP_MARKER = (char) 1;
    
    static final char NAMLIST_OP_CHAR = '@';
    static final char NAMLIST_VOICE_CHAR = '+';
    static final char NAMLIST_HALFOP_CHAR = '%';
    
    static final String MODE_OP = "o";
    static final String MODE_VOICE = "v";
    static final String MODE_HALFOP = "h";
    
    static final int MAX_NEW_WINDOW_X = 500;
    static final int MAX_NEW_WINDOW_Y = 400;
    static final int MIN_NEW_WINDOW_X = 10;
    static final int MIN_NEW_WINDOW_Y = 10;
    static final int NEW_WINDOW_X_INCREMENT = 20;
    static final int NEW_WINDOW_Y_INCREMENT = 20;
    static final int DEFAULT_WINDOW_WIDTH = 700;
    static final int DEFAULT_WINDOW_HEIGHT = 500;
    
    static final int DOCK_NOWHERE = -1;
    static final int DOCK_TOP = 0;
    static final int DOCK_RIGHT = 1;
    static final int DOCK_BOTTOM = 2;
    static final int DOCK_LEFT = 3;
    static final String [] DOCK_STR = { "t", "r", "b", "l" };
    static final double DEFAULT_DOCK_WEIGHT = 0.25;
    static final int DEFAULT_DIVIDER_LOCATION = 100;  // pixels
    static final int SPLIT_PRIMARY = 0;
    static final int SPLIT_SECONDARY = 1;
    static final int SPLIT_NOT_SPLIT_MEMBER = -1;
    // With any luck, UNKNOWN_ORIENTATION != VERTICAL_SPLIT
    // and UNKNOWN_ORIENTATION != HORIZONTAL_SPLIT,
    // and this fact remains true in future Java versions!
    static final int UNKNOWN_ORIENTATION = -1;
    
    static final int NO_PANE_TYPE = -1;
    static final int GEOIRC_CONTENT_PANE = 0;
    static final int DESKTOP_PANE = 1;
    static final int CHILD_CONTENT_PANE = 2;
    static final int TEXT_PANE = 3;
    static final int INFO_PANE = 4;
    static final int SPLIT_PANE = 5;
    static final int EXTERNAL_CONTENT_PANE = 6;
    static final int CONSOLE_PANE = 7;
    
    static final boolean INCLUDE_SPLIT_PANES = true;
    static final boolean EXCLUDE_SPLIT_PANES = false;
    
    static final int UNKNOWN_FRAME_TYPE = -1;
    static final int GEOIRC_FRAME = 0;
    static final int GIWINDOW_FRAME = 1;
    static final int GIEXTERNALWINDOW_FRAME = 2;
    
    static final int INTERNAL_WINDOW = 0;
    static final int EXTERNAL_WINDOW = 1;
    
    static final int NO_PARENT = -1;

    static final int DEFAULT_NUDGE_AMOUNT = 20;

    static final int MAX_HISTORY_SIZE = 30;
    static final int MOST_RECENT_ENTRY = 0;

    static final int DELAY_FOR_SERVER_READER_DEATH = 100;  // milliseconds
    
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
    static final int SORT_MODE_ALPHABETICAL_ASCENDING = 2;
    static final int SORT_MODE_TIME_SINCE_LAST_ASCENDING = 3;
    static final int DEFAULT_SORT_ORDER = SORT_ALPHABETICAL_ASCENDING;
    
    static final int PROCESS_WATCH_INTERVAL = 1000;  // millseconds
    
    static final int DCC_NOT_YET_SET = -1;
    static final int DCC_CHAT = 0;
    static final int DCC_SEND = 1;
    
    static final int DEFAULT_MAXIMUM_NICK_WIDTH = 20;
    
    static final int DEFAULT_MINIMUM_WORD_LENGTH = 4;
    
    static final char MIRC_COLOUR_CONTROL_CHAR = '\003';
    static final String DEFAULT_MIRC_FOREGROUND_COLOUR = "808080";
    static final String DEFAULT_MIRC_BACKGROUND_COLOUR = "c0c0c0";
    static final char MIRC_BOLD_CONTROL_CHAR = '\002';
    static final char MIRC_ITALIC_CONTROL_CHAR = '\035';  // 35 octal == 29 decimal
    static final char MIRC_UNDERLINE_CONTROL_CHAR = '\037';  // 37 octal == 31 decimal
    static final char MIRC_NORMAL_CONTROL_CHAR = '\017';  // 17 octal == 15 decimal

    static final int DEFAULT_PASTE_FLOOD_ALLOWANCE = 4;
    static final int DEFAULT_PASTE_FLOOD_DELAY = 1000;  // milliseconds
    
    static final int DEFAULT_LOWEST_DCC_PORT = 35000;
    static final int DEFAULT_HIGHEST_DCC_PORT = 35999;
    
    static final int COMPLETE_NONE_FOUND = 0;
    static final int COMPLETE_ONE_FOUND = 1;
    static final int COMPLETE_MORE_THAN_ONE_FOUND = 2;
    
    static final int INFO_WINDOW_TREE_ROW_HEIGHT = 16;
    
    static final int SERVER_READER_POLLING_INTERVAL = 200; // milliseconds
    
    static final int MSG_TEXT = 0;
    static final int MSG_QUALITIES = 1;
    
    static final int STAGE_SCRIPTING = 0;
    static final int STAGE_PROCESSING = 1;

    static final int DEFAULT_MAX_DCC_SEND_FILESIZE = 20480000;  // bytes
    static final int DEFAULT_PACKET_SIZE = 8192;  // bytes
    
    static final String DEFAULT_LANGUAGE = Locale.getDefault().getLanguage();
    static final String DEFAULT_COUNTRY = Locale.getDefault().getCountry();
    
    static final int DEFAULT_SERVER_TIMEOUT = 60 * 1000; // milliseconds
    
    static final int DESKTOP_PANE_INDEX = 1;
    static final int NO_PARTNER_USER_INDEX_SPECIFIED = 0;
    
    static final String ICON_PATH = "icons" + java.io.File.separator;
    
    static final boolean CREATE_NODES = true;
    static final boolean DONT_CREATE_NODES = false;
    
    static final long MININUM_TIME_BETWEEN_SETTINGS_SAVES = 10;  // milliseconds
    
    static final int DEFAULT_MAX_TEXT_BUFFER_SIZE = 65536;  // characters
    static final int MIN_TEXT_BUFFER_MAX = 80;  // characters
    
    static final boolean CASE_SENSITIVE = false;
    static final boolean CASE_INSENSITIVE = true;
    
    static final int MENU_PANE = 0;
    static final int MENU_TEXT_PANE = 1;
    static final int MENU_INFO_PANE = 2;
    
    static final String SUBMENU_NODE = "submenu";
    static final String ITEM_NODE = "item";
    static final String PANE_LIST_NODE = "pane_list";
    
    static final int DEFAULT_CONSOLE_BUFFER_SIZE = 1000;
    static final int DEFAULT_NUM_CONSOLE_COLUMNS = 80;
    static final int DEFAULT_NUM_CONSOLE_ROWS = 25;
    
    static final String PID_PREFIX = "Process ";
}
