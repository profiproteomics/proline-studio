package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.core.orm.uds.QuantitationMethod;
import fr.proline.core.orm.uds.dto.DDatasetType;
import fr.proline.studio.NbPreferences;
import fr.proline.studio.WindowManager;
import fr.proline.studio.corewrapper.data.QuantPostProcessingParams;
import fr.proline.studio.gui.CheckBoxTitledBorder;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.parameter.*;
import fr.proline.studio.rsmexplorer.gui.model.PurityCorrectionMatrixTableModel;
import fr.proline.studio.table.DecoratedTable;
import fr.proline.studio.table.TablePopupMenu;
import fr.proline.studio.table.renderer.DefaultColoredCellRenderer;
import fr.proline.studio.utils.IconManager;
import fr.proline.studio.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

/**
 * panel with the different parameters for computing post processing on quantitation.
 */

public class QuantSimplifiedPostProcessingPanel extends JPanel {

    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer.QuantSimplifiedPostProcessingPanel");

    private ParameterList m_parameterList;

    private File m_purityMatrixFile;
    private String m_purityMatrixValues;
    JPanel m_psmMatrixPanel;

    PurityCorrectionMatrixTableModel m_readOnlyPurityTableModel;
    JButton m_savePurityMatrix;
    JLabel m_psmFileMatrixStatus;
    JLabel m_psmTableMatrixStatus;
    private CheckBoxTitledBorder m_usePurityCorrectionMatrixCBoxTitle;
    private JComboBox<String> m_peptidesSelectionMethodCB;
    private JCheckBox m_discardMissCleavedPeptidesChB;
    private JCheckBox m_discardModifiedPeptidesChB;
    private CheckBoxTitledBorder m_usePIFCBoxTitle;// private JCheckBox m_discardPSMUsingPIFChB;
    private JTextField m_discardPSMPIFValueTF;
    private JLabel m_discardPSMPIFValueLabel;
    private JCheckBox m_applyPepNormalizationChB;
    private JCheckBox m_discardPeptideIonsSharingPeakelsChB;
    private List<JCheckBox> m_peptideModificationListChB;
    private JComboBox<String> m_peptideAbundanceSummarizingMethodCB;
    private JLabel m_modifiedPeptidesFilteringMethodLabel;
    private JComboBox<String> m_modifiedPeptidesFilteringMethodCB;
    private JComboBox<String> m_peptideIonAbundanceSummarizingMethodCB;
    private JComboBox<String> m_psmAbundanceSummarizingMethodCB;

    private JCheckBox m_applyProtNormalizationChB;

    private BooleanParameter m_discardPSMUsingPIFParameter;
    private FloatParameter m_discardPSMPIFValueParameter;
    private StringParameter m_purityCorrectionMatrixParameter;
    private BooleanParameter m_usePurityCorrectionMatrixParameter;
    private BooleanParameter m_discardMissCleavedPeptidesParameter;
    private BooleanParameter m_discardModifiedPeptidesParameter;
    private BooleanParameter m_discardPeptideIonsSharingPeakelsParameter;
    private BooleanParameter m_applyPepNormalizationParameter;
    private BooleanParameter m_applyProtNormalizationParameter;
    private List<BooleanParameter> m_peptidesModificationListParameter;
    private ObjectParameter<String> m_peptidesSelectionMethodParameter;
    private ObjectParameter<String> m_peptideAbundanceSummarizingMethodParameter;
    private ObjectParameter<String> m_modifiedPeptidesFilteringMethodParameter;
    private ObjectParameter<String> m_pepIonsAbundanceSummarizingMethodParameter;
    private ObjectParameter<String> m_psmAbundanceSummarizingMethodParameter;

    private final boolean m_readOnly;
    private final DDatasetType.QuantitationMethodInfo m_quantitationMethodInfo;

    private final QuantitationMethod m_quantitationMethod;
    Map<Long, String> m_ptmSpecificityNameById;
    boolean m_isValidLabeledQMethod;


    public QuantSimplifiedPostProcessingPanel(boolean readOnly, QuantitationMethod quantitationMethod, DDatasetType.QuantitationMethodInfo quantitationMethodInfo, Map<Long, String> ptmSpecificityNameById, boolean isValidLabeledQMethod) {
        super();
        m_readOnly = readOnly;
        m_ptmSpecificityNameById = ptmSpecificityNameById;
        m_quantitationMethodInfo = quantitationMethodInfo;
        m_quantitationMethod = quantitationMethod;
        m_isValidLabeledQMethod = isValidLabeledQMethod;
        init();
    }

    private void init() {
        // completeMode can be changed in the preferences file with the Profilizer key
        m_parameterList = new ParameterList(QuantPostProcessingParams.SETTINGS_KEY);
        m_parameterList.addBackwardCompatiblePrefix(QuantPostProcessingParams.PREVIOUS_SETTINGS_KEY);
        m_peptideModificationListChB = new ArrayList<>();
        m_peptidesModificationListParameter = new ArrayList<>();
        createParameters();
        initPanel();
        m_parameterList.updateValues(NbPreferences.root());
        for (AbstractParameter param : m_parameterList) {
            param.setUsed(true);
        }
        updateDiscardPTMs();
        updateDiscardPIF();
    }

