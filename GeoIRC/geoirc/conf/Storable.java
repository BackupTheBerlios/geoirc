/*
 * Storable.java
 * 
 * Created on 20.08.2003
 */
package geoirc.conf;

/**
 * @author netseeker aka Michael Manske
 */
public interface Storable
{
	public boolean saveData();
	public boolean hasErrors();
    public boolean hasChanges();    
}
