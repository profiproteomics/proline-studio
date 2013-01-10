package fr.proline.studio.rsmexplorer.node;

import fr.proline.core.orm.uds.Project;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.ProjectData;
import fr.proline.studio.utils.IconManager;
import java.util.Enumeration;
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;

/**
 * Node for the opened Project
 * @author JM235353
 */
public class RSMProjectNode extends RSMNode {

    boolean isChanging = false;
    
    public RSMProjectNode(AbstractData data) {
        super(RSMNode.NodeTypes.PROJECT, data);
    }

    public void setIsChanging(boolean isChanging) {
        this.isChanging = isChanging;
    }
    
    public Project getProject() {
        return ((ProjectData) getData()).getProject();
    }
    
    @Override
    public ImageIcon getIcon() {
        if (isChanging) {
            return IconManager.getIcon(IconManager.IconType.HOUR_GLASS);
        }
        
        return IconManager.getIcon(IconManager.IconType.PROJECT);
    }
    /*
    @Override
    public Image getOpenedIcon(int i) {
        return icon;
    }

*/


}
