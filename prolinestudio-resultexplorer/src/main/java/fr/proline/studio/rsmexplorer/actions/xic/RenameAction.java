/* 
 * Copyright (C) 2019
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
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.OptionDialog;
import fr.proline.studio.rsmexplorer.actions.identification.AbstractRSMAction;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.xic.XICBiologicalSampleAnalysisNode;

import javax.swing.tree.DefaultTreeModel;

import fr.proline.studio.WindowManager;

/**
 * Rename a Node in a Xic QuantExperimentalDesignTree
 * @author JM235353
 */
public class RenameAction extends AbstractRSMAction {


    public RenameAction(AbstractTree tree) {
        super("Rename...", tree);
    }

    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {

        // only one node selected for this action
        final AbstractNode n = selectedNodes[0];

        AbstractNode.NodeTypes nodeType = n.getType();

        switch (nodeType) {
            case BIOLOGICAL_GROUP:
            case BIOLOGICAL_SAMPLE:
            case DATA_SET:
                DataSetData data = ((DataSetData) n.getData());

                String name = data.getName();
                String newName = showRenameDialog(name, x, y);

                if (newName == null) {
                    return;
                }

                if (name.compareTo(newName) != 0) {
                    data.setTemporaryName(newName);
                    ((DefaultTreeModel) getTree().getModel()).nodeChanged(n);
                }
                break;
            case BIOLOGICAL_SAMPLE_ANALYSIS:
                XICBiologicalSampleAnalysisNode bioSplAnaysisNode = ((XICBiologicalSampleAnalysisNode) n);
                String qChName = bioSplAnaysisNode.getQuantChannelName();
                String qChNewName = showRenameDialog(qChName, x, y);

                if (qChNewName == null) {
                    return;
                }

                if (qChName.compareTo(qChNewName) != 0) {
                    bioSplAnaysisNode.setQuantChannelName(qChNewName);
                    ((DefaultTreeModel) getTree().getModel()).nodeChanged(n);
                }
            default:
                break;
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
        if ((nodeType != AbstractNode.NodeTypes.BIOLOGICAL_GROUP)
                && (nodeType != AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE)
                && (nodeType != AbstractNode.NodeTypes.DATA_SET)
                && (nodeType != AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS)) {
            setEnabled(false);
            return;
        }

        setEnabled(true);

    }

}
