package fr.proline.studio.rsmexplorer.tree.xic;

import fr.proline.core.orm.uds.Aggregation;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.parameter.BooleanParameter;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import javax.swing.JCheckBox;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.openide.util.NbPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Management of the drag and drop from the IdentificationSelectionTree to the XIC
 XICDesignTree
 *
 * @author JM235353
 */
public class XICTransferHandler extends AbstractTreeTransferHandler {

    private Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private ParameterList m_parameterList;
    private BooleanParameter m_parameter;
    private static final String GENERAL_APPLICATION_SETTINGS = "General Application Settings";
    private AbstractTree m_tree;

    public XICTransferHandler(boolean isSelectionTree, AbstractTree tree) {
        super(isSelectionTree);
        m_tree = tree;
        m_parameterList = new ParameterList(GENERAL_APPLICATION_SETTINGS);
        JCheckBox checkBox = new JCheckBox("Use dataset type to create Xic Design by DnD");
        m_parameter = new BooleanParameter("XIC_Transfer_Handler_Retains_Structure", "XIC Transfer Handler Retains Structure", checkBox, true);
        m_parameterList.add(m_parameter);
        m_parameterList.loadParameters(NbPreferences.root());
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {

        if (!m_isIdentificationSelectionTree) {

            support.setShowDropLocation(true);

            if (support.isDataFlavorSupported(XICSelectionTransferable.RSMNodeList_FLAVOR)) {

                // drop path
                TreePath dropTreePath = ((JTree.DropLocation) support.getDropLocation()).getPath();
                if (dropTreePath == null) {
                    // should not happen
                    return false;
                }

                boolean designData;
                AbstractNode.NodeTypes designNodeType = null;
                try {
                    XICSelectionTransferable transfer = (XICSelectionTransferable) support.getTransferable().getTransferData(XICSelectionTransferable.RSMNodeList_FLAVOR);
                    XICSelectionTransferable.TransferData data = XICSelectionTransferable.getData(transfer.getTransferKey());
                    designData = data.isDesignData();
                    if (designData) {
                        designNodeType = data.getDesignNodeType();
                    }
                } catch (UnsupportedFlavorException | IOException e) {
                    // should never happen
                    m_logger.error(getClass().getSimpleName() + " DnD error ", e);
                    return false;
                }

                // Determine whether we accept the location
                Object dropComponent = dropTreePath.getLastPathComponent();
                if (designData) {
                    if (!(dropComponent instanceof AbstractNode)) {
                        return false;
                    }

                    AbstractNode.NodeTypes dropType = ((AbstractNode) dropComponent).getType();
                    switch (dropType) {
                        case DATA_SET:
                            return (designNodeType == AbstractNode.NodeTypes.BIOLOGICAL_GROUP);
                        case BIOLOGICAL_GROUP:
                            return (designNodeType == AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE);
                        case BIOLOGICAL_SAMPLE:
                            return (designNodeType == AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS);
                        default:
                            return false;

                    }

                } else {

                    if ((dropComponent instanceof XICBiologicalSampleNode) || // Sample Node
                            (dropComponent instanceof DataSetNode) || // XIC Node
                            (dropComponent instanceof XICBiologicalGroupNode)) {    // Group Node
                        return true;
                    }

                }
            }
        }

        return false;
    }

    @Override
    public boolean importData(TransferSupport support) {

        if (canImport(support)) {

            try {
                XICSelectionTransferable transfer = (XICSelectionTransferable) support.getTransferable().getTransferData(XICSelectionTransferable.RSMNodeList_FLAVOR);
                XICSelectionTransferable.TransferData data = XICSelectionTransferable.getData(transfer.getTransferKey());
                return importNodes(support, data);

            } catch (UnsupportedFlavorException | IOException e) {
                // should never happen
                m_logger.error(getClass().getSimpleName() + " DnD error ", e);
                return false;
            }

        }

        return false;
    }

    private boolean importNodes(TransferSupport support, XICSelectionTransferable.TransferData data) {

        JTree.DropLocation location = ((JTree.DropLocation) support.getDropLocation());
        TreePath dropTreePath = location.getPath();
        int childIndex = location.getChildIndex();
        AbstractNode dropRSMNode = (AbstractNode) dropTreePath.getLastPathComponent();
        
        DefaultTreeModel treeModel = (DefaultTreeModel) m_tree.getModel();

        // no insert index specified -> we insert at the end
        if (childIndex == -1) {
            childIndex = dropRSMNode.getChildCount();
        }

        ArrayList<DataSetNode> datasetList = (ArrayList<DataSetNode>) data.getDatasetList();
        if (datasetList != null) {

            m_tree.expandNodeIfNeeded(dropRSMNode);

            String suffix = Integer.toString(dropRSMNode.getChildCount() + 1);

            // Issue 11312: if the dragged node is a merged node, we use its name as suffix
            if (!datasetList.isEmpty()) {
                
                // all dataset are in the same merged dataset parent
                DDataset parentNode = datasetList.get(0).getParentMergedDataset();
                AbstractNode ancestorNode = datasetList.get(0).getLowestAncestor();
                
                if (parentNode != null) {
                    
                    int nb = datasetList.size();
                    
                    boolean sameParent = true;
                    boolean sameAncestor = true;
                    
                    for (int i = 1; i < nb; i++) {
                        
                        AbstractNode a = datasetList.get(i).getLowestAncestor();
                        
                        DDataset p = datasetList.get(i).getParentMergedDataset();

                        if (p != null && p.getId() != parentNode.getId()) {
                            sameParent = false;
                        }
                        
                        if(a == null || a != ancestorNode){
                            sameAncestor = false;
                        }
                        
                    }
                    if (sameParent) {
                        suffix = parentNode.getName();
                    }else if(sameAncestor){
                        suffix = ancestorNode.toString();
                    }                    
                }
            } //END datasetList NOT Empty Issue 11312:

            if (dropRSMNode instanceof DataSetNode) {
                // top node, we create a group now
                
                String groupName = "Group " + suffix;
                XICBiologicalGroupNode biologicalGroupNode = new XICBiologicalGroupNode(new DataSetData(groupName, Dataset.DatasetType.AGGREGATE, Aggregation.ChildNature.OTHER));
                treeModel.insertNodeInto(biologicalGroupNode, dropRSMNode, childIndex);
                
                childIndex = 0;
                
                dropRSMNode = biologicalGroupNode;
                m_tree.expandNodeIfNeeded(dropRSMNode);
            }

            m_parameterList.loadParameters(NbPreferences.root());
            boolean retainStructure = (boolean) m_parameter.getObjectValue();

            if (retainStructure) {

                //<--------------------------------------------------------------------------------->
                //Here I must intervene so that the mechanism changes!
                if (dropRSMNode instanceof XICBiologicalGroupNode) {

                    //Here I divide Leafs into teams depending on their father (Luke I am your father!)
                    Hashtable<String, ArrayList<DataSetNode>> samplesHashtable = new Hashtable<String, ArrayList<DataSetNode>>();

                    LinkedHashSet<String> order = new LinkedHashSet<String>();

                    for (int i = 0; i < datasetList.size(); i++) {

                        DataSetNode node = datasetList.get(i);

                        String currentParent = (node.getParent() == null) ? "null" : node.getParent().toString();

                        order.add(currentParent);

                        if (samplesHashtable.containsKey(currentParent)) {
                            samplesHashtable.get(currentParent).add(node);
                        } else {
                            ArrayList<DataSetNode> newSample = new ArrayList<DataSetNode>();
                            newSample.add(node);
                            samplesHashtable.put(currentParent, newSample);
                        }

                    }
                    
                    

                    Iterator iter = order.iterator();
                    while (iter.hasNext()) {
                        String key = (String) iter.next();             

                        ArrayList<DataSetNode> currentSampleList = samplesHashtable.get(key);

                        suffix = (key.contains("Identifications")) ? String.valueOf(dropRSMNode.getChildCount() + 1) : key;

                        String sampleName = "Sample " + suffix;

                        XICBiologicalSampleNode biologicalSampleNode = new XICBiologicalSampleNode(new DataSetData(sampleName, Dataset.DatasetType.AGGREGATE, Aggregation.ChildNature.OTHER));
                        treeModel.insertNodeInto(biologicalSampleNode, dropRSMNode, childIndex);

                        childIndex = 0;
                        m_tree.expandNodeIfNeeded(biologicalSampleNode);

                        for (int i = 0; i < currentSampleList.size(); i++) {

                            // create the new node
                            XICBiologicalSampleAnalysisNode sampleAnalysisNode = new XICBiologicalSampleAnalysisNode(currentSampleList.get(i).getData());

                            // add to new parent
                            treeModel.insertNodeInto(sampleAnalysisNode, biologicalSampleNode, childIndex);
                            childIndex++;

                        }

                        childIndex -= currentSampleList.size()-1;

                    }
                } else if (dropRSMNode instanceof XICBiologicalSampleNode) {

                    for (int i = 0; i < datasetList.size(); i++) {

                        // create the new node
                        XICBiologicalSampleAnalysisNode sampleAnalysisNode = new XICBiologicalSampleAnalysisNode(datasetList.get(i).getData());

                        // add to new parent
                        treeModel.insertNodeInto(sampleAnalysisNode, dropRSMNode, childIndex);
                        childIndex++;

                    }
                }

                //<--------------------------------------------------------------------------------->        
            } else {

                if (dropRSMNode instanceof XICBiologicalGroupNode) {
                    // Group Node, we create a sample node
                    String sampleName = "Sample " + suffix;
                    XICBiologicalSampleNode biologicalSampleNode = new XICBiologicalSampleNode(new DataSetData(sampleName, Dataset.DatasetType.AGGREGATE, Aggregation.ChildNature.OTHER));
                    treeModel.insertNodeInto(biologicalSampleNode, dropRSMNode, childIndex);
                    dropRSMNode = biologicalSampleNode;
                    childIndex = 0;
                    m_tree.expandNodeIfNeeded(dropRSMNode);
                }

                if (dropRSMNode instanceof XICBiologicalSampleNode) {
                    int nbNodes = datasetList.size();
                    for (int i = 0; i < nbNodes; i++) {
                        DataSetNode node = datasetList.get(i);

                        // create the new node
                        XICBiologicalSampleAnalysisNode sampleAnalysisNode = new XICBiologicalSampleAnalysisNode(node.getData());

                        // add to new parent
                        treeModel.insertNodeInto(sampleAnalysisNode, dropRSMNode, childIndex);

                        childIndex++;
                    }
                }

            }
        } else { //datalist == null  ==> isDesignData
            ArrayList<AbstractNode> rsmList = (ArrayList<AbstractNode>) data.getDesignList();
            int nbNodes = rsmList.size();
            for (int i = 0; i < nbNodes; i++) {
                AbstractNode node = rsmList.get(i);

                // specific case when the node is moved in its parent
                int indexChild;
                if (dropRSMNode.isNodeChild(node)) {
                    // we are moving the node in its parent
                    indexChild = dropRSMNode.getIndex(node);
                    if (indexChild < childIndex) {
                        childIndex--;
                    }
                }

                // remove from parent (required when drag and dropped in the same parent)
                treeModel.removeNodeFromParent(node);

                // add to new parent
                treeModel.insertNodeInto(node, dropRSMNode, childIndex);

                childIndex++;

            }
        }
        return true;

    }

}
