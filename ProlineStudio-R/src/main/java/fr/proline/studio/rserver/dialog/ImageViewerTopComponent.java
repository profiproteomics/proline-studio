package fr.proline.studio.rserver.dialog;

import fr.proline.studio.utils.IconManager;
import java.awt.*;
import javax.swing.JPanel;
import org.openide.windows.TopComponent;

/**
 *
 * @author JM235353
 */
public class ImageViewerTopComponent extends TopComponent {
    
    public ImageViewerTopComponent(String name, Image img) {
        
        // Set Name
        setName(name); 
        
        // Set Tooltip
        setToolTipText(name);
        
        setLayout(new GridLayout());
        add(new ImagePanel(img));
    }
    
    @Override
    public Image getIcon() {
        return IconManager.getImage(IconManager.IconType.WAVE);
    }
    
    public class ImagePanel extends JPanel {
        
        private Dimension m_dimension = null;
        private Image m_img = null;
        
        public ImagePanel(Image img) {
            m_dimension = new Dimension(img.getWidth(null),img.getHeight(null));
            m_img = img;
        }
        
        @Override
        public Dimension getPreferredSize() {
            return m_dimension;
        }
        
        @Override
        public void paint(Graphics g) {
            
            g.setColor(Color.white);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.drawImage(m_img, 0, 0, this);
        }
    }
}
