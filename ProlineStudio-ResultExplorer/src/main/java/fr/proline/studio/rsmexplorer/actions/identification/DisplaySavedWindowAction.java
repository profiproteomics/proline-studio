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
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import java.util.prefs.Preferences;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author JM235353
 */
public class DisplaySavedWindowAction extends AbstractRSMAction {

    public DisplaySavedWindowAction() {
        super(NbBundle.getMessage(DisplaySavedWindowAction.class, "CTL_DisplaySavedWindowAction"), AbstractTree.TreeType.TREE_IDENTIFICATION);

    }

    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {


        Preferences preferences = NbPreferences.root();
        String wndDefinition = preferences.get("TESTWND", null);

        boolean needsRsm = WindowBoxFactory.hasResultSummaryParameter(wndDefinition);


        int nbNodes = selectedNodes.length;
        for (int i = 0; i < nbNodes; i++) {
            DataSetNode dataSetNode = (DataSetNode) selectedNodes[i];

            actionImpl(wndDefinition, dataSetNode, needsRsm);
        }
    }

    private void actionImpl(String wndDefinition, DataSetNode dataSetNode, boolean needsRsm) {
        
        final DDataset dataSet = ((DataSetData) dataSetNode.getData()).getDataset();
        long projectId = dataSet.getProject().getId();
        
        
        AbstractDataBox[] databoxes = WindowBoxFactory.readBoxes(wndDefinition);
        databoxes[0].setProjectId(projectId);
        WindowBox wbox = WindowBoxFactory.getFromBoxesWindowBox("", databoxes, true, false);
        
        if (needsRsm) {
            ResultSummary rsm = dataSetNode.getResultSummary();
            if (rsm == null) {
                loadRsmOrRsetAndDisplayWnd(dataSet, wbox, needsRsm);
                return;
            }
        } else {
            ResultSet rset = dataSetNode.getResultSet();
            if (rset == null) {
                loadRsmOrRsetAndDisplayWnd(dataSet, wbox, needsRsm);
                return;
            }
        }
        
        displayWnd(dataSet, wbox, needsRsm);

    }
    private void loadRsmOrRsetAndDisplayWnd(final DDataset dataSet, final WindowBox wbox, final boolean needsRsm) {
        // we have to load the result set
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                displayWnd(dataSet, wbox, needsRsm);
            }
        };


        // ask asynchronous loading of data
        DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
        task.initLoadRsetAndRsm(dataSet);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
    }


    private void displayWnd(DDataset dataSet, WindowBox wbox, boolean needsRsm) {
        long projectId = dataSet.getProject().getId();
        
        if (needsRsm) {
            wbox.setEntryData(projectId, dataSet.getResultSummary());
        } else {
            wbox.setEntryData(projectId, dataSet.getResultSet());
        }
         // open a window to display the window box
        DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
        win.open();
        win.requestActive();
    }


    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {
        
       Preferences preferences = NbPreferences.root();
       String wndDefinition = preferences.get("TESTWND", null);
        
       if (wndDefinition == null) {
           setEnabled(false);
           return;
       }
       
       boolean needsRset = WindowBoxFactory.hasResultSetParameter(wndDefinition);
       boolean needsRsm = WindowBoxFactory.hasResultSummaryParameter(wndDefinition);
       
       
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
        }
        

        setEnabled(true);
    }
}