    private void createParameters() {

        String paramKey = QuantPostProcessingParams.getSettingKey(QuantPostProcessingParams.DISCARD_MISS_CLEAVED_PEPTIDES);
        m_discardMissCleavedPeptidesChB = new JCheckBox(StringUtils.getLabelFromCamelCase(paramKey));
        m_discardMissCleavedPeptidesChB.setEnabled(!m_readOnly);
        m_discardMissCleavedPeptidesParameter = new BooleanParameter(paramKey, StringUtils.getLabelFromCamelCase(paramKey), m_discardMissCleavedPeptidesChB, false);
        m_discardMissCleavedPeptidesParameter.addBackwardCompatibleKey(QuantPostProcessingParams.getSettingKey(QuantPostProcessingParams.DISCARD_MISS_CLEAVED_PEPTIDES_PREV));
        m_parameterList.add(m_discardMissCleavedPeptidesParameter);

        paramKey = QuantPostProcessingParams.getSettingKey(QuantPostProcessingParams.DISCARD_PIF_PEPTIDE_MATCHES_VALUE);
        m_discardPSMPIFValueTF = new JTextField();
        m_discardPSMPIFValueLabel =new JLabel("Discard PSM with PIF <= ");
        m_discardPSMPIFValueTF.setEnabled(!m_readOnly);
        m_discardPSMPIFValueLabel.setEnabled(!m_readOnly);
        m_discardPSMPIFValueParameter = new FloatParameter(paramKey, StringUtils.getLabelFromCamelCase(paramKey), m_discardPSMPIFValueTF, QuantPostProcessingParams.DEFAULT_PIF_FILTER_VALUE, 0.0f, 1.0f);
        m_parameterList.add(m_discardPSMPIFValueParameter);

        paramKey = QuantPostProcessingParams.getSettingKey(QuantPostProcessingParams.DISCARD_PIF_PEPTIDE_MATCHES);
        m_usePIFCBoxTitle = new CheckBoxTitledBorder("Use Precursor Ion Fraction filter", false);
//        String usePifLabel= "Use Precursor Ion Fraction filter";
//        m_discardPSMUsingPIFChB = new JCheckBox(usePifLabel);
        m_usePIFCBoxTitle.setEnabled(!m_readOnly);
        m_usePIFCBoxTitle.addChangeListener(e -> updateDiscardPIF());
        m_discardPSMUsingPIFParameter = new BooleanParameter(paramKey, "Use Precursor Ion Fraction filter", m_usePIFCBoxTitle.getInternalCheckBox(), false);
        m_parameterList.add(m_discardPSMUsingPIFParameter);


        paramKey = QuantPostProcessingParams.getSettingKey(QuantPostProcessingParams.DISCARD_MODIFIED_PEPTIDES);
        m_discardModifiedPeptidesChB = new JCheckBox(StringUtils.getLabelFromCamelCase(paramKey));
        m_discardModifiedPeptidesChB.addActionListener(e -> updateDiscardPTMs());
        m_discardModifiedPeptidesChB.setEnabled(!m_readOnly);
        m_discardModifiedPeptidesParameter = new BooleanParameter(paramKey, StringUtils.getLabelFromCamelCase(paramKey), m_discardModifiedPeptidesChB, false);
        m_discardModifiedPeptidesParameter.addBackwardCompatibleKey(QuantPostProcessingParams.getSettingKey(QuantPostProcessingParams.DISCARD_OXIDIZED_PEPTIDES));
        m_parameterList.add(m_discardModifiedPeptidesParameter);

        m_ptmSpecificityNameById.forEach((id, name) -> {
            JCheckBox discardPeptidesWithModifChB = new JCheckBox(name);
            discardPeptidesWithModifChB.setEnabled(!m_readOnly);
            BooleanParameter ptmToDiscardParameter = new BooleanParameter(QuantPostProcessingParams.PREFIX_PTM_IDS_TO_DISCARD + id, name, discardPeptidesWithModifChB, false);
            ptmToDiscardParameter.setAssociatedData(id);//Associate PtmDef Id
            m_peptideModificationListChB.add(discardPeptidesWithModifChB);
            m_peptidesModificationListParameter.add(ptmToDiscardParameter);
            m_parameterList.add(ptmToDiscardParameter);
        });

        paramKey = QuantPostProcessingParams.getSettingKey(QuantPostProcessingParams.DISCARD_PEP_IONS_SHARING_PEAKELS);
        m_discardPeptideIonsSharingPeakelsChB = new JCheckBox(StringUtils.getLabelFromCamelCase(paramKey));
        m_discardPeptideIonsSharingPeakelsChB.setEnabled(!m_readOnly);
        m_discardPeptideIonsSharingPeakelsParameter = new BooleanParameter(paramKey, StringUtils.getLabelFromCamelCase(paramKey), m_discardPeptideIonsSharingPeakelsChB, false);
        m_discardPeptideIonsSharingPeakelsParameter.addBackwardCompatibleKey(QuantPostProcessingParams.getSettingKey(QuantPostProcessingParams.DISCARD_PEPTIDES_SHARING_PEAKELS));
        m_parameterList.add(m_discardPeptideIonsSharingPeakelsParameter);

        paramKey = QuantPostProcessingParams.getSettingKey(QuantPostProcessingParams.USE_PURITY_CORRECTION_MATRIX);
        m_usePurityCorrectionMatrixCBoxTitle = new CheckBoxTitledBorder(StringUtils.getLabelFromCamelCase(paramKey), true);
        m_usePurityCorrectionMatrixCBoxTitle.setEnabled(!m_readOnly);
        m_usePurityCorrectionMatrixCBoxTitle.addChangeListener(e -> setEnabled(m_psmMatrixPanel, !m_readOnly && m_usePurityCorrectionMatrixCBoxTitle.isSelected()));
        m_usePurityCorrectionMatrixParameter = new BooleanParameter(paramKey, StringUtils.getLabelFromCamelCase(paramKey), m_usePurityCorrectionMatrixCBoxTitle.getInternalCheckBox(), false);
        m_parameterList.add(m_usePurityCorrectionMatrixParameter);

        paramKey = QuantPostProcessingParams.getSettingKey(QuantPostProcessingParams.PURITY_CORRECTION_MATRIX);
        m_purityCorrectionMatrixParameter = new StringParameter(paramKey, StringUtils.getLabelFromCamelCase(paramKey), JTextField.class, "", 0, Integer.MAX_VALUE);
        m_parameterList.add(m_purityCorrectionMatrixParameter);

        m_applyPepNormalizationChB = new JCheckBox("Apply Normalization");
        m_applyPepNormalizationChB.setEnabled(!m_readOnly);
        m_applyPepNormalizationParameter = new BooleanParameter("applyPepNormalization", "Apply Normalization on peptides", m_applyPepNormalizationChB, false);
        m_parameterList.add(m_applyPepNormalizationParameter);

        m_applyProtNormalizationChB = new JCheckBox("Apply Normalization");
        m_applyProtNormalizationChB.setEnabled(!m_readOnly);
        m_applyProtNormalizationParameter = new BooleanParameter("applyProtNormalization", "Apply Normalization on proteins", m_applyProtNormalizationChB, false);
        m_parameterList.add(m_applyProtNormalizationParameter);


        paramKey = QuantPostProcessingParams.getSettingKey(QuantPostProcessingParams.PEPTIDE_SELECTION_METHOD);
        m_peptidesSelectionMethodCB = new JComboBox<>(QuantPostProcessingParams.getPeptidesSelectionMethodValues());
        m_peptidesSelectionMethodCB.setEnabled(!m_readOnly);
        m_peptidesSelectionMethodParameter = new ObjectParameter<>(paramKey, StringUtils.getLabelFromCamelCase(paramKey), m_peptidesSelectionMethodCB, QuantPostProcessingParams.getPeptidesSelectionMethodValues(), QuantPostProcessingParams.getPeptidesSelectionMethodKeys(),0,null);
        m_parameterList.add(m_peptidesSelectionMethodParameter);

        paramKey = QuantPostProcessingParams.getSettingKey(QuantPostProcessingParams.PEPTIDE_ABUNDANCE_SUMMARIZING_METHOD);
        m_peptideAbundanceSummarizingMethodCB = new JComboBox<>(QuantPostProcessingParams.getPeptideAbundanceSummarizingMethodValues());
        m_peptideAbundanceSummarizingMethodCB.setEnabled(!m_readOnly);
        m_peptideAbundanceSummarizingMethodParameter = new ObjectParameter<>(paramKey, StringUtils.getLabelFromCamelCase(paramKey), m_peptideAbundanceSummarizingMethodCB, QuantPostProcessingParams.getPeptideAbundanceSummarizingMethodValues(), QuantPostProcessingParams.getPeptideAbundanceSummarizingMethodKeys(), 5, null);
        m_peptideAbundanceSummarizingMethodParameter.addBackwardCompatibleKey(QuantPostProcessingParams.getSettingKey(QuantPostProcessingParams.ABUNDANCE_SUMMARIZING_METHOD));
        m_peptideAbundanceSummarizingMethodParameter.addBackwardCompatibleKey(QuantPostProcessingParams.getSettingKey(QuantPostProcessingParams.ABUNDANCE_SUMMARIZER_METHOD));
        m_parameterList.add(m_peptideAbundanceSummarizingMethodParameter);

        paramKey = QuantPostProcessingParams.getSettingKey(QuantPostProcessingParams.PEP_ION_ABUNDANCE_SUMMARIZING_METHOD);
        m_peptideIonAbundanceSummarizingMethodCB = new JComboBox<>(QuantPostProcessingParams.getPepIonAbundanceSummarizingMethodValues());
        m_peptideIonAbundanceSummarizingMethodCB.setEnabled(!m_readOnly);
        m_pepIonsAbundanceSummarizingMethodParameter = new ObjectParameter<>(paramKey, StringUtils.getLabelFromCamelCase(paramKey), m_peptideIonAbundanceSummarizingMethodCB, QuantPostProcessingParams.getPepIonAbundanceSummarizingMethodValues(), QuantPostProcessingParams.getPepIonAbundanceSummarizingMethodKeys(), 0, null);
        m_pepIonsAbundanceSummarizingMethodParameter.addBackwardCompatibleKey(QuantPostProcessingParams.ION_ABUNDANCE_SUMMARIZER_METHOD_SETTING_V2);
        m_parameterList.add(m_pepIonsAbundanceSummarizingMethodParameter);


        paramKey = QuantPostProcessingParams.getSettingKey(QuantPostProcessingParams.PSM_ABUNDANCE_SUMMARIZING_METHOD_KEY);
        m_psmAbundanceSummarizingMethodCB = new JComboBox<>(QuantPostProcessingParams.getPSMAbundanceSummarizingMethodValues());
        m_psmAbundanceSummarizingMethodCB.setEnabled(!m_readOnly);
        String psmSummarizingLabel = "PSM Abundance Summarizing Method";
        m_psmAbundanceSummarizingMethodParameter = new ObjectParameter<>(paramKey, psmSummarizingLabel, m_psmAbundanceSummarizingMethodCB, QuantPostProcessingParams.getPSMAbundanceSummarizingMethodValues(), QuantPostProcessingParams.getPSMAbundanceSummarizingMethodKeys(), 0, null);
        m_parameterList.add(m_psmAbundanceSummarizingMethodParameter);

        paramKey = QuantPostProcessingParams.getSettingKey(QuantPostProcessingParams.MODIFIED_PEPTIDE_FILTERING_METHOD);
        m_modifiedPeptidesFilteringMethodCB = new JComboBox<>(QuantPostProcessingParams.getModifiedPeptideFilteringMethodValues());
        m_modifiedPeptidesFilteringMethodCB.setEnabled(!m_readOnly);
        m_modifiedPeptidesFilteringMethodParameter = new ObjectParameter<>(paramKey, StringUtils.getLabelFromCamelCase(paramKey), m_modifiedPeptidesFilteringMethodCB, QuantPostProcessingParams.getModifiedPeptideFilteringMethodValues(), QuantPostProcessingParams.getModifiedPeptideFilteringMethodKeys(), 0, null);
        m_modifiedPeptidesFilteringMethodParameter.addBackwardCompatibleKey(QuantPostProcessingParams.MODIFIED_PEPTIDES_FILTERING_METHOD_SETTING_V1_2);
        m_parameterList.add(m_modifiedPeptidesFilteringMethodParameter);
    }

