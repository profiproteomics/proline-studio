package fr.proline.studio.rserver.node;

import fr.proline.studio.rserver.data.AbstractRData;
import fr.proline.studio.rserver.data.RExpression;
import fr.proline.studio.utils.IconManager;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author JM235353
 */
public abstract class RNode extends DefaultMutableTreeNode {
    
    public enum NodeTypes {

        TREE_PARENT,
        MSN_SET,
        GRAPHIC
    }
    
    protected NodeTypes m_type;

    protected boolean m_isChanging = false;
    
    public RNode(NodeTypes type, AbstractRData data) {
        super(data);
        m_type = type;
    }

    public NodeTypes getType() {
        return m_type;
    }
    
    public AbstractRData getData() {
        return (AbstractRData) getUserObject();
    }
    
    @Override
    public String toString() {
        AbstractRData data = getData();
        if (data == null) {
            return "";
        }

        return data.getName();
    }
    
    public abstract ImageIcon getIcon();

    public ImageIcon getIcon(IconManager.IconType iconType) {
        if (m_isChanging) {
            return IconManager.getIconWithHourGlass(iconType);
        }
        return IconManager.getIcon(iconType);
    }
    
    public void setIsChanging(boolean isChanging) {
        m_isChanging = isChanging;
    }

    public boolean isChanging() {
        return m_isChanging;
    }

    public String getToolTipText() {
        RExpression expression = getData().getRExpression();
        if (expression == null) {
            return null;
        }
        return expression.getRExpression();
    }
    
    public String getLongDisplayName() {
        return getData().getLongDisplayName();
    }
    
    public void setLongDisplayName(String longDisplayName) {
        getData().setLongDisplayName(longDisplayName);
    }
    
    public RExpression getRExpression() {
        return getData().getRExpression();
    }
    
    public void setRExpression(RExpression e) {
        getData().setRExpression(e);
    }
}
