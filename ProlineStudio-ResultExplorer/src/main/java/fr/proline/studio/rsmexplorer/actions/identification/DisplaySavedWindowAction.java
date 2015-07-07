package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.pattern.WindowSavedManager;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import java.util.ArrayList;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author JM235353
 */
public class DisplaySavedWindowAction extends AbstractRSMAction {

    private int m_wndIndex;
    
    public DisplaySavedWindowAction(String name, int wndIndex, AbstractTree.TreeType treeType) {
        super(name, treeType);
        m_wndIndex = wndIndex;
    }

    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {

        ArrayList<String> savedWindowsList = WindowSavedManager.readSavedWindows();
        if (savedWindowsList.isEmpty()) {
            return;
        }

        String savedWindow = savedWindowsList.get(m_wndIndex);

        char windowType = WindowSavedManager.getWindowType(savedWindow);
        String windowName = WindowSavedManager.getWindowName(savedWindow);

        int nbNodes = selectedNodes.length;
        for (int i = 0; i < nbNodes; i++) {
            DataSetNode dataSetNode = (DataSetNode) selectedNodes[i];

            actionImpl(windowName, savedWindow, dataSetNode, windowType);
        }
    }

    private void actionImpl(String windowName, String savedWindow, DataSetNode dataSetNode, char windowType) {
        
        final DDataset dataSet = ((DataSetData) dataSetNode.getData()).getDataset();
        long projectId = dataSet.getProject().getId();
        
        
        AbstractDataBox[] databoxes = WindowSavedManager.readBoxes(savedWindow);
        databoxes[0].setProjectId(projectId);
        WindowBox wbox = WindowBoxFactory.getFromBoxesWindowBox(dataSet.getName()+" "+windowName, databoxes, false, dataSetNode.isQuantXIC(),  windowType);
        
        if (windowType == WindowSavedManager.SAVE_WINDOW_FOR_RSM) {
            ResultSummary rsm = dataSetNode.getResultSummary();
            if (rsm == null) {
                loadRsmOrRsetAndDisplayWnd(dataSet, wbox, WindowSavedManager.SAVE_WINDOW_FOR_RSM);
                return;
            }
        } else if (windowType == WindowSavedManager.SAVE_WINDOW_FOR_RSET){
            ResultSet rset = dataSetNode.getResultSet();
            if (rset == null) {
                loadRsmOrRsetAndDisplayWnd(dataSet, wbox, WindowSavedManager.SAVE_WINDOW_FOR_RSET);
                return;
            }
        }else if (windowType == WindowSavedManager.SAVE_WINDOW_FOR_QUANTI){
            DDataset ds = dataSetNode.getDataset();
            if (ds == null) {
                loadDatasetAndDisplayWnd(dataSet, wbox);
                return;
            }
        }
        
        displayWnd(dataSet, wbox, windowType);

    }
    private void loadRsmOrRsetAndDisplayWnd(final DDataset dataSet, final WindowBox wbox, final char windowType) {
        // we have to load the result set
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                displayWnd(dataSet, wbox, windowType);
            }
        };


        // ask asynchronous loading of data
        DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
        task.initLoadRsetAndRsm(dataSet);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
    }
    
    private void loadDatasetAndDisplayWnd(final DDataset dataSet, final WindowBox wbox){
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                displayWnd(dataSet, wbox, WindowSavedManager.SAVE_WINDOW_FOR_QUANTI);
            }
        };


        // ask asynchronous loading of data
        DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
        task.initLoadQuantitation(dataSet.getProject(), dataSet);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
    }


    private void displayWnd(DDataset dataSet, WindowBox wbox, char windowType) {
        long projectId = dataSet.getProject().getId();
        
        if (windowType == WindowSavedManager.SAVE_WINDOW_FOR_RSM) {
            wbox.setEntryData(projectId, dataSet.getResultSummary());
        } else if (windowType == WindowSavedManager.SAVE_WINDOW_FOR_RSET){
            wbox.setEntryData(projectId, dataSet.getResultSet());
        }else if (windowType == WindowSavedManager.SAVE_WINDOW_FOR_QUANTI){
            wbox.setEntryData(projectId, dataSet);
        }

         // open a window to display the window box
        DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
        win.open();
        win.requestActive();
    }


    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {
        
       Preferences preferences = NbPreferences.root();
       String wndDefinition = WindowSavedManager.readSavedWindows().get(m_wndIndex);
       
       boolean needsRset = WindowSavedManager.hasResultSetParameter(wndDefinition);
       boolean needsRsm = WindowSavedManager.hasResultSummaryParameter(wndDefinition);
       boolean needsDataset = WindowSavedManager.hasQuantiParameter(wndDefinition);
       
       
       int nbSelectedNodes = selectedNodes.length;

        // at least one node must be selected
        if (nbSelectedNodes <0) {
            setEnabled(false);
            return;
        }
        
        for (int i=0;i<nbSelectedNodes;i++) {
            AbstractNode node = selectedNodes[i];
            if (node.getType() != AbstractNode.NodeTypes.DATA_SET) {
                setEnabled(false);
                return;
            }

            DataSetNode dataSetNode = (DataSetNode) node;
            if ((needsRset) && (!dataSetNode.hasResultSet())) {
                setEnabled(false);
                return;
            }


            if ((needsRsm) && (!dataSetNode.hasResultSummary())) {
                setEnabled(false);
                return;
            }
            
            if ((needsDataset) && (!dataSetNode.isQuantitation())) {
                setEnabled(false);
                return;
            }
        }
        

        setEnabled(true);
    }
}
