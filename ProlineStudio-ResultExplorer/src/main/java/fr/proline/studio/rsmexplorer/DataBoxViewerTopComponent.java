package fr.proline.studio.rsmexplorer;

import fr.proline.studio.pattern.WindowBox;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import org.openide.windows.TopComponent;

/**
 *
 * @author JM235353
 */
public class DataBoxViewerTopComponent extends TopComponent {

    WindowBox m_windowBox = null;
    
    /**
     * Creates new form DataBoxViewerTopComponent
     */
    public DataBoxViewerTopComponent(WindowBox windowBox) {
        
        m_windowBox = windowBox;
        
        // Add panel
        setLayout(new GridLayout());
        add(windowBox.getPanel());
        
        // Set Name
        setName(windowBox.getName()); 
        
        // Set Tooltip
        setToolTipText(windowBox.getName()); 

    }

    @Override
    public Image getIcon() {
        return m_windowBox.getIcon();
    }
    
    
    @Override
    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        
        // JPM.HACK : force the default size after the first display of the window
        // I have not found another way to do it.
        if (firstPaint) {
            firstPaint = false;
            
            // size correctly the sub panels
            m_windowBox.resetDefaultSize();
        }
    }
    private boolean firstPaint = true;

}
