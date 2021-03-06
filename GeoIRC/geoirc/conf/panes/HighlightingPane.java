/*
 * HighlightingPane.java
 * 
 * Created on 21.08.2003
 */
package geoirc.conf.panes;

import geoirc.GeoIRCConstants;
import geoirc.XmlProcessable;
import geoirc.conf.BaseSettingsPanel;
import geoirc.conf.GeoIRCDefaults;
import geoirc.conf.JValidatingTable;
import geoirc.conf.Storable;
import geoirc.conf.TableCellColorRenderer;
import geoirc.conf.TitlePane;
import geoirc.conf.beans.Highlighting;
import geoirc.conf.beans.ValueRule;
import geoirc.util.JBoolRegExTextField;
import geoirc.util.JRegExTextField;
import geoirc.util.Util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

/**
 * @author netseeker aka Michael Manske
 */
public class HighlightingPane extends BaseSettingsPanel implements Storable, GeoIRCConstants
{
    private LittleTableModel ltm = new LittleTableModel();
    private ValueRule colorRule;

    private JValidatingTable table;
    private JButton delButton;
    private JButton upButton;
    private JButton downButton;

    /**
     * @param settings
     * @param valueRules
     * @param name
     */
    public HighlightingPane(XmlProcessable settings, GeoIRCDefaults valueRules, String name)
    {
        super(settings, valueRules, name);
    }

