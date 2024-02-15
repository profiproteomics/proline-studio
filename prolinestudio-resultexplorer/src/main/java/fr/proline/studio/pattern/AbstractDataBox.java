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

import fr.proline.core.orm.util.TransientDataInterface;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.memory.TransientMemoryCacheManager;
import fr.proline.studio.dam.memory.TransientMemoryClientInterface;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.tasks.AbstractDatabaseTask;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.id.ProjectId;
import fr.proline.studio.pattern.xic.*;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.table.TableInfo;
import fr.proline.studio.utils.IconManager;
import org.jdesktop.swingx.JXTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.*;

/**
 *
 * A Box receive IN-parameters. According to them, it loads data from the
 * database. These data are displayed to the user in an associated Graphical
 * Panel. If the user select data in the panel, the box offers the selected data
 * as OUT-parameters for the next dataBox.
 *
 * @author JM235353
 */
public abstract class AbstractDataBox implements ChangeListener, ProgressInterface, SplittedPanelContainer.UserActions, TransientMemoryClientInterface {

    // Panel corresponding to this box
    protected DataBoxPanelInterface m_panel;

    // In and out Parameters Registered
    private ParameterList m_inParameters = null;
    private ParameterList m_outParameters = null;

    private final HashMap<Long, TaskInfo> m_taskMap = new HashMap<>();

    private final ProjectId m_projectId = new ProjectId();

    protected String m_typeName;
    protected String m_dataName;
    protected String m_fullName = null;
    protected String m_description = "";
    protected String m_userName = null;

    private SplittedPanelContainer.PanelLayout m_layout = SplittedPanelContainer.PanelLayout.VERTICAL;

    protected ArrayList<AbstractDataBox> m_nextDataBoxArray = null;
    protected AbstractDataBox m_previousDataBox = null;

    private int m_loadingId = 0;

    private int m_id = -1;
    private static int m_idCount = 0;

    private Image m_icon;

    protected DataboxStyle m_style;
    protected DataboxType m_type;

    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer.databox");
    
    public enum DataboxStyle {
        STYLE_XIC,
        STYLE_SC,
        STYLE_RSM,
        STYLE_RSET,
        STYLE_UNKNOWN
    }

    public enum DataboxType {
        DataBoxRsetAll(0),
        DataBoxRsetAllProteinMatch(1),
        DataBoxRsetPSM(2),
        DataBoxRsetPeptideFragmentation(3),
        DataBoxRsetPeptideSpectrum(4),
        DataBoxRsetPeptideSpectrumError(5),
        DataBoxRsetProteinsForPeptideMatch(6),
        DataBoxRsmAllProteinSet(7),
        DataBoxRsmPSM(8),
        DataBoxRsmPeptideInstances(9),
        DataBoxRsmPeptidesOfProtein(10),
        DataBoxRsmProteinAndPeptideSequence(11),
        DataBoxRsmProteinSetOfPeptides(12),
        DataBoxRsmProteinsOfProteinSet(13),
        //DataBoxStatisticsFrequencyResponse(15),
        DataBoxTaskDescription(16),
        DataBoxTaskList(17),
        DataboxRsetPeptidesOfProtein(18),
        DataboxRsmPSMOfProteinSet(19),
        DataboxRsmPSMOfPeptide(20),
        DataboxXicProteinSet(21),
        DataboxRsetMSDiag(22),
        DataboxXicPeptideSetShortList(23),
        DataboxCompareResult(24),
        DataboxXicPeptideSet(25),
        DataboxXicPeptideIon(26),
        DataboxGraphics(27),
        DataboxXicChildFeature(28),
        DataboxMultiGraphics(29),
        DataboxPSMOfMasterQuantPeptide(30),
        DataBoxMzScope(31),
        DataboxDataAnalyzer(32),
        DataboxExperimentalDesign(33),
        DataBoxAdjacencyMatrix(34),
        DataBoxAdjacencyMatrixChoice(35),
        DataBoxMapAlignment(36),
        DataBoxMSQueriesForRSM(37),
        DataBoxRSMPSMForMsQuery(38),
        DataBoxRsetPeptideSpectrumValues(39),
        DataBoxMSQueriesForRset(40),
        DataBoxRsetPSMForMsQuery(41),
        DataBoxDataAnalyzerResults(43),
        DataBoxImage(44),
        DataBoxSystemTasks(45),
        DataBoxFrozenCopy(46),
        DataBoxPTMSiteProtein(47),
        DataBoxPTMSitePeptides(48),
        DataBoxPTMSitePepMatches(49),
        DataBoxXicPTMPeptides(50),
        DataBoxXicPTMPeptidesMatches(51),
        DataBoxPTMSitePeptidesGraphic(52),
        DataboxMultiGraphicsDoubleYAxis(53),
        DataBoxPTMPeptides(54),
        DataBoxPTMClusters(55),
        DataBoxPTMPeptidesMatches(56),
        DataBoxPTMPeptidesGraphic(57),
        DataboxXicParentsPeptideIon(58),
        DataBoxPTMSiteAsClusters(59),
        DataboxXicReporterIon(62)
        ;
        int m_type;
        private static HashMap<Integer, DataboxType> m_databoxTypeMap = null;

