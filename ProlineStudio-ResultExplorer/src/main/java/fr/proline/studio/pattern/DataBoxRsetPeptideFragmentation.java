package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.rsmexplorer.gui.spectrum.RsetPeptideFragmentationTablePanel;
import fr.proline.studio.rsmexplorer.gui.spectrum.PeptideFragmentationData;

/**
 *
 * @author AW
 */
public class DataBoxRsetPeptideFragmentation extends AbstractDataBox {

    private DPeptideMatch m_previousPeptideMatch = null;

    public DataBoxRsetPeptideFragmentation() {
        super(DataboxType.DataBoxRsetPeptideFragmentation);
        
        // Name of this databox
        m_name = "Fragmentation Table";
        m_description = "Fragmentation table of a Peptide";

        // Register in parameters
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(DPeptideMatch.class, false);
        inParameter.addParameter(PeptideFragmentationData.class, false);
        registerInParameter(inParameter);


    }

    @Override
    public void createPanel() {
        RsetPeptideFragmentationTablePanel p = new RsetPeptideFragmentationTablePanel();
        p.setName(m_name);
        p.setDataBox(this);
        m_panel = p;
    }

    @Override
    public void dataChanged() {
        final PeptideFragmentationData fragmentationData = (PeptideFragmentationData) m_previousDataBox.getData(false, PeptideFragmentationData.class);
        final DPeptideMatch peptideMatch = (fragmentationData!=null) ? fragmentationData.getPeptideMatch() : null;

        if ((m_previousPeptideMatch == peptideMatch) && (fragmentationData == null)) {
            return;
        }
        m_previousPeptideMatch = peptideMatch;

        if (peptideMatch == null) {
            ((RsetPeptideFragmentationTablePanel) m_panel).setData(null, null);
            return;
        }

        ((RsetPeptideFragmentationTablePanel) m_panel).setData(peptideMatch, fragmentationData);


    }
}
