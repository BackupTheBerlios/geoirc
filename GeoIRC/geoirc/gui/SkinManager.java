/*
 * SkinManager.java
 * 
 * Created on 10.10.2003
 */
package geoirc.gui;

import geoirc.I18nManager;

import javax.swing.UIManager;

import com.l2fprod.gui.plaf.skin.CompoundSkin;
import com.l2fprod.gui.plaf.skin.Skin;
import com.l2fprod.gui.plaf.skin.SkinLookAndFeel;

/**
 * @author netseeker aka Michael Manske
 */
public class SkinManager
{
    private String skin_messages;
    
    public SkinManager()
    {
        skin_messages = "";
    }

    public boolean applySkin(String skin1, String skin2, I18nManager i18n_manager)
    {
        Skin skin = null;
        skin_messages = "";

        try
        {
            if ((skin1 != null) && (skin2 != null))
            {
                skin = new CompoundSkin(SkinLookAndFeel.loadSkin(skin1), SkinLookAndFeel.loadSkin(skin2));
            }
            else if (skin1 != null)
            {
                skin = SkinLookAndFeel.loadSkin(skin1);
                skin_messages += i18n_manager.getString("no second skin");
            }
            else
            {
                skin_messages += i18n_manager.getString("no skins");
            }

            if (skin != null)
            {
                SkinLookAndFeel.setSkin(skin);
                UIManager.setLookAndFeel("com.l2fprod.gui.plaf.skin.SkinLookAndFeel");
                UIManager.setLookAndFeel(new SkinLookAndFeel());
                skin_messages += i18n_manager.getString("skin applied");
            }
            else
            {
                skin_messages += i18n_manager.getString("no skin applied");
            }
        }
        catch (Exception e)
        {
            skin_messages += i18n_manager.getString("skin failure");
            if (skin1 != null)
            {
                skin_messages += "(" + skin1 + ")\n";
            }
            if (skin2 != null)
            {
                skin_messages += "(" + skin2 + ")\n";
            }
            skin_messages += e.getMessage() + "\n";
            
            return false;
        }
        
        return true;
    }
    
    public String getSkinMessages()
    {
        return this.skin_messages;
    }
}
