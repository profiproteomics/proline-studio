package fr.proline.studio.pattern;


import fr.proline.core.orm.msi.PeptideMatch;
import fr.proline.core.orm.msi.PeptideSet;
import fr.proline.core.orm.msi.ProteinMatch;
import fr.proline.studio.rsmexplorer.gui.RsetPeptideSpectrumPanel;
import fr.proline.studio.rsmexplorer.gui.RsmProteinAndPeptideSequencePanel;

/**
 *
 * @author JM235353
 */
public class DataBoxRsetPeptideSpectrum extends AbstractDataBox {

    public DataBoxRsetPeptideSpectrum() {

        // Name of this databox
        name = "Spectrum";
        
        // Register in parameters
        registerInParameterType(null, PeptideMatch.class);

        // Register possible out parameters
        // none
    }

    @Override
    public void createPanel() {
        RsetPeptideSpectrumPanel p = new RsetPeptideSpectrumPanel();
        p.setName(name);
        p.setDataBox(this);
        panel = p;
    }

    @Override
    public void dataChanged(AbstractDataBox srcDataBox) {
        PeptideMatch peptideMatch = (PeptideMatch) srcDataBox.getData(null, PeptideMatch.class);

        ((RsetPeptideSpectrumPanel) panel).setData(peptideMatch);
    }
}
