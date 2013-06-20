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
        description = "Protein Sequence and its Peptides Sequences";
        
        // Register in parameters
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(ProteinMatch.class, false);
        //inParameter.addParameter(PeptideInstance.class, false); 
        inParameter.addParameter(ResultSummary.class, false); // needs a ProteinMatch and a ResultSummary (PeptideInstance is optionnal)
        registerInParameter(inParameter);


        // Register possible out parameters
        // none
    }

    @Override
    public void createPanel() {
        RsmProteinAndPeptideSequencePanel p = new RsmProteinAndPeptideSequencePanel();
        p.setName(name);
        p.setDataBox(this);
        m_panel = p;
    }

    @Override
    public void dataChanged(Class dataType) {
        ProteinMatch proteinMatch = (ProteinMatch) previousDataBox.getData(false, ProteinMatch.class);
        PeptideInstance selectedPeptide = (PeptideInstance) previousDataBox.getData(false, PeptideInstance.class);
        ResultSummary resultSummary = (ResultSummary) previousDataBox.getData(false, ResultSummary.class);
       
        
        if ((proteinMatch == null) || (selectedPeptide == null) || (resultSummary == null)) {
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
        ((RsmProteinAndPeptideSequencePanel) m_panel).setData(proteinMatch, selectedPeptide, peptideInstances);
    }
}
