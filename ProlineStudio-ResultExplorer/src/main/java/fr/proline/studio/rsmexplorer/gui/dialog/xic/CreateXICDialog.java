package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.studio.rsmexplorer.tree.xic.XICDesignTree;
import fr.proline.core.orm.uds.Aggregation;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.RawFile;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.util.DStoreCustomPoolConnectorFactory;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.data.RunInfoData;
import fr.proline.studio.dam.data.RunInfoData.Status;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabasePeaklistTask;
import fr.proline.studio.dam.tasks.DatabaseRunsTask;
import fr.proline.studio.dam.tasks.DatabaseVerifySpectrumFromResultSets;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.LoadWaitingDialog;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.studio.rsmexplorer.tree.xic.XICBiologicalSampleAnalysisNode;
import fr.proline.studio.rsmexplorer.tree.xic.XICReferenceRSMNode;
import fr.proline.studio.rsmexplorer.tree.xic.XICRunNode;
import fr.proline.studio.settings.FilePreferences;
import fr.proline.studio.settings.SettingsDialog;
import fr.proline.studio.settings.SettingsUtils;
import fr.proline.studio.utils.IconManager;
import fr.proline.studio.utils.StudioExceptions;
import java.awt.Dialog;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Window;
import java.io.File;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.persistence.EntityManager;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dialog to build a XIC Design
 *
 * @author JM235353
 */
public class CreateXICDialog extends DefaultDialog {
    
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");

    private static final int STEP_PANEL_CREATE_XIC_DESIGN = 0;
    private static final int STEP_PANEL_DEFINE_RAW_FILES = 1;
    private static final int STEP_PANEL_DEFINE_XIC_PARAMS = 2;
    private int m_step = STEP_PANEL_CREATE_XIC_DESIGN;
    
    private static final String SETTINGS_KEY = "XIC";
    private static CreateXICDialog m_singletonDialog = null;
    
    private XICDesignTree m_designTree = null;
    private IdentificationTree m_selectionTree = null;
    private AbstractNode m_finalXICDesignNode = null;

    private DDataset m_refDataset = null;
    private Long m_refResultSummaryId = null;
    private SelectRawFilesPanel m_selectRawFilePanel;
    private DatabaseVerifySpectrumFromResultSets m_spectrumTask;

