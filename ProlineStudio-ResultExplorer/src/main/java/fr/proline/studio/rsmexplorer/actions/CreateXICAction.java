package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.RunXICTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.CreateXICDialog;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import java.util.*;
import javax.swing.JOptionPane;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author JM235353
 */
public class CreateXICAction extends AbstractRSMAction {
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    
    public CreateXICAction() {
        super(NbBundle.getMessage(ConnectAction.class, "CTL_CreateXIC"), false);
    }
    
     @Override
    public void actionPerformed(RSMNode[] selectedNodes, int x, int y) {
         
         if( ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject() == null){
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), "A project should be selected !", "Warning", JOptionPane.ERROR_MESSAGE);
            return; 
         }
         
         Long pID = ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject().getId();
         CreateXICDialog dialog =  CreateXICDialog.getDialog(WindowManager.getDefault().getMainWindow());
         dialog.reinit();
         dialog.setLocation(x, y);
         dialog.setVisible(true);
         final Long[] _xicQuantiDataSetId= new Long[1];
         
         if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            
             //User specified experimental design dataset
            DataSetData _quantiDS  = null;
            Map<String,Object> expParams = null;
            
            String errorMsg = null;
            if(dialog.getDesignRSMNode() == null){
                errorMsg = "No experimental design defined";
                
            } else if(!DataSetData.class.isInstance(dialog.getDesignRSMNode().getData())){
                errorMsg = "Invalide Quantitation Dataset specified ";                    
                
            } else {
                //*** Get experimental design values                
                _quantiDS = (DataSetData)dialog.getDesignRSMNode().getData();
                                
                try{
                    expParams = dialog.getDesignParameters();
                } catch (IllegalAccessException iae){
                    errorMsg = iae.getMessage();
                }
            }
            
            Map<String,Object> quantParams = dialog.getQuantiParameters();
            if(quantParams == null)
                errorMsg = "Null Quantitation parameters !";      
            
            if(errorMsg != null) {
                JOptionPane.showMessageDialog(dialog, errorMsg, "Warning", JOptionPane.ERROR_MESSAGE);
                return;                
            }
            
            m_logger.debug(" Will Compute XIC Quanti with on "+ ((List)expParams.get("biological_samples")).size()+"samples.");              

            // CallBack for Xic Quantitation Service
            AbstractServiceCallback xicCallback = new AbstractServiceCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success) {
                    if (success) {
                        m_logger.debug(" XIC SUCCESS : "+_xicQuantiDataSetId[0]);
                        
                    } else {
                        m_logger.debug(" XIC ERROR ");

                    }
                }
            };
    
            RunXICTask task = new RunXICTask(xicCallback, pID, _quantiDS.getName(), quantParams,  expParams, _xicQuantiDataSetId);          
            AccessServiceThread.getAccessServiceThread().addTask(task);
            
         } //End OK entered         
     }
    
    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {
         setEnabled(true);
    }
    
}
