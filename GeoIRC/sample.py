class Message:
    def __init__( self, text_, qualities_ ):
        self.text = text_
        self.qualities = qualities_

class RawChanger:
    def onRaw( self, text_, qualities_ ):
        text = text_
        qualities = qualities_
        line = text.split()
        if( len( line ) > 1 ):
            if( line[ 1 ] == "PRIVMSG" ):
                text = text + "!!"
            else:
                text = text + " [" + line[ 1 ] + "]"
        msg = Message( text, qualities )
        return msg; 

changer = RawChanger()
#my_msg = changer.onRaw( "aoeu PRIVMSG aoeu", "snth snth snth" )
#print my_msg.text
geoirc.registerRawListener( changer )