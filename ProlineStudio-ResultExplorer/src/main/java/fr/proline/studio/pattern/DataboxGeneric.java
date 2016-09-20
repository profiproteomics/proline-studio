package fr.proline.studio.pattern;

import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.ExtraDataType;
import fr.proline.studio.comparedata.GlobalTabelModelProviderInterface;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.rsmexplorer.gui.GenericPanel;
import fr.proline.studio.table.GlobalTableModelInterface;
import java.util.ArrayList;

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
        inParameter.addParameter(CompareDataInterface.class, false);
        registerInParameter(inParameter);
        
        
        // Register possible out parameters
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(CompareDataInterface.class, false);
        registerOutParameter(outParameter);

        
        
    }
    
    @Override
    public void createPanel() {
        GenericPanel p = new GenericPanel(m_removeStripAndSort);
        p.setName(m_typeName);
        p.setDataBox(this);
        m_panel = p;
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
                     outParameter.addParameter(c, false);
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
            dataInterface = (GlobalTableModelInterface) m_previousDataBox.getData(false, GlobalTableModelInterface.class);
        }

        ((GenericPanel) m_panel).setData(dataInterface);

    }
    
        @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType != null) {
            if (parameterType.equals(CompareDataInterface.class)) {
                return ((GlobalTabelModelProviderInterface) m_panel).getGlobalTableModelInterface();
            }
            if (parameterType.equals(CrossSelectionInterface.class)) {
                return ((GlobalTabelModelProviderInterface)m_panel).getCrossSelectionInterface();
            }
            ArrayList<ExtraDataType> extraDataTypeList = m_entryModel.getExtraDataTypes();
             if (extraDataTypeList != null) {
                 for (ExtraDataType extraDataType : extraDataTypeList) {
                     if (extraDataType.getTypeClass().equals(parameterType)) {
                         return ((GenericPanel) m_panel).getValue(parameterType, extraDataType.isList());
                     }
                 }
             }
        }
        return super.getData(getArray, parameterType);
    }
    
}
