/* 
 * Copyright (C) 2019
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
package fr.proline.studio.pattern.xic;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DDatasetType;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.ParameterList;
import fr.proline.studio.pattern.ParameterSubtypeEnum;
import fr.proline.studio.rsmexplorer.gui.xic.ProteinQuantPanel;
import fr.proline.studio.rsmexplorer.gui.xic.QuantChannelInfo;
import fr.proline.studio.rsmexplorer.gui.xic.XicProteinSetPanel;
import fr.proline.studio.types.XicMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JM235353
 */
public class DataboxXicProteinSet extends AbstractDataBox {

    private static final Logger m_logger = LoggerFactory.getLogger(DataboxXicProteinSet.class);
    private DDataset m_dataset;
    private QuantChannelInfo m_quantChannelInfo;
    private List<DMasterQuantProteinSet> m_masterQuantProteinSetList;
    private DDatasetType.QuantitationMethodInfo m_quantMethodInfo;

    public DataboxXicProteinSet() {
        super(DataboxType.DataboxXicProteinSet, DataboxStyle.STYLE_XIC);

        // Name of this databox
        m_typeName = "Quanti Protein Sets";
        m_description = "All Protein Sets of a Quantitation";
        m_quantMethodInfo = DDatasetType.QuantitationMethodInfo.FEATURES_EXTRACTION;

        // Register in parameters
        ParameterList inParameter = new ParameterList();
        inParameter.addParameter(DDataset.class);
        registerInParameter(inParameter);

        // Register possible out parameters
        ParameterList outParameter = new ParameterList();
        
        outParameter.addParameter(DDataset.class);
        outParameter.addParameter(ResultSummary.class);
        
        outParameter.addParameter(DProteinSet.class);
        outParameter.addParameter(DMasterQuantProteinSet.class);
        outParameter.addParameter(QuantChannelInfo.class);
        outParameter.addParameter(DDatasetType.QuantitationMethodInfo.class);

        outParameter.addParameter(ExtendedTableModelInterface.class);
        outParameter.addParameter(ExtendedTableModelInterface.class, ParameterSubtypeEnum.LIST_DATA);
        outParameter.addParameter(CrossSelectionInterface.class);
        
        registerOutParameter(outParameter);

    }

    public void setQuantitationMethodInfo(DDatasetType.QuantitationMethodInfo quantMethodInfo) {
        m_quantMethodInfo = quantMethodInfo;

        m_style = (m_quantMethodInfo.equals(DDatasetType.QuantitationMethodInfo.SPECTRAL_COUNTING)) ? DataboxStyle.STYLE_SC : DataboxStyle.STYLE_XIC;
        if (getDataBoxPanelInterface() != null) {
            getDataBoxPanelInterface().addSingleValue(m_quantMethodInfo);
        }
    }

    @Override
    public Long getRsetId() {
        if (m_dataset != null) {
            return m_dataset.getResultSetId();
        }
        return null;
    }

    @Override
    public Long getRsmId() {
        if (m_dataset != null) {
            return m_dataset.getResultSummaryId();
        }
        return null;
    }

    @Override
    public void createPanel() {
        XicProteinSetPanel p = new XicProteinSetPanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);

