package fr.proline.studio.rserver.node;

import fr.proline.studio.rserver.data.AbstractRData;
import fr.proline.studio.utils.IconManager;
import javax.swing.ImageIcon;

/**
 *
 * @author JM235353
 */
public class RMsnSetNode extends RNode {

    
    private boolean m_scriptStart;
    
    public RMsnSetNode(AbstractRData data, boolean scriptStart) {
        super(RNode.NodeTypes.MSN_SET, data);
        
        m_scriptStart = scriptStart;
    }


    
    @Override
    public ImageIcon getIcon() {
        return getIcon(IconManager.IconType.MSN_SET);
    }
    
    public boolean isScriptStart() {
        return m_scriptStart;
    }
}
