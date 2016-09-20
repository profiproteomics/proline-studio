package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadPeptideMatchTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.rsmexplorer.gui.PeptideMatchPanel;


/**
 * Databox : All PSM of an Identification Summary or corresponding to a Peptide Instance
 * @author JM235353
 */
public class DataBoxRsmPSM extends AbstractDataBox {

    
    private ResultSummary m_rsm = null;

    private boolean m_mergedData;
    
    public DataBoxRsmPSM() {
        this(false);
    }
    
    public DataBoxRsmPSM(boolean mergedData) {
        super(DataboxType.DataBoxRsmPSM, DataboxStyle.STYLE_RSM);

        m_mergedData = mergedData;
        
        // Name of this databox
        m_typeName = "PSM";
        m_description = "All PSM of an Identification Summary or corresponding to a Peptide Instance";
        
        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(ResultSummary.class, false);
        registerInParameter(inParameter);
        
        // Register possible out parameters
        // One or Multiple PeptideMatch
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(DPeptideMatch.class, true);
        registerOutParameter(outParameter);

        outParameter = new GroupParameter();
        outParameter.addParameter(CompareDataInterface.class, true);
        registerOutParameter(outParameter);
       
    }
    

    @Override
    public void createPanel() {
        PeptideMatchPanel p = new PeptideMatchPanel(true, m_mergedData, true, true, false);
        p.setName(m_typeName);
        p.setDataBox(this);
        m_panel = p;
    }
    
    @Override
    public void dataChanged() {
        
        final ResultSummary _rsm = (m_rsm!=null) ? m_rsm : (ResultSummary) m_previousDataBox.getData(false, ResultSummary.class);

        
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                
               if (subTask == null) {

                    DPeptideMatch[] peptideMatchArray = _rsm.getTransientData().getPeptideMatches();
                    long[] peptideMatchIdArray = _rsm.getTransientData().getPeptideMatchesId();
                    ((PeptideMatchPanel)m_panel).setData(taskId, peptideMatchArray, peptideMatchIdArray, finished);
               } else {
                    ((PeptideMatchPanel)m_panel).dataUpdated(subTask, finished);
                }
               
                if (finished) {
                    unregisterTask(taskId);
                    propagateDataChanged(CompareDataInterface.class);
                }
            }
        };
        

        // ask asynchronous loading of data
        registerTask(new DatabaseLoadPeptideMatchTask(callback, getProjectId(), _rsm));

       
        
    }
    
    @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType!= null ) {
            if (parameterType.equals(DPeptideMatch.class)) {
                return ((PeptideMatchPanel)m_panel).getSelectedPeptideMatch();
            }
            if (parameterType.equals(ResultSummary.class)) {
                if (m_rsm != null) {
                    return m_rsm;
                }
            }
            if (parameterType.equals(CompareDataInterface.class)) {
                return ((GlobalTabelModelProviderInterface) m_panel).getGlobalTableModelInterface();
            }
            if (parameterType.equals(CrossSelectionInterface.class)) {
                return ((GlobalTabelModelProviderInterface)m_panel).getCrossSelectionInterface();
            }
        }
        return super.getData(getArray, parameterType);
    }
 
    @Override
    public void setEntryData(Object data) {
        
        m_panel.addSingleValue(data);
        
        if (data instanceof ResultSummary) {
            m_rsm = (ResultSummary) data;
            m_panel.addSingleValue(data);
            dataChanged();
        }
    }
    
    @Override
    public Class[] getImportantInParameterClass() {
        Class[] classList = {DPeptideMatch.class};
        return classList;
    }

    @Override
    public String getImportantOutParameterValue() {
        DPeptideMatch p = (DPeptideMatch) getData(false, DPeptideMatch.class);
        if (p != null) {
            Peptide peptide =  p.getPeptide();
            if (peptide != null) {
                return peptide.getSequence();
            }
        }
        return null;
    }
}
