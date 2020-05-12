/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadPeptidesInstancesTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.data.ptm.PTMPeptideInstance;
import fr.proline.studio.dam.tasks.data.ptm.PTMSite;
import fr.proline.studio.rsmexplorer.gui.ptm.PTMGraphicCtrlPanel;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is very similaire as DataBoxPTMSitePeptides
 *
 * @author Karine XUE
 */
public class DataBoxPTMPeptidesGraphic extends AbstractDataBoxPTMPeptides {

    private static Logger m_logger = LoggerFactory.getLogger("ProlineStudio.rsmexplorer.ptm");

    /**
     * Create a DataBoxPTMPeptidesGraphic : graphical view of
     * PTMPeptideInstances.
     */
    public DataBoxPTMPeptidesGraphic() {
        super(DataboxType.DataBoxPTMPeptidesGraphic, DataboxStyle.STYLE_RSM);
        m_displayAllPepMatches = false;
        m_isXICResult = false;

        // Name of this databox
        m_typeName = "Graphical Peptides PTMs info";
        m_description = "PTMs information of Peptides of a [Group] Modification Site using graphical display.";

        // Register Possible in parameters          
        super.registerParameters();
    }

    @Override
    public void createPanel() {
        PTMGraphicCtrlPanel p = new PTMGraphicCtrlPanel(true);
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

    protected ArrayList<Integer> getSelectedIndex() {
        ArrayList<Integer>  result = new ArrayList();
        result.add(((PTMGraphicCtrlPanel) this.m_panel).getSelectedIndex());
        return result;
    }

    @Override
    public void updateData() {
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

    /**
     * Only called 1 time by AbstractDataBoxPTMPeptides in dataChanged(),
     * if(valueUnchanged), so pepInstance == null come from previous databox who
     * has not this value, but it is impossible for actual Peptides Graphic
     * Area.
     *
     * @param pepInstance
     */
    @Override
    protected void setSelectedPTMPeptide(PTMPeptideInstance pepInstance) {
        if (pepInstance != null) {
            ((PTMGraphicCtrlPanel) getDataBoxPanelInterface()).setSelectedPTMPeptide(pepInstance);
        }
    }

    @Override
    protected PTMPeptideInstance getSelectedPTMPeptide() {
        return ((PTMGraphicCtrlPanel) getDataBoxPanelInterface()).getSelectedPTMPeptideInstance();
    }


    @Override
    protected DPeptideMatch getSelectedPeptideMatch() {
        return ((PTMGraphicCtrlPanel) getDataBoxPanelInterface()).getSelectedPTMPeptideInstance().getRepresentativePepMatch(m_ptmClusters);
    }

    @Override
    protected void loadXicAndPropagate() {
        //Should not occur : m_isXICResult = false;
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
