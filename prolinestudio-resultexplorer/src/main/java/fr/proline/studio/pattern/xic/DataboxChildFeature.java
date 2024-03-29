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

import fr.proline.core.orm.lcms.Feature;
import fr.proline.core.orm.lcms.Peakel;
import fr.proline.core.orm.lcms.Peak;
import fr.proline.core.orm.lcms.dto.DFeature;
import fr.proline.core.orm.msi.dto.DMasterQuantPeptideIon;


import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadLcMSTask;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.mzscope.MzScopeInterface;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.ParameterList;
import fr.proline.studio.rsmexplorer.gui.xic.QuantChannelInfo;
import fr.proline.studio.rsmexplorer.gui.xic.XicFeaturePanel;
import static fr.proline.studio.rsmexplorer.gui.xic.XicFeaturePanel.VIEW_ALL_GRAPH_PEAKS;
import static fr.proline.studio.rsmexplorer.gui.xic.XicFeaturePanel.VIEW_ALL_ISOTOPES_FOR_FEATURE;
import fr.proline.studio.rsmexplorer.gui.xic.XicPeakPanel;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.pattern.ParameterSubtypeEnum;
import fr.proline.studio.pattern.extradata.GraphicExtraData;
import fr.proline.studio.rsmexplorer.gui.xic.PeakTableModel;
import java.util.Arrays;
import java.util.HashSet;

/**
 *
 * @author JM235353
 */
public class DataboxChildFeature extends AbstractDataBox {

    private DMasterQuantPeptideIon m_masterQuantPeptideIon;
    private List<DFeature> m_childFeatureList;
    private QuantChannelInfo m_quantChannelInfo;
    private List<Boolean> m_featureHasPeak;

    private List<List<Peakel>> m_peakelList;
    private List<List<List<Peak>>> m_peakList;

    private HashSet<DFeature> m_extractedXICSet = null;
    
    private Boolean m_keepZoom = Boolean.FALSE;
    
    public DataboxChildFeature() {
        super(DataboxType.DataboxXicChildFeature, DataboxStyle.STYLE_XIC);

        // Name of this databox
        m_typeName = "XIC Features";
        m_description = "All Features for a Quanti. Peptide Ion";

        // Register in parameters
        // One Map 
        ParameterList inParameter = new ParameterList();
        inParameter.addParameter(DMasterQuantPeptideIon.class);
        inParameter.addParameter(QuantChannelInfo.class);
        registerInParameter(inParameter);

        // Register possible out parameters
        ParameterList outParameter = new ParameterList();
        outParameter.addParameter(Feature.class);
        outParameter.addParameter(DFeature.class, ParameterSubtypeEnum.LIST_DATA);

        outParameter.addParameter(ExtendedTableModelInterface.class);
        outParameter.addParameter(CrossSelectionInterface.class);
        
        outParameter.addParameter(ExtendedTableModelInterface.class, ParameterSubtypeEnum.LIST_DATA);
        outParameter.addParameter(CrossSelectionInterface.class, ParameterSubtypeEnum.LIST_DATA);


        registerOutParameter(outParameter);

    }

    @Override
    public void createPanel() {
        XicFeaturePanel p = new XicFeaturePanel(true);
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }

