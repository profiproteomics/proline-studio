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

import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.tasks.AbstractDatabaseTask;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.id.ProjectId;
import fr.proline.studio.pattern.xic.DataboxChildFeature;
import fr.proline.studio.pattern.xic.DataboxExperimentalDesign;
import fr.proline.studio.pattern.xic.DataboxMapAlignment;
import fr.proline.studio.pattern.xic.DataboxPSMOfMasterQuantPeptide;
import fr.proline.studio.pattern.xic.DataboxXicPeptideIon;
import fr.proline.studio.pattern.xic.DataboxXicPeptideSet;
import fr.proline.studio.pattern.xic.DataboxXicProteinSet;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.table.TableInfo;
import fr.proline.studio.utils.IconManager;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jdesktop.swingx.JXTable;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 *
 * A Box receive IN-parameters. According to them, it loads data from the
 * database. These data are displayed to the user in an associated Graphical
 * Panel. If the user select data in the panel, the box offers the selected data
 * as OUT-parameters for the next dataBox.
 *
 * @author JM235353
 */
public abstract class AbstractDataBox implements ChangeListener, ProgressInterface, SplittedPanelContainer.UserActions {
    //private static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer.AbstractDataBox");
    // Panel corresponding to this box
    private DataBoxPanelInterface m_panel;

    // In and out Parameters Registered
    private final HashSet<GroupParameter> m_inParameters = new HashSet<>();
    private final ArrayList<GroupParameter> m_outParameters = new ArrayList<>();

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
        DataBoxPTMPeptidesGraphic(57)
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
                case DataBoxPTMSiteProtein:
                    return new DataBoxPTMSiteProtein();
                case DataBoxDataAnalyzerResults:
                    return new DataBoxDataAnalyzerResults();
                case DataBoxImage:
                    return new DataBoxImage();
                case DataBoxSystemTasks:
                    return new DataBoxSystemTasks();
                case DataBoxPTMSitePeptides:
                    return new DataBoxPTMSitePeptides();
                case DataBoxPTMSitePepMatches:
                    return new DataBoxPTMSitePepMatches();
                case DataBoxPTMSitePeptidesGraphic:
                    return new DataBoxPTMSitePeptidesGraphic();
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
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(ProjectId.class, false);
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

    protected final void registerInParameter(GroupParameter parameter) {
        m_inParameters.add(parameter);
    }

    protected final void registerOutParameter(GroupParameter parameter) {
        m_outParameters.add(parameter);
    }

    public ArrayList<GroupParameter> getOutParameters() {
        return m_outParameters;
    }

    public HashSet<GroupParameter> getInParameters() {
        return m_inParameters;
    }

    public boolean isDataDependant(Class dataType) {
        Iterator<GroupParameter> it = m_inParameters.iterator();
        while (it.hasNext()) {
            GroupParameter parameter = it.next();
            if (parameter.isDataDependant(dataType)) {
                return true;
            }
        }
        return false;
    }

    public boolean isDataProvider(Class dataType) {
        Iterator<GroupParameter> it = m_outParameters.iterator();
        while (it.hasNext()) {
            GroupParameter parameter = it.next();
            if (parameter.isDataDependant(dataType)) {
                return true;
            }
        }
        return false;
    }

    public void loadedDataModified(Long rsetId, Long rsmId, Class dataType, ArrayList modificationsList, int reason) {
        if (isDataProvider(dataType)) {
            dataMustBeRecalculated(rsetId, rsmId, dataType, modificationsList, reason);
        }
        if (m_nextDataBoxArray != null) {
            for (AbstractDataBox nextDataBox : m_nextDataBoxArray) {
                nextDataBox.loadedDataModified(rsetId, rsmId, dataType, modificationsList, reason);
            }
        }
    }

    public double calculateParameterCompatibilityDistance(ArrayList<GroupParameter> outParameters) {
        Iterator<GroupParameter> it = m_inParameters.iterator();
        while (it.hasNext()) {
            GroupParameter parameter = it.next();

            if (parameter.isCompatibleWithOutParameter(outParameters)) {
                return 0;
            }
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
     * @param dataType
     */
    public void dataMustBeRecalculated(Long rsetId, Long rsmId, Class dataType, ArrayList modificationsList, int reason) {

    }

    // VDS FIXME : getArray : ?? Not used 
    public Object getData(boolean getArray, Class parameterType) {

        if ((parameterType != null) && (parameterType.equals(ProjectId.class))) {
            if ((m_projectId == null) || (m_projectId.getId() == -1L)) {
                if (m_previousDataBox != null) {
                    return m_previousDataBox.getData(getArray, parameterType);
                }
            }
            return m_projectId;
        }

        if (m_previousDataBox != null) {
            return m_previousDataBox.getData(getArray, parameterType);
        }
        return null;
    }

    // VDS: FIXME
    // isList => return a List of object from data => transformToList ?? 
    public Object getData(boolean getArray, Class parameterType, boolean isList) {

        if ((parameterType != null) && (parameterType.equals(ProjectId.class))) {
            return m_projectId;
        }

        if (m_previousDataBox != null) {
            return m_previousDataBox.getData(getArray, parameterType, isList);
        }
        return null;
    }

    public void setEntryData(Object data) {
        throw new UnsupportedOperationException();
    }

    public void propagateDataChanged(Class dataType) {
        if (m_nextDataBoxArray != null) {
            for (AbstractDataBox nextDataBox : m_nextDataBoxArray) {
                if (nextDataBox.isDataDependant(dataType)) { 
                    //m_logger.debug("nexDataBox:{} isDataDependant for datatype: {}",nextDataBox.getTypeName(), dataType.getName());
                    nextDataBox.dataChanged();
                }
                nextDataBox.propagateDataChanged(dataType);
            }
        }

    }

    public void setProjectId(long projectId) {
        if (m_panel != null) {
            getDataBoxPanelInterface().addSingleValue(m_projectId);
        }
        m_projectId.setId(projectId);
    }

    public long getProjectId() {
        ProjectId projectId = (ProjectId) getData(false, ProjectId.class);
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

    public Class[] getImportantInParameterClass() {
        return null;
    }

    public String getImportantOutParameterValue() {
        return null;
    }

}
