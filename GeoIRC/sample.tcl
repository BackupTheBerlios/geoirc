# See also http://tcljava.sourceforge.net/docs/TclJava/contents.html

package require java

# Nearly all basic java classes can be used.
# Instantiate them with the java::new call.
set generator [java::new java.util.Random]

# RawListeners have the ability to change the line that they receive and
# send it back to GeoIRC in an altered state.

# Here we define a proc which acts as a RawListener.  It can have any name,
# but must take two string arguments, the message text and
# the message qualities.  It must return a two-element list, which contains
# the message which possibly has been altered.

proc rawchanger {text qualities} {
    set new_text $text
    set new_qualities $qualities
    
    set line [split $text]
    
    if {[llength $line] > 1} {
	if {[lindex $line 1] == "PRIVMSG"} {
	    set new_text "$new_text!!"
	}
    }

    return [list $new_text $new_qualities]
}

# Here we define a proc which acts as an InputListener.  It can have any name,
# but must take a single string argument, which is the text input by the user.
# It must return a single value, which is a string containing the possibly
# altered input text.

proc inputchanger {text} {
    set new_text $text
    
    if {[string index 0] != "/"} {
	set new_text "$new_text!!"
    }
    
    return $new_text
}

proc generate {range} {
global generator geoirc
    set random_number [$generator nextInt $range]
    # geoirc.execute can be used to execute any GeoIRC command
    # Note, however, that the argument to the execute command must be a
    # single string.
    $geoirc execute "PRIVMSG #geoshell $random_number"
}

# Register our RawListener with GeoIRC.
$geoirc registerRawListener "rawchanger"

# Register our InputListener with GeoIRC.
$geoirc registerInputListener "inputchanger"

# Load your script into GeoIRC with the "loadtcl <script filename>" command.

# GeoIRC executes Tcl code by the command
# "exectclproc <any tcl command or proc, with trailing arguments>"
# In this example, we have a proc called "generate", so it might be called
# as follows:
# /exectclproc generate 3

# Your Tcl procs can take any number of arguments; any arguments that
# follow "exectclmethod <procname>" are passed to the method.
