package fr.proline.studio.dam.tasks.xic;

import fr.proline.core.orm.lcms.Feature;
import fr.proline.core.orm.lcms.Map;
import fr.proline.core.orm.lcms.MapAlignment;
import fr.proline.core.orm.lcms.Peakel;
import fr.proline.core.orm.lcms.ProcessedMap;
import fr.proline.core.orm.msi.MasterQuantPeptideIon;
import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DMasterQuantitationChannel;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.AbstractDatabaseSlicerTask;
import fr.proline.studio.dam.tasks.SubTask;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

/**
 * load LcMS data
 *
 * @author MB243701
 */
public class DatabaseLoadLcMSTask extends AbstractDatabaseSlicerTask {

    // used to slice the task in sub tasks
    private static final int SLICE_SIZE = 300;

    // different possible subtasks
    public static final int SUB_TASK_COUNT_MAP = 0; // <<----- get in sync  
    public static final int SUB_TASK_COUNT_PEAKEL = 1; // <<----- get in sync  
    public static final int SUB_TASK_PEAKEL = 0;
    public static final int SUB_TASK_COUNT_FEATURE = 1; // <<----- get in sync  
    public static final int SUB_TASK_FEATURE = 0;
    public static final int SUB_TASK_COUNT_MASTER_FEATURE = 0; // <<----- get in sync  
    public static final int SUB_TASK_COUNT_CHILD_FEATURE = 0; // <<----- get in sync 
    public static final int SUB_TASK_COUNT_PEAKEL_FOR_FEATURE = 1;// <<----- get in sync  
    public static final int SUB_TASK_PEAKEL_FOR_FEATURE = 0;
    public static final int SUB_TASK_COUNT_MASTER_PEPTIDE_ION_WITH_PEAKELS = 0; // <<----- get in sync  
    public static final int SUB_TASK_MAP_ALIGNMENT = 0; // <<----- get in sync  

    private Long m_projectId;
    private DDataset m_dataset;
    private Map m_map;
    private List<Map> m_mapList;
    private List<Peakel> m_peakelList;
    private List<Feature> m_featureList;
    private DMasterQuantPeptide m_masterQuantPeptide;
    private Feature m_masterFeature;
    private List<Feature> m_masterFeatureList;
    private MasterQuantPeptideIon m_masterQuantPeptideIon;
    private List<Feature> m_childFeatureList;
    private Feature m_childFeature;
    private List<Peakel> m_peakelForChildFeatureList;
    private List<List<Peakel>> m_peakelListPerFeature;
    private DDataset m_dataset_align;
    private List<MapAlignment> m_mapAlignmentList;
    private List<MapAlignment> m_allMapAlignmentList;
    private List<ProcessedMap> m_allMaps;

    // data kept for sub tasks
    private List<Long> m_peakelIds = null;
    private List<Long> m_featureIds = null;
    private List<Long> m_peakelForFeatureIds = null;

    private int action;
    private static final int LOAD_MAP_FOR_XIC = 0;
    private static final int LOAD_PEAKELS_FOR_MAP = 1;
    private static final int LOAD_FEATURES_FOR_MAP = 2;
    private static final int LOAD_MASTER_FEATURE_FOR_PEPTIDE = 3;
    private static final int LOAD_FEATURE_FOR_MASTER = 4;
    private static final int LOAD_PEAKELS_FOR_FEATURE = 5;
    private static final int LOAD_FEATURE_FOR_PEPTIDE_ION_WITH_PEAKELS = 6;
    private static final int LOAD_ALIGNMENT_FOR_XIC = 7;

    public DatabaseLoadLcMSTask(AbstractDatabaseCallback callback) {
        super(callback);
    }

