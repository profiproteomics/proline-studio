package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.PeptideInstance;
import fr.proline.core.orm.msi.PeptideSet;
import fr.proline.core.orm.msi.ProteinMatch;
import fr.proline.studio.rsmexplorer.gui.RsmProteinAndPeptideSequencePanel;

/**
 *
 * @author JM235353
 */
public class DataBoxRsmProteinAndPeptideSequence extends AbstractDataBox {

    public DataBoxRsmProteinAndPeptideSequence() {

        // Name of this databox
        name = "Protein Sequence";
        
        // Register in parameters
        DataParameter inParameter = new DataParameter();
        inParameter.addParameter(ProteinMatch.class, false);
        inParameter.addParameter(PeptideInstance.class, false); // needs a ProteinMatch and a PeptideInstance
        registerInParameter(inParameter);


        // Register possible out parameters
        // none
    }

    @Override
    public void createPanel() {
        RsmProteinAndPeptideSequencePanel p = new RsmProteinAndPeptideSequencePanel();
        p.setName(name);
        p.setDataBox(this);
        panel = p;
    }

    @Override
    public void dataChanged(AbstractDataBox srcDataBox, Class dataType) {
        ProteinMatch proteinMatch = (ProteinMatch) srcDataBox.getData(null, ProteinMatch.class);
        PeptideInstance selectedPeptide = (PeptideInstance) srcDataBox.getData(null, PeptideInstance.class);
        
        if ((proteinMatch == null) || (selectedPeptide == null)) {
            ((RsmProteinAndPeptideSequencePanel) panel).setData(null, null, null);
            return;
        }

        PeptideSet peptideSet = proteinMatch.getTransientPeptideSet();
        if (peptideSet == null) {
            ((RsmProteinAndPeptideSequencePanel) panel).setData(null, null, null);
            return;
        }
        
        PeptideInstance[] peptideInstances = peptideSet.getTransientPeptideInstances();
        ((RsmProteinAndPeptideSequencePanel) panel).setData(proteinMatch, selectedPeptide, peptideInstances);
    }
}
