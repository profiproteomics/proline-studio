package fr.proline.studio.rsmexplorer.gui.calc.graphics;

import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.gui.AdvancedSelectionPanel;
import fr.proline.studio.parameter.MultiObjectParameter;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import fr.proline.studio.rsmexplorer.gui.calc.ProcessCallbackInterface;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphConnector;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphicGraphNode;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import java.util.ArrayList;

/**
 *
 * @author JM235353
 */
public class ParallelCoordinatesGraphic extends AbstractGraphic {

    private MultiObjectParameter m_columnsParameter = null;
    
    public ParallelCoordinatesGraphic(GraphPanel panel) {
        super(panel);
    }
    
    @Override
    public String getName() {
        return "Parallel Coordinates";
    }

    @Override
    public void inLinkDeleted() {
        super.inLinkDeleted();
        m_columnsParameter = null;
    }
    
    @Override
    public void process(GraphConnector[] graphObjects, GraphicGraphNode graphicGraphNode, ProcessCallbackInterface callback) {

        try {

            if (m_graphicsModelInterface != null) {
                return;
            }

            ArrayList<Integer> selectedColumnsList = (ArrayList<Integer>) m_columnsParameter.getAssociatedValues(true);
            int nbSelected = selectedColumnsList.size();

            int[] cols = new int[nbSelected];
            for (int i = 0; i < nbSelected; i++) {
                cols[i] = selectedColumnsList.get(i);
            }


            m_graphicsModelInterface = new LockedDataGraphicsModel(graphObjects[0].getGlobalTableModelInterface(), PlotType.PARALLEL_COORDINATES_PLOT, cols);
        } finally {
            callback.finished(graphicGraphNode);
        }
    }

    @Override
    public void generateDefaultParameters(GraphConnector[] graphObjects) {
        GlobalTableModelInterface model1 = graphObjects[0].getGlobalTableModelInterface();

        if (m_columnsParameter == null) {

            ArrayList<String> columnNamesArrayList = new ArrayList<>();
            ArrayList<Integer> columnIdsArrayList = new ArrayList<>();

            int nbColumns = model1.getColumnCount();
            for (int i = 0; i < nbColumns; i++) {
                Class c = model1.getDataColumnClass(i);
                if ((c.equals(String.class)) || (c.equals(Double.class)) || (c.equals(Float.class)) || (c.equals(Long.class)) || (c.equals(Integer.class))) {
                    columnNamesArrayList.add(model1.getDataColumnIdentifier(i));
                    columnIdsArrayList.add(i);
                }
            }

            int nb = columnNamesArrayList.size();

            boolean[] selection = new boolean[nb];
            for (int i = 0; i < nb; i++) {
                selection[i] = false;
            }

            Object[] columnNamesArray = columnNamesArrayList.toArray(new String[nb]);
            Object[] columnIdsArray = columnIdsArrayList.toArray(new Integer[nb]);

            m_columnsParameter = new MultiObjectParameter("MULTI_COLUMNS", "Columns Selection", "Selected Columns", "Unselected Columns", AdvancedSelectionPanel.class, columnNamesArray, columnIdsArray, selection, null);

        }     
                
                
        ParameterList parameterList1 = new ParameterList("graphic1");

        m_parameters = new ParameterList[1];
        m_parameters[0] = parameterList1;

        
        parameterList1.add(m_columnsParameter);
    }

    @Override
    public ParameterError checkParameters(GraphConnector[] graphObjects) {
        return null;
    }

    @Override
    public void userParametersChanged() {
        // nothing to do
    }


    @Override
    public AbstractGraphic cloneGraphic(GraphPanel p) {
        AbstractGraphic clone = new ParallelCoordinatesGraphic(p);
        clone.cloneInfo(this);
        return clone;
    }

    @Override
    public boolean calculationDone() {
        return (m_graphicsModelInterface != null);
    }

    @Override
    public boolean settingsDone() {
        if ((m_parameters==null) || (m_columnsParameter == null)) { 
            return false;
        }

        ArrayList<Integer> selectedColumnsList = (ArrayList<Integer>) m_columnsParameter.getAssociatedValues(true);
        
        if ((selectedColumnsList == null) || (selectedColumnsList.size()<2)) {
            return false;
        }

        return true;
    }
    
}
