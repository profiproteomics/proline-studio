package fr.proline.studio.rsmexplorer.actions;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.ChangeTypicalProteinTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.gui.dialog.ChangeTypicalProteinDialog;
import fr.proline.studio.rsmexplorer.node.RSMDataSetNode;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.IdentificationTree;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import fr.proline.core.orm.msi.ResultSummary;

/**
 *
 * @author JM235353
 */
public class ChangeTypicalProteinAction extends AbstractRSMAction {

    
    public ChangeTypicalProteinAction() {
        super(NbBundle.getMessage(ValidateAction.class, "CTL_ChangeTypicalProtein"));
    }

    @Override
    public void actionPerformed(RSMNode[] selectedNodes, int x, int y) {

        

        ChangeTypicalProteinDialog dialog = ChangeTypicalProteinDialog.getDialog(WindowManager.getDefault().getMainWindow());
        dialog.setLocation(x, y);
        dialog.setVisible(true);


        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {

            String regex = dialog.getRegex();
            boolean regexOnAccession = dialog.regexOnAccession();

            IdentificationTree tree = IdentificationTree.getCurrentTree();
            DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();

            // start validation for each selected Dataset
            int nbNodes = selectedNodes.length;
            for (int i = 0; i < nbNodes; i++) {
                final RSMDataSetNode dataSetNode = (RSMDataSetNode) selectedNodes[i];

                dataSetNode.setIsChanging(true);
                treeModel.nodeChanged(dataSetNode);

                final DDataset d = dataSetNode.getDataset();


                
                
                AbstractServiceCallback callback = new AbstractServiceCallback() {

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


                ChangeTypicalProteinTask task = new ChangeTypicalProteinTask(callback, d, regex, regexOnAccession);
                AccessServiceThread.getAccessServiceThread().addTask(task);



            }


        }
    }

    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {

        // note : we can ask for the validation of multiple ResultSet in one time

        int nbSelectedNodes = selectedNodes.length;
        for (int i = 0; i < nbSelectedNodes; i++) {
            RSMNode node = selectedNodes[i];

            // parent node is being created, we can not validate it (for the moment)
            if (node.isChanging()) {
                setEnabled(false);
                return;
            }

            // parent node must be a dataset
            if (node.getType() != RSMNode.NodeTypes.DATA_SET) {
                setEnabled(false);
                return;
            }

            // parent node must have a ResultSet
            RSMDataSetNode dataSetNode = (RSMDataSetNode) node;
            if (!dataSetNode.hasResultSummary()) {
                setEnabled(false);
                return;
            }
        }

        setEnabled(true);

    }
    
}
