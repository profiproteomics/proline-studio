package fr.proline.studio.rsmexplorer.actions.identification;


import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.pattern.*;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.gui.dialog.DataBoxChooserDialog;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;

import java.util.ArrayList;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * Action to display the dialog to choose a view (databox) for a user window
 * @author JM235353
 */
public class DisplayUserWindowAction extends AbstractRSMAction {
    
    private final char m_windowType;
    
    public DisplayUserWindowAction(char windowType, AbstractTree tree) {
        super(NbBundle.getMessage(DisplayRsmProteinSetsAction.class, "CTL_DisplayUserWindowAction"), tree);
        m_windowType = windowType;
    }
    
    private boolean forRsm(){
        return m_windowType == WindowSavedManager.SAVE_WINDOW_FOR_RSM;
    }
    
    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {
        // only one node selected for this action
        DataSetNode dataSetNode = (DataSetNode) selectedNodes[0];
        
        ArrayList<GroupParameter> outParameters = new ArrayList<>();
        if ((!forRsm()) && (dataSetNode.hasResultSet())) {
            GroupParameter outParameter = new GroupParameter();
            outParameter.addParameter(ResultSet.class, false);
            outParameters.add(outParameter);
        }
        if ((forRsm()) && (dataSetNode.hasResultSummary())) {
            GroupParameter outParameter = new GroupParameter();
            outParameter.addParameter(ResultSummary.class, false);
            outParameters.add(outParameter);
        }
        if (m_windowType == WindowSavedManager.SAVE_WINDOW_FOR_QUANTI && dataSetNode.isQuantitation()){
            GroupParameter outParameter = new GroupParameter();
            outParameter.addParameter(DDataset.class, false);
            outParameters.add(outParameter);
        }

        final DDataset dataSet = ((DataSetData) dataSetNode.getData()).getDataset();

        DataBoxChooserDialog dialog = new DataBoxChooserDialog(WindowManager.getDefault().getMainWindow(), outParameters, true);
        dialog.setLocation(x, y);
        dialog.setVisible(true);
        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            AbstractDataBox genericDatabox = dialog.getSelectedDataBox();
            try {
                AbstractDataBox databox = (AbstractDataBox) genericDatabox.getClass().newInstance();

                final WindowBox wbox = WindowBoxFactory.getUserDefinedWindowBox(dataSet.getName(), dataSet.getName()+" "+dialog.getWndTitle(), databox, false, dataSetNode.isQuantXIC(), m_windowType);

                if (forRsm()) {
                    
                    // --- RSM
                    
                    ResultSummary rsm = dataSetNode.getResultSummary();



                    if (rsm != null) {


                        wbox.setEntryData(dataSet.getProject().getId(), rsm);

                        // open a window to display the window box
                        DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
                        win.open();
                        win.requestActive();
                    } else {

                        // open a window to display the window box
                        DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
                        win.open();
                        win.requestActive();

                        // we have to load the result set
                        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                            @Override
                            public boolean mustBeCalledInAWT() {
                                return true;
                            }

                            @Override
                            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                                // prepare window box
                                wbox.setEntryData(dataSet.getProject().getId(), dataSet.getResultSummary());
                            }
                        };


                        // ask asynchronous loading of data
                        DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
                        task.initLoadRsetAndRsm(dataSet);
                        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

                    }
                } else if (m_windowType == WindowSavedManager.SAVE_WINDOW_FOR_RSET){
                    // --- RSET


                    ResultSet rset = dataSetNode.getResultSet();


                    if (rset != null) {

                        wbox.setEntryData(dataSet.getProject().getId(), rset);



                        // open a window to display the window box
                        DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
                        win.open();
                        win.requestActive();
                    } else {

                        DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
                        win.open();
                        win.requestActive();

                        // we have to load the result set
                        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                            @Override
                            public boolean mustBeCalledInAWT() {
                                return true;
                            }

                            @Override
                            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                                // prepare window box
                                wbox.setEntryData(dataSet.getProject().getId(), dataSet.getResultSet());
                            }
                        };


                        // ask asynchronous loading of data
                        DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
                        task.initLoadRsetAndRsm(dataSet);
                        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

                    }
                }else if (m_windowType == WindowSavedManager.SAVE_WINDOW_FOR_QUANTI){
                    // --- QUANTI
                    DDataset ds = dataSetNode.getDataset();


                    if (ds != null) {

                        wbox.setEntryData(dataSet.getProject().getId(), ds);



                        // open a window to display the window box
                        DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
                        win.open();
                        win.requestActive();
                    } else {

                        DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
                        win.open();
                        win.requestActive();

                        // we have to load the result set
                        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                            @Override
                            public boolean mustBeCalledInAWT() {
                                return true;
                            }

                            @Override
                            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                                // prepare window box
                                wbox.setEntryData(dataSet.getProject().getId(), dataSet.getResultSet());
                            }
                        };


                        // ask asynchronous loading of data
                        DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
                        task.initLoadQuantitation(dataSet.getProject(), dataSet);
                        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

                    }
                }


            } catch (InstantiationException | IllegalAccessException e) {
                // should never happen
            }
         }

        
        
    }

    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {
         int nbSelectedNodes = selectedNodes.length;

        // only one node can be selected
        if (nbSelectedNodes != 1) {
            setEnabled(false);
            return;
        }
        
         AbstractNode node = selectedNodes[0];
        if (node.getType() != AbstractNode.NodeTypes.DATA_SET && node.getType() != AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS) {
            setEnabled(false);
            return;
        }
        
        DataSetNode dataSetNode = (DataSetNode) node;
        
        if ((m_windowType == WindowSavedManager.SAVE_WINDOW_FOR_RSET) && (!dataSetNode.hasResultSet())) {
            setEnabled(false);
            return;
        }
        
                
        if ((forRsm()) && (!dataSetNode.hasResultSummary())) {
            setEnabled(false);
            return;
        }
        
        if ((m_windowType == WindowSavedManager.SAVE_WINDOW_FOR_QUANTI && (!dataSetNode.isQuantitation())) ) {
            setEnabled(false);
            return;
        }
        
        setEnabled(true);
    }
    
}
