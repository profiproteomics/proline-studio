package fr.proline.studio.pattern;


import fr.proline.core.orm.msi.PeptideSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.studio.rsmexplorer.gui.RsmProteinAndPeptideSequencePanel;

/**
 * Databox : Protein Sequence + inner peptide sequence when peptide are given by
 * a previous databox.
 * @author JM235353
 */
public class DataBoxRsmProteinAndPeptideSequence extends AbstractDataBox {

    public DataBoxRsmProteinAndPeptideSequence() {
        super(DataboxType.DataBoxRsmProteinAndPeptideSequence);

        // Name of this databox
        m_name = "Protein Sequence";
        m_description = "Protein Sequence and its Peptides Sequences";
        
        // Register in parameters
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(DProteinMatch.class, false);
        inParameter.addParameter(DPeptideInstance.class, false, false /* parameter is not compulsory */); 
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
        DProteinMatch proteinMatch = (DProteinMatch) m_previousDataBox.getData(false, DProteinMatch.class);
        DPeptideInstance selectedPeptide = (DPeptideInstance) m_previousDataBox.getData(false, DPeptideInstance.class);
        ResultSummary resultSummary = (ResultSummary) m_previousDataBox.getData(false, ResultSummary.class);
       
        
        if ((proteinMatch == null) || (resultSummary == null)) {
            ((RsmProteinAndPeptideSequencePanel) m_panel).setData(null, null, null, null);
            return;
        }

        PeptideSet peptideSet = proteinMatch.getPeptideSet(resultSummary.getId());
        if (peptideSet == null) {
            ((RsmProteinAndPeptideSequencePanel) m_panel).setData(null, null, null, null);
            return;
        }
        
        DPeptideInstance[] peptideInstances = peptideSet.getTransientDPeptideInstances();
        
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
        
        
        ((RsmProteinAndPeptideSequencePanel) m_panel).setData(resultSummary.getId() , proteinMatch, selectedPeptide, peptideInstances);
    }
}
