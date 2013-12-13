/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.node;

import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.utils.IconManager;
import javax.swing.ImageIcon;
import org.openide.nodes.Sheet;



/**
 *
 * @author JM235353
 */
public class RSMAllImportedNode extends RSMNode {
    
    public RSMAllImportedNode(AbstractData data) {
        super(NodeTypes.DATA_ALL_IMPORTED, data);
    }
    
    @Override
    public ImageIcon getIcon() {
        return getIcon(IconManager.IconType.ALL_IMPORTED);
    }

    @Override
    public Sheet createSheet() {
       return null;
    }

    @Override
    public void loadDataForProperties(Runnable callback) {
    }
    
}
