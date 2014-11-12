package fr.proline.studio.pattern.xic;

import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.uds.QuantitationChannel;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DMasterQuantitationChannel;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.GroupParameter;
import fr.proline.studio.rsmexplorer.gui.xic.XicProteinSetPanel;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JM235353
 */
public class DataboxXicProteinSet extends AbstractDataBox {

    private DDataset m_dataset;
    private List<DMasterQuantProteinSet> m_masterQuantProteinSetList ;
    
    public DataboxXicProteinSet() { 
        super(DataboxType.DataboxXicProteinSet);
        
        // Name of this databox
        m_name = "XIC Protein Sets";
        m_description = "All Protein Sets of a XIC";

        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(DDataset.class, false); //JPM.TODO
        registerInParameter(inParameter);


        // Register possible out parameters
        // One or Multiple ProteinSet
        /*GroupParameter outParameter = new GroupParameter(); //JPM.TODO
        outParameter.addParameter(DProteinSet.class, true);
        outParameter.addParameter(ResultSummary.class, false);
        registerOutParameter(outParameter);*/

    }
    
     @Override
    public void createPanel() {
        XicProteinSetPanel p = new XicProteinSetPanel();
        p.setName(m_name);
        p.setDataBox(this);
        m_panel = p;
    }

    @Override
    public void dataChanged() {


      

        final int loadingId = setLoading();

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                if (subTask == null) {
                    // list quant Channels
                    List<DQuantitationChannel> listQuantChannel = new ArrayList();
                    if (m_dataset.getMasterQuantitationChannels() != null && !m_dataset.getMasterQuantitationChannels().isEmpty()) {
                        DMasterQuantitationChannel masterChannel = m_dataset.getMasterQuantitationChannels().get(0);
                        listQuantChannel = masterChannel.getQuantitationChannels();
                    }
                    DQuantitationChannel[] quantitationChannelArray = new DQuantitationChannel[listQuantChannel.size()];
                    listQuantChannel.toArray(quantitationChannelArray);
                    // proteins set 
                    DMasterQuantProteinSet[] masterQuantProteinSetArray = new DMasterQuantProteinSet[m_masterQuantProteinSetList.size()];
                    m_masterQuantProteinSetList.toArray(masterQuantProteinSetArray);
                    ((XicProteinSetPanel) m_panel).setData(taskId, quantitationChannelArray, masterQuantProteinSetArray, finished);
                } else {
                    ((XicProteinSetPanel) m_panel).dataUpdated(subTask, finished);
                }

                setLoaded(loadingId);
                
                if (finished) {
                    unregisterTask(taskId);
                }
            }
        };


        // ask asynchronous loading of data
        m_masterQuantProteinSetList = new ArrayList();
        DatabaseLoadXicMasterQuantTask task = new DatabaseLoadXicMasterQuantTask(callback);
        task.initLoadProteinSets(getProjectId(), m_dataset, m_masterQuantProteinSetList);
        //Long taskId = task.getId();
        /*if (m_previousTaskId != null) {
            // old task is suppressed if it has not been already done
            AccessDatabaseThread.getAccessDatabaseThread().abortTask(m_previousTaskId);
        }*/
        //m_previousTaskId = taskId;
        registerTask(task);

    }
    //private Long m_previousTaskId = null;
    
    
    @Override
    public void setEntryData(Object data) {
        m_dataset = (DDataset) data;
        dataChanged();
    }
}
