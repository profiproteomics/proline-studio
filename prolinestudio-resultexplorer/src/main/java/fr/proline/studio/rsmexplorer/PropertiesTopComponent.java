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
package fr.proline.studio.rsmexplorer;

import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.rsmexplorer.tree.PropertiesWrapperNode;
import fr.proline.studio.utils.PropertiesProviderInterface;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.windows.TopComponent;

/**
 * Top Component to display properties
 * @author JM235353
 */
public class PropertiesTopComponent extends TopComponent {
 
    private HourglassPanel m_hourglassPanel = null;
    private PropertySheet m_propertySheet = null;
    
    public PropertiesTopComponent(String name) {
        
        setName(name);
        
        m_propertySheet = new PropertySheet();
        
        m_hourglassPanel = new HourglassPanel();
        m_hourglassPanel.setLoading(1);
        m_hourglassPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.weighty = 1.0;
        m_hourglassPanel.add(m_propertySheet, c);
        
        
        
        setLayout (new BorderLayout ());
        add(m_hourglassPanel, BorderLayout.CENTER);
    }
    
    public void setProperties(PropertiesProviderInterface[] propertiesProviders) {
        
        int nbProp = propertiesProviders.length;
        
        PropertiesWrapperNode[] wrappedNodes = new PropertiesWrapperNode[nbProp];
        for (int i=0;i<nbProp;i++) {
            wrappedNodes[i] = new PropertiesWrapperNode(propertiesProviders[i]);
        }
        
        m_propertySheet.setNodes(wrappedNodes);
        
        m_hourglassPanel.setLoaded(1);
    }

    
}
