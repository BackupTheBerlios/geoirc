/*
 * RegExpTesterInputFieldEditor.java
 * 
 * Created on 04.10.2003
 */
package geoirc.conf;

import geoirc.util.JBoolRegExTextField;
import geoirc.util.JRegExTextField;
import geoirc.util.JValidatingTextField;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

/**
 * @author netseeker aka Michael Manske
 */
public class RegExpTesterInputFieldEditor extends AbstractCellEditor implements TableCellEditor
{
    private JTextField input;
    private JPanel panel;
    private final RegularExpressionTester tester =
        new RegularExpressionTester(RegularExpressionTester.SHOW_APPLY_CANCEL_OPTION);

    public static final int INPUT_DEFAULT_TEXTFIELD = 0;
    public static final int INPUT_VALIDATING_TEXTFIELD = 1;
    public static final int INPUT_VALIDATING_REGEXP_TEXTFIELD = 2;
    public static final int INPUT_VALIDATION_BOOLEXP_TEXTFIELD = 3;

    public RegExpTesterInputFieldEditor(int input_type)
    {
        switch (input_type)
        {
            case INPUT_DEFAULT_TEXTFIELD :
                this.input = new JTextField();
                break;
            case INPUT_VALIDATING_TEXTFIELD :
                this.input = new JValidatingTextField(null);
                break;
            case INPUT_VALIDATING_REGEXP_TEXTFIELD :
                this.input = new JRegExTextField(null);
                break;
            case INPUT_VALIDATION_BOOLEXP_TEXTFIELD :
                this.input = new JBoolRegExTextField(null);
                break;
            default :
                this.input = new JTextField();
        }

        initComponents();
    }

    public RegExpTesterInputFieldEditor(JValidatingTextField input_field)
    {
        this.input = input_field;
        initComponents();
    }

    private void initComponents()
    {
        panel = new JPanel();
        this.panel.setLayout(new BoxLayout(panel, 0));

        JButton button = new JButton("..");
        Dimension d = new Dimension(20, 20);
        button.setPreferredSize(d);
        button.setMaximumSize(d);

        button.addActionListener(new ActionListener()
        {
            boolean isDialogShow = false;

            public void actionPerformed(ActionEvent arg0)
            {
                Object value = getCellEditorValue();
                tester.setRegExp((String)value);
                tester.addCloseListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent evt)
                    {
                        if (evt.getActionCommand().equals(RegularExpressionTester.APPLY_OPTION))
                        {
                            input.setText(tester.getRegExp());
                        }
                        fireEditingStopped();
                    }
                });

                tester.setVisible(true);
            }
        });

        this.input.setEditable(true);
        this.panel.add(this.input);
        this.panel.add(button);
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
     */
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
    {
        if (value instanceof String)
        {
            this.input.setText((String)value);
        }

        return this.panel;
    }

    /* (non-Javadoc)
     * @see javax.swing.CellEditor#getCellEditorValue()
     */
    public Object getCellEditorValue()
    {
        return this.input.getText();
    }
}
