/*
 * SettingsPeer.java
 * 
 * Created on 28.09.2003
 */
package geoirc.conf;

import geoirc.CommandAlias;
import geoirc.XmlProcessable;

import java.util.ArrayList;
import java.util.List;

/**
 * Class providing ...
 * Common usage:
 * 
 * @author netseeker aka Michael Manske
 * TODO Add source documentation
 */
public class SettingsPeer
{
    public static List loadCommandAliases(XmlProcessable settings_manager)
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

}
