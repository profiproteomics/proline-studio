package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.jms.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.AbstractServiceTask;
import fr.proline.studio.dpm.task.GenerateMSDiagReportTask;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import fr.proline.studio.rsmexplorer.gui.RsetMSDiagPanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RSMs Weighted Spectral Count result DataBox.
 *
 * @author JM235353
 */
public class DataBoxRsetMSDiag extends AbstractDataBox {

    public HashMap<String, String> m_message_back = null; //= "if this message is shown then msdiag data not succesfully passed into...";
    public ArrayList<Object> m_messages_back;
    private ResultSet m_rset = null;

    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");

    /**
     *
     * @param name : title of the created Windows
     * @param readData : specify if the spectral count to display has to be
     * retrieve from computing (false) or read back operation (true)
     * @return
     */
    public DataBoxRsetMSDiag(HashMap<String, String> resultMessageHashMap) {
        super(DataboxType.DataboxRsetMSDiag);

        m_messages_back = new ArrayList<>(2); // will contain the return data for msdiag (0: settings: 1:results)

        // Name of this databox
        m_typeName = "Statistics";
        m_description = "Statistical results";

        m_messages_back.add(resultMessageHashMap); // first element is the settings (as hashmap type)

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
        p.setName(m_typeName);
        p.setDataBox(this);
        m_panel = p;
    }

    @Override
    public void dataChanged() {

        final int loadingId = setLoading(true);

        ResultSet _rset = (m_rset != null) ? m_rset : (ResultSet) m_previousDataBox.getData(false, ResultSet.class);

        long rSetId = _rset.getId();

        Map<String, Object> parameters = new HashMap<>();
        String scoreWindow = ((HashMap<String, String>) m_messages_back.get(0)).get("score.windows");
        parameters.put("Score window", scoreWindow);
        //--
        String maxRank = ((HashMap<String, String>) m_messages_back.get(0)).get("max.rank");
        parameters.put("Max rank", maxRank);
        //--
        String scanGroupSize = "1"; // disabled currently as no report needing this parameter (will change soon)
        if (((HashMap<String, String>) m_messages_back.get(0)).containsKey("scan.groups.size")) {
            ((HashMap<String, String>) m_messages_back.get(0)).get("scan.groups.size");
        }
        parameters.put("Scan groups size", scanGroupSize);

    	//--
        if (JMSConnectionManager.getJMSConnectionManager().isJMSDefined()) {
            AbstractJMSCallback callback = new AbstractJMSCallback() {
                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success) {
                    m_logger.debug("  get MSDiag data");
                    if (success) {
                        ((RsetMSDiagPanel) m_panel).setData((String) m_messages_back.get(1)); // send last element containing JSON information (data to be represented)
                    } else {
                        ((RsetMSDiagPanel) m_panel).setData(null);
                    }

                    setLoaded(loadingId);
                }
            };

            fr.proline.studio.dpm.task.jms.GenerateMSDiagReportTask task = new fr.proline.studio.dpm.task.jms.GenerateMSDiagReportTask(callback, getProjectId(), rSetId, parameters, m_messages_back);
            ((RsetMSDiagPanel) m_panel).setData("task running...please wait.(or come back later)");
            AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
        } else {

            AbstractServiceCallback callback = new AbstractServiceCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success) {
                    m_logger.debug("  get MSDiag data");
                    if (success) {

                        ((RsetMSDiagPanel) m_panel).setData((String) m_messages_back.get(1)); // send last element containing JSON information (data to be represented)

                    } else {
                        ((RsetMSDiagPanel) m_panel).setData(null);

                    }

                    setLoaded(loadingId);
                }
            };

            AbstractServiceTask task = new GenerateMSDiagReportTask(callback, getProjectId(), rSetId, parameters, m_messages_back);

            ((RsetMSDiagPanel) m_panel).setData("task running...please wait.(or come back later)");

            AccessServiceThread.getAccessServiceThread().addTask(task);
        }
    }

    @Override
    public void setEntryData(Object data) {
        
        m_panel.addSingleValue(data);
        
        if (data instanceof ResultSet) {
            m_rset = (ResultSet) data;
            dataChanged();
        } else if (data instanceof ResultSummary) {
            m_rset = ((ResultSummary) data).getResultSet();
        }
    }

}
