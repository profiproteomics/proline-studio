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
package fr.proline.studio.pattern.xic;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.memory.TransientMemoryCacheManager;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadLcMSTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.GroupParameter;
import fr.proline.studio.rsmexplorer.gui.xic.ProteinQuantPanel;
import fr.proline.studio.rsmexplorer.gui.xic.QuantChannelInfo;
import fr.proline.studio.rsmexplorer.gui.xic.XicProteinSetPanel;
import fr.proline.studio.types.XicMode;
import java.util.ArrayList;
import java.util.List;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.rsmexplorer.gui.xic.XicAbundanceProteinTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author JM235353
 */
public class DataboxXicProteinSet extends AbstractDataBox {

    private static final Logger m_logger = LoggerFactory.getLogger(DataboxXicProteinSet.class);
    private DDataset m_dataset;
    private QuantChannelInfo m_quantChannelInfo;
    private List<DMasterQuantProteinSet> m_masterQuantProteinSetList;
    private boolean m_isXICMode = true;

    public DataboxXicProteinSet() {
        super(DataboxType.DataboxXicProteinSet, DataboxStyle.STYLE_XIC);

        // Name of this databox
        m_typeName = "Quanti Protein Sets";
        m_description = "All Protein Sets of a Quantitation";

        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(DDataset.class, false);
        registerInParameter(inParameter);

        // Register possible out parameters
        // One or Multiple ProteinSet 
        //VDS : Only one ?!
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(DProteinSet.class, false);
        outParameter.addParameter(DMasterQuantProteinSet.class, false);
        outParameter.addParameter(DDataset.class, false);
        outParameter.addParameter(ResultSummary.class, false);
        outParameter.addParameter(QuantChannelInfo.class, false);
        outParameter.addParameter(XicMode.class, false);

        registerOutParameter(outParameter);

        outParameter = new GroupParameter();
        outParameter.addParameter(ExtendedTableModelInterface.class, false);
        registerOutParameter(outParameter);

        outParameter = new GroupParameter();
        outParameter.addParameter(ExtendedTableModelInterface.class, true);
        registerOutParameter(outParameter);

        outParameter = new GroupParameter();
        outParameter.addParameter(CrossSelectionInterface.class, true);
        registerOutParameter(outParameter);

        outParameter = new GroupParameter();
        outParameter.addParameter(XicAbundanceProteinTableModel.class, true);
        registerOutParameter(outParameter);

    }

    public boolean isXICMode() {
        return m_isXICMode;
    }

    public void setXICMode(boolean isXICMode) {
        m_isXICMode = isXICMode;
        m_style = (m_isXICMode) ? DataboxStyle.STYLE_XIC : DataboxStyle.STYLE_SC;
        if (getDataBoxPanelInterface() != null) {
            getDataBoxPanelInterface().addSingleValue(new XicMode((isXICMode)));
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

        getDataBoxPanelInterface().addSingleValue(new XicMode((m_isXICMode)));
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

                    AbstractDatabaseCallback mapCallback = new AbstractDatabaseCallback() {

                        @Override
                        public boolean mustBeCalledInAWT() {
                            return true;
                        }

                        @Override
                        public void run(boolean success, long task2Id, SubTask subTask, boolean finished) {
                            m_quantChannelInfo = new QuantChannelInfo(m_dataset);
                            getDataBoxPanelInterface().addSingleValue(m_quantChannelInfo);

                            // proteins set 
                            //DMasterQuantProteinSet[] masterQuantProteinSetArray = new DMasterQuantProteinSet[m_masterQuantProteinSetList.size()];
                            //m_masterQuantProteinSetList.toArray(masterQuantProteinSetArray);
                            ((XicProteinSetPanel) getDataBoxPanelInterface()).setData(taskId, m_quantChannelInfo.getQuantChannels(), m_masterQuantProteinSetList, m_isXICMode, finished);
                            if (finished) {
                                unregisterTask(task2Id);
                                propagateDataChanged(ExtendedTableModelInterface.class);
                            }
                        }
                    };
                    // ask asynchronous loading of data
                    DatabaseLoadLcMSTask taskMap = new DatabaseLoadLcMSTask(mapCallback);
                    taskMap.initLoadAlignmentForXic(getProjectId(), m_dataset);
                    registerTask(taskMap);

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
        m_masterQuantProteinSetList = new ArrayList();
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
    public void dataMustBeRecalculated(Long rsetId, Long rsmId, Class dataType, ArrayList modificationsList, int reason) {
        if (m_dataset.getResultSetId() != rsetId) {
            return;
        }
        if (m_dataset.getResultSummaryId() != rsmId) {
            return;
        }
        if (dataType.equals(DMasterQuantProteinSet.class)) {
            ((XicProteinSetPanel) getDataBoxPanelInterface()).dataModified(modificationsList, reason);
        }
    }

    @Override
    public void setEntryData(Object data) {
        getDataBoxPanelInterface().addSingleValue(data);
        m_dataset = (DDataset) data;
        dataChanged();
    }

    @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType != null) {
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
            if (parameterType.equals(XicMode.class)) {
                return new XicMode(isXICMode());
            }

        }
        return super.getData(getArray, parameterType);
    }

    @Override
    public Object getData(boolean getArray, Class parameterType, boolean isList) {
        if (parameterType != null && isList) {
            if (parameterType.equals(ExtendedTableModelInterface.class)) {
                return getCompareDataInterfaceList();
            }
            if (parameterType.equals(CrossSelectionInterface.class)) {
                return getCrossSelectionInterfaceList();
            }
        }
        return super.getData(getArray, parameterType, isList);
    }

    @Override
    public String getFullName() {
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
        aProtPanel.setData(m_quantChannelInfo.getQuantChannels(), ((XicProteinSetPanel) getDataBoxPanelInterface()).getSelectedMasterQuantProteinSet(), m_isXICMode);
        return aProtPanel;
    }

    @Override
    public Class[] getImportantInParameterClass() {
        Class[] classList = {DMasterQuantProteinSet.class, DProteinSet.class};
        return classList;
    }

    @Override
    public String getImportantOutParameterValue() {
        DProteinSet p = (DProteinSet) getData(false, DProteinSet.class);
        if (p != null) {
            DProteinMatch pm = p.getTypicalProteinMatch();
            if (pm != null) {
                return pm.getAccession();
            }
        }
        return null;
    }
}