    public void initialize()
    {
        addComponent(new TitlePane("Highlighting"), 0, 0, 3, 1, 0, 0);

        table = new JValidatingTable(ltm, validation_listener);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(true);
        table.setRowHeight(18);
        colorRule = rules.getValueRule("COLOR");
        ltm.setData(getHighlightings());

        TableColumn sportColumn = table.getColumnModel().getColumn(3);
        JComboBox comboBox = new JComboBox();
        comboBox.addItem(STYLE_BACKGROUND);
        comboBox.addItem(STYLE_FOREGROUND);
        sportColumn.setCellEditor(new DefaultCellEditor(comboBox));

        JBoolRegExTextField valueField = new JBoolRegExTextField(null);
        table.setValidatingCellEditor(valueField, 0);

        JRegExTextField patternField = new JRegExTextField(null);
        table.setRegExpTestingCellEditor(patternField, 1);

        table.setPreferredScrollableViewportSize(new Dimension(500, 300));
        table.getColumnModel().getColumn(0).setPreferredWidth(140);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(90);
        table.getColumnModel().getColumn(3).setPreferredWidth(70);

        JScrollPane scroller = new JScrollPane(table);
        addComponent(scroller, 0, 1, 4, 4, 0, 0);

        //Set up renderer and editor for the color column.
        table.setDefaultRenderer(Color.class, new TableCellColorRenderer(true));
        setUpColorEditor(table);

        upButton = new BasicArrowButton(BasicArrowButton.NORTH);
        downButton = new BasicArrowButton(BasicArrowButton.SOUTH);

        upButton.setToolTipText("move selected highlighting one position up");
        downButton.setToolTipText("move selected highlighting one position down");
        upButton.setEnabled(false);
        downButton.setEnabled(false);

        addComponent(upButton, 5, 1, 1, 1, 0, 0);
        addComponent(downButton, 5, 4, 1, 1, 0, 0, GridBagConstraints.SOUTHWEST);

        upButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {
                int pos = table.getSelectedRow();
                Object target = ltm.getRow(pos - 1);
                ltm.setRow(pos - 1, ltm.getRow(pos));
                ltm.setRow(pos, target);
                table.setRowSelectionInterval(pos - 1, pos - 1);
            }
        });

        downButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {
                int pos = table.getSelectedRow();
                Object target = ltm.getRow(pos + 1);
                ltm.setRow(pos + 1, ltm.getRow(pos));
                ltm.setRow(pos, target);
                table.setRowSelectionInterval(pos + 1, pos + 1);
            }
        });

        delButton = new JButton("delete");
        delButton.setEnabled(false);
        delButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {
                int pos = table.getSelectedRow();
                ltm.delRow(pos);
                if (table.getRowCount() > 0)
                {
                    table.setRowSelectionInterval(pos - 1, pos - 1);
                }
            }
        });

        JButton button = new JButton("new");
        button.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {
                int count = table.getRowCount() - 1;
                ltm.addRow(new Highlighting("", ".*", colorRule.getValue().toString()));
                table.setRowSelectionInterval(count, count);
            }
        });

        addComponent(delButton, 0, 5, 1, 1, 0, 0);
        addComponent(button, 3, 5, 1, 1, 0, 0, GridBagConstraints.NORTHEAST);

        addHorizontalLayoutStopper(6, 5);
        addLayoutStopper(0, 6);

        ListSelectionModel rowSM = table.getSelectionModel();
        rowSM.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                //Ignore extra messages.
                if (e.getValueIsAdjusting())
                    return;

                ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                delButton.setEnabled(!lsm.isSelectionEmpty());

                if (lsm.isSelectionEmpty())
                {
                    upButton.setEnabled(false);
                    downButton.setEnabled(false);
                }
                else
                {
                    int pos = table.getSelectedRow();

                    if (pos == 0)
                    {
                        upButton.setEnabled(false);
                    }
                    else
                    {
                        upButton.setEnabled(true);
                    }

                    if (pos == table.getRowCount() - 1)
                    {
                        downButton.setEnabled(false);
                    }
                    else
                    {
                        downButton.setEnabled(true);
                    }
                }
            }
        });
    }

    private List getHighlightings()
    {
        String path = "/gui/text windows/highlighting/";
        int i = 0;
        String node = path + String.valueOf(i) + "/";
        List data = new ArrayList();

        while (settings_manager.nodeExists(node) == true)
        {
            String filter = settings_manager.getString(node + "filter", "");
            String regexp = settings_manager.getString(node + "regexp", "");
            String format = settings_manager.getString(node + "format", colorRule.getValue().toString());
            if (filter.length() > 0 || regexp.length() > 0)
                data.add(new Highlighting(filter, regexp, format));

            i++;
            node = path + String.valueOf(i) + "/";
        }

        return data;
    }

    /* (non-Javadoc)
     * @see geoirc.conf.Storable#saveData()
     */
    public boolean saveData()
    {
        List list = ltm.getData();
        String path = "/gui/text windows/highlighting/";
        String node;
        settings_manager.removeNode(path);
        int a = 0;

        for (int i = 0; i < list.size(); i++)
        {
            node = path + String.valueOf(a) + "/";
            Highlighting hl = (Highlighting)list.get(i);
            //if(hl.getFilter().length() > 0 && hl.getFormat().length() > 0)
            if (hl.getFormat().length() > 0)
            {
                settings_manager.setString(node + "filter", hl.getFilter());
                settings_manager.setString(node + "regexp", hl.getRegexp());
                settings_manager.setString(node + "format", hl.getFormat());
                a++;
            }
        }

        return true;
    }

    /**
     * Set up the editor for the Color cells.<br>
     * source based on TableDialogEditDemo by Sun Microsystems
     * @param table
     */
    private void setUpColorEditor(JTable table)
    {
        //First, set up the button that brings up the dialog.
        final JButton button = new JButton("")
        {
            public void setText(String s)
            {
                    //Button never shows text -- only color.
    }
        };
        button.setBackground(Color.white);
        button.setBorderPainted(false);
        button.setMargin(new Insets(0, 0, 0, 0));

        //Now create an editor to encapsulate the button, and
        //set it up as the editor for all Color cells.
        final ColorEditor colorEditor = new ColorEditor(button);
        table.setDefaultEditor(Color.class, colorEditor);

        //Set up the dialog that the button brings up.
        final JColorChooser colorChooser = new JColorChooser();
        ActionListener okListener = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                colorEditor.currentColor = colorChooser.getColor();
            }
        };
        final JDialog dialog = JColorChooser.createDialog(button, "Pick a Color", true, colorChooser, okListener, null);

        //Here's the code that brings up the dialog.
        button.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                button.setBackground(colorEditor.currentColor);
                colorChooser.setColor(colorEditor.currentColor);
                //Without the following line, the dialog comes up
                //in the middle of the screen.
                //dialog.setLocationRelativeTo(button);
                dialog.show();
            }
        });
    }

    class LittleTableModel extends AbstractTableModel
    {
        final String[] columnNames = { "Filter", "Regular Expression", "Color", "Color Type" };
        List data = new ArrayList();

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
            if (c != 2)
                return getValueAt(0, c).getClass();
            else
                return Color.class;
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
            return getListValue(row, col);
        }

        public boolean isCellEditable(int row, int col)
        {
            return true;
        }

        public void setValueAt(Object value, int row, int col)
        {
            setListValue(value, row, col);
            fireTableCellUpdated(row, col);
        }

        private void setListValue(Object value, int row, int col)
        {
            Highlighting hl = (Highlighting)data.get(row);
            switch (col)
            {
                case 0 :
                    hl.setFilter((String)value);
                    break;
                case 1 :
                    hl.setRegexp((String)value);
                    break;
                case 2 :
                    Color color = (Color)value;
                    hl.setColorString(Util.colorToHexString(color));
                    break;
                case 3 :
                    hl.setColorPrefix((String)value);
                    break;
            }
        }

        private Object getListValue(int row, int col)
        {
            Highlighting hl = (Highlighting)data.get(row);
            Object result = null;

            switch (col)
            {
                case 0 :
                    result = hl.getFilter();
                    break;
                case 1 :
                    result = hl.getRegexp();
                    break;
                case 2 :
                    String str = hl.getColorString();
                    if (str != null)
                    {
                        if (str.length() == 6)
                        {
                            int[] rgb = Util.getRGB(str);
                            Color color = new Color(rgb[0], rgb[1], rgb[2]);
                            result = color;
                        }
                    }
                    break;
                case 3 :
                    result = hl.getColorPrefix();
                    break;
            }

            return result;
        }

        public void setData(List data)
        {
            this.data = data;
            fireTableDataChanged();
        }

        public List getData()
        {
            return this.data;
        }

        public void addRow(Object row)
        {
            data.add(row);
            fireTableDataChanged();
        }

        public void delRow(int row)
        {
            data.remove(row);
            fireTableDataChanged();
        }

        public Object getRow(int row)
        {
            return data.get(row);
        }

        public void setRow(int row, Object rowdata)
        {
            data.set(row, rowdata);
            fireTableDataChanged();
        }
    }

    /*
     * The editor button that brings up the dialog.
     * We extend DefaultCellEditor for convenience,
     * even though it mean we have to create a dummy
     * check box.  Another approach would be to copy
     * the implementation of TableCellEditor methods
     * from the source code for DefaultCellEditor.
     */
    class ColorEditor extends DefaultCellEditor
    {
        Color currentColor = null;

        public ColorEditor(JButton b)
        {
            super(new JCheckBox()); //Unfortunately, the constructor
            //expects a check box, combo box,
            //or text field.
            editorComponent = b;
            setClickCountToStart(1); //This is usually 1 or 2.

            //Must do this so that editing stops when appropriate.
            b.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    fireEditingStopped();
                }
            });
        }

        protected void fireEditingStopped()
        {
            super.fireEditingStopped();
        }

        public Object getCellEditorValue()
        {
            return currentColor;
        }

        public Component getTableCellEditorComponent(
            JTable table,
            Object value,
            boolean isSelected,
            int row,
            int column)
        {
            ((JButton)editorComponent).setText(value.toString());
            currentColor = (Color)value;
            return editorComponent;
        }
    }

}
