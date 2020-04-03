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
import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;
import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.core.orm.msi.dto.DQuantPeptide;
import fr.proline.core.orm.msi.dto.DQuantProteinSet;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DMasterQuantitationChannel;
import fr.proline.core.orm.util.DStoreCustomPoolConnectorFactory;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.memory.TransientMemoryCacheManager;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.DatabasePTMsTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.data.ptm.PTMCluster;
import fr.proline.studio.dam.tasks.data.ptm.PTMDataset;
import fr.proline.studio.dam.tasks.data.ptm.PTMPeptideInstance;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadLcMSTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.rsmexplorer.gui.PTMClustersProteinPanel;
import fr.proline.studio.rsmexplorer.gui.xic.QuantChannelInfo;
import fr.proline.studio.types.XicMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author VD225637
 */
public class DataBoxPTMClusters extends AbstractDataBox {
    
    private final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer.ptm");
    private long logStartTime;
    
    private PTMDataset m_ptmDataset;
    private ResultSummary m_rsm;
    private QuantChannelInfo m_quantChannelInfo; //Xic Specific
    private boolean m_isXicResult = false; //If false: display Ident result PTM Clusters

    public DataBoxPTMClusters() {
        super(AbstractDataBox.DataboxType.DataBoxPTMClusters, AbstractDataBox.DataboxStyle.STYLE_RSM);
        
       // Name of this databox
        m_typeName = "Dataset PTMs Clusters"; //May be Quant PTM Protein Sites... 
        m_description = "Clusters of Modification Sites of a Dataset";//May be Ident or Quant dataset... 

        // Register Possible in parameters
        // One PTMDatasert ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(PTMDataset.class, false);
        registerInParameter(inParameter);
        
        inParameter = new GroupParameter();
        inParameter.addParameter(DDataset.class, false);
        registerInParameter(inParameter);
        
        // Register possible out parameters
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(ResultSummary.class, false);
        registerOutParameter(outParameter);
        
        outParameter = new GroupParameter();
        outParameter.addParameter(PTMPeptideInstance.class, true);
        outParameter.addParameter(PTMDataset.class, false);
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

        outParameter = new GroupParameter();
        outParameter.addParameter(CrossSelectionInterface.class, true);
        registerOutParameter(outParameter);
                        
    }
    
    private boolean isXicResult() {
        return m_isXicResult;
    }

    
    public void setXicResult(boolean isXICResult) {
        m_isXicResult = isXICResult;
        m_style = (m_isXicResult) ? DataboxStyle.STYLE_XIC : DataboxStyle.STYLE_SC;
        if(m_isXicResult)
            registerXicOutParameter();
    }
    
    private void registerXicOutParameter(){
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(DMasterQuantProteinSet.class, false);
        outParameter.addParameter(DProteinSet.class, false);
        registerOutParameter(outParameter);        
                
        outParameter = new GroupParameter();
        outParameter.addParameter(QuantChannelInfo.class, false);
        registerOutParameter(outParameter);
    }
    
    @Override
    public Long getRsetId() {
        if (m_ptmDataset == null) {
            return null;
        }
        return m_ptmDataset.getDataset().getResultSetId();
    }

    @Override
    public Long getRsmId() {
        if (m_ptmDataset == null) {
            return null;
        }
        return m_ptmDataset.getDataset().getResultSummaryId();
    }
    
    @Override
    public void createPanel() {
        PTMClustersProteinPanel p = new PTMClustersProteinPanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }

