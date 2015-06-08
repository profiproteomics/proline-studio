package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseProteinsAndPeptidesTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import java.awt.event.ActionListener;
//import fr.proline.studio.rsmexplorer.adjacentmatrix.visualize.MatrixSelectionPanel;


/**
 *
 * @author JM235353
 */
public class DataBoxAdjacencyMatrix extends AbstractDataBox {
    
    private ResultSummary m_rsm = null;
    
    public DataBoxAdjacencyMatrix() {
        super(DataboxType.DataBoxAdjacentMatrix);
        
        // Name of this databox
        m_typeName = "Proteins Adjacency Matrix";
        m_description = "All Proteins and Peptides put in an Adjacency Matrix";
        
        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(ResultSummary.class, false);
        registerInParameter(inParameter);
        
        // Register possible out parameters
        //JPM.TODO
    }
    
    @Override
    public void createPanel() {
        
        //JPM.TODO
        MatrixSelectionPanel p = new MatrixSelectionPanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        m_panel = p;
    }
    
    @Override
    public void dataChanged() {

        final ResultSummary _rsm = (m_rsm != null) ? m_rsm : (ResultSummary) m_previousDataBox.getData(false, ResultSummary.class);

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                if (subTask == null) {

                    //JPM.TODO
                    
                    /*DPeptideMatch[] peptideMatchArray = _rsm.getTransientData().getPeptideMatches();
                    long[] peptideMatchIdArray = _rsm.getTransientData().getPeptideMatchesId();
                    ((PeptideMatchPanel) m_panel).setData(taskId, peptideMatchArray, peptideMatchIdArray, finished);*/
                }

                if (finished) {
                    unregisterTask(taskId);
                }
            }
        };

        // ask asynchronous loading of data
        registerTask(new DatabaseProteinsAndPeptidesTask(callback, getProjectId(), _rsm));

    }
    
    @Override
    public void setEntryData(Object data) {
        if (data instanceof ResultSummary) {
            m_rsm = (ResultSummary) data;
            dataChanged();
        }
    }
    
    
    // Stub for the moment
    public class MatrixSelectionPanel extends HourglassPanel implements DataBoxPanelInterface {

        private AbstractDataBox m_dataBox;

        @Override
        public void setDataBox(AbstractDataBox dataBox) {
            m_dataBox = dataBox;
        }

        @Override
        public AbstractDataBox getDataBox() {
            return m_dataBox;
        }

        @Override
        public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public ActionListener getAddAction(SplittedPanelContainer splittedPanel) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public ActionListener getSaveAction(SplittedPanelContainer splittedPanel) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }



    }
}
