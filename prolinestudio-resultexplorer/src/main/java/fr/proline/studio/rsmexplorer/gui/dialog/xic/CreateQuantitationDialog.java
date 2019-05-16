package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.core.orm.msi.PtmSpecificity;
import fr.proline.studio.rsmexplorer.tree.xic.QuantExperimentalDesignTree;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.QuantitationLabel;
import fr.proline.core.orm.uds.QuantitationMethod;
import fr.proline.core.orm.uds.RawFile;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.util.DStoreCustomPoolConnectorFactory;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.data.RunInfoData;
import fr.proline.studio.dam.data.RunInfoData.Status;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabasePTMsTask;
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
import fr.proline.studio.rsmexplorer.tree.xic.DatasetReferenceNode;
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
 * Dialog to build a Quantitation Design
 *
 * @author JM235353
 */
public class CreateQuantitationDialog extends DefaultDialog {

    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");

    private static final int STEP_PANEL_CREATE_EXPERIMENTAL_DESIGN = 0;
    private static final int STEP_PANEL_QUANT_METHOD_PARAMS = 1;
    private static final int STEP_PANEL_LINK_RAW_FILES = 2;
    private static final int STEP_PANEL_LABEL_FREE_PARAMS = 3;
    private int m_step = STEP_PANEL_CREATE_EXPERIMENTAL_DESIGN;

    private static final String SETTINGS_KEY = "XIC";
    private static CreateQuantitationDialog m_singletonDialog = null;

    private IdentificationTree m_selectionTree = null;
    private AbstractNode m_experimentalDesignNode = null;
    private QuantitationMethod.Type m_quantitationType;
    private DDataset m_refIdentDataset = null;
    private Long m_refResultSummaryId = null;
    private SelectRawFilesPanel m_selectRawFilePanel = null;
    private AbstractParamsPanel m_quantMethodParamsPanel;
    private DatabaseVerifySpectrumFromResultSets m_spectrumTask;
    private QuantExperimentalDesignPanel m_experimentalDesignPanel;
    private List<PtmSpecificity> m_identifiedPtms = null;

