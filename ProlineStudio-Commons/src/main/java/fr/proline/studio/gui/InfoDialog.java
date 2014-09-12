package fr.proline.studio.gui;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.utils.IconManager;
import java.awt.*;
import java.util.prefs.Preferences;
import javax.swing.*;
import org.openide.util.NbPreferences;



/**
 * Dialog to display an information (warning, question or info)
 * @author JM235353
 */
public class InfoDialog extends DefaultDialog {
    
    public enum InfoType {
        WARNING,
        QUESTION,
        INFO
    }
    
    private String m_message = null;
    
    private JCheckBox m_showNextTimecheckBox = null;
    private String m_showAtStartKey = null;
    
    public InfoDialog(Window parent, InfoType type, String title, String message) {
        this(parent, type, title, message, null);
    }
    public InfoDialog(Window parent, InfoType type, String title, String message, String showAtStartKey) {

        super(parent);

        setTitle(title);
        m_message = message;
        m_showAtStartKey = showAtStartKey;

        // hide default and ol button
        setButtonVisible(BUTTON_HELP, false);
        setButtonVisible(BUTTON_DEFAULT, false);
        setButtonName(BUTTON_OK, "Yes");
        setButtonName(BUTTON_CANCEL, "No");
        setStatusVisible(false);

        
        // Image at left
        Icon icon = null;
        switch (type) {
            case WARNING:
                icon = IconManager.getIcon(IconManager.IconType.BIG_WARNING);
                break;
            case QUESTION:
                icon = IconManager.getIcon(IconManager.IconType.BIG_HELP);
                break;
            case INFO:
                icon = IconManager.getIcon(IconManager.IconType.BIG_INFO);
                break;
        }


        JLabel leftImageLabel = new JLabel(icon);
        leftImageLabel.setBackground(Color.white);

        // info panel
        JPanel infoPanel = getInfoPanel();

        
        JPanel internalPanel = new JPanel(new GridBagLayout());
        internalPanel.setBackground(Color.white);
        internalPanel.setOpaque(true);
        internalPanel.setBorder(BorderFactory.createTitledBorder(""));
        

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;
        internalPanel.add(leftImageLabel, c);
        
        c.weightx = 1;
        c.gridx++;
        internalPanel.add(infoPanel, c);
        
        if (showAtStartKey != null) {
            
            m_showNextTimecheckBox = new JCheckBox("Do not show this dialog again at start");
            m_showNextTimecheckBox.setBackground(Color.white);
            Preferences preferences = NbPreferences.root();
            boolean selected = preferences.getBoolean(showAtStartKey, Boolean.FALSE);
            m_showNextTimecheckBox.setSelected(selected);
   
            c.weighty = 0;
            c.gridy++;
            c.gridx=0;
            c.gridwidth=2;
            internalPanel.add(m_showNextTimecheckBox, c);
            
        }

        
        
        setInternalComponent(internalPanel);

    }

    private JPanel getInfoPanel() {
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(Color.white);
        
        
        
        infoPanel.setLayout(new GridBagLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder(""));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        if (m_message != null) {
            c.gridwidth = 2;
            String messageArray[] = m_message.split("\n");
            Font font = null;
            for (int i=0;i<messageArray.length;i++) {
                JLabel messageLabel = new JLabel(messageArray[i]);
                if (font == null) {
                    font = messageLabel.getFont().deriveFont(14).deriveFont(Font.BOLD);
                }
                messageLabel.setFont(font);
                
                
                messageLabel.setBackground(Color.white);
                infoPanel.add(messageLabel, c);
                c.gridy++;
            }
            c.gridwidth = 1;
        }

        
        return infoPanel;
    }


    @Override
    protected boolean okCalled() {
        
        if (m_showNextTimecheckBox != null) {
            Preferences preferences = NbPreferences.root();
            preferences.putBoolean(m_showAtStartKey, !m_showNextTimecheckBox.isSelected());
        }
        
        return true;
    }
    @Override
    protected boolean cancelCalled() {
        return true;
    }

    public static boolean showAtStart(String showAtStartKey) {
        Preferences preferences = NbPreferences.root();
        return preferences.getBoolean(showAtStartKey, Boolean.TRUE);
    }
    
    
}
