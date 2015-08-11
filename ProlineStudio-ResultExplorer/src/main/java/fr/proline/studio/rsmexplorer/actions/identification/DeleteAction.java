package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.uds.Project;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.quantitation.QuantitationTree;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import org.openide.util.NbBundle;

/**
 * Delete a dataset Action
 * @author JM235353
 */
public class DeleteAction extends AbstractRSMAction {

    private AbstractTree.TreeType m_treeType;
    
    public DeleteAction(AbstractTree.TreeType treeType) {
        super(NbBundle.getMessage(DeleteAction.class, "CTL_DeleteAction"), treeType);
        this.m_treeType = treeType;
    }

    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) { 
        // depends on the treeType
        if (m_treeType == AbstractTree.TreeType.TREE_IDENTIFICATION ){
            IdentificationTree tree = IdentificationTree.getCurrentTree();
            tree.moveToTrash(selectedNodes);
        }else if (m_treeType == AbstractTree.TreeType.TREE_QUANTITATION ){
             QuantitationTree tree = QuantitationTree.getCurrentTree();
             tree.moveToTrash(selectedNodes);
        }
    }
    
    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {

        // to execute this action, the user must be the owner of the project
        Project selectedProject = ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject();
        if (!DatabaseDataManager.getDatabaseDataManager().ownProject(selectedProject)) {
            setEnabled(false);
            return;
        }
        
        // look if all the nodes can be separately deleted and 
        // are in the same project
        
        long commonProjectId = -1;
        int nbSelectedNodes = selectedNodes.length;
        HashSet<AbstractNode> selectedNodesHashSet = new HashSet<>();
        for (int i=0;i<nbSelectedNodes;i++) {
            AbstractNode node = selectedNodes[i];
            
            if (! node.canBeDeleted()) {
                setEnabled(false);
                return;
            }
            
            if (selectedNodes[i] instanceof DataSetNode) {
                DataSetNode datasetNode = (DataSetNode) selectedNodes[i];
                long projectId = datasetNode.getDataset().getProject().getId();
                if (commonProjectId == -1) {
                    commonProjectId = projectId;
                } else if (commonProjectId != projectId) {
                    setEnabled(false);
                    return;
                }
                selectedNodesHashSet.add(datasetNode);
                addChildren(datasetNode, selectedNodesHashSet);
            }else{
                // no datasetNode (can be also BiologicalSampleNode or BiologicalGroupNode) 
                setEnabled(false);
                return;
            }
            
            
        }
        
        // Check if one selected node is a child of a non selected node with merged results
        // in this case, it can not be suppressed alone
        Iterator<AbstractNode> it = selectedNodesHashSet.iterator();
        while (it.hasNext()) {
            AbstractNode node = it.next();
            AbstractNode parent = (AbstractNode) node.getParent();
            if (parent instanceof DataSetNode) {
                
                DataSetNode parentDataset = (DataSetNode) parent;
                if (!selectedNodesHashSet.contains(parentDataset)) {
                    if (parentDataset.hasResultSet() || parentDataset.hasResultSummary()) {
                        // we have a merged parent dataset which is not selected to be deleted, so its son can not be deleted
                        setEnabled(false);
                        return;
                    }
                }
            }
        }
        
        
        setEnabled(true);

    }
    
    private void addChildren(AbstractNode parentNode, HashSet<AbstractNode> nodesHashSet) {
        Enumeration e = parentNode.children();
        while (e.hasMoreElements()) {
            AbstractNode child = (AbstractNode) e.nextElement();
            nodesHashSet.add(child);
            addChildren(child, nodesHashSet);
        }
    }
}