package fr.proline.studio.rserver.node;

import fr.proline.studio.rserver.data.AbstractRData;
import fr.proline.studio.utils.IconManager;
import javax.swing.ImageIcon;

/**
 * R Node
 * @author JM235353
 */
public class RGraphicNode extends RNode {
        private String m_RVariable = null;
    
    public RGraphicNode(AbstractRData data) {
        super(RNode.NodeTypes.GRAPHIC, data);
    }

    public void setRVariable(String RVariable) {
        m_RVariable = RVariable;
    }
    
    public String getRVariable() {
        return m_RVariable;
    }
    
    
    @Override
    public ImageIcon getIcon() {
        return getIcon(IconManager.IconType.WAVE);
    }
}