    @Override
    public void dataChanged() {
        
        m_extractedXICSet = null;
        
        DMasterQuantPeptideIon oldIon = m_masterQuantPeptideIon;
        m_masterQuantPeptideIon = (DMasterQuantPeptideIon) getData(DMasterQuantPeptideIon.class);
        m_quantChannelInfo = (QuantChannelInfo) getData(QuantChannelInfo.class);

        if (m_masterQuantPeptideIon != null && (m_masterQuantPeptideIon.equals(oldIon))) {
            return;
        }
        if (m_masterQuantPeptideIon == null && oldIon == null) {
            return;
        }

        
        if (m_quantChannelInfo == null) {
            
            ((XicFeaturePanel) getDataBoxPanelInterface()).setData(-1l, null, null, null, true, null);
            
            return;
        }


        final int loadingId = setLoading();
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                if (success) {
                    m_peakList = new ArrayList<>();
                    if (m_childFeatureList != null) {
                        for (int i = 0; i < m_childFeatureList.size(); i++) {
                            boolean hasPeak = false;
                            List<List<Peak>> list = new ArrayList<>();
                            if (m_peakelList.size() >= i + 1) {
                                for (Peakel peakel : m_peakelList.get(i)) {
                                    List<Peak> listPeak = peakel.getPeakList();
                                    if (listPeak.size() > 0) {
                                        hasPeak = true;
                                    }
                                    list.add(listPeak);
                                }
                            }
                            m_peakList.add(list);
                            m_featureHasPeak.add(hasPeak);
                        }

                    }
                    
                    ((XicFeaturePanel) getDataBoxPanelInterface()).setData(taskId, m_childFeatureList, m_quantChannelInfo, m_featureHasPeak, finished, m_masterQuantPeptideIon);
                    
                } else {
                    m_childFeatureList = new ArrayList<>();
                    m_featureHasPeak = new ArrayList<>();
                    m_peakelList = new ArrayList<>();
                    m_peakList = new ArrayList<>();
                    ((XicFeaturePanel) getDataBoxPanelInterface()).setData(taskId, m_childFeatureList, m_quantChannelInfo, m_featureHasPeak, finished, m_masterQuantPeptideIon);
                }


                setLoaded(loadingId);

                if (finished) {
                    unregisterTask(taskId);
                    
                    addDataChanged(ExtendedTableModelInterface.class, ParameterSubtypeEnum.SINGLE_DATA);
                    addDataChanged(ExtendedTableModelInterface.class, ParameterSubtypeEnum.LIST_DATA);
                    propagateDataChanged();
                }

            }
        };


        // ask asynchronous loading of data
        m_childFeatureList = new ArrayList<>();
        m_featureHasPeak = new ArrayList<>();
        m_peakelList = new ArrayList<>();
        m_peakList = new ArrayList<>();
        DatabaseLoadLcMSTask task = new DatabaseLoadLcMSTask(callback);

//        Long alnRefMapId = m_quantChannelInfo.getDataset().getAlnReferenceMapId();
//        List<MapAlignment> allMapAlignments = new ArrayList();
//        allMapAlignments.addAll(m_quantChannelInfo.getDataset().getMapAlignments());
//        allMapAlignments.addAll(m_quantChannelInfo.getDataset().getMapReversedAlignments());


