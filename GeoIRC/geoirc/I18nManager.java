/*
 * I18nManager.java
 *
 * Created on October 7, 2003, 3:06 PM
 */

package geoirc;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * InternationalizationManager
 *
 * @author  Pistos
 */
public class I18nManager implements GeoIRCConstants
{
    protected String language;
    protected String country;
    protected Locale locale;
    protected ResourceBundle geoirc_messages;
    protected MessageFormat formatter;
    
    private I18nManager() { }
    
    public I18nManager( SettingsManager settings_manager )
    {
        language = settings_manager.getString(
            "/personal/language",
            DEFAULT_LANGUAGE
        );
        country = settings_manager.getString(
            "/personal/country",
            DEFAULT_COUNTRY
        );
        locale = new Locale( language, country );
        geoirc_messages = ResourceBundle.getBundle( "geoirc_messages", locale );

        formatter = new MessageFormat( "" );
        formatter.setLocale( locale );
    }
    
    public String getString( String message_key )
    {
        String retval = "MissingResourceException";
        try
        {
            retval = geoirc_messages.getString( message_key );
        }
        catch( MissingResourceException e ) { 
        }
        return retval;
    }
    
    public String getString( String message_key, Object [] arguments )
    {
        String retval = "MissingResourceException";
        try
        {
            formatter.applyPattern( geoirc_messages.getString( message_key ) );
            retval = formatter.format( arguments );
        }
        catch( MissingResourceException e ) { }
        return retval;
    }
}
