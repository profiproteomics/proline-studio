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

import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.QuantitationMethod;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DDatasetType;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.RunQuantitationTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.CreateQuantitationDialog;
import fr.proline.studio.rsmexplorer.tree.quantitation.QuantitationTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.ChildFactory;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import java.util.*;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author JM235353
 */
public class CreateQuantitationAction extends AbstractRSMAction {

    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");

    private QuantitationMethod.Type m_quantitationType;
    private boolean m_isFromExistingQuantitation = false;
    private boolean m_referenceDataSetDefined = false;

    static String getMessage(boolean fromExistingXIC, QuantitationMethod.Type type) {
        
        switch (type) {
            case LABEL_FREE: {
                if (fromExistingXIC) {
                  return NbBundle.getMessage(CreateQuantitationAction.class, "CTL_CreateXIC_Clone");
                } else {
                  return NbBundle.getMessage(CreateQuantitationAction.class, "CTL_CreateXIC");
                } 
            }
            case RESIDUE_LABELING: return NbBundle.getMessage(CreateQuantitationAction.class, "CTL_CreateResidueLabeling");
            case ISOBARIC_TAGGING: return NbBundle.getMessage(CreateQuantitationAction.class, "CTL_CreateIsobaricLabeling");
            
        }
       return "unknown";
    }

    public CreateQuantitationAction(AbstractTree tree, boolean fromExistingXIC) {
        super(CreateQuantitationAction.getMessage(fromExistingXIC, QuantitationMethod.Type.LABEL_FREE), tree);
        // warn : at this time cloning is only supported for LABEL FREE quantitations
        m_quantitationType = QuantitationMethod.Type.LABEL_FREE;
        m_isFromExistingQuantitation = fromExistingXIC;
    }

