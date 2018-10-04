/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.core.orm.uds.Aggregation;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DMasterQuantitationChannel;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.xic.XICDesignTree;
import java.awt.Dialog;
import java.awt.Window;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
public class AggregateQuantitationDialog extends DefaultDialog {

    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private static AggregateQuantitationDialog m_singletonDialog = null;

    public static AggregateQuantitationDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new AggregateQuantitationDialog(parent);
        }
        return m_singletonDialog;
    }

    private XICDesignTree m_designTree = null;
    private AbstractNode m_experimentalDesignNode = null;
    private List<DDataset> m_quantitations;
    
    private AggregateQuantitationDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
        setTitle("Aggregate Quantitation Wizard");
        setDocumentationSuffix("id.2dlolyb");
        setSize(1000, 768);
        setResizable(true);
    }

    public Map<String, Object> getExperimentalDesignParameters() throws IllegalAccessException {
        if (m_experimentalDesignNode == null) {
            throw new IllegalAccessException("Design parameters have not been set.");
        }
        DDataset dataset = m_quantitations.get(0);
        DDataset refDataset = dataset.getMasterQuantitationChannels().get(0).getIdentDataset();
        Long refRsmId = dataset.getMasterQuantitationChannels().get(0).getIdentResultSummaryId();
        Map<String, Object> experimentalDesignParams = XICDesignTree.toExperimentalDesignParameters(m_experimentalDesignNode, refDataset,refRsmId);
        return experimentalDesignParams;
    }

    public Map<String, Object> getQuantiParameters() {
        Map<Long, List<Long>> qcsByMasterqc = new HashMap<>();
        for (DDataset dataset : m_quantitations) {
            DMasterQuantitationChannel childMQC = dataset.getMasterQuantitationChannels().get(0);
            List<Long> qcList = childMQC.getQuantitationChannels().stream().map(qc -> qc.getId()).collect(Collectors.toList());
            qcsByMasterqc.put(childMQC.getId(), qcList);
        }
        Map<String, Object> aggregationConfig = new HashMap<>();
        aggregationConfig.put("quantitation_ids", m_quantitations.stream().map(d -> d.getId()).toArray());
        List<Map<String, Object>> mappingList = new ArrayList<>();
        int index = 0;
        for (DQuantitationChannel qc : m_quantitations.get(0).getMasterQuantitationChannels().get(0).getQuantitationChannels()) {
            final int _index = index;
            Map<String, Object> mapping = new HashMap<>();
            mapping.put("quant_channel_number", qc.getNumber());
            Map<Long, Long> map = qcsByMasterqc.entrySet().stream().collect(Collectors.toMap(
              entry -> entry.getKey(),
              entry -> entry.getValue().get(_index)
            ));
            index++;
            mapping.put("mapped_quant_channel_id_by_master_quant_channel_id", map);
            mappingList.add(mapping);
        }
        aggregationConfig.put("quant_channels_mapping", mappingList);
        
        return aggregationConfig;
    }

    public AbstractNode getExperimentalDesignNode() {
        return m_experimentalDesignNode;
    }

    public void setQuantitationDatasets(List<DDataset> loadedQuantitations) {
        m_quantitations = loadedQuantitations;
        displayDesignTree();
    }

    public void displayDesignTree() {
        if (m_quantitations != null && m_quantitations.size() > 0) {
            m_experimentalDesignNode = new DataSetNode(new DataSetData("XIC", Dataset.DatasetType.QUANTITATION, Aggregation.ChildNature.QUANTITATION_FRACTION));
            m_designTree = new XICDesignTree(m_experimentalDesignNode, true);
            DDataset dataset = m_quantitations.get(0);
            XICDesignTree.displayExperimentalDesign(dataset, (AbstractNode) m_designTree.getModel().getRoot(), m_designTree, false, false, true);
            m_designTree.renameXicTitle(dataset.getName() + "-Agg");
            replaceInternalComponent(m_designTree);
            revalidate();
            repaint();
        }
    }

}