    @Override
    public void dataChanged() {
        
        TransientMemoryCacheManager.getSingleton().linkCache(this, m_rsm);
            
        final int loadingId = setLoading();
        final long logStartTimelocal = System.currentTimeMillis();
        List<PTMDataset> ptmDS = new ArrayList<>();
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {                   
                m_logger.debug("DataBoxPTMClusters : **** Callback task "+taskId+", success "+success+", finished "+finished+"; with subtask : "+(subTask != null)+". Duration: "+(System.currentTimeMillis()-logStartTimelocal)+" TimeMillis"); 
                if(success) {
                    if(subTask == null){
                        //Main task callback!
                        m_ptmDataset = ptmDS.get(0);
                        m_logger.debug("  -- created "+m_ptmDataset.getPTMClusters().size()+" PTMCluster.");
                        m_loadPepMatchOnGoing=true;
                        ((PTMClustersProteinPanel) getDataBoxPanelInterface()).setData(taskId, (ArrayList) m_ptmDataset.getPTMClusters(), finished);
                        loadPeptideMatches();

                    }
                } else{
                    displayLoadError(taskId, finished);
                }

                if (finished) {
                    setLoaded(loadingId);
                    unregisterTask(taskId);
                    m_logger.debug(" Task "+taskId+" DONE. Should propagate changes ");
                    propagateDataChanged(ExtendedTableModelInterface.class);
                }
            }
        };


        // ask asynchronous loading of data
        
