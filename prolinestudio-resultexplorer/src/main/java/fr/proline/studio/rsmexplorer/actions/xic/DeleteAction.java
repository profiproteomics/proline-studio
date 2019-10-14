/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.rsmexplorer.actions.xic;

import fr.proline.core.orm.uds.Project;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.rsmexplorer.actions.identification.AbstractRSMAction;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import java.util.ArrayList;
import org.openide.util.NbBundle;

/**
 * Delete a Node in a Xic QuantExperimentalDesignTree
 * @author JM235353
 */
public class DeleteAction  extends AbstractRSMAction {
    

    public DeleteAction(AbstractTree tree) {
        super(NbBundle.getMessage(DeleteAction.class, "CTL_DeleteAction"), tree);
    }

    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {
        
        int nbSelectedNode = selectedNodes.length;
        if (nbSelectedNode == 0) {
            return;
        }

        AbstractTree.RSMTreeModel model = (AbstractTree.RSMTreeModel) getTree().getModel();
        
        // we must keep only parent nodes
        // if a child and its parent are selected, we keep only the parent
        ArrayList<AbstractNode> keptNodes = new ArrayList<>(nbSelectedNode);
        keptNodes.add(selectedNodes[0]);
        mainloop:
        for (int i = 1; i < nbSelectedNode; i++) {
            AbstractNode curNode = selectedNodes[i];

            // look for an ancestor
            int nbKeptNodes = keptNodes.size();
            for (int j = 0; j < nbKeptNodes; j++) {

                AbstractNode curKeptNode = keptNodes.get(j);
                if (curNode.isNodeAncestor(curKeptNode)) {
                    // ancestor is already in kept node
                    continue mainloop;
                }
            }
            // look for children and remove them
            for (int j = nbKeptNodes - 1; j >= 0; j--) {

                AbstractNode curKeptNode = keptNodes.get(j);
                if (curKeptNode.isNodeAncestor(curNode)) {
                    // we have found a children
                    keptNodes.remove(j);
                }
            }
            keptNodes.add(curNode);

        }


        int nbKeptNodes = keptNodes.size();
        for (int i=0;i<nbKeptNodes;i++) {
            AbstractNode nodeCur = keptNodes.get(i);
            model.removeNodeFromParent(nodeCur);
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
        
        int nbSelectedNode = selectedNodes.length;
        for (int i = 0; i < nbSelectedNode; i++) {
            AbstractNode node = selectedNodes[i];

            AbstractNode.NodeTypes type = node.getType();

            if ((type != AbstractNode.NodeTypes.BIOLOGICAL_GROUP) && (type != AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS) && (type != AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE)) {
                setEnabled(false);
                return;
            }

        }
        
        setEnabled(true);

    }
}
