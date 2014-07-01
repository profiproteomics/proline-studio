package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.RSMTree;
import fr.proline.studio.rsmexplorer.node.xic.RSMRunNode;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;

/**
 *
 * @author JM235353
 */
public class DesignTree extends RSMTree {
    
    private static DesignTree m_designTree = null;
    
    public static DesignTree getDesignTree() {
        return m_designTree;
    }
    
    public static DesignTree getDesignTree(RSMNode top) {
        m_designTree = new DesignTree(top);
        return m_designTree;
    }
    
    
    private DesignTree(RSMNode top) {
        
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        SelectionTransferHandler handler = new SelectionTransferHandler(false);
        setTransferHandler(handler);

        setDragEnabled(true);

        initTree(top);
    }

    

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            
            RSMNode[] selectedNodes = getSelectedNodes();
            int nbNodes = selectedNodes.length;
            if (nbNodes == 1) {
                RSMNode n = selectedNodes[0];
                if (n instanceof RSMRunNode) {
                    RSMRunNode runNode = (RSMRunNode) n;
                    //JPM.TODO
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
