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
package fr.proline.studio.tabs;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author MB243701
 */
public class TestPanel extends JPanel implements IWrappedPanel{

    private Long nb; 
    
    private Component tabHeaderComponent;
    
    public TestPanel(Long nb) {
        this.nb = nb;
        init();
    }
    
    private void init(){
        setLayout(new BorderLayout());
        this.add(new JLabel("label "+nb), BorderLayout.CENTER);
    }

    
    @Override
    public String getTitle() {
        return "Panel "+this.nb;
    }


    @Override
    public JPanel getComponent() {
        return this;
    }
    
    @Override
    public Long getId(){
        return this.nb;
    }
    
    @Override
    public void setTabHeaderComponent(Component c){
        this.tabHeaderComponent = c;
    }
    
    @Override
    public Component getTabHeaderComponent(){
        return tabHeaderComponent;
    }
    
}
