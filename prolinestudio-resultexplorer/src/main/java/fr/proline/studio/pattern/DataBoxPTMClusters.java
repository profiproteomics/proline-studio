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

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;
import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.util.DStoreCustomPoolConnectorFactory;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.tasks.*;
import fr.proline.studio.dam.tasks.data.ptm.*;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadLcMSTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.rsmexplorer.DataBoxViewerManager;
import fr.proline.studio.rsmexplorer.gui.PTMClustersPanel;
import fr.proline.studio.rsmexplorer.gui.xic.QuantChannelInfo;
import fr.proline.studio.types.XicMode;

import java.util.*;
import java.util.stream.Collectors;
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

    private boolean m_loadSitesAsClusters;
    private boolean m_isAnnotatedData;
    private long logStartTime;

    private PTMDatasetPair m_ptmDatasetPair;
    private DDataset m_datasetSet;
    private ResultSummary m_rsm;
    private QuantChannelInfo m_quantChannelInfo; //Xic Specific
    private boolean m_isXicResult = false; //If false: display Ident result PTM Clusters

    private  boolean m_shouldBeSaved = false; //Indicates data of this Databox are not saved in datastore;

    public DataBoxPTMClusters() {
        this(false, false);
    }

    public DataBoxPTMClusters(boolean viewSitesAsClusters) {
        this(viewSitesAsClusters, false);
    }

    public DataBoxPTMClusters(boolean viewSitesAsClusters, boolean isAnnotatedData) {
        super(viewSitesAsClusters ? DataboxType.DataBoxPTMSiteAsClusters : DataboxType.DataBoxPTMClusters, AbstractDataBox.DataboxStyle.STYLE_RSM);
        m_loadSitesAsClusters = viewSitesAsClusters;
        m_isAnnotatedData = isAnnotatedData;

       // Name of this databox
        m_typeName = viewSitesAsClusters ?  (isAnnotatedData ? "Annotated PTMs Sites" : "Dataset PTMs Sites") :  (isAnnotatedData ? "Annotated PTMs Clusters" : "Dataset PTMs Clusters"); //May be Quant PTM Protein Sites...
        m_description = "Clusters of Modification Sites of a Dataset";//May be Ident or Quant dataset...
        
        // Register in parameters
        ParameterList inParameter = new ParameterList();
        
        inParameter.addParameter(PTMDataset.class);
        inParameter.addParameter(DDataset.class);
        
        registerInParameter(inParameter);
        
        // Register possible out parameters
        ParameterList outParameter = new ParameterList();
        outParameter.addParameter(ResultSummary.class);

        outParameter.addParameter(PTMPeptideInstance.class, ParameterSubtypeEnum.LEAF_PTMPeptideInstance);
        outParameter.addParameter(PTMPeptideInstance.class, ParameterSubtypeEnum.PARENT_PTMPeptideInstance);
        outParameter.addParameter(PTMDataset.class);
        outParameter.addParameter(PTMDatasetPair.class);

        outParameter.addParameter(DProteinMatch.class); 
        outParameter.addParameter(DPeptideMatch.class);
        
        outParameter.addParameter(PTMCluster.class, ParameterSubtypeEnum.LIST_DATA);

        outParameter.addParameter(ExtendedTableModelInterface.class);
        outParameter.addParameter(CrossSelectionInterface.class);
        
        registerOutParameter(outParameter);
                        
    }


    private boolean isXicResult() {
        return m_isXicResult;
    }
    
    public void setXicResult(boolean isXICResult) {
        m_isXicResult = isXICResult;
        m_style = (m_isXicResult) ? DataboxStyle.STYLE_XIC : DataboxStyle.STYLE_RSM;
        if (m_isXicResult) {
            registerXicOutParameter();
        }
    }
    
    private void registerXicOutParameter(){
        ParameterList outParameter = new ParameterList();
        
        outParameter.addParameter(DMasterQuantProteinSet.class);
        outParameter.addParameter(DProteinSet.class);
        outParameter.addParameter(QuantChannelInfo.class);
        
        registerOutParameter(outParameter);
    }
    
    @Override
    public Long getRsetId() {
        if (m_ptmDatasetPair == null) {
            return null;
        }
        return m_datasetSet.getResultSetId();
    }

    @Override
    public Long getRsmId() {
        if (m_ptmDatasetPair == null) {
            return null;
        }
        return m_datasetSet.getResultSummaryId();
    }
    
    @Override
    public void createPanel() {
        PTMClustersPanel p = new PTMClustersPanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }


    @Override
    public void dataMustBeRecalculated(Long rsetId, Long rsmId, Class dataType, ArrayList modificationsList, byte reason) {
        if(! ( isDataOfInterest(rsetId, rsmId, dataType) && dataType.equals(PTMCluster.class)))
            return;

        DataBoxViewerManager.REASON_MODIF reasonModif = DataBoxViewerManager.REASON_MODIF.getReasonModifFor(reason);
        if(reasonModif == null)
            return;
        switch (reasonModif){
            case REASON_PTMCLUSTER_MERGED :
            case REASON_PTMCLUSTER_MODIFIED:
                m_shouldBeSaved = true;
                break;

            case REASON_PTMDATASET_SAVED:
                m_shouldBeSaved = false;
                break;

            case REASON_PEPTIDE_SUPPRESSED:
                m_shouldBeSaved = true;
                addDataChanged(PTMPeptideInstance.class, null);
                propagateDataChanged();
                break;
        }
        m_ptmDatasetPair.setShouldSavePTMDataset(m_shouldBeSaved);
    }

    public boolean isClosable(){
        return !m_shouldBeSaved;
    }

    public String getClosingWarningMessage(){
        if(m_shouldBeSaved)
            return "Annotated PTM Dataset has not been saved....";
        return "";
    }

    //VDS TODO: Allow real inParameter: Actually this view works only as Top View ! Data set using setEntryData
    @Override
    public void dataChanged() {

        // register the link to the Transient Data
        linkCache(m_rsm);
            
        final int loadingId = setLoading();
        final long logStartTimelocal = System.currentTimeMillis();
        List<PTMDatasetPair> ptmDSSet = new ArrayList<>();
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

                        m_ptmDatasetPair = ptmDSSet.get(0);
                        m_shouldBeSaved = false;
                        m_logger.debug("  -- created "+getPTMDatasetToView().getPTMClusters().size()+" PTMCluster.");
                        m_loadPepMatchOnGoing=true;
                        ((PTMClustersPanel) getDataBoxPanelInterface()).setData(taskId, (ArrayList<PTMCluster>) getPTMDatasetToView().getPTMClusters(), finished);
                        loadPeptideMatches();
                    } else {
                        ((PTMClustersPanel) getDataBoxPanelInterface()).dataUpdated(subTask, finished);
                    }
                } else{
                    displayLoadError(taskId, finished);
                }

                if (finished) {
                    setLoaded(loadingId);
                    unregisterTask(taskId);
                    if(m_isAnnotatedData)
                        DatabaseDataManager.getDatabaseDataManager().addLoadedAnnotatedPTMDatasetSet(ptmDSSet.get(0));
                    else
                        DatabaseDataManager.getDatabaseDataManager().addLoadedPTMDatasetSet(ptmDSSet.get(0));
                    m_logger.debug(" Task "+taskId+" DONE. Should propagate changes ");
                    addDataChanged(ExtendedTableModelInterface.class);
                    propagateDataChanged();
                }
            }
        };


        // ask asynchronous loading of data

        DatabaseDatasetPTMsTask task = new DatabaseDatasetPTMsTask(callback);
        task.initLoadPTMDataset(getProjectId(), m_datasetSet, ptmDSSet, m_isAnnotatedData);
        m_logger.debug("DataBoxPTMClusters : **** Register task DatabasePTMsTask.initLoadPTMDataset. ID= "+task.getId());
        registerTask(task);
          
    }

    private void displayLoadError(long taskId, boolean isFinished){
        TaskInfo ti =  getTaskInfo(taskId);
        
        TaskError taskError = ti.getTaskError();
        if (taskError != null) {
            if (DatabaseDatasetPTMsTask.ERROR_PTM_CLUSTER_LOADING.equals(taskError.getErrorTitle()) ) {
                JOptionPane.showMessageDialog(((JPanel) getDataBoxPanelInterface()), "To display Modification Sites or Modification Clusters, you must run \"Identify Modification Sites\" beforehand.", taskError.getErrorTitle(), JOptionPane.WARNING_MESSAGE);      
            } else {
                JOptionPane.showMessageDialog(((JPanel) getDataBoxPanelInterface()), taskError.getErrorText(), DatabaseDatasetPTMsTask.ERROR_PTM_CLUSTER_LOADING, JOptionPane.ERROR_MESSAGE);
            }
            
        }

        ((PTMClustersPanel) getDataBoxPanelInterface()).setData(taskId, null, isFinished);

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
                    m_ptmDatasetPair.updateParentPTMPeptideInstanceClusters();
                    unregisterTask(taskId);


                    if(isXicResult()){
                        loadXicData(loadingId);
                    } else {
                        setLoaded(loadingId);                        
                        ((PTMClustersPanel) getDataBoxPanelInterface()).dataUpdated(subTask, finished);
                        addDataChanged(PTMPeptideInstance.class, null);  //JPM.DATABOX : put null, because I don't know which subtype has been change : null means all. So it works as previously
                        addDataChanged(ExtendedTableModelInterface.class);
                        propagateDataChanged();
                    }
                    unregisterTask(taskId);
                }
            }
        };
        
        DatabaseDatasetPTMsTask task = new DatabaseDatasetPTMsTask(callback);
        List<PTMSite> allSites = new ArrayList<>(m_ptmDatasetPair.getClusterPTMDataset().getPTMSites());
        allSites.addAll(m_ptmDatasetPair.getSitePTMDataset().getPTMSites());
        task.initFillPTMSites(getProjectId(), m_ptmDatasetPair, allSites);
        logStartTime = System.currentTimeMillis();
        m_logger.debug("DataBoxPTMClusters : **** Register task DatabasePTMsTask.initFillPTMSites. ID= " +task.getId());
        registerTask(task);        
    }
    
    private void loadXicData(int loadingId) {
        List<DMasterQuantProteinSet> masterQuantProteinSetList = new ArrayList<>();

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, final long taskId, SubTask subTask, boolean finished) {               
                m_logger.debug("DataBoxPTMClusters : **** Callback task "+taskId+", success "+success+", finished "+finished+"; with subtask : "+subTask+"; found "+masterQuantProteinSetList.size()+" mqPrS Duration: "+(System.currentTimeMillis()-logStartTime)+" TimeMillis");
                if (subTask == null) {
                    //Do at Main task return. 
                    m_quantChannelInfo = new QuantChannelInfo(m_datasetSet);
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
                                unregisterTask(task2Id);                                
                            }
                        }

                    };
                    // ask asynchronous loading of data
                    DatabaseLoadLcMSTask taskMap = new DatabaseLoadLcMSTask(mapCallback);
                    taskMap.initLoadAlignmentForXic(getProjectId(), m_datasetSet);
                    m_logger.info("DataBoxPTMClusters **** +++ Register task DatabaseLoadLcMSTask.initLoadAlignmentForXic "+taskMap.getId());
                    registerTask(taskMap);

                }

                if (finished) {
                    m_logger.info(" **** +++ Unregister "+taskId+" + loadProteinMatchMapping + propagate and set Loaded");
                    setLoaded(loadingId);                    
                    Map<Long, Long> typicalProteinMatchIdByProteinMatchId = loadProteinMatchMapping();
                    m_ptmDatasetPair.getClusterPTMDataset().setQuantProteinSets(masterQuantProteinSetList, typicalProteinMatchIdByProteinMatchId);
                    m_ptmDatasetPair.getSitePTMDataset().setQuantProteinSets(masterQuantProteinSetList, typicalProteinMatchIdByProteinMatchId);
                    unregisterTask(taskId);
                    List<PTMCluster> allClusters =  new ArrayList<>(m_ptmDatasetPair.getClusterPTMDataset().getPTMClusters());
                    allClusters.addAll(m_ptmDatasetPair.getSitePTMDataset().getPTMClusters());
                    startLoadingMasterQuantPeptides(allClusters);
                    addDataChanged(ExtendedTableModelInterface.class);
                    propagateDataChanged();
                }
            }
        };

        // ask asynchronous loading of data        
        DatabaseLoadXicMasterQuantTask task = new DatabaseLoadXicMasterQuantTask(callback);
        task.initLoadProteinSets(getProjectId(), m_datasetSet, masterQuantProteinSetList);
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
            proteinMatchQuery.setParameter("rsmId", m_datasetSet.getResultSummaryId());
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
        List<DMasterQuantPeptide> masterQuantPeptideList = new ArrayList<>();

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
                    Map<Long, DMasterQuantPeptide> mqPepByPepInstId = masterQuantPeptideList.stream().collect(Collectors.toMap(DMasterQuantPeptide::getPeptideInstanceId, x -> x));

                    for (PTMCluster currentCluster : proteinPTMClusters) {
                        currentCluster.setRepresentativeMQPepMatch(getPTMDatasetToView().getRepresentativeMQPeptideForCluster(currentCluster, mqPepByPepInstId));
                    }

                    //Update Cluster Panel
                    ((PTMClustersPanel) getDataBoxPanelInterface()).dataUpdated(subTask, finished);
                }
            }
        };


        DatabaseLoadXicMasterQuantTask task = new DatabaseLoadXicMasterQuantTask(callback);
        List<Long> parentPepInstanceIds = proteinPTMClusters.stream().flatMap(cluster -> cluster.getParentPeptideInstances().stream()).map(DPeptideInstance::getId).distinct().collect(Collectors.toList());
        //VDS To complete parentPepInstanceIds with both PTMDataset ?!
        m_logger.info("Loading {} peptideInstances XIC data", parentPepInstanceIds.size());
        task.initLoadPeptides(getProjectId(),m_datasetSet, parentPepInstanceIds.toArray(new Long[0]), masterQuantPeptideList, true);
        registerTask(task);
    }
    
    
    @Override
    public Object getDataImpl(Class parameterType, ParameterSubtypeEnum parameterSubtype) {
        if (parameterType!= null ) {
            
            if (parameterSubtype == ParameterSubtypeEnum.SINGLE_DATA) {

                if (parameterType.equals(ResultSummary.class)) {
                    return m_rsm;
                }

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

                if (parameterType.equals(PTMDataset.class)) {
                    return getPTMDatasetToView();
                }
                if (parameterType.equals(DDataset.class)) {
                    return m_datasetSet;
                }
                if(parameterType.equals(PTMDatasetPair.class)){
                    return m_ptmDatasetPair;
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
                if (parameterType.equals(XicMode.class) && m_datasetSet.isQuantitation()) {
                    return new XicMode(true);
                }
            }

            if (parameterSubtype == ParameterSubtypeEnum.LIST_DATA) {
                if (parameterType.equals(PTMCluster.class)) {
                    return ((PTMClustersPanel) getDataBoxPanelInterface()).getSelectedPTMClusters();
                }
            }
            

            if (parameterType.equals(PTMPeptideInstance.class) && (parameterSubtype.equals(ParameterSubtypeEnum.PARENT_PTMPeptideInstance) || parameterSubtype.equals(ParameterSubtypeEnum.LEAF_PTMPeptideInstance)) ) {
                if (m_loadPepMatchOnGoing) {
                    return null;
                }
                boolean parentPTMPeptideInstance = parameterSubtype.equals(ParameterSubtypeEnum.PARENT_PTMPeptideInstance);
                
                List<PTMCluster> clusters = ((PTMClustersPanel) getDataBoxPanelInterface()).getSelectedPTMClusters();
                List<PTMPeptideInstance> ptmPeptideInstances =  new ArrayList<>();
                if (!clusters.isEmpty()) {
                    Collections.sort(clusters);                    
                    //get First Selected Cluster, and consider only PTMCluster on same protein match
                    Long protMatchId = ((PTMClustersPanel) getDataBoxPanelInterface()).getSelectedProteinPTMCluster().getProteinMatch().getId();
                    clusters.stream().filter(cluster -> protMatchId.equals(cluster.getProteinMatch().getId())).forEach(cluster -> {
                        ptmPeptideInstances.addAll(parentPTMPeptideInstance ? cluster.getParentPTMPeptideInstances() : cluster.getLeafPTMPeptideInstances());
                    });
                }
                return ptmPeptideInstances;
            }

          
        }
        
        return super.getDataImpl(parameterType, parameterSubtype);
    }

    private PTMDataset getPTMDatasetToView(){
        if(m_loadSitesAsClusters)
            return m_ptmDatasetPair.getSitePTMDataset();
        else
            return m_ptmDatasetPair.getClusterPTMDataset();
    }

    @Override
    public void setEntryData(Object data) {
        
        getDataBoxPanelInterface().addSingleValue(data);
        if(data instanceof DDataset){
            //Test if PTMDataset already loaded
            m_datasetSet = (DDataset)data;
            m_ptmDatasetPair = m_isAnnotatedData ?  DatabaseDataManager.getDatabaseDataManager().getAnnotatedPTMDatasetSetForDS(m_datasetSet.getId() ) :  DatabaseDataManager.getDatabaseDataManager().getPTMDatasetSetForDS( m_datasetSet.getId() );

            //Not yet loaded. Init dataset if needed before loading all data =>  dataChanged();
            if( m_ptmDatasetPair == null){

                //Test if rsm is loaded
                if (m_datasetSet.getResultSummary() == null) {

                    // we have to load the RSM/RS
                    AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                        @Override
                        public boolean mustBeCalledInAWT() {
                            return true;
                        }

                        @Override
                        public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                            m_rsm = ((DDataset) data).getResultSummary(); //facilitation === PTM Ident
                            dataChanged();
                        }
                    };

                    // ask asynchronous loading of data
                    DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
                    task.initLoadRsetAndRsm((DDataset) data);
                    AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
                } else {
                    //RSM already loaded.
                    m_rsm = ((DDataset) data).getResultSummary(); //facilitation === PTM Ident
                    dataChanged();
                }

                // End PTMDataset is not defined
            } else {
                //PTMDataset exist.
                m_rsm = m_datasetSet.getResultSummary();
                if(m_ptmDatasetPair.shouldSavePTMDataset())
                    m_shouldBeSaved = true;

                // Verfiy if PTMDataset has quantitation data (if needed)
                if(isXicResult()) {
                    boolean allDataLoaded = getPTMDatasetToView().isQuantDataLoaded();
                    if(allDataLoaded) {
                        //data already loaded. init param and load associated panel
                        m_quantChannelInfo = new QuantChannelInfo(m_datasetSet);
                        getDataBoxPanelInterface().addSingleValue(m_quantChannelInfo);
                        ((PTMClustersPanel) getDataBoxPanelInterface()).setData(-1L, (ArrayList<PTMCluster>) ((PTMDataset) data).getPTMClusters(), false);
                        ((PTMClustersPanel) getDataBoxPanelInterface()).dataUpdated(null, true);
                    } else {
                        //load XIC data
                        final int loadingId = setLoading();
                        loadXicData(loadingId);
                    }
                }  else {
                    //Identification PTMDataset
                    //load associated panel
                    ((PTMClustersPanel) getDataBoxPanelInterface()).setData(-1L, (ArrayList<PTMCluster>) ((PTMDataset) data).getPTMClusters(), false);
                    ((PTMClustersPanel) getDataBoxPanelInterface()).dataUpdated(null, true);
                }

            } //END IF/Else PTMDataset exist

        } else if(data instanceof PTMDataset){ //specified data is not DDataset, check if it is a PTMDataset

            // If PTMDataset is specified it should be initialized ...
            m_ptmDatasetPair = m_isAnnotatedData ?   DatabaseDataManager.getDatabaseDataManager().getAnnotatedPTMDatasetSetForDS( ((PTMDataset) data).getDataset().getId() ) : DatabaseDataManager.getDatabaseDataManager().getPTMDatasetSetForDS( ((PTMDataset) data).getDataset().getId() );
            if(m_ptmDatasetPair == null || (!m_ptmDatasetPair.getClusterPTMDataset().equals(data) && !m_ptmDatasetPair.getSitePTMDataset().equals(data)))
                throw new IllegalArgumentException("Invalid specified PTMDataset.");

            //set parameter before loading associated panel
            m_datasetSet = m_ptmDatasetPair.getDataset();
            m_rsm = m_datasetSet.getResultSummary();

            // Verfiy if PTMDataset has quantitation data (if needed)
            if(isXicResult()) {
                boolean allDataLoaded = getPTMDatasetToView().isQuantDataLoaded();
                if(allDataLoaded) {
                    //data already loaded. init param and load associated panel
                    m_quantChannelInfo = new QuantChannelInfo(m_datasetSet);
                    getDataBoxPanelInterface().addSingleValue(m_quantChannelInfo);
                    ((PTMClustersPanel) getDataBoxPanelInterface()).setData(-1L, (ArrayList<PTMCluster>) ((PTMDataset) data).getPTMClusters(), false);
                    ((PTMClustersPanel) getDataBoxPanelInterface()).dataUpdated(null, true);
                } else {
                    //load XIC data
                    final int loadingId = setLoading();
                    loadXicData(loadingId);
                }
            }  else {
                //Identification PTMDataset
                //load associated panel
                ((PTMClustersPanel) getDataBoxPanelInterface()).setData(-1L, (ArrayList<PTMCluster>) ((PTMDataset) data).getPTMClusters(), false);
                ((PTMClustersPanel) getDataBoxPanelInterface()).dataUpdated(null, true);
            }

        } else  //specified data is neither a DDataset nor PTMDataset
            throw new IllegalArgumentException("Unsupported type "+data.getClass()+": DDataset or PTMDataset expected.");               
    }

    @Override
    public Class[] getDataboxNavigationOutParameterClasses() {
        if(isXicResult()){
            return new Class[]{DProteinMatch.class, PTMDatasetPair.class, DMasterQuantProteinSet.class};
        }else{
            return new Class[]{DProteinMatch.class, PTMDatasetPair.class};
        }
    }
    
    @Override
    public String getDataboxNavigationDisplayValue() {
        List<PTMCluster> selectedCl = ((PTMClustersPanel) getDataBoxPanelInterface()).getSelectedPTMClusters();
        if(selectedCl == null || selectedCl.isEmpty())
            return null;

        DProteinMatch p = selectedCl.get(0).getProteinMatch();

        int nbrPM = selectedCl.stream().collect( Collectors.groupingBy(PTMCluster::getProteinMatch)).size();

        if(nbrPM>1)
            return p.getAccession()+"+ "+(nbrPM-1);
        else
            return p.getAccession();

    }
    
    
}
