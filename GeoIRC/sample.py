from java.util import Random
generator = Random()

# RawListeners have the ability to change the line that they receive and
# send it back to GeoIRC in an altered state.  You can define your class by
# any name you like, as long as it has "text" and "qualities" attributes.
class Message:
    def __init__( self, text_, qualities_ ):
        self.text = text_
        self.qualities = qualities_

# Here we define a RawListener--it can have any name, but must have an
# onRaw method which takes two string arguments, the message text and
# the message qualities.  We are returning a Message object (as defined above)
# which contains the message which possibly has been altered.
class RawChanger:
    def onRaw( self, text_, qualities_ ):
        text = text_
        qualities = qualities_
        line = text.split()
        if( len( line ) > 1 ):
            if( line[ 1 ] == "PRIVMSG" ):
                text = text + "!!"
        msg = Message( text, qualities )
        return msg; 

class Rand:
    def generate( self, range ):
        random_number = generator.nextInt( int( range ) )
	# geoirc.execute can be used to execute any GeoIRC command
        geoirc.execute( "PRIVMSG #geoshell " + str( random_number ) )

# Instantiate our RawListener ...
changer = RawChanger()
# ... and register it with GeoIRC.
geoirc.registerRawListener( changer )

rando = Rand()

# Any method of any class can be registered with GeoIRC.

# GeoIRC executes the method by the command
# "execpymethod instance.methodname args"

# In this example, the instance is named "rando", and the method is "generate".
# Note that they are passed to the registerMethod method as strings.

# The method can take up to one argument (of string type).  Any arguments that
# follow "execpymethod instance.methodname" are passed as a single string to
# the method.
geoirc.registerMethod( "rando", "generate" )
