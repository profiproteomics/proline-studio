/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions.xic;

import fr.proline.core.orm.uds.Aggregation;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.OptionDialog;
import fr.proline.studio.rsmexplorer.actions.identification.AbstractRSMAction;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.xic.XICBiologicalGroupNode;
import fr.proline.studio.rsmexplorer.tree.xic.XICBiologicalSampleNode;
import fr.proline.studio.rsmexplorer.tree.xic.XICDesignTree;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author AK249877
 */
public class CreateSampleNodeAction extends AbstractRSMAction {

    private final XICDesignTree m_tree;

    public CreateSampleNodeAction(XICDesignTree tree) {
        super(NbBundle.getMessage(CreateSampleNodeAction.class, "CTL_CreateSampleAction"), AbstractTree.TreeType.TREE_XIC_DESIGN, tree);
        m_tree = tree;
    }

    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {
        setEnabled(selectedNodes.length == 1 && (selectedNodes[0].getType() == AbstractNode.NodeTypes.BIOLOGICAL_GROUP || selectedNodes[0].getType() == AbstractNode.NodeTypes.DATA_SET));
    }

    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {

        final AbstractNode node = selectedNodes[0];

        if (node.getType() == AbstractNode.NodeTypes.BIOLOGICAL_GROUP) {

            String sampleName = showGroupNameDialog("Sample1", x, y);

            if (sampleName == null) {
                return;
            }

            XICBiologicalSampleNode biologicalSampleNode = new XICBiologicalSampleNode(DataSetData.createTemporaryAggregate(sampleName)); //new DataSetData(sampleName, Dataset.DatasetType.AGGREGATE, Aggregation.ChildNature.OTHER));
            DefaultTreeModel treeModel = (DefaultTreeModel) m_tree.getModel();
            treeModel.insertNodeInto(biologicalSampleNode, node, 0);
            m_tree.expandNodeIfNeeded(node);
        } else if (node.getType() == AbstractNode.NodeTypes.DATA_SET) {
            String sampleName = showGroupNameDialog("New Sample", x, y);

            if (sampleName == null) {
                return;
            }
            
            XICBiologicalGroupNode biologicalGroupNode = new XICBiologicalGroupNode(DataSetData.createTemporaryAggregate("New Group")); //new DataSetData("New Group", Dataset.DatasetType.AGGREGATE, Aggregation.ChildNature.OTHER));
            XICBiologicalSampleNode biologicalSampleNode = new XICBiologicalSampleNode(DataSetData.createTemporaryAggregate(sampleName)); //new DataSetData(sampleName, Dataset.DatasetType.AGGREGATE, Aggregation.ChildNature.OTHER));
            
            DefaultTreeModel treeModel = (DefaultTreeModel) m_tree.getModel();
            
            treeModel.insertNodeInto(biologicalSampleNode, biologicalGroupNode, 0);            
            treeModel.insertNodeInto(biologicalGroupNode, node, 0);
            
            m_tree.expandNodeIfNeeded(biologicalGroupNode);
            
            m_tree.expandNodeIfNeeded(node);
            
        } else {
            return;
        }

    }

    private String showGroupNameDialog(String defaultName, int x, int y) {

        OptionDialog dialog = new OptionDialog(WindowManager.getDefault().getMainWindow(), "Name", null, "New Sample's Name", OptionDialog.OptionDialogType.TEXTFIELD);
        dialog.setText(defaultName);
        dialog.setLocation(x, y);
        dialog.setVisible(true);
        String newName = null;
        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            newName = dialog.getText();
        }

        if ((newName != null) && (newName.length() > 0)) {
            return newName;
        }

        return null;
    }

}
