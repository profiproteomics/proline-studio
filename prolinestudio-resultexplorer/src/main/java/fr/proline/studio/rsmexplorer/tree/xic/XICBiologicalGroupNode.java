/* 
 * Copyright (C) 2019
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
package fr.proline.studio.rsmexplorer.tree.xic;

import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.utils.IconManager;
import javax.swing.ImageIcon;

/**
 * Biologic Group node used in XIC Design Tree
 * @author JM235353
 */
public class XICBiologicalGroupNode extends AbstractNode {

    public XICBiologicalGroupNode(AbstractData data) {
        super(AbstractNode.NodeTypes.BIOLOGICAL_GROUP, data);
    }
    
    @Override
    public ImageIcon getIcon(boolean expanded) {
        return getIcon(IconManager.IconType.BIOLOGICAL_GROUP);
    }

    @Override
    public AbstractNode copyNode() {
        return null;
    }

    @Override
    public boolean canBeDeleted() {
        return true;
    }
    
}