//        task.initLoadChildFeatureForPeptideIonWithPeakel(getProjectId(), m_masterQuantPeptideIon, m_childFeatureList, m_peakelList, allMapAlignments, alnRefMapId, m_quantChannelInfo.getDataset().getMaps());
        task.initLoadChildFeatureForPeptideIonWithPeakel(getProjectId(), m_masterQuantPeptideIon, m_quantChannelInfo.getQuantChannels(), m_childFeatureList, m_peakelList, m_quantChannelInfo.getDataset());
        registerTask(task);

    }

    private List<XicPeakPanel> getPeakTableModelList() {
        List<XicPeakPanel> list = new ArrayList<>();
        int viewType = ((XicFeaturePanel) getDataBoxPanelInterface()).getGraphViewType();
        if (m_childFeatureList != null) {
            switch (viewType) {
                case VIEW_ALL_GRAPH_PEAKS: {
                    for (int i = 0; i < m_childFeatureList.size(); i++) {
                        DFeature feature = m_childFeatureList.get(i);
                        Color color = m_quantChannelInfo.getQuantChannelColor(feature.getQuantChannelId());
                        String title = m_quantChannelInfo.getQuantChannels(feature.getQuantChannelId()).getName();
                        if (m_peakelList != null && m_peakelList.size() > i) {
                            List<Peakel> peakels = m_peakelList.get(i);
                            if (peakels.size() > 0) {
                                // get the first isotope
                                int idP = 0;
                                for (Peakel peakel : peakels) {
                                    if (peakel.getIsotopeIndex() == 0) {
                                        if (m_peakList.size() > i && !m_peakList.get(i).isEmpty()) {
                                            List<Peak> peaks = m_peakList.get(i).get(idP);
                                            XicPeakPanel peakPanel = new XicPeakPanel();
                                            peakPanel.setData((long) -1, feature, peakel, peakel.getIsotopeIndex(), peaks, color, title, true);
                                            list.add(peakPanel);
                                        }
                                    }
                                    idP++;
                                }

                            }
                        }
                    }
                    break;
                }
                case VIEW_ALL_ISOTOPES_FOR_FEATURE: {
                    DFeature selectedFeature = ((XicFeaturePanel) getDataBoxPanelInterface()).getSelectedFeature();
                    if (selectedFeature != null) {
                        int id = m_childFeatureList.indexOf(selectedFeature);
                        if (id != -1) {
                            Color color = m_quantChannelInfo.getQuantChannelColor(selectedFeature.getQuantChannelId());
                            String title = m_quantChannelInfo.getQuantChannels(selectedFeature.getQuantChannelId()).getName();
                            if (m_peakelList != null && m_peakelList.size() > id) {
                                List<Peakel> peakels = m_peakelList.get(id);
                                int nbPeakel = peakels.size();
                                if (m_peakList.size() > id && !m_peakList.get(id).isEmpty()) {
                                    List<List<Peak>> listPeaks = m_peakList.get(id);
                                    for (int p = 0; p < nbPeakel; p++) {
                                        List<Peak> peaks = listPeaks.get(p);
                                        XicPeakPanel peakPanel = new XicPeakPanel();
                                        peakPanel.setData((long) -1, selectedFeature, peakels.get(p), peakels.get(p).getIsotopeIndex(), peaks, color, title, true);
                                        list.add(peakPanel);
                                    }
                                }
                            }
                        }
                    }
                    break;
                }
            }

        }

        return list;
    }
    
    
    private void addExtractedXIC(List<ExtendedTableModelInterface> modelList) {

        if (m_extractedXICSet == null) {
            return;
        }
        
        int viewType = ((XicFeaturePanel) getDataBoxPanelInterface()).getGraphViewType();
        if (m_childFeatureList != null) {
            switch (viewType) {
                case VIEW_ALL_GRAPH_PEAKS: {   
                    for (DFeature feature : m_extractedXICSet) {
                        if (!feature.hasPeaks()) {
                            // no extracted XIC for this feature
                            continue;
                        }
                        Color color = m_quantChannelInfo.getQuantChannelColor(feature.getQuantChannelId());
                        String title = m_quantChannelInfo.getQuantChannels(feature.getQuantChannelId()).getName();
                        
                        // Take only first isotop
                        Peak[] peakArray = feature.getPeakArray(0 /*first isotop*/);
                        if (peakArray != null) {
                            PeakTableModel model = new PeakTableModel(null);
                            model.setData(-1L, null, null, -1, new ArrayList<>(Arrays.asList(peakArray)), color, true, title);
                            modelList.add(model);
                        }
                    }   
                    break;
                }
                case VIEW_ALL_ISOTOPES_FOR_FEATURE: {
                    DFeature selectedFeature = ((XicFeaturePanel) getDataBoxPanelInterface()).getSelectedFeature();
                    if (!selectedFeature.hasPeaks()) {
                        // no extracted XIC for this feature
                        return;
                    }
                    Color color = m_quantChannelInfo.getQuantChannelColor(selectedFeature.getQuantChannelId());
                    String title = m_quantChannelInfo.getQuantChannels(selectedFeature.getQuantChannelId()).getName();
                    for (int i=0;i<3;i++) {
                        Peak[] peakArray = selectedFeature.getPeakArray(i);
                        if (peakArray != null) {
                            PeakTableModel model = new PeakTableModel(null);
                            model.setData(-1L, null, null, -1, new ArrayList<>(Arrays.asList(peakArray)), color, true, title);
                            modelList.add(model);
                        }
                    }
                    break;
                }
            }

        }
    }

    @Override
    public void setEntryData(Object data) {
        getDataBoxPanelInterface().addSingleValue(data);
        m_masterQuantPeptideIon = (DMasterQuantPeptideIon) data;
        dataChanged();
    }
    
    public DMasterQuantPeptideIon getMasterQuantPeptideIon() {
        return m_masterQuantPeptideIon;
    }

    
    @Override
    public Object getDataImpl(Class parameterType, ParameterSubtypeEnum parameterSubtype) {
        
        if (parameterType!= null ) {

            // Returning single data
            if (parameterSubtype == ParameterSubtypeEnum.SINGLE_DATA) {
                if (parameterType.equals(MzScopeInterface.class)) {
                    return ((XicFeaturePanel) getDataBoxPanelInterface()).getMzScopeInterface();
                }

                if (parameterType.equals(Feature.class)) {
                    return ((XicFeaturePanel) getDataBoxPanelInterface()).getSelectedFeature();
                }
                
                if (parameterType.equals(ExtendedTableModelInterface.class)) {
                    return ((GlobalTabelModelProviderInterface) getDataBoxPanelInterface()).getGlobalTableModelInterface();
                }
                
                if (parameterType.equals(CrossSelectionInterface.class)) {
                    return ((GlobalTabelModelProviderInterface) getDataBoxPanelInterface()).getCrossSelectionInterface();
                }
            }
            
            // Returning a list of data
            if (parameterSubtype == ParameterSubtypeEnum.LIST_DATA) {
                if (parameterType.equals(DFeature.class)) {
                    return m_childFeatureList;
                }
                
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


   /**
     * Return potential extra data available for the corresponding parameter of class type
     * @param parameterType parameter class type
     * @return data available for the corresponding parameter of class type
     */
    @Override
    public Object getExtraData(Class parameterType) {
        if (parameterType.equals(ExtendedTableModelInterface.class)) {
            return new GraphicExtraData(m_keepZoom, null); // True during xic extraction 
        }
        
        return super.getExtraData(parameterType);
    }

    @Override
    public String getFullName() {
        if (m_masterQuantPeptideIon == null) {
            return super.getFullName();
        }
        return m_masterQuantPeptideIon.getCharge() + " " + getTypeName();
    }

    private List<ExtendedTableModelInterface> getCompareDataInterfaceList() {
        List<ExtendedTableModelInterface> listCDI = new ArrayList<>();
        List<XicPeakPanel> listPeakPanel = getPeakTableModelList();  //JPM : use of XicPeakPanel is a nonsense
        for (XicPeakPanel peakPanel : listPeakPanel) {
            listCDI.add(peakPanel.getGlobalTableModelInterface());
        }
        
        addExtractedXIC(listCDI);
        
        

        
        return listCDI;
    }


    
    private List<CrossSelectionInterface> getCrossSelectionInterfaceList() {
        List<CrossSelectionInterface> listCSI = new ArrayList<>();
        List<XicPeakPanel> listPeakPanel = getPeakTableModelList();
        for (XicPeakPanel peakPanel : listPeakPanel) {
            listCSI.add(peakPanel.getCrossSelectionInterface());
        }

        
        return listCSI;
    }
    
    public void setRetrievedXic(HashSet<DFeature> featureList) {
        if (m_extractedXICSet == null) {
            m_extractedXICSet = new HashSet<>();
        }
        
        m_extractedXICSet.addAll(featureList);

        propagateModelChangeWithoutModifyingZoom();
    }
    
    public void propagateModelChangeWithoutModifyingZoom() {
        m_keepZoom = Boolean.TRUE;
        addDataChanged(ExtendedTableModelInterface.class, ParameterSubtypeEnum.SINGLE_DATA);
        addDataChanged(ExtendedTableModelInterface.class, ParameterSubtypeEnum.LIST_DATA);
        propagateDataChanged();
        m_keepZoom = Boolean.FALSE;
    }
    
    
}