        DataboxType(int type) {
            m_type = type;
        }

        public int intValue() {
            return m_type;
        }

        public AbstractDataBox getDatabox() {
            switch (this) {
                case DataBoxRsetAll:
                    return new DataBoxRsetAll();
                case DataBoxRsetAllProteinMatch:
                    return new DataBoxRsetAllProteinMatch();
                case DataBoxRsetPSM:
                    return new DataBoxRsetPSM();
                case DataBoxRsetPeptideFragmentation:
                    return new DataBoxRsetPeptideFragmentation();
                case DataBoxRsetPeptideSpectrum:
                    return new DataBoxRsetPeptideSpectrum();
                case DataBoxRsetPeptideSpectrumError:
                    return new DataBoxRsetPeptideSpectrumError();
                case DataBoxRsetProteinsForPeptideMatch:
                    return new DataBoxRsetProteinsForPeptideMatch();
                case DataBoxRsmAllProteinSet:
                    return new DataBoxRsmAllProteinSet();
                case DataBoxRsmPSM:
                    return new DataBoxRsmPSM();
                case DataBoxRsmPeptideInstances:
                    return new DataBoxRsmPeptideInstances();
                case DataBoxRsmPeptidesOfProtein:
                    return new DataBoxRsmPeptidesOfProtein();
                case DataBoxRsmProteinAndPeptideSequence:
                    return new DataBoxRsmProteinAndPeptideSequence();
                case DataBoxRsmProteinSetOfPeptides:
                    return new DataBoxRsmProteinSetOfPeptides();
                case DataBoxRsmProteinsOfProteinSet:
                    return new DataBoxRsmProteinsOfProteinSet();
                case DataBoxTaskDescription:
                    return new DataBoxTaskDescription();
                case DataBoxTaskList:
                    return new DataBoxTaskList();
                case DataboxRsetPeptidesOfProtein:
                    return new DataboxRsetPeptidesOfProtein();
                case DataboxRsmPSMOfProteinSet:
                    return new DataboxRsmPSMOfProteinSet();
                case DataboxRsmPSMOfPeptide:
                    return new DataboxRsmPSMOfPeptide();
                case DataboxXicProteinSet:
                    return new DataboxXicProteinSet();
                case DataboxXicPeptideSetShortList:
                    return new DataboxXicPeptideSet(true);
                case DataboxXicPeptideSet:
                    return new DataboxXicPeptideSet();
                case DataboxRsetMSDiag:
                    return new DataBoxRsetMSDiag(null);
                case DataboxXicPeptideIon:
                    return new DataboxXicPeptideIon();
                case DataboxGraphics:
                    return new DataboxGraphics(false);
                case DataboxXicChildFeature:
                    return new DataboxChildFeature();
                case DataboxMultiGraphics:
                    return new DataboxMultiGraphics();
                case DataboxMultiGraphicsDoubleYAxis:
                    return new DataboxMultiGraphics(false,false,true);
                case DataboxPSMOfMasterQuantPeptide:
                    return new DataboxPSMOfMasterQuantPeptide();
                case DataBoxMzScope:
                    return new DataBoxMzScope();
                case DataBoxAdjacencyMatrix:
                    return new DataBoxAdjacencyMatrix();
                case DataBoxAdjacencyMatrixChoice:
                    return new DataBoxAdjacencyMatrixChoice();
                case DataboxExperimentalDesign:
                    return new DataboxExperimentalDesign();
                case DataBoxMapAlignment:
                    return new DataboxMapAlignment();
                case DataBoxMSQueriesForRSM:
                    return new DataBoxMSQueriesForRSM();
                case DataBoxRSMPSMForMsQuery:
                    return new DataBoxRsmPSMForMsQuery();
                case DataBoxRsetPeptideSpectrumValues:
                    return new DataBoxRsetPeptideSpectrumValues();
                case DataBoxMSQueriesForRset:
                    return new DataBoxMSQueriesForRset();
                case DataBoxRsetPSMForMsQuery:
                    return new DataboxRsetPSMForMsQuery();
                case DataBoxDataAnalyzerResults:
                    return new DataBoxDataAnalyzerResults();
                case DataBoxImage:
                    return new DataBoxImage();
                case DataBoxSystemTasks:
                    return new DataBoxSystemTasks();
                case DataBoxPTMPeptides:
                    return new DataBoxPTMPeptides(false, false);
                case DataBoxXicPTMPeptides:
                    return new DataBoxPTMPeptides(true, false);                    
                case DataBoxPTMClusters:
                    return new DataBoxPTMClusters();                    
                case DataBoxFrozenCopy:
                    return null; // not used for frozen copy
                case DataBoxPTMPeptidesMatches:
                    return new DataBoxPTMPeptides(false, true);                                       
                case DataBoxXicPTMPeptidesMatches:
                    return new DataBoxPTMPeptides(true, true);                         
                case DataBoxPTMPeptidesGraphic:
                    return new DataBoxPTMPeptidesGraphic();  
                case DataboxXicParentsPeptideIon:
                    return new DataboxXicParentsPeptideIon();
                case DataBoxPTMSiteAsClusters:
                    return new DataBoxPTMClusters(true);
                case DataboxXicReporterIon:
                    return new DataboxXicReporterIon();

            }
            return null; // should not happen
        }

