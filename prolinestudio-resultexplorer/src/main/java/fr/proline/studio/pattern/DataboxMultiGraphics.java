package fr.proline.studio.pattern;

import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.rsmexplorer.gui.MultiGraphicsPanel;
import java.util.List;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.extendedtablemodel.SecondAxisTableModelInterface;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author JM235353
 */
public class DataboxMultiGraphics extends AbstractDataBox {

    private static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private List<ExtendedTableModelInterface> m_plotValues = null;
    private SecondAxisTableModelInterface m_plotSecondAxisValues = null;
    private List<CrossSelectionInterface> m_crossSelectionValues = null;

    private boolean m_defaultLocked = false;
    private boolean m_canChooseColor = false;
    private boolean m_displayDoubleYAxis = false;
    
    public DataboxMultiGraphics() {
        this(false, false, false);
    }
    
    public DataboxMultiGraphics(boolean defaultLocked, boolean canChooseColor) {
         this(defaultLocked, canChooseColor, false);
    }

    public DataboxMultiGraphics(boolean defaultLocked, boolean canChooseColor, boolean displayDoubleYAxis) {
        super((displayDoubleYAxis ? DataboxType.DataboxMultiGraphicsDoubleYAxis : DataboxType.DataboxMultiGraphics), DataboxStyle.STYLE_UNKNOWN);

        m_defaultLocked = defaultLocked;
        m_canChooseColor = canChooseColor;
        m_displayDoubleYAxis = displayDoubleYAxis;
        
        // Name of this databox
        if(m_displayDoubleYAxis) {
            m_typeName = "Linear Plot (two axis)";
            m_description = "Display two sets of data as linear plot using 2 axis";
        } else {
            m_typeName = "Linear Plot";
            m_description = "Display data as linear plot";            
        }

        // Register Possible in parameters
        if(! m_displayDoubleYAxis) {
            GroupParameter inParameter = new GroupParameter();
            inParameter.addParameter(ExtendedTableModelInterface.class, true);
            inParameter.addParameter(CrossSelectionInterface.class, true);
            registerInParameter(inParameter);
        } else {
            GroupParameter inParameter = new GroupParameter();
            inParameter.addParameter(ExtendedTableModelInterface.class, true);
            inParameter.addParameter(CrossSelectionInterface.class, true);
            inParameter.addParameter(SecondAxisTableModelInterface.class, true);
            registerInParameter(inParameter);
        }
        
    }
    
    protected boolean isDoubleYAxis(){
        return m_displayDoubleYAxis;
    }

    @Override
    public void createPanel() {
        MultiGraphicsPanel p = new MultiGraphicsPanel(m_defaultLocked, m_canChooseColor, m_displayDoubleYAxis);
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }

    @Override
    public void dataChanged() {      
        final List<ExtendedTableModelInterface> dataModelInterfaceSet1 = (List<ExtendedTableModelInterface>) m_previousDataBox.getData(false, ExtendedTableModelInterface.class, true);
        final List<CrossSelectionInterface> crossSelectionInterfaceL =  (List<CrossSelectionInterface>) m_previousDataBox.getData(false, CrossSelectionInterface.class, true);
        SecondAxisTableModelInterface dataModelInterfaceSet2 = m_displayDoubleYAxis ? (SecondAxisTableModelInterface) m_previousDataBox.getData(false, SecondAxisTableModelInterface.class, true) : null;
        
        boolean valueUnchanged  = Objects.equals(dataModelInterfaceSet1, m_plotValues) && Objects.equals(crossSelectionInterfaceL,m_crossSelectionValues) && Objects.equals(dataModelInterfaceSet2,m_plotSecondAxisValues);
        if(valueUnchanged)
            return;
        m_plotValues = dataModelInterfaceSet1;
        m_crossSelectionValues = crossSelectionInterfaceL;
        m_plotSecondAxisValues = dataModelInterfaceSet2;
        if (m_plotValues != null)
            ((MultiGraphicsPanel)getDataBoxPanelInterface()).setData(m_plotValues, m_crossSelectionValues,m_plotSecondAxisValues);
    }

    @Override
    public void setEntryData(Object data) {
        m_plotValues = (List<ExtendedTableModelInterface>) data;
        ((MultiGraphicsPanel)getDataBoxPanelInterface()).setData(m_plotValues, null);
    }

}
