/*
 * HotkeyPane.java
 * 
 * Created on 03.09.2003
 */
package geoirc.conf.panes;

import geoirc.SettingsManager;
import geoirc.XmlProcessable;
import geoirc.conf.BaseSettingsPanel;
import geoirc.conf.GeoIRCDefaults;
import geoirc.conf.Storable;
import geoirc.conf.TitlePane;
import geoirc.util.JKeyRecordField;
import geoirc.util.JValidatingTextField;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * @author netseeker aka Michael Manske
 */
public class HotkeyPane extends BaseSettingsPanel implements Storable, DocumentListener
{

    private JTable table;
    private ActionMap action_map;
    LittleTableModel ltm = new LittleTableModel();
    JValidatingTextField command_field = new JValidatingTextField();
    JKeyRecordField hotkey_field = new JKeyRecordField();
    JButton newButton = new JButton("new");
    JButton addButton = new JButton("add");
    JButton delButton = new JButton("delete");
    ActionListener addListener = new AddActionListener();
    ActionListener setListener = new SetActionListener();
    boolean errorState = false;

    /**
     * @param settings
     * @param valueRules
     * @param name
     */
    public HotkeyPane(XmlProcessable settings, GeoIRCDefaults valueRules, String name, ActionMap action_map)
    {
        super(settings, valueRules, name);
        this.action_map = action_map;
    }

