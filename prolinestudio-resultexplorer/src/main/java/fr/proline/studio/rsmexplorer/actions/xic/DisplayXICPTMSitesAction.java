/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions.xic;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.data.ptm.PTMDataset;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.actions.identification.AbstractRSMAction;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import javax.swing.JOptionPane;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author VD225637
 */
public class DisplayXICPTMSitesAction extends AbstractRSMAction {

    public DisplayXICPTMSitesAction(AbstractTree tree) {
       super(NbBundle.getMessage(DisplayXICPTMSitesAction.class, "CTL_DisplayPtmSiteProtein"), tree);
    }
    
    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {
        int answer= JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(), "Do you want to view Protein Sites V2 ?");
        int nbNodes = selectedNodes.length;
        for (int i = 0; i < nbNodes; i++) {
            DataSetNode dataSetNode = (DataSetNode) selectedNodes[i];

            actionImpl(dataSetNode, answer);
        }

    }
    
    private void actionImpl(DataSetNode dataSetNode, int answer) {
        
        final DDataset dataSet = ((DataSetData) dataSetNode.getData()).getDataset();
        
        if (!dataSetNode.hasResultSummary()) {
            return; // should not happen
        }
        
        ResultSummary rsm = dataSetNode.getResultSummary();
        if (rsm != null) {

            // prepare window box
            WindowBox wbox;
            if(answer == JOptionPane.YES_OPTION){
                wbox = WindowBoxFactory.getXicPTMSitesWindowBoxV2(dataSet.getName());
            } else if (answer == JOptionPane.NO_OPTION){
                wbox = WindowBoxFactory.getXicPTMSitesWindowBoxV1(dataSet.getName());            
            } else {
                return;         
            }
            wbox.setEntryData(dataSet.getProject().getId(), new PTMDataset(dataSet));    
               
            // open a window to display the window box
            DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
            win.open();
            win.requestActive();
            
        } else {

            
            // we have to load the result set
            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                    WindowBox wbox;
                    if(answer == JOptionPane.YES_OPTION){
                        wbox = WindowBoxFactory.getXicPTMSitesWindowBoxV2(dataSet.getName());
                    } else if (answer == JOptionPane.NO_OPTION){
                        wbox = WindowBoxFactory.getXicPTMSitesWindowBoxV1(dataSet.getName());            
                    } else {
                        return;         
                    }
                    // open a window to display the window box
                    DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
                    win.open();
                    win.requestActive();
                    
                    // prepare window box
                    wbox.setEntryData(dataSet.getProject().getId(), new PTMDataset(dataSet));
                }
            };


            // ask asynchronous loading of data
            DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
            task.initLoadRsetAndRsm(dataSet);
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
        }
    }
    

    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {
        int nbSelectedNodes = selectedNodes.length;
        

        if (nbSelectedNodes <0) {
            setEnabled(false);
            return;
        }
        
        for (int i=0;i<nbSelectedNodes;i++) {
            AbstractNode node = selectedNodes[i];
            if (node.getType() != AbstractNode.NodeTypes.DATA_SET && node.getType() != AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS) {
                setEnabled(false);
                return;
            }

            DataSetNode dataSetNode = (DataSetNode) node;
            if (! dataSetNode.hasResultSummary()) {
                setEnabled(false);
                return;
            }
             // must be a quantitation XIC
            if (! dataSetNode.isQuantXIC()) {
                setEnabled(false);
                return;
            }            
        }

        
        setEnabled(true);
    }
    
    
}
