/*
 * HotkeyPane.java
 * 
 * Created on 03.09.2003
 */
package geoirc.conf.panes;

import geoirc.GIAction;
import geoirc.XmlProcessable;
import geoirc.conf.BaseSettingsPanel;
import geoirc.conf.GeoIRCDefaults;
import geoirc.conf.Storable;
import geoirc.conf.TitlePane;
import geoirc.util.JKeyRecordField;

import java.awt.Dimension;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Vector;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * @author netseeker aka Michael Manske
 */
public class HotkeyPane extends BaseSettingsPanel implements Storable {

    private JTable table;
    private JButton addButton;
    private JButton delButton;
    private ActionMap action_map;
    LittleTableModel ltm = new LittleTableModel();
    
    /**
     * @param settings
     * @param valueRules
     * @param name
     */
    public HotkeyPane(
        XmlProcessable settings,
        GeoIRCDefaults valueRules,
        String name,
        ActionMap action_map) {
        super(settings, valueRules, name);
        this.action_map = action_map;
        initComponents();
    }
    
    private void initComponents()
    {
        addComponent(new TitlePane("Hotkey Settings"), 0, 0, 5, 1, 0, 0);

        table = new JTable(ltm);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        Document doc = null;
        try {
            Preferences node = ((Preferences)settings_manager.getBuffer()).node("/keyboard");

            OutputStream os = new ByteArrayOutputStream();
            node.exportSubtree(os);
            SAXBuilder builder = new SAXBuilder();

            StringReader in = new StringReader(os.toString());
            doc = builder.build(in);            
        }
        catch (BackingStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JDOMException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }        
        /*
        Element root = doc.getRootElement().getChild("keyboard").getChild("map");
        
        for( Iterator it = root.getChildren().iterator(); it.hasNext(); )
        {
            Element elem = (Element)it.next();
            Object[] obj = { elem.getAttributeValue("key"), elem.getAttributeValue("value") };
            ltm.addRow( obj );
        }
        */
        table.setPreferredScrollableViewportSize(new Dimension(400, 200));
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(300);
        JScrollPane scroller = new JScrollPane(table);
        addComponent(scroller, 0, 1, 5, 1, 0, 0);
        
        addComponent(new JKeyRecordField(), 0, 2, 1, 1, 0, 0);
        addHorizontalLayoutStopper(2,1);
        addLayoutStopper(0,3);
    }

    /* (non-Javadoc)
     * @see geoirc.conf.Storable#saveData()
     */
    public boolean saveData() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see geoirc.conf.Storable#hasErrors()
     */
    public boolean hasErrors() {
        // TODO Auto-generated method stub
        return false;
    }

    class LittleTableModel extends AbstractTableModel {
        final String[] columnNames =
            { "Hotkey", "Command Alias" };
        Vector data = new Vector();

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getRowCount()
         */
        public int getRowCount() {
            return data.size();
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getColumnCount()
         */
        public int getColumnCount() {
            return columnNames.length;
        }

        public Class getColumnClass(int c) {
            return String.class;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getValueAt(int, int)
         */
        public Object getValueAt(int row, int col) {
            Object[] rowdata = (Object[])data.get(row);
            return rowdata[col];
        }

        public boolean isCellEditable(int row, int col) {
            return false;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
         */
        public void setValueAt(Object value, int row, int col) {
            Object[] rowdata = (Object[])data.get(row);
            rowdata[col] = value;
            fireTableDataChanged();
        }

        public void addRow(Object[] row) {
            data.add(row);
            fireTableDataChanged();
        }

        public void delRow(int row) {
            data.remove(row);
            fireTableDataChanged();
        }
        public void setData(Vector data) {
            this.data = data;
            fireTableDataChanged();
        }

        public Vector getData() {
            return this.data;
        }
    }

}
