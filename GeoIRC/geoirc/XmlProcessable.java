/*
 * XmlProcessable.java
 * 
 * Created on 11.08.2003
 */
package geoirc;

import org.jdom.Element;

/**
 * @author netseeker aka Michael Manske
 */
public interface XmlProcessable
{
	//public void setFilePath(String path);
	public boolean loadSettingsFromXML();
	public boolean saveSettingsToXML();
	//public void listenToPreferences();
        public Element getNode( String absolute_path );
	public String get( String path, String default_ );
	public String getString( String path, String default_ );
	public int getInt( String path, int default_ );
	public boolean getBoolean( String path, boolean default_ );
	public void set( String path, String value );
	public void setString( String path, String value );
	public void setInt( String path, int value );
	public void setBoolean( String path, boolean value );
	public boolean removeNode( String path );
	public boolean nodeExists( String path );
	public Element getBuffer();	
}
