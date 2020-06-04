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


import fr.proline.studio.table.TableInfo;
import fr.proline.studio.rsmexplorer.gui.calc.DataAnalyzerPanel;
import fr.proline.studio.rsmexplorer.gui.calc.ProcessEngineInfo;

/**
 * Databox corresponding to the Data Analyzer ( Tree of functions and data + graph zone)
 * 
 * @author JM235353
 */
public class DataboxDataAnalyzer extends AbstractDataBox {

    public DataboxDataAnalyzer() {
        super(DataboxType.DataboxDataAnalyzer, DataboxStyle.STYLE_UNKNOWN);

        // Name of this databox
        m_typeName = "Data Analyzer";


    }

    @Override
    public void createPanel() {
        DataAnalyzerPanel p = new DataAnalyzerPanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);

    }

    @Override
    public void dataChanged() {
    }

    @Override
    public void setEntryData(Object data) {
        if (data instanceof TableInfo) {
            ((DataAnalyzerPanel) getDataBoxPanelInterface()).addTableInfoToGraph((TableInfo) data);
        }
    }

    @Override
    public Object getDataImpl(Class parameterType, ParameterSubtypeEnum parameterSubtype) {
        if (parameterType != null) {
            
            if (parameterSubtype == ParameterSubtypeEnum.SINGLE_DATA) {
                if (parameterType.equals(ProcessEngineInfo.class)) {
                    return ((DataAnalyzerPanel) getDataBoxPanelInterface()).getProcessEngineInfoToDisplay();
                }
            }

        }
        return super.getDataImpl(parameterType, parameterSubtype);
    }

}
