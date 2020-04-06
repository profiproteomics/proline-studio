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
import fr.proline.core.orm.msi.dto.*;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DMasterQuantitationChannel;
import fr.proline.core.orm.util.DStoreCustomPoolConnectorFactory;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.memory.TransientMemoryCacheManager;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.DatabasePTMSitesTask;
import fr.proline.studio.dam.tasks.DatabasePTMsTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.data.ptm.PTMDataset;
import fr.proline.studio.dam.tasks.data.ptm.PTMPeptideInstance;
import fr.proline.studio.dam.tasks.data.ptm.PTMSite;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadLcMSTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.rsmexplorer.gui.ProteinPTMSitePanel;

import java.util.*;

import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.rsmexplorer.gui.xic.QuantChannelInfo;
import fr.proline.studio.types.XicMode;

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
 * @author JM235353
 */
public class DataBoxPTMSiteProtein extends AbstractDataBox {

    
    private final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ptm");
    private long logStartTime;
    
    private PTMDataset m_ptmDataset;
    private ResultSummary m_rsm;
    private QuantChannelInfo m_quantChannelInfo; //Xic Specific
    private boolean m_isXicResult = false; //If false: display Ident result PTMSite
    private boolean m_viewPTMResultV2; //If false: display Ident result PTMSit
    
    public DataBoxPTMSiteProtein() { 
        this(false);
    }
    
    public DataBoxPTMSiteProtein(boolean isV2PTMData) { 
        super(AbstractDataBox.DataboxType.DataBoxPTMSiteProtein, AbstractDataBox.DataboxStyle.STYLE_RSM);
        m_viewPTMResultV2 = isV2PTMData;
        
        // Name of this databox
        m_typeName = m_viewPTMResultV2 ? "Sites Protein" : "Sites Proteins(V1)"; //May be Quant PTM Protein Sites... 
        m_description = "Protein view for Modification Sites of a Dataset";//May be Ident or Quant dataset... 

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
        outParameter.addParameter(PTMSite.class, true);
        outParameter.addParameter(ResultSummary.class, false);
        registerOutParameter(outParameter);
        
        outParameter = new GroupParameter();
        outParameter.addParameter(PTMPeptideInstance.class, true);
        outParameter.addParameter(PTMSite.class, true);
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
        ProteinPTMSitePanel p = new ProteinPTMSitePanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }
    
    @Override
    public void dataChanged() {

        // register the link to the Transient Data
        linkCache(m_rsm);
        
        if(m_viewPTMResultV2)
            dataChangedV2();
        else
            dataChangedV1();
    }
    
