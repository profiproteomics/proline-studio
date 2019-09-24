/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 19 déc. 2018
 */
package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadPeptidesInstancesTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.data.ptm.PTMPeptideInstance;
import fr.proline.studio.dam.tasks.data.ptm.PTMSite;
import fr.proline.studio.rsmexplorer.gui.ptm.PTMGraphicCtrlPanel;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is very similaire as DataBoxPTMSitePeptides
 *
 * @author Karine XUE
 */
public class DataBoxPTMPeptidesGraphic extends AbstractDataBoxPTMPeptides {

    private boolean m_isClusterData;

    /**
     * Create a DataBoxPTMPeptidesGraphic : graphical view of
     * PTMPeptideInstances.
     */
    public DataBoxPTMPeptidesGraphic() {
        super(DataboxType.DataBoxPTMPeptidesGraphic, DataboxStyle.STYLE_RSM);
        m_isClusterData = false;
        m_displayAllPepMatches = false;
        m_isXICResult = false;

        // Name of this databox
        m_typeName = "Graphical Peptides PTMs info";
        m_description = "PTMs information of Peptides of a [Group] Modification Site using graphical display.";

        // Register Possible in parameters          
        super.registerParameters();
    }

    public void setIsClusterData(boolean isClusterData) {
        this.m_isClusterData = isClusterData;
    }

    @Override
    public void createPanel() {
        //PTMPeptidesGraphicView p = new PTMPeptidesGraphicView(m_isClusterData);
        PTMGraphicCtrlPanel p = new PTMGraphicCtrlPanel(m_isClusterData);
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }

    private void loadPeptidesInstances() {
        DProteinMatch proteinMatch = (DProteinMatch) m_previousDataBox.getData(false, DProteinMatch.class);

        ArrayList<ResultSummary> rsmList = new ArrayList<>(1);
        rsmList.add(m_rsm);
        final int loadingId = setLoading();

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                setLoaded(loadingId);
                if (success) {
                    ((PTMGraphicCtrlPanel) getDataBoxPanelInterface()).setSelectedProtein(proteinMatch);
                } else {
                }
                if (finished) {
                    unregisterTask(taskId);
                }
            }
        };
        DatabaseLoadPeptidesInstancesTask task = new DatabaseLoadPeptidesInstancesTask(callback, getProjectId(), proteinMatch, rsmList);
        Long taskId = task.getId();
       
        registerTask(task);
    }
    

    @Override
    public void updateData() {
        // PTMPeptidesGraphicView graphicView = (PTMPeptidesGraphicView) getDataBoxPanelInterface();
        PTMGraphicCtrlPanel graphicView = (PTMGraphicCtrlPanel) getDataBoxPanelInterface();
        if (m_ptmPepInstances == null || m_ptmPepInstances.isEmpty()) {
            graphicView.setData(null);
            return;
        }
        loadPeptidesInstances();
        final List<PTMSite> notLoadedPtmSite = getNotLoadedPTMSite();

        if (notLoadedPtmSite.isEmpty()) {
            resetPrevPTMTaskId();
            graphicView.setData(m_ptmPepInstances);
            propagateDataChanged(PTMPeptideInstance.class);

        } else {

            loadPtmSite(notLoadedPtmSite);
        }

    }

    @Override
    protected void setSelectedPTMPeptide(PTMPeptideInstance pepInstance) {
        //((PTMPeptidesGraphicView) getDataBoxPanelInterface()).setSelectedPTMPeptide(pepInstance);
        ((PTMGraphicCtrlPanel) getDataBoxPanelInterface()).setSelectedPTMPeptide(pepInstance);
    }

    @Override
    protected PTMPeptideInstance getSelectedPTMPeptide() {
        //return ((PTMPeptidesGraphicView) getDataBoxPanelInterface()).getSelectedPTMPeptideInstance();
        return ((PTMGraphicCtrlPanel) getDataBoxPanelInterface()).getSelectedPTMPeptideInstance();
    }

    @Override
    protected void loadXicAndPropagate() {
        //Should not occur : m_isXICResult = false;
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
