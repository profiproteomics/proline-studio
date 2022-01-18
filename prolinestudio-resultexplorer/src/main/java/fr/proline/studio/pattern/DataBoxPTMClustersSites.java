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
package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.studio.dam.tasks.data.ptm.PTMCluster;
import fr.proline.studio.dam.tasks.data.ptm.PTMDataset;
import fr.proline.studio.dam.tasks.data.ptm.PTMDatasetPair;
import fr.proline.studio.dam.tasks.data.ptm.PTMPeptideInstance;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.rsmexplorer.gui.PTMClustersPanel;
import fr.proline.studio.rsmexplorer.gui.xic.QuantChannelInfo;
import fr.proline.studio.types.XicMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author VD225637
 */
public class DataBoxPTMClustersSites extends AbstractDataBox {

    private final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer.ptm");

    private PTMDatasetPair m_currentPtmDs;
    private ArrayList<PTMCluster> m_clusters;
    private QuantChannelInfo m_quantChannelInfo; //Xic Specific
    private boolean m_isXicResult;

    public DataBoxPTMClustersSites() {
        this(false);
    }

    public DataBoxPTMClustersSites(boolean isXICResult) {
        super(isXICResult ? DataboxType.DataBoxXicPTMClustersSites : DataboxType.DataBoxPTMClustersSites, isXICResult ? DataboxStyle.STYLE_XIC : DataboxStyle.STYLE_RSM);

       // Name of this databox
        m_typeName =  isXICResult ? "Quantitation Clusters PTMs Sites" : "Clusters PTMs Sites";

        m_description = isXICResult ? "Quantitation Modification Sites of Clusters" : "Modification Sites of Clusters";
        m_isXicResult = isXICResult;

        // Register in parameters
        ParameterList inParameter = new ParameterList();
        inParameter.addParameter(PTMCluster.class, ParameterSubtypeEnum.LIST_DATA);
        inParameter.addParameter(PTMDatasetPair.class);
        if (m_isXicResult) {
            inParameter.addParameter(QuantChannelInfo.class);
        }
        registerInParameter(inParameter);
        
        // Register possible out parameters
        ParameterList outParameter = new ParameterList();
        outParameter.addParameter(PTMPeptideInstance.class, ParameterSubtypeEnum.LEAF_PTMPeptideInstance);
        outParameter.addParameter(PTMPeptideInstance.class, ParameterSubtypeEnum.PARENT_PTMPeptideInstance);

        outParameter.addParameter(DProteinMatch.class); 
        outParameter.addParameter(DPeptideMatch.class);
        
        outParameter.addParameter(PTMCluster.class, ParameterSubtypeEnum.LIST_DATA);

        outParameter.addParameter(ExtendedTableModelInterface.class);
        outParameter.addParameter(CrossSelectionInterface.class);
        
        registerOutParameter(outParameter);
        if (m_isXicResult) {
            registerXicOutParameter();
        }
    }

    private boolean isXicResult() {
        return m_isXicResult;
    }

    private void registerXicOutParameter(){
        ParameterList outParameter = new ParameterList();
        
        outParameter.addParameter(DMasterQuantProteinSet.class);
        outParameter.addParameter(DProteinSet.class);
        outParameter.addParameter(QuantChannelInfo.class);
        
        registerOutParameter(outParameter);
    }

    @Override
    public void createPanel() {
        PTMClustersPanel p = new PTMClustersPanel(false);
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }


