package fr.proline.studio.rsmexplorer.actions;


import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.pattern.*;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.gui.dialog.DataBoxChooserDialog;
import fr.proline.studio.rsmexplorer.node.RSMDataSetNode;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import java.util.ArrayList;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author JM235353
 */
public class DisplayUserWindowAction extends AbstractRSMAction {
    
    private boolean m_forRsm;
    
    public DisplayUserWindowAction(boolean forRsm) {
        super(NbBundle.getMessage(DisplayRsmProteinSetsAction.class, "CTL_DisplayUserWindowAction"));
        m_forRsm = forRsm;
    }
    
    @Override
    public void actionPerformed(RSMNode[] selectedNodes, int x, int y) {
        // only one node selected for this action
        RSMDataSetNode dataSetNode = (RSMDataSetNode) selectedNodes[0];
        
        ArrayList<GroupParameter> outParameters = new ArrayList<>();
        if ((!m_forRsm) && (dataSetNode.hasResultSet())) {
            GroupParameter outParameter = new GroupParameter();
            outParameter.addParameter(ResultSet.class, false);
            outParameters.add(outParameter);
        }
        if ((m_forRsm) && (dataSetNode.hasResultSummary())) {
            GroupParameter outParameter = new GroupParameter();
            outParameter.addParameter(ResultSummary.class, false);
            outParameters.add(outParameter);
        }

        final Dataset dataSet = ((DataSetData) dataSetNode.getData()).getDataset();

        DataBoxChooserDialog dialog = new DataBoxChooserDialog(WindowManager.getDefault().getMainWindow(), outParameters);
        dialog.setLocation(x, y);
        dialog.setVisible(true);
        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            AbstractDataBox genericDatabox = dialog.getSelectedDataBox();
            try {
                AbstractDataBox databox = (AbstractDataBox) genericDatabox.getClass().newInstance();

                final WindowBox wbox = WindowBoxFactory.getUserDefinedWindowBox(databox.getName(), databox, false);

                if (m_forRsm) {
                    
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
                                wbox.setEntryData(dataSet.getProject().getId(), dataSet.getTransientData().getResultSummary());
                            }
                        };


                        // ask asynchronous loading of data
                        DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
                        task.initLoadRsetAndRsm(dataSet);
                        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

                    }
                } else {
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
                                wbox.setEntryData(dataSet.getProject().getId(), dataSet.getTransientData().getResultSet());
                            }
                        };


                        // ask asynchronous loading of data
                        DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
                        task.initLoadRsetAndRsm(dataSet);
                        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

                    }
                }


            } catch (InstantiationException | IllegalAccessException e) {
                // should never happen
            }
         }

        
        
    }

    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {
         int nbSelectedNodes = selectedNodes.length;

        // only one node can be selected
        if (nbSelectedNodes != 1) {
            setEnabled(false);
            return;
        }
        
         RSMNode node = selectedNodes[0];
        if (node.getType() != RSMNode.NodeTypes.DATA_SET) {
            setEnabled(false);
            return;
        }
        
        RSMDataSetNode dataSetNode = (RSMDataSetNode) node;
        
        if ((!m_forRsm) && (!dataSetNode.hasResultSet())) {
            setEnabled(false);
            return;
        }
        
                
        if ((m_forRsm) && (!dataSetNode.hasResultSummary())) {
            setEnabled(false);
            return;
        }
        
        setEnabled(true);
    }
    
}
