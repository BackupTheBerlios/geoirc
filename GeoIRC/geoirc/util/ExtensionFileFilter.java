/*
 * ExtensionFileFilter.java
 * 
 * Created on 05.10.2003
 */
package geoirc.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.filechooser.FileFilter;

/**
 * Class providing ...
 * Common usage:
 * 
 * @author netseeker aka Michael Manske
 * TODO Add source documentation
 */
public class ExtensionFileFilter extends FileFilter
{
    private List extensions = new ArrayList();
           
    /**
     * 
     */
    public ExtensionFileFilter(String extension)
    {
        super();
        if( extension.startsWith(".") )
        {
            extension = extension.substring(1);
        }
        this.extensions.add( extension );
    }

    /**
     * 
     */
    public ExtensionFileFilter( String[] extensions )
    {
        super();
        for(int i = 0; i < extensions.length; i++)
        {
            String buf = extensions[i];
            if( buf.startsWith(".") )
            {
                buf = buf.substring(1);
            }
            this.extensions.add( buf );        
        }
    }


    /* (non-Javadoc)
     * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
     */
    public boolean accept(File file)
    {
        if( file.isDirectory() )
        {
            return false;
        }
        
        String path = file.getName();
        try
        {
            path = path.substring( path.lastIndexOf(".") + 1 );
        }
        catch( Exception e)
        {
            return false;
        }
        
        return this.extensions.contains( path );
    }

    /* (non-Javadoc)
     * @see javax.swing.filechooser.FileFilter#getDescription()
     */
    public String getDescription()
    {
        StringBuffer desc = new StringBuffer();
        
        for(Iterator it = extensions.iterator(); it.hasNext(); )
        {
            desc.append( "*." );
            desc.append( ((String)it.next()) );
            if( it.hasNext() )
            {
                desc.append(", ");
            }
        }
        
        return desc.toString();
    }

}
