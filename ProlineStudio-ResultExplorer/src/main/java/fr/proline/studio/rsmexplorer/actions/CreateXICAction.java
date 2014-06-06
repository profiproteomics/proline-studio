package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.RunXICTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.CreateXICDialog;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
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
            
             //User specified experimental design
            HashMap<String, ArrayList<String>> _samplesByGroup = new HashMap<>();
            HashMap<String, ArrayList<String>> _samplesAnalysisBySample = new HashMap<>();
            HashMap<String, Long> _rsmIdBySampleAnalysis = new HashMap<>();
            DataSetData _quantiDS  = null;
            
            String errorMsg = null;
            if(dialog.getDesignRSMNode() == null){
                errorMsg = "No experimental design defined";
                
            } else if(!DataSetData.class.isInstance(dialog.getDesignRSMNode().getData())){
                errorMsg = "Invalide Quantitation Dataset specified ";                    
                
            } else {
                //*** Get experimental design values                
                _quantiDS = (DataSetData)dialog.getDesignRSMNode().getData();
                                
                Enumeration xicGrps = dialog.getDesignRSMNode().children();                  
                //Iterate over Groups
                while(xicGrps.hasMoreElements()&& errorMsg==null){
                    RSMNode grpNode = (RSMNode) xicGrps.nextElement();
                    String grpName = grpNode.getData().getName();

                    //Iterate over Samples
                    Enumeration grpSpls  = grpNode.children();      
                    ArrayList<String> splNames = new ArrayList<>();
                    while(grpSpls.hasMoreElements() && errorMsg==null){

                        RSMNode splNode = (RSMNode) grpSpls.nextElement();
                        String sampleName = splNode.getData().getName();
                        splNames.add(sampleName);
                        
                        //Iterate over SampleAnalysis
                        Enumeration identRSMs  = splNode.children();      
                        ArrayList<String> splAnalysisNames = new ArrayList<>();
                        while(identRSMs.hasMoreElements()){
                            //VD TODO TEST child type
                            RSMNode qChannelNode = (RSMNode) identRSMs.nextElement();
                            String spAnalysisName = qChannelNode.getData().getName();
                            splAnalysisNames.add(spAnalysisName);
                            if(!DataSetData.class.isInstance(qChannelNode.getData())){
                                errorMsg = "Invalide Sample Analysis specified ";
                                break;
                            }
                            _rsmIdBySampleAnalysis.put(spAnalysisName, ((DataSetData)qChannelNode.getData()).getDataset().getResultSummaryId());                            
                        }
                        _samplesAnalysisBySample.put(sampleName, splAnalysisNames);
                    } //End go through group's sample
                    _samplesByGroup.put(grpName,splNames);
                }//End go through Grp
            }
            
            Map<String,Object> params = dialog.getQuantiParameters();
            if(params == null)
                errorMsg = "Null Quantitation parameter !s";      
            
            if(errorMsg != null) {
                JOptionPane.showMessageDialog(dialog, errorMsg, "Warning", JOptionPane.ERROR_MESSAGE);
                return;                
            }
            
            m_logger.debug(" Will Compute XIC Quanti with "+_samplesByGroup.size()+" groups regrouping "+_rsmIdBySampleAnalysis.size()+" identification summary. ");              
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
    
            RunXICTask task = new RunXICTask(xicCallback, pID, _quantiDS.getName(), params,  _samplesByGroup, _samplesAnalysisBySample, _rsmIdBySampleAnalysis, _xicQuantiDataSetId);          
            AccessServiceThread.getAccessServiceThread().addTask(task);
            
         } //End OK entered         
     }
    
    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {
         setEnabled(true);
    }
    
}
