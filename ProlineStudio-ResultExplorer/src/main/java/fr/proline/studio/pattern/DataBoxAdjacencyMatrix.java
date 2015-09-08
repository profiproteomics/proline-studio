package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseProteinsAndPeptidesTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.data.AdjacencyMatrixData;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.rsmexplorer.adjacencymatrix.visualize.Component;
import fr.proline.studio.rsmexplorer.adjacencymatrix.visualize.DrawVisualization;
import fr.proline.studio.rsmexplorer.adjacencymatrix.visualize.MatrixPanel;
import fr.proline.studio.rsmexplorer.adjacencymatrix.visualize.MatrixSelectionPanel;
import java.awt.event.ActionListener;
//import fr.proline.studio.rsmexplorer.adjacentmatrix.visualize.MatrixSelectionPanel;


/**
 *
 * @author JM235353
 */
public class DataBoxAdjacencyMatrix extends AbstractDataBox {
    
    private ResultSummary m_rsm = null;
    
    public DataBoxAdjacencyMatrix() {
        super(DataboxType.DataBoxAdjacencyMatrix);
        
        // Name of this databox
        m_typeName = "Proteins Adjacency Matrix";
        m_description = "Proteins Adjacency Matrix";
        
        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(DrawVisualization.class, false);
        inParameter.addParameter(Component.class, false);
        registerInParameter(inParameter);
        
        
        // Register possible out parameters

    }
    
    @Override
    public void createPanel() {
        
        //JPM.TODO
        MatrixPanel p = new MatrixPanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        m_panel = p;
    }
    
    @Override
    public void dataChanged() {

        Component component = (Component) m_previousDataBox.getData(false, Component.class);
        DrawVisualization drawVisualization = (DrawVisualization) m_previousDataBox.getData(false, DrawVisualization.class);
        ((MatrixPanel) m_panel).setData(component, drawVisualization);

    }

    
}
