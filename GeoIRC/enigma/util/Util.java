package enigma.util;

import java.io.*;
import java.text.*;
import java.util.*;

/** 
 * Contains utility methods for internal Enigma use.
 *
 *@status.stable
 *@author Ethan Royael Nicholas (<a href="mailto:ethan@ethannicholas.com">ethan@ethannicholas.com</a>)
 */
public class Util {
    /** 
     * Loads a localized message for the specified class.  Messages are located
     * in the <code>enigma-i18n.jar</code> file in properties files corresponding
     * to the class name.
     *
     *@param clazz the class to which the message belongs
     *@param msgKey the name of the message to load
     */
    public static final String msg(Class clazz, String msgKey) {
        // FIXME: currently only supports one language in the enigma-i18n.jar file
        // need to figure out how best to handle localization
        try {
            Properties messages = new Properties();
            String resource = "/messages/" + clazz.getName().replace('.', '/') + ".properties";
            InputStream in = clazz.getResourceAsStream(resource);
            if (in == null)
                throw new IOException("resource not found: " + resource);
            messages.load(in);
            String result = messages.getProperty(msgKey);
            if (result == null)
                throw new Error("message not defined: " + clazz + ":" + msgKey);
            return result;
        }
        catch (IOException e) {
            throw new Error("IOException reading properties file: " + e);
        }
    }        
    

    /** 
     * Loads and formats localized message for the specified class.  Messages are 
     * located in the <code>enigma-i18n.jar</code> file in properties files 
     * corresponding to the class name, and are formatted using
     * <code>java.text.MessageFormat</code>.
     *
     *@param clazz the class to which the message belongs
     *@param msgKey the name of the message to load
     *@param args the objects to be formatted and substituted
     */
    public static final String msg(Class clazz, String msgKey, Object[] args) {
        return MessageFormat.format(msg(clazz, msgKey), args);
    }
}