    private void updateDiscardPTMs() {
        if (!m_readOnly) {
            m_peptideModificationListChB.forEach((ptmCBx) -> ptmCBx.setEnabled(m_discardModifiedPeptidesChB.isSelected()));
            m_modifiedPeptidesFilteringMethodLabel.setEnabled(m_discardModifiedPeptidesChB.isSelected());
            m_modifiedPeptidesFilteringMethodCB.setEnabled(m_discardModifiedPeptidesChB.isSelected());
        }
    }
    private void updateDiscardPIF() {
        if (!m_readOnly) {
            m_discardPSMPIFValueTF.setEnabled(m_usePIFCBoxTitle.isSelected());
            m_discardPSMPIFValueLabel.setEnabled(m_usePIFCBoxTitle.isSelected());
        }
    }

    private static void setEnabled(Container container, boolean isEnabled) {
        if(container==null)
            return;

        container.setEnabled(isEnabled);

        for (Component children : container.getComponents()) {
            if (Container.class.isAssignableFrom(children.getClass())) {
                setEnabled((Container) children, isEnabled);
            } else {
                children.setEnabled(isEnabled);
            }
        }
    }

    /**
     * load parameters from registed preference file
     *
     * @param filePreferences
     * @throws BackingStoreException
     */
    public void loadParameters(Preferences filePreferences, String version) throws BackingStoreException {

        boolean loadedPTMsParamsError = false;//Specify if loaded params from file don't contain this Quant PTMs list
        Preferences preferences = NbPreferences.root();
        String[] keys = filePreferences.keys();

        //Convert modification params if needed
        Set<String> thisQuantPtmIds = new HashSet<>();
        Set<String> prefQuantPtmIds = new HashSet<>();
        for (BooleanParameter ptmToDiscardParameter : m_peptidesModificationListParameter) {
            Long ptmId = (Long) ptmToDiscardParameter.getAssociatedData();
            thisQuantPtmIds.add(m_parameterList.getPrefixName()+QuantPostProcessingParams.PREFIX_PTM_IDS_TO_DISCARD+ptmId);
        }
        boolean discardModifiedPeptides = false;
        boolean purityMatrixDefined =false;
        boolean usePurityMatrix = false;
        String filePrefix = version.equals("1.0") ? m_parameterList.getBackwardCompatiblePrefixes().get(0) : m_parameterList.getPrefixName();
        for (String key : keys) {
            if(key.startsWith(filePrefix+QuantPostProcessingParams.PREFIX_PTM_IDS_TO_DISCARD)){
                prefQuantPtmIds.add(key);
            }

            if(key.startsWith(filePrefix+QuantPostProcessingParams.getSettingKey(QuantPostProcessingParams.DISCARD_MODIFIED_PEPTIDES)))
                discardModifiedPeptides = filePreferences.getBoolean(key, false);
            else if(key.startsWith(filePrefix+QuantPostProcessingParams.getSettingKey(QuantPostProcessingParams.DISCARD_OXIDIZED_PEPTIDES))) {
                discardModifiedPeptides =  filePreferences.getBoolean(key, false);
            }

            //redefine
            if(key.startsWith(filePrefix+QuantPostProcessingParams.getSettingKey(QuantPostProcessingParams.USE_PURITY_CORRECTION_MATRIX)) ){
                usePurityMatrix =  filePreferences.getBoolean(key, false) &&  m_quantitationMethodInfo.equals(DDatasetType.QuantitationMethodInfo.ISOBARIC_TAGGING) && m_isValidLabeledQMethod;
                filePreferences.put(filePrefix+QuantPostProcessingParams.getSettingKey(QuantPostProcessingParams.USE_PURITY_CORRECTION_MATRIX), Boolean.toString(usePurityMatrix));
            }

            if(key.startsWith(filePrefix+QuantPostProcessingParams.getSettingKey(QuantPostProcessingParams.PURITY_CORRECTION_MATRIX)) && m_quantitationMethodInfo.equals(DDatasetType.QuantitationMethodInfo.ISOBARIC_TAGGING) && m_isValidLabeledQMethod){
                m_purityMatrixValues = filePreferences.get(key, null);
                if(m_purityMatrixValues != null && !m_purityMatrixValues.isEmpty())
                    purityMatrixDefined = true;
            }

            String value = filePreferences.get(key, null);
            preferences.put(key, value);
        }

        if(prefQuantPtmIds.size() >= thisQuantPtmIds.size()){ //More PTMs in loaded file. Test if at least same as PTMs of this quant
            if(!prefQuantPtmIds.containsAll(thisQuantPtmIds)){
                loadedPTMsParamsError = true;
            }
        } else {
            loadedPTMsParamsError = true;
        }
        if(loadedPTMsParamsError && discardModifiedPeptides){
            JOptionPane.showMessageDialog(this, "Warning: PTMs to discard don't match current PTMs list", "Load Parameter ERROR",JOptionPane.ERROR_MESSAGE);
            String label = m_discardModifiedPeptidesChB.getText()+" (WARNING-PTMs: Read parameters don't match current list)";
            m_discardModifiedPeptidesChB.setText(label);
        }
        if(purityMatrixDefined){
            m_psmFileMatrixStatus.setIcon(IconManager.getIcon(IconManager.IconType.TICK_SMALL));
        }
        getParameterList().loadParameters(filePreferences); //Load params
        updateDiscardPTMs();
    }

    public ParameterList getParameterList() {
        m_parameterList.getParameter(QuantPostProcessingParams.getSettingKey(QuantPostProcessingParams.PURITY_CORRECTION_MATRIX)).setValue(m_purityMatrixValues);
        return m_parameterList;
    }

    private void initPanel() {
        this.setLayout(new BorderLayout());

        JTabbedPane m_tabbedPane = new JTabbedPane();
        m_tabbedPane.addTab(" Abundances computing ", null, getAbundancePanel(), "Parameters used to compute abundances");
        m_tabbedPane.addTab(" Aggregation config. ", null, getAggregationCfgPanel(), "Parameters used to aggregate abundances");

        JScrollPane m_scrollPane = new JScrollPane();
        m_scrollPane.setViewportView(m_tabbedPane);
        m_scrollPane.createVerticalScrollBar();
        add(m_scrollPane, BorderLayout.CENTER);
    }

