/*
 * Log.java
 * 
 * Created on 24.09.2003
 */
package geoirc.conf.beans;

/**
 * Class providing ...
 * Common usage:
 * 
 * @author netseeker aka Michael Manske
 * TODO Add source documentation
 */
public class Log
{
    private String filter;
    private String regexp;
    private String file;
    
    /**
     * 
     */
    public Log()
    {
    }

    public Log(String filter, String regexp, String file)
    {
        this.filter = filter;
        this.regexp = regexp;
        this.file = file;
    }
    /**
     * @return
     */
    public String getFilter()
    {
        return filter;
    }

    /**
     * @return
     */
    public String getRegexp()
    {
        return regexp;
    }

    /**
     * @param string
     */
    public void setFilter(String string)
    {
        filter = string;
    }

    /**
     * @param string
     */
    public void setRegexp(String string)
    {
        regexp = string;
    }

    /**
     * @return
     */
    public String getFile()
    {
        return file;
    }

    /**
     * @param string
     */
    public void setFile(String string)
    {
        file = string;
    }

}
