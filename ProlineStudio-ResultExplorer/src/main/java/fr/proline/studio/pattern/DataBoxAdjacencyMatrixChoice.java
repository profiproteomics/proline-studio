package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseProteinsAndPeptidesTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.data.AdjacencyMatrixData;
import fr.proline.studio.rsmexplorer.adjacencymatrix.visualize.Component;
import fr.proline.studio.rsmexplorer.adjacencymatrix.visualize.DrawVisualization;
import fr.proline.studio.rsmexplorer.adjacencymatrix.visualize.MatrixSelectionPanel;

/**
 *
 * @author JM235353
 */
public class DataBoxAdjacencyMatrixChoice extends AbstractDataBox {
    
    private ResultSummary m_rsm = null;
    private boolean dataLoadedForRSM = false;
    
    public DataBoxAdjacencyMatrixChoice() {
        super(DataboxType.DataBoxAdjacencyMatrixChoice);
        
        // Name of this databox
        m_typeName = "Proteins Adjacency Matrices";
        m_description = "All Adjacency Matrices";
        
        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(ResultSummary.class, false);
        inParameter.addParameter(DProteinSet.class, false);
        registerInParameter(inParameter);
        
        
        // Register possible out parameters
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(DrawVisualization.class, false);
        outParameter.addParameter(Component.class, false);
        registerOutParameter(outParameter);
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
        
        
        final DProteinSet _proteinSet = (m_previousDataBox==null) ? null : (DProteinSet) m_previousDataBox.getData(false, DProteinSet.class);
        
        final AdjacencyMatrixData matrixData = new AdjacencyMatrixData();
        
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                ((MatrixSelectionPanel) m_panel).setData(matrixData, _proteinSet);
                
                if (finished) {
                    unregisterTask(taskId);
                }
            }
        };

        // ask asynchronous loading of data
        if (!dataLoadedForRSM) {
            registerTask(new DatabaseProteinsAndPeptidesTask(callback, getProjectId(), _rsm, matrixData));
            dataLoadedForRSM = true;
        } else {
            ((MatrixSelectionPanel) m_panel).setData(_proteinSet);
        }

    }
    
    @Override
    public void setEntryData(Object data) {
        m_panel.addSingleValue(data);
        
        if (data instanceof ResultSummary) {
            m_rsm = (ResultSummary) data;
            dataChanged();
        }
    }
    
        @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType!= null ) {
            if (parameterType.equals(Component.class)) {
                return ((MatrixSelectionPanel) m_panel).getCurrentComponent();
            }
            if (parameterType.equals(DrawVisualization.class)) {
                return ((MatrixSelectionPanel) m_panel).getDrawVisualization();
            }
        }
        return super.getData(getArray, parameterType);
    }
    
}

