/*
 * CommandAlias.java
 *
 * Created on July 9, 2003, 5:35 PM
 */

package geoirc;

/**
 *
 * @author  Pistos
 */
public class CommandAlias
{
    protected String alias;
    protected String expansion;
    
    private CommandAlias() { }

    public CommandAlias( String alias, String expansion )
    {
        this.alias = alias;
        this.expansion = expansion;
    }
    
    public String getAlias()
    {
        return alias;
    }
    
    public String expand( String input )
    {
    }
}
