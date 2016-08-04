package fr.proline.studio.pattern;

import fr.proline.studio.comparedata.GlobalTabelModelProviderInterface;
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
import fr.proline.studio.python.data.TableInfo;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jdesktop.swingx.JXTable;

/**
 *
 * A Box receive IN-parameters. According to them, it loads data from the database.
 * These data are displayed to the user in an associated Graphical Panel.
 * If the user select data in the panel, the box offers the selected data as OUT-parameters
 * for the next dataBox.
 * 
 * @author JM235353
 */
public abstract class AbstractDataBox implements ChangeListener, ProgressInterface, SplittedPanelContainer.UserActions {
   

    
    // Panel corresponding to this box
    protected DataBoxPanelInterface m_panel;
    
    // In and out Parameters Registered
    private final HashSet<GroupParameter> m_inParameters = new HashSet<>();
    private final ArrayList<GroupParameter> m_outParameters = new ArrayList<>();
    
    
    private final HashMap<Long, TaskInfo> m_taskMap = new HashMap<>();
    
    private ProjectId m_projectId = new ProjectId();
    
    protected String m_typeName;
    protected String m_dataName;
    protected String m_fullName = null;
    protected String m_description = "";
    
    private SplittedPanelContainer.PanelLayout m_layout = SplittedPanelContainer.PanelLayout.VERTICAL;
    
    protected ArrayList<AbstractDataBox> m_nextDataBoxArray = null;
    protected AbstractDataBox m_previousDataBox = null;
    
    private int m_loadingId = 0;
    
    private int m_id = -1;
    private static int m_idCount = 0;
    
    private Image m_icon;
    
    protected DataboxType m_type;
    
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
        //DataboxSelectCompareData(23),
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
        DataBoxPTMProteinSite(42),
        DataBoxDataAnalyzerResults(43),
        DataBoxImage(44),
        DataBoxSystemTasks(45);
        
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
                case DataboxXicPeptideSet:
                    return new DataboxXicPeptideSet();
                case DataboxRsetMSDiag:
                	return new DataBoxRsetMSDiag(null);
                case DataboxXicPeptideIon:
                    return new DataboxXicPeptideIon();
                case DataboxGraphics:
                    return new DataboxGraphics();
                case DataboxXicChildFeature:
                    return new DataboxChildFeature();
                case DataboxMultiGraphics:
                    return new DataboxMultiGraphics();
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
                    return new DataboxRSMPSMForMsQuery();
                case DataBoxRsetPeptideSpectrumValues:
                    return new DataBoxRsetPeptideSpectrumValues();
                case DataBoxMSQueriesForRset:
                    return new DataBoxMSQueriesForRset();
                case DataBoxRsetPSMForMsQuery:
                    return new DataboxRsetPSMForMsQuery();
                case DataBoxPTMProteinSite:
                    return new DataBoxPTMProteinSite();
                case DataBoxDataAnalyzerResults:
                    return new DataBoxDataAnalyzerResults();
                case DataBoxImage:
                    return new DataBoxImage();
                case DataBoxSystemTasks:
                    return new DataBoxSystemTasks();
            }
            return null; // should not happen
        }
        
        private static HashMap<Integer, DataboxType> generateDataboxTypeMap() {
            HashMap<Integer, DataboxType> map = new HashMap<>();
            DataboxType[] databoxTypeArray = DataboxType.values();
            for (int i=0;i<databoxTypeArray.length;i++) {
                DataboxType type = databoxTypeArray[i];
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
    
    public AbstractDataBox(DataboxType type) {
        m_type = type;
        
        // Register possible out parameters
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(ProjectId.class, false);
        registerOutParameter(outParameter);
    }
    
    public DataboxType getType() {
        return m_type;
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
    
    protected void deleteThis() {
        
        // cancel task possibily running
        AccessDatabaseThread.getAccessDatabaseThread().abortTasks(m_taskMap.keySet());
     
        m_taskMap.clear();
    }
    
    /**
     * A task must be registered, so it can be cancelled
     * (a DataBox must not directly call AccessDatabaseThread.getAccessDatabaseThread().addTask()
     * method
     * @param task 
     */
    protected void registerTask(AbstractDatabaseTask task) {
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
        m_taskMap.put(task.getId(), task.getTaskInfo());
        
    }
    protected void unregisterTask(Long taskId) {
        m_taskMap.remove(taskId);
    }
 
    
    protected final void registerInParameter(GroupParameter parameter) {
        m_inParameters.add(parameter);
    }
    
    protected final void registerOutParameter(GroupParameter parameter)  {
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
    
    
    public double calculateParameterCompatibilityDistance(AvailableParameters avalaibleParameters, AbstractDataBox nextDataBox) {
        
        
        return avalaibleParameters.calculateParameterCompatibilityDistance(nextDataBox);

    }
    
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
     * To be overriden if the modification in a following databox
     * can lead to a modificiation of the data of the current databox.
     * (for instance, disabling peptides -> modifications of protein set
     * in the XIC View
     * @param dataType 
     */
    public void dataMustBeRecalculated(Long rsetId, Long rsmId, Class dataType, ArrayList modificationsList, int reason) {
        
    }
    
    public Object getData(boolean getArray, Class parameterType) {
        
        if ((parameterType!= null ) && (parameterType.equals(ProjectId.class))) {
            if ((m_projectId==null) || (m_projectId.getId() == -1l)) {
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
    
    public Object getData(boolean getArray, Class parameterType, boolean isList) {
        
        if ((parameterType!= null ) && (parameterType.equals(ProjectId.class))) {
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
                    nextDataBox.dataChanged();
                }
                nextDataBox.propagateDataChanged(dataType);
            }
        }
        
    }
 
    
    
    public void setProjectId(long projectId) {
        m_panel.addSingleValue(m_projectId);
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
        /*if (m_fullName != null) {
            int index = m_fullName.indexOf(m_typeName);
            if (index != -1) {
                return m_fullName.substring(0, index).trim();
            }
        }*/
        return null;
    }
    
    public String getFullName() {
        if (m_fullName != null) {
            return m_fullName;
        }
        // try to construct a full name
        String dataName = getDataName();
        if (dataName != null) {
            m_fullName = dataName+' '+m_typeName;
            return m_fullName;
        }

        return getTypeName();
    }
    
    /*public void setFullName(String fullName) {
        m_fullName = fullName;
    }*/
    
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
    
    
    protected int setLoading(boolean andCalculating) {
        final int loadingId = m_loadingId++;
        m_panel.setLoading(loadingId, andCalculating);
        return loadingId;
    }
    
    protected int setLoading() {
        final int loadingId = m_loadingId++;
        m_panel.setLoading(loadingId);
        return loadingId;
    }
    
    protected void setLoaded(int loadingId) {
        m_panel.setLoaded(loadingId);
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
            percentage +=info.getPercentage();
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
                TableInfo info = new TableInfo(getId(), getDataName(), getTypeName(), table);
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
    

}
