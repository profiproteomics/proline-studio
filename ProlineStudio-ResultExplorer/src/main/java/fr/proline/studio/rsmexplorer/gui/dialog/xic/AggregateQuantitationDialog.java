/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.core.orm.uds.BiologicalGroup;
import fr.proline.core.orm.uds.BiologicalSample;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.WizardPanel;
import fr.proline.studio.rsmexplorer.gui.TreeUtils;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.xic.XICBiologicalGroupNode;
import fr.proline.studio.rsmexplorer.tree.xic.XICBiologicalSampleAnalysisNode;
import fr.proline.studio.rsmexplorer.tree.xic.XICBiologicalSampleNode;
import fr.proline.studio.rsmexplorer.tree.xic.XICDesignTree;
import fr.proline.studio.rsmexplorer.tree.xic.DatasetReferenceNode;
import fr.proline.studio.utils.IconManager;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Window;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.JPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
public class AggregateQuantitationDialog extends DefaultDialog {

    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private static AggregateQuantitationDialog m_singletonDialog = null;

    private static final int STEP_PANEL_DEFINE_EXP_DESIGN = 0;
    private static final int STEP_PANEL_DEFINE_AGGREGATION_PARAMS = 1;

    private int m_step = STEP_PANEL_DEFINE_EXP_DESIGN;

