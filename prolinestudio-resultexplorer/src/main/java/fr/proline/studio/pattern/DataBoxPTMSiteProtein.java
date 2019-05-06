package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.*;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DMasterQuantitationChannel;
import fr.proline.core.orm.util.DStoreCustomPoolConnectorFactory;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
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
    
    public DataBoxPTMSiteProtein() { 
        super(AbstractDataBox.DataboxType.DataBoxPTMSiteProtein, AbstractDataBox.DataboxStyle.STYLE_RSM);
        
        // Name of this databox
        m_typeName = "PTM Protein Sites"; //May be Quant PTM Protein Sites... 
        m_description = "PTM Protein Sites of a dataset";//May be Ident or Quant dataset... 

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

        final int loadingId = setLoading();

        final ArrayList<PTMSite> ptmSiteArray = new ArrayList<>();
        
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                m_logger.debug("**** END task "+taskId+" call loadPeptideMatches &  unregisterTask if finished "+finished); 
                if (finished) {
                    m_ptmDataset.setPTMSites(ptmSiteArray);
                    loadPeptideMatches(loadingId, taskId, ptmSiteArray, finished);
                    unregisterTask(taskId);
                }
            }
        };


        // ask asynchronous loading of data
        
        DatabasePTMsTask task = new DatabasePTMsTask(callback);
        task.initLoadPTMSites(getProjectId(), m_ptmDataset.getDataset().getResultSummary(), ptmSiteArray);
        m_logger.debug("**** Register task DatabasePTMsTask.initLoadPTMSites "+task.getId()+" : "+task.toString());
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

                    AbstractDatabaseCallback mapCallback = new AbstractDatabaseCallback() {

                        @Override
                        public boolean mustBeCalledInAWT() {
                            return true;
                        }

                        @Override
                        public void run(boolean success, long task2Id, SubTask subTask, boolean finished) {
                            m_logger.info("**** +++ --- END  task "+task2Id+" if finished ? "+finished+" unregister + set loaded");
                            if (finished) {                                                               
                                m_quantChannelInfo = new QuantChannelInfo(m_ptmDataset.getDataset());
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
                        // by default set to null
                        currentSite.setExpressionValue(Double.NaN);
                        if (currentSite.getMasterQuantProteinSet() != null) {

                            for (int groupNumber = 0 ; groupNumber < masterQC.getGroupsCount(); groupNumber++) {
                                Stream<DQuantProteinSet> stream = masterQC.getQuantitationChannels(groupNumber+1).stream().map(c -> currentSite.getMasterQuantProteinSet().getQuantProteinSetByQchIds().get(c.getId())).filter(q -> q!= null);
                                protAbundances[groupNumber] = stream.collect(Collectors.averagingDouble(dqps -> dqps.getAbundance())).floatValue();
                            }

                            for (DPeptideInstance pep : currentSite.getParentPeptideInstances()) {
                                DMasterQuantPeptide mqPep = mqPepById.get(pep.getId());
                                if (mqPep != null) {
                                    float[] pepAbundances = new float[masterQC.getGroupsCount()];
                                    for (int groupNumber = 0 ; groupNumber < masterQC.getGroupsCount(); groupNumber++) {
                                        Stream<DQuantPeptide> stream = masterQC.getQuantitationChannels(groupNumber+1).stream().map(c -> mqPep.getQuantPeptideByQchIds().get(c.getId())).filter(q -> q!= null);
                                        pepAbundances[groupNumber] = stream.collect(Collectors.averagingDouble(dqp -> dqp.getAbundance())).floatValue();
                                    }
                                    // Compute an expression metric from protAbundances and pepAbundances and set this value
                                    // to PTMSite expression value
                                    double expression = Double.isNaN((Double)currentSite.getExpressionValue()) ? 0.0 : (Double)currentSite.getExpressionValue();
                                    for (int groupNumber = 1 ; groupNumber < masterQC.getGroupsCount(); groupNumber++) {
                                        double pepFC = Math.log(pepAbundances[groupNumber]/pepAbundances[groupNumber-1])/Math.log(2);
                                        double protFC = Math.log(protAbundances[groupNumber]/protAbundances[groupNumber-1])/Math.log(2);
                                        double diffFC = protFC - pepFC;
                                        if (Math.abs(diffFC) > Math.abs(expression)) {
                                            expression = diffFC;
                                        }
                                    }
                                    currentSite.setExpressionValue(expression);
                                }
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

        DatabasePTMsTask task = new DatabasePTMsTask(callback);
        task.initFillPTMSites(getProjectId(), m_ptmDataset.getDataset().getResultSummary(), ptmSiteArray);
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
                if (proteinPtmSite != null) {
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