    public void initLoadMapForXic(long projectId, DDataset dataset, List<Map> mapList) {
        init(SUB_TASK_COUNT_MAP, new TaskInfo("Load Maps of XIC " + dataset.getName(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_dataset = dataset;
        m_mapList = mapList;
        action = LOAD_MAP_FOR_XIC;
    }

    public void initLoadPeakelForMap(long projectId, Map map, List<Peakel> peakelList) {
        init(SUB_TASK_COUNT_PEAKEL, new TaskInfo("Load Peakels of Map " + (map == null ? "" : map.getName()), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_map = map;
        m_peakelList = peakelList;
        action = LOAD_PEAKELS_FOR_MAP;
    }

    public void initLoadFeatureForMap(long projectId, Map map, List<Feature> featureList) {
        init(SUB_TASK_COUNT_FEATURE, new TaskInfo("Load Features of Map " + (map == null ? "" : map.getName()), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_map = map;
        m_featureList = featureList;
        action = LOAD_FEATURES_FOR_MAP;
    }

    public void initLoadMasterFeatureForPeptide(long projectId, DMasterQuantPeptide masterQuantPeptide, List<Feature> masterFeatureList) {
        String peptideName = "";
        if (masterQuantPeptide != null && masterQuantPeptide.getPeptideInstance() != null && masterQuantPeptide.getPeptideInstance().getBestPeptideMatch() != null && masterQuantPeptide.getPeptideInstance().getBestPeptideMatch().getPeptide() != null) {
            peptideName = masterQuantPeptide.getPeptideInstance().getBestPeptideMatch().getPeptide().getSequence();
        }
        init(SUB_TASK_COUNT_MASTER_FEATURE, new TaskInfo("Load Master Features for Peptide " + peptideName, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_masterQuantPeptide = masterQuantPeptide;
        m_masterFeatureList = masterFeatureList;
        action = LOAD_MASTER_FEATURE_FOR_PEPTIDE;
    }

    public void initLoadChildFeatureForMasterFeature(long projectId, Feature masterFeature, List<Feature> childFeatureList) {
        init(SUB_TASK_COUNT_CHILD_FEATURE, new TaskInfo("Load Child Features for Master Feature " + (masterFeature == null ? "" : (masterFeature.getElutionTime())), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_masterFeature = masterFeature;
        m_childFeatureList = childFeatureList;
        action = LOAD_FEATURE_FOR_MASTER;
    }

    public void initLoadChildFeatureForPeptideIonWithPeakel(long projectId, MasterQuantPeptideIon masterQuantPeptideIon, List<Feature> childFeatureList, List<List<Peakel>> peakelListPerFeature) {
        init(SUB_TASK_COUNT_CHILD_FEATURE, new TaskInfo("Load Child Features + peakels for PeptideIon "+ (masterQuantPeptideIon == null ? "" : (masterQuantPeptideIon.getId())) , false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_masterQuantPeptideIon = masterQuantPeptideIon;
        m_childFeatureList = childFeatureList;
        m_peakelListPerFeature = peakelListPerFeature;
        action = LOAD_FEATURE_FOR_PEPTIDE_ION_WITH_PEAKELS;
    }

    public void initLoadPeakelForFeature(long projectId, Feature feature, List<Peakel> peakelList) {
        init(SUB_TASK_COUNT_PEAKEL_FOR_FEATURE, new TaskInfo("Load Peakels for Feature ", false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_childFeature = feature;
        m_peakelForChildFeatureList = peakelList;
        action = LOAD_PEAKELS_FOR_FEATURE;
    }
    
    public void initLoadAlignmentForXic(long projectId, DDataset dataset, List<MapAlignment> mapAlignmentList, List<MapAlignment> allMapAlignmentList, List<ProcessedMap> allMapList) {
        init(SUB_TASK_COUNT_MAP, new TaskInfo("Load Map Alignment of XIC " + dataset.getName(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_dataset_align = dataset;
        m_mapAlignmentList = mapAlignmentList;
        m_allMapAlignmentList = allMapAlignmentList;
        m_allMaps = allMapList;
        action = LOAD_ALIGNMENT_FOR_XIC;
    }

    @Override
    // must be implemented for all AbstractDatabaseSlicerTask 
    public void abortTask() {
        super.abortTask();
        switch (action) {
            case LOAD_MAP_FOR_XIC:
                m_mapList = null;
                break;
            case LOAD_PEAKELS_FOR_MAP:
                m_peakelList = null;
                break;
            case LOAD_FEATURES_FOR_MAP:
                m_featureList = null;
                break;
            case LOAD_MASTER_FEATURE_FOR_PEPTIDE:
                m_masterFeatureList = null;
                break;
            case LOAD_FEATURE_FOR_MASTER:
                m_childFeatureList = null;
                break;
            case LOAD_PEAKELS_FOR_FEATURE:
                m_peakelForChildFeatureList = null;
                break;
            case LOAD_FEATURE_FOR_PEPTIDE_ION_WITH_PEAKELS:
                m_childFeatureList = null;
                m_peakelListPerFeature = null;
                break;
            case LOAD_ALIGNMENT_FOR_XIC:
                m_mapAlignmentList = null;
                break;
        }
    }

    @Override
    public boolean fetchData() {
        if (action == LOAD_MAP_FOR_XIC) {
            if (needToFetch()) {
                DatabaseLoadXicMasterQuantTask.fetchDataQuantChannels(m_projectId, m_dataset, m_taskError);
                return fetchDataMainTaskMapForXic();
            }
        } else if (action == LOAD_PEAKELS_FOR_MAP) {
            if (needToFetch()) {
                return fetchDataMainTaskPeakelForMap();
            } else {
                // fetch data of SubTasks
                return fetchPeakelDataSubTask();
            }
        } else if (action == LOAD_FEATURES_FOR_MAP) {
            if (needToFetch()) {
                return fetchDataMainTaskFeatureForMap();
            } else {
                // fetch data of SubTasks
                return fetchFeatureDataSubTask();
            }
        } else if (action == LOAD_MASTER_FEATURE_FOR_PEPTIDE) {
            if (needToFetch()) {
                return fetchDataMainTaskMasterFeatureForPeptide();
            }
        } else if (action == LOAD_FEATURE_FOR_MASTER) {
            if (needToFetch()) {
                return fetchDataMainTaskChildFeatureForMaster();
            }
        } else if (action == LOAD_PEAKELS_FOR_FEATURE) {
            if (needToFetch()) {
                return fetchDataMainTaskPeakelForFeature();
            } else {
                // fetch data of SubTasks
                return fetchPeakelForFeatureDataSubTask();
            }
        } else if (action == LOAD_FEATURE_FOR_PEPTIDE_ION_WITH_PEAKELS) {
            if (needToFetch()) {
                return fetchDataMainTaskChildFeatureForPeptideIonWithPeakels();
            }
        }else if (action == LOAD_ALIGNMENT_FOR_XIC) {
            if (needToFetch()) {
                DatabaseLoadXicMasterQuantTask.fetchDataQuantChannels(m_projectId, m_dataset, m_taskError);
                return fetchDataMainTaskAlignmentForXic();
            }
        } 
        return true; // should not happen
    }

    @Override
    public boolean needToFetch() {
        switch (action) {
            case LOAD_MAP_FOR_XIC:
                return (m_mapList == null || m_mapList.isEmpty());
            case LOAD_PEAKELS_FOR_MAP:
                return (m_peakelList == null || m_peakelList.isEmpty());
            case LOAD_FEATURES_FOR_MAP:
                return (m_featureList == null || m_featureList.isEmpty());
            case LOAD_MASTER_FEATURE_FOR_PEPTIDE:
                return (m_masterFeatureList == null || m_masterFeatureList.isEmpty());
            case LOAD_FEATURE_FOR_MASTER:
                return (m_childFeatureList == null || m_childFeatureList.isEmpty());
            case LOAD_PEAKELS_FOR_FEATURE:
                return (m_peakelForChildFeatureList == null || m_peakelForChildFeatureList.isEmpty());
            case LOAD_FEATURE_FOR_PEPTIDE_ION_WITH_PEAKELS:
                return (m_childFeatureList == null || m_childFeatureList.isEmpty());
            case LOAD_ALIGNMENT_FOR_XIC:
                return (m_mapAlignmentList == null || m_mapAlignmentList.isEmpty());
        }
        return false; // should not happen 
    }

    /**
     * main task to load all maps for a xic
     *
     * @return
     */
    private boolean fetchDataMainTaskMapForXic() {
        EntityManager entityManagerLCMS = DataStoreConnectorFactory.getInstance().getLcMsDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
        try {
            entityManagerLCMS.getTransaction().begin();
            if (m_dataset != null && m_dataset.getMasterQuantitationChannels() != null) {
                List<DMasterQuantitationChannel> listMasterQuantitationChannels = m_dataset.getMasterQuantitationChannels();
                for (DMasterQuantitationChannel masterQuantitationChannel : listMasterQuantitationChannels) {
                    List<DQuantitationChannel> listQuantChannels = masterQuantitationChannel.getQuantitationChannels();
                    if (listQuantChannels != null && !listQuantChannels.isEmpty()) {
                        List<Long> listMapIds = new ArrayList();
                        for (DQuantitationChannel quantitationChannel : listQuantChannels) {
                            listMapIds.add(quantitationChannel.getLcmsMapId());
                        }
                        String queryRawMapIds = "SELECT pmrm.rawMap.id "
                                + "FROM fr.proline.core.orm.lcms.Map  m, ProcessedMap pm, ProcessedMapRawMapMapping pmrm  "
                                + "WHERE m.id IN (:listId) "
                                + "AND m.id = pm.id "
                                + "AND pm.id = pmrm.id.processedMapId ";
                        TypedQuery<Long> queryRawMapLcms = entityManagerLCMS.createQuery(queryRawMapIds, Long.class);
                        queryRawMapLcms.setParameter("listId", listMapIds);
                        List<Long> rawMapIds = queryRawMapLcms.getResultList();
                        String queryMap = "SELECT m "
                                + "FROM  fr.proline.core.orm.lcms.Map  m  "
                                + "WHERE m.id IN (:listId) "
                                + "ORDER BY m.id ";
                        TypedQuery<Map> queryMapLcms = entityManagerLCMS.createQuery(queryMap, Map.class);
                        queryMapLcms.setParameter("listId", rawMapIds);
                        List<Map> mapList = queryMapLcms.getResultList();
                        m_mapList.addAll(mapList);
                    }
                }
            }
            entityManagerLCMS.getTransaction().commit();
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            entityManagerLCMS.getTransaction().rollback();
            return false;
        } finally {
            entityManagerLCMS.close();
        }

        // set priority as low for the possible sub tasks
        m_defaultPriority = Priority.LOW;
        m_currentPriority = Priority.LOW;
        return true;
    }

    /**
     * main task to load all peakels for a map
     *
     * @return
     */
    private boolean fetchDataMainTaskPeakelForMap() {
        EntityManager entityManagerLCMS = DataStoreConnectorFactory.getInstance().getLcMsDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
        try {
            entityManagerLCMS.getTransaction().begin();
            String queryP = "SELECT p.id "
                    + "FROM fr.proline.core.orm.lcms.Peakel p "
                    + "WHERE p.map.id =:mapId "
                    + "ORDER BY p.id ASC ";
            TypedQuery<Long> queryPeakel = entityManagerLCMS.createQuery(queryP, Long.class);
            queryPeakel.setParameter("mapId", m_map == null ? -1 : m_map.getId());
            List<Long> listIds = queryPeakel.getResultList();
            m_peakelIds = listIds;

            for (Long peakelId : m_peakelIds) {
                Peakel p = new Peakel();
                p.setId(peakelId);
                m_peakelList.add(p);
            }

            // slice the task and get the first one
            SubTask subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_PEAKEL, m_peakelIds.size(), SLICE_SIZE);

            // execute the first slice now
            fetchPeakelData(subTask, entityManagerLCMS);

            entityManagerLCMS.getTransaction().commit();
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            entityManagerLCMS.getTransaction().rollback();
            return false;
        } finally {
            entityManagerLCMS.close();
        }

        // set priority as low for the possible sub tasks
        m_defaultPriority = Priority.LOW;
        m_currentPriority = Priority.LOW;
        return true;
    }

    /**
     * main task to load all features for a map
     *
     * @return
     */
    private boolean fetchDataMainTaskFeatureForMap() {
        EntityManager entityManagerLCMS = DataStoreConnectorFactory.getInstance().getLcMsDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
        try {
            entityManagerLCMS.getTransaction().begin();
            String queryF = "SELECT f.id "
                    + "FROM fr.proline.core.orm.lcms.Feature f "
                    + "WHERE f.map.id =:mapId "
                    + "ORDER BY f.id ASC ";
            TypedQuery<Long> queryFeature = entityManagerLCMS.createQuery(queryF, Long.class);
            queryFeature.setParameter("mapId", m_map == null ? -1 : m_map.getId());
            List<Long> listIds = queryFeature.getResultList();
            m_featureIds = listIds;

            for (Long featureId : m_featureIds) {
                Feature f = new Feature();
                f.setId(featureId);
                m_featureList.add(f);
            }

            // slice the task and get the first one
            SubTask subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_FEATURE, m_featureIds.size(), SLICE_SIZE);

            // execute the first slice now
            fetchFeatureData(subTask, entityManagerLCMS);

            entityManagerLCMS.getTransaction().commit();
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            entityManagerLCMS.getTransaction().rollback();
            return false;
        } finally {
            entityManagerLCMS.close();
        }

        // set priority as low for the possible sub tasks
        m_defaultPriority = Priority.LOW;
        m_currentPriority = Priority.LOW;
        return true;
    }

    /**
     * main task to load all peakels for a feature
     *
     * @return
     */
    private boolean fetchDataMainTaskPeakelForFeature() {
        EntityManager entityManagerLCMS = DataStoreConnectorFactory.getInstance().getLcMsDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
        try {
            entityManagerLCMS.getTransaction().begin();
            String queryP = "SELECT fpi.id.peakelId  "
                    + "FROM fr.proline.core.orm.lcms.FeaturePeakelItem fpi "
                    + "WHERE fpi.id.featureId =:featureId "
                    + "ORDER BY fpi.isotopeIndex ASC ";
            TypedQuery<Long> queryPeakel = entityManagerLCMS.createQuery(queryP, Long.class);
            queryPeakel.setParameter("featureId", m_childFeature == null ? -1 : m_childFeature.getId());
            List<Long> listIds = queryPeakel.getResultList();
            m_peakelForFeatureIds = listIds;

            for (Long peakelId : m_peakelForFeatureIds) {
                Peakel p = new Peakel();
                p.setId(peakelId);
                m_peakelForChildFeatureList.add(p);
            }
            if (m_peakelForFeatureIds != null && m_peakelForFeatureIds.size() > 0) {
                // slice the task and get the first one
                SubTask subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_PEAKEL_FOR_FEATURE, m_peakelForFeatureIds.size(), SLICE_SIZE);

                // execute the first slice now
                fetchPeakelForFeatureData(subTask, entityManagerLCMS);
            }

            entityManagerLCMS.getTransaction().commit();
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            entityManagerLCMS.getTransaction().rollback();
            return false;
        } finally {
            entityManagerLCMS.close();
        }

        // set priority as low for the possible sub tasks
        m_defaultPriority = Priority.LOW;
        m_currentPriority = Priority.LOW;
        return true;
    }

    /**
     * subTask to load peakel
     *
     * @return
     */
    private boolean fetchPeakelDataSubTask() {
        SubTask slice = m_subTaskManager.getNextSubTask();
        if (slice == null) {
            return true; // nothing to do : should not happen
        }

        EntityManager entityManagerLCMS = DataStoreConnectorFactory.getInstance().getLcMsDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
        try {
            entityManagerLCMS.getTransaction().begin();
            switch (slice.getSubTaskId()) {
                case SUB_TASK_PEAKEL:
                    fetchPeakelData(slice, entityManagerLCMS);
                    break;
            }
            entityManagerLCMS.getTransaction().commit();
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            entityManagerLCMS.getTransaction().rollback();
            return false;
        } finally {
            entityManagerLCMS.close();
        }

        return true;
    }

    /**
     * subTask to load peakel
     *
     * @return
     */
    private boolean fetchPeakelForFeatureDataSubTask() {
        SubTask slice = m_subTaskManager.getNextSubTask();
        if (slice == null) {
            return true; // nothing to do : should not happen
        }

        EntityManager entityManagerLCMS = DataStoreConnectorFactory.getInstance().getLcMsDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
        try {
            entityManagerLCMS.getTransaction().begin();
            switch (slice.getSubTaskId()) {
                case SUB_TASK_PEAKEL_FOR_FEATURE:
                    fetchPeakelForFeatureData(slice, entityManagerLCMS);
                    break;
            }
            entityManagerLCMS.getTransaction().commit();
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            entityManagerLCMS.getTransaction().rollback();
            return false;
        } finally {
            entityManagerLCMS.close();
        }

        return true;
    }

    /**
     * subTask to load feature
     *
     * @return
     */
    private boolean fetchFeatureDataSubTask() {
        SubTask slice = m_subTaskManager.getNextSubTask();
        if (slice == null) {
            return true; // nothing to do : should not happen
        }

        EntityManager entityManagerLCMS = DataStoreConnectorFactory.getInstance().getLcMsDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
        try {
            entityManagerLCMS.getTransaction().begin();
            switch (slice.getSubTaskId()) {
                case SUB_TASK_FEATURE:
                    fetchFeatureData(slice, entityManagerLCMS);
                    break;
            }
            entityManagerLCMS.getTransaction().commit();
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            entityManagerLCMS.getTransaction().rollback();
            return false;
        } finally {
            entityManagerLCMS.close();
        }

        return true;
    }

    /**
     * *
     * load Peakels data for a given subTask
     *
     * @param subTask
     * @param entityManagerLCMS
     * @return
     */
    private boolean fetchPeakelData(SubTask subTask, EntityManager entityManagerLCMS) {
        List<Long> sliceOfPeakelsIds = subTask.getSubList(m_peakelIds);
        return fetchPeakelData(entityManagerLCMS, sliceOfPeakelsIds, m_peakelList);
    }

    /**
     * *
     * load Peakels data for a given subTask
     *
     * @param subTask
     * @param entityManagerLCMS
     * @return
     */
    private boolean fetchPeakelForFeatureData(SubTask subTask, EntityManager entityManagerLCMS) {
        List<Long> sliceOfPeakelsIds = subTask.getSubList(m_peakelForFeatureIds);
        return fetchPeakelData(entityManagerLCMS, sliceOfPeakelsIds, m_peakelForChildFeatureList);
    }

    /**
     * *
     * load Features data for a given subTask
     *
     * @param subTask
     * @param entityManagerLCMS
     * @return
     */
    private boolean fetchFeatureData(SubTask subTask, EntityManager entityManagerLCMS) {
        List<Long> sliceOfFeaturesIds = subTask.getSubList(m_featureIds);
        return fetchFeatureData(entityManagerLCMS, sliceOfFeaturesIds);
    }

    /**
     * load peakel data for a given list of Ids
     *
     * @param entityManagerLCMS
     * @param listPeakelsIds
     * @return
     */
    private boolean fetchPeakelData(EntityManager entityManagerLCMS, List<Long> listPeakelsIds, List<Peakel> peakelList) {
        int nbP = peakelList.size();
        String queryP = "SELECT p "
                + "FROM fr.proline.core.orm.lcms.Peakel p "
                + "WHERE p.id IN (:listId) "
                + "ORDER BY p.id ASC ";
        TypedQuery<Peakel> queryPeakel = entityManagerLCMS.createQuery(queryP, Peakel.class);
        queryPeakel.setParameter("listId", listPeakelsIds);
        List<Peakel> resultList = queryPeakel.getResultList();
        for (Peakel peakel : resultList) {
            // index
            int index = -1;
            for (int k = 0; k < nbP; k++) {
                if (peakelList.get(k).getId().equals(peakel.getId())) {
                    index = k;
                    break;
                }
            }
            // update the list
            peakelList.set(index, peakel);
        }

        return true;
    }

    /**
     * load feature data for a given list of Ids
     *
     * @param entityManagerLCMS
     * @param listFeaturesIds
     * @return
     */
    private boolean fetchFeatureData(EntityManager entityManagerLCMS, List<Long> listFeaturesIds) {
        int nbF = m_featureList.size();
        String queryF = "SELECT f "
                + "FROM fr.proline.core.orm.lcms.Feature f "
                + "WHERE f.id IN (:listId) "
                + "ORDER BY f.id ASC ";
        TypedQuery<Feature> queryFeature = entityManagerLCMS.createQuery(queryF, Feature.class);
        queryFeature.setParameter("listId", listFeaturesIds);
        List<Feature> resultList = queryFeature.getResultList();
        for (Feature feature : resultList) {
            // index
            int index = -1;
            for (int k = 0; k < nbF; k++) {
                if (m_featureList.get(k).getId().equals(feature.getId())) {
                    index = k;
                    break;
                }
            }
            // update the list
            m_featureList.set(index, feature);
        }

        return true;
    }

    /**
     * main task to load all master features for a masterQuantPeptide
     *
     * @return
     */
    private boolean fetchDataMainTaskMasterFeatureForPeptide() {
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
        EntityManager entityManagerLCMS = DataStoreConnectorFactory.getInstance().getLcMsDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
        try {
            entityManagerMSI.getTransaction().begin();
            entityManagerLCMS.getTransaction().begin();
            if (m_masterQuantPeptide != null) {
                String queryPepIon = "SELECT mqpi.lcmsMasterFeatureId  "
                        + "FROM MasterQuantPeptideIon mqpi "
                        + "WHERE mqpi.masterQuantPeptideId=:masterQuantPeptideId  "
                        + "ORDER BY mqpi.charge ASC";
                TypedQuery<Long> peptidesIonQuery = entityManagerMSI.createQuery(queryPepIon, Long.class);
                peptidesIonQuery.setParameter("masterQuantPeptideId", m_masterQuantPeptide.getId());
                List<Long> listLcmsIds = (List<Long>) peptidesIonQuery.getResultList();
                if(listLcmsIds != null && !listLcmsIds.isEmpty()) {
                    String queryMaster = "SELECT f "
                        + "FROM fr.proline.core.orm.lcms.Feature f "
                        + "WHERE f.id IN (:listId) "
                        + "ORDER BY f.elutionTime ASC ";
                    TypedQuery<Feature> masterQuery = entityManagerLCMS.createQuery(queryMaster, Feature.class);
                    masterQuery.setParameter("listId", listLcmsIds);
                    m_masterFeatureList.addAll(masterQuery.getResultList());
                }
            }
            entityManagerMSI.getTransaction().commit();
            entityManagerLCMS.getTransaction().commit();
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            entityManagerMSI.getTransaction().rollback();
            entityManagerLCMS.getTransaction().rollback();
            return false;
        } finally {
            entityManagerMSI.close();
            entityManagerLCMS.close();
        }

        // set priority as low for the possible sub tasks
        m_defaultPriority = Priority.LOW;
        m_currentPriority = Priority.LOW;
        return true;
    }

    /**
     * main task to load all child features for a masterFeature
     *
     * @return
     */
    private boolean fetchDataMainTaskChildFeatureForMaster() {
        EntityManager entityManagerLCMS = DataStoreConnectorFactory.getInstance().getLcMsDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
        try {
            entityManagerLCMS.getTransaction().begin();
            if (m_masterFeature != null) {
                String query = "SELECT f "
                        + "FROM fr.proline.core.orm.lcms.Feature f, fr.proline.core.orm.lcms.MasterFeatureItem mfi   "
                        + "WHERE f.id = mfi.childFeature.id AND "
                        + "mfi.masterFeature.id =:masterFeatureId ";
                TypedQuery<Feature> queryChild = entityManagerLCMS.createQuery(query, Feature.class);
                queryChild.setParameter("masterFeatureId", m_masterFeature.getId());
                List<Feature> resultList = queryChild.getResultList();
                for (Feature feature : resultList) {
                    String queryF = "SELECT f "
                            + "FROM fr.proline.core.orm.lcms.Feature f, fr.proline.core.orm.lcms.MasterFeatureItem mfi  "
                            + "WHERE f.id = mfi.childFeature.id AND "
                            + "mfi.masterFeature.id =:masterFeatureId ";
                    TypedQuery<Feature> queryChildF = entityManagerLCMS.createQuery(queryF, Feature.class);
                    queryChildF.setParameter("masterFeatureId", feature.getId());
                    List<Feature> r2 = queryChildF.getResultList();
                    if (r2 != null && !r2.isEmpty()) {
                        // load childs for this cluster feature
                        m_childFeatureList.addAll(r2);
                    } else {
                        // no childs: load this feature
                        m_childFeatureList.add(feature);
                    }
                }
            }
            entityManagerLCMS.getTransaction().commit();
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            entityManagerLCMS.getTransaction().rollback();
            return false;
        } finally {
            entityManagerLCMS.close();
        }

        // set priority as low for the possible sub tasks
        m_defaultPriority = Priority.LOW;
        m_currentPriority = Priority.LOW;
        return true;
    }

    /**
     * main task to load all child features for a masterQuantPeptideIon with peakels
     *
     * @return
     */
    private boolean fetchDataMainTaskChildFeatureForPeptideIonWithPeakels() {
        EntityManager entityManagerLCMS = DataStoreConnectorFactory.getInstance().getLcMsDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
        try {
            entityManagerLCMS.getTransaction().begin();
            if (m_masterQuantPeptideIon != null) {
                String query = "SELECT f "
                        + "FROM fr.proline.core.orm.lcms.Feature f, fr.proline.core.orm.lcms.MasterFeatureItem mfi   "
                        + "WHERE f.id = mfi.childFeature.id AND "
                        + "mfi.masterFeature.id =:masterFeatureId ";
                TypedQuery<Feature> queryChild = entityManagerLCMS.createQuery(query, Feature.class);
                queryChild.setParameter("masterFeatureId", m_masterQuantPeptideIon.getLcmsMasterFeatureId());
                List<Feature> resultList = queryChild.getResultList();
                List<Feature> allFeature = new ArrayList();
                allFeature.addAll(resultList);
                String queryF = "SELECT f "
                            + "FROM fr.proline.core.orm.lcms.Feature f, fr.proline.core.orm.lcms.FeatureClusterItem fci  "
                            + "WHERE f.id = fci.subFeature.id AND "
                            + "fci.clusterFeature.id =:masterFeatureId ";
                TypedQuery<Feature> queryChildF = entityManagerLCMS.createQuery(queryF, Feature.class);
                for (Feature feature : resultList) {
                    queryChildF.setParameter("masterFeatureId", feature.getId());
                    List<Feature> r2 = queryChildF.getResultList();
                    if (r2 != null && !r2.isEmpty()) {
                        allFeature.addAll(r2);
                    }
                }
                
                //load peakel
                String queryP = "SELECT fpi.id.peakelId  "
                            + "FROM fr.proline.core.orm.lcms.FeaturePeakelItem fpi "
                            + "WHERE fpi.id.featureId =:featureId "
                            + "ORDER BY fpi.isotopeIndex ASC ";
                TypedQuery<Long> queryPeakel = entityManagerLCMS.createQuery(queryP, Long.class);
                
                
                for (Feature feature : allFeature) {
                    m_childFeatureList.add(feature);
                    java.util.Map<String,Object>  prop = feature.getSerializedPropertiesAsMap(); //init the serializedProp.
                    queryPeakel.setParameter("featureId", feature.getId());
                    List<Long> listIds = queryPeakel.getResultList();
                    if (listIds != null && !listIds.isEmpty()) {
                        String queryPeakelLoadS = "SELECT p, fpi.isotopeIndex "
                                + "FROM fr.proline.core.orm.lcms.Peakel p, FeaturePeakelItem fpi "
                                + "WHERE p.id IN (:listId) AND p.id = fpi.id.peakelId AND fpi.id.featureId =:featureId ";
                        Query queryPeakelLoad = entityManagerLCMS.createQuery(queryPeakelLoadS);
                        queryPeakelLoad.setParameter("listId", listIds);
                        queryPeakelLoad.setParameter("featureId", feature.getId());
                        List<Object[]> resultPeakelList = queryPeakelLoad.getResultList();
                        List<Peakel> peakelResultList = new ArrayList();
                        for (Object[] resCur : resultPeakelList) {
                            Peakel peakel = (Peakel) resCur[0];
                            peakel.setIsotopeIndex((Integer)resCur[1]);
                            peakelResultList.add(peakel);
                        }
                        m_peakelListPerFeature.add(peakelResultList);
                    } else {
                        m_peakelListPerFeature.add(new ArrayList());
                    }
                }
            }
            entityManagerLCMS.getTransaction().commit();
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            entityManagerLCMS.getTransaction().rollback();
            return false;
        } finally {
            entityManagerLCMS.close();
        }

        // set priority as low for the possible sub tasks
        m_defaultPriority = Priority.LOW;
        m_currentPriority = Priority.LOW;
        return true;
    }
    
    
    /**
     * main task to load all map alignment for a xic
     *
     * @return
     */
    private boolean fetchDataMainTaskAlignmentForXic() {
        EntityManager entityManagerLCMS = DataStoreConnectorFactory.getInstance().getLcMsDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
        try {
            entityManagerLCMS.getTransaction().begin();
            if (m_dataset_align != null && m_dataset_align.getMasterQuantitationChannels() != null) {
                List<DMasterQuantitationChannel> listMasterQuantitationChannels = m_dataset_align.getMasterQuantitationChannels();
                for (DMasterQuantitationChannel masterQuantitationChannel : listMasterQuantitationChannels) {
                    List<DQuantitationChannel> listQuantChannels = masterQuantitationChannel.getQuantitationChannels();
                    if (listQuantChannels != null && !listQuantChannels.isEmpty()) {
                        List<Long> listMapIds = new ArrayList();
                        for (DQuantitationChannel quantitationChannel : listQuantChannels) {
                            listMapIds.add(quantitationChannel.getLcmsMapId());
                        }
                        // determine the alignment reference map and the mapSet
                        String queryMasterMap = "SELECT pm.id, pm.mapSet.id "
                                + "FROM fr.proline.core.orm.lcms.ProcessedMap pm "
                                + "WHERE pm.id IN (:listId) "
                                + "AND pm.isAlnReference is true ";
                        Query queryMasterMapQ = entityManagerLCMS.createQuery(queryMasterMap);
                        queryMasterMapQ.setParameter("listId", listMapIds);
                        List<Object[]> rl = new ArrayList();
                        Long algRefMapId = (long) -1;
                        Long mapSetId  = (long)-1;
                        if (!listMapIds.isEmpty()) {
                            rl = queryMasterMapQ.getResultList();
                        }
                        Iterator<Object[]> it2 = rl.iterator();
                        while (it2.hasNext()) { // should be one
                            Object[] res = it2.next();
                            algRefMapId = (Long) res[0];
                            mapSetId = (Long) res[1];
                        }
                        
                        String queryMapAlignS = "SELECT ma "
                                + "FROM fr.proline.core.orm.lcms.MapAlignment ma  "
                                + "WHERE ma.mapSet.id=:mapSetId "
                                + "AND  ma.sourceMap.id=:algRefMapId ";
                        Query queryMapAlign = entityManagerLCMS.createQuery(queryMapAlignS);
                        queryMapAlign.setParameter("mapSetId", mapSetId);
                        queryMapAlign.setParameter("algRefMapId", algRefMapId);
                        List<Object> resultList = new ArrayList();
                        if (!listMapIds.isEmpty()) {
                            resultList = queryMapAlign.getResultList();
                        }
                        for (Object resCur : resultList) {
                            MapAlignment ma = (MapAlignment) resCur;
                            m_mapAlignmentList.add(ma);
                        }
                        
                        // all alignments
                        String queryAllAlignS = "SELECT ma "
                                + "FROM fr.proline.core.orm.lcms.MapAlignment ma "
                                + "WHERE ma.mapSet.id=:mapSetId  ";
                        TypedQuery<MapAlignment> queryAllAlign = entityManagerLCMS.createQuery(queryAllAlignS, MapAlignment.class);
                        queryAllAlign.setParameter("mapSetId", mapSetId);
                        List<MapAlignment> rs2 = queryAllAlign.getResultList();
                        for (MapAlignment ma : rs2) {
                            m_allMapAlignmentList.add(ma);
                        }
                        // all processed map -- order by quant channel
                        String orderBy = " case pm.id ";
                        int index = 1;
                        for (Long id: listMapIds){
                            orderBy+=" when "+id+" then "+index;
                            index++;
                        }
                        orderBy += " else "+index+ " end "  ;
                        String queryAllProcessedMapS = "SELECT pm "
                                + "FROM fr.proline.core.orm.lcms.ProcessedMap pm "
                                + "WHERE pm.id IN (:listId) "
                                + "ORDER BY "+orderBy;
                        TypedQuery<ProcessedMap> queryAllProcessedMap = entityManagerLCMS.createQuery(queryAllProcessedMapS, ProcessedMap.class);
                        queryAllProcessedMap.setParameter("listId", listMapIds);
                        List<ProcessedMap> rs3 = queryAllProcessedMap.getResultList();
                        for (ProcessedMap pm : rs3) {
                            m_allMaps.add(pm);
                        }
                    }
                }
            }
            entityManagerLCMS.getTransaction().commit();
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            entityManagerLCMS.getTransaction().rollback();
            return false;
        } finally {
            entityManagerLCMS.close();
        }

        // set priority as low for the possible sub tasks
        m_defaultPriority = Priority.LOW;
        m_currentPriority = Priority.LOW;
        return true;
    }
}
