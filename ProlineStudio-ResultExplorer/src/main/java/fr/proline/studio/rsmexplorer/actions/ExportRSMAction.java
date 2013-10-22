/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions;

import fr.proline.core.orm.uds.Dataset;
import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.ExportRSMTask;
import fr.proline.studio.rsmexplorer.node.RSMDataSetNode;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author VD225637
 */
public class ExportRSMAction extends AbstractRSMAction {
     protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
     
    public ExportRSMAction(){
        super("TEST EXPORT");        
    }
    
        @Override
    public void actionPerformed(RSMNode[] selectedNodes, int x, int y) {

        // only one node selected for this action        
        final RSMDataSetNode dataSetNode = (RSMDataSetNode) selectedNodes[0];
        final Dataset d = dataSetNode.getDataset();
        
        // used as out parameter for the service
        final String[] _filePath = new String[1];
        
        AbstractServiceCallback callback = new AbstractServiceCallback() {

                    @Override
                    public boolean mustBeCalledInAWT() {
                        return true;
                    }

                    @Override
                    public void run(boolean success) {
                        if (success) {
                            m_logger.info("OK "+_filePath[0]);

                        } else {
                            m_logger.info("ERROR");
                        } 
                    }
                       
                };

    
            ExportRSMTask task = new ExportRSMTask(callback, dataSetNode.getDataset(), _filePath);
           AccessServiceThread.getAccessServiceThread().addTask(task); 
     


    }
     
    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {

        int nbSelectedNodes = selectedNodes.length;

        // Only one at the time
        if (nbSelectedNodes != 1) {
            setEnabled(false);
            return;
        }

        RSMNode node = selectedNodes[0];
        RSMNode.NodeTypes nodeType = node.getType();
        if ((nodeType != RSMNode.NodeTypes.DATA_SET) && (nodeType != RSMNode.NodeTypes.PROJECT )) {
            setEnabled(false);
            return;
        }

        setEnabled(true);
    }
    
}
