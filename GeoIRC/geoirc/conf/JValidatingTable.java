/*
 * JValidatingTable.java
 * 
 * Created on 20.09.2003
 */
package geoirc.conf;

import geoirc.util.JValidatingTextField;

import java.awt.Container;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/**
 * JValidatingTable is basically a workaround for tables containing
 * validating textfields which uses listeners to notify observers of
 * changges of validation state.<br>
 * Description: Standard JTable uses one and only one intance of a cell editor
 * component, e.g. JTextField, for all contained cells. When using listeners and
 * one cell contains an invalid value an event is fired for notification of the invalid state,
 * if the user then activates another cell which may contain a valid value an event will be fired
 * for notification of the valid state. The second event will cancel out the first event, so the observer
 * will not longer know that the first cell contains an invalid value.<br>
 * JValidatingTable creates an empty container for each cell and wraps events fired by celleditor from the
 * celleditor to an empty container. Then all listener observers get notified of the cellstate but they don't get
 * the celleditor component as source of the event but the empty cell container. This way each cell can be validated
 * by using only one instance of the celleditor. 
 * 
 * @author netseeker aka Michael Manske
 */
public class JValidatingTable extends JTable
{    
    private List target_listeners = new ArrayList();
    
    private ValidationListener source_listener = new ValidationListener()
    {
        void validationPerformed(Object source, boolean isvalid)
        {
            fireTargetEvent( source, isvalid);
        }
    };
    
    /*
     * container for temporary containers used as listener
     * source for cells instead of the celleditor 
     */
    private Map cell_validator_components = new HashMap();    
    

    /**
     * 
     */
    public JValidatingTable()
    {
        super();
    }

    /**
     * @param listener
     */
    public JValidatingTable( ValidationListener listener)
    {
        super();
        if( listener != null )
        {
            target_listeners.add( listener );
        }
    }

    /**
     * @param listener
     */
    public JValidatingTable( TableModel tm )
    {
        super(tm);
    }


    /**
     * @param tm
     * @param listener
     */
    public JValidatingTable(TableModel tm, ValidationListener listener)
    {
        super(tm);

        if( listener != null )
        {
            target_listeners.add( listener );
        }
    }

    /**
     * @param editor
     * @param column
     */
    public void setValidatingCellEditor( JValidatingTextField editor, int column )
    {
        TableColumn cmdColumn = getColumnModel().getColumn( column );
        
        PropertyChangeListener[] tmp_listeners = editor.getValidationListeners();
        
        for( int i = 0; i < tmp_listeners.length; i++ )
        {
            target_listeners.add( tmp_listeners[i] );
            editor.removeValidationListener( (ValidationListener)tmp_listeners[i] );
        }
        
        cmdColumn.setCellEditor( new DefaultCellEditor(editor) );
        editor.addValidationListener( source_listener );        
    }

    public void setRegExpTestingCellEditor( JValidatingTextField editor, int column )
    {
        TableColumn cmdColumn = getColumnModel().getColumn( column );
        
        PropertyChangeListener[] tmp_listeners = editor.getValidationListeners();
        
        for( int i = 0; i < tmp_listeners.length; i++ )
        {
            target_listeners.add( tmp_listeners[i] );
            editor.removeValidationListener( (ValidationListener)tmp_listeners[i] );
        }
        
        RegExpTesterInputFieldEditor reg_editor_cell = new RegExpTesterInputFieldEditor( editor ); 
        cmdColumn.setCellEditor( reg_editor_cell );
        editor.addValidationListener( source_listener );        
    }

    
    /**
     * @param source
     * @param isvalid
     */
    private void fireTargetEvent(Object source, boolean isvalid)
    {
        if( source instanceof JValidatingTextField)
        {
            Container c = null;
            String key = String.valueOf(getSelectedColumn()) + "_" + String.valueOf(getSelectedRow());
            
            if( !cell_validator_components.containsKey(key) )
            {
                c = new Container();
                cell_validator_components.put(key, c);
            }
            else
            {
                c = (Container)cell_validator_components.get( key );
            }
            
            PropertyChangeEvent evt = new PropertyChangeEvent( c, ValidationListener.VALIDATION_RESULT, String.valueOf(!isvalid), String.valueOf(isvalid) );
            for( Iterator it = target_listeners.iterator(); it.hasNext(); )
            {
                ((ValidationListener)it.next()).propertyChange( evt );
            }                               
        }
    }
}
