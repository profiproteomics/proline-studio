package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.PeptideInstance;
import fr.proline.core.orm.msi.PeptideSet;
import fr.proline.core.orm.msi.ProteinMatch;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.rsmexplorer.gui.RsmProteinAndPeptideSequencePanel;

/**
 * Databox : Protein Sequence + inner peptide sequence when peptide are given by
 * a previous databox.
 * @author JM235353
 */
public class DataBoxRsmProteinAndPeptideSequence extends AbstractDataBox {

    public DataBoxRsmProteinAndPeptideSequence() {

        // Name of this databox
        m_name = "Protein Sequence";
        m_description = "Protein Sequence and its Peptides Sequences";
        
        // Register in parameters
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(ProteinMatch.class, false);
        inParameter.addParameter(PeptideInstance.class, false, false /* parameter is not compulsory */); 
        inParameter.addParameter(ResultSummary.class, false); // needs a ProteinMatch and a ResultSummary (PeptideInstance is optionnal)
        registerInParameter(inParameter);


        // Register possible out parameters
        // none
    }

    @Override
    public void createPanel() {
        RsmProteinAndPeptideSequencePanel p = new RsmProteinAndPeptideSequencePanel();
        p.setName(m_name);
        p.setDataBox(this);
        m_panel = p;
    }

    @Override
    public void dataChanged() {
        ProteinMatch proteinMatch = (ProteinMatch) m_previousDataBox.getData(false, ProteinMatch.class);
        PeptideInstance selectedPeptide = (PeptideInstance) m_previousDataBox.getData(false, PeptideInstance.class);
        ResultSummary resultSummary = (ResultSummary) m_previousDataBox.getData(false, ResultSummary.class);
       
        
        if ((proteinMatch == null) || (resultSummary == null)) {
            ((RsmProteinAndPeptideSequencePanel) m_panel).setData(null, null, null);
            return;
        }
        
        ProteinMatch.TransientData data = proteinMatch.getTransientData();

        PeptideSet peptideSet = (data!=null) ? data.getPeptideSet(resultSummary.getId()) : null;
        if (peptideSet == null) {
            ((RsmProteinAndPeptideSequencePanel) m_panel).setData(null, null, null);
            return;
        }
        
        PeptideInstance[] peptideInstances = peptideSet.getTransientPeptideInstances();
        
        // check that the selectedPeptide found in previous databox is really a peptide of the proteinMatch (they
        // can come from different databoxes)
        if (peptideInstances == null) {
            selectedPeptide = null;
        } else if (selectedPeptide!=null) {
        
            boolean foundPeptide = false;
            for (int i=0;i<peptideInstances.length;i++) {
                if (peptideInstances[i].getId() == selectedPeptide.getId()) {
                    foundPeptide = true;
                    break;
                }
            }
            if (!foundPeptide) {
                selectedPeptide = null;
            }
        } 
        
        
        ((RsmProteinAndPeptideSequencePanel) m_panel).setData(proteinMatch, selectedPeptide, peptideInstances);
    }
}
