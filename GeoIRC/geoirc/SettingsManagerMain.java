/*
 * SettingsManagerMain.java
 *
 * Created on June 27, 2003, 3:34 PM
 */

package geoirc;

/**
 *
 * @author  livesNbox
 */

//imports
    import geoirc.*;
    import java.beans.*;
    import java.util.prefs.*;
    import java.io.*;
    
public class SettingsManagerMain implements NodeChangeListener, PreferenceChangeListener  {
    
    private static Preferences myPreferences = Preferences.userNodeForPackage(GeoIRC.class);
    
    /** Creates a new instance of SettingsManagerMain */
    public SettingsManagerMain() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        SettingsManagerMain myMain = new SettingsManagerMain();
        SettingsManager myMgr = new SettingsManager();
        myPreferences.addPreferenceChangeListener(myMain);
        myPreferences.addNodeChangeListener(myMain);

        
        //myPreferences = myPreferences.node("GUI");
        String mySkin = myPreferences.node("GUI").get("Skin", "");
        System.out.println(mySkin);
                
    }
   
    public void preferenceChange(PreferenceChangeEvent evt) {
        System.out.println("Preference Changed");
    }           
    public void childAdded(NodeChangeEvent evt) {
        System.out.println("Child added");
    }
    public void childRemoved(NodeChangeEvent evt) {
        System.out.println("Child removed");
    }

}
