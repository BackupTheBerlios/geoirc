/*
 * BaseXmlHandler.java
 * 
 * Created on 09.08.2003
 */
package geoirc;

import geoirc.util.TypeConverter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.StringTokenizer;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

/**
 * Handling of xml based configuration files<br>
 * 
 * @author netseeker aka Michael Manske
 */
public class BaseXmlHandler implements XmlProcessable
{
	protected Element root = null;
	protected String filePath = null;
	protected boolean isResource = false;
	protected DisplayManager displayManager = null;
	protected boolean autoSave = false;

	/**
	 * @param displayManager
	 * @param filePath
	 */
	public BaseXmlHandler(DisplayManager displayManager, String filePath)
	{
		this.displayManager = displayManager;
		this.filePath = filePath;
	}

	/**
	 * @param displayManager
	 * @param filePath
	 */
	public BaseXmlHandler(DisplayManager displayManager, String filePath, boolean isResource)
	{
		this(displayManager, filePath);
		this.isResource = isResource;
	}

	/**
	 * @param key
	 * @param def
	 * @return
	 */
	public String get(String key, String def)
	{
		String value = null;

		try
		{
			value = getValueByXPath(key);
		}
		catch (JDOMException je)
		{
			return def;
		}

		if (value != null)
			return value;

		return def;
	}

	/**
	 * @param key
	 * @param def
	 * @return
	 */
	public int getInt(String key, int def)
	{
		try
		{
			String value = getValueByXPath(key);
			return Integer.parseInt(value);
		}
		catch (Exception e)
		{
			return def;
		}
	}

	/**
	 * @param key
	 * @param def
	 * @return
	 */
	public boolean getBoolean(String key, boolean def)
	{
		String value = null;

		try
		{
			value = getValueByXPath(key);
		}
		catch (JDOMException je)
		{
			return def;
		}

		if (value.equalsIgnoreCase("true")
			|| value.equalsIgnoreCase("on")
			|| value.equals("1"))
			return true;
		else if (
			value.equalsIgnoreCase("false")
				|| value.equalsIgnoreCase("off")
				|| value.equals("0"))
			return false;

		return def;
	}

	/**
	 * @param key
	 * @param def
	 * @return
	 */
	public String getString(String key, String def)
	{
		return get(key, def);
	}

	/**
	 * @param key
	 * @param def
	 * @return
	 */
	public BigDecimal getBigDecimal(String key, BigDecimal def)
	{
		String value = null;

		try
		{
			value = getValueByXPath(key);
		}
		catch (JDOMException je)
		{
			return def;
		}

		return (BigDecimal) TypeConverter.convert(
			value,
			TypeConverter.BIGDECIMAL,
			def);
	}

	/**
	 * @param key
	 * @param value
	 */
	public synchronized void set(String key, String value)
	{
		Element elem = getSafeElementByPath(key);
		elem.setText(value);

		if (autoSave == true)
			saveSettingsToXML();
	}
    
	/**
	 * @param key
	 * @param value
	 */
	public void setInt(String key, int value)
	{
		set(key, String.valueOf(value));
	}

    public void putInt(String key, int value)
    {
        setInt(key, value);
    }
    
	/**
	 * @param key
	 * @param value
	 */
	public void setBoolean(String key, boolean value)
	{
		set(key, String.valueOf(value));
	}
    
    public void putBoolean(String key, boolean value)
    {
        setBoolean(key, value);
    }

	/**
	 * @param key
	 * @param value
	 */
	public void setString(String key, String value)
	{
		set(key, value);
	}
    
    public void putString(String key, String value)
    {
        setString(key, value);
    }

	/**
	 * @param key
	 * @param value
	 */
	public void setBigDecimal(String key, BigDecimal value)
	{
		set(key, String.valueOf(value));
	}

	/**
	 * 
	 */
	public void listenToPreferences()
	{
		autoSave = true;
	}

	/**
	 * @return
	 */
	public synchronized boolean loadSettingsFromXML()
	{
		SAXBuilder builder = new SAXBuilder();
		Document doc = null;
		try
		{
			if(isResource == false)
			{
				StringReader in = new StringReader(filePath);
				doc = builder.build(in);
			}
			else
			{
				InputStream in = GeoIRC.class.getResourceAsStream(filePath);
				doc = builder.build(in);
			}
			root = doc.getRootElement();
			return true;
		}
		catch (JDOMException e)
		{
			printlnDebug(
				"Invalid format in '"
					+ filePath
					+ "'; cannot import settings.");
		}
		catch (IOException e)
		{
			printlnDebug(
				"I/O problem while trying to load settings from '"
					+ filePath
					+ "'.");
		}

		return false;
	}