    public static CreateQuantitationDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new CreateQuantitationDialog(parent);
        }

        return m_singletonDialog;
    }

    private CreateQuantitationDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("Quantitation Wizard");
        setDocumentationSuffix("id.2dlolyb");
        setSize(1000, 768);
        setResizable(true);

    }

    /**
     * for step 1 Initialize this dialog with the specified parameters.
     * existingQuantDataset can be null when this dialog is created to build a
     * new quantification.
     *
     * @param existingQuantDataset : the quantitation dataset to clone (can be
     * null when creating a new quantitation)
     * @param refIdentDataset : the reference identification dataset
     * @param selectionTree : the tree from which identification datasets can be
     * drag'n'dropped to build the experimental design
     * @param m_quantitationType : the quantification type
     */
    public void initializeExperimentalDesignTree(DDataset existingQuantDataset, DDataset refIdentDataset, IdentificationTree selectionTree, QuantitationMethod.Type quantitationType) {
        m_selectionTree = selectionTree;
        m_refIdentDataset = refIdentDataset;
        m_identifiedPtms = null;
        m_quantitationType = quantitationType;
        DataSetNode rootNode = new DataSetNode(DataSetData.createTemporaryQuantitation("Quant"));
        m_experimentalDesignPanel = new QuantExperimentalDesignPanel(rootNode, m_selectionTree, quantitationType);
        QuantExperimentalDesignTree experimentalDesignTree = m_experimentalDesignPanel.getExperimentalDesignTree();
        if (existingQuantDataset != null) {
            QuantExperimentalDesignTree.displayExperimentalDesign(existingQuantDataset, rootNode, experimentalDesignTree, false, false);
            experimentalDesignTree.renameXicTitle(existingQuantDataset.getName() + "-Copy");
            if (rootNode.getChildCount() > 0) {
                AbstractNode firstChildNode = (AbstractNode) rootNode.getChildAt(0);
                if (DatasetReferenceNode.class.isInstance(firstChildNode)) {
                    if (((DatasetReferenceNode) firstChildNode).isInvalidReference()) {
                        //Save previous RSM associated to reference dataset to use the same one !
                        m_refResultSummaryId = existingQuantDataset.getMasterQuantitationChannels().get(0).getIdentResultSummaryId();
                        //QuantExperimentalDesignPanel.getPanel().updatePanel();
                    }
                }
            }

            try {
                Map<String, Object> quantConfig = existingQuantDataset.getQuantProcessingConfigAsMap();
                if (quantConfig.containsKey("config_version") && quantConfig.get("config_version").equals("2.0")) {
                    LabelFreeMSParamsPanel.getLabelFreeMSQuantParamsPanel().setIsSimplifiedPanel(false); //to view all config 
                    LabelFreeMSParamsPanel.getLabelFreeMSQuantParamsPanel().getParamsPanel().setQuantParams(quantConfig);
                } else {
                    String msg = "Can't clone old quantitation parameters to new one. Default parameters will be used !!";
                    JOptionPane.showMessageDialog(this, msg, "Clone Error", JOptionPane.ERROR_MESSAGE);
                    ParameterList parameterList = LabelFreeMSParamsPanel.getLabelFreeMSQuantParamsPanel().getParamsPanel().getParameterList();
                    parameterList.initDefaults();
                    LabelFreeMSParamsPanel.getLabelFreeMSQuantParamsPanel().setIsSimplifiedPanel(true);
                }
            } catch (Exception ex) {
                LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error("Error while setting Quant Param ", ex);
                StudioExceptions.notify("An error occured while cloning XIC parameters", ex);
            }
        } else {
            // set refDataset node
            if (m_refIdentDataset != null) {
                DatasetReferenceNode refDatasetNode = new DatasetReferenceNode(DataSetData.createTemporaryAggregate(m_refIdentDataset.getName()));
                rootNode.add(refDatasetNode);
            }
        }

        //reset drop zone
        if (m_selectRawFilePanel != null) {
            m_selectRawFilePanel.resetDropZonePanel();
        }

        displayExperimentalDesignPanel(rootNode);
    }

    //for step 1
    private void displayExperimentalDesignPanel(AbstractNode rootNode) {

        m_experimentalDesignNode = rootNode;
        m_step = STEP_PANEL_CREATE_EXPERIMENTAL_DESIGN;

        setButtonName(DefaultDialog.BUTTON_OK, "Next");
        setButtonIcon(DefaultDialog.BUTTON_OK, IconManager.getIcon(IconManager.IconType.ARROW));
        setButtonVisible(BUTTON_LOAD, false);
        setButtonVisible(BUTTON_SAVE, false);
        setButtonVisible(BUTTON_BACK, false);

        // Update and Replace panel
        setHelp("<html><b>Step 1:</b> Drag and Drop Identification Summaries to build the Experimental Design.</html>", null);
        replaceInternalComponent(m_experimentalDesignPanel);
        revalidate();
        repaint();
    }

    //for step 2
    protected void displayLinkRawFilesPanel() {

        m_step = STEP_PANEL_LINK_RAW_FILES;

        setButtonName(DefaultDialog.BUTTON_OK, "Next");
        setButtonIcon(DefaultDialog.BUTTON_OK, IconManager.getIcon(IconManager.IconType.ARROW));
        setButtonVisible(BUTTON_LOAD, false);
        setButtonVisible(BUTTON_SAVE, false);
        setButtonVisible(BUTTON_BACK, true);

        // Update and Replace panel
        m_selectRawFilePanel = SelectRawFilesPanel.getPanel(m_experimentalDesignNode);
        setHelp("<html><b>Step 2:</b> Drag and Drop File Path.</html>", null);
        replaceInternalComponent(m_selectRawFilePanel);
        revalidate();
        repaint();

    }

    protected void displayQuantMethodParamsPanel() {

        m_step = STEP_PANEL_QUANT_METHOD_PARAMS;

        setButtonName(DefaultDialog.BUTTON_OK, "Next");
        setButtonIcon(DefaultDialog.BUTTON_OK, IconManager.getIcon(IconManager.IconType.ARROW));
        setButtonVisible(BUTTON_LOAD, false);
        setButtonVisible(BUTTON_SAVE, false);
        setButtonVisible(BUTTON_BACK, true);

        // Update and Replace panel
        if (m_quantitationType == QuantitationMethod.Type.RESIDUE_LABELING) {
            if (m_identifiedPtms == null) {
                final ArrayList<PtmSpecificity> ptms = new ArrayList<>();
                AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
                    @Override
                    public boolean mustBeCalledInAWT() {
                        return true;
                    }

                    @Override
                    public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                        if (success) {
                            m_identifiedPtms = ptms;
                            m_quantMethodParamsPanel = new ResidueMethodParamsPanel(m_experimentalDesignPanel.getQuantitationMethod(), m_identifiedPtms);
                            setHelp("<html><b>Step 1.5:</b> Specify residue labeling method parameters.</html>", null);
                            replaceInternalComponent(m_quantMethodParamsPanel);
                            revalidate();
                            repaint();
                        }
                    }
                };

                DatabasePTMsTask task = new DatabasePTMsTask(callback);
                if (m_refIdentDataset == null) {
                    //get information from leaf rsm
                    Long projectId = ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject().getId();
                    List<Long> rsmIds = m_experimentalDesignPanel.getQuantifiedRsmIds();
                    task.initLoadUsedPTMs(projectId, rsmIds, ptms);
                } else {
                    task.initLoadUsedPTMs(m_refIdentDataset.getProject().getId(), m_refIdentDataset.getResultSummaryId(), ptms);
                }
                AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
            } else {
                m_quantMethodParamsPanel = new ResidueMethodParamsPanel(m_experimentalDesignPanel.getQuantitationMethod(), m_identifiedPtms);
                setHelp("<html><b>Step 1.5:</b> Specify residue labeling method parameters.</html>", null);
                replaceInternalComponent(m_quantMethodParamsPanel);
                revalidate();
                repaint();
            }
        } else if (m_quantitationType == QuantitationMethod.Type.ISOBARIC_TAGGING) {
            m_quantMethodParamsPanel = new IsobaricMethodParamsPanel(m_experimentalDesignPanel.getQuantitationMethod());
            setHelp("<html><b>Step 1.5:</b> Specify isobaric quantitation method parameters.</html>", null);
            replaceInternalComponent(m_quantMethodParamsPanel);
            revalidate();
            repaint();
        }

    }

    //for step 3
    protected void displayLabelFreeParamsPanel() {
        m_step = STEP_PANEL_LABEL_FREE_PARAMS;

        setButtonName(DefaultDialog.BUTTON_OK, org.openide.util.NbBundle.getMessage(DefaultDialog.class, "DefaultDialog.okButton.text"));
        setButtonIcon(DefaultDialog.BUTTON_OK, IconManager.getIcon(IconManager.IconType.OK));

        setButtonVisible(BUTTON_BACK, true);
        setButtonVisible(BUTTON_LOAD, true);
        setButtonVisible(BUTTON_SAVE, true);

        // Update and Replace panel
        LabelFreeMSParamsPanel quantPanel = LabelFreeMSParamsPanel.getLabelFreeMSQuantParamsPanel();
        setHelp("<html><b>Step 3:</b> Specify quantitation parameters.</html>", null);
        quantPanel.getParamsPanel().resetScrollbar();
        replaceInternalComponent(quantPanel);
        revalidate();
        repaint();
    }

    @Override
    public void pack() {
        // forbid pack by overloading the method
    }

    public DataSetData getQuantitationDataset() {
        return (m_experimentalDesignNode == null) ? null : (DataSetData) m_experimentalDesignNode.getData();
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

            Enumeration xicGroups = m_experimentalDesignNode.children();

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
        if (m_experimentalDesignNode == null) {
            throw new IllegalAccessException("Design parameters have not been set.");
        }
        Map<String, Object> experimentalDesignParams = QuantExperimentalDesignTree.toExperimentalDesignParameters(m_experimentalDesignNode, m_refIdentDataset, m_refResultSummaryId);

        // Hack : modify experimentalDesignParams for SILAC / TMT methods
        if (m_quantitationType != QuantitationMethod.Type.LABEL_FREE) {
            QuantitationMethod method = m_experimentalDesignPanel.getQuantitationMethod();
            ArrayList<Object> qcs = (ArrayList) ((Map) ((List) experimentalDesignParams.get("master_quant_channels")).get(0)).get("quant_channels");
            int nbQcs = qcs.size();
            ArrayList<Object> newQcs = new ArrayList<>(nbQcs * method.getLabels().size());
            for (Object o : qcs) {
                Map qc = (Map) o;
                for (QuantitationLabel label : method.getLabels()) {
                    Map newQc = new HashMap(qc);
                    newQc.put("quant_label_id", label.getId());
                    newQc.put("name", qc.get("name") + "." + label.getName());
                    newQc.put("number", newQcs.size() + 1);
                    newQcs.add(newQc);
                }
            }
            qcs.clear();
            qcs.addAll(newQcs);
        }

        return experimentalDesignParams;
    }

    public Long getQuantMethodId() {
        if (m_quantitationType == QuantitationMethod.Type.LABEL_FREE) {
            return 3L;
        }
        return m_experimentalDesignPanel.getQuantitationMethod().getId();
    }

    public Map<String, Object> getQuantiParameters() {
        Map<String, Object> lfParamas = LabelFreeMSParamsPanel.getLabelFreeMSQuantParamsPanel().getParamsPanel().getQuantParams();
        if (m_quantitationType == QuantitationMethod.Type.LABEL_FREE) {
            return lfParamas;
        } else {
            Map<String, Object> quantiParams = m_quantMethodParamsPanel.getQuantParams();
            quantiParams.put("label_free_quant_config", lfParamas);
            return quantiParams;
        }
    }

    private void displayNextPanel() {
        switch (m_step) {
            case STEP_PANEL_CREATE_EXPERIMENTAL_DESIGN:
                if (m_quantitationType == QuantitationMethod.Type.LABEL_FREE) {
                    displayLinkRawFilesPanel();
                } else {
                    displayQuantMethodParamsPanel();
                }
                break;
            case STEP_PANEL_QUANT_METHOD_PARAMS:
                displayLinkRawFilesPanel();
                break;
            case STEP_PANEL_LINK_RAW_FILES:
                displayLabelFreeParamsPanel();
                break;
            default:
                break;
        }
    }

    @Override
    protected boolean okCalled() {

        switch (m_step) {
            case STEP_PANEL_CREATE_EXPERIMENTAL_DESIGN:
                //CreateXICDesignPanel-NEXT button

                //VDS: Can't checkDesignStructure and checkBiologicalGroupName be merged !!
                if ((!checkDesignStructure(m_experimentalDesignNode, new HashSet<>())) || (!checkBiologicalGroupName(m_experimentalDesignNode))) {
                    return false;
                }

                //Will call displayLinkRawFilesPanel if Spectrum are OK !
                checkSpectrum();

                return false;
            case STEP_PANEL_QUANT_METHOD_PARAMS:
                displayNextPanel();

                return false;
            case STEP_PANEL_LINK_RAW_FILES:
                //SelectRawFilesPanel-NEXT button

                if (!checkRawFiles()) {
                    return false;
                }

                displayNextPanel();

                return false;
            default:
                //STEP_PANEL_DEFINE_XIC_PARAMS , LabelFreeMSParamsPanel-OK button

                if (!checkQuantParameters()) {
                    return false;
                }

                // Save Parameters
                ParameterList parameterList = LabelFreeMSParamsPanel.getLabelFreeMSQuantParamsPanel().getParamsPanel().getParameterList();
                Preferences preferences = NbPreferences.root();
                parameterList.saveParameters(preferences);
                preferences.putBoolean(AbstractLabelFreeMSParamsPanel.XIC_SIMPLIFIED_PARAMS, LabelFreeMSParamsPanel.getLabelFreeMSQuantParamsPanel().getParamsPanel().isSimplifiedPanel());

                return true;
        }

    }

    private boolean checkQuantParameters() {

        ParameterList parameterList = LabelFreeMSParamsPanel.getLabelFreeMSQuantParamsPanel().getParamsPanel().getParameterList();

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
            ParameterList parameterList = LabelFreeMSParamsPanel.getLabelFreeMSQuantParamsPanel().getParamsPanel().getParameterList();
            parameterList.saveParameters(filePreferences);
            filePreferences.putBoolean(AbstractLabelFreeMSParamsPanel.XIC_SIMPLIFIED_PARAMS, LabelFreeMSParamsPanel.getLabelFreeMSQuantParamsPanel().getParamsPanel().isSimplifiedPanel());
            filePreferences.put(AbstractLabelFreeMSParamsPanel.XIC_PARAMS_VERSION_KEY, LabelFreeMSParamsPanel.getLabelFreeMSQuantParamsPanel().getParamsVersion());
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
                ParameterList parameterList = LabelFreeMSParamsPanel.getLabelFreeMSQuantParamsPanel().getParamsPanel().getParameterList();
                parameterList.initDefaults();
            } else {
                try {
                    File settingsFile = settingsDialog.getSelectedFile();
                    FilePreferences filePreferences = new FilePreferences(settingsFile, null, "");
                    if (Arrays.asList(filePreferences.keys()).contains(AbstractLabelFreeMSParamsPanel.XIC_SIMPLIFIED_PARAMS)) {
                        boolean isSimplified = filePreferences.getBoolean(AbstractLabelFreeMSParamsPanel.XIC_SIMPLIFIED_PARAMS, true);
                        LabelFreeMSParamsPanel.getLabelFreeMSQuantParamsPanel().setIsSimplifiedPanel(isSimplified);
                    }
                    String version = filePreferences.get(AbstractLabelFreeMSParamsPanel.XIC_PARAMS_VERSION_KEY, "1.0");
                    String panelVersion = LabelFreeMSParamsPanel.getLabelFreeMSQuantParamsPanel().getParamsVersion();
                    if (!version.equals(panelVersion)) {
                        String msg = "Can't load " + version + " quantitation parameters to " + panelVersion + " one. All parameters may not have been taken into account !";
                        JOptionPane.showMessageDialog(this, msg, "Load XIC parameters error", JOptionPane.ERROR_MESSAGE);
                    }
                    LabelFreeMSParamsPanel.getLabelFreeMSQuantParamsPanel().getParamsPanel().loadParameters(filePreferences);

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
        switch (m_step) {
            case STEP_PANEL_QUANT_METHOD_PARAMS:
                displayExperimentalDesignPanel(m_experimentalDesignNode);

                return false;
            case STEP_PANEL_LINK_RAW_FILES:
                m_selectRawFilePanel.pruneDesignTree();
                if (m_quantitationType == QuantitationMethod.Type.LABEL_FREE) {
                    displayExperimentalDesignPanel(m_experimentalDesignNode);
                } else {
                    displayQuantMethodParamsPanel();
                }

                return false;
            case STEP_PANEL_LABEL_FREE_PARAMS:
                displayLinkRawFilesPanel();

                return false;
            default:
                break;
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

        Map<Long, DataSetNode> spectraNodesPerRsId = getSampleAnalysisNodesPerRsId(m_experimentalDesignNode);

        if (spectraNodesPerRsId.size() > 0) {

            Long projectId = spectraNodesPerRsId.values().iterator().next().getDataset().getProject().getId();
            List<Long> resultSetIds = new ArrayList<>(spectraNodesPerRsId.keySet());
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

                                    displayNextPanel();

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
            loadWaitingDialog.setLocation(p.x, p.y);
            loadWaitingDialog.setVisible(true);
        } else {
            displayNextPanel();
        }
    }

    private boolean checkDesignStructure(AbstractNode parentNode, Set<String> duplicates) {
        AbstractNode.NodeTypes type = parentNode.getType();
        switch (type) {
            case DATA_SET:
                if (parentNode.isLeaf()) {
                    showErrorOnNode(parentNode, "Your Experimental Design is empty.");
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
        QuantExperimentalDesignTree experimentalDesignTree = m_experimentalDesignPanel.getExperimentalDesignTree();
        // expand parentnode if needed
        AbstractNode parentNode = (AbstractNode) node.getParent();
        if (parentNode != null) {
            TreePath pathToExpand = new TreePath(parentNode.getPath());
            if (!experimentalDesignTree.isExpanded(pathToExpand)) {
                experimentalDesignTree.expandPath(pathToExpand);
            }
        }
        // scroll to node if needed
        TreePath path = new TreePath(node.getPath());
        experimentalDesignTree.scrollPathToVisible(path);

        // display error
        setStatus(true, error);
        highlight(experimentalDesignTree, experimentalDesignTree.getPathBounds(path));
    }

    @Override
    protected boolean cancelCalled() {
        return true;
    }

}