    private JPanel getAbundancePanel() {
        JPanel abundancePanel = new JPanel(new GridBagLayout());

        GridBagConstraints tabPanelC = new GridBagConstraints();
        tabPanelC.anchor = GridBagConstraints.NORTHWEST;
        tabPanelC.fill = GridBagConstraints.BOTH;
        tabPanelC.insets = new Insets(5, 2, 2, 2);
        tabPanelC.gridwidth=1;
        tabPanelC.gridx=0;
        tabPanelC.gridy=0;
        tabPanelC.weightx=1;

        // PSMs Ab
        if(m_quantitationMethodInfo.equals(DDatasetType.QuantitationMethodInfo.ISOBARIC_TAGGING)) {
            JPanel psmPanel = createPSMAbundancePanel();
            abundancePanel.add(psmPanel, tabPanelC);
            tabPanelC.gridy++;
        }

        // Peptide Ab
        JPanel pepPanel = new JPanel(new BorderLayout() );
        pepPanel.setBorder(BorderFactory.createTitledBorder(" Peptides "));
        pepPanel.add(m_applyPepNormalizationChB, BorderLayout.NORTH);

        tabPanelC.gridx = 0;
        abundancePanel.add(pepPanel, tabPanelC);

        // Protein Ab
        JPanel protPanel = createProteinAbundancePanel();

        tabPanelC.gridx = 0;
        tabPanelC.gridy++;
        abundancePanel.add(protPanel, tabPanelC);

        tabPanelC.gridy++;
        tabPanelC.weighty = 1;
        abundancePanel.add(Box.createVerticalGlue(), tabPanelC);

        return abundancePanel;
    }

    private JPanel createProteinAbundancePanel() {
        JPanel protPanel = new JPanel(new BorderLayout() );
        protPanel.setBorder(BorderFactory.createTitledBorder(" Proteins "));

        // --- pep selection
        JPanel pepSelectionPanel = new JPanel();
        pepSelectionPanel.setBorder(new TitledBorder("Selection"));
        pepSelectionPanel.setLayout(new GridBagLayout());
        GridBagConstraints c1 = new GridBagConstraints();
        c1.anchor = GridBagConstraints.NORTHWEST;
        c1.fill = GridBagConstraints.BOTH;
        c1.insets = new Insets(5, 10, 5, 5);

        // peptide selection method
        c1.gridx = 0;
        c1.gridy = 0;
        c1.weightx = 0;
        JLabel peptideSelectionMethodLabel = new JLabel(m_peptidesSelectionMethodParameter.getName());
//        peptideSelectionMethodLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        pepSelectionPanel.add(peptideSelectionMethodLabel, c1);
        c1.insets = new Insets(5, 5, 5, 5);

        c1.gridx++;
        c1.weightx = 1;
        pepSelectionPanel.add(m_peptidesSelectionMethodCB, c1);

        // discardMissedCleavedPeptides
        c1.gridx = 0;
        c1.gridy++;
        c1.gridwidth = 2;
        c1.weightx = 1;
        pepSelectionPanel.add(m_discardMissCleavedPeptidesChB, c1);

        // discardModifiedPeptides
        c1.gridy++;
        pepSelectionPanel.add(m_discardModifiedPeptidesChB, c1);
        c1.insets = new Insets(5, 20, 5, 5);
        for (JCheckBox modifCB : m_peptideModificationListChB) {
            c1.gridy++;
            c1.weightx = 0.5;
            pepSelectionPanel.add(modifCB, c1);
        }

        c1.gridy++;
        c1.weightx = 0;
        c1.gridwidth = 1;
        c1.insets = new Insets(5, 10, 5, 5);
        m_modifiedPeptidesFilteringMethodLabel = new JLabel(m_modifiedPeptidesFilteringMethodParameter.getName()); //Modified Peptide Filtering Method :");
//        m_modifiedPeptidesFilteringMethodLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        pepSelectionPanel.add(m_modifiedPeptidesFilteringMethodLabel, c1);

        c1.insets = new Insets(5, 5, 5, 5);
        c1.gridx++;
        c1.weightx = 1;
        pepSelectionPanel.add(m_modifiedPeptidesFilteringMethodCB, c1);

        // discardPeptidesSharingPeakels
        c1.gridx = 0;
        c1.gridy++;
        c1.gridwidth = 2;
        c1.weightx = 1;
        pepSelectionPanel.add(m_discardPeptideIonsSharingPeakelsChB, c1);
        protPanel.add(m_applyProtNormalizationChB, BorderLayout.NORTH);
        protPanel.add(pepSelectionPanel, BorderLayout.CENTER);
        return protPanel;
    }


