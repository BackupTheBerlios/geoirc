/*
 * FileChooserHandler.java
 * 
 * Created on 21.08.2003
 */
package geoirc.conf;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JTextField;

/**
 * @author netseeker aka Michael Manske
 */
public class FileChooserHandler implements ActionListener
{
	private JTextField field;
	private BaseSettingsPanel pane;
    private String path;
		
	JFileChooser chooser = new JFileChooser();

	public FileChooserHandler(JTextField field, BaseSettingsPanel parent)
	{
		this.field = field;
		this.pane = parent;
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	}

    public FileChooserHandler(JTextField field, BaseSettingsPanel parent, int selection_mode)
    {
        this.field = field;
        this.pane = parent;
        chooser.setFileSelectionMode(selection_mode);
    }

    public FileChooserHandler(JTextField field, BaseSettingsPanel parent, String path)
    {
        this(field, parent);
        this.path = path;
    }

    public FileChooserHandler(JTextField field, BaseSettingsPanel parent, String path, int selection_mode)
    {
        this(field, parent, path);
        chooser.setFileSelectionMode(selection_mode);
    }

	public void actionPerformed(ActionEvent arg0)
	{
		chooser.setCurrentDirectory(new File(this.path));
        int returnVal = chooser.showOpenDialog(pane);
		if(returnVal == JFileChooser.APPROVE_OPTION) 
		{				   
			field.setText(chooser.getSelectedFile().getPath());
		}		
	}
}
