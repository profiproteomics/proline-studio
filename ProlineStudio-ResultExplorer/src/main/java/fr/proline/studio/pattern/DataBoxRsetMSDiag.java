package fr.proline.studio.pattern;


import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.uds.Aggregation;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.DatabaseSearchPeptideInstanceTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.data.SpectralCountResultData;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.AbstractServiceTask;
import fr.proline.studio.dpm.task.GenerateMSDiagReportTask;
import fr.proline.studio.dpm.task.RetrieveSpectralCountTask;
import fr.proline.studio.dpm.task.SpectralCountTask;
import fr.proline.studio.rsmexplorer.actions.identification.SpectralCountAction;
import fr.proline.studio.rsmexplorer.gui.PeptideMatchPanel;
import fr.proline.studio.rsmexplorer.gui.RsetMSDiagPanel;
import fr.proline.studio.rsmexplorer.tree.quantitation.QuantitationTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.stats.ValuesForStatsAbstract;

import java.util.ArrayList;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultTreeModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RSMs Weighted Spectral Count result DataBox.
 * @author JM235353
 */
public class DataBoxRsetMSDiag extends AbstractDataBox {


    public String m_message_back = "if this message is shown then msdiag data not succesfully passed into...";
    public ArrayList<String> m_messages_back;
    private ResultSet m_rset = null;
    
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    
     /**
     * 
     * @param name : title of the created Windows 
     * @param readData : specify if the spectral count to display has to be retrieve from computing (false)
     * or read back operation (true)
     * @return 
     */
    public DataBoxRsetMSDiag(String resultMessage/*boolean readData*/) {
        super(DataboxType.DataboxRsetMSDiag);
        
        m_messages_back = new ArrayList<String>(0);
//        m_messages_back.add("arraylist element 1");
//        m_messages_back.add("arraylist element 2");
        // Name of this databox
        m_name = "MSDiag databox";
        m_description = "MSDiag results";
        m_message_back = resultMessage;
        m_messages_back.add(m_message_back);
        
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(ResultSet.class, false);
        registerInParameter(inParameter);
        
        // Register possible out parameters
        // One or Multiple ProteinMatch
//        GroupParameter outParameter = new GroupParameter();
//        outParameter.addParameter(ResultSet.class, false);
//        registerOutParameter(outParameter);
        
    }
    
    @Override
    public void createPanel() {
        RsetMSDiagPanel p = new RsetMSDiagPanel("please wait, retreiving data from server...");
        p.setName(m_name);
        p.setDataBox(this);
        m_panel = p;
    }

 
    @Override
    public void dataChanged() {

    	
    	final int loadingId = setLoading(true); 
        
    	 
        AbstractServiceCallback callback = new AbstractServiceCallback() {
            
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success) {
                m_logger.debug("  get MSDiag data");
                if (success) {
  
                	int size = m_messages_back.size();
                    //((RsetMSDiagPanel)m_panel).setData("nb messages: " + m_messages_back.size() + "\n element (" + size + ")= " + m_messages_back.get(size-1));
                	((RsetMSDiagPanel)m_panel).setData(m_messages_back.get(size-1)); // send last element containing JSON information (data to be represented)
                                      
                } else {
                    ((RsetMSDiagPanel)m_panel).setData(null);
                    
                    
                }
                
                setLoaded(loadingId);
            }
        };
        AbstractServiceTask task;
        
      	ResultSet _rset = (m_rset!=null) ? m_rset : (ResultSet) m_previousDataBox.getData(false, ResultSet.class);

    	long rSetId = _rset.getId();
    	
    	task = new GenerateMSDiagReportTask(callback,  getProjectId(), rSetId, m_messages_back);
    	
    	//int size = m_messages_back.size();
    	//JOptionPane.showMessageDialog(null,"message back from service (size: " + size + "): " + m_messages_back.get(size-1) + "\n" 
		//		 + m_messages_back.get(size-1), "DataBoxRsetMSDiag",1);          
    	//((RsetMSDiagPanel)m_panel).setData(m_messages_back.get(size-1));
    	((RsetMSDiagPanel)m_panel).setData("task running...please wait.(or come back later)");
        
        AccessServiceThread.getAccessServiceThread().addTask(task);

    }
    
    
    @Override
    public void setEntryData(Object data) {
        if (data instanceof ResultSet) {
           // JOptionPane.showMessageDialog(null,"l 247 (setEntryData) running with data " , "m_rset = " + ((ResultSet) data).getName(),1);          
            m_rset = (ResultSet) data;
            dataChanged();
        } else if (data instanceof ResultSummary) {
         //   JOptionPane.showMessageDialog(null,"l 251 (setEntryData) running with data " , "m_rset = " + ((ResultSet) data).getName(),1);          
            m_rset = ((ResultSummary) data).getResultSet();
        }
    }
    
    
}
