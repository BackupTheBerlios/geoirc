/*
 * SettingsPeer.java
 * 
 * Created on 28.09.2003
 */
package geoirc.conf;

import geoirc.CommandAlias;
import geoirc.XmlProcessable;
import geoirc.conf.beans.Highlighting;
import geoirc.conf.beans.Log;
import geoirc.conf.beans.Trigger;
import geoirc.conf.beans.ValueRule;
import geoirc.conf.beans.Variable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author netseeker aka Michael Manske
 */
public class SettingsPeer
{
    public static List loadCommandAliases(XmlProcessable settings_manager, GeoIRCDefaults rules)
    {
        String path = "/command aliases/";
        List aliases = new ArrayList(); 
        int i = 0;
        String nodePath = path + String.valueOf(i) + "/";
        while (settings_manager.nodeExists(nodePath))
        {
            String alias = settings_manager.getString(nodePath + "alias", "");
            String expansion = settings_manager.getString(nodePath + "expansion", "");
            if (alias.length() > 0)
            {
                aliases.add(new CommandAlias(alias, expansion));
            }
            i++;
            nodePath = path + String.valueOf(i) + "/";
        }
        
        return aliases;        
    }

    public static List loadVariables(XmlProcessable settings_manager, GeoIRCDefaults rules)
    {
        String path = "/variables/captured/";
        List variables = new ArrayList(); 

        int i = 0;
        String nodePath = path + String.valueOf(i) + "/";
        while (settings_manager.nodeExists(nodePath))
        {
            String filter = settings_manager.getString(nodePath + "filter", "");
            String regexp = settings_manager.getString(nodePath + "regexp", "");
            String name = settings_manager.getString(nodePath + "name", ""); 
            
            if (name.length() > 0)
            {
                variables.add( new Variable( name, regexp, filter ) );
            }
            i++;
            nodePath = path + String.valueOf(i) + "/";
        }
        
        return variables;
    }
    
    public static List loadLogs(XmlProcessable settings_manager, GeoIRCDefaults rules)
    {
        String path = "/logs/";
        int i = 0;
        String node = path + String.valueOf(i) + "/";
        List data = new ArrayList();

        while (settings_manager.nodeExists(node) == true)
        {
            String filter = settings_manager.getString(node + "filter", "");
            String regexp = settings_manager.getString(node + "regexp", "");
            String file = settings_manager.getString(node + "file", "");
            if (filter.length() > 0 || file.length() > 0)
            {
                data.add(new Log(filter, regexp, file));
            }
            i++;
            node = path + String.valueOf(i) + "/";
        }

        return data;
    }

    public static List loadTriggers(XmlProcessable settings_manager, GeoIRCDefaults rules)
    {
        String path = "/triggers/";
        int i = 0;
        String node = path + String.valueOf(i) + "/";
        List data = new ArrayList();

        while (settings_manager.nodeExists(node) == true)
        {
            String filter = settings_manager.getString(node + "filter", "");
            String regexp = settings_manager.getString(node + "regexp", "");
            String command = settings_manager.getString(node + "command", "");
            if (filter.length() > 0 || regexp.length() > 0)
            {
                data.add(new Trigger(filter, regexp, command));
            }                
            i++;
            node = path + String.valueOf(i) + "/";
        }

        return data;
    }    

    public static List loadHighlightings(XmlProcessable settings_manager, GeoIRCDefaults rules)
    {
        String path = "/gui/text windows/highlighting/";
        ValueRule colorRule = rules.getValueRule("COLOR");
        int i = 0;
        String node = path + String.valueOf(i) + "/";
        List data = new ArrayList();

        while (settings_manager.nodeExists(node) == true)
        {
            String filter = settings_manager.getString(node + "filter", "");
            String regexp = settings_manager.getString(node + "regexp", "");
            String format = settings_manager.getString(node + "format", colorRule.getValue().toString());
            if (filter.length() > 0 || regexp.length() > 0)
                data.add(new Highlighting(filter, regexp, format));

            i++;
            node = path + String.valueOf(i) + "/";
        }

        return data;
    }

}
