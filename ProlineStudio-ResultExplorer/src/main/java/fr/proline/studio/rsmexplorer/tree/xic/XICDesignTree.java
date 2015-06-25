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
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.actions.identification.AbstractRSMAction;
import fr.proline.studio.rsmexplorer.actions.xic.DeleteAction;
import fr.proline.studio.rsmexplorer.actions.xic.RenameAction;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.CreateXICDialog;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.SelectRawFileDialog;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 * XICDesignTree represents a XIC design
 * @author JM235353
 */
public class XICDesignTree extends AbstractTree {

    private static XICDesignTree m_designTree = null;
    //private boolean m_rootPathError = false;
    //private ServerFile m_defaultDirectory = null;

    public static XICDesignTree getDesignTree() {
        return m_designTree;
    }

    public static XICDesignTree getDesignTree(AbstractNode top) {
        m_designTree = new XICDesignTree(top, true);
        return m_designTree;
    }
    
    public static XICDesignTree getDesignTree(AbstractNode top, boolean editable) {
        m_designTree = new XICDesignTree(top, editable);
        return m_designTree;
    }

    private XICDesignTree(AbstractNode top, boolean editable) {

        setEditable(editable);
        
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        XICTransferHandler handler = new XICTransferHandler(false);
        setTransferHandler(handler);

        setDropMode(DropMode.ON_OR_INSERT);
        setDragEnabled(true);

        
        
        initTree(top);
    }

    @Override
    protected void initTree(AbstractNode top) {
        super.initTree(top);

    }
    