    @Override
    public void dataChanged() {

        m_currentPtmDs  =(PTMDatasetPair) m_previousDataBox.getData(PTMDatasetPair.class);
        List<PTMCluster> newClusters = (List<PTMCluster>) m_previousDataBox.getData(PTMCluster.class, ParameterSubtypeEnum.LIST_DATA);
        if(newClusters == null || newClusters.isEmpty())  { // && m_clusters != null && !m_clusters.isEmpty() ) {
            m_clusters = null;
            ((PTMClustersPanel) getDataBoxPanelInterface()).setData(null, m_clusters, true);
            m_logger.debug("No PTM Clusters'Site to display. ");
            return;
        }

        m_clusters = new ArrayList<>();

        if(m_isXicResult) {
            m_quantChannelInfo = (QuantChannelInfo) m_previousDataBox.getData(QuantChannelInfo.class);
            getDataBoxPanelInterface().addSingleValue(m_quantChannelInfo);
        }

        PTMDataset siteDS = m_currentPtmDs.getSitePTMDataset();
        for(PTMCluster c : newClusters){
            c.getPTMSites().forEach(site -> {
                PTMCluster siteCluster = siteDS.getPTMCluster(site.getId());
                if(siteCluster != null && !m_clusters.contains(siteCluster))
                    m_clusters.add(siteCluster);
            });
        }

        ((PTMClustersPanel) getDataBoxPanelInterface()).setData(null, m_clusters, true);

    }

    
    @Override
    public Object getDataImpl(Class parameterType, ParameterSubtypeEnum parameterSubtype) {
        if (parameterType!= null ) {
            
            if (parameterSubtype == ParameterSubtypeEnum.SINGLE_DATA) {
                if (parameterType.equals(DProteinMatch.class)) {
                    PTMCluster cluster = ((PTMClustersPanel)getDataBoxPanelInterface()).getSelectedProteinPTMCluster();
                    if (cluster != null) {
                        return cluster.getProteinMatch();
                    }
                }
                if (parameterType.equals(DPeptideMatch.class)) {
                    PTMCluster cluster = ((PTMClustersPanel) getDataBoxPanelInterface()).getSelectedProteinPTMCluster();
                    if (cluster != null) {
                        return cluster.getRepresentativePepMatch();
                    }
                }


                //XIC Specific ---- 
                if (parameterType.equals(DMasterQuantProteinSet.class) && isXicResult()) {
                    PTMCluster cluster = ((PTMClustersPanel) getDataBoxPanelInterface()).getSelectedProteinPTMCluster();
                    if (cluster != null) {
                        return cluster.getMasterQuantProteinSet();
                    }
                }
                if (parameterType.equals(DProteinSet.class) && isXicResult()) {
                    PTMCluster cluster = ((PTMClustersPanel) getDataBoxPanelInterface()).getSelectedProteinPTMCluster();
                    if (cluster != null && cluster.getMasterQuantProteinSet() != null) {
                        return cluster.getMasterQuantProteinSet().getProteinSet();
                    }
                }
                if (parameterType.equals(QuantChannelInfo.class) && isXicResult()) {
                    return m_quantChannelInfo;
                }

                if (parameterType.equals(ExtendedTableModelInterface.class)) {
                    return ((GlobalTabelModelProviderInterface) getDataBoxPanelInterface()).getGlobalTableModelInterface();
                }
                if (parameterType.equals(CrossSelectionInterface.class)) {
                    return ((GlobalTabelModelProviderInterface) getDataBoxPanelInterface()).getCrossSelectionInterface();
                }
                if (parameterType.equals(XicMode.class) && m_currentPtmDs.getClusterPTMDataset().isQuantitation()) {
                    return new XicMode(true);
                }
            }

            if (parameterSubtype == ParameterSubtypeEnum.LIST_DATA) {
                if (parameterType.equals(PTMCluster.class)) {
                    return ((PTMClustersPanel) getDataBoxPanelInterface()).getSelectedPTMClusters();
                }
            }
            

            if (parameterType.equals(PTMPeptideInstance.class) && (parameterSubtype.equals(ParameterSubtypeEnum.PARENT_PTMPeptideInstance) || parameterSubtype.equals(ParameterSubtypeEnum.LEAF_PTMPeptideInstance)) ) {

                boolean parentPTMPeptideInstance = parameterSubtype.equals(ParameterSubtypeEnum.PARENT_PTMPeptideInstance);
                
                List<PTMCluster> clusters = ((PTMClustersPanel) getDataBoxPanelInterface()).getSelectedPTMClusters();
                List<PTMPeptideInstance> ptmPeptideInstances =  new ArrayList<>();
                if (!clusters.isEmpty()) {
                    Collections.sort(clusters);                    
                    //get First Selected Cluster, and consider only PTMCluster on same protein match
                    Long protMatchId = ((PTMClustersPanel) getDataBoxPanelInterface()).getSelectedProteinPTMCluster().getProteinMatch().getId();
                    clusters.stream().filter(cluster -> protMatchId.equals(cluster.getProteinMatch().getId())).forEach(cluster -> {ptmPeptideInstances.addAll(parentPTMPeptideInstance ? cluster.getParentPTMPeptideInstances() : cluster.getLeafPTMPeptideInstances()); });                    
                }
                return ptmPeptideInstances;
            }

          
        }
        
        return super.getDataImpl(parameterType, parameterSubtype);
    }



    @Override
    public Class[] getDataboxNavigationOutParameterClasses() {
        if(isXicResult()){
            return new Class[]{DProteinMatch.class, DMasterQuantProteinSet.class};
        }else{
            return new Class[]{DProteinMatch.class};
        }
    }
    
    @Override
    public String getDataboxNavigationDisplayValue() {
        DProteinMatch p = (DProteinMatch) getData(DProteinMatch.class);
        if (p != null) {
            return p.getAccession();
        }
        return null;
    }
    
    
}
