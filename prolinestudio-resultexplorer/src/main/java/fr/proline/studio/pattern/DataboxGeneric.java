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

import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.rsmexplorer.gui.GenericPanel;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import java.util.ArrayList;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;

/**
 *
 * @author JM235353
 */
public class DataboxGeneric extends AbstractDataBox {

    private GlobalTableModelInterface m_entryModel = null;
    
    private final boolean m_removeStripAndSort;
    
    public DataboxGeneric(String dataName, String typeName, boolean removeStripAndSort) {
        super(DataboxType.DataboxCompareResult, DataboxStyle.STYLE_UNKNOWN);
        
        // Name of this databox
        m_dataName = dataName;
        m_typeName = typeName;
        m_description = typeName;
        m_removeStripAndSort = removeStripAndSort;
        
        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(ExtendedTableModelInterface.class);
        registerInParameter(inParameter);
        
        
        // Register possible out parameters
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(ExtendedTableModelInterface.class);
        registerOutParameter(outParameter);

        
        
    }
    
    @Override
    public void createPanel() {
        GenericPanel p = new GenericPanel(m_removeStripAndSort);
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }
    
    @Override
    public void setEntryData(Object data) {
        if (data instanceof GlobalTableModelInterface) {
            m_entryModel = (GlobalTableModelInterface) data;
            
            
             ArrayList<ExtraDataType> extraDataTypeList = m_entryModel.getExtraDataTypes();
             if (extraDataTypeList != null) {
                 for(ExtraDataType extraDataType : extraDataTypeList) {
                     Class c = extraDataType.getTypeClass();

                     GroupParameter outParameter = new GroupParameter();
                     outParameter.addParameter(c);
                     registerOutParameter(outParameter);
                 }
             }
            
        }
         dataChanged();
    }

    @Override
    public void dataChanged() {
        GlobalTableModelInterface dataInterface = m_entryModel;
        if (dataInterface == null) {
            dataInterface = (GlobalTableModelInterface) m_previousDataBox.getData(GlobalTableModelInterface.class);
        }

        ((GenericPanel) getDataBoxPanelInterface()).setData(dataInterface);

    }
    
    @Override
    public Object getData(Class parameterType, ParameterSubtypeEnum parameterSubtype) {
        if (parameterType != null) {
            
            if (parameterSubtype == ParameterSubtypeEnum.SINGLE_DATA) {
                if (parameterType.equals(ExtendedTableModelInterface.class)) {
                    return ((GlobalTabelModelProviderInterface) getDataBoxPanelInterface()).getGlobalTableModelInterface();
                }
                if (parameterType.equals(CrossSelectionInterface.class)) {
                    return ((GlobalTabelModelProviderInterface) getDataBoxPanelInterface()).getCrossSelectionInterface();
                }
                ArrayList<ExtraDataType> extraDataTypeList = m_entryModel.getExtraDataTypes();
                if (extraDataTypeList != null) {
                    for (ExtraDataType extraDataType : extraDataTypeList) {
                        if (extraDataType.getTypeClass().equals(parameterType)) {
                            return ((GenericPanel) getDataBoxPanelInterface()).getValue(parameterType, extraDataType.isList());
                        }
                    }
                }
            }
        }
        return super.getData(parameterType, parameterSubtype);
    }
    
}