	/**
	 * 
	 */
	public void printSettings()
	{
		XMLOutputter out = new XMLOutputter();
		StringWriter outWriter = new StringWriter();
		out.setTextTrim(true);
		out.setIndent("  ");
		out.setNewlines(true);

		try
		{
			out.output(root.getDocument(), outWriter);
			printlnDebug(outWriter.toString());
		}
		catch (IOException e)
		{
			printlnDebug(e.getLocalizedMessage());
		}
	}

	/**
	 * @return
	 */
	public synchronized boolean saveSettingsToXML()
	{
		XMLOutputter out = new XMLOutputter();
		FileOutputStream outStream = null;
		try
		{
			outStream = new FileOutputStream(filePath);
		}
		catch (FileNotFoundException e1)
		{
			printlnDebug(e1.getLocalizedMessage());
			return false;
		}

		out.setTextTrim(true);
		out.setIndent("  ");
		out.setNewlines(true);

		try
		{
			out.output(root.getDocument(), outStream);
			outStream.close();
		}
		catch (IOException e)
		{
			printlnDebug(e.getLocalizedMessage());
			return false;
		}

		return true;
	}

	/**
	 * @param expr
	 * @return
	 * @throws JDOMException
	 */
	protected String getValueByXPath(String expr) throws JDOMException
	{
		XPath xPath = XPath.newInstance(expr);
		Object obj = xPath.selectSingleNode(root);
		return xPath.valueOf(obj);
	}

	/**
	 * @param path
	 * @return
	 */
	protected String getKey(String path)
	{
		String key = path;
		int index = path.lastIndexOf("/");
		if (index > -1)
		{
			// We add 1 in order to exclude the slash.
			key = path.substring(index + 1);
		}

		return key;
	}

	/**
	 * @param path
	 * @return
	 */
	protected String getNodePath(String path)
	{
		String nodepath = "";

		int index = path.lastIndexOf("/");
		if (index > -1)
		{
			int start = 0;
			if (path.startsWith("/"))
				start = 1;

			nodepath = path.substring(start, index);
		}

		return nodepath;
	}

	protected void printlnDebug(String s)
	{
		if (displayManager != null)
		{
			displayManager.printlnDebug(s);
		}
		else
		{
			System.err.println(s);
		}
	}

	/* (non-Javadoc)
	 * @see geoirc.XmlProcessable#removeNode(java.lang.String)
	 */
	public synchronized boolean removeNode(String path)
	{
		try
		{
			getElementByPath(path).getParent().removeChild(getKey(path));
            if (autoSave == true)
                saveSettingsToXML();
            
			return true;
		}
		catch (Exception e)
		{
			printlnDebug(e.getLocalizedMessage());
			return false;
		}
	}

	/**
	 * @param path
	 * @return
	 */
	protected Element getElementByPath(String path)
	{
		StringTokenizer tok = new StringTokenizer(path, "/");
		Element elem = root;

		while (tok.hasMoreTokens())
		{
			elem = elem.getChild(tok.nextToken());
		}

		return elem;
	}

	/**
	 * @param path
	 * @return
	 */
	protected Element getSafeElementByPath(String path)
	{
		StringTokenizer tok = new StringTokenizer(path, "/");
		Element elem = root;
		Element oldElem = root;
		boolean create = false;

		while (tok.hasMoreTokens())
		{
			String name = tok.nextToken();

			if (create == false)
			{
				elem = elem.getChild(name);
				if (elem == null)
				{
					elem = new Element(name);
					oldElem.addContent(elem);
					oldElem = elem;
					create = true;
				}

			}
			else
			{
				elem = new Element(name);
				oldElem.addContent(elem);
				oldElem = elem;
			}
		}

		return elem;
	}

	/* (non-Javadoc)
	 * @see geoirc.XmlProcessable#getBuffer()
	 */
	public Object getBuffer()
	{
		return root; 
	}

	public boolean nodeExists(String nodePath)
	{
		XPath xPath;
		Object obj;
		try
		{
			xPath = XPath.newInstance(nodePath);
			obj = xPath.selectSingleNode(root);
		}
		catch (JDOMException e)
		{
			return false;
		}
		 
		return (obj == null) ? false : true;
	}


}
