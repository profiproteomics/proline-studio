/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 11 mars 2019
 */
package fr.proline.studio.pattern.xic;

import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.GroupParameter;
import fr.proline.studio.rsmexplorer.gui.xic.XicAbundanceGraphicPanel;
import fr.proline.studio.rsmexplorer.gui.xic.XicAbundanceProteinTableModel;
import java.util.List;

/**
 *
 * @author Karine XUE
 */
public class DataboxXicPeptideProteinGraphic extends AbstractDataBox {

    private List<ExtendedTableModelInterface> m_valuesList = null;
    private boolean m_defaultLocked = false;
    private boolean m_canChooseColor = false;

    public DataboxXicPeptideProteinGraphic() {
        super(DataboxType.DataboxXicAbundanceGraphic, DataboxStyle.STYLE_UNKNOWN);

        // Name of this databox
        m_typeName = "Graphic peptide/protein abundance";
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
        XicAbundanceGraphicPanel p = new XicAbundanceGraphicPanel(m_defaultLocked, m_canChooseColor);
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }

    @Override
    public void dataChanged() {
        final List<ExtendedTableModelInterface> valuesL = (List<ExtendedTableModelInterface>) m_previousDataBox.getData(false, ExtendedTableModelInterface.class, true);
        final List<CrossSelectionInterface> crossSelectionInterfaceL = (List<CrossSelectionInterface>) m_previousDataBox.getData(false, CrossSelectionInterface.class, true);
        XicAbundanceProteinTableModel proteinAbundance = (XicAbundanceProteinTableModel) m_previousDataBox.getData(false, XicAbundanceProteinTableModel.class, false);
        ((XicAbundanceGraphicPanel) getDataBoxPanelInterface()).setData(valuesL, crossSelectionInterfaceL, proteinAbundance);
    }

    @Override
    public void setEntryData(Object data) {
        m_valuesList = (List<ExtendedTableModelInterface>) data;
        dataChanged();
    }

}
