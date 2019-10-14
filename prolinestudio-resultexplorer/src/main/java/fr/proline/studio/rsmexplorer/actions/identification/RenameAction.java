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
package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.Project;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.OptionDialog;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdProjectIdentificationNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * Action to Rename a dataset
 * @author JM235353
 */
public class RenameAction extends AbstractRSMAction {

    /**
     * Builds the RenameAction depending of the treeType
     */
    public RenameAction(AbstractTree tree) {
        super(NbBundle.getMessage(RenameAction.class, "CTL_RenameAction"), tree);
    }

    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {

        // only one node selected for this action
        final AbstractNode n = selectedNodes[0];

        
        AbstractNode.NodeTypes nodeType = n.getType();
        if (nodeType == AbstractNode.NodeTypes.DATA_SET) {
            DataSetNode datasetNode = (DataSetNode) n;
            DDataset dataset = datasetNode.getDataset();
            
            String name = dataset.getName();
            String newName = showRenameDialog(name, x, y);

            datasetNode.rename(newName, getTree());
            
        } else if (nodeType == AbstractNode.NodeTypes.PROJECT_IDENTIFICATION) {
            IdProjectIdentificationNode projectNode = (IdProjectIdentificationNode) n;
            Project project = projectNode.getProject();
            
            String name = project.getName();
            String newName = showRenameDialog(name,  x, y);

            projectNode.changeNameAndDescription(newName, project.getDescription());

        }


    }
        
    private String showRenameDialog(String name, int x, int y) {
        
        OptionDialog dialog = new OptionDialog(WindowManager.getDefault().getMainWindow(), "Rename", null, "New Name", OptionDialog.OptionDialogType.TEXTFIELD);
        dialog.setText(name);
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
    
    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {

        // to execute this action, the user must be the owner of the project
        Project selectedProject = ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject();
        if (!DatabaseDataManager.getDatabaseDataManager().ownProject(selectedProject)) {
            setEnabled(false);
            return;
        }
        
        int nbSelectedNodes = selectedNodes.length;

        // we rename multiple nodes
        if (nbSelectedNodes != 1) {
            setEnabled(false);
            return;
        }

        AbstractNode node = selectedNodes[0];
        AbstractNode.NodeTypes nodeType = node.getType();
        if ((nodeType != AbstractNode.NodeTypes.DATA_SET) && (nodeType != AbstractNode.NodeTypes.PROJECT_IDENTIFICATION )) {
            setEnabled(false);
            return;
        }

        setEnabled(true);

    }
    
    


}