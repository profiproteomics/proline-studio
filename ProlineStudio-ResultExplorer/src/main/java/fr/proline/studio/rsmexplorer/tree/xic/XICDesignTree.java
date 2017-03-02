package fr.proline.studio.rsmexplorer.tree.xic;

import fr.proline.core.orm.uds.Aggregation;
import fr.proline.core.orm.uds.BiologicalGroup;
import fr.proline.core.orm.uds.BiologicalSample;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.RawFile;
import fr.proline.core.orm.uds.GroupSetup;
import fr.proline.core.orm.uds.SampleAnalysis;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.core.orm.uds.BiologicalSplSplAnalysisMap;
import fr.proline.core.orm.uds.Run;
import fr.proline.studio.dam.data.RunInfoData;
import fr.proline.studio.rsmexplorer.actions.identification.AbstractRSMAction;
import fr.proline.studio.rsmexplorer.actions.xic.CreateAction;
import fr.proline.studio.rsmexplorer.actions.xic.DeleteAction;
import fr.proline.studio.rsmexplorer.actions.xic.RenameAction;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 * XICDesignTree represents a XIC design
 *
 * @author JM235353
 */
public class XICDesignTree extends AbstractTree {

    private JPopupMenu m_mainPopup;
    private ArrayList<AbstractRSMAction> m_mainActions;

    public XICDesignTree(AbstractNode top, boolean editable) {

        setEditable(editable);

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        XICTransferHandler handler = new XICTransferHandler(false, this);
        setTransferHandler(handler);

        setDropMode(DropMode.ON_OR_INSERT);
        setDragEnabled(true);

        initTree(top);
    }

    @Override
    public void rename(AbstractNode rsmNode, String newName) {

        AbstractNode.NodeTypes nodeType = rsmNode.getType();
        if ((nodeType == AbstractNode.NodeTypes.BIOLOGICAL_GROUP)
                || (nodeType == AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE)
                || (nodeType == AbstractNode.NodeTypes.DATA_SET)) {

            ((DataSetData) rsmNode.getData()).setTemporaryName(newName);
        }
    }