    public void dataChangedV1() {

        final int loadingId = setLoading();

        final ArrayList<PTMSite> ptmSiteArray = new ArrayList<>();
        
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                m_logger.debug("**** Callback task "+taskId+" call loadPeptideMatches &  unregisterTask if finished : "+finished); 
                if(success){
                    loadPeptideMatches(loadingId, taskId, ptmSiteArray, finished);
                } else{
                    setLoaded(loadingId);
                    TaskInfo ti =  getTaskInfo(taskId);
                    String message = (ti != null && ti.hasTaskError()) ? ti.getTaskError().getErrorText() : "Error loading PTM Site";
                    JOptionPane.showMessageDialog(((JPanel) getDataBoxPanelInterface()), message,"PTM Site loading error", JOptionPane.ERROR_MESSAGE);
                }
                if (finished) {
                    m_ptmDataset.setPTMSites(ptmSiteArray);                    
                    unregisterTask(taskId);
                }
            }
        };

        
        // ask asynchronous loading of data
            
            DatabasePTMSitesTask task = new DatabasePTMSitesTask(callback);
            task.initLoadPTMSites(getProjectId(), m_ptmDataset.getDataset().getResultSummary(), ptmSiteArray);
            m_logger.debug("**** Register task DatabasePTMSitesTask.initLoadPTMSites ID: "+task.getId());
            registerTask(task);

    }
    
    public void dataChangedV2() {
           
        final int loadingId = setLoading();
       
        List<PTMDataset> ptmDS = new ArrayList<>();
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {                   
                m_logger.debug("**** Callback task "+taskId+": subtask = "+subTask+" is finished = "+finished); 
                if(success) {
                    if(subTask == null){
                        //Main task callback!
                        m_ptmDataset = ptmDS.get(0);
                        m_logger.debug(" Add PTMCluster "+ptmDS.get(0).getPTMClusters().size());
                        loadPeptideMatchesV2();

                    } else  {
                        //In subtask, update data! 
                        m_logger.debug(" Should update PTMSites table "+ptmDS.get(0).getPTMSites().size());
                        ((ProteinPTMSitePanel)getDataBoxPanelInterface()).dataUpdated(subTask, finished);                    
                    }   
                } else{                    
                    TaskInfo ti =  getTaskInfo(taskId);
                    String message = (ti != null && ti.hasTaskError()) ? ti.getTaskError().getErrorText() : "Error loading PTM Site";
                    JOptionPane.showMessageDialog(((JPanel) getDataBoxPanelInterface()), message,"PTM Site loading error", JOptionPane.ERROR_MESSAGE);                    
                    ((ProteinPTMSitePanel) getDataBoxPanelInterface()).setData(taskId, null, finished); 
                }
                
                if (finished) {
                    setLoaded(loadingId);
                    unregisterTask(taskId);
                    m_logger.debug(" DONE Should propagate changes ");
                    propagateDataChanged(ExtendedTableModelInterface.class);
                }
            }
        };


        // ask asynchronous loading of data
        
        DatabasePTMsTask task = new DatabasePTMsTask(callback);
        task.initLoadPTMDataset(getProjectId(), m_ptmDataset.getDataset(), ptmDS);
        m_logger.debug("**** Register task DatabasePTMsTask.initLoadPTMDataset ID : "+task.getId());
        registerTask(task); 
    }
    
    public void loadXicData(int loadingId, long taskId, ArrayList<PTMSite> proteinPTMSiteArray, boolean finished) {
        List<DMasterQuantProteinSet> m_masterQuantProteinSetList = new ArrayList();
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, final long taskId, SubTask subTask, boolean finished) {
                m_logger.info(" **** +++ END task  "+taskId+" : is finished ? (unregister) "+finished+"; subtask ? "+subTask+"; sucess ? "+success+"; found "+m_masterQuantProteinSetList.size()+" mqPrS");
                if (subTask == null) {
                    m_quantChannelInfo = new QuantChannelInfo(m_ptmDataset.getDataset());
                    AbstractDatabaseCallback mapCallback = new AbstractDatabaseCallback() {

                        @Override
                        public boolean mustBeCalledInAWT() {
                            return true;
                        }

                        @Override
                        public void run(boolean success, long task2Id, SubTask subTask, boolean finished) {
                            m_logger.info("**** +++ --- END  task "+task2Id+" if finished ? "+finished+" unregister + set loaded");
                            if (finished) {                                                               
//                                m_quantChannelInfo = new QuantChannelInfo(m_ptmDataset.getDataset());
                                getDataBoxPanelInterface().addSingleValue(m_quantChannelInfo);
                                unregisterTask(task2Id);
                                setLoaded(loadingId);
                            }
                        }

                    };
                    // ask asynchronous loading of data
                    DatabaseLoadLcMSTask taskMap = new DatabaseLoadLcMSTask(mapCallback);
                    taskMap.initLoadAlignmentForXic(getProjectId(), m_ptmDataset.getDataset());
                    m_logger.info("**** +++ --- Register taskMap DatabaseLoadLcMSTask.initLoadAlignmentForXic "+taskMap.getId()+" : "+taskMap.toString());
                    registerTask(taskMap);

                } else {
//                    m_logger.info("call PTMProteinSitePanel dataUpdated  ");
//                    ((PTMProteinSitePanel) getDataBoxPanelInterface()).dataUpdated(subTask, finished);
                }

                if (finished) {        
                    m_logger.info(" **** +++ Unregister "+taskId+" loadProteinMatchMapping + propagate and set Loaded ");
                    Map<Long, Long> typicalProteinMatchIdByProteinMatchId = loadProteinMatchMapping();
                    m_ptmDataset.setQuantProteinSets(m_masterQuantProteinSetList, typicalProteinMatchIdByProteinMatchId);
                    m_logger.info("{} mq proteinset assigned", m_masterQuantProteinSetList.size());
                    ((ProteinPTMSitePanel) getDataBoxPanelInterface()).setData(taskId, proteinPTMSiteArray, finished);
                    unregisterTask(taskId);
                    setLoaded(loadingId);
                    startLoadingMasterQuantPeptides(proteinPTMSiteArray);
                    propagateDataChanged(PTMPeptideInstance.class);
                    propagateDataChanged(ExtendedTableModelInterface.class);
                }
            }
        };

        // ask asynchronous loading of data        
        DatabaseLoadXicMasterQuantTask task = new DatabaseLoadXicMasterQuantTask(callback);
        task.initLoadProteinSets(getProjectId(), m_ptmDataset.getDataset(), m_masterQuantProteinSetList);
        m_logger.debug("**** +++ Register task XicMasterQuantTask . initLoadProteinSets "+task.getId()+" : "+task.toString());        
        registerTask(task);

    }


    private void startLoadingMasterQuantPeptides(final ArrayList<PTMSite> proteinPTMSiteArray) {
        m_logger.debug("start loading MQPeptides from PTMSites and compute PTMSite expression values");
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
                    DMasterQuantitationChannel masterQC = proteinPTMSiteArray.get(0).getPTMdataset().getDataset().getMasterQuantitationChannels().get(0);
                    Map<Long, DMasterQuantPeptide> mqPepById = masterQuantPeptideList.stream().collect(Collectors.toMap(x -> x.getPeptideInstanceId(), x -> x));

                    float[] protAbundances = new float[masterQC.getGroupsCount()];
                    for (PTMSite currentSite : proteinPTMSiteArray) {
                        // by default set expression to null
                        currentSite.setExpressionValue(Double.NaN);
                        if (currentSite.getMasterQuantProteinSet() != null) {

                            for (int groupNumber = 0 ; groupNumber < masterQC.getGroupsCount(); groupNumber++) {
                                Stream<DQuantProteinSet> stream = masterQC.getQuantitationChannels(groupNumber+1).stream().map(c -> currentSite.getMasterQuantProteinSet().getQuantProteinSetByQchIds().get(c.getId())).filter(q -> q!= null);
                                protAbundances[groupNumber] = stream.collect(Collectors.averagingDouble(dqps -> dqps.getAbundance())).floatValue();
                            }

                            List<DPeptideInstance> parentPeptideInstances = currentSite.getParentPeptideInstances();
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
                                    currentSite.setExpressionValue(Math.max(maxFinite.get(), max.get()));
                                } else {
                                    currentSite.setExpressionValue(maxFinite.get());
                                }
                            } else {
                                currentSite.setExpressionValue(max.get());
                            }

                        }
                    }
                }
            }
        };


        DatabaseLoadXicMasterQuantTask task = new DatabaseLoadXicMasterQuantTask(callback);
        List<Long> parentPepInstanceIds = proteinPTMSiteArray.stream().flatMap(site -> site.getParentPeptideInstances().stream()).map(pepInstance -> pepInstance.getId()).distinct().collect(Collectors.toList());
        m_logger.info("Loading {} peptideInstances XIC data", parentPepInstanceIds.size());
        task.initLoadPeptides(getProjectId(), m_ptmDataset.getDataset(), parentPepInstanceIds.toArray(new Long[parentPepInstanceIds.size()]), masterQuantPeptideList, true);
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
      
    public void loadPeptideMatches(int loadingId, long taskId, ArrayList<PTMSite> ptmSiteArray, boolean finished) {

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, final long taskId, SubTask subTask, boolean finished) {
                m_logger.debug("**** --- END task "+taskId+ " if finished ("+finished+") call unregister + loadXicData ? "+ m_isXicResult+" duration "+ (System.currentTimeMillis()-logStartTime)+" TimeMillis"); 
                if (finished) {
                    if(isXicResult()){
                        loadXicData(loadingId, taskId, ptmSiteArray, finished);
                    } else {
                        ((ProteinPTMSitePanel) getDataBoxPanelInterface()).setData(taskId, ptmSiteArray, finished);
                        setLoaded(loadingId);
                        propagateDataChanged(ExtendedTableModelInterface.class);
                    }
                    unregisterTask(taskId);
                }
            }
        };

        DatabasePTMSitesTask task = new DatabasePTMSitesTask(callback);
        task.initFillPTMSites(getProjectId(), m_ptmDataset.getDataset().getResultSummary(), ptmSiteArray);
        logStartTime = System.currentTimeMillis();
        m_logger.debug("**** --- Register task DatabasePTMsTask.initFillPTMSites " +task.getId()+" : "+task.toString());
        registerTask(task);

    }
    
    private boolean m_loadPepMatchOnGoing = false;
    private void loadPeptideMatchesV2(){
                
        final int loadingId = setLoading();
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, final long taskId, SubTask subTask, boolean finished) {
                m_logger.debug("**** --- CallBack task "+taskId+ " if finished ("+finished+") subtask = "+ subTask+" duration "+ (System.currentTimeMillis()-logStartTime)+" TimeMillis"); 
                if(subTask == null){
                    //Main task callback!                    
                    m_logger.debug(" PTM Sites PepInstance on going ... first iteration");
                    ((ProteinPTMSitePanel) getDataBoxPanelInterface()).setData(taskId, (ArrayList) m_ptmDataset.getPTMSites(), finished);

                } else  {
                    //In subtask, update data! 
                    m_logger.debug(" PTM Sites PepInstance on going .. subtask");
                    ((ProteinPTMSitePanel) getDataBoxPanelInterface()).dataUpdated(subTask, finished);               
                }   
                if (finished) {         
                    m_loadPepMatchOnGoing = false;
                    if(isXicResult()){
                        loadXicData(loadingId, taskId, (ArrayList) m_ptmDataset.getPTMSites(), finished);
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
        m_loadPepMatchOnGoing = true;
        logStartTime = System.currentTimeMillis();
        m_logger.debug("**** --- Register task DatabasePTMsTask.initFillPTMSites " +task.getId()+" : "+task.toString());
        registerTask(task);        
    }
 
    @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType!= null ) {
            if (parameterType.equals(ResultSummary.class)) {
                return m_rsm;
            }
            
            if (parameterType.equals(DProteinMatch.class)) {
                PTMSite proteinPtmSite = ((ProteinPTMSitePanel)getDataBoxPanelInterface()).getSelectedProteinPTMSite();
                if (proteinPtmSite != null) {
                    return proteinPtmSite.getProteinMatch();
                }
            }
            if (parameterType.equals(DPeptideMatch.class)) {
                PTMSite proteinPtmSite = ((ProteinPTMSitePanel) getDataBoxPanelInterface()).getSelectedProteinPTMSite();
                if (proteinPtmSite != null) {
                    return proteinPtmSite.getBestPeptideMatch();
                }
            }
            if (parameterType.equals(PTMSite.class)) {
                return ((ProteinPTMSitePanel) getDataBoxPanelInterface()).getSelectedProteinPTMSite();
            }
            if (parameterType.equals(PTMDataset.class)){
                return m_ptmDataset;
            }
            if (parameterType.equals(DDataset.class)) {
                return m_ptmDataset.getDataset();
            }
             //XIC Specific ---- 
            if(parameterType.equals(DMasterQuantProteinSet.class) && isXicResult()) {
                PTMSite proteinPtmSite = ((ProteinPTMSitePanel)getDataBoxPanelInterface()).getSelectedProteinPTMSite();
                if (proteinPtmSite != null) {
                    return proteinPtmSite.getMasterQuantProteinSet();
                }
            }
            if (parameterType.equals(DProteinSet.class) && isXicResult()) {
               PTMSite proteinPtmSite = ((ProteinPTMSitePanel)getDataBoxPanelInterface()).getSelectedProteinPTMSite();
                if (proteinPtmSite != null && proteinPtmSite.getMasterQuantProteinSet() != null) {
                    return proteinPtmSite.getMasterQuantProteinSet().getProteinSet();
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
        if(parameterType.equals(PTMPeptideInstance.class) && isList){
        if(m_loadPepMatchOnGoing)
                return null;            
            PTMSite site = ((ProteinPTMSitePanel) getDataBoxPanelInterface()).getSelectedProteinPTMSite();
            List<PTMPeptideInstance> sitePtmPepInstance =  new ArrayList<>();
            if(site != null)
                sitePtmPepInstance = site.getAssociatedPTMPeptideInstances();
            return sitePtmPepInstance;
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
