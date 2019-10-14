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


import fr.proline.studio.utils.PropertiesProviderInterface;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;


/**
 * This node class wraps a PropertiesProviderInterface, so we can display properties
 * with a PropertiesSheet (which accepts only netbean Nodes )
 * @author JM235353
 */
public class PropertiesWrapperNode extends AbstractNode {

    private PropertiesProviderInterface m_propertiesProvider;
    
    public PropertiesWrapperNode(PropertiesProviderInterface propertiesProvider) {
        super(new Children.Array());
        
        m_propertiesProvider = propertiesProvider;

    }
    
    @Override
    protected Sheet createSheet() {
        return m_propertiesProvider.createSheet();
    }
   

    
}