    @Override
    public boolean isPathEditable(TreePath path) {

        Project selectedProject = ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject();
        if (!DatabaseDataManager.getDatabaseDataManager().ownProject(selectedProject)) {
            return false;
        }

        if (isEditable()) {
            AbstractNode node = (AbstractNode) path.getLastPathComponent();
            AbstractNode.NodeTypes nodeType = node.getType();
            if ((nodeType == AbstractNode.NodeTypes.BIOLOGICAL_GROUP)
                    || (nodeType == AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE)
                    || (nodeType == AbstractNode.NodeTypes.DATA_SET)) {

                return true;
            }
        }
        return false;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (isEditable() && SwingUtilities.isRightMouseButton(e)) {
            triggerPopup(e);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    private void triggerPopup(MouseEvent e) {

        // retrieve selected nodes
        AbstractNode[] selectedNodes = getSelectedNodes();

        JPopupMenu popup;
        ArrayList<AbstractRSMAction> actions;

        // creation of the popup if needed
        if (m_mainPopup == null) {

            // create the actions
            m_mainActions = new ArrayList<AbstractRSMAction>(5);  // <--- get in sync

            CreateAction createAction = new CreateAction(this);
            m_mainActions.add(createAction);
            
            m_mainActions.add(null);
            
            RenameAction renameAction = new RenameAction(this);
            m_mainActions.add(renameAction);

            m_mainActions.add(null);  // separator

            DeleteAction deleteAction = new DeleteAction(this);
            m_mainActions.add(deleteAction);

            // add actions to popup
            m_mainPopup = new JPopupMenu();
            for (int i = 0; i < m_mainActions.size(); i++) {
                AbstractRSMAction action = m_mainActions.get(i);
                if (action == null) {
                    m_mainPopup.addSeparator();
                } else {
                    m_mainPopup.add(action.getPopupPresenter());
                }
            }

        }

        popup = m_mainPopup;
        actions = m_mainActions;

        // update of the enable/disable state
        for (int i = 0; i < actions.size(); i++) {
            AbstractRSMAction action = actions.get(i);

            if (action == null) {
                continue;
            }

            action.updateEnabled(selectedNodes);
        }

        popup.show((JComponent) e.getSource(), e.getX(), e.getY());
    }

    /**
     * This is called when Experimental Design already exist : Create XIC by clone, Display XIC result or Experimental Design... 
     * @param dataset
     * @param rootNode
     * @param tree
     * @param expandPath
     * @param includeRunNodes 
     */
    public static void setExpDesign(DDataset dataset, AbstractNode rootNode, AbstractTree tree, boolean expandPath, boolean includeRunNodes) {
        if (dataset == null) {
            return;
        }
        RSMTreeModel treeModel = (RSMTreeModel) tree.getModel();
        if (rootNode.getChildCount() > 0) {
            rootNode.remove(0); // remove the first child which correspond to the hour glass
            treeModel.nodeStructureChanged(rootNode);
        }

        rootNode.setIsChanging(false);
        treeModel.nodeChanged(rootNode);
        GroupSetup groupSetup = dataset.getGroupSetup();
        if (groupSetup == null) {
            return;
        }
        List<DQuantitationChannel> listQuantChannels = dataset.getMasterQuantitationChannels().isEmpty() ? new ArrayList() : dataset.getMasterQuantitationChannels().get(0).getQuantitationChannels();

        List<BiologicalGroup> listBiologicalGroups = groupSetup.getBiologicalGroups();
        int childIndex = 0;
        for (BiologicalGroup bioGroup : listBiologicalGroups) {
            XICBiologicalGroupNode biologicalGroupNode = new XICBiologicalGroupNode(new DataSetData(bioGroup.getName(), Dataset.DatasetType.AGGREGATE, Aggregation.ChildNature.OTHER));
            treeModel.insertNodeInto(biologicalGroupNode, rootNode, childIndex);
            List<BiologicalSample> listSample = bioGroup.getBiologicalSamples();
            int childSampleIndex = 0;
            for (BiologicalSample sample : listSample) {
                String sampleName = sample.getName();
                // split the name because in Studio the sampleName is saved as groupName+sampleName see Issue#12628
                if (sampleName.startsWith(bioGroup.getName())) {
                    sampleName = sampleName.substring(bioGroup.getName().length());
                }
                XICBiologicalSampleNode biologicalSampleNode = new XICBiologicalSampleNode(new DataSetData(sampleName, Dataset.DatasetType.AGGREGATE, Aggregation.ChildNature.OTHER));
                treeModel.insertNodeInto(biologicalSampleNode, biologicalGroupNode, childSampleIndex);
                List<BiologicalSplSplAnalysisMap> listSampleAnalysis = sample.getBiologicalSplSplAnalysisMap();
                int childSampleAnalysisIndex = 0;
                for (BiologicalSplSplAnalysisMap sampleAnalysis : listSampleAnalysis) {
                    DQuantitationChannel qCh = getQuantChannelSampleAnalysis(sampleAnalysis.getSampleAnalysis(), listQuantChannels);
                    if (qCh != null) {
                        String name = qCh.getResultFileName();
                        // fake dataset
                        DDataset dds = new DDataset(qCh.getIdentDatasetId(), dataset.getProject(), name, Dataset.DatasetType.IDENTIFICATION, 0, qCh.getIdentRs().getId(), qCh.getIdentResultSummaryId(), 1);
                        dds.setResultSet(qCh.getIdentRs());
                        DataSetData dsData = new DataSetData(name, Dataset.DatasetType.IDENTIFICATION, Aggregation.ChildNature.SAMPLE_ANALYSIS);
                        dsData.setDataset(dds);
                        XICBiologicalSampleAnalysisNode sampleAnalysisNode = new XICBiologicalSampleAnalysisNode(dsData);
                        if (qCh.getName() != null) {
                            sampleAnalysisNode.setQuantChannelName(qCh.getName());
                        }

                        if (includeRunNodes) {
                            RunInfoData runInfoData = new RunInfoData();
                            RawFile rawFile = new RawFile();
                            int id = qCh.getMzdbFileName().indexOf(".");
                            if (id == -1) {
                                id = qCh.getMzdbFileName().length();
                            }
                            String identifier = qCh.getMzdbFileName().substring(0, id);
                            rawFile.setRawFileName(qCh.getMzdbFileName());
                            rawFile.setIdentifier(identifier);
                            rawFile.setMzDbFileName(qCh.getMzdbFileName());
                            runInfoData.setLinkedRawFile(rawFile);

                            List<Run> runs = new ArrayList();
                            Run run = new Run();
                            run.setId(-1); // explicitely set the runId to -1, to avoid to register runidentification (no need)
                            run.setRawFile(rawFile);
                            runInfoData.setRun(run);
                            runs.add(run);
                            rawFile.setRuns(runs);

                            XICRunNode runNode = new XICRunNode(runInfoData, tree);
                            sampleAnalysisNode.add(runNode);

                            runNode.init(sampleAnalysisNode.getDataset(), treeModel, null, null);
                        }

                        treeModel.insertNodeInto(sampleAnalysisNode, biologicalSampleNode, childSampleAnalysisIndex);
                        if (expandPath) {
                            tree.expandPath(new TreePath(sampleAnalysisNode.getPath()));
                        } else {
                            tree.expandPath(new TreePath(biologicalSampleNode.getPath()));
                        }
                        childSampleAnalysisIndex++;
                    }
                }
                childSampleIndex++;
            }
            // expandPath( new TreePath(biologicalGroupNode.getPath()));
            childIndex++;
        }
    }

    private static DQuantitationChannel getQuantChannelSampleAnalysis(SampleAnalysis sampleAnalysis, List<DQuantitationChannel> listQuantChannels) {
        for (DQuantitationChannel qCh : listQuantChannels) {
            SampleAnalysis sampleReplicate = qCh.getSampleReplicate();
            if (sampleReplicate != null && sampleReplicate.getId() == sampleAnalysis.getId()) {
                return qCh;
            }
        }
        return null;
    }

    public void renameXicTitle(String newName) {
        AbstractNode rootNode = (AbstractNode) m_model.getRoot();
        rename(rootNode, newName);
        ((DefaultTreeModel) getModel()).nodeChanged(rootNode);
    }

}
