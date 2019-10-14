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

import fr.proline.studio.rsmexplorer.tree.quantitation.QuantitationProjectNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdProjectIdentificationNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdAllImportedNode;
import fr.proline.studio.dam.data.AbstractData;



/**
 * Class used to create children of a node asynchronously
 * @author JM235353
 */
public class ChildFactory  {

    public static AbstractNode createNode(AbstractData key) {
        
        // result Node
        AbstractNode result = null;
        
                // Creation of the correct Node type
        AbstractData.DataTypes type = key.getDataType();
        switch (type) {
            case MAIN:
                result = new TreeParentNode(key);
                break;
            case PROJECT_IDENTIFICATION:
                result = new IdProjectIdentificationNode(key);
                break;
            case PROJECT_QUANTITATION:
                result = new QuantitationProjectNode(key);
                break;
            case DATA_SET:
                result = new DataSetNode(key);
                break;
            case ALL_IMPORTED:
                result = new IdAllImportedNode(key);
                break;
        }
        
        if (key.hasChildren()) {
            result.add(new HourGlassNode(null));
        }
        
        return result;
    }
    
    
    
    
    
 
    
}