        private static HashMap<Integer, DataboxType> generateDataboxTypeMap() {
            HashMap<Integer, DataboxType> map = new HashMap<>();
            DataboxType[] databoxTypeArray = DataboxType.values();
            for (DataboxType type : databoxTypeArray) {
                map.put(type.m_type, type);
            }
            return map;
        }

        public static DataboxType getDataboxType(int type) {
            if (m_databoxTypeMap == null) {
                m_databoxTypeMap = generateDataboxTypeMap();
            }
            return m_databoxTypeMap.get(type);
        }

    }

    public AbstractDataBox(DataboxType type, DataboxStyle style) {
        m_type = type;
        m_style = style;

        // Register possible out parameters
        ParameterList outParameter = new ParameterList();
        outParameter.addParameter(ProjectId.class);
        registerOutParameter(outParameter);
    }

    protected void setDataBoxPanelInterface(DataBoxPanelInterface panelInterface) {
        m_panel = panelInterface;
        if (m_panel != null) {
            getDataBoxPanelInterface().addSingleValue(m_projectId);
        }
    }

    protected DataBoxPanelInterface getDataBoxPanelInterface() {
        return m_panel;
    }

    public void setUserName(String userName) {
        m_userName = userName;
    }

    public String getUserName() {
        return m_userName;
    }

