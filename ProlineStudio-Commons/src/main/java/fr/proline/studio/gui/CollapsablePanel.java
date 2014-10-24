package fr.proline.studio.gui;

import fr.proline.studio.utils.IconManager;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.util.ArrayList;
import javax.swing.ImageIcon;
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
    private boolean m_selected;  
    /* content panel */
    private JPanel m_contentPanel;  
    /* header panel with title */
    private HeaderPanel m_headerPanel; 
    
    /* Collapse listener list */
    private List<CollapseListener> m_listeners = new ArrayList<>();
    
    /*default font for the title*/
    private final static  Font DEFAULT_HEADER_FONT = new Font("Tahoma", Font.PLAIN, 11); 
    
    
    
    
    
    /**
     * header panel, always displayed, containing the arrow and the title
     */
    private class HeaderPanel extends JPanel implements MouseListener {  
        private String m_headerText;  
        private Font m_font;  
        private ImageIcon m_open;
        private ImageIcon m_closed;  
        
        /* distance between the image and the text*/
        private final int OFFSET = 25;
        /*pad for displaying the image */
        private final int PADX = 5;
        private final int PADY = 2;  
        
  
        /**
         * HeaderPanel contructor with a title and a font
         * @param text
         * @param font 
         */
        public HeaderPanel(String text, Font font) { 
            this.m_headerText = text;  
            this.m_font = font;  
            initHeaderPanel();
  
        }  
        
        /* initialization of the header panel, loads the images*/
        private void initHeaderPanel(){
            addMouseListener(this); 
            // setRequestFocusEnabled(true);  
            setPreferredSize(new Dimension(200, 20));  
            getWidth();  
            getHeight();  
  
            try { 
                this.m_open = IconManager.getIcon(IconManager.IconType.ARROW_DOWN);
                this.m_closed = IconManager.getIcon(IconManager.IconType.ARROW_RIGHT);
            } catch (Exception e) { 
                LoggerFactory.getLogger("ProlineStudio.Commons").error(getClass().getSimpleName() + " failed", e);
            } 
        }
  
        @Override
        protected void paintComponent(Graphics g) {  
            super.paintComponent(g);  
            Graphics2D g2 = (Graphics2D) g;  
            
            //icon 
            int h = this.m_open.getIconHeight();  
            int w = this.m_open.getIconWidth(); 
            if (m_selected) 
                g2.drawImage(this.m_open.getImage(), PADX, PADY, w, h, this); 
            else 
                g2.drawImage(this.m_closed.getImage(), PADX, PADY, w, h, this); 
            // title
            g2.setFont(this.m_font);  
            FontRenderContext frc = g2.getFontRenderContext();  
            LineMetrics lm = this.m_font.getLineMetrics(this.m_headerText, frc);  
            float height = lm.getAscent() + lm.getDescent();  
            float x = OFFSET;  
            float y = (h + height) / 2 - lm.getDescent();  
            g2.drawString(this.m_headerText, x, y);  
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
    
    
    
    
    
    /**
     * New CollapsablePanel with the title, the content panel and the default value for exanding the panel
     * @param headerTitle
     * @param contentPanel
     * @param defaultExpand 
     */
    public CollapsablePanel(String headerTitle, JPanel contentPanel, boolean defaultExpand) {  
        super(new GridBagLayout());  
        this.m_selected = defaultExpand ;
        initPanel(headerTitle, contentPanel);
  
    }  
    
    /* builds the headerPanel and add the content panel */
    private void initPanel(String text, JPanel panel) {
        GridBagConstraints gbc = new GridBagConstraints();  
        gbc.insets = new Insets(1, 3, 0, 3);  
        gbc.weightx = 1.0;  
        gbc.fill = GridBagConstraints.HORIZONTAL;  
        gbc.gridwidth = GridBagConstraints.REMAINDER;  
  
        m_headerPanel = new HeaderPanel(text, DEFAULT_HEADER_FONT); 
        m_contentPanel = panel;  
  
        add(m_headerPanel, gbc);  
        add(m_contentPanel, gbc);  
        m_contentPanel.setVisible(m_selected);  
  
        JLabel padding = new JLabel();  
        gbc.weighty = 1.0;  
        add(padding, gbc); 
    }
  
    /**
     * method called by clicking on the arrow, changes the status 
     */
    public void toggleSelection() {  
        m_selected = !m_selected;  
  
        if (m_contentPanel.isShowing())  
            m_contentPanel.setVisible(false);  
        else  
            m_contentPanel.setVisible(true);  
  
        validate();  
  
        m_headerPanel.repaint();
        // Notify 
        for (CollapseListener l : m_listeners) {
            l.collapse();
        }
    }  
    
    /**
     * add a new listener @see CollapseListener
     * @param toAdd 
     */
    public void addCollapseListener(CollapseListener toAdd) {
        m_listeners.add(toAdd);
    }
    
    
}
