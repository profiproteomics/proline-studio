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
package fr.proline.studio.pattern;

import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.rsmexplorer.gui.GraphicsPanel;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;

/**
 *
 * @author JM235353
 */
public class DataboxGraphics extends AbstractDataBox  {

    private ExtendedTableModelInterface m_values = null;

    private boolean m_defaultLocked = false;
    
    public DataboxGraphics() {
       this(true);
    }
    
    public DataboxGraphics(boolean defaultLocked) {
         super(DataboxType.DataboxGraphics, DataboxStyle.STYLE_UNKNOWN);

         m_defaultLocked = defaultLocked;
         
        // Name of this databox
        m_typeName = "Customisable Graphical Display";
        m_description = "Plots data as Histogram / Scatter Plot / Venn Diagram / Parallel Coordinates";

        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(ExtendedTableModelInterface.class);
        registerInParameter(inParameter);
        
    }
    
    protected void setDefaultLocked(boolean defaultLocked){
        m_defaultLocked = defaultLocked;
    }
    
    protected boolean isDefaultLocked(){
        return m_defaultLocked;
    }
    
    @Override
    public void createPanel() {
        GraphicsPanel p = new GraphicsPanel(m_defaultLocked);
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }

    @Override
    public void dataChanged() {
        final ExtendedTableModelInterface values = (m_values!=null) ? m_values : (ExtendedTableModelInterface) m_previousDataBox.getData(ExtendedTableModelInterface.class);
        final CrossSelectionInterface crossSelectionInterface = (m_values!=null) ? null : (CrossSelectionInterface) m_previousDataBox.getData(CrossSelectionInterface.class);
        ((GraphicsPanel)getDataBoxPanelInterface()).setData(values, crossSelectionInterface);
    }
    
    @Override
    public void setEntryData(Object data) {
        m_values = (ExtendedTableModelInterface) data;
        dataChanged();
    }
    
}
