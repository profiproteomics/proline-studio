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

import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;
import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.ParameterList;
import fr.proline.studio.pattern.ParameterSubtypeEnum;
import fr.proline.studio.rsmexplorer.gui.xic.QuantChannelInfo;
import fr.proline.studio.rsmexplorer.gui.xic.XicPeptidePanel;
import fr.proline.studio.types.XicMode;
import java.util.ArrayList;
import java.util.List;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.extendedtablemodel.SecondAxisTableModelInterface;
import fr.proline.studio.pattern.extradata.GraphicExtraData;
import fr.proline.studio.rsmexplorer.gui.xic.XICComparePeptideTableModel;
import fr.proline.studio.rsmexplorer.gui.xic.XicAbundanceProteinTableModel;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author JM235353
 */
public class DataboxXicPeptideSet extends AbstractDataBox {

    private static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private DDataset m_dataset;
    private DProteinSet m_proteinSet;
    private DMasterQuantProteinSet m_masterQuantProteinSet;
    private List<DMasterQuantPeptide> m_masterQuantPeptideList;
    private QuantChannelInfo m_quantChannelInfo;

    private boolean m_isXICMode = true;
    //Display only a subList of peptides instances not all from Peptide/ProteinSet
    private boolean m_displayPeptidesSubList = false;
    
    private Boolean m_keepZoom = Boolean.TRUE;

    public DataboxXicPeptideSet() {
        this(false);
    }

    public DataboxXicPeptideSet(boolean displayPeptidesSubList) {
        super((displayPeptidesSubList ? DataboxType.DataboxXicPeptideSetShortList : DataboxType.DataboxXicPeptideSet), DataboxStyle.STYLE_XIC);

        m_displayPeptidesSubList = displayPeptidesSubList;

        // Name of this databox
        if (!m_displayPeptidesSubList) {
            m_typeName = "Quanti Peptides";
            m_description = "All Quanti. Peptides of a ProteinSet";
        } else {
            m_typeName = "Quanti Peptides";
            m_description = "Short list of Quanti. Peptides";
        }

        // Register in parameters
        ParameterList inParameter = new ParameterList();
        inParameter.addParameter(DDataset.class);
        inParameter.addParameter(DProteinSet.class);
        inParameter.addParameter(DMasterQuantProteinSet.class);
        inParameter.addParameter(QuantChannelInfo.class);
        inParameter.addParameter(XicMode.class);
        registerInParameter(inParameter);

        // Register possible out parameters
        ParameterList outParameter = new ParameterList();
        outParameter.addParameter(DDataset.class);
        outParameter.addParameter(ResultSummary.class);
        
        outParameter.addParameter(DMasterQuantPeptide.class);
        outParameter.addParameter(XicMode.class);
        
        outParameter.addParameter(QuantChannelInfo.class);
        outParameter.addParameter(DPeptideMatch.class);

        outParameter.addParameter(ExtendedTableModelInterface.class);
        outParameter.addParameter(CrossSelectionInterface.class);

        outParameter.addParameter(SecondAxisTableModelInterface.class);
        
        outParameter.addParameter(ExtendedTableModelInterface.class, ParameterSubtypeEnum.LIST_DATA);
        

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
        if (m_dataset == null) {
            return null;
        }
        return m_dataset.getResultSetId();
    }

    @Override
    public Long getRsmId() {
        if (m_dataset == null) {
            return null;
        }
        return m_dataset.getResultSummaryId();
    }

    @Override
    public void createPanel() {
        XicPeptidePanel p = new XicPeptidePanel(false, m_isXICMode);
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);

