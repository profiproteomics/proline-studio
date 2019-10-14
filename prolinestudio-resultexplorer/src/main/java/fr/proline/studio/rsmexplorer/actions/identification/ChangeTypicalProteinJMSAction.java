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
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.gui.dialog.ChangeTypicalProteinDialog;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.uds.Project;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dpm.data.ChangeTypicalRule;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.ChangeTypicalProteinTask;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import java.util.List;

/**
 *
 * @author JM235353
 */
public class ChangeTypicalProteinJMSAction extends AbstractRSMAction {

    
    public ChangeTypicalProteinJMSAction(AbstractTree tree) {
        super(NbBundle.getMessage(ChangeTypicalProteinJMSAction.class, "CTL_ChangeTypicalProtein"), tree);
    }

    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {

        

        ChangeTypicalProteinDialog dialog = ChangeTypicalProteinDialog.getDialog(WindowManager.getDefault().getMainWindow());
        dialog.setLocation(x, y);
        dialog.setVisible(true);


        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {

            List<ChangeTypicalRule> rules = dialog.getChangeTypicalRules();

            IdentificationTree tree = IdentificationTree.getCurrentTree();
            DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();

            // start validation for each selected Dataset
            int nbNodes = selectedNodes.length;
            for (int i = 0; i < nbNodes; i++) {
                final DataSetNode dataSetNode = (DataSetNode) selectedNodes[i];

                dataSetNode.setIsChanging(true);
                treeModel.nodeChanged(dataSetNode);

                final DDataset d = dataSetNode.getDataset();
                
                AbstractJMSCallback callback = new AbstractJMSCallback() {

                    @Override
                    public boolean mustBeCalledInAWT() {
                        return true;
                    }

                    @Override
                    public void run(boolean success) {

                        // reinitialize already loaded data.
                        // Protein Sets will have to be reloaded
                        ResultSummary rsm = d.getResultSummary();
                        if (rsm != null) {
                            rsm.getTransientData().setProteinSetArray(null);
                        }
                        
                        dataSetNode.setIsChanging(false);

                        IdentificationTree tree = IdentificationTree.getCurrentTree();
                        DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
                        treeModel.nodeChanged(dataSetNode);
                    }
                };


                ChangeTypicalProteinTask task = new ChangeTypicalProteinTask(callback, d, rules);
                AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);

            }


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
        
        // note : we can ask for the validation of multiple ResultSet in one time

        int nbSelectedNodes = selectedNodes.length;
        for (int i = 0; i < nbSelectedNodes; i++) {
            AbstractNode node = selectedNodes[i];

            // parent node is being created, we can not validate it (for the moment)
            if (node.isChanging()) {
                setEnabled(false);
                return;
            }

            // parent node must be a dataset
            if (node.getType() != AbstractNode.NodeTypes.DATA_SET) {
                setEnabled(false);
                return;
            }

            // parent node must have a ResultSet
            DataSetNode dataSetNode = (DataSetNode) node;
            if (!dataSetNode.hasResultSummary()) {
                setEnabled(false);
                return;
            }
        }

        setEnabled(true);

    }
    
}
