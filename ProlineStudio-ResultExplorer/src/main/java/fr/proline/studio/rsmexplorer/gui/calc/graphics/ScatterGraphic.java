package fr.proline.studio.rsmexplorer.gui.calc.graphics;

import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.parameter.ObjectParameter;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import fr.proline.studio.rsmexplorer.gui.calc.graph.AbstractGraphObject;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphicGraphNode;
import fr.proline.studio.table.GlobalTableModelInterface;

/**
 * Definition of a scatter plot with its parameters for the data mixer
 * @author JM235353
 */
public class ScatterGraphic extends AbstractGraphic {

    
    private static final String SEL_COLS1 = "SEL_COLS1";
    private static final String SEL_COLS2 = "SEL_COLS2";
    
    private ObjectParameter m_columnsParameter1 = null;
    private ObjectParameter m_columnsParameter2 = null;
    
    public ScatterGraphic(GraphPanel panel) {
        super(panel);
    }
    
    @Override
    public String getName() {
        return "Scatter Plot";
    }

    @Override
    public void process(AbstractGraphObject[] graphObjects, GraphicGraphNode graphicGraphNode, boolean display) {
        
        Object o1 = m_columnsParameter1.getAssociatedObjectValue();
        Object o2 = m_columnsParameter2.getAssociatedObjectValue();
        
        m_graphicsModelInterface = new LockedDataGraphicsModel(graphObjects[0].getGlobalTableModelInterface(), PlotType.SCATTER_PLOT, (Integer) o1, (Integer) o2);
        if (display) {
            display(graphicGraphNode.getPreviousDataName(), getName());
        }
    }

    @Override
    public void generateDefaultParameters(AbstractGraphObject[] graphObjects) {
        GlobalTableModelInterface model1 = graphObjects[0].getGlobalTableModelInterface();
        int nbColumns = model1.getColumnCount();
        int nbColumnsKept = 0;
        for (int i = 0; i < nbColumns; i++) {
            Class c = model1.getDataColumnClass(i);
            if (c.equals(Integer.class) || c.equals(Float.class) || c.equals(Double.class)) {
                nbColumnsKept++;
            }
        }
        Object[] objectArray1 = new Object[nbColumnsKept];
        Object[] associatedObjectArray1 = new Object[nbColumnsKept];
        int iKept = 0;
        for (int i = 0; i < nbColumns; i++) {
            Class c = model1.getDataColumnClass(i);
            if (c.equals(Integer.class) || c.equals(Float.class) || c.equals(Double.class)) {
                objectArray1[iKept] = model1.getColumnName(i);
                associatedObjectArray1[iKept] = i;
                iKept++;
            }
        }
        
        m_columnsParameter1 = new ObjectParameter(SEL_COLS1, "Column for X Axis", null, objectArray1, associatedObjectArray1, -1, null);
        m_columnsParameter2 = new ObjectParameter(SEL_COLS2, "Column for Y Axis", null, objectArray1, associatedObjectArray1, -1, null);
     
        ParameterList parameterList1 = new ParameterList("graphic1");
        ParameterList parameterList2 = new ParameterList("graphic2");
        m_parameters = new ParameterList[2];
        m_parameters[0] = parameterList1;
        m_parameters[1] = parameterList2;
        
        parameterList1.add(m_columnsParameter1);
        parameterList2.add(m_columnsParameter2);
    }

    @Override
    public ParameterError checkParameters() {
        return null;
    }

    @Override
    public void userParametersChanged() {
        // nothing to do
    }

    @Override
    public AbstractGraphic cloneGraphic(GraphPanel p) {
        return new ScatterGraphic(p);
    }

    @Override
    public boolean calculationDone() {
        return true;
    }

    @Override
    public boolean settingsDone() {
        if (m_columnsParameter1 == null) {
            return false;
        }

        Object o1 = m_columnsParameter1.getAssociatedObjectValue();
        Object o2 = m_columnsParameter2.getAssociatedObjectValue();
        if ((o1 == null) || (o2 == null)) {
            return false;
        }

        return true;
    }
    
}