    public DataboxType getType() {
        return m_type;
    }

    public DataboxStyle getStyle() {
        return m_style;
    }

    public int getId() {
        if (m_id == -1) {
            m_id = ++m_idCount;
        }
        return m_id;
    }

    public void setIcon(Image icon) {
        m_icon = icon;
    }

    public Image getIcon() {
        return m_icon;
    }

    public Image getDefaultIcon() {
        switch (m_style) {
            case STYLE_RSET:
                return IconManager.getImage(IconManager.IconType.DATASET_RSET);
            case STYLE_RSM:
                return IconManager.getImage(IconManager.IconType.DATASET_RSM);
            case STYLE_XIC:
                return IconManager.getImage(IconManager.IconType.QUANT_XIC);
            case STYLE_SC:
                return IconManager.getImage(IconManager.IconType.QUANT_SC);
            case STYLE_UNKNOWN:
                return null;
        }
        return null;
    }

    protected void deleteThis() {

        // free Transient Data Memory
        unlinkCache();

        // cancel task possibily running
        if (!m_taskMap.isEmpty()) {
            AccessDatabaseThread.getAccessDatabaseThread().abortTasks(m_taskMap.keySet());
            m_taskMap.clear();
        }

        if (m_nextDataBoxArray != null) {
            for (AbstractDataBox nextDataBox : m_nextDataBoxArray) {
                nextDataBox.deleteThis();
            }
        }


    }

    protected void linkCache(TransientDataInterface cache) {
        if (m_previousDataBox != null) {
            m_previousDataBox.linkCache(cache);
            return;
        }
        TransientMemoryCacheManager.getSingleton().linkCache(this, cache);
    }

    private void unlinkCache() {
        if (m_previousDataBox != null) {
            m_previousDataBox.unlinkCache();
            return;
        }
        TransientMemoryCacheManager.getSingleton().unlinkCache(this);
    }

    /**
     * A task must be registered, so it can be cancelled (a DataBox must not
     * directly call AccessDatabaseThread.getAccessDatabaseThread().addTask()
     * method
     *
     * @param task
     */
    protected void registerTask(AbstractDatabaseTask task) {
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
        m_taskMap.put(task.getId(), task.getTaskInfo());

    }

    protected void unregisterTask(Long taskId) {
        m_taskMap.remove(taskId);
    }
    
    protected TaskInfo getTaskInfo(Long taskId){        
        return m_taskMap.get(taskId);        
    }

    protected final void registerInParameter(ParameterList parameter) {
        if (m_inParameters == null) {
            m_inParameters = parameter;
        } else {
            m_inParameters.addParameter(parameter);
        }
    }

    protected final void registerOutParameter(ParameterList parameter) {
        if (m_outParameters == null) {
            m_outParameters = parameter;
        } else {
            m_outParameters.addParameter(parameter);
        }
    }

    public ParameterList getOutParameters() {
        return m_outParameters;
    }

    public ParameterList getInParameters() {
        return m_inParameters;
    }

    /**
     * Return potential extra data available for the corresponding parameter of class type
     * @param parameterType
     * @return 
     */
    public Object getExtraData(Class parameterType) {
        if (isDataDependant(parameterType, ParameterSubtypeEnum.SINGLE_DATA)) {
            return null;
        }
        if (m_previousDataBox != null) {
            return  m_previousDataBox.getExtraData(parameterType);
        }
        return null;
    }