    @Override
    public void rename(AbstractNode rsmNode, String newName) {
        
        AbstractNode.NodeTypes nodeType = rsmNode.getType();
         if ((nodeType == AbstractNode.NodeTypes.BIOLOGICAL_GROUP) ||
             (nodeType == AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE) ||
             (nodeType == AbstractNode.NodeTypes.DATA_SET)) {
            
            ((DataSetData)rsmNode.getData()).setTemporaryName(newName);
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
            if ((nodeType == AbstractNode.NodeTypes.BIOLOGICAL_GROUP) ||
                (nodeType == AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE) ||
                (nodeType == AbstractNode.NodeTypes.DATA_SET)) {
            
                return true;
            }
        }
        return false;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            
            TreePath path = getPathForLocation(e.getX(), e.getY());
            if (path == null) {
                return;
            }
            AbstractNode n = (AbstractNode) path.getLastPathComponent();

            if (n instanceof XICRunNode) {
                XICRunNode runNode = (XICRunNode) n;

                if (runNode.isChanging()) {
                    return;
                }
                
                RunInfoData runInfoData = (RunInfoData) runNode.getData();

                if (runInfoData.getRawFileSouce().getLinkedRawFile() != null) {
                    return; // already a registered raw file, the user can no longer change it
                }
                
                ArrayList<RawFile> potentialRawFiles = runInfoData.getPotentialRawFiles();
                if (potentialRawFiles == null) {
                    potentialRawFiles = new ArrayList<>();
                    runInfoData.setPotentialRawFiles(potentialRawFiles);
                }
                
                SelectRawFileDialog selectRawFileDialog = SelectRawFileDialog.getSelectRawFileDialog(CreateXICDialog.getDialog(null));
                selectRawFileDialog.init(potentialRawFiles, runInfoData.getRawFileSouce());
                selectRawFileDialog.centerToWindow(CreateXICDialog.getDialog(null));
                selectRawFileDialog.setVisible(true);
                if (selectRawFileDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                    
                    
    
                    RawFile rawFile = selectRawFileDialog.getSelectedRawFile();
                    if (rawFile != null) {
                        runInfoData.getRawFileSouce().setSelectedRawFile(rawFile);
                       
                        runInfoData.setRun(rawFile.getRuns().get(0));
                        //runInfoData.setRawFilePath(rawFile.getDirectory()+File.separator+rawFile.getRawFileName());  //JPM.RUNINFODATA
                        //runInfoData.setRunInfoInDatabase(true);
                        runNode.warnParent(false);
                        ((DefaultTreeModel)getModel()).nodeChanged(runNode);
                        return;
                    }
                    File rawFileOnDisk = selectRawFileDialog.getSelectedDiskFile();
                    if (rawFileOnDisk != null) {
                        runNode.setRawFile(rawFileOnDisk);
                    }
                }
                
                /*if (runInfoData.hasPotentialRawFiles()) {
                    // select one of the raw files
                    SelectRawFileDialog selectRawFileDialog = SelectRawFileDialog.getSelectRawFileDialog(CreateXICDialog.getDialog(null));
                    selectRawFileDialog.init(runInfoData.getPotentialRawFiles());
                    selectRawFileDialog.centerToWindow(CreateXICDialog.getDialog(null));
                    selectRawFileDialog.setVisible(true);
                    if (selectRawFileDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                        RawFile rawFile = selectRawFileDialog.getSelectedRawFile();
                        runInfoData.setRawFile(rawFile);
                        runInfoData.setRun(rawFile.getRuns().get(0));
                        runInfoData.setRawFilePath(rawFile.getDirectory()+File.separator+rawFile.getRawFileName());
                        runInfoData.setRunInfoInDatabase(true);
                        runNode.warnParent(false);
                        ((DefaultTreeModel)getModel()).nodeChanged(runNode);
                    } else {
                        return;
                    }
                } else {

                    if (m_fileChooser == null) {
                        if ((m_defaultDirectory != null) && (m_defaultDirectory.isDirectory())) {
                            m_fileChooser = new JFileChooser(m_defaultDirectory, ServerFileSystemView.getServerFileSystemView());
                        } else {
                            // should not happen in fact
                            m_fileChooser = new JFileChooser(ServerFileSystemView.getServerFileSystemView());
                        }
                    }
                    int result = m_fileChooser.showOpenDialog(this);
                    if (result == JFileChooser.APPROVE_OPTION) {

                        runNode.setRawFile(m_fileChooser.getSelectedFile());
                    }
                }*/
            }

        }
    }
    //private JFileChooser m_fileChooser = null;

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

        int nbNodes = selectedNodes.length;

        
        
        JPopupMenu popup;
        ArrayList<AbstractRSMAction> actions;
        


        // creation of the popup if needed
        if (m_mainPopup == null) {

            // create the actions
            m_mainActions = new ArrayList<>(12);  // <--- get in sync

            RenameAction renameAction = new RenameAction();
            m_mainActions.add(renameAction);

            

            m_mainActions.add(null);  // separator

            DeleteAction deleteAction = new DeleteAction();
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
        for (int i=0;i<actions.size();i++) {
            AbstractRSMAction action = actions.get(i);
            
            if (action == null) {
                continue;
            }
            
            action.updateEnabled(selectedNodes);
        }
        
        popup.show( (JComponent)e.getSource(), e.getX(), e.getY() );
    }
    private JPopupMenu m_mainPopup;
    private ArrayList<AbstractRSMAction> m_mainActions;

    
    public void setExpDesign(DDataset dataset){
        if (dataset == null){
            return;
        }
        GroupSetup groupSetup = dataset.getGroupSetup();
        if (groupSetup == null){
            return;
        }
        List<DQuantitationChannel> listQuantChannels = dataset.getMasterQuantitationChannels().isEmpty() ? new ArrayList() : dataset.getMasterQuantitationChannels().get(0).getQuantitationChannels();
        
        AbstractNode rootNode = (AbstractNode) m_model.getRoot();
        List<BiologicalGroup> listBiologicalGroups = groupSetup.getBiologicalGroups();
        int childIndex  = 0;
        for (BiologicalGroup bioGroup : listBiologicalGroups) {
            XICBiologicalGroupNode biologicalGroupNode = new XICBiologicalGroupNode(new DataSetData(bioGroup.getName(), Dataset.DatasetType.AGGREGATE, Aggregation.ChildNature.OTHER));
            m_model.insertNodeInto(biologicalGroupNode, rootNode, childIndex);
            List<BiologicalSample> listSample = bioGroup.getBiologicalSamples();
            int childSampleIndex = 0;
            for (BiologicalSample sample : listSample) {
                String sampleName = sample.getName();
                // split the name because in Studio the sampleName is saved as groupName+sampleName see Issue#12628
                if (sampleName.startsWith(bioGroup.getName())){
                    sampleName = sampleName.substring(bioGroup.getName().length());
                }
                XICBiologicalSampleNode biologicalSampleNode = new XICBiologicalSampleNode(new DataSetData(sampleName, Dataset.DatasetType.AGGREGATE, Aggregation.ChildNature.OTHER));
                m_model.insertNodeInto(biologicalSampleNode, biologicalGroupNode, childSampleIndex);
                List<BiologicalSplSplAnalysisMap> listSampleAnalysis = sample.getBiologicalSplSplAnalysisMap();
                int childSampleAnalysisIndex = 0;
                for (BiologicalSplSplAnalysisMap sampleAnalysis : listSampleAnalysis) {
                    DQuantitationChannel qCh = getQuantChannelSampleAnalysis(sampleAnalysis.getSampleAnalysis(), listQuantChannels);
                    if (qCh != null){
                        String name = qCh.getResultFileName();
                        // fake dataset
                        DDataset dds = new DDataset(-1,dataset.getProject() , name, Dataset.DatasetType.IDENTIFICATION, 0, dataset.getResultSetId(), qCh.getIdentResultSummaryId(), 1);
                        DataSetData dsData = new DataSetData(name, Dataset.DatasetType.IDENTIFICATION, Aggregation.ChildNature.SAMPLE_ANALYSIS ); 
                        dsData.setDataset(dds);
                        XICBiologicalSampleAnalysisNode sampleAnalysisNode = new XICBiologicalSampleAnalysisNode(dsData);
                        RunInfoData runInfoData = new RunInfoData();
                        RunInfoData.RawFileSource rawFileSource = new RunInfoData.RawFileSource();
                        RawFile rawFile = new RawFile();
                        int id= qCh.getMzdbFileName().indexOf(".");
                        if (id == -1){
                            id = qCh.getMzdbFileName().length();
                        }
                        String identifier = qCh.getMzdbFileName().substring(0, id);
                        rawFile.setRawFileName(qCh.getMzdbFileName());
                        rawFile.setIdentifier(identifier);
                        rawFile.setMzDbFileName(qCh.getMzdbFileName());
                        rawFileSource.setLinkedRawFile(rawFile);
                        runInfoData.setRawFileSource(rawFileSource);
                        List<Run> runs = new ArrayList();
                        Run run = new Run();
                        run.setRawFile(rawFile);
                        runInfoData.setRun(run);
                        runs.add(run);
                        rawFile.setRuns(runs);
                        XICRunNode runNode = new XICRunNode(runInfoData);
                        sampleAnalysisNode.add(runNode);
                        sampleAnalysisNode.m_hasError = false;
                        m_model.insertNodeInto(sampleAnalysisNode, biologicalSampleNode, childSampleAnalysisIndex);
                        expandPath( new TreePath(sampleAnalysisNode.getPath()));
                        childSampleAnalysisIndex++;
                    }
                }
                childSampleIndex++;
            }
           // expandPath( new TreePath(biologicalGroupNode.getPath()));
            childIndex++;
        }
    }
    
    private DQuantitationChannel getQuantChannelSampleAnalysis(SampleAnalysis sampleAnalysis, List<DQuantitationChannel> listQuantChannels){
        for (DQuantitationChannel qCh : listQuantChannels) {
            SampleAnalysis sampleReplicate = qCh.getSampleReplicate();
            if (sampleReplicate != null && sampleReplicate.getId() == sampleAnalysis.getId()){
                return qCh;
            }
        }
        return null;
    }
    
    public void renameXicTitle(String newName){
        AbstractNode rootNode = (AbstractNode) m_model.getRoot();
        rename(rootNode, newName);
        ((DefaultTreeModel) XICDesignTree.getDesignTree().getModel()).nodeChanged(rootNode);
    }
    
}
