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
import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.jms.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.RegisterRawFileTask;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.LoadWaitingDialog;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.studio.rsmexplorer.tree.xic.XICBiologicalSampleAnalysisNode;
import fr.proline.studio.rsmexplorer.tree.xic.XICRunNode;
import fr.proline.studio.settings.FilePreferences;
import fr.proline.studio.settings.SettingsDialog;
import fr.proline.studio.settings.SettingsUtils;
import fr.proline.studio.utils.IconManager;
import java.awt.Dialog;
import java.awt.Point;
import java.awt.Window;
import java.io.File;
import java.util.*;
import java.util.prefs.Preferences;
import javax.persistence.EntityManager;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;
import org.slf4j.LoggerFactory;

/**
 * Dialog to build a XIC Design
 *
 * @author JM235353
 */
public class CreateXICDialog extends DefaultDialog {

    private static final int STEP_PANEL_CREATE_XIC_DESIGN = 0;
    private static final int STEP_PANEL_DEFINE_RAW_FILES = 1;
    private static final int STEP_PANEL_DEFINE_XIC_PARAMS = 2;
    private int m_step = STEP_PANEL_CREATE_XIC_DESIGN;
    private DDataset m_refDataset = null;

    private static final String SETTINGS_KEY = "XIC";

    private static CreateXICDialog m_singletonDialog = null;

    private AbstractNode m_finalXICDesignNode = null;
    private IdentificationTree m_selectionTree = null;

    private SelectRawFilesPanel m_selectRawFilePanel;
    private DatabaseVerifySpectrumFromResultSets m_spectrumTask;

    private HashMap<String, XICBiologicalSampleAnalysisNode> m_duplicates;

    private XICDesignTree m_designTree = null;

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

    public void setParentDataset(DDataset parentDS) {
        m_refDataset = parentDS;
    }

    public void displayDesignTree() {
        displayDesignTree(new DataSetNode(new DataSetData("XIC", Dataset.DatasetType.QUANTITATION, Aggregation.ChildNature.QUANTITATION_FRACTION)));
    }

    private void displayDesignTree(AbstractNode finalXICDesignNode) {

        m_finalXICDesignNode = finalXICDesignNode;
        m_step = STEP_PANEL_CREATE_XIC_DESIGN;

        setButtonName(DefaultDialog.BUTTON_OK, "Next");
        setButtonIcon(DefaultDialog.BUTTON_OK, IconManager.getIcon(IconManager.IconType.ARROW));
        setButtonVisible(BUTTON_LOAD, false);
        setButtonVisible(BUTTON_SAVE, false);
        setButtonVisible(BUTTON_BACK, false);

        // Update and Replace panel
        CreateXICDesignPanel createXICDesignPanel = CreateXICDesignPanel.getPanel(m_finalXICDesignNode, m_selectionTree);
        replaceInternaleComponent(createXICDesignPanel);
        m_designTree = createXICDesignPanel.getDesignTree();
        revalidate();
        repaint();

    }

    public void displayDefineRawFiles() {

        m_step = STEP_PANEL_DEFINE_RAW_FILES;

        setButtonName(DefaultDialog.BUTTON_OK, "Next");
        setButtonIcon(DefaultDialog.BUTTON_OK, IconManager.getIcon(IconManager.IconType.ARROW));
        setButtonVisible(BUTTON_LOAD, false);
        setButtonVisible(BUTTON_SAVE, false);
        setButtonVisible(BUTTON_BACK, true);

        // Update and Replace panel
        m_selectRawFilePanel = SelectRawFilesPanel.getPanel(m_finalXICDesignNode, m_designTree);
        replaceInternaleComponent(m_selectRawFilePanel);
        revalidate();
        repaint();

    }

    public void displayDefineXICParams() {
        m_step = STEP_PANEL_DEFINE_XIC_PARAMS;

        setButtonName(DefaultDialog.BUTTON_OK, org.openide.util.NbBundle.getMessage(DefaultDialog.class, "DefaultDialog.okButton.text"));
        setButtonIcon(DefaultDialog.BUTTON_OK, IconManager.getIcon(IconManager.IconType.OK));

        setButtonVisible(BUTTON_BACK, true);
        setButtonVisible(BUTTON_LOAD, true);
        setButtonVisible(BUTTON_SAVE, true);

        // Update and Replace panel
        DefineQuantParamsPanel quantPanel = DefineQuantParamsPanel.getDefineQuantPanel();
        quantPanel.getParamsPanel().resetScrollbar();
        replaceInternaleComponent(quantPanel);
        revalidate();
        repaint();
    }