    /**
     * 
     */
    public void initialize()
    {
        addComponent(new TitlePane("Hotkey Settings"), 0, 0, 10, 1, 0, 0);

        table = new JTable(ltm);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(18);
        hotkey_field.setEnabled(false);
        command_field.setEnabled(false);

        Element root = getKeyboardNode();

        for (Iterator it = root.getChildren().iterator(); it.hasNext();)
        {
            Element elem = (Element)it.next();
            Object[] obj = { elem.getAttributeValue("key"), elem.getAttributeValue("value")};
            ltm.addRow(obj);
        }

        table.setPreferredScrollableViewportSize(new Dimension(500, 300));
        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        table.getColumnModel().getColumn(1).setPreferredWidth(380);
        JScrollPane scroller = new JScrollPane(table);
        addComponent(scroller, 0, 1, 10, 1, 0, 0);

        addComponent(new JLabel("Hotkey: "), 0, 2, 1, 1, 0, 0);
        addComponent(hotkey_field, 1, 2, 1, 1, 0, 0);
        addComponent(new JLabel("Command: "), 0, 3, 1, 1, 0, 0, GridBagConstraints.WEST);
        command_field.setPreferredSize(new Dimension(250, JValidatingTextField.PREFERED_HEIGHT));
        addComponent(command_field, 1, 3, 1, 1, 0, 0, GridBagConstraints.WEST);

        addComponent(newButton, 2, 3, 1, 1, 0, 0);
        addComponent(addButton, 3, 3, 1, 1, 0, 0);
        addComponent(delButton, 4, 3, 1, 1, 0, 0);
        setButtonsState(-1);

        addHorizontalLayoutStopper(5, 3);
        addLayoutStopper(0, 4);

        newButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {

                ListSelectionModel rowSM = table.getSelectionModel();
                rowSM.clearSelection();
                hotkey_field.setEnabled(true);
                command_field.setEnabled(true);
                hotkey_field.requestFocus();
            }
        });

        delButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {
                int pos = table.getSelectedRow();
                ltm.delRow(pos);
            }
        });

        ListSelectionModel rowSM = table.getSelectionModel();
        rowSM.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                //Ignore extra messages.
                if (e.getValueIsAdjusting())
                    return;

                ListSelectionModel lsm = (ListSelectionModel)e.getSource();

                if (!lsm.isSelectionEmpty())
                {
                    int pos = table.getSelectedRow();
                    Object[] row = ltm.getRow(pos);
                    hotkey_field.setEnabled(true);
                    command_field.setEnabled(true);
                    hotkey_field.setText((String)row[0]);
                    command_field.setText((String)row[1]);
                    setButtonsState(0);
                }
                else
                {
                    hotkey_field.setEnabled(false);
                    command_field.setEnabled(false);
                    hotkey_field.setText("");
                    command_field.setText("");
                    setButtonsState(-1);
                }
            }
        });

        hotkey_field.getDocument().addDocumentListener(this);
        command_field.getDocument().addDocumentListener(this);
    }

    /**
     * Converts the "/keyboard" node from Preferences to an JDOM Element
     * TODO: FIXME, converting is very slow, we have to find a better way
     */
    private Element getKeyboardNode()
    {
        try
        {
            Preferences node = (Preferences)settings_manager.getBuffer();

            OutputStream os = new ByteArrayOutputStream();
            node.exportSubtree(os);
            StringReader in = new StringReader(os.toString());
            Document doc = new SAXBuilder().build(in);

            //using jdom's build in XPath support would cause dependencies to jaxen and saxpath          
            StringTokenizer tokenizer = new StringTokenizer("root,node", ",");
            Element root = doc.getRootElement();
            while (tokenizer.hasMoreTokens())
            {
                root = root.getChild(tokenizer.nextToken());
            }

            for (Iterator it = root.getChildren("node").iterator(); it.hasNext();)
            {
                Element elem = (Element)it.next();
                String name = elem.getAttributeValue("name");
                if (name != null && name.equalsIgnoreCase("keyboard"))
                {
                    return elem.getChild("map");
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            errorState = true;
        }

        return new Element("keyboard");
    }

    /**
     * @return
     */
    private Object[] getCommandAliases()
    {
        List aliases = new ArrayList();
        String path = "/command aliases/";
        int i = 0;
        String nodePath = path + String.valueOf(i) + "/";

        while (settings_manager.nodeExists(nodePath))
        {
            String alias = settings_manager.getString(nodePath + "alias", "");
            if (alias.length() > 0)
                aliases.add(alias);

            i++;
            nodePath = path + String.valueOf(i) + "/";
        }

        return aliases.toArray();
    }

    /**
     * @param state
     */
    private void setButtonsState(int state)
    {
        if (state != 0)
            addButton.setText("add");
        else
            addButton.setText("set");

        switch (state)
        {
            case -1 :
                addButton.setEnabled(false);
                delButton.setEnabled(false);
                break;
            case 0 :
                addButton.setEnabled(true);
                addButton.removeActionListener(addListener);
                addButton.removeActionListener(setListener);
                addButton.addActionListener(setListener);
                delButton.setEnabled(true);
                break;
            case 1 :
                addButton.setEnabled(true);
                addButton.removeActionListener(setListener);
                addButton.removeActionListener(addListener);
                addButton.addActionListener(addListener);
                delButton.setEnabled(false);
                break;
        }
    }

    private void checkInputState()
    {
        if (hotkey_field.isValid() && command_field.isValid())
        {
            if (table.getSelectedRow() == -1)
                setButtonsState(1);
            else
                setButtonsState(0);
        }

    }

    /* (non-Javadoc)
     * @see geoirc.conf.Storable#saveData()
     */
    public boolean saveData()
    {

        SettingsManager mgr = (SettingsManager)settings_manager;
        mgr.removeNode("/keyboard/");
        for (Iterator it = ltm.getData().iterator(); it.hasNext();)
        {
            Object[] data = (Object[])it.next();
            mgr.put("/keyboard/" + (String)data[0], (String)data[1]);
        }

        return true;
    }

    /* (non-Javadoc)
     * @see geoirc.conf.Storable#hasErrors()
     */
    public boolean hasErrors()
    {

        return errorState;
    }

    /**
     * 
     */
    class LittleTableModel extends AbstractTableModel
    {
        final String[] columnNames = { "Hotkey", "Command Alias" };
        Vector data = new Vector();

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getRowCount()
         */
        public int getRowCount()
        {
            return data.size();
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getColumnCount()
         */
        public int getColumnCount()
        {
            return columnNames.length;
        }

        public Class getColumnClass(int c)
        {
            return String.class;
        }

        public String getColumnName(int col)
        {
            return columnNames[col];
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getValueAt(int, int)
         */
        public Object getValueAt(int row, int col)
        {
            Object[] rowdata = (Object[])data.get(row);
            return rowdata[col];
        }

        public boolean isCellEditable(int row, int col)
        {
            return false;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
         */
        public void setValueAt(Object value, int row, int col)
        {
            Object[] rowdata = (Object[])data.get(row);
            rowdata[col] = value;
            fireTableDataChanged();
        }

        public void addRow(Object[] row)
        {
            data.add(row);
            fireTableDataChanged();
        }

        public void setRow(Object[] row, int pos)
        {
            data.set(pos, row);
            fireTableDataChanged();
        }

        public void delRow(int row)
        {
            data.remove(row);
            fireTableDataChanged();
        }
        public void setData(Vector data)
        {
            this.data = data;
            fireTableDataChanged();
        }

        public Vector getData()
        {
            return this.data;
        }

        public Object[] getRow(int row)
        {
            return (Object[])data.get(row);
        }
    }

    /**
     * 
     */
    class AddActionListener implements ActionListener
    {
        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent arg0)
        {
            ltm.addRow(new Object[] { hotkey_field.getText(), command_field.getText()});
        }
    }

    /**
     * 
     */
    class SetActionListener implements ActionListener
    {
        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent arg0)
        {
            int pos = table.getSelectedRow();
            ltm.setRow(new Object[] { hotkey_field.getText(), command_field.getText()}, pos);
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
     */
    public void insertUpdate(DocumentEvent arg0)
    {
        checkInputState();
    }

    /* (non-Javadoc)
     * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
     */
    public void removeUpdate(DocumentEvent arg0)
    {
        checkInputState();
    }

    /* (non-Javadoc)
     * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
     */
    public void changedUpdate(DocumentEvent arg0)
    {
        checkInputState();
    }

}