        getDataBoxPanelInterface().addSingleValue(m_quantMethodInfo);
    }

    @Override
    public void dataChanged() {
        final int loadingId = setLoading();

        // register the link to the Transient Data
        linkCache(m_dataset.getResultSummary());
        
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, final long taskId, SubTask subTask, boolean finished) {
                if (subTask == null) {

                    m_quantChannelInfo = new QuantChannelInfo(m_dataset);
                    getDataBoxPanelInterface().addSingleValue(m_quantChannelInfo);

                    // proteins set 
                    //DMasterQuantProteinSet[] masterQuantProteinSetArray = new DMasterQuantProteinSet[m_masterQuantProteinSetList.size()];
                    //m_masterQuantProteinSetList.toArray(masterQuantProteinSetArray);
                    ((XicProteinSetPanel) getDataBoxPanelInterface()).setData(taskId, m_quantChannelInfo.getQuantChannels(), m_masterQuantProteinSetList, m_quantMethodInfo, finished);

                    addDataChanged(ExtendedTableModelInterface.class, null);  //JPM.DATABOX : put null, because I don't know which subtype has been change : null means all. So it works as previously
                    propagateDataChanged();

                } else {
                    ((XicProteinSetPanel) getDataBoxPanelInterface()).dataUpdated(subTask, finished);
                }
                setLoaded(loadingId);

                if (finished) {
                    unregisterTask(taskId);
                }
            }
        };

        // ask asynchronous loading of data
        m_masterQuantProteinSetList = new ArrayList<>();
        DatabaseLoadXicMasterQuantTask task = new DatabaseLoadXicMasterQuantTask(callback);
        task.initLoadProteinSets(getProjectId(), m_dataset, m_masterQuantProteinSetList);
        //Long taskId = task.getId();
        /*if (m_previousTaskId != null) {
         // old task is suppressed if it has not been already done
         AccessDatabaseThread.getAccessDatabaseThread().abortTask(m_previousTaskId);
         }*/
        //m_previousTaskId = taskId;
        registerTask(task);

    }
    //private Long m_previousTaskId = null;

    /**
     * To be overriden if the modification in a following databox can lead to a
     * modificiation of the data of the current databox. (for instance,
     * disabling peptides -> modifications of protein set in the XIC View
     *
     * @param dataType
     */
    @Override
    public void dataMustBeRecalculated(Long rsetId, Long rsmId, Class dataType, ArrayList modificationsList, byte reason) {
        if(! ( isDataOfInterest(rsetId, rsmId, dataType) && dataType.equals(DMasterQuantProteinSet.class)))
            return;

        ((XicProteinSetPanel) getDataBoxPanelInterface()).dataModified(modificationsList, reason);
    }

    @Override
    public void setEntryData(Object data) {
        getDataBoxPanelInterface().addSingleValue(data);
        m_dataset = (DDataset) data;
        dataChanged();
    }

    @Override
    public Object getDataImpl(Class parameterType, ParameterSubtypeEnum parameterSubtype) {
                
        if (parameterType != null) {
            
            // Returning single data
            if (parameterSubtype == ParameterSubtypeEnum.SINGLE_DATA) {
                if (parameterType.equals(ResultSummary.class)) {
                    return m_dataset.getResultSummary();
                }
                if (parameterType.equals(DDataset.class)) {
                    return m_dataset;
                }
                if (parameterType.equals(QuantChannelInfo.class)) {
                    return m_quantChannelInfo;
                }
                if (parameterType.equals(DProteinSet.class)) {
                    return ((XicProteinSetPanel) getDataBoxPanelInterface()).getSelectedProteinSet();
                }
                if (parameterType.equals(DMasterQuantProteinSet.class)) {
                    return ((XicProteinSetPanel) getDataBoxPanelInterface()).getSelectedMasterQuantProteinSet();
                }
                if (parameterType.equals(ExtendedTableModelInterface.class)) {
                    return ((GlobalTabelModelProviderInterface) getDataBoxPanelInterface()).getGlobalTableModelInterface();
                }
                if (parameterType.equals(CrossSelectionInterface.class)) {
                    return ((GlobalTabelModelProviderInterface) getDataBoxPanelInterface()).getCrossSelectionInterface();
                }
                if (parameterType.equals(DDatasetType.QuantitationMethodInfo.class)) {
                    return m_quantMethodInfo;
                }
            }
            
            // Returning a list of data
            if (parameterSubtype == ParameterSubtypeEnum.LIST_DATA) {
                if (parameterType.equals(ExtendedTableModelInterface.class)) {
                    return getCompareDataInterfaceList();
                }
                if (parameterType.equals(CrossSelectionInterface.class)) {
                    return getCrossSelectionInterfaceList();
                }
            }

        }
        
 
        return super.getDataImpl(parameterType, parameterSubtype);
        
    }


    @Override
    public String getFullName() {
        if (m_dataset == null) {
            return super.getFullName();
        }
        return m_dataset.getName() + " " + getTypeName();
    }

    private List<ExtendedTableModelInterface> getCompareDataInterfaceList() {
        List<ExtendedTableModelInterface> listCDI = new ArrayList();
        ProteinQuantPanel aProtPanel = getProteinQuantTableModelList();
        listCDI.add(aProtPanel.getGlobalTableModelInterface());
        return listCDI;
    }

    private List<CrossSelectionInterface> getCrossSelectionInterfaceList() {
        List<CrossSelectionInterface> listCSI = new ArrayList();
        ProteinQuantPanel aProtPanel = getProteinQuantTableModelList();
        listCSI.add(aProtPanel.getCrossSelectionInterface());
        return listCSI;
    }

    private ProteinQuantPanel getProteinQuantTableModelList() {
        ProteinQuantPanel aProtPanel = new ProteinQuantPanel();
        aProtPanel.setData(m_quantChannelInfo.getQuantChannels(), ((XicProteinSetPanel) getDataBoxPanelInterface()).getSelectedMasterQuantProteinSet(), m_quantMethodInfo);
        return aProtPanel;
    }

    @Override
    public Class[] getDataboxNavigationOutParameterClasses() {
        Class[] classList = {DMasterQuantProteinSet.class, DProteinSet.class};
        return classList;
    }

    @Override
    public String getDataboxNavigationDisplayValue() {
        DProteinSet p = (DProteinSet) getData(DProteinSet.class);
        if (p != null) {
            DProteinMatch pm = p.getTypicalProteinMatch();
            if (pm != null) {
                return pm.getAccession();
            }
        }
        return null;
    }
}