        getDataBoxPanelInterface().addSingleValue(new XicMode((m_isXICMode)));
    }

    @Override
    public void dataChanged() {
        final boolean allPeptides = (m_previousDataBox == null);
        List<DPeptideInstance> pepInstances = null;
        if (!allPeptides) {
            DProteinSet newProSet = (DProteinSet) m_previousDataBox.getData(DProteinSet.class);
            DMasterQuantProteinSet newMasterProtSet = (DMasterQuantProteinSet) m_previousDataBox.getData(DMasterQuantProteinSet.class);
            DDataset newDS = (DDataset) m_previousDataBox.getData(DDataset.class);

            boolean valueUnchanged = Objects.equals(newProSet, m_proteinSet) && Objects.equals(newMasterProtSet, m_masterQuantProteinSet) && Objects.equals(newDS, m_dataset);
            if (valueUnchanged && !m_displayPeptidesSubList) {
                return;
            }
            m_proteinSet = newProSet;
            m_masterQuantProteinSet = newMasterProtSet;
            m_dataset = newDS;
            if (m_proteinSet == null) {
                return;
            }

            m_isXICMode = ((XicMode) m_previousDataBox.getData(XicMode.class)).isXicMode();
            m_quantChannelInfo = (QuantChannelInfo) m_previousDataBox.getData(QuantChannelInfo.class);
            if (m_quantChannelInfo == null) {
                throw new RuntimeException("Xic PeptideSet : Can't get QuantChannelInfo from previous databox");
            }

            if (m_displayPeptidesSubList) {
                pepInstances = (List<DPeptideInstance>) m_previousDataBox.getData(DPeptideInstance.class, ParameterSubtypeEnum.LIST_DATA);
            }
        }

        // register the link to the Transient Data
        if (m_dataset != null) {
            linkCache(m_dataset.getResultSummary());
        }

        final int loadingId = setLoading();

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, final long taskId, SubTask subTask, boolean finished) {
                if (subTask == null) {
                    if (!allPeptides && m_quantChannelInfo != null) {
                        ((XicPeptidePanel) getDataBoxPanelInterface()).setData(taskId, m_masterQuantProteinSet, m_quantChannelInfo.getQuantChannels(), m_masterQuantPeptideList, m_isXICMode, finished);

                    } else {

                        m_quantChannelInfo = new QuantChannelInfo(m_dataset);
                        getDataBoxPanelInterface().addSingleValue(m_quantChannelInfo);

                        ((XicPeptidePanel) getDataBoxPanelInterface()).setData(taskId, m_masterQuantProteinSet, m_quantChannelInfo.getQuantChannels(), m_masterQuantPeptideList, m_isXICMode, finished);

                    }
                } else {
                    ((XicPeptidePanel) getDataBoxPanelInterface()).dataUpdated(subTask, finished);
                }
                setLoaded(loadingId);

                if (finished) {
                    unregisterTask(taskId);
                    addDataChanged(ExtendedTableModelInterface.class);
                    addDataChanged(ExtendedTableModelInterface.class, ParameterSubtypeEnum.LIST_DATA);
                    m_keepZoom = Boolean.FALSE;
                    propagateDataChanged();
                    m_keepZoom = Boolean.TRUE;
                }
            }
        };

        // ask asynchronous loading of data
        m_masterQuantPeptideList = new ArrayList<>();
        DatabaseLoadXicMasterQuantTask task = new DatabaseLoadXicMasterQuantTask(callback);
        if (allPeptides) {
            task.initLoadPeptides(getProjectId(), m_dataset, m_masterQuantPeptideList, isXICMode());
        } else if (pepInstances != null && !pepInstances.isEmpty()) {
            List<Long> parentPepInstanceIdsL = pepInstances.stream().map(pi -> pi.getId()).collect(Collectors.toList());
            task.initLoadPeptides(getProjectId(), m_dataset, parentPepInstanceIdsL.toArray(new Long[parentPepInstanceIdsL.size()]), m_masterQuantPeptideList, isXICMode());
        } else {
            task.initLoadPeptides(getProjectId(), m_dataset, m_masterQuantProteinSet, m_masterQuantPeptideList, isXICMode());
        }
        registerTask(task);

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
            
            if (parameterSubtype == ParameterSubtypeEnum.SINGLE_DATA) {
                if (parameterType.equals(ResultSummary.class)) {
                    return m_dataset.getResultSummary();
                }
                if (parameterType.equals(DMasterQuantPeptide.class)) {
                    return ((XicPeptidePanel) getDataBoxPanelInterface()).getSelectedMasterQuantPeptide();
                }
                if (parameterType.equals(DPeptideMatch.class)) {
                    DMasterQuantPeptide map = ((XicPeptidePanel) getDataBoxPanelInterface()).getSelectedMasterQuantPeptide();
                    if (map == null) {
                        return null;
                    }
                    DPeptideInstance pi = map.getPeptideInstance();
                    if (pi == null) {
                        return null;
                    }
                    return pi.getBestPeptideMatch(); //VDS or representativePepMatch ? Warning, representative may not belong to same RSM
                }
                if (parameterType.equals(DDataset.class)) {
                    return m_dataset;
                }
                if (parameterType.equals(QuantChannelInfo.class)) {
                    return m_quantChannelInfo;
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
                
                if (parameterType.equals(SecondAxisTableModelInterface.class)) {
                    if (m_quantChannelInfo == null || m_masterQuantProteinSet == null) {
                        return null;
                    }
                    XicAbundanceProteinTableModel protTableModel = new XicAbundanceProteinTableModel();
                    protTableModel.setData(m_quantChannelInfo.getQuantChannels(), m_masterQuantProteinSet);
                    protTableModel.setName("Protein");
                    return protTableModel;
                }
            }
            
            if (parameterSubtype == ParameterSubtypeEnum.LIST_DATA) {

                if (parameterType.equals(ExtendedTableModelInterface.class)) {
                    return getTableModelInterfaceList();
                }
            }
            
            if (parameterSubtype == ParameterSubtypeEnum.PEPTIDES_SELECTION_LIST) {
                if (parameterType.equals(Integer.class)) {
                    CrossSelectionInterface crossSelectionInterface = ((XicPeptidePanel) this.m_panel).getCrossSelectionInterface();
                    ArrayList<Integer> result = new ArrayList();
                    if (crossSelectionInterface != null) {
                        ArrayList<Long> selection = null;
                        try {
                            selection = crossSelectionInterface.getSelection();
                        } catch (Exception e) {
                            m_logger.error("wart : selection not ready ", e);
                        }
                        if (selection != null) {
                            for (Long l : selection) {
                                result.add(l.intValue());
                            }
                        }
                    }
                    return result;
                }
            }
            
            


            
        }
        return super.getDataImpl(parameterType, parameterSubtype);
    }

       /**
     * Return potential extra data available for the corresponding parameter of class type
     * @param parameterType parameter class type
     * @return  potential extra data available for the corresponding parameter of class type
     */
    @Override
    public Object getExtraData(Class parameterType) {
        if (parameterType.equals(ExtendedTableModelInterface.class)) {
            return new GraphicExtraData(m_keepZoom, Double.valueOf(0)); // Keep Zoom - minimal value of min Y value
        }
        
        return super.getExtraData(parameterType);
    }


    @Override
    public String getFullName() {
        if (m_dataset == null) {
            return super.getFullName();
        }
        return m_dataset.getName() + " " + getTypeName();
    }

    private List<ExtendedTableModelInterface> getTableModelInterfaceList() {
        if (m_quantChannelInfo == null) {
            if (m_previousDataBox != null) {
                m_quantChannelInfo = (QuantChannelInfo) getData(QuantChannelInfo.class);
                if (m_quantChannelInfo == null) {
                    return null;
                }
            }
        }

        List<ExtendedTableModelInterface> list = new ArrayList();
        if (m_masterQuantPeptideList != null) {
            //for (DMasterQuantPeptide quantPeptide : m_masterQuantPeptideList) {
            for (int i = 0; i < m_masterQuantPeptideList.size(); i++) {

                DMasterQuantPeptide quantPeptide = m_masterQuantPeptideList.get(i);
                XICComparePeptideTableModel peptideData = new XICComparePeptideTableModel();
                peptideData.setData(m_quantChannelInfo.getQuantChannels(), quantPeptide, m_isXICMode);
                list.add(peptideData);
            }
        }
        return list;
    }

    @Override
    public Class[] getDataboxNavigationOutParameterClasses() {
        return new Class[]{DMasterQuantPeptide.class, DPeptideMatch.class};
    }

    @Override
    public String getDataboxNavigationDisplayValue() {
        DMasterQuantPeptide mqp = (DMasterQuantPeptide) getData(DMasterQuantPeptide.class);
        if (mqp != null) {
            DPeptideInstance peptideInstance = mqp.getPeptideInstance();

            if (peptideInstance != null) {
                Peptide peptide = peptideInstance.getPeptide();
                if (peptide != null) {
                    return peptide.getSequence();
                }
            }
        }
        return null;
    }
}
