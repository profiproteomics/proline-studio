package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.data.ptm.PTMDataset;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.Window;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author JM235353
 */
public class DisplayPTMSitesAction  extends AbstractRSMAction {

    public DisplayPTMSitesAction(AbstractTree tree) {
       super(NbBundle.getMessage(DisplayPTMSitesAction.class, "CTL_PtmSiteProtein"), tree);
    }
    
    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {
//        int answer= JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(), "Do you want to view Protein Sites V2 ?");
        DisplayPTMSiteDialog dialog = new DisplayPTMSiteDialog(WindowManager.getDefault().getMainWindow());
        dialog.setLocation(x, y);
        dialog.setVisible(true);        
        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            int nbNodes = selectedNodes.length;
            for (int i = 0; i < nbNodes; i++) {
                DataSetNode dataSetNode = (DataSetNode) selectedNodes[i];

                actionImpl(dataSetNode, dialog.getServiceVersion());
            }
        }
    }
    
    private void actionImpl(DataSetNode dataSetNode, String serviceVersion) {
        
        final DDataset dataSet = ((DataSetData) dataSetNode.getData()).getDataset();
        
        if (!dataSetNode.hasResultSummary()) {
            return; // should not happen
        }
        
        ResultSummary rsm = dataSetNode.getResultSummary();
        if (rsm != null) {

            // prepare window box
            WindowBox wbox;
            if (serviceVersion.equals("2.0")) {
                wbox = WindowBoxFactory.getPTMSitesWindowBoxV2(dataSet.getName());
            } else {
                wbox = WindowBoxFactory.getPTMSitesWindowBoxV1(dataSet.getName());            
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
                    
                    WindowBox wbox ;
                    if (serviceVersion.equals("2.0")) {
                        wbox = WindowBoxFactory.getPTMSitesWindowBoxV2(dataSet.getName());
                    } else{
                        wbox = WindowBoxFactory.getPTMSitesWindowBoxV1(dataSet.getName());            
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
        }

        
        setEnabled(true);
    }
    
    class DisplayPTMSiteDialog extends DefaultDialog {
        
       JComboBox<String>  m_serviceVersionCbx;
                
        public DisplayPTMSiteDialog(Window parent){
           super(parent, Dialog.ModalityType.APPLICATION_MODAL); 
            setTitle("Display PTM sites");
            setHelpHeaderText("Select the version of PTM identification service that was used: \n<br>"+
            " &bull; v1.0 a list of all identified sites was generated \n"+
            " &bull; v2.0 a PTM dataset in which sites are clusterized has been generated.\n<br>");
            initInternalPanel();
            pack();           
        }
        
        private void initInternalPanel() {
            JPanel internalPanel = new JPanel();    
            internalPanel.setLayout(new java.awt.GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new java.awt.Insets(15, 15, 15, 15);
            c.gridx = 0;
            c.gridy = 0;            
            
            JLabel label = new JLabel("Service version:");     
            internalPanel.add(label, c);
            String[] versions = new String[] {"Ptm Sites (v1.0)", "Ptm Dataset (v2.0)"};
            m_serviceVersionCbx = new JComboBox(versions);
             c.gridx++;
            internalPanel.add(m_serviceVersionCbx, c);
            setInternalComponent(internalPanel);            
        }
        
        public String getServiceVersion() {
            return m_serviceVersionCbx.getItemAt(m_serviceVersionCbx.getSelectedIndex()).contains("2.0") ? "2.0" : "1.0";
        }
        
    }
}
