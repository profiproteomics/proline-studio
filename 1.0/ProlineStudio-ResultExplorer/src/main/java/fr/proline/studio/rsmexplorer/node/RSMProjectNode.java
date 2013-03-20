package fr.proline.studio.rsmexplorer.node;

import fr.proline.core.orm.uds.Project;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.ProjectData;
import fr.proline.studio.utils.IconManager;
import javax.swing.ImageIcon;

/**
 * Node for the opened Projects
 * @author JM235353
 */
public class RSMProjectNode extends RSMNode {

    
    public RSMProjectNode(AbstractData data) {
        super(RSMNode.NodeTypes.PROJECT, data);
    }


    public Project getProject() {
        return ((ProjectData) getData()).getProject();
    }
    
    @Override
    public ImageIcon getIcon() {
        return getIcon(IconManager.IconType.PROJECT);
    }



}
