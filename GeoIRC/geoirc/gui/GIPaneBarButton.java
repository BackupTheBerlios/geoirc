/*
 * GIPaneBarButton.java
 *
 * Created on October 26, 2003, 2:40 PM
 */

package geoirc.gui;

import java.awt.event.ActionEvent;
import javax.swing.Icon;

/**
 *
 * @author  Pistos
 */
public class GIPaneBarButton extends javax.swing.JToggleButton implements java.awt.event.ActionListener
{
    protected GIPaneWrapper associated_pane;
    
    private GIPaneBarButton() { }
    private GIPaneBarButton( String text ) { }
    private GIPaneBarButton( String text, boolean state ) { }
    private GIPaneBarButton( Icon icon ) { }
    private GIPaneBarButton( String text, Icon icon ) { }
    
    public GIPaneBarButton( GIPaneWrapper gipw )
    {
        super( gipw.getTitle() );
        setToolTipText( gipw.getTitle() );
        associated_pane = gipw;
        gipw.setAssociatedButton( this );
        addActionListener( this );
        setFocusable( false );
    }
    
    public GIPaneWrapper getAssociatedPane()
    {
        return associated_pane;
    }
    
    public void actionPerformed( ActionEvent e )
    {
        setSelected( true );
        GIFrameWrapper gifw = (GIFrameWrapper) associated_pane.getFrame();
        if( gifw != null )
        {
            gifw.activate();
        }
    }
    
}
