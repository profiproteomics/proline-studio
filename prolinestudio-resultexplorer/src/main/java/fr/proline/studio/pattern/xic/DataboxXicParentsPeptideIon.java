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

import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;
import fr.proline.core.orm.msi.dto.DMasterQuantPeptideIon;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.uds.QuantitationChannel;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.ParameterList;
import fr.proline.studio.pattern.ParameterSubtypeEnum;
import fr.proline.studio.rsmexplorer.gui.xic.QuantChannelInfo;
import fr.proline.studio.rsmexplorer.gui.xic.XicParentPeptideIonPanel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jean-Philippe
 */
public class DataboxXicParentsPeptideIon extends AbstractDataBox {
    
    private DDataset m_dataset;
    private DMasterQuantPeptide m_masterQuantPeptide;
    private DMasterQuantPeptideIon m_aggregatedMasterQuantPeptideIon;
    
    private QuantChannelInfo m_quantChannelInfo;
    
    private HashMap<Long, DQuantitationChannel> m_quantitationChannelsMap = new HashMap<>();
    
    
    //private boolean m_isXICMode = true;
    
    public DataboxXicParentsPeptideIon() {
        super(AbstractDataBox.DataboxType.DataboxXicPeptideIon, AbstractDataBox.DataboxStyle.STYLE_XIC);

        // Name of this databox
        m_typeName = "Source Quanti. Peptides Ions";
        m_description = "All Sources Quanti. Peptides Ions of an Aggregated Quanti. Peptide Ion";

        // Register in parameters
        ParameterList inParameter = new ParameterList();
        
        inParameter.addParameter(DDataset.class, ParameterSubtypeEnum.SINGLE_DATA, false /* not compulsory */);
        inParameter.addParameter(QuantChannelInfo.class, ParameterSubtypeEnum.SINGLE_DATA, false /* not compulsory */);
        inParameter.addParameter(DMasterQuantPeptideIon.class);
        //inParameter.addParameter(XicMode.class);
        
        registerInParameter(inParameter);

        // Register possible out parameters
        ParameterList outParameter = new ParameterList();

        /*outParameter.addParameter(ResultSummary.class);
        outParameter.addParameter(DDataset.class);
        outParameter.addParameter(DMasterQuantPeptideIon.class);
        outParameter.addParameter(QuantChannelInfo.class);
        outParameter.addParameter(DPeptideMatch.class);
        
        outParameter.addParameter(ExtendedTableModelInterface.class);*/ //JPM.PARENT.TODO
        
        registerOutParameter(outParameter);
        
    }
    
    @Override
    public void createPanel() {
        XicParentPeptideIonPanel p = new XicParentPeptideIonPanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
        
        //getDataBoxPanelInterface().addSingleValue(new XicMode((m_isXICMode)));
    }
    
    @Override
    public void dataChanged() {
        
        
        DMasterQuantPeptideIon oldParentMasterQuantPeptideIon = m_aggregatedMasterQuantPeptideIon;

        m_aggregatedMasterQuantPeptideIon = (DMasterQuantPeptideIon) m_previousDataBox.getData(DMasterQuantPeptideIon.class);

        if (m_aggregatedMasterQuantPeptideIon == null || m_aggregatedMasterQuantPeptideIon.equals(oldParentMasterQuantPeptideIon)) {
            return;
        }
        
        m_masterQuantPeptide = (DMasterQuantPeptide) m_previousDataBox.getData(DMasterQuantPeptide.class);
        m_dataset = (DDataset) m_previousDataBox.getData(DDataset.class);
        m_quantChannelInfo = (QuantChannelInfo) m_previousDataBox.getData(QuantChannelInfo.class);
        
        Map<String, Object> map = null;
        try {
            map = m_dataset.getQuantProcessingConfigAsMap();
        } catch (Exception e) {
            
        }
        
        final HashMap<Long, HashSet<Long>> aggregatedToChildrenQuantChannelsId = new HashMap<>();
        
        ArrayList mappingList = (ArrayList) map.get("quant_channels_mapping");
        for (int i=0;i<mappingList.size();i++) {
            Map m = (Map) mappingList.get(i);
            Integer quantChannelNumberIndex = (Integer) m.get("quant_channel_number");
            
            Long aggregatedQuantChannelId = m_quantChannelInfo.getQuantChannels()[quantChannelNumberIndex-1].getId();
            
            HashSet<Long> childrenQuantitationId = new HashSet<>();
            aggregatedToChildrenQuantChannelsId.put(aggregatedQuantChannelId, childrenQuantitationId);
            
            Map<String, Integer> quantChannelsMatching = (Map<String, Integer>) m.get("quant_channels_matching");
            for (String key : quantChannelsMatching.keySet()) {
                Integer childQuantChannelId = quantChannelsMatching.get(key);
                childrenQuantitationId.add(childQuantChannelId.longValue());
            }
            
        }
        
        List<DMasterQuantPeptideIon> masterQuantPeptideIonList = new ArrayList<>();

        final int loadingId = setLoading();
        
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }
            
