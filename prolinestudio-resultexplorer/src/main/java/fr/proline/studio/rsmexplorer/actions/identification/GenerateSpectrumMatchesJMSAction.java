/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.GenerateSpectrumMatchTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.GenerateSpectrumMatchesDialog;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;

import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * action to generate spectrum matches via JMS 
 * @author MB243701
 */
public class GenerateSpectrumMatchesJMSAction extends AbstractRSMAction {
    
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");

    public GenerateSpectrumMatchesJMSAction(AbstractTree tree) {
        super(NbBundle.getMessage(GenerateSpectrumMatchesJMSAction.class, "CTL_GenerateSpectrumMatchesAction"), tree);
    }
    
    @Override
    public void actionPerformed(final AbstractNode[] selectedNodes, int x, int y) {
        

        final DefaultTreeModel treeModel = (DefaultTreeModel) getTree().getModel();
        
        int nbNodes = selectedNodes.length;
        List<DDataset> allDSs = new ArrayList();
        //load ds RS and RSM  
        for (int i = 0; i < nbNodes; i++) {
            DDataset ds = ((DataSetNode) selectedNodes[i]).getDataset();
            allDSs.add(ds);
            loadRSData(ds);
        }
                             
        GenerateSpectrumMatchesDialog dialog = new GenerateSpectrumMatchesDialog(WindowManager.getDefault().getMainWindow(),allDSs);
        dialog.setLocation(x, y);
        dialog.setVisible(true);
        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            Long frsId = dialog.getFragmentationRuleSetId();
            Boolean forceGenerate = dialog.getDoForceGenerate();
            for (int i = 0; i < nbNodes; i++) {
                final DataSetNode node = (DataSetNode) selectedNodes[i];
                node.setIsChanging(true);
                treeModel.nodeChanged(node);

                AbstractJMSCallback callback = new AbstractJMSCallback() {

                    @Override
                    public boolean mustBeCalledInAWT() {
                        return true;
                    }

                    @Override
                    public void run(boolean success) {
                        node.setIsChanging(false);
                        treeModel.nodeChanged(node);
                    }
                };

                final DDataset dataset = node.getDataset();
                Long projectId = dataset.getProject().getId();
                Long resultSummaryId = dataset.getResultSummaryId();
                // TODO : if resultSummaryId != null open a dialog to choose between generate spectrum matches for the whole resultSet or only RSM
                Long resultSetId = dataset.getResultSetId();
                if ((resultSetId == null) && dataset.isQuantitation()) { // can happen for XIC
                    // we read more info with resultSetId

                    AbstractDatabaseCallback rsetIdCallback = new AbstractDatabaseCallback() {

                        @Override
                        public boolean mustBeCalledInAWT() {
                            return true;
                        }

                        @Override
                        public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                            if (success) {
                                GenerateSpectrumMatchTask task = new GenerateSpectrumMatchTask(callback, dataset.getName(), projectId, dataset.getResultSetId(), resultSummaryId, null, frsId, forceGenerate);
                                AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
                            }
                        }
                    };

                    DatabaseLoadXicMasterQuantTask task = new DatabaseLoadXicMasterQuantTask(rsetIdCallback);
                    task.initLoadQuantChannels(dataset.getProject().getId(), dataset);
                    AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

                } else {
                    GenerateSpectrumMatchTask task = new GenerateSpectrumMatchTask(callback, dataset.getName(), projectId, resultSetId, resultSummaryId, null, frsId, forceGenerate);
                    AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
                }
            }
        }
    }

    private void loadRSData(DDataset ds){
        DatabaseDataSetTask task = new DatabaseDataSetTask(null);
        task.initLoadRsetAndRsm(ds);
        task.fetchData();
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
        for (int i = 0; i < nbSelectedNodes; i++) {
            AbstractNode node = selectedNodes[i];

            if (node.isChanging()) {
                setEnabled(false);
                return;
            }

            if (node.getType() != AbstractNode.NodeTypes.DATA_SET) {
                setEnabled(false);
                return;
            }
            
            DataSetNode dataSetNode = (DataSetNode) node;

            if (dataSetNode.isFolder()) {
                setEnabled(false);
                return;
            }

            if (! dataSetNode.hasResultSet()) {
                setEnabled(false);
                return;
            }


        }
        setEnabled(true);
    }
    
}
