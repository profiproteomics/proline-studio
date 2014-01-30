package fr.proline.studio.rserver.node;

import fr.proline.studio.rserver.command.AbstractCommand;
import fr.proline.studio.rserver.command.GenericCommand;
import fr.proline.studio.rserver.command.RVar;
import fr.proline.studio.rserver.data.AbstractRData;
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
        return getLongDisplayName();
    }
    
    public String getLongDisplayName() {
        return getData().getLongDisplayName();
    }
    
    public void setLongDisplayName(String longDisplayName) {
        getData().setLongDisplayName(longDisplayName);
    }

    public void setCommand(AbstractCommand c) {
        getData().setCommand(c);
    }

    public AbstractCommand getCommand() {
        return getData().getCommand();
    }

    public void setVar(RVar v) {
        getData().setVar(v);
    }

    public RVar getVar() {
        return getData().getVar();
    }
}