    public CreateQuantitationAction(AbstractTree tree, QuantitationMethod.Type type) {
        super(CreateQuantitationAction.getMessage(false, type), tree);
        m_quantitationType = type; 
        m_isFromExistingQuantitation = false;
    }

    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {

        if (ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject() == null) {
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), "A project should be selected !", "Warning", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (m_isFromExistingQuantitation) {
            final DDataset currentDataset = ((DataSetData) ((DataSetNode) selectedNodes[0]).getData()).getDataset();
            final int posx = x;
            final int posy = y;
            
            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                    createQuantitationDialog(currentDataset, posx, posy);
                }
            };
            DatabaseLoadXicMasterQuantTask task = new DatabaseLoadXicMasterQuantTask(callback);
            task.initLoadQuantChannels(ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject().getId(), currentDataset);
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

        } else {
            AbstractNode n = selectedNodes[0];
            DDataset refDataset = null;
            if (!n.isRoot() && DataSetNode.class.isInstance(n)) {
                DataSetNode node = (DataSetNode) n;
                DataSetData dataset = (DataSetData) node.getData();
                DDatasetType dsType = dataset.getDatasetType();
                if (node.hasResultSummary() && (dsType.isAggregation())) {
                    m_referenceDataSetDefined = true;
                    refDataset = dataset.getDataset();
                } else {
                    m_referenceDataSetDefined = false;
                }
            }
            createQuantitationDialog(refDataset, x, y);
        }
    }

    private void createQuantitationDialog(DDataset dataset, int x, int y) {
        Long pID = ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject().getId();
        CreateQuantitationDialog dialog = CreateQuantitationDialog.getDialog(WindowManager.getDefault().getMainWindow());
        IdentificationTree childTree = null;
        DDataset refDataset = null;
        DDataset existingDataset = (m_isFromExistingQuantitation) ? dataset : null;
        
        if (m_referenceDataSetDefined) {
            childTree = IdentificationTree.getCurrentTree().copyDataSetRootSubTree(dataset, dataset.getProject().getId());
            refDataset = dataset;
        } else if (existingDataset != null && existingDataset.getMasterQuantitationChannels().get(0).getIdentDataset() != null) {
            refDataset = dataset.getMasterQuantitationChannels().get(0).getIdentDataset();
            childTree = IdentificationTree.getCurrentTree().copyDataSetRootSubTree(refDataset, refDataset.getProject().getId());
            if (childTree == null) {
                m_logger.debug("Reference dataset not already loaded: creates a new node to display in a new IdentificationTree");
                childTree = new IdentificationTree(ChildFactory.createNode(new DataSetData(refDataset)));
            }
        }

        dialog.initializeExperimentalDesignTree(existingDataset, refDataset, childTree, m_quantitationType);

        dialog.setLocation(x, y);
        dialog.setVisible(true);
        final Long[] _xicQuantiDataSetId = new Long[1];

        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {

            //User specified experimental design dataset
            DataSetData _quantiDS = null;
            Map<String, Object> expParams = null;

            StringBuffer errorMsg = new StringBuffer("");

            Map<String, Object> quantParams = dialog.getQuantiParameters();
            if (quantParams == null) {
                errorMsg.append("Null Quantitation parameters !  ");
            }

            if (dialog.getQuantitationDataset() == null) {
                errorMsg.append("No experimental design defined  ");

            } else {

                String registerErrMsg = dialog.registerRawFiles();
                if(registerErrMsg != null && !registerErrMsg.isEmpty())
                    errorMsg.append( registerErrMsg);

                //*** Get experimental design values                
                _quantiDS = dialog.getQuantitationDataset();

                try {
                    expParams = dialog.getExperimentalDesignParameters();
                } catch (IllegalAccessException iae) {
                    errorMsg.append(iae.getMessage());
                }
            }

            if (!errorMsg.toString().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, errorMsg, "Warning", JOptionPane.ERROR_MESSAGE);
                return;
            }

            m_logger.debug(" Will Compute Quantitation on " + ((List) expParams.get("biological_samples")).size() + "samples.");

            final QuantitationTree tree = QuantitationTree.getCurrentTree();
            final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
            final DataSetNode[] _quantitationNode = { QuantitationTree.getCurrentTree().createQuantitationNode(_quantiDS.getName()) };
            
            // CallBack for Xic Quantitation Service
            AbstractJMSCallback quantCallback = new AbstractJMSCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success) {
                    if (success) {
                        m_logger.debug(" Quantitation SUCCESS : " + _xicQuantiDataSetId[0]);
                        QuantitationTree.getCurrentTree().loadDataSet(_xicQuantiDataSetId[0], _quantitationNode[0]);
                    } else {
                        m_logger.debug(" Quantitation ERROR ");
                        treeModel.removeNodeFromParent(_quantitationNode[0]);
                    }
                }
            };
           
            RunQuantitationTask task = new RunQuantitationTask(quantCallback, pID, _quantiDS.getName(), quantParams, expParams, dialog.getQuantMethodId(), _xicQuantiDataSetId);
            AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
           
        } //End OK entered      
    }

    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {

        // to execute this action, the user must be the owner of the project
        Project selectedProject = ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject();
        if (!DatabaseDataManager.getDatabaseDataManager().ownProject(selectedProject)) {
            setEnabled(false);
            return;
        }

        if (m_isFromExistingQuantitation) {
            // only one node selected
            if (selectedNodes.length != 1) {
                setEnabled(false);
                return;
            }

            AbstractNode node = (AbstractNode) selectedNodes[0];

            // the node must not be in changing state
            if (node.isChanging()) {
                setEnabled(false);
                return;
            }

            // must be a dataset 
            if (node.getType() != AbstractNode.NodeTypes.DATA_SET) {
                setEnabled(false);
                return;
            }

            DataSetNode datasetNode = (DataSetNode) node;

            // must be a quantitation XIC
            if (!datasetNode.isQuantXIC()) {
                setEnabled(false);
                return;
            }
        } else {
            AbstractNode node = (AbstractNode) selectedNodes[0];
            
            // must be a dataset or a project quantitation
            if ((node.getType() != AbstractNode.NodeTypes.DATA_SET) && (node.getType() != AbstractNode.NodeTypes.PROJECT_QUANTITATION) ) {
                setEnabled(false);
                return;
            }
            
            if ((node.getType() == AbstractNode.NodeTypes.DATA_SET) && ((DataSetNode)node).isFolder() ) {
                setEnabled(false);
                return;
            }
            
        }
        
        setEnabled(true);
    }

}
