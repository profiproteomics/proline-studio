package fr.proline.studio.gui;

import fr.proline.studio.utils.IconManager;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.slf4j.LoggerFactory;
import java.util.List;


/**
 * The collapsable panel is a panel which can be expanded or collapsed. It contains a header panel, always displayed, and a content panel
 * The header panel contains the arrow (down/right) and the title
 * if the component containing the CollapsablePanel needs to know the current status (expand/collapse) for example fo rresizing, 
 * it must implement the @see CollapseListener interface
 * @author MB243701
 */

public class CollapsablePanel extends JPanel {
    
    /* true when the panel is expanded */
    private boolean m_expanded;  
    
    /* content panel */
    private JPanel m_contentPanel;  
    
    /* header panel with title */
    private HeaderLabel m_headerLabel; 
    
    /* Collapse listener list */
    private List<CollapseListener> m_listeners = new ArrayList<>();

    
        /**
     * New CollapsablePanel with the title, the content panel and the default value for exanding the panel
     * @param headerTitle
     * @param contentPanel
     * @param defaultExpand 
     */
    public CollapsablePanel(String headerTitle, JPanel contentPanel, boolean defaultExpand) {  
        super(new GridBagLayout());  
        m_expanded = defaultExpand ;
        initPanel(headerTitle, contentPanel);
  
    }  
    
    /* builds the headerPanel and add the content panel */
    private void initPanel(String text, JPanel panel) {
        GridBagConstraints gbc = new GridBagConstraints();  
        gbc.insets = new Insets(1, 3, 0, 3);  
        gbc.weightx = 1.0;  
        gbc.fill = GridBagConstraints.HORIZONTAL;  
        gbc.gridwidth = GridBagConstraints.REMAINDER;  
  
        m_headerLabel = new HeaderLabel(text, m_expanded); 
        m_contentPanel = panel;  
  
        add(m_headerLabel, gbc);  
        add(m_contentPanel, gbc);  
        m_contentPanel.setVisible(m_expanded);  
  
        JLabel padding = new JLabel();  
        gbc.weighty = 1.0;  
        add(padding, gbc); 
    }
  
    /**
     * method called by clicking on the arrow, changes the status 
     */
    public void toggleSelection() {  
        m_expanded = !m_expanded;  
  
        m_headerLabel.setIcon(null);
        
        if (m_contentPanel.isShowing())  
            m_contentPanel.setVisible(false);  
        else  
            m_contentPanel.setVisible(true);  
  
        validate();  
  
        m_headerLabel.setIcon(m_expanded ? IconManager.getIcon(IconManager.IconType.ARROW_DOWN) : IconManager.getIcon(IconManager.IconType.ARROW_RIGHT));
        
        m_headerLabel.repaint();
        // Notify 
        for (CollapseListener l : m_listeners) {
            l.collapse(!m_expanded);
        }
    }  
    
    /**
     * add a new listener @see CollapseListener
     * @param toAdd 
     */
    public void addCollapseListener(CollapseListener toAdd) {
        m_listeners.add(toAdd);
    }
    
    
    /**
     * header label, always displayed, containing the arrow and the title
     */
    private class HeaderLabel extends JLabel implements MouseListener {  
 
        /**
         * HeaderPanel contructor with a title and a font
         * @param text
         * @param font 
         */
        public HeaderLabel(String text, boolean defaultExpand) { 
            super(text, defaultExpand ? IconManager.getIcon(IconManager.IconType.ARROW_DOWN) : IconManager.getIcon(IconManager.IconType.ARROW_RIGHT) , JLabel.LEFT);
            addMouseListener(this); 
  
        }  

        @Override
        public void mouseClicked(MouseEvent e) {  
            toggleSelection();  
        }  
  
        @Override
        public void mouseEntered(MouseEvent e) {  
        }  
  
        @Override
        public void mouseExited(MouseEvent e) {  
        }  
  
        @Override
        public void mousePressed(MouseEvent e) {  
        }  
  
        @Override
        public void mouseReleased(MouseEvent e) {  
        }  
  
    } // end header panel
    
    
    
    
    

    
    
}
