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
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/**
 * @author netseeker aka Michael Manske
 */
public class JValidatingTable extends JTable
{
    private ValidationListener target_listener;
    private ValidationListener source_listener;
    private Map cell_validator_components = new HashMap();    
    
    /**
     * @param tm
     */
    public JValidatingTable(TableModel tm, ValidationListener listener)
    {
        super(tm);
        target_listener = listener; 
        source_listener = new ValidationListener()
        {
            void validationPerformed(Object source, boolean isvalid)
            {
                fireTargetEvent( source, isvalid);
            }
        };
    }

    public void setValidatingCellEditor( JValidatingTextField editor, int column )
    {
        TableColumn cmdColumn = getColumnModel().getColumn( column );
        
        PropertyChangeListener[] tmp_listeners = editor.getValidationListeners();
        
        for( int i = 0; i < tmp_listeners.length; i++ )
        {
            editor.removeValidationListener( (ValidationListener)tmp_listeners[i] );
        }
        
        cmdColumn.setCellEditor( new DefaultCellEditor(editor) );
        editor.addValidationListener( source_listener );        
    }
    
    private void fireTargetEvent(Object source, boolean isvalid)
    {
        if( source instanceof JValidatingTextField)
        {
            Container c = null;
            String key = String.valueOf(getSelectedColumn()) + "_" + String.valueOf(getSelectedRow());
            
            if(!cell_validator_components.containsKey(key))
            {
                c = new Container();
                cell_validator_components.put(key, c);
            }
            else
            {
                c = (Container)cell_validator_components.get(key);
            }
            
            //((JValidatingTextField)source).setSourceForValidationListeners(c);
            PropertyChangeEvent evt = new PropertyChangeEvent(c, ValidationListener.VALIDATION_RESULT, String.valueOf(!isvalid), String.valueOf(isvalid));
            target_listener.propertyChange(evt);                   
        }
    }
}
