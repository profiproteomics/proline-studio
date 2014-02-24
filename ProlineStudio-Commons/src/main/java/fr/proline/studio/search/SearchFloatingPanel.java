package fr.proline.studio.search;

import fr.proline.studio.utils.IconManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

/**
 *
 * @author JM235353
 */
public class SearchFloatingPanel extends JPanel {
    
    private JToggleButton m_srcButton = null;
    private AbstractSearch m_search = null;
    
    private JButton m_searchButton;
    private JTextField m_searchTextField;

    
    public SearchFloatingPanel(AbstractSearch search) {
        

        m_search = search;
        
        setBorder(BorderFactory.createLineBorder(Color.darkGray, 1, true));
        setOpaque(true);
        setLayout(new FlowLayout());
        
        JButton closeButton = new JButton(IconManager.getIcon(IconManager.IconType.CROSS_SMALL7));
        closeButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        //closeButton.setBorderPainted(false);
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        

        closeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                if (m_srcButton != null) {
                    m_srcButton.setSelected(false);
                }
            }
        });
        
        m_searchTextField = new JTextField(16);
        
        m_searchButton = new JButton(IconManager.getIcon(IconManager.IconType.SEARCH));
        m_searchButton.setMargin(new Insets(1, 1, 1, 1));
        
        m_searchButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                final String searchText = m_searchTextField.getText();
                m_search.doSearch(searchText);
            }
        });
        
        add(closeButton);
        add(m_searchTextField);
        add(m_searchButton);

        
        Dimension d = getPreferredSize();
        setBounds(0,0,(int)d.getWidth(),(int)d.getHeight());
        
        
        MouseAdapter dragGestureAdapter = new MouseAdapter() {
            int dX, dY;
            
            @Override
            public void mouseDragged(MouseEvent e) {
                Component panel = e.getComponent();
                
                int newX = e.getLocationOnScreen().x - dX;
                int newY = e.getLocationOnScreen().y - dY;
                
                Component parentComponent = panel.getParent();
                int  parentX = parentComponent.getX();
                if (newX<parentX) {
                    newX = parentX;
                }
                int parentY = parentComponent.getY();
                if (newY<parentY) {
                    newY = parentY;
                }
                int parentWidth = parentComponent.getWidth();
                if (newX+panel.getWidth()>parentWidth-parentX) {
                    newX = parentWidth-parentX-panel.getWidth();
                }
                int parentHeight = parentComponent.getHeight();
                if (newY+panel.getHeight()>parentHeight-parentY) {
                    newY = parentHeight-parentY-panel.getHeight();
                }
                
                
                panel.setLocation(newX, newY);
                
                dX = e.getLocationOnScreen().x - panel.getX();
                dY = e.getLocationOnScreen().y - panel.getY();
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                JPanel panel = (JPanel) e.getComponent();
                panel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                dX = e.getLocationOnScreen().x - panel.getX();
                dY = e.getLocationOnScreen().y - panel.getY();
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                JPanel panel = (JPanel) e.getComponent();
                panel.setCursor(null);
            }
        };
        
        
        addMouseMotionListener(dragGestureAdapter);
        addMouseListener(dragGestureAdapter);
        
        setVisible(false);

        search.setFloatingPanel(this);
    }

    public void setToggleButton(JToggleButton srcButton) {
        m_srcButton = srcButton;
    }
    
    public void enableSearch(boolean enable) {
        if (m_srcButton != null) {
            m_searchButton.setEnabled(enable);
        }
    }
    
    public void setFocus() {
        m_searchTextField.requestFocus();
    }
    
    
}
