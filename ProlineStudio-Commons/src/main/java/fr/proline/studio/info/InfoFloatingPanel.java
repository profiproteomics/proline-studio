package fr.proline.studio.info;

import fr.proline.studio.utils.IconManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

/**
 *
 * Floating Panel to display an info as text
 * 
 * @author JM235353
 */
public class InfoFloatingPanel extends JPanel {

    private JToggleButton m_infoButton = null;

    private JLabel m_infoLabel = null;

    public InfoFloatingPanel() {

        setBorder(BorderFactory.createLineBorder(Color.darkGray, 1, true));
        setOpaque(true);
        setLayout(new FlowLayout());
        
        JButton closeButton = new JButton(IconManager.getIcon(IconManager.IconType.CROSS_SMALL7));
        closeButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        

        closeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                if (m_infoButton != null) {
                    m_infoButton.setSelected(false);
                }
            }

        });

        m_infoLabel = new JLabel();
        
        add(closeButton);
        add(m_infoLabel);

        
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

    }

    public void setInfo(String info) {
        m_infoLabel.setText(info);
        Dimension d = getPreferredSize();
        setSize((int)d.getWidth(),(int)d.getHeight());
    }
    
    public void setToggleButton(JToggleButton srcButton) {
        m_infoButton = srcButton;
    }

    
}
