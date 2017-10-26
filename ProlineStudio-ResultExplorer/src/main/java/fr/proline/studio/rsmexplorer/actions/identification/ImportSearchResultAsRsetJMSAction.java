package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.studio.rsmexplorer.tree.identification.IdAllImportedNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.identification.IdProjectIdentificationNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.core.orm.uds.Project;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.CertifyIdentificationTask;
import fr.proline.studio.dpm.task.jms.ImportIdentificationTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.ImportIdentificationDialog;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * Action to import one or multiple identification
 * @author JM235353
 */
public class ImportSearchResultAsRsetJMSAction extends AbstractRSMAction {

    private static int m_beingImportedNumber = 0;
    
    private static HashMap<Long, ArrayList<ChangeListener>> m_listenerMap = new HashMap<>();
    
    public ImportSearchResultAsRsetJMSAction() {
        super(NbBundle.getMessage(ImportSearchResultAsRsetJMSAction.class, "CTL_AddSearchResult"), AbstractTree.TreeType.TREE_IDENTIFICATION);
    }
    
    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {
        
        // only IdAllImportedNode selected for this action
        final IdAllImportedNode allImportedNode = (IdAllImportedNode) selectedNodes[0];
        
        IdProjectIdentificationNode projectNode = (IdProjectIdentificationNode) allImportedNode.getParent();
        Project project =  projectNode.getProject();
        
        ImportIdentificationDialog dialog = ImportIdentificationDialog.getDialog(WindowManager.getDefault().getMainWindow());
        dialog.setLocation(x, y);
        dialog.setVisible(true);
        
        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            
             // retrieve parameters
            final File[] filePaths = dialog.getFilePaths();
            final HashMap<String, String> parserArguments = dialog.getParserArguments();
            

            
            final String parserId = dialog.getParserId();
            final String decoyRegex = dialog.getDecoyRegex();
            final long instrumentId = dialog.getInstrumentId();
            final long peaklistSoftwareId = dialog.getPeaklistSoftwareId();
            final boolean saveSpectrumMatches = false; // ABU old import parameter
            
            IdentificationTree tree = IdentificationTree.getCurrentTree();
            final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
            
            allImportedNode.setIsChanging(true);
            treeModel.nodeChanged(allImportedNode);
            
            final Long projectId = project.getId();


            // --------------------- Pre-Import --------------------------

            int nbFiles = filePaths.length;
            String[] pathArray = new String[nbFiles];
            for (int i = 0; i < nbFiles; i++) {
                File f = filePaths[i];

                pathArray[i] = f.getPath();
            }

 
            final String[] result = new String[1];
            
            AbstractJMSCallback callback = new AbstractJMSCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success) {

                    if (success) {
                        // start imports
                        startImport(projectId, allImportedNode, filePaths, treeModel, parserId, parserArguments, decoyRegex, instrumentId, peaklistSoftwareId, saveSpectrumMatches  );
                    } else {
                        allImportedNode.setIsChanging(false);
                        treeModel.nodeChanged(allImportedNode);
                    }
                }
            };

            CertifyIdentificationTask task = new CertifyIdentificationTask(callback, parserId, parserArguments, pathArray, projectId, result);
            AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
            
        }
    }
    
    private void startImport(final Long projectId, final IdAllImportedNode allImportedNode, File[] filePaths, final DefaultTreeModel treeModel, String parserId, HashMap<String, String> parserArguments, String decoyRegex, long instrumentId, long peaklistSoftwareId,  boolean saveSpectrumMatches ) {
        
        AbstractJMSCallback callback = new AbstractJMSCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success) {

                fireListener(projectId);

                m_beingImportedNumber--;
                if (m_beingImportedNumber == 0) {
                    allImportedNode.setIsChanging(false);
                    treeModel.nodeChanged(allImportedNode);
                }
            }
        };


        // Start identification for each file
        int nbFiles = filePaths.length;
        m_beingImportedNumber += nbFiles;
        for (int i = 0; i < nbFiles; i++) {
            File f = filePaths[i];

            String path = f.getPath();
            Long[] resultSetId = new Long[1];

            ImportIdentificationTask task = new ImportIdentificationTask(callback, parserId, parserArguments, path, decoyRegex, instrumentId, peaklistSoftwareId, saveSpectrumMatches, projectId, resultSetId);
            AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
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
        
        int nbSelectedNodes = selectedNodes.length;

        // identification must be added only in "All imported Node"
        if (nbSelectedNodes != 1) {
            setEnabled(false);
            return;
        }

        setEnabled(true);
    }
    
    
    public static synchronized void addEventListener(Long key, ChangeListener l) {
        ArrayList<ChangeListener> listeners = m_listenerMap.get(key);
        if (listeners == null) {
            listeners = new ArrayList<>(1);
            m_listenerMap.put(key, listeners);
        }
        listeners.add(l);
    }
    public static synchronized void removeEventListener(Long key, ChangeListener l) {
        ArrayList<ChangeListener> listeners = m_listenerMap.get(key);
        if (listeners == null) {
            return;
        }
        listeners.remove(l);
        if (listeners.isEmpty()) {
            m_listenerMap.remove(key);
        }
    }
    public static synchronized void fireListener(Long key) {
        
        ArrayList<ChangeListener> listeners = m_listenerMap.get(key);
        if (listeners == null) {
            return;
        }
        
        int nb = listeners.size();
        for (int i=0;i<nb;i++) {
            listeners.get(i).stateChanged(null);
        }
    }
    
}