            @Override
            public void run(boolean success, final long taskId, SubTask subTask, boolean finished) {

                // register the link to the Transient Data
                if (m_dataset != null) {
                    linkCache(m_dataset.getResultSummary());
                }
                

                ((XicParentPeptideIonPanel) getDataBoxPanelInterface()).setData(m_aggregatedMasterQuantPeptideIon, masterQuantPeptideIonList, m_quantChannelInfo, aggregatedToChildrenQuantChannelsId, m_quantitationChannelsMap);

           
                
                setLoaded(loadingId);
                
                if (finished) {
                    unregisterTask(taskId);
                    addDataChanged(ExtendedTableModelInterface.class);
                    propagateDataChanged();
                }
            }
        };

        // ask asynchronous loading of data
        DatabaseLoadXicMasterQuantTask task = new DatabaseLoadXicMasterQuantTask(callback);
        task.initLoadParentPeptideIons(getProjectId(), m_dataset, m_masterQuantPeptide, m_aggregatedMasterQuantPeptideIon, masterQuantPeptideIonList, m_quantitationChannelsMap);
        
        Long taskId = task.getId();
        registerTask(task);
        
    }
    
    /*public boolean isXICMode() {
        return m_isXICMode;
    }*/
    
    /*public void setXICMode(boolean isXICMode) {
        m_isXICMode = isXICMode;
        m_style = (m_isXICMode) ? AbstractDataBox.DataboxStyle.STYLE_XIC : AbstractDataBox.DataboxStyle.STYLE_SC;
        if (getDataBoxPanelInterface() != null) {
            getDataBoxPanelInterface().addSingleValue(new XicMode((isXICMode)));
        }
    }*/
    
    @Override
    public void setEntryData(Object data) {
        getDataBoxPanelInterface().addSingleValue(data);
        m_dataset = (DDataset) data;
        dataChanged();
    }
    
    @Override
    public Object getDataImpl(Class parameterType, ParameterSubtypeEnum parameterSubtype) {
        
        /*
        if (parameterType != null) {
            
            if (parameterSubtype == ParameterSubtypeEnum.SINGLE_DATA) {

                if (parameterType.equals(ResultSummary.class)) {
                    return m_dataset.getResultSummary();
                }
                if (parameterType.equals(DMasterQuantPeptideIon.class)) {
                    return ((XicPeptideIonPanel) getDataBoxPanelInterface()).getSelectedMasterQuantPeptideIon();
                }
                if (parameterType.equals(QuantChannelInfo.class)) {
                    if (m_quantChannelInfo != null) {
                        return m_quantChannelInfo;
                    }
                }
                if (parameterType.equals(DPeptideMatch.class)) {
                    DMasterQuantPeptideIon qpi = ((XicPeptideIonPanel) getDataBoxPanelInterface()).getSelectedMasterQuantPeptideIon();
                    if (qpi == null) {
                        return null;
                    }
                    DPeptideInstance pi = qpi.getPeptideInstance();
                    if (pi == null) {
                        return null;
                    }
                    return pi.getBestPeptideMatch();
                }
                if (parameterType.equals(DDataset.class)) {
                    return m_dataset;
                }
                if (parameterType.equals(ExtendedTableModelInterface.class)) {
                    return ((GlobalTabelModelProviderInterface) getDataBoxPanelInterface()).getGlobalTableModelInterface();
                }
                if (parameterType.equals(CrossSelectionInterface.class)) {
                    return ((GlobalTabelModelProviderInterface) getDataBoxPanelInterface()).getCrossSelectionInterface();
                }
                if (parameterType.equals(QuantChannelInfo.class)) {
                    return m_quantChannelInfo;
                }
            }
        }*/
        return super.getDataImpl(parameterType, parameterSubtype);
    }
    
    @Override
    public String getFullName() {
        if (m_dataset == null) {
            return super.getFullName();
        }
        return m_dataset.getName() + " " + getTypeName();
    }
    
    @Override
    public Class[] getImportantInParameterClass() {
        Class[] classList = {DMasterQuantPeptideIon.class, DPeptideMatch.class};
        return classList;
    }
    
    @Override
    public String getImportantOutParameterValue() {
        DMasterQuantPeptideIon peptideIon = (DMasterQuantPeptideIon) getData(DMasterQuantPeptideIon.class);
        if (peptideIon != null) {
            DPeptideInstance peptideInstance = peptideIon.getPeptideInstance();
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