    @Override
    public void pack() {
        // forbid pack by overloading the method
    }

    public AbstractNode getDesignRSMNode() {
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
    /*
     * Return map reflecting JSON obhject for experimental_design :
     "biological_samples": [{
     "name": "ff 0",
     "number": 0
     }],
     "master_quant_channels": [{
     "quant_channels": [{
     "ident_result_summary_id": 6,
     "number": 0,
     "sample_number": 0
     }],
     "name": "TEST 0206",
     "number": 0
     }],
     "group_setups": [{
     "biological_groups": [{
     "name": "br 0",
     "number": 0,
     "sample_numbers": [0]
     }],
     "ratio_definitions": [{
     "denominator_group_number": 0,
     "numerator_group_number": 0,
     "number": 0
     }],
     "name": "TEST 0206",
     "number": 0
     }]
     
     * @throws IllegalAccessException 
     */

    private HashMap<Long, Long> getRunIdForRSMs(Collection<Long> rsmIDs) {
        //Get Run Ids for specified RSMs
        Long pID = ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject().getId();
        HashMap<Long, Long> returnedRunIdsByRsmIds = new HashMap<>();
        DatabaseRunsTask loadRunIdsTask = new DatabaseRunsTask(null);
        loadRunIdsTask.initLoadRunIdsForRsms(pID, new ArrayList(rsmIDs), returnedRunIdsByRsmIds);
        loadRunIdsTask.fetchData();

        return returnedRunIdsByRsmIds;
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
                                boolean isJMSDefined = JMSConnectionManager.getJMSConnectionManager().isJMSDefined();
                                try {
                                    synchronized (mutexFileRegistered) {
                                        // TODO : get the right instrumentId !!! 
                                        long instrumentID = 1;
                                        if (isJMSDefined) {
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
                                        } else {
                                            AbstractServiceCallback callback = new AbstractServiceCallback() {

                                                @Override
                                                public boolean mustBeCalledInAWT() {
                                                    return false;
                                                }

                                                @Override
                                                public void run(boolean success) {
                                                    synchronized (mutexFileRegistered) {
                                                        mutexFileRegistered.notifyAll();
                                                    }
                                                }
                                            };

                                            RegisterRawFileTask task = new RegisterRawFileTask(callback, instrumentID, project.getOwner().getId(), runData);
                                            AccessServiceThread.getAccessServiceThread().addTask(task);
                                        }
                                        // wait until the files are loaded
                                        mutexFileRegistered.wait();
                                    }

                                } catch (InterruptedException ie) {
                                    // should not happen
                                    ie.printStackTrace();
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

    public Map<String, Object> getDesignParameters() throws IllegalAccessException {
        if (m_finalXICDesignNode == null) {
            throw new IllegalAccessException("Design parameters have not been set.");
        }

        Map<String, Object> experimentalDesignParams = new HashMap<>();

        String errorMsg = null;

        //Number used for Experimental Design data 
        int ratioNumeratorGrp = 1;
        int ratioDenominatorGrp = 1;
        int grpNumber = 1;
        int splNumber = 1;
        int splAnalysisNumber = 1;

        List _biologicalGroupList = new ArrayList();
        HashMap<String, Long> _rsmIdBySampleAnalysis = new HashMap<>();
        HashMap<Long, Long> _runIdByRSMId;
        HashMap<String, ArrayList<String>> _samplesAnalysisBySample = new HashMap<>();
        Map<String, Integer> splNbrByName = new HashMap<>();

        Enumeration xicGrps = m_finalXICDesignNode.children();
        while (xicGrps.hasMoreElements() && errorMsg == null) {
            AbstractNode grpNode = (AbstractNode) xicGrps.nextElement();
            String grpName = grpNode.getData().getName();

            //Iterate over Samples
            Enumeration grpSpls = grpNode.children();
            List<Integer> splNumbers = new ArrayList();

            while (grpSpls.hasMoreElements() && errorMsg == null) {
                AbstractNode splNode = (AbstractNode) grpSpls.nextElement();
                String sampleName = grpName + splNode.getData().getName();
                splNbrByName.put(sampleName, splNumber);
                splNumbers.add(splNumber++);

                //Iterate over SampleAnalysis
                Enumeration identRSMs = splNode.children();
                ArrayList<String> splAnalysisNames = new ArrayList<>();
                while (identRSMs.hasMoreElements()) {
                    //VD TODO TEST child type
                    XICBiologicalSampleAnalysisNode qChannelNode = (XICBiologicalSampleAnalysisNode) identRSMs.nextElement();
                    String quantChName = qChannelNode.getQuantChannelName();
                    if ((quantChName == null) || (quantChName.isEmpty())) {
                        quantChName = qChannelNode.getData().getName();
                    }
                    splAnalysisNames.add(quantChName);
                    if (!DataSetData.class.isInstance(qChannelNode.getData())) {
                        errorMsg = "Invalide Sample Analysis specified ";
                        break;
                    }
                    _rsmIdBySampleAnalysis.put(quantChName, ((DataSetData) qChannelNode.getData()).getDataset().getResultSummaryId());
                }
                _samplesAnalysisBySample.put(sampleName, splAnalysisNames);
            } //End go through group's sample

            Map<String, Object> biologicalGroupParams = new HashMap<>();
            biologicalGroupParams.put("number", grpNumber++);
            biologicalGroupParams.put("name", grpName);
            biologicalGroupParams.put("sample_numbers", splNumbers);
            _biologicalGroupList.add(biologicalGroupParams);

        }// End go through groups

        //Read Run IDs
        _runIdByRSMId = getRunIdForRSMs(_rsmIdBySampleAnalysis.values());

        if (errorMsg != null) {
            throw new IllegalAccessException(errorMsg);
        }

        if (grpNumber > 2) {
            ratioDenominatorGrp = 2; //VD TODO :  Comment gerer les ratois ?
        }
        Map<String, Object> ratioParams = new HashMap<>();
        ratioParams.put("number", 1);
        ratioParams.put("numerator_group_number", ratioNumeratorGrp);
        ratioParams.put("denominator_group_number", ratioDenominatorGrp);
        List ratioParamsList = new ArrayList();
        ratioParamsList.add(ratioParams);

        Map<String, Object> groupSetupParams = new HashMap<>();
        groupSetupParams.put("number", 1);
        groupSetupParams.put("name", m_finalXICDesignNode.getData().getName());
        groupSetupParams.put("biological_groups", _biologicalGroupList);
        groupSetupParams.put("ratio_definitions", ratioParamsList);

        ArrayList groupSetupParamsList = new ArrayList();
        groupSetupParamsList.add(groupSetupParams);
        experimentalDesignParams.put("group_setups", groupSetupParamsList);

        List biologicalSampleList = new ArrayList();
        List quantChanneList = new ArrayList();

        Iterator<String> samplesIt = splNbrByName.keySet().iterator();
        while (samplesIt.hasNext()) {
            String nextSpl = samplesIt.next();
            Integer splNbr = splNbrByName.get(nextSpl);

            Map<String, Object> biologicalSampleParams = new HashMap<>();
            biologicalSampleParams.put("number", splNbr);
            biologicalSampleParams.put("name", nextSpl);

            biologicalSampleList.add(biologicalSampleParams);

            List<String> splAnalysis = _samplesAnalysisBySample.get(nextSpl);
            for (String nextSplAnalysis : splAnalysis) {
                Map<String, Object> quantChannelParams = new HashMap<>();
                quantChannelParams.put("number", splAnalysisNumber++);
                quantChannelParams.put("sample_number", splNbr);
                quantChannelParams.put("name", nextSplAnalysis);
                quantChannelParams.put("ident_result_summary_id", _rsmIdBySampleAnalysis.get(nextSplAnalysis));
                quantChannelParams.put("run_id", _runIdByRSMId.get(_rsmIdBySampleAnalysis.get(nextSplAnalysis)));
                quantChanneList.add(quantChannelParams);
            }

        } // End go through samples
        experimentalDesignParams.put("biological_samples", biologicalSampleList);

        List masterQuantChannelsList = new ArrayList();
        Map<String, Object> masterQuantChannelParams = new HashMap<>();
        masterQuantChannelParams.put("number", 1);
        masterQuantChannelParams.put("name", m_finalXICDesignNode.getData().getName());
        masterQuantChannelParams.put("quant_channels", quantChanneList);
        if (m_refDataset != null) {
            masterQuantChannelParams.put("ident_dataset_id", m_refDataset.getId());
            masterQuantChannelParams.put("ident_result_summary_id", m_refDataset.getResultSummaryId());
        }
        masterQuantChannelsList.add(masterQuantChannelParams);

        experimentalDesignParams.put("master_quant_channels", masterQuantChannelsList);

        return experimentalDesignParams;
    }

    /*"quantitation_config": {
     "extraction_params": {
     "moz_tol": "5",
     "moz_tol_unit": "PPM"
     },
     "clustering_params": {
     "moz_tol": "5",
     "moz_tol_unit": "PPM",
     "time_tol": "15",
     "time_computation": "MOST_INTENSE",
     "intensity_computation": "MOST_INTENSE"
     },
     "aln_method_name": "ITERATIVE",
     "aln_params": {
     "mass_interval": "20000",
     "max_iterations": "3",
     "smoothing_method_name": "TIME_WINDOW",
     "smoothing_params": {
     "window_size": "200",
     "window_overlap": "20",
     "min_window_landmarks": "50"
     },
     "ft_mapping_params": {
     "moz_tol": "5",
     "moz_tol_unit": "PPM",
     "time_tol": "600"
     }
     },
     "ft_filter": {
     "name": "INTENSITY",
     "operator": "GT",
     "value": "0"
     },
     "ft_mapping_params": {
     "moz_tol": "10",
     "moz_tol_unit": "PPM",
     "time_tol": "120"
     },
     "normalization_method": "MEDIAN_RATIO"
     }*/
    public Map<String, Object> getQuantiParameters() {
        
        return DefineQuantParamsPanel.getDefineQuantPanel().getParamsPanel().getQuantParams();

    }

    @Override
    protected boolean okCalled() {

        if (m_step == STEP_PANEL_CREATE_XIC_DESIGN) {

            if (m_duplicates == null) {
                m_duplicates = new HashMap<>();
            } else {
                m_duplicates.clear();
            }

            //VDS: Can't checkDesignStructure and checkBiologicalGroupName be merged !! 
            if ((!checkDesignStructure(m_finalXICDesignNode)) || (!checkBiologicalGroupName(m_finalXICDesignNode))) {
                return false;
            }

            //Will run displayDefineRawFiles if Spectrum are OK ! 
            this.checkSpectrum();

            return false;
        } else if (m_step == STEP_PANEL_DEFINE_RAW_FILES) {

            if (!checkRawFiles()) {
                return false;
            }

            displayDefineXICParams();

            return false;
        } else { //STEP_PANEL_DEFINE_XIC_PARAMS

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

                    DefineQuantParamsPanel.getDefineQuantPanel().getParamsPanel().loadParameters(filePreferences);

                } catch (Exception e) {
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
            displayDesignTree(m_finalXICDesignNode);

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
                                    ArrayList<String> failedNodes = new ArrayList<String>();

                                    for (int i = 0; i < failedRSIds.size(); i++) {
                                        failedNodes.add(spectraNodesPerRsId.get(failedRSIds.get(i)).toString());
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

    private boolean checkDesignStructure(AbstractNode parentNode) {
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
                    if (m_duplicates.containsKey(parentNode.toString())) {
                        showErrorOnNode(parentNode, "Biological Sample Analysis is a duplicate. Please rename using right click!");
                        return false;
                    } else {
                        m_duplicates.put(parentNode.toString(), (XICBiologicalSampleAnalysisNode) parentNode);
                        return true;
                    }
                }
                break;
        }

        Enumeration children = parentNode.children();
        //Iterate over Groups
        while (children.hasMoreElements()) {
            AbstractNode rsmNode = (AbstractNode) children.nextElement();
            if (!checkDesignStructure(rsmNode)) {
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

    public void setDefaultDesignTree(DDataset dataset) {
        m_designTree.setExpDesign(dataset, (AbstractNode) m_designTree.getModel().getRoot(), m_designTree, false, false);
        m_designTree.renameXicTitle(dataset.getName() + "-Copy");

        try {
            DefineQuantParamsPanel.getDefineQuantPanel().getParamsPanel().setQuantParams(dataset.getQuantProcessingConfigAsMap());
        } catch (Exception ex) {
            LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error("Error while setting Quant Param ", ex);
        }
    }

}
