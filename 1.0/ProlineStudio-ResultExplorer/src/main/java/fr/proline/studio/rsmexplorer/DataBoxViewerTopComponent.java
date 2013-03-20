package fr.proline.studio.rsmexplorer;

import fr.proline.studio.pattern.WindowBox;
import java.awt.Graphics;
import java.awt.GridLayout;
import org.openide.windows.TopComponent;

/**
 *
 * @author JM235353
 */
public class DataBoxViewerTopComponent extends TopComponent {

    WindowBox windowBox = null;
    
    /**
     * Creates new form DataBoxViewerTopComponent
     */
    public DataBoxViewerTopComponent(WindowBox windowBox) {
        
        this.windowBox = windowBox;
        
        // Add panel
        setLayout(new GridLayout());
        add(windowBox.getPanel());
        
        // Set Name
        setName(windowBox.getName()); 
        
        // Set Tooltip
        setToolTipText(windowBox.getName()); 
        
        
        
        
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
            windowBox.resetDefaultSize();
        }
    }
    private boolean firstPaint = true;

}
