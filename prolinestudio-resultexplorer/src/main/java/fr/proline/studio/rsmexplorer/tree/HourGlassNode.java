/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.rsmexplorer.tree;


import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.utils.IconManager;
import javax.swing.ImageIcon;
import org.openide.nodes.Sheet;

/**
 * Node used for a part of the tree which is not already loaded
 * @author JM235353
 */
public class HourGlassNode extends AbstractNode {
    
 
    
    public HourGlassNode(AbstractData data) {
        super(AbstractNode.NodeTypes.HOUR_GLASS, data);
    }
 

 
    
    @Override
    public String toString() {
        return "Loading...";
    }
    
    @Override
    public ImageIcon getIcon(boolean expanded) {
        return getIcon(IconManager.IconType.HOUR_GLASS);
    }

    @Override
    public void loadDataForProperties(Runnable callback) {
        // nothing to do
        callback.run();
    }
    
    @Override
    public Sheet createSheet() {
        return null; // should never be called
    }

    @Override
    public AbstractNode copyNode() {
        AbstractNode copy = new HourGlassNode(getData());
        return copy;
    }
    
    public boolean canBeDeleted() {
        return true;
    }
    
}
