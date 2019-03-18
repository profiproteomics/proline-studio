package fr.proline.studio.pattern;

import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.rsmexplorer.gui.MultiGraphicsPanel;
import java.util.List;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;

/**
 *
 * @author JM235353
 */
public class DataboxMultiGraphics extends AbstractDataBox {

    private List<ExtendedTableModelInterface> m_valuesList = null;

    private boolean m_defaultLocked = false;
    private boolean m_canChooseColor = false;

    public DataboxMultiGraphics() {
        this(false, false);
    }

    public DataboxMultiGraphics(boolean defaultLocked, boolean canChooseColor) {
        super(DataboxType.DataboxMultiGraphics, DataboxStyle.STYLE_UNKNOWN);

        m_defaultLocked = defaultLocked;
        m_canChooseColor = canChooseColor;

        // Name of this databox
        m_typeName = "Graphic";
        m_description = "Linear Plot";

        // Register Possible in parameters
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(ExtendedTableModelInterface.class, true);
        registerInParameter(inParameter);

        inParameter.addParameter(CrossSelectionInterface.class, true);
        registerInParameter(inParameter);
        

    }

    @Override
    public void createPanel() {
        MultiGraphicsPanel p = new MultiGraphicsPanel(m_defaultLocked, m_canChooseColor, false);
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }

    /**
     * in the case of DisplayMapAlignement action, m_previousDataBox is
     * DataboxMapAlignment
     */
    @Override
    public void dataChanged() {
        final List<ExtendedTableModelInterface> valuesL = (List<ExtendedTableModelInterface>) m_previousDataBox.getData(false, ExtendedTableModelInterface.class, true);
        final List<CrossSelectionInterface> crossSelectionInterfaceL =  (List<CrossSelectionInterface>) m_previousDataBox.getData(false, CrossSelectionInterface.class, true);
        if (valuesL != null)
            ((MultiGraphicsPanel)getDataBoxPanelInterface()).setData(valuesL, crossSelectionInterfaceL);
    }

    @Override
    public void setEntryData(Object data) {
        m_valuesList = (List<ExtendedTableModelInterface>) data;
        dataChanged();
    }

}
