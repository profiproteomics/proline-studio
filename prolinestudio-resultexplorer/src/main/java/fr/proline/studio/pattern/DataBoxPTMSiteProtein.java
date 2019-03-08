package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabasePTMsTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.data.ptm.PTMDataset;
import fr.proline.studio.dam.tasks.data.ptm.PTMSite;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.rsmexplorer.gui.PTMProteinSitePanel;
import java.util.ArrayList;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;

/**
 *
 * @author JM235353
 */
public class DataBoxPTMSiteProtein extends AbstractDataBox {

    private PTMDataset m_dataset = null;
    
    public DataBoxPTMSiteProtein() { 
        super(DataboxType.DataBoxPTMProteinSite, DataboxStyle.STYLE_RSM);
        
        // Name of this databox
        m_typeName = "PTM Protein Sites";
        m_description = "PTM Protein Sites of an Identification Summary";

        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(ResultSummary.class, false);
        registerInParameter(inParameter);

        // Register possible out parameters
        // One or Multiple ProteinSet

        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(PTMSite.class, true);
        outParameter.addParameter(ResultSummary.class, false);
        registerOutParameter(outParameter);
        
        outParameter = new GroupParameter();
        outParameter.addParameter(DProteinMatch.class, true); 
        outParameter.addParameter(ResultSummary.class, false);
        registerOutParameter(outParameter);
        
        outParameter = new GroupParameter();
        outParameter.addParameter(DPeptideMatch.class, true);
        outParameter.addParameter(ResultSummary.class, false);
        registerOutParameter(outParameter);
        
        outParameter = new GroupParameter();
        outParameter.addParameter(ExtendedTableModelInterface.class, true);
        registerOutParameter(outParameter);

        
    }


    @Override
    public void createPanel() {
        PTMProteinSitePanel p = new PTMProteinSitePanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }
    
    @Override
    public void dataChanged() {

        final int loadingId = setLoading();

        final ArrayList<PTMSite> ptmSiteArray = new ArrayList<>();
        
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                if (finished) {
                    m_dataset.setPTMSites(ptmSiteArray);
                    loadPeptideMatches(loadingId, taskId, ptmSiteArray, finished);
                    
                }
            }
        };


        // ask asynchronous loading of data

        DatabasePTMsTask task = new DatabasePTMsTask(callback);
        task.initLoadPTMSites(getProjectId(), m_dataset.getDataset().getResultSummary(), ptmSiteArray);
        registerTask(task);

    }
    
    
    public void loadPeptideMatches(int loadingId, long taskId, ArrayList<PTMSite> ptmSiteArray, boolean finished) {

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, final long taskId, SubTask subTask, boolean finished) {
                if (finished) {
                    ((PTMProteinSitePanel) getDataBoxPanelInterface()).setData(taskId, ptmSiteArray, finished);
                    setLoaded(loadingId);
                    unregisterTask(taskId);
                    propagateDataChanged(ExtendedTableModelInterface.class);
                }
            }
        };

        DatabasePTMsTask task = new DatabasePTMsTask(callback);
        task.initFillPTMSites(getProjectId(), m_dataset.getDataset().getResultSummary(), ptmSiteArray);
        registerTask(task);

    }
    
 
    @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType!= null ) {
            if (parameterType.equals(ResultSummary.class)) {
                if (m_dataset.getDataset().getResultSummary() != null) {
                    return m_dataset.getDataset().getResultSummary();
                }
            }
            
            if (parameterType.equals(DProteinMatch.class)) {
                PTMSite proteinPtmSite = ((PTMProteinSitePanel)getDataBoxPanelInterface()).getSelectedProteinPTMSite();
                if (proteinPtmSite != null) {
                    return proteinPtmSite.getProteinMatch();
                }
            }
            if (parameterType.equals(DPeptideMatch.class)) {
                PTMSite proteinPtmSite = ((PTMProteinSitePanel) getDataBoxPanelInterface()).getSelectedProteinPTMSite();
                if (proteinPtmSite != null) {
                    return proteinPtmSite.getBestPeptideMatch();
                }
            }
            if (parameterType.equals(PTMSite.class)) {
                return ((PTMProteinSitePanel) getDataBoxPanelInterface()).getSelectedProteinPTMSite();
            }
 
            if (parameterType.equals(ExtendedTableModelInterface.class)) {
                return ((GlobalTabelModelProviderInterface) getDataBoxPanelInterface()).getGlobalTableModelInterface();
            }
            if (parameterType.equals(CrossSelectionInterface.class)) {
                return ((GlobalTabelModelProviderInterface) getDataBoxPanelInterface()).getCrossSelectionInterface();
            }
        }
        return super.getData(getArray, parameterType);
    }
 
    @Override
    public void setEntryData(Object data) {
        getDataBoxPanelInterface().addSingleValue(data);
        m_dataset = (PTMDataset) data;
        dataChanged();
    }

    @Override
    public Class[] getImportantInParameterClass() {
        Class[] classList = {DProteinMatch.class};
        return classList;
    }
    
    @Override
    public String getImportantOutParameterValue() {
        DProteinMatch p = (DProteinMatch) getData(false, DProteinMatch.class);
        if (p != null) {
            return p.getAccession();
        }
        return null;
    }
   
    

}
