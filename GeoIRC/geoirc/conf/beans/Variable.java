/*
 * Variable.java
 * 
 * Created on 06.10.2003
 */
package geoirc.conf.beans;

/**
 * Class providing ...
 * Common usage:
 * 
 * @author netseeker aka Michael Manske
 * TODO Add source documentation
 */
public class Variable
{
    private String name;
    private String regexp;
    private String filter;
    
    public Variable()
    {
    }
    
    public Variable( String name, String regexp, String filter )
    {
        this.name = name;
        this.regexp = regexp;
        this.filter = filter;
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
    public String getName()
    {
        return name;
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
    public void setName(String string)
    {
        name = string;
    }

    /**
     * @param string
     */
    public void setRegexp(String string)
    {
        regexp = string;
    }

}