    public static CreateXICDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new CreateXICDialog(parent);
        } else {
            if (m_singletonDialog.m_selectRawFilePanel != null) {
                m_singletonDialog.m_selectRawFilePanel.resetDropZonePanel();
            }
        }

        return m_singletonDialog;
    }

    private CreateXICDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("XIC Quantitation Wizard");

        setDocumentationSuffix("id.2dlolyb");

        setSize(1000, 768);
        setResizable(true);

    }

    public void setSelectableIdentTree(IdentificationTree selectionTree) {
        m_selectionTree = selectionTree;
    }

    public void setReferenceIdentDataset(DDataset parentDS) {
        m_refDataset = parentDS;
    }

    /**
     * 
     * XIC Dialog params should be set before :
     * - selectionTree using setSelectableIdentTree
     * - parentDS : reference DS used for XIC using setReferenceIdentDataset
     * 
     */
    public void displayExperimentalDesignTree() {
        DataSetNode rootNode = new DataSetNode(new DataSetData("XIC", Dataset.DatasetType.QUANTITATION, Aggregation.ChildNature.QUANTITATION_FRACTION));
        if (m_refDataset != null) {
            XICReferenceRSMNode refDatasetNode = new XICReferenceRSMNode(new DataSetData(m_refDataset.getName(), Dataset.DatasetType.AGGREGATE, Aggregation.ChildNature.OTHER));
            rootNode.add(refDatasetNode);
        }
        displayExperimentalDesignTree(rootNode);
    }

    private void displayExperimentalDesignTree(AbstractNode finalXICDesignNode) {

        m_finalXICDesignNode = finalXICDesignNode;
        m_step = STEP_PANEL_CREATE_XIC_DESIGN;

        setButtonName(DefaultDialog.BUTTON_OK, "Next");
        setButtonIcon(DefaultDialog.BUTTON_OK, IconManager.getIcon(IconManager.IconType.ARROW));
        setButtonVisible(BUTTON_LOAD, false);
        setButtonVisible(BUTTON_SAVE, false);
        setButtonVisible(BUTTON_BACK, false);

        // Update and Replace panel
        CreateXICDesignPanel createXICDesignPanel = CreateXICDesignPanel.getPanel(m_finalXICDesignNode, m_selectionTree);
        replaceInternalComponent(createXICDesignPanel);
        m_designTree = createXICDesignPanel.getDesignTree();
        revalidate();
        repaint();

    }

    protected void displayDefineRawFiles() {

        m_step = STEP_PANEL_DEFINE_RAW_FILES;

        setButtonName(DefaultDialog.BUTTON_OK, "Next");
        setButtonIcon(DefaultDialog.BUTTON_OK, IconManager.getIcon(IconManager.IconType.ARROW));
        setButtonVisible(BUTTON_LOAD, false);
        setButtonVisible(BUTTON_SAVE, false);
        setButtonVisible(BUTTON_BACK, true);

        // Update and Replace panel
        m_selectRawFilePanel = SelectRawFilesPanel.getPanel(m_finalXICDesignNode, m_designTree);
        replaceInternalComponent(m_selectRawFilePanel);
        revalidate();
        repaint();

    }

    protected void displayDefineXICParams() {
        m_step = STEP_PANEL_DEFINE_XIC_PARAMS;
 
        setButtonName(DefaultDialog.BUTTON_OK, org.openide.util.NbBundle.getMessage(DefaultDialog.class, "DefaultDialog.okButton.text"));
        setButtonIcon(DefaultDialog.BUTTON_OK, IconManager.getIcon(IconManager.IconType.OK));

        setButtonVisible(BUTTON_BACK, true);
        setButtonVisible(BUTTON_LOAD, true);
        setButtonVisible(BUTTON_SAVE, true);

        // Update and Replace panel
        //QUANTV2 : comment to use V1
        DefineQuantParamsPanel.setUsePanelV2(true);
        DefineQuantParamsPanel quantPanel = DefineQuantParamsPanel.getDefineQuantPanel();
        quantPanel.getParamsPanel().resetScrollbar();
        replaceInternalComponent(quantPanel);
        revalidate();
        repaint();
    }
    
    public String getParamVersion(){
        return DefineQuantParamsPanel.getDefineQuantPanel().getParamsVersion();
    }
    
    @Override
    public void pack() {
        // forbid pack by overloading the method
    }

    public AbstractNode getExperimentalDesignNode() {
        return m_finalXICDesignNode;
    }

    private void updatePeaklist(RunInfoData runInfoData, long projectID, long resultSetID) {
        if (runInfoData.getStatus() == Status.LINKED_IN_DATABASE || runInfoData.getStatus() == Status.USER_DEFINED || runInfoData.getStatus() == Status.SYSTEM_PROPOSED) {

            RawFile rawFile = (runInfoData.getSelectedRawFile() == null) ? runInfoData.getLinkedRawFile() : runInfoData.getSelectedRawFile();

            if (rawFile == null) {
                return; //should not occurs TODO error ?                
            }

            // ask asynchronous loading of data
            DatabasePeaklistTask task = new DatabasePeaklistTask(null);
            task.initUpdatePeaklistIdentifier(projectID, resultSetID, rawFile.getIdentifier());
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
        }
    }

    public String registerRawFiles() {

        long pID = ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject().getId();

        Project project;
        String errorMsg = null;
        EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();

        try {
            project = entityManagerUDS.find(Project.class, pID);

            final Object mutexFileRegistered = new Object();

            Enumeration xicGroups = m_finalXICDesignNode.children();

            while (xicGroups.hasMoreElements() && errorMsg == null) {
                AbstractNode grpNode = (AbstractNode) xicGroups.nextElement();

                //Iterate over Samples
                Enumeration groupSamples = grpNode.children();
                while (groupSamples.hasMoreElements() && errorMsg == null) {

                    AbstractNode groupSampleNode = (AbstractNode) groupSamples.nextElement();

                    //Iterate over SampleAnalysis
                    Enumeration identiResultSummaries = groupSampleNode.children();

                    while (identiResultSummaries.hasMoreElements()) {

                        AbstractNode biologicalSampleAnalysisNode = (AbstractNode) identiResultSummaries.nextElement();
                        Long rsID = ((DataSetNode) biologicalSampleAnalysisNode).getDataset().getResultSetId();
                        Enumeration runNodes = biologicalSampleAnalysisNode.children();

                        while (runNodes.hasMoreElements()) {

                            XICRunNode runNode = (XICRunNode) runNodes.nextElement();
                            RunInfoData runData = (RunInfoData) runNode.getData();
                            if (!runData.isRunInfoInDatabase()) {
                                // RawFile does not exists, create it
                                try {
                                    synchronized (mutexFileRegistered) {
                                        // TODO : get the right instrumentId !!! 
                                        long instrumentID = 1;

                                        AbstractJMSCallback registerRawFileCallback = new AbstractJMSCallback() {

                                            @Override
                                            public boolean mustBeCalledInAWT() {
                                                return false;
                                            }

                                            @Override
                                            public void run(boolean success) {
                                                if (success) {
                                                    updatePeaklist(runData, pID, rsID);
                                                }
                                                synchronized (mutexFileRegistered) {
                                                    mutexFileRegistered.notifyAll();
                                                }
                                            }
                                        };

                                        fr.proline.studio.dpm.task.jms.RegisterRawFileTask task = new fr.proline.studio.dpm.task.jms.RegisterRawFileTask(registerRawFileCallback, instrumentID, project.getOwner().getId(), runData);
                                        AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);

                                        // wait until the files are loaded
                                        mutexFileRegistered.wait();
                                    }

                                } catch (InterruptedException ie) {
                                    // should not happen
                                    m_logger.error("", ie); 
                                }
                            } else {
                                this.updatePeaklist(runData, pID, rsID);
                            }

                            //  then map IdentificationDataset to RawFile and Run
                            if (runData.getRun().getId() > -1) { // case runId = -1 when coming from an existing xic, no need to register again the run_identification
                                DatabaseRunsTask registerRunIdsTask = new DatabaseRunsTask(null);
                                registerRunIdsTask.initRegisterIdentificationDatasetRun(((DataSetData) biologicalSampleAnalysisNode.getData()).getDataset().getId(), runData.getSelectedRawFile(), runData.getRun());
                                registerRunIdsTask.fetchData();
                            }
                        }
                    }
                }
            } //End go through group's sample

        } catch (Exception e) {
            errorMsg = "Raw File(s) Registration Failed.";
            LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error(getClass().getSimpleName() + " failed", e);
        } finally {
            entityManagerUDS.close();
        }

        return errorMsg;
    }

    public Map<String, Object> getExperimentalDesignParameters() throws IllegalAccessException {
        if (m_finalXICDesignNode == null) {
            throw new IllegalAccessException("Design parameters have not been set.");
        }
        Map<String, Object> experimentalDesignParams = XICDesignTree.toExperimentalDesignParameters(m_finalXICDesignNode, m_refDataset, m_refResultSummaryId);
        return experimentalDesignParams;
    }
    
    public Map<String, Object> getQuantiParameters() {
        return DefineQuantParamsPanel.getDefineQuantPanel().getParamsPanel().getQuantParams();
    }

    @Override
    protected boolean okCalled() {

        if (m_step == STEP_PANEL_CREATE_XIC_DESIGN) {//CreateXICDesignPanel-NEXT button

            //VDS: Can't checkDesignStructure and checkBiologicalGroupName be merged !! 
            if ((!checkDesignStructure(m_finalXICDesignNode, new HashSet<>())) || (!checkBiologicalGroupName(m_finalXICDesignNode))) {
                return false;
            }

            //Will run displayDefineRawFiles if Spectrum are OK ! 
            this.checkSpectrum();

            return false;
        } else if (m_step == STEP_PANEL_DEFINE_RAW_FILES) {//SelectRawFilesPanel-NEXT button

            if (!checkRawFiles()) {
                return false;
            }

            displayDefineXICParams();

            return false;
        } else { //STEP_PANEL_DEFINE_XIC_PARAMS , DefineQuantParamsPanel-OK button

            if (!checkQuantParameters()) {
                return false;
            }

            // Save Parameters  
            ParameterList parameterList = DefineQuantParamsPanel.getDefineQuantPanel().getParamsPanel().getParameterList();
            Preferences preferences = NbPreferences.root();
            parameterList.saveParameters(preferences);
            preferences.putBoolean(AbstractDefineQuantParamsPanel.XIC_SIMPLIFIED_PARAMS, DefineQuantParamsPanel.getDefineQuantPanel().getParamsPanel().isSimplifiedPanel());

            return true;
        }

    }

    private boolean checkQuantParameters() {

        ParameterList parameterList = DefineQuantParamsPanel.getDefineQuantPanel().getParamsPanel().getParameterList();

        // check parameters
        ParameterError error = parameterList.checkParameters();
        if (error != null) {
            setStatus(true, error.getErrorMessage());
            highlight(error.getParameterComponent());
            return false;
        }
        return true;
    }

    @Override
    protected boolean saveCalled() {
        // check parameters
        if (!checkQuantParameters()) {
            return false;
        }

        JFileChooser fileChooser = SettingsUtils.getFileChooser(SETTINGS_KEY);
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File f = fileChooser.getSelectedFile();
            FilePreferences filePreferences = new FilePreferences(f, null, "");

            // Save Parameters  
            ParameterList parameterList = DefineQuantParamsPanel.getDefineQuantPanel().getParamsPanel().getParameterList();
            parameterList.saveParameters(filePreferences);
            filePreferences.putBoolean(AbstractGenericQuantParamsPanel.XIC_SIMPLIFIED_PARAMS, DefineQuantParamsPanel.getDefineQuantPanel().getParamsPanel().isSimplifiedPanel());
            filePreferences.put(AbstractGenericQuantParamsPanel.XIC_PARAMS_VERSION_KEY, DefineQuantParamsPanel.getDefineQuantPanel().getParamsVersion());
            SettingsUtils.addSettingsPath(SETTINGS_KEY, f.getAbsolutePath());
            SettingsUtils.writeDefaultDirectory(SETTINGS_KEY, f.getParent());
        }

        return false;
    }

    @Override
    protected boolean loadCalled() {

        SettingsDialog settingsDialog = new SettingsDialog(this, SETTINGS_KEY);
        settingsDialog.setLocationRelativeTo(this);
        settingsDialog.setVisible(true);

        if (settingsDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            if (settingsDialog.isDefaultSettingsSelected()) {
                ParameterList parameterList = DefineQuantParamsPanel.getDefineQuantPanel().getParamsPanel().getParameterList();
                parameterList.initDefaults();
            } else {
                try {
                    File settingsFile = settingsDialog.getSelectedFile();
                    FilePreferences filePreferences = new FilePreferences(settingsFile, null, "");
                    if(Arrays.asList(filePreferences.keys()).contains(AbstractDefineQuantParamsPanel.XIC_SIMPLIFIED_PARAMS)){
                        boolean isSimplified = filePreferences.getBoolean(AbstractDefineQuantParamsPanel.XIC_SIMPLIFIED_PARAMS, true);
                        DefineQuantParamsPanel.getDefineQuantPanel().setIsSimplifiedPanel(isSimplified);                        
                    }
                    String version = filePreferences.get(AbstractGenericQuantParamsPanel.XIC_PARAMS_VERSION_KEY, "1.0");
                    String panelVersion = DefineQuantParamsPanel.getDefineQuantPanel().getParamsVersion();
                    if(!version.equals(panelVersion)){
                        String msg = "Can't load "+version+" quantitation parameters to "+panelVersion+" one. All parameters may not have been taken into account !";
                        JOptionPane.showMessageDialog(this, msg, "Load XIC parameters error",JOptionPane.ERROR_MESSAGE);
                    }
                    DefineQuantParamsPanel.getDefineQuantPanel().getParamsPanel().loadParameters(filePreferences);

                } catch (HeadlessException | BackingStoreException e) {
                    LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error("Parsing of User Settings File Failed", e);
                    setStatus(true, "Parsing of your Settings File failed");
                }
            }
        }

        return false;
    }

    @Override
    protected boolean backCalled() {
        if (m_step == STEP_PANEL_DEFINE_RAW_FILES) {

            m_selectRawFilePanel.pruneDesignTree();
            displayExperimentalDesignTree(m_finalXICDesignNode);

            return false;
        } else if (m_step == STEP_PANEL_DEFINE_XIC_PARAMS) {

            displayDefineRawFiles();

            return false;
        }

        return false;

    }

    private Map<Long, DataSetNode> getSampleAnalysisNodesPerRsId(AbstractNode parentNode) {
        Map<Long, DataSetNode> spectraNodesPerRsId = new HashMap<>();
        Enumeration children = parentNode.children();
        //Iterate over Groups
        while (children.hasMoreElements()) {
            AbstractNode currentChild = (AbstractNode) children.nextElement();
            AbstractNode.NodeTypes type = currentChild.getType();
            if (type == AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS) {
                Long rsID = ((DataSetNode) currentChild).getDataset().getResultSetId();

                XICBiologicalSampleAnalysisNode.SpectrumVerificationStatus verificationStatus = ((XICBiologicalSampleAnalysisNode) currentChild).getVerificationStatus();

                if (verificationStatus == XICBiologicalSampleAnalysisNode.SpectrumVerificationStatus.NOT_VERIFIED || verificationStatus == XICBiologicalSampleAnalysisNode.SpectrumVerificationStatus.UNSUCCESSFULLY_VERIFIED) {
                    spectraNodesPerRsId.put(rsID, (DataSetNode) currentChild);
                }

            } else {
                spectraNodesPerRsId.putAll(getSampleAnalysisNodesPerRsId(currentChild));
            }
        }
        return spectraNodesPerRsId;
    }

    private void checkSpectrum() {

        Map<Long, DataSetNode> spectraNodesPerRsId = getSampleAnalysisNodesPerRsId(m_finalXICDesignNode);

        if (spectraNodesPerRsId.size() > 0) {

            Long projectId = spectraNodesPerRsId.values().iterator().next().getDataset().getProject().getId();
            List<Long> resultSetIds = new ArrayList<>();
            resultSetIds.addAll(spectraNodesPerRsId.keySet());
            List<Long> failedRSIds = new ArrayList<>();
            Map<Long, List<Long>> failedSpectraPerRSIds = new HashMap<>();
            //Add waiting dialog while checking spectrum file 
            String waitingTxt = "Please wait while checking Identification Spectra...";
            final LoadWaitingDialog loadWaitingDialog = new LoadWaitingDialog(WindowManager.getDefault().getMainWindow(), waitingTxt);
            DefaultDialog.ProgressTask loadConfigTasktask = new DefaultDialog.ProgressTask() {

                @Override
                public int getMinValue() {
                    return 0;
                }

                @Override
                public int getMaxValue() {
                    return 100; 
                }

                @Override
                protected Object doInBackground() throws Exception {
                    
                    AbstractDatabaseCallback spectraTestCallback = new AbstractDatabaseCallback() {
                        @Override
                        public boolean mustBeCalledInAWT() {
                            return true;
                        }

                        @Override
                        public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                            loadWaitingDialog.setVisible(false);
                            if (success) {

                                for (Map.Entry<Long, DataSetNode> entry : spectraNodesPerRsId.entrySet()) {
                                    if (failedSpectraPerRSIds.containsKey(entry.getKey())) {
                                        ((XICBiologicalSampleAnalysisNode) entry.getValue()).setVerificationStatus(XICBiologicalSampleAnalysisNode.SpectrumVerificationStatus.SUCCESSFULLY_VERIFIED);
                                    } else {
                                        ((XICBiologicalSampleAnalysisNode) entry.getValue()).setVerificationStatus(XICBiologicalSampleAnalysisNode.SpectrumVerificationStatus.UNSUCCESSFULLY_VERIFIED);
                                    }
                                }

                                if (failedRSIds.isEmpty()) {

                                    for (Map.Entry<Long, DataSetNode> entry : spectraNodesPerRsId.entrySet()) {
                                        ((XICBiologicalSampleAnalysisNode) entry.getValue()).setVerificationStatus(XICBiologicalSampleAnalysisNode.SpectrumVerificationStatus.SUCCESSFULLY_VERIFIED);
                                    }

                                    displayDefineRawFiles();

                                } else if (failedRSIds.size() == 1) {
                                    DataSetNode dsNode = spectraNodesPerRsId.get(failedRSIds.get(0));
                                    showErrorOnNode(dsNode, dsNode.getDataset().getName() + " at least one of the following attributes {First Time, First Scan, First Cycle} must be initialized. Remove the highlighted node from your design.");
                                } else if (failedRSIds.size() > 1) {
                                    ArrayList<String> failedNodes = new ArrayList<>();

                                    for (Long failedRSId : failedRSIds) {
                                        failedNodes.add(spectraNodesPerRsId.get(failedRSId).toString());
                                    }

                                    JList failedList = new JList(failedNodes.toArray());

                                    JOptionPane.showMessageDialog(rootPane, failedList, "The following datasets failed spectrum check.", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        }
                    };

                    m_spectrumTask = new DatabaseVerifySpectrumFromResultSets(spectraTestCallback, resultSetIds, projectId, failedRSIds, failedSpectraPerRSIds);
                    AccessDatabaseThread.getAccessDatabaseThread().addTask(m_spectrumTask);
                           
                    return null;
                }

            };

            
            loadWaitingDialog.setTask(loadConfigTasktask);
            Point p = this.getLocation();
            loadWaitingDialog.setLocation(p.x,p.y);
            loadWaitingDialog.setVisible(true);
        } else {
            displayDefineRawFiles();
        }
    }

    private boolean checkDesignStructure(AbstractNode parentNode, Set<String> duplicates) {
        AbstractNode.NodeTypes type = parentNode.getType();

        switch (type) {
            case DATA_SET:
                if (parentNode.isLeaf()) {
                    showErrorOnNode(parentNode, "Your XIC Design is empty.");
                    return false;
                }
                break;
            case BIOLOGICAL_GROUP:
                if (parentNode.isLeaf()) {
                    showErrorOnNode(parentNode, "You must add at least one Biological Sample for each Biological Group.");
                    return false;
                }
                break;
            case BIOLOGICAL_SAMPLE:
                if (parentNode.isLeaf()) {
                    showErrorOnNode(parentNode, "You must add at least one Identification for each Biological Sample.");
                    return false;
                }
                break;
            case BIOLOGICAL_SAMPLE_ANALYSIS:
                if (parentNode.isLeaf()) {
                    if (duplicates.contains(parentNode.toString())) {
                        showErrorOnNode(parentNode, "Biological Sample Analysis is a duplicate. Please rename using right click!");
                        return false;
                    } else {
                        duplicates.add(parentNode.toString());
                        return true;
                    }
                }
                break;
        }

        Enumeration children = parentNode.children();
        //Iterate over Groups
        while (children.hasMoreElements()) {
            AbstractNode rsmNode = (AbstractNode) children.nextElement();
            if (!checkDesignStructure(rsmNode, duplicates)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Issue 13023: check the biological group names and biological sample
     * names: must be unique
     *
     * @param parentNode
     * @return
     */
    private boolean checkBiologicalGroupName(AbstractNode parentNode) {
        List<String> listBiologicalGroupName = new ArrayList();
        Enumeration children = parentNode.children();
        //Iterate over Groups
        while (children.hasMoreElements()) {
            AbstractNode rsmNode = (AbstractNode) children.nextElement();
            AbstractNode.NodeTypes type = rsmNode.getType();
            switch (type) {
                case BIOLOGICAL_GROUP: {
                    if (listBiologicalGroupName.contains(rsmNode.getData().getName())) {
                        showErrorOnNode(rsmNode, "The Biological Group name must be unique.");
                        return false;
                    }
                    listBiologicalGroupName.add(rsmNode.getData().getName());
                    List<String> listBiologicalSampleName = new ArrayList();
                    //Iterate over Samples
                    Enumeration childrenS = rsmNode.children();
                    while (childrenS.hasMoreElements()) {
                        AbstractNode sampleNode = (AbstractNode) childrenS.nextElement();
                        AbstractNode.NodeTypes typeS = sampleNode.getType();
                        switch (typeS) {
                            case BIOLOGICAL_SAMPLE: {
                                if (listBiologicalSampleName.contains(sampleNode.getData().getName())) {
                                    showErrorOnNode(sampleNode, "The Biological Sample name must be unique.");
                                    return false;
                                }
                                listBiologicalSampleName.add(sampleNode.getData().getName());
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean checkRawFiles() {
        boolean check = SelectRawFilesPanel.getPanel().check(this);
        if (!check) {
            setStatus(true, "You must specify a Raw(mzDb) File for each Sample Analysis.");
        }
        return check;
    }

    private void showErrorOnNode(AbstractNode node, String error) {

        // expand parentnode if needed
        AbstractNode parentNode = (AbstractNode) node.getParent();
        if (parentNode != null) {
            TreePath pathToExpand = new TreePath(parentNode.getPath());
            if (!m_designTree.isExpanded(pathToExpand)) {
                m_designTree.expandPath(pathToExpand);
            }
        }

        // scroll to node if needed
        TreePath path = new TreePath(node.getPath());
        m_designTree.scrollPathToVisible(path);

        // display error
        setStatus(true, error);
        highlight(m_designTree, m_designTree.getPathBounds(path));
    }

    @Override
    protected boolean cancelCalled() {
        return true;
    }

    /**
     * 
     * XIC Dialog params should be set before :
     * - selectionTree using setSelectableIdentTree
     * - parentDS : reference DS used for XIC using setReferenceIdentDataset
     * 
     */
    public void copyClonedDesignTree(DDataset xicDataset2Clone) {
        AbstractNode rootNode = (AbstractNode) m_designTree.getModel().getRoot();
        XICDesignTree.displayExperimentalDesign(xicDataset2Clone, rootNode, m_designTree, false, false);
        m_designTree.renameXicTitle(xicDataset2Clone.getName() + "-Copy");
        if(rootNode.getChildCount()>0){
            AbstractNode firstChildNode = (AbstractNode) rootNode.getChildAt(0);
            if(XICReferenceRSMNode.class.isInstance(firstChildNode)){
                if(((XICReferenceRSMNode)firstChildNode).isRefDatasetIncorrect()){
                    //Save previous RSM associated to reference dataset to use the same one !
                    m_refResultSummaryId = xicDataset2Clone.getMasterQuantitationChannels().get(0).getIdentResultSummaryId();
                    CreateXICDesignPanel.getPanel().updatePanel();
                }
            }
        }
        
        try {            
            //QUANTV2: uncomment until END V2 to use V2
            DefineQuantParamsPanel.setUsePanelV2(true);
            Map<String, Object> quantConfig = xicDataset2Clone.getQuantProcessingConfigAsMap();
            if(quantConfig.containsKey("config_version") && quantConfig.get("config_version").equals("2.0")) { //TODO to create V2 panel even if clone from V1
                DefineQuantParamsPanel.getDefineQuantPanel().setIsSimplifiedPanel(false); //to view all config 
                DefineQuantParamsPanel.getDefineQuantPanel().getParamsPanel().setQuantParams(quantConfig);
            } else {
                String msg = "Can't clone old quantitation parameters to new one. Default parameters will be used !!";
                JOptionPane.showMessageDialog(this, msg, "Clone Error",JOptionPane.ERROR_MESSAGE);
                ParameterList parameterList = DefineQuantParamsPanel.getDefineQuantPanel().getParamsPanel().getParameterList();
                parameterList.initDefaults();
                DefineQuantParamsPanel.getDefineQuantPanel().setIsSimplifiedPanel(true);
            }
            //END V2
            //QUANTV2 : uncomment until END V1 to use V1
//            DefineQuantParamsPanel.setUsePanelV2(false);
//            Map<String, Object> quantConfig = xicDataset2Clone.getQuantProcessingConfigAsMap();
//            if(quantConfig.containsKey("config_version")) { 
//                String msg = "Can't clone new quantitation parameters to old one. Default parameters will be used !!";
//                JOptionPane.showMessageDialog(this, msg, "Clone Error",JOptionPane.ERROR_MESSAGE);   
//                DefineQuantParamsPanel.getDefineQuantPanel().setIsSimplifiedPanel(true);
//                ParameterList parameterList = DefineQuantParamsPanel.getDefineQuantPanel().getParamsPanel().getParameterList();
//                parameterList.initDefaults();                
//            } else {
//                DefineQuantParamsPanel.getDefineQuantPanel().setIsSimplifiedPanel(false); //to view all config 
//                DefineQuantParamsPanel.getDefineQuantPanel().getParamsPanel().setQuantParams(quantConfig);
//            }
//            //END V1
        } catch (Exception ex) {
            LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error("Error while setting Quant Param ", ex);
            StudioExceptions.notify("An error occured while cloning XIC parameters", ex);
        }
    }

}