    public static AggregateQuantitationDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new AggregateQuantitationDialog(parent);
        }
        return m_singletonDialog;
    }

    private JPanel m_designPanel = null;
    private AbstractNode m_experimentalDesignNode = null;
    private List<DDataset> m_quantitations;
    private AggregationQuantChannelsPanel m_quantChannelsPanel;
    private DDataset m_refDataset = null;


    private AggregateQuantitationDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
        setTitle("Aggregate Quantitation Wizard");
        setDocumentationSuffix("id.2dlolyb");
        setSize(750, 576);
        setResizable(true);
    }

     @Override
    public void pack() {
        // forbid pack by overloading the method
    }
    
    public Map<String, Object> getExperimentalDesignParameters() throws IllegalAccessException {
        if (m_experimentalDesignNode == null) {
            throw new IllegalAccessException("Design parameters have not been set.");
        }
        
        Map<String, Object> experimentalDesignParams = XICDesignTree.toExperimentalDesignParameters(m_experimentalDesignNode, m_refDataset, null);
        return experimentalDesignParams;
    }

    public Map<String, Object> getQuantiParameters() {
        Map<String, Object> aggregationConfig = new HashMap<>();
        aggregationConfig.put("quantitation_ids", m_quantitations.stream().map(d -> d.getId()).toArray());
        aggregationConfig.put("quant_channels_mapping", m_quantChannelsPanel.getQuantChannelsMatching());
        aggregationConfig.put("intensity_computation_method_name", "INTENSITY_SUM"); // change manually to MOST_INTENSE to test another summarization method
        return aggregationConfig;
    }

    public AbstractNode getExperimentalDesignNode() {
        return m_experimentalDesignNode;
    }

    public void setQuantitationDatasets(List<DDataset> loadedQuantitations) {
        m_quantitations = loadedQuantitations;
                
        List<Long> refDatasetIds = m_quantitations.stream().map( ds -> ds.getMasterQuantitationChannels().get(0).getIdentDataset()).map(ids -> (ids == null) ? -1 : ids.getId()).distinct().collect(Collectors.toList());
        // If all quantification datasets are using the same reference, set that reference to m_refDataset
        if (refDatasetIds.size() == 1 && refDatasetIds.get(0) != -1) {
            DDataset dataset = m_quantitations.get(0);
            m_refDataset = dataset.getMasterQuantitationChannels().get(0).getIdentDataset();
        } else {
            m_refDataset = null;
        }
        displayExperimentalDesignTree();
    }

    public void displayExperimentalDesignTree() {
      AbstractNode rootNode = inferExperimentalDesign();
      displayExperimentalDesignTree(rootNode);
    }

    public void displayExperimentalDesignTree(AbstractNode node) {
        if (m_quantitations != null && m_quantitations.size() > 0) {
            m_step = STEP_PANEL_DEFINE_EXP_DESIGN;
            setButtonName(DefaultDialog.BUTTON_OK, "Next");
            setButtonIcon(DefaultDialog.BUTTON_OK, IconManager.getIcon(IconManager.IconType.ARROW));
            setButtonVisible(BUTTON_LOAD, false);
            setButtonVisible(BUTTON_SAVE, false);
            setButtonVisible(BUTTON_BACK, false);

            if (node != m_experimentalDesignNode) {
                m_experimentalDesignNode = node;
                m_designPanel = new JPanel();
                m_designPanel.setLayout(new BorderLayout());
                XICDesignTree designTree = new XICDesignTree(m_experimentalDesignNode, true);
                m_designPanel.add(new WizardPanel("<html><b>Step 1:</b> Define the aggregation experimental design.</html>"), BorderLayout.NORTH);
                m_designPanel.add(designTree, BorderLayout.CENTER);
                
                TreeUtils.expandTree(designTree, true);
            }
            replaceInternalComponent(m_designPanel);
            revalidate();
            repaint();
        }
    }

    private void displayQuantChannelsMapping() {
        m_step = STEP_PANEL_DEFINE_AGGREGATION_PARAMS;

        setButtonName(DefaultDialog.BUTTON_OK, org.openide.util.NbBundle.getMessage(DefaultDialog.class, "DefaultDialog.okButton.text"));
        setButtonIcon(DefaultDialog.BUTTON_OK, IconManager.getIcon(IconManager.IconType.OK));

        setButtonVisible(BUTTON_BACK, true);
        setButtonVisible(BUTTON_LOAD, false);
        setButtonVisible(BUTTON_SAVE, false);

        m_quantChannelsPanel = AggregationQuantChannelsPanel.getPanel(m_experimentalDesignNode, m_quantitations);
        replaceInternalComponent(m_quantChannelsPanel);
        revalidate();
        repaint();
    }

    @Override
    protected boolean okCalled() {
        if (m_step == STEP_PANEL_DEFINE_EXP_DESIGN) {
            displayQuantChannelsMapping();
            return false;
        }
        return true;
    }

    @Override
    protected boolean backCalled() {
        if (m_step == STEP_PANEL_DEFINE_AGGREGATION_PARAMS) {
            displayExperimentalDesignTree(m_experimentalDesignNode);
            return false;
        }
        return false;
    }

    private DataSetNode inferExperimentalDesign() {
        int childIndex = 0;
        
        DataSetNode rootNode = new DataSetNode(DataSetData.createTemporaryQuantitation("XIC Aggregation")); //new DataSetData("XIC Aggregation", Dataset.DatasetType.QUANTITATION, Aggregation.ChildNature.QUANTITATION_FRACTION));
        
        DatasetReferenceNode refDatasetNode = new DatasetReferenceNode(DataSetData.createTemporaryAggregate(m_refDataset == null ? "auto" : m_refDataset.getName()));//new DataSetData(m_refDataset == null ? "auto" : m_refDataset.getName(), Dataset.DatasetType.AGGREGATE, Aggregation.ChildNature.OTHER));
        if(m_refDataset != null){
            Long refResultSummaryId = m_quantitations.get(0).getMasterQuantitationChannels().get(0).getIdentResultSummaryId();
            if(refResultSummaryId == null || !refResultSummaryId.equals(m_refDataset.getResultSummaryId()) )
                refDatasetNode.setInvalidReference(true);
        }
        rootNode.insert(refDatasetNode, childIndex++);
        // group all Biological groups by name
        Map<String, List<BiologicalGroup>> groups = m_quantitations.stream().map(ds -> ds.getGroupSetup().getBiologicalGroups()).flatMap(Collection::stream).collect(Collectors.groupingBy(bg -> bg.getName(), Collectors.toList()));
        int qcIndex = 1; 
        for (String groupName : groups.keySet()) {
            XICBiologicalGroupNode biologicalGroupNode = new XICBiologicalGroupNode(DataSetData.createTemporaryAggregate(groupName)); //new DataSetData(groupName, Dataset.DatasetType.AGGREGATE, Aggregation.ChildNature.OTHER));
            rootNode.insert(biologicalGroupNode, childIndex++);
            
            Map<String, List<BiologicalSample>> samples = groups.get(groupName).stream().map( bg -> bg.getBiologicalSamples()).flatMap(Collection::stream).collect(Collectors.groupingBy(s -> s.getName(), Collectors.toList() ));
            String sampleName = shortenSampleName(groupName, samples.keySet().iterator().next());
            int maxReplicates = 0;
            for (BiologicalGroup bg : groups.get(groupName)) {
                  int replicates = bg.getBiologicalSamples().stream().map(bs -> bs.getQuantitationChannels().size()).max(Comparator.comparing(Integer::valueOf)).get();
                  maxReplicates = Math.max(replicates, maxReplicates);
            }
            XICBiologicalSampleNode biologicalSampleNode = new XICBiologicalSampleNode(DataSetData.createTemporaryAggregate(sampleName)); //new DataSetData(sampleName, Dataset.DatasetType.AGGREGATE, Aggregation.ChildNature.OTHER));
            biologicalGroupNode.insert(biologicalSampleNode, 0);
            for (int i = 0; i < maxReplicates; i++) {
                String name = "Channel " + Integer.toString(qcIndex++);
                DataSetData dsData = DataSetData.createTemporaryIdentification(name); //new DataSetData(name, Dataset.DatasetType.IDENTIFICATION, Aggregation.ChildNature.SAMPLE_ANALYSIS);
                XICBiologicalSampleAnalysisNode sampleAnalysisNode = new XICBiologicalSampleAnalysisNode(dsData);
                sampleAnalysisNode.setQuantChannelName(name);
                biologicalSampleNode.insert(sampleAnalysisNode, biologicalSampleNode.getChildCount());
            }

        }
        
        
        return rootNode;
    }
    
    private String shortenSampleName(String groupName, String sampleName) {
        if (sampleName.startsWith(groupName)) {
                    return sampleName.substring(groupName.length());
        }
        return sampleName;
    }

}

