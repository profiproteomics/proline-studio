package fr.proline.studio.pattern;

import fr.proline.studio.rsmexplorer.adjacencymatrix.visualize.Component;
import fr.proline.studio.rsmexplorer.adjacencymatrix.visualize.DrawVisualization;
import fr.proline.studio.rsmexplorer.adjacencymatrix.visualize.MatrixPanel;



/**
 *
 * @author JM235353
 */
public class DataBoxAdjacencyMatrix extends AbstractDataBox {
    
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
