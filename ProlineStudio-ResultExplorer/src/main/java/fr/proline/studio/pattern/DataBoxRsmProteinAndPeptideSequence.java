package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.PeptideInstance;
import fr.proline.core.orm.msi.PeptideSet;
import fr.proline.core.orm.msi.ProteinMatch;
import fr.proline.core.orm.msi.ResultSummary;
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
        inParameter.addParameter(PeptideInstance.class, false); 
        inParameter.addParameter(ResultSummary.class, false); // needs a ProteinMatch, a PeptideInstance and a ResultSummary
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
        ResultSummary resultSummary = (ResultSummary) srcDataBox.getData(null, ResultSummary.class);
       
        
        if ((proteinMatch == null) || (selectedPeptide == null) || (resultSummary == null)) {
            ((RsmProteinAndPeptideSequencePanel) panel).setData(null, null, null);
            return;
        }
        
        ProteinMatch.TransientData data = proteinMatch.getTransientData();

        PeptideSet peptideSet = (data!=null) ? data.getPeptideSet(resultSummary.getId()) : null;
        if (peptideSet == null) {
            ((RsmProteinAndPeptideSequencePanel) panel).setData(null, null, null);
            return;
        }
        
        PeptideInstance[] peptideInstances = peptideSet.getTransientPeptideInstances();
        ((RsmProteinAndPeptideSequencePanel) panel).setData(proteinMatch, selectedPeptide, peptideInstances);
    }
}