    private JPanel createPSMAbundancePanel() {
        JPanel psmPanel = new JPanel(new BorderLayout());
        psmPanel.setBorder(BorderFactory.createTitledBorder(" PSMs "));

        JPanel pifPanel = new JPanel(new GridBagLayout());
        pifPanel.setBorder(m_usePIFCBoxTitle);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0;
        c.weighty = 0;
        c.insets = new Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 0;
//        m_discardPSMPIFValueLabel =new JLabel("Discard PSM with PIF <= ");
        pifPanel.add(m_discardPSMPIFValueLabel, c);
        c.weightx = 0.5;
        c.gridx++;
        pifPanel.add(m_discardPSMPIFValueTF,c);
        psmPanel.add(pifPanel, BorderLayout.NORTH);

        if(m_isValidLabeledQMethod) {
            JPanel psmMatrixPanel = new JPanel(new BorderLayout());
            psmMatrixPanel.setBorder(m_usePurityCorrectionMatrixCBoxTitle);
            if (!m_readOnly) {
                m_psmMatrixPanel = new JPanel();
                m_psmMatrixPanel.setLayout(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.anchor = GridBagConstraints.WEST;
                gbc.fill = GridBagConstraints.NONE;
                gbc.weightx = 0;
                gbc.weighty = 0;
                gbc.insets = new Insets(5, 5, 5, 5);
                gbc.gridx = 0;
                gbc.gridy = 0;
                JLabel psmMatrixFileLabel = new JLabel("Select Purity Correction Matrix file:");
                m_psmMatrixPanel.add(psmMatrixFileLabel, gbc);

                gbc.gridx++;
                gbc.insets = new Insets(5, 5, 5, 2);
                JButton fileChooser = new JButton(IconManager.getIcon(IconManager.IconType.OPEN_FILE));
                fileChooser.setMargin(new Insets(5, 5, 5, 5));
                fileChooser.addActionListener(e -> getPurityMatrixFile());
                m_psmMatrixPanel.add(fileChooser, gbc);

                gbc.gridx++;
                gbc.weightx = 1;
                gbc.insets = new Insets(5, 0, 5, 5);
                m_psmFileMatrixStatus = new JLabel();
                m_psmFileMatrixStatus.setIcon(IconManager.getIcon(IconManager.IconType.CROSS_SMALL16));
                m_psmFileMatrixStatus.setBorder(new EmptyBorder(new Insets(1, 1, 1, 1)));
                m_psmMatrixPanel.add(m_psmFileMatrixStatus, gbc);


                //add create Matrix access
                gbc.gridx = 0;
                gbc.gridy++;
                gbc.weightx = 0;
                JLabel label2 = new JLabel("Specify Purity Correction Matrix :");
                m_psmMatrixPanel.add(label2, gbc);
                gbc.gridx++;
                gbc.weightx = 0;
                gbc.insets = new Insets(5, 5, 5, 2);
                JButton matrixAccess = new JButton(IconManager.getIcon(IconManager.IconType.GRID));
                matrixAccess.setMargin(new Insets(5, 5, 5, 5));
                matrixAccess.addActionListener(e -> openMatrixDialog());
                m_psmMatrixPanel.add(matrixAccess, gbc);

                gbc.gridx++;
                gbc.weightx = 1;
                gbc.insets = new Insets(5, 0, 5, 5);
                m_psmTableMatrixStatus = new JLabel();
                m_psmTableMatrixStatus.setIcon(IconManager.getIcon(IconManager.IconType.CROSS_SMALL16));
                m_psmTableMatrixStatus.setBorder(new EmptyBorder(new Insets(1, 1, 1, 1)));
                m_psmMatrixPanel.add(m_psmTableMatrixStatus, gbc);

                //Buttons
                gbc.gridy++;
                gbc.anchor = GridBagConstraints.EAST;
                gbc.fill = GridBagConstraints.NONE;
                gbc.gridx++;
                gbc.weightx = 1;
                gbc.insets = new Insets(5, 0, 5, 2);
                m_psmMatrixPanel.add(Box.createHorizontalGlue(), gbc);

                gbc.gridx++;

                gbc.weightx = 0;
                JButton matrixView = new JButton("View Matrix");
                matrixView.setMargin(new Insets(5, 5, 5, 5));
                matrixView.addActionListener(e -> viewMatrixDialog());
                m_psmMatrixPanel.add(matrixView, gbc);

                gbc.gridx++;
                gbc.weightx = 0;
                m_savePurityMatrix = new JButton("Save Matrix");
                m_savePurityMatrix.setMargin(new Insets(5, 5, 5, 5));
                m_savePurityMatrix.addActionListener(e -> saveMatrixDialog());
                m_psmMatrixPanel.add(m_savePurityMatrix, gbc);

            } else { //readOnly : display purityMatrix
                m_psmMatrixPanel = new JPanel();
                m_psmMatrixPanel.setLayout(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.anchor = GridBagConstraints.WEST;
                gbc.fill = GridBagConstraints.BOTH;
                gbc.weightx = 1;
                gbc.weighty = 1;
                gbc.insets = new Insets(5, 5, 5, 5);
                gbc.gridx = 0;
                gbc.gridy = 0;

                JScrollPane readOnlyTableScrollPane = new JScrollPane();
                DecoratedTable purityCorrectionTable = createReadOnlyTable();
                readOnlyTableScrollPane.setViewportView(purityCorrectionTable);
                readOnlyTableScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                readOnlyTableScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
                purityCorrectionTable.setFillsViewportHeight(true);

                m_psmMatrixPanel.add(readOnlyTableScrollPane, gbc);
            }
            setEnabled(m_psmMatrixPanel, !m_readOnly);
            psmMatrixPanel.add(m_psmMatrixPanel, BorderLayout.NORTH);
            psmPanel.add(psmMatrixPanel, BorderLayout.CENTER);
        }
        return psmPanel;
    }

    private void saveMatrixDialog()  {
        if(m_purityMatrixValues == null || m_purityMatrixValues.isEmpty()) {
            JOptionPane.showMessageDialog(this.getParent(),"\nNo Purity matrix was defined yet ...\n","View Purity Matrix", JOptionPane.ERROR_MESSAGE);

        } else {
            JFileChooser fchooser = new JFileChooser();
            fchooser.setMultiSelectionEnabled(false);
            int result = fchooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File newFile = fchooser.getSelectedFile();
                try {
                    if(newFile.exists()){
                        int resultOverwrite = JOptionPane.showConfirmDialog(this, "File exist. Overwrite ?!","Overwrite File",JOptionPane.YES_NO_OPTION);
                        if(resultOverwrite == JOptionPane.YES_OPTION)
                            newFile.delete();
                        else
                            return;
                    }

                    int resultSave = JOptionPane.showConfirmDialog(this, "Save current purity matrix in file "+newFile.getName()+" (in generic format) ? ","Save matrix",JOptionPane.YES_NO_OPTION);
                    if(resultSave == JOptionPane.YES_OPTION) {
                        FileWriter fw = new FileWriter(newFile);
                        fw.write(m_purityMatrixValues);
                        fw.close();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private DecoratedTable createReadOnlyTable(){
        DecoratedTable table = new DecoratedTable() {
            @Override
            public void prepostPopupMenu() {
            }

            @Override
            public TablePopupMenu initPopupMenu() {
                return null;
            }
        };

        table.removeStriping();
        m_readOnlyPurityTableModel = new PurityCorrectionMatrixTableModel(m_quantitationMethod, true);
        table.setModel(m_readOnlyPurityTableModel);
        table.getTableHeader().setDefaultRenderer(DefaultColoredCellRenderer.disabledCellRendered);
        TableColumnModel columnModel = table.getColumnModel();
        // set preferred width of different columns
        int colCount= m_readOnlyPurityTableModel.getColumnCount();
        for(int i=0; i<colCount; i++){
            if(i == m_readOnlyPurityTableModel.getCenterColIndex()) {
                columnModel.getColumn(i).setMaxWidth(40);
            } else if( i ==0) {
                columnModel.getColumn(i).setMaxWidth(80);
            } else {
                int colWidth = m_readOnlyPurityTableModel.getCoeffColumWidth();
                columnModel.getColumn(i).setMaxWidth(colWidth);
            }
        }

        return table;
    }

    private void viewMatrixDialog() {

        if(m_purityMatrixValues == null || m_purityMatrixValues.isEmpty()) {
            JOptionPane.showMessageDialog(this.getParent(),"\nNo Purity matrix was defined yet ...\n","View Purity Matrix", JOptionPane.ERROR_MESSAGE);

        } else {
            EditIsobaricMatrixDialog viewDialog = new EditIsobaricMatrixDialog(WindowManager.getDefault().getMainWindow(), m_quantitationMethod, convertStringToDoubleMatrix(m_purityMatrixValues));
            viewDialog.setMaximumSize(new Dimension(100,100));

            viewDialog.setVisible(true);
        }
    }

    private EditIsobaricMatrixDialog m_matrixDialog;
    private void openMatrixDialog() {
        if(m_matrixDialog == null) {
            m_matrixDialog = new EditIsobaricMatrixDialog(WindowManager.getDefault().getMainWindow(), m_quantitationMethod);
        }
        m_matrixDialog.setVisible(true);
        m_purityMatrixValues = null;
        m_psmFileMatrixStatus.setIcon(IconManager.getIcon(IconManager.IconType.CROSS_SMALL16));
        if (m_matrixDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            m_purityMatrixValues = m_matrixDialog.getPurityMatrix();
            m_psmTableMatrixStatus.setIcon(IconManager.getIcon(IconManager.IconType.TICK_SMALL));
        } else {
            m_psmTableMatrixStatus.setIcon(IconManager.getIcon(IconManager.IconType.CROSS_SMALL16));
        }
    }


    private int getAggLabelMaxSize(){
        FontMetrics fm = new JLabel().getFontMetrics(new JLabel().getFont());
        int width = fm.stringWidth(m_pepIonsAbundanceSummarizingMethodParameter.getName());
        int width1 = fm.stringWidth(m_psmAbundanceSummarizingMethodParameter.getName());

        if(width<width1)
            width=width1;
        width1 = fm.stringWidth(m_peptideAbundanceSummarizingMethodParameter.getName());
        if(width<width1)
            width=width1;
        return  width;
    }

    private JPanel getAggregationCfgPanel() {
        JPanel tabPanel = new JPanel(new GridBagLayout());

        GridBagConstraints tabPanelC = new GridBagConstraints();
        tabPanelC.anchor = GridBagConstraints.NORTHWEST;
        tabPanelC.fill = GridBagConstraints.BOTH;
        tabPanelC.insets = new Insets(5, 2, 2, 2);
        tabPanelC.gridwidth=1;
        tabPanelC.gridx=0;
        tabPanelC.gridy=0;
        tabPanelC.weightx=1;

        int labelWidth = getAggLabelMaxSize();
        int labelHeight = 20;

        if(m_quantitationMethodInfo.equals(DDatasetType.QuantitationMethodInfo.ISOBARIC_TAGGING)) {
            // PepIon agg
            JPanel psmCfgPanel = new JPanel(new GridBagLayout());
            psmCfgPanel.setBorder(BorderFactory.createTitledBorder(" To Pep. Ions "));
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 0;
            c.weighty = 0;
            c.gridx = 0;
            c.gridy = 0;
            c.insets = new Insets(5, 5, 5, 5);
            JLabel psmAblabel = new JLabel(m_psmAbundanceSummarizingMethodParameter.getName());
            psmAblabel.setPreferredSize(new Dimension(labelWidth, labelHeight));
            psmAblabel.setHorizontalAlignment(SwingConstants.RIGHT);
            psmCfgPanel.add(psmAblabel, c);

            c.weightx = 1;
            c.gridx++;
            psmCfgPanel.add(m_psmAbundanceSummarizingMethodCB, c);

            tabPanel.add(psmCfgPanel, tabPanelC);
            tabPanelC.gridy++;
        }

        // Peptide agg
        JPanel pepCfgPanel = new JPanel(new GridBagLayout());
        pepCfgPanel.setBorder(BorderFactory.createTitledBorder(" To Peptides "));
        GridBagConstraints c2 = new GridBagConstraints();
        c2.anchor = GridBagConstraints.NORTHWEST;
        c2.fill = GridBagConstraints.BOTH;
        c2.insets = new Insets(5, 5, 5, 5);
        c2.gridx = 0;
        c2.gridy = 0;
        c2.weightx = 0;
        c2.gridwidth = 1;
        JLabel ionAbundanceSummarizerMethodLabel = new JLabel(m_pepIonsAbundanceSummarizingMethodParameter.getName());// "Peptide Ion Abundance Summarizer Method :");
        ionAbundanceSummarizerMethodLabel.setPreferredSize(new Dimension(labelWidth, labelHeight));
        ionAbundanceSummarizerMethodLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        pepCfgPanel.add(ionAbundanceSummarizerMethodLabel, c2);

        c2.gridx++;
        c2.weightx = 1;
        pepCfgPanel.add(m_peptideIonAbundanceSummarizingMethodCB, c2);

        tabPanel.add(pepCfgPanel, tabPanelC);

        // Protein agg
        JPanel protCfgPanel = new JPanel(new GridBagLayout());
        protCfgPanel.setBorder(BorderFactory.createTitledBorder(" To Proteins "));
        GridBagConstraints c3 = new GridBagConstraints();
        c3.anchor = GridBagConstraints.NORTHWEST;
        c3.fill = GridBagConstraints.NONE;
        c3.insets = new Insets(5, 5, 5, 5);
        c3.gridx = 0;
        c3.gridy = 0;
        c3.gridwidth = 1;
        JLabel abundanceSummarizerMethodLabel = new JLabel(m_peptideAbundanceSummarizingMethodParameter.getName());//"Abundance Summarizer Method :");
        abundanceSummarizerMethodLabel.setPreferredSize(new Dimension(labelWidth, labelHeight));
        abundanceSummarizerMethodLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        protCfgPanel.add(abundanceSummarizerMethodLabel, c3);

        c3.fill = GridBagConstraints.HORIZONTAL;
        c3.gridx++;
        c3.weightx = 1;
        protCfgPanel.add(m_peptideAbundanceSummarizingMethodCB, c3);

        tabPanelC.gridy++;
        tabPanel.add(protCfgPanel, tabPanelC);

        tabPanelC.gridy++;
        tabPanelC.weighty = 1;
        tabPanel.add(Box.createVerticalGlue(), tabPanelC);

        return tabPanel;
    }

    private void getPurityMatrixFile(){
        JFileChooser fchooser = new JFileChooser();
        fchooser.setMultiSelectionEnabled(false);
        fchooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fchooser.showOpenDialog(this);
        m_purityMatrixFile = null;
        m_purityMatrixValues=null;
        if (result == JFileChooser.APPROVE_OPTION) {
            m_psmTableMatrixStatus.setIcon(IconManager.getIcon(IconManager.IconType.CROSS_SMALL16));
            m_purityMatrixFile = fchooser.getSelectedFile();
            boolean fileOk = readPurityMatrixFile();
            if(fileOk){
                m_psmFileMatrixStatus.setIcon(IconManager.getIcon(IconManager.IconType.TICK_SMALL));
            } else {
                m_psmFileMatrixStatus.setIcon(IconManager.getIcon(IconManager.IconType.CROSS_SMALL16));
                m_purityMatrixFile = null;
            }
        } else {
            m_psmFileMatrixStatus.setIcon(IconManager.getIcon(IconManager.IconType.CROSS_SMALL16));
        }
    }

    private boolean readPurityMatrixFile(){
        //read values from file
        if(m_purityMatrixFile != null && m_purityMatrixFile.exists()){
            try {
                BufferedReader br = new BufferedReader(new FileReader(m_purityMatrixFile));
                String purityMatrixValues = br.lines().collect(Collectors.joining(" "));
                Double[][] matrix = convertStringToDoubleMatrix(purityMatrixValues);
                if(matrix == null){
                    m_logger.warn("Invalid matrix file specified");
                    return false;
                }
                m_purityMatrixValues =purityMatrixValues;
                return true;
            } catch (FileNotFoundException e) {
                m_logger.warn("Quant Post Processing Error: Unable to read purity matrix file");
                return false;
            }
        }
        return false;
    }

    boolean checkQuantPostProcessingParam() {
        errorMsg = "";
        errorCompo = null;

        if(m_quantitationMethodInfo.equals(DDatasetType.QuantitationMethodInfo.ISOBARIC_TAGGING)  && m_isValidLabeledQMethod && m_usePurityCorrectionMatrixCBoxTitle.isSelected()){
          if(m_purityMatrixValues == null || m_purityMatrixValues.isEmpty()) {//  if(m_purityMatrixFile == null ||!m_purityMatrixFile.exists()) {
            errorMsg = "No (or invalid) purity matrix specified ";
            errorCompo = m_psmMatrixPanel; //m_usePurityCorrectionMatrixCBoxTitle.getInternalCheckBox();
            return false;
          }

           //check specified matrix
          Double[][] matrix = convertStringToDoubleMatrix(m_purityMatrixValues);
          if(matrix == null){
              errorMsg = "Invalid purity matrix specified ";
              errorCompo = m_psmMatrixPanel; //m_usePurityCorrectionMatrixCBoxTitle.getInternalCheckBox();
              return false;
          }
        }

        boolean discardPSMOK = true;
        if(m_discardModifiedPeptidesChB.isSelected()){
            discardPSMOK = false;
            for(JCheckBox cb : m_peptideModificationListChB){
                if(cb.isSelected()){
                    discardPSMOK = true;
                    break;
                }
            }
            if(!discardPSMOK){
                errorMsg = "No modification specified for Discard modified peptides option.";
                errorCompo =m_discardModifiedPeptidesChB;
            }
        }
        return discardPSMOK;
    }

    private String errorMsg;
    private Component errorCompo;
    public String getCheckErrorMessage(){
        return errorMsg;
    }

    public Component getCheckErrorComponent(){
        return errorCompo;
    }

    public Map<String, Object> getQuantParams() {
        //for tab Pep. selection
        Map<String, Object> params = new HashMap<>();
        params.put(QuantPostProcessingParams.CONFIG_VERSION,QuantPostProcessingParams.CURRENT_VERSION);
        params.put(QuantPostProcessingParams.DISCARD_PIF_PEPTIDE_MATCHES, m_usePIFCBoxTitle.isSelected()); //To add to IHM
        params.put(QuantPostProcessingParams.DISCARD_PIF_PEPTIDE_MATCHES_VALUE, Float.valueOf(m_discardPSMPIFValueTF.getText())); //To add to IHM
        params.put(QuantPostProcessingParams.DISCARD_MISS_CLEAVED_PEPTIDES, m_discardMissCleavedPeptidesChB.isSelected());
        params.put(QuantPostProcessingParams.DISCARD_MODIFIED_PEPTIDES, m_discardModifiedPeptidesChB.isSelected());
        params.put(QuantPostProcessingParams.PEP_ION_ABUNDANCE_SUMMARIZING_METHOD, QuantPostProcessingParams.getPepIonAbundanceSummarizingMethodKeys()[m_peptideIonAbundanceSummarizingMethodCB.getSelectedIndex()]);

        params.put(QuantPostProcessingParams.PEPTIDE_SELECTION_METHOD, QuantPostProcessingParams.getPeptidesSelectionMethodKeys()[m_peptidesSelectionMethodCB.getSelectedIndex()]);

        Map<String, Object> modifiedPeptideFilterConfigMap = new HashMap<>();
        List<Long> ptmIds = new ArrayList<>();
        for (BooleanParameter ptmToDiscardParameter : m_peptidesModificationListParameter) {
            if (((JCheckBox) ptmToDiscardParameter.getComponent()).isSelected()) {
                Long ptmId = (Long) ptmToDiscardParameter.getAssociatedData();
                ptmIds.add(ptmId);
            }
        }
        modifiedPeptideFilterConfigMap.put(QuantPostProcessingParams.PTM_DEFINITION_IDS_TO_DISCARD, ptmIds);//list of PTM
        params.put(QuantPostProcessingParams.MODIFIED_PEPTIDE_FILTER_CONFIG,modifiedPeptideFilterConfigMap);
        params.put(QuantPostProcessingParams.MODIFIED_PEPTIDE_FILTERING_METHOD, QuantPostProcessingParams.getModifiedPeptideFilteringMethodKeys()[m_modifiedPeptidesFilteringMethodCB.getSelectedIndex()]);
        params.put(QuantPostProcessingParams.DISCARD_PEP_IONS_SHARING_PEAKELS, m_discardPeptideIonsSharingPeakelsChB.isSelected()); //last one
        params.put(QuantPostProcessingParams.PEPTIDE_ABUNDANCE_SUMMARIZING_METHOD, QuantPostProcessingParams.getPeptideAbundanceSummarizingMethodKeys()[m_peptideAbundanceSummarizingMethodCB.getSelectedIndex()]);//shown in Protein tab
        params.put(QuantPostProcessingParams.APPLY_PROFILE_CLUSTERING, false);
        params.put(QuantPostProcessingParams.USE_PURITY_CORRECTION_MATRIX, m_usePurityCorrectionMatrixCBoxTitle.isSelected());
        params.put(QuantPostProcessingParams.PSM_ABUNDANCE_SUMMARIZING_METHOD_KEY,QuantPostProcessingParams.getPSMAbundanceSummarizingMethodKeys()[m_psmAbundanceSummarizingMethodCB.getSelectedIndex()] );
        //read file if exist
        if(m_purityMatrixValues != null && !m_purityMatrixValues.isEmpty()){
            params.put(QuantPostProcessingParams.PURITY_CORRECTION_MATRIX, m_purityMatrixValues);
        }

        //Use default value for all but ApplyNormalization . Not used from Studio (pep. Configuration)
        Map<String, Object> peptideStatConfigMap = new HashMap<>();
        peptideStatConfigMap.put(QuantPostProcessingParams.STAT_TESTS_ALPHA,"0.01");
        peptideStatConfigMap.put(QuantPostProcessingParams.APPLY_NORMALIZATION, m_applyPepNormalizationChB.isSelected());
        peptideStatConfigMap.put(QuantPostProcessingParams.APPLY_MISS_VAL_INFERENCE, false);
        peptideStatConfigMap.put(QuantPostProcessingParams.APPLY_VARIANCE_CORRECTION, false);
        peptideStatConfigMap.put(QuantPostProcessingParams.APPLY_T_TEST,false);
        peptideStatConfigMap.put(QuantPostProcessingParams.APPLY_Z_TEST, false);
        params.put(QuantPostProcessingParams.getPrefixedParam("peptide", QuantPostProcessingParams.STAT_CONFIG), peptideStatConfigMap);

        //Use default value for all but ApplyNormalization . Not used from Studio (Prot. Configuration)
        Map<String, Object> proteinStatConfigMap = new HashMap<>();
        proteinStatConfigMap.put(QuantPostProcessingParams.STAT_TESTS_ALPHA, "0.01");
        proteinStatConfigMap.put(QuantPostProcessingParams.APPLY_NORMALIZATION, m_applyProtNormalizationChB.isSelected());
        proteinStatConfigMap.put(QuantPostProcessingParams.APPLY_MISS_VAL_INFERENCE, false);
        proteinStatConfigMap.put(QuantPostProcessingParams.APPLY_VARIANCE_CORRECTION, false);
        proteinStatConfigMap.put(QuantPostProcessingParams.APPLY_T_TEST, false);
        proteinStatConfigMap.put(QuantPostProcessingParams.APPLY_Z_TEST, false);
        params.put(QuantPostProcessingParams.getPrefixedParam("protein", QuantPostProcessingParams.STAT_CONFIG), proteinStatConfigMap);

        return params;
    }

    /**
     * set the refined parameters (used by display)
     *
     * @param refinedParams
     */
    public void setRefinedParams(Map<String, Object> refinedParams) {
        //Get config_version
        boolean isVersion3 = false;
        boolean isVersion2 = false;
        boolean isVersion1 = false;
        String version = (String) refinedParams.get(QuantPostProcessingParams.CONFIG_VERSION);
        Object isDiscardModifiedPeptide = refinedParams.get(QuantPostProcessingParams.DISCARD_MODIFIED_PEPTIDES);
        if(version == null){
            isVersion1 = true;
        } else if("2.0".equals(version)) {
            isVersion2= true;
        } else {
            isVersion3 = true;
        }

        String selectionMethod = "";
        if(isVersion3){
            selectionMethod = (String) refinedParams.getOrDefault(QuantPostProcessingParams.PEPTIDE_SELECTION_METHOD, QuantPostProcessingParams.PEPTIDES_SELECTION_ALL_PEPTIDES_METHOD_KEY);
        } else {
            boolean useOnlySpecificPeptides = Boolean.parseBoolean(refinedParams.getOrDefault(QuantPostProcessingParams.USE_ONLY_SPECIFIC_PEPTIDES,"false").toString());
            selectionMethod = useOnlySpecificPeptides ? QuantPostProcessingParams.PEPTIDES_SELECTION_SPECIFIC_METHOD_KEY : QuantPostProcessingParams.PEPTIDES_SELECTION_ALL_PEPTIDES_METHOD_KEY;
        }

        String[] pepSelectionMethodKeys = QuantPostProcessingParams.getPeptidesSelectionMethodKeys();
        int index = 0;
        for (int i = 0; i < pepSelectionMethodKeys.length; i++) {
            if (pepSelectionMethodKeys[i].equals(selectionMethod)) {
                index = i;
                break;
            }
        }
        m_peptidesSelectionMethodCB.setSelectedIndex(index);

        Object isDmcPep = isVersion1 ? refinedParams.getOrDefault(QuantPostProcessingParams.DISCARD_MISS_CLEAVED_PEPTIDES_PREV,"false") : refinedParams.getOrDefault(QuantPostProcessingParams.DISCARD_MISS_CLEAVED_PEPTIDES, "false");
        m_discardMissCleavedPeptidesChB.setSelected(Boolean.parseBoolean(isDmcPep.toString()));

        boolean isV1DiscardOxidPepSelected = false;
        if (isVersion1) {
            isV1DiscardOxidPepSelected = Boolean.parseBoolean(refinedParams.get(QuantPostProcessingParams.DISCARD_OXIDIZED_PEPTIDES).toString());
            m_discardModifiedPeptidesChB.setSelected(isV1DiscardOxidPepSelected);
            m_modifiedPeptidesFilteringMethodLabel.setEnabled(false);
        } else {//V2
            m_discardModifiedPeptidesChB.setSelected(Boolean.parseBoolean(isDiscardModifiedPeptide.toString()));//V2
        }

        ArrayList ptmIdListFromParam = null;
        if(isVersion2){
            ptmIdListFromParam = (ArrayList) refinedParams.get(QuantPostProcessingParams.PTM_DEFINITION_IDS_TO_DISCARD);//return is an Integer ArrayList, not desired Long ArrayList
        } else if (isVersion3){
            Map<String, Object> modifiedPeptideFilterConfigMap =  (Map<String, Object>) refinedParams.get(QuantPostProcessingParams.MODIFIED_PEPTIDE_FILTER_CONFIG);
            ptmIdListFromParam = (ArrayList) modifiedPeptideFilterConfigMap.get(QuantPostProcessingParams.PTM_DEFINITION_IDS_TO_DISCARD);
        }

        List<Long> ptmIdList = new ArrayList<>();
        if (ptmIdListFromParam != null) {
            //convert Integer ArrayList to Long ArrayList
            for (Object l : ptmIdListFromParam) {
                ptmIdList.add(Long.parseLong("" + l));
            }
        } else if(isV1DiscardOxidPepSelected) {//V1
            ptmIdList = new ArrayList<>();//a list of Long
            for (Long id : m_ptmSpecificityNameById.keySet()) {
                if (m_ptmSpecificityNameById.get(id).contains("Oxidation")) {
                    ptmIdList.add(id);
                }
            }
        }

        for (BooleanParameter ptmToDiscardParameter : m_peptidesModificationListParameter) {
            JCheckBox ptmChB = (JCheckBox) ptmToDiscardParameter.getComponent();
            Long ptmId = (Long) ptmToDiscardParameter.getAssociatedData();
            ptmChB.setSelected(ptmIdList.contains(ptmId));
        }

        String pmfMethod = isVersion1 ? "" : (String) refinedParams.get(QuantPostProcessingParams.MODIFIED_PEPTIDE_FILTERING_METHOD) ;
        index = 0;
        String[] modifPepFilteringMethodKeys = QuantPostProcessingParams.getModifiedPeptideFilteringMethodKeys();
        for (int i = 0; i < modifPepFilteringMethodKeys.length; i++) {
            if (modifPepFilteringMethodKeys[i].equals(pmfMethod)) {
                index = i;
                break;
            }
        }
        m_modifiedPeptidesFilteringMethodCB.setSelectedIndex(index);//V2

        Boolean discardSharingPeakel = isVersion3 ? Boolean.valueOf(refinedParams.get(QuantPostProcessingParams.DISCARD_PEP_IONS_SHARING_PEAKELS).toString()) : Boolean.valueOf(refinedParams.get(QuantPostProcessingParams.DISCARD_PEPTIDES_SHARING_PEAKELS).toString());
        m_discardPeptideIonsSharingPeakelsChB.setSelected(discardSharingPeakel);

        Boolean usePifOption = Boolean.valueOf(refinedParams.getOrDefault(QuantPostProcessingParams.DISCARD_PIF_PEPTIDE_MATCHES, false).toString());
        m_usePIFCBoxTitle.setSelected(usePifOption);
        if(usePifOption){
            try {
                Float pifValue = Float.valueOf(refinedParams.getOrDefault(QuantPostProcessingParams.DISCARD_PIF_PEPTIDE_MATCHES_VALUE, QuantPostProcessingParams.DEFAULT_PIF_FILTER_VALUE).toString());
                m_discardPSMPIFValueTF.setText("" + pifValue);
            } catch (NumberFormatException ex) {
                m_logger.error("error while settings m_discardPSMPIFValueTF quantification params " + ex);
                m_discardPSMPIFValueTF.setText("" + QuantPostProcessingParams.DEFAULT_PIF_FILTER_VALUE);
            }
        }


        Boolean usePurityCorrMatrix = Boolean.valueOf(refinedParams.getOrDefault(QuantPostProcessingParams.USE_PURITY_CORRECTION_MATRIX, false).toString());
        m_usePurityCorrectionMatrixCBoxTitle.setSelected(usePurityCorrMatrix);
        if(usePurityCorrMatrix){
            m_purityMatrixValues = refinedParams.getOrDefault(QuantPostProcessingParams.PURITY_CORRECTION_MATRIX, "").toString();
            m_readOnlyPurityTableModel.setData(convertStringToDoubleMatrix(m_purityMatrixValues));
        }

        String[] abundanceSummarizingMethodKeys = QuantPostProcessingParams.getPeptideAbundanceSummarizingMethodKeys();
        String summarisedMethodKey = isVersion3 ? (String) refinedParams.get(QuantPostProcessingParams.PEPTIDE_ABUNDANCE_SUMMARIZING_METHOD) : (String) refinedParams.get(QuantPostProcessingParams.ABUNDANCE_SUMMARIZING_METHOD);
        index = 0;
        for (int i = 0; i < abundanceSummarizingMethodKeys.length; i++) {
            if (abundanceSummarizingMethodKeys[i].equals(summarisedMethodKey)) {
                index = i;
                break;
            }
        }
        m_peptideAbundanceSummarizingMethodCB.setSelectedIndex(index);

        String ionPepSummarisingMethodKey = isVersion1 ? QuantPostProcessingParams.ION_ABUNDANCE_SUMMARIZING_BEST_ION_METHOD_KEY : (String) refinedParams.getOrDefault(QuantPostProcessingParams.PEP_ION_ABUNDANCE_SUMMARIZING_METHOD, QuantPostProcessingParams.ION_ABUNDANCE_SUMMARIZING_BEST_ION_METHOD_KEY);
        String[] ionAbundanceSummarizingMethodKeys = QuantPostProcessingParams.getPepIonAbundanceSummarizingMethodKeys();
        index = 0;
        for (int i = 0; i < ionAbundanceSummarizingMethodKeys.length; i++) {
            if (ionAbundanceSummarizingMethodKeys[i].equals(ionPepSummarisingMethodKey)) {
                index = i;
                break;
            }
        }
        m_peptideIonAbundanceSummarizingMethodCB.setSelectedIndex(index);

        Map<String, Object> peptideStatConfigMap = (Map<String, Object>) refinedParams.get(QuantPostProcessingParams.getPrefixedParam("peptide", QuantPostProcessingParams.STAT_CONFIG));
        m_applyPepNormalizationChB.setSelected(Boolean.parseBoolean(peptideStatConfigMap.get(QuantPostProcessingParams.APPLY_NORMALIZATION).toString()));

        Map<String, Object> proteinStatConfigMap = (Map<String, Object>) refinedParams.get(QuantPostProcessingParams.getPrefixedParam("protein", QuantPostProcessingParams.STAT_CONFIG));
        m_applyProtNormalizationChB.setSelected(Boolean.valueOf(proteinStatConfigMap.get(QuantPostProcessingParams.APPLY_NORMALIZATION).toString()));

        updateDiscardPTMs();
        updateDiscardPIF();
    }

    private Double[][] convertStringToDoubleMatrix(String input) {
        if(input == null || input.isEmpty())
            return null;

        try {
            String[] allCoeffs = input.substring(1, input.length() - 1).replaceAll("\\s", "").split("\\],\\[");
            Double[][] finalMatrix = new Double[allCoeffs.length][];

            for (int i = 0; i < allCoeffs.length; i++) {
                String aReporterCoeffList = allCoeffs[i].replaceAll("[\\[\\]\\s]", "");
                String[] indivCoefList = aReporterCoeffList.split(",");
                if(indivCoefList.length != allCoeffs.length){ //not square matrix ...
                    m_logger.debug(" Error converting matrix : not square "+allCoeffs.length+" X "+indivCoefList.length);
                    return null;
                }
                // Initialize the row in the result matrix
                finalMatrix[i] = new Double[indivCoefList.length];
                for (int j = 0; j < indivCoefList.length; j++) {
                    finalMatrix[i][j] = Double.parseDouble(indivCoefList[j]);
                }
            }
            return finalMatrix;
        }catch (Exception e){
            m_logger.debug(" Error converting matrix : Exception "+e.getMessage());
            return null;
        }
    }


    /**
     * for XIC Aggregation, the m_discardPeptidesSharingPeakelsChB should be
     * invalid; but for other XIC, this option must be enable to avoid
     * preference parameters setting.
     *
     * @param isAggregation
     */
    public void setDiscardPeptidesSharingPeakelsChB(boolean isAggregation) {
        if (isAggregation) {
            m_discardPeptideIonsSharingPeakelsChB.setSelected(false);
        }
        m_discardPeptideIonsSharingPeakelsChB.setEnabled(!isAggregation);
    }
}