        DatabasePTMsTask task = new DatabasePTMsTask(callback);
        task.initLoadPTMDataset(getProjectId(), m_ptmDataset.getDataset(), ptmDS);       
        m_logger.debug("DataBoxPTMClusters : **** Register task DatabasePTMsTask.initLoadPTMDataset. ID= "+task.getId());
        registerTask(task);
       
          
    }

    private void displayLoadError(long taskId, boolean isFinished){
        TaskInfo ti =  getTaskInfo(taskId);
        String message = (ti != null && ti.hasTaskError()) ? ti.getTaskError().getErrorText() : "Error loading PTM Cluster";
        JOptionPane.showMessageDialog(((JPanel) getDataBoxPanelInterface()), message,"PTM Cluster loading error", JOptionPane.ERROR_MESSAGE);
        ((PTMClustersProteinPanel) getDataBoxPanelInterface()).setData(taskId, null, isFinished);

    }
    
    private boolean m_loadPepMatchOnGoing = false;
    private void loadPeptideMatches(){
                
        final int loadingId = setLoading();
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, final long taskId, SubTask subTask, boolean finished) {
                m_logger.debug("DataBoxPTMClusters : **** Callback task "+taskId+", success "+success+", finished "+finished+"; with subtask : "+subTask+". Duration: "+(System.currentTimeMillis()-logStartTime)+" TimeMillis");
                if(!success){
                    displayLoadError(taskId, finished);
                }
                
                if (finished) {
                    m_loadPepMatchOnGoing = false;
                    m_logger.debug(" Task "+taskId+" DONE. Should propagate changes or get Xic DATA ");
                    m_ptmDataset.updateParentPTMPeptideInstanceClusters();
                    if(isXicResult()){
                        loadXicData(loadingId);
                    } else {
                        setLoaded(loadingId);                        
                        propagateDataChanged(PTMPeptideInstance.class);
                        propagateDataChanged(ExtendedTableModelInterface.class);
                    }
                    unregisterTask(taskId);
                }
            }
        };
        
        DatabasePTMsTask task = new DatabasePTMsTask(callback);
        task.initFillPTMSites(getProjectId(), m_ptmDataset, m_ptmDataset.getPTMSites());
        logStartTime = System.currentTimeMillis();
        m_logger.debug("DataBoxPTMClusters : **** Register task DatabasePTMsTask.initFillPTMSites. ID= " +task.getId());
        registerTask(task);        
    }
    
    public void loadXicData(int loadingId) {
        List<DMasterQuantProteinSet> m_masterQuantProteinSetList = new ArrayList();
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, final long taskId, SubTask subTask, boolean finished) {               
                m_logger.debug("DataBoxPTMClusters : **** Callback task "+taskId+", success "+success+", finished "+finished+"; with subtask : "+subTask+"; found "+m_masterQuantProteinSetList.size()+" mqPrS Duration: "+(System.currentTimeMillis()-logStartTime)+" TimeMillis");                
                if (subTask == null) {
                    //Do at Main task return. 
                    m_quantChannelInfo = new QuantChannelInfo(m_ptmDataset.getDataset());
                    getDataBoxPanelInterface().addSingleValue(m_quantChannelInfo);
                    AbstractDatabaseCallback mapCallback = new AbstractDatabaseCallback() {

                        @Override
                        public boolean mustBeCalledInAWT() {
                            return true;
                        }

                        @Override
                        public void run(boolean success, long task2Id, SubTask subTask, boolean finished) {
                            m_logger.info("**** +++ END task "+task2Id+" if finished ? "+finished);
                            if (finished) {                                                               
//                                m_quantChannelInfo = new QuantChannelInfo(m_ptmDataset.getDataset());
//                                getDataBoxPanelInterface().addSingleValue(m_quantChannelInfo);
                                unregisterTask(task2Id);                                
                            }
                        }

                    };
                    // ask asynchronous loading of data
                    DatabaseLoadLcMSTask taskMap = new DatabaseLoadLcMSTask(mapCallback);
                    taskMap.initLoadAlignmentForXic(getProjectId(), m_ptmDataset.getDataset());
                    m_logger.info("DataBoxPTMClusters **** +++ Register task DatabaseLoadLcMSTask.initLoadAlignmentForXic "+taskMap.getId());
                    registerTask(taskMap);

                }

                if (finished) {
                    m_logger.info(" **** +++ Unregister "+taskId+" + loadProteinMatchMapping + propagate and set Loaded +dataUpdated");
                    setLoaded(loadingId);                    
                    Map<Long, Long> typicalProteinMatchIdByProteinMatchId = loadProteinMatchMapping();
                    m_ptmDataset.setQuantProteinSets(m_masterQuantProteinSetList, typicalProteinMatchIdByProteinMatchId);
                    //((PTMClustersProteinPanel) getDataBoxPanelInterface()).dataUpdated(subTask, finished);
                    unregisterTask(taskId);
                    setLoaded(loadingId);
                    startLoadingMasterQuantPeptides(m_ptmDataset.getPTMClusters());
                    propagateDataChanged(ExtendedTableModelInterface.class);
                }
            }
        };

        // ask asynchronous loading of data        
        DatabaseLoadXicMasterQuantTask task = new DatabaseLoadXicMasterQuantTask(callback);
        task.initLoadProteinSets(getProjectId(), m_ptmDataset.getDataset(), m_masterQuantProteinSetList);
        logStartTime = System.currentTimeMillis();        
        m_logger.debug("DataBoxPTMClusters : ****Register task XicMasterQuantTask - initLoadProteinSets. ID = "+task.getId());        
        registerTask(task);
    }    
    

    private Map<Long, Long> loadProteinMatchMapping() {
        Map<Long, Long> typicalProteinMatchIdByProteinMatchId = new HashMap<>();
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(getProjectId()).createEntityManager();
        try {

            entityManagerMSI.getTransaction().begin();

            long start = System.currentTimeMillis();
            // Load Proteins for PeptideMatch
            Query proteinMatchQuery = entityManagerMSI.createQuery("SELECT pspmi.proteinMatch.id, pspmi.proteinSet.representativeProteinMatchId FROM ProteinSetProteinMatchItem pspmi WHERE pspmi.resultSummary.id=:rsmId");
            proteinMatchQuery.setParameter("rsmId", m_ptmDataset.getDataset().getResultSummaryId());
            List<Object[]> resultList = proteinMatchQuery.getResultList();
            Iterator<Object[]> iterator = resultList.iterator();
            
            while (iterator.hasNext()) {
                Object[] cur = iterator.next();
                Long proteinMatchId = (Long) cur[0];
                Long typicalProteinMatchId = (Long) cur[1];
                typicalProteinMatchIdByProteinMatchId.put(proteinMatchId, typicalProteinMatchId);
            }
            
            m_logger.info("Protein match ids map retrieve {} entries in {} ms", typicalProteinMatchIdByProteinMatchId.size(), (System.currentTimeMillis() - start));
            entityManagerMSI.getTransaction().commit();
        } catch (RuntimeException e) {
            try {
                entityManagerMSI.getTransaction().rollback();
            } catch (Exception rollbackException) {
                m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
            }
            m_logger.error(getClass().getSimpleName() + " failed : ", e);
            return null;
        } finally {
            entityManagerMSI.close();
        }
        return typicalProteinMatchIdByProteinMatchId;
    }

    
    private void startLoadingMasterQuantPeptides(final List<PTMCluster> proteinPTMClusters) {
        m_logger.debug("start loading MQPeptides from PTMCluster and compute PTMCluster expression values");
        List<DMasterQuantPeptide> masterQuantPeptideList = new ArrayList();

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, final long taskId, SubTask subTask, boolean finished) {
                if(finished) {
                    unregisterTask(taskId);
                    m_logger.info("MQPeptides loaded !! ");
                    DMasterQuantitationChannel masterQC = proteinPTMClusters.get(0).getPTMDataset().getDataset().getMasterQuantitationChannels().get(0);
                    Map<Long, DMasterQuantPeptide> mqPepById = masterQuantPeptideList.stream().collect(Collectors.toMap(x -> x.getPeptideInstanceId(), x -> x));

                    float[] protAbundances = new float[masterQC.getGroupsCount()];
                    for (PTMCluster currentCluster : proteinPTMClusters) {
                      // by default set to null
                        currentCluster.setExpressionValue(Double.NaN);
                        if (currentCluster.getMasterQuantProteinSet() != null) {

                            // Update DMasterQuantPeptide for bestPtm
                            // Get BestPepMatch Parent DPeptideInstance
                            List<DPeptideInstance> parentPeptideInstances = currentCluster.getParentPeptideInstances();
                            Long bestPepMatchPepId = currentCluster.getBestPeptideMatch().getPeptide().getId();
                            Optional<DPeptideInstance> parentBestPepI = parentPeptideInstances.stream().filter(peI ->bestPepMatchPepId.equals(peI.getPeptideId())).findFirst();
                            if(parentBestPepI.isPresent()) {
                                DMasterQuantPeptide mqPep = mqPepById.get(parentBestPepI.get().getId());
                                currentCluster.setBestQuantPeptideMatch(mqPep);
                            }
                            
                            for (int groupNumber = 0 ; groupNumber < masterQC.getGroupsCount(); groupNumber++) {
                                Stream<DQuantProteinSet> stream = masterQC.getQuantitationChannels(groupNumber+1).stream().map(c -> currentCluster.getMasterQuantProteinSet().getQuantProteinSetByQchIds().get(c.getId())).filter(q -> q!= null);
                                protAbundances[groupNumber] = stream.collect(Collectors.averagingDouble(dqps -> dqps.getAbundance())).floatValue();
                            }

                            
                            List<Double> expressionValues = new ArrayList<>(parentPeptideInstances.size());

                            for (DPeptideInstance pep : parentPeptideInstances) {
                                DMasterQuantPeptide mqPep = mqPepById.get(pep.getId());
                                if (mqPep != null) {
                                    float[] pepAbundances = new float[masterQC.getGroupsCount()];
                                    for (int groupNumber = 0 ; groupNumber < masterQC.getGroupsCount(); groupNumber++) {
                                        Stream<DQuantPeptide> stream = masterQC.getQuantitationChannels(groupNumber+1).stream().map(c -> mqPep.getQuantPeptideByQchIds().get(c.getId())).filter(q -> q!= null);
                                        pepAbundances[groupNumber] = stream.collect(Collectors.averagingDouble(dqp -> dqp.getAbundance())).floatValue();
                                    }
                                    // Compute an expression metric from protAbundances and pepAbundances and set this value
                                    // to PTMSite expression value
                                    for (int groupNumber = 1 ; groupNumber < masterQC.getGroupsCount(); groupNumber++) {
                                        double pepFC = Math.log(pepAbundances[groupNumber]/pepAbundances[groupNumber-1])/Math.log(2);
                                        double protFC = Math.log(protAbundances[groupNumber]/protAbundances[groupNumber-1])/Math.log(2);
                                        double diffFC = protFC - pepFC;
                                        expressionValues.add(diffFC);
                                    }
                                }
                            }
                            
                            Optional<Double> maxFinite = expressionValues.stream().filter( d -> Double.isFinite(d) ).max(Comparator.comparingDouble(Math::abs));
                            Optional<Double> max = expressionValues.stream().max(Comparator.comparingDouble(Math::abs));

                            if (maxFinite.isPresent()) {
                                if (max.isPresent() && !Double.isNaN(max.get())) {
                                    currentCluster.setExpressionValue(Math.max(maxFinite.get(), max.get()));
                                } else {
                                    currentCluster.setExpressionValue(maxFinite.get());
                                }
                            } else if (max.isPresent()) {
                                currentCluster.setExpressionValue(max.get());
                            }
                        }
                    }
                    //Update Cluster Panel
                    ((PTMClustersProteinPanel) getDataBoxPanelInterface()).dataUpdated(subTask, finished);                    
                }
            }
        };


        DatabaseLoadXicMasterQuantTask task = new DatabaseLoadXicMasterQuantTask(callback);
        List<Long> parentPepInstanceIds = proteinPTMClusters.stream().flatMap(cluster -> cluster.getParentPeptideInstances().stream()).map(pepInstance -> pepInstance.getId()).distinct().collect(Collectors.toList());
        m_logger.info("Loading {} peptideInstances XIC data", parentPepInstanceIds.size());
        task.initLoadPeptides(getProjectId(), m_ptmDataset.getDataset(), parentPepInstanceIds.toArray(new Long[parentPepInstanceIds.size()]), masterQuantPeptideList, true);
        registerTask(task);
    }
    
    
    @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType!= null ) {
            if (parameterType.equals(ResultSummary.class)) {
                return m_rsm;
            }
            
            if (parameterType.equals(DProteinMatch.class)) {
                PTMCluster cluster = ((PTMClustersProteinPanel)getDataBoxPanelInterface()).getSelectedProteinPTMCluster();
                if (cluster != null) {
                    return cluster.getProteinMatch();
                }
            }
            if (parameterType.equals(DPeptideMatch.class)) {
                PTMCluster cluster = ((PTMClustersProteinPanel) getDataBoxPanelInterface()).getSelectedProteinPTMCluster();
                if (cluster != null) {
                    return cluster.getBestPeptideMatch();
                }
            }

            if (parameterType.equals(PTMDataset.class)){
                return m_ptmDataset;
            }
            if (parameterType.equals(DDataset.class)) {
                return m_ptmDataset.getDataset();
            }
             //XIC Specific ---- 
            if(parameterType.equals(DMasterQuantProteinSet.class) && isXicResult()) {
                PTMCluster cluster = ((PTMClustersProteinPanel)getDataBoxPanelInterface()).getSelectedProteinPTMCluster();
                if (cluster != null) {
                    return cluster.getMasterQuantProteinSet();
                }
            }
            if (parameterType.equals(DProteinSet.class) && isXicResult()) {
               PTMCluster cluster = ((PTMClustersProteinPanel)getDataBoxPanelInterface()).getSelectedProteinPTMCluster();
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
            if (parameterType.equals(XicMode.class) && m_ptmDataset.getDataset().isQuantitation()) {
                return new XicMode(true);
            }
        }
        return super.getData(getArray, parameterType);
    }
 
    @Override
    public Object getData(boolean getArray, Class parameterType, boolean isList) {
        if (parameterType != null && isList) {
            if(parameterType.equals(PTMPeptideInstance.class) && !getArray){
                if(m_loadPepMatchOnGoing)
                    return null;
                List<PTMCluster> clusters = ((PTMClustersProteinPanel) getDataBoxPanelInterface()).getSelectedProteinPTMClusters();
                List<PTMPeptideInstance> ptmPeptideInstances =  new ArrayList<>();
                if(!clusters.isEmpty()){
                    Collections.sort(clusters);                    
                    //get First Selected Cluster, and consider only PTMCluster on same protein match
                    Long protMatchId = ((PTMClustersProteinPanel) getDataBoxPanelInterface()).getSelectedProteinPTMCluster().getProteinMatch().getId();
                    clusters.stream().filter(cluster -> protMatchId.equals(cluster.getProteinMatch().getId())).forEach(cluster -> {ptmPeptideInstances.addAll(cluster.getParentPTMPeptideInstances()); });                    
                }
                return ptmPeptideInstances;
//                PTMCluster cluster = ((PTMClustersProteinPanel) getDataBoxPanelInterface()).getSelectedProteinPTMCluster();
//                List<PTMPeptideInstance> ptmPeptideInstances =  new ArrayList<>();
//                if(cluster != null)
//                    ptmPeptideInstances = cluster.getParentPTMPeptideInstances();
//                return ptmPeptideInstances;
            }
            
            //FIXME TODO  !!! VDS BIG WART !!! TO BE REMOVED WITH Propagate refactoring
            // Use "getArray" to specify parent or leaf PTMPeptideInstance...
            if(parameterType.equals(PTMPeptideInstance.class) && getArray){
                if(m_loadPepMatchOnGoing)
                    return null;
                List<PTMCluster> clusters = ((PTMClustersProteinPanel) getDataBoxPanelInterface()).getSelectedProteinPTMClusters();
                List<PTMPeptideInstance> ptmPeptideInstances =  new ArrayList<>();
                if(!clusters.isEmpty()) { 
                    //get First Selected Cluster, and consider only PTMCluster on same protein match
                    Long protMatchId = ((PTMClustersProteinPanel) getDataBoxPanelInterface()).getSelectedProteinPTMCluster().getProteinMatch().getId();
                    clusters.stream().filter(cluster -> protMatchId.equals(cluster.getProteinMatch().getId())).forEach(cluster -> {ptmPeptideInstances.addAll(cluster.getLeafPTMPeptideInstances()); });               
                }
                return ptmPeptideInstances;  
//                PTMCluster cluster = ((PTMClustersProteinPanel) getDataBoxPanelInterface()).getSelectedProteinPTMCluster();
//                List<PTMPeptideInstance> ptmPeptideInstances =  new ArrayList<>();
//                if(cluster != null)
//                    ptmPeptideInstances = cluster.getLeafPTMPeptideInstances();
//                return ptmPeptideInstances;
            } 
        }
        return super.getData(getArray, parameterType, isList);
    }
            
    @Override
    public void setEntryData(Object data) {
        
        getDataBoxPanelInterface().addSingleValue(data);
        if(DDataset.class.isInstance(data)){
            m_ptmDataset = new PTMDataset((DDataset)data);
            //Test if rsm is loaded
            if(m_ptmDataset.getDataset().getResultSummary() == null ){
                // we have to load the RSM/RS
                AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                    @Override
                    public boolean mustBeCalledInAWT() {
                        return true;
                    }

                    @Override
                    public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                        m_rsm = m_ptmDataset.getDataset().getResultSummary(); //facilitation === PTM Ident
                        dataChanged();
                    }
                };

                // ask asynchronous loading of data
                DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
                task.initLoadRsetAndRsm((DDataset)data);
                AccessDatabaseThread.getAccessDatabaseThread().addTask(task);            
            } else {
                m_rsm = m_ptmDataset.getDataset().getResultSummary(); //facilitation === PTM Ident
                dataChanged();
            }
        } else if(PTMDataset.class.isInstance(data)){
            m_ptmDataset = (PTMDataset) data;
            if(m_ptmDataset.getDataset().getResultSummary() == null ){
                throw new IllegalArgumentException("PTMDataset not associated to a valid DDataset (empty RSM).");
            }
            m_rsm = m_ptmDataset.getDataset().getResultSummary(); //facilitation === PTM Ident
            
            dataChanged();
        } else 
            throw new IllegalArgumentException("Unsupported type "+data.getClass()+": DDataset or PTMDataset expected.");               
    }

    @Override
    public Class[] getImportantInParameterClass() {
        if(isXicResult()){
            Class[] classList = {DProteinMatch.class, DMasterQuantProteinSet.class};
            return classList;
        }else{
            Class[] classList = {DProteinMatch.class};        
            return classList;
        }
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