    public boolean isDataDependant(Class dataType, ParameterSubtypeEnum subtype) {
        return (m_inParameters.isDataDependant(dataType, subtype));
    }
    public boolean isDataDependant(HashMap<Class, HashSet<ParameterSubtypeEnum>> dataTypeMap) {
        for (Class dataType : dataTypeMap.keySet()) {
            HashSet<ParameterSubtypeEnum> subtypeSet = dataTypeMap.get(dataType);
            for (ParameterSubtypeEnum subtype : subtypeSet) {
                if (m_inParameters.isDataDependant(dataType, subtype)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isDataProvider(Class dataType, ParameterSubtypeEnum subtype) {
        return (m_outParameters.isDataDependant(dataType, subtype));
    }

    public boolean isDataOfInterest(Long rsetId, Long rsmId, Class dataType) {
        boolean isOfInterest = rsetId == null || rsetId.equals(getRsetId());
        isOfInterest =  rsmId!=null ? (isOfInterest && rsmId.equals(getRsmId()) ) : isOfInterest;
        return (isOfInterest && isDataProvider(dataType, null));
    }


    public boolean isClosable(){
        return true;
    }

    public String getClosingWarningMessage(){
        return "";
    }


    public void loadedDataModified(Long rsetId, Long rsmId, Class dataType, ArrayList modificationsList, byte reason) {
        if (isDataProvider(dataType, null)) {
            dataMustBeRecalculated(rsetId, rsmId, dataType, modificationsList, reason);
        }
        if (m_nextDataBoxArray != null) {
            for (AbstractDataBox nextDataBox : m_nextDataBoxArray) {
                nextDataBox.loadedDataModified(rsetId, rsmId, dataType, modificationsList, reason);
            }
        }
    }

    public double calculateParameterCompatibilityDistance(ArrayList<ParameterList> outParameters) {
        if (m_inParameters.isCompatibleWithOutParameter(outParameters)) {
            return 0;
        }
        return -1;
    }

    public double calculateParameterCompatibilityDistance(AvailableParameters avalaibleParameters, AbstractDataBox nextDataBox, Class compulsoryInParameterClass) {

        return avalaibleParameters.calculateParameterCompatibilityDistance(nextDataBox, compulsoryInParameterClass);

    }

    /**
     * set m_nextDataBoxArray and m_previousDataBox
     *
     * @param nextDataBox
     */
    public void addNextDataBox(AbstractDataBox nextDataBox) {
        if (m_nextDataBoxArray == null) {
            m_nextDataBoxArray = new ArrayList<>(1);
        }
        m_nextDataBoxArray.add(nextDataBox);

        if (nextDataBox != null) {
            nextDataBox.m_previousDataBox = this;
        }
    }

    public void removeNextDataBox(AbstractDataBox nextDataBox) {
        if (m_nextDataBoxArray == null) {
            // should not happen
            return;
        }
        m_nextDataBoxArray.remove(nextDataBox);
    }

    public abstract void createPanel();

    public abstract void dataChanged();

    /**
     * To be overriden if the modification in a following databox can lead to a
     * modificiation of the data of the current databox. (for instance,
     * disabling peptides -> modifications of protein set in the XIC View
     *
     * @param rsetId : Result Set Id the modified data belongs to. May be null
     * @param rsmId : Result Summary Id the modified data belongs to. May be null
     * @param modificationsList :modified data list
     * @param dataType : dataType type of data that may be impacted by modification
     * @param reason: Combination of DataBoxViewerManager.REASON_MODIF Flags. Combination is done using REASON_MODIF.getReasonValue
     */
    public void dataMustBeRecalculated(Long rsetId, Long rsmId, Class dataType, ArrayList modificationsList, byte reason) {

    }

    public final Object getData(Class parameterType) {
        return getData(parameterType, ParameterSubtypeEnum.SINGLE_DATA);
    }

    public final Object getData(Class parameterType, ParameterSubtypeEnum parameterSubtype) {

        // I remove this log, it generates too much logs
        // put it back if needed
        //m_logger.debug("Ask to DataBox {} for parameter {}-{}", getClass().getName(), parameterType.getName(), parameterSubtype);
        
        // check if the box has the right to ask this data
        checkDataAsked(parameterType, parameterSubtype);

        //If no specidic subtype assume it's SINGLE_DATA
        if(parameterSubtype == null )
            parameterSubtype = ParameterSubtypeEnum.SINGLE_DATA;

        return getDataImpl(parameterType, parameterSubtype);
    }
    
    public Object getDataImpl(Class parameterType, ParameterSubtypeEnum parameterSubtype) {

        if ((parameterType != null) && (parameterType.equals(ProjectId.class))) {
            if ((m_projectId == null) || (m_projectId.getId() == -1L)) {
                if (m_previousDataBox != null) {
                    return m_previousDataBox.getDataImpl(parameterType, parameterSubtype);
                }
            }
            return m_projectId;
        }

        if (m_previousDataBox != null) {
            Object result = m_previousDataBox.getDataImpl(parameterType, parameterSubtype);
           
            // Check that the databox returns a correct data
            checkDataReturned(parameterType, parameterSubtype, result);
            
            return result;
        }
        return null;
    }
    
    private void checkDataAsked(Class parameterType, ParameterSubtypeEnum parameterSubtype) {
        // Check that the databox has the right to ask this data
        // it has the right to ask this data if :
        // - it has declared this data as an in parameter
        // - or it has declared this data as an out parameter itself
        // - or if the next parameter has it as in parameter (happens when calling directly getData on previous parameter)
        if (!parameterType.equals(ProjectId.class)
                && !m_inParameters.isDataDependant(parameterType, parameterSubtype)
                && !m_outParameters.isDataDependant(parameterType, parameterSubtype)) {
            // the box ask for a data not registered in in parameters

            boolean nextOk = false;
            if (m_nextDataBoxArray != null) {
                for (AbstractDataBox nextDataBox : m_nextDataBoxArray) {
                    if (nextDataBox.m_inParameters.isDataDependant(parameterType, parameterSubtype)) {
                        nextOk = true;
                        break;
                    }
                }
            }
            if (!nextOk) {
                m_logger.warn("DataBox {} ask for unregistered in parameter {}", getClass().getName(), parameterType.getName());
            }
        }
    }
    
    private void checkDataReturned(Class parameterType, ParameterSubtypeEnum parameterSubtype, Object data) {
        if (data != null) {

            boolean errorFound = false;
            if (parameterSubtype.equals(ParameterSubtypeEnum.SINGLE_DATA)) {
                // classes must correspond perfectly or inherits
                if (!parameterType.equals(data.getClass()) && !parameterType.isAssignableFrom(data.getClass())) {
                    errorFound = true;
                }
            } else {
                // classes must correspond/inherit or enclosing class like for ArrayList<DPeptideMatch> must correspond/inhherits
                Class alternativeClass = null;
                if (data instanceof List) {
                    List l = (List) data;
                    if (!l.isEmpty()) {
                        Object elem = l.get(0);
                        if (elem != null) {
                            alternativeClass = l.get(0).getClass();
                        }
                    }
                }
                if (alternativeClass != null) {
                    if (!parameterType.equals(data.getClass()) && !parameterType.isAssignableFrom(data.getClass()) && !parameterType.equals(alternativeClass) && !parameterType.isAssignableFrom(alternativeClass)) {
                        errorFound = true;
                    }
                }
            }

            if (errorFound) {
                m_logger.warn("DataBox {} returns a wrong type : {} instead of {}. Error can come form ArrayList<A> instead of A.", m_previousDataBox.getClass().getName(), data.getClass().getName(), parameterType.getName());
            }
        }
    }

    public void setEntryData(Object data) {
        throw new UnsupportedOperationException();
    }


    private static void propagateDataChanged(AbstractDataBox a) {
        
        if (queueList.contains(a)) {
            // nothing to do, propagation for this box will be propagated
            return;
        }
        
        // add the box to the gueue
        queueList.add(a);
        
        if (queueList.size() > 1) {
            // a box is being treated, we will treat this one later
            return;
        }

        // treats all box one by one (first in, first out)
        // during this operation, new boxes can be added to the queue.
        try {
            while (!queueList.isEmpty()) {
                AbstractDataBox currentBox = queueList.peekFirst();

                // log propagateDataChanged
                if (m_logger.isDebugEnabled()) {
                    logPropagateDataChanged(currentBox);

                }

                currentBox.propagateDataChanged(currentBox.m_dataChangedMap);
                currentBox.m_dataChangedMap.clear();
                queueList.pop();
            }
        } catch (Exception e) {
            // if an exception occurs, clean up the queuelist to be restart from clean bases
            queueList.clear();
            throw e;
        }
    }
    private static LinkedList<AbstractDataBox> queueList = new LinkedList();
    
    
    
    private HashMap<Class, HashSet<ParameterSubtypeEnum>> m_dataChangedMap = new HashMap<>();    
    public void addDataChanged(Class dataType) {
        addDataChanged(dataType, ParameterSubtypeEnum.SINGLE_DATA);
    }
    public void addDataChanged(Class dataType, ParameterSubtypeEnum parameterSubtype) {
        
        if (m_nextDataBoxArray == null) {
            // no need to add data changed 
            return;
        }
        
        HashSet<ParameterSubtypeEnum> hashSet = m_dataChangedMap.get(dataType);
        if (hashSet == null) {
            hashSet = new HashSet<>();
            m_dataChangedMap.put(dataType, hashSet);
        }
        
        hashSet.add(parameterSubtype);
    }
    public void propagateDataChanged() {
        
        if (m_nextDataBoxArray == null) {
            // no need to add data changed 
            return;
        }
        
        // propagation is queued up and can be treated later
        propagateDataChanged(this);
    }
    private void propagateDataChanged(HashMap<Class, HashSet<ParameterSubtypeEnum>> dataTypeMap) {

        if (m_nextDataBoxArray != null) {
            for (AbstractDataBox nextDataBox : m_nextDataBoxArray) {
                if (nextDataBox.isDataDependant(dataTypeMap)) {

                    nextDataBox.dataChanged();
                }
            }
            for (AbstractDataBox nextDataBox : m_nextDataBoxArray) {
                nextDataBox.propagateDataChanged(dataTypeMap);
            }
        }
    }

    public static void logPropagateDataChanged(AbstractDataBox box) {
        m_sb.setLength(0);
        m_sb.append(box.getType()).append("#").append(box.getClass().getName()).append("@").append(Integer.toHexString(box.hashCode())).append("#").append(box.getFullName()).append(" : ");
        HashMap<Class, HashSet<ParameterSubtypeEnum>> dataChangedMap = box.m_dataChangedMap;
        Iterator<Class> itClass = dataChangedMap.keySet().iterator();
        while (itClass.hasNext()) {
            Class c = itClass.next();
            m_sb.append(c.getName()).append(" - ");
            HashSet<ParameterSubtypeEnum> subParameters = dataChangedMap.get(c);
            Iterator<ParameterSubtypeEnum> it = subParameters.iterator();
            while (it.hasNext()) {
                ParameterSubtypeEnum subtype = it.next();
                m_sb.append(subtype);
                if (it.hasNext()) {
                    m_sb.append(", ");
                }
            }

            if (itClass.hasNext()) {
                m_sb.append("  | ");
            }
        }
        m_logger.debug("propagateDataChanged() {}", m_sb.toString());
    }
    private static StringBuilder m_sb = new StringBuilder();
    
    public void setProjectId(long projectId) {
        if (m_panel != null) {
            getDataBoxPanelInterface().addSingleValue(m_projectId);
        }
        m_projectId.setId(projectId);
    }

    public long getProjectId() {
        ProjectId projectId = (ProjectId) getData(ProjectId.class);
        if (projectId == null) {
            return -1;
        }
        return projectId.getId();
    }

    public Long getRsetId() {
        if (m_previousDataBox != null) {
            return m_previousDataBox.getRsetId();
        }
        return null;
    }

    public Long getRsmId() {
        if (m_previousDataBox != null) {
            return m_previousDataBox.getRsmId();
        }
        return null;
    }

    public DataBoxPanelInterface getPanel() {
        return m_panel;
    }

    public void setLayout(SplittedPanelContainer.PanelLayout layout) {
        m_layout = layout;
    }

    public SplittedPanelContainer.PanelLayout getLayout() {
        return m_layout;
    }

    public String getTypeName() {
        return m_typeName;
    }

    public void setDataName(String dataName) {
        m_dataName = dataName;
    }

    public String getDataName() {

        if (m_dataName != null) {
            return m_dataName;
        }

        if (m_previousDataBox != null) {
            return m_previousDataBox.getDataName();
        }

        return null;
    }

    public String getFullName() {
        if (m_userName != null) {
            return m_userName;
        }
        if (m_fullName != null) {
            return m_fullName;
        }
        // try to construct a full name
        String dataName = getDataName();
        if (dataName != null) {
            m_fullName = dataName + ' ' + m_typeName;
            return m_fullName;
        }

        return getTypeName();
    }

    @Override
    public String getMemoryClientName() {
        return getFullName();
    }
    @Override
    public String getMemoryDataName() {
        String dataName = getDataName();
        return (dataName != null) ? dataName : "";
    }

    public String getDescription() {
        return m_description;
    }

    public void windowClosed() {
        deleteThis();
    }

    public void windowOpened() {
    }

    @Override
    public void stateChanged(ChangeEvent e) {
    }

    public int setLoading(boolean andCalculating) {
        final int loadingId = m_loadingId++;
        getDataBoxPanelInterface().setLoading(loadingId, andCalculating);
        return loadingId;
    }

    public int setLoading() {
        final int loadingId = m_loadingId++;
        getDataBoxPanelInterface().setLoading(loadingId);
        return loadingId;
    }

    public void setLoaded(int loadingId) {
        getDataBoxPanelInterface().setLoaded(loadingId);
    }

    protected void selectDataWhenLoaded(HashSet data) {

    }

    @Override
    public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel) {
        return new RemoveDataBoxActionListener(splittedPanel, this);
    }

    @Override
    public ActionListener getAddAction(SplittedPanelContainer splittedPanel) {
        return new AddDataBoxActionListener(splittedPanel, this);
    }

    @Override
    public ActionListener getSaveAction(SplittedPanelContainer splittedPanel) {
        return new SaveDataBoxActionListener(splittedPanel);
    }

    @Override
    public int getLoadingPercentage() {
        if (m_taskMap.isEmpty()) {
            return 100;
        }

        float percentage = 0;
        int nb = 0;
        Iterator<TaskInfo> it = m_taskMap.values().iterator();
        while (it.hasNext()) {
            TaskInfo info = it.next();
            percentage += info.getPercentage();
            nb++;
        }
        percentage /= nb;

        return (int) Math.round(percentage);
    }

    @Override
    public boolean isLoaded() {
        if (m_taskMap.isEmpty()) {
            return true;
        }

        Iterator<TaskInfo> it = m_taskMap.values().iterator();
        while (it.hasNext()) {
            TaskInfo info = it.next();
            if (!info.isFinished() && !info.isAborted()) {
                return false;
            }
        }

        return true;
    }

    public void retrieveTableModels(ArrayList<TableInfo> list) {

        if (m_panel instanceof GlobalTabelModelProviderInterface) {
            JXTable table = ((GlobalTabelModelProviderInterface) m_panel).getGlobalAssociatedTable();
            if (table != null) {
                TableInfo info = new TableInfo(getId(), m_userName, getDataName(), getTypeName(), table);
                list.add(info);
            }
        }

        if (m_nextDataBoxArray != null) {
            for (AbstractDataBox nextDataBox : m_nextDataBoxArray) {
                nextDataBox.retrieveTableModels(list);
            }
        }
    }

    public ArrayList<AbstractDataBox> getNextDataBoxArray() {
        return m_nextDataBoxArray;
    }

    public Class[] getDataboxNavigationOutParameterClasses() {
        return null;
    }

    public String getDataboxNavigationDisplayValue() {
        return null;
    }

}
