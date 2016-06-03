package fr.proline.studio.rsmexplorer.gui.calc.graphics;

import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.parameter.ObjectParameter;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import fr.proline.studio.rsmexplorer.gui.calc.ProcessCallbackInterface;
import fr.proline.studio.rsmexplorer.gui.calc.graph.AbstractConnectedGraphObject;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphicGraphNode;
import fr.proline.studio.table.GlobalTableModelInterface;

/**
 * Definition of a scatter plot with its parameters for the data analyzer
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
    public void inLinkDeleted() {
        super.inLinkDeleted();
        m_columnsParameter1 = null;
        m_columnsParameter2 = null;
    }
    
    @Override
    public void process(AbstractConnectedGraphObject[] graphObjects, GraphicGraphNode graphicGraphNode, ProcessCallbackInterface callback) {
        
        try {

            if (m_graphicsModelInterface != null) {
                return;
            }

            Object o1 = m_columnsParameter1.getAssociatedObjectValue();
            Object o2 = m_columnsParameter2.getAssociatedObjectValue();

            m_graphicsModelInterface = new LockedDataGraphicsModel(graphObjects[0].getGlobalTableModelInterface(), PlotType.SCATTER_PLOT, (Integer) o1, (Integer) o2);
        } finally {
            callback.finished(graphicGraphNode);
        }
    }

    @Override
    public void generateDefaultParameters(AbstractConnectedGraphObject[] graphObjects) {
        GlobalTableModelInterface model1 = graphObjects[0].getGlobalTableModelInterface();
        int bestXColumnIndex = model1.getBestXAxisColIndex(PlotType.SCATTER_PLOT);
        int bestYColumnIndex = model1.getBestYAxisColIndex(PlotType.SCATTER_PLOT);
        int selectedIndexX = -1;
        int selectedIndexY = -1;
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
                if (i == bestXColumnIndex) {
                    selectedIndexX = iKept;
                } else if (i == bestYColumnIndex) {
                    selectedIndexY = iKept;
                }
                iKept++;
            }
        }
        
        m_columnsParameter1 = new ObjectParameter(SEL_COLS1, "Column for X Axis", null, objectArray1, associatedObjectArray1, selectedIndexX, null);
        m_columnsParameter2 = new ObjectParameter(SEL_COLS2, "Column for Y Axis", null, objectArray1, associatedObjectArray1, selectedIndexY, null);
     
        ParameterList parameterList1 = new ParameterList("graphic1");
        ParameterList parameterList2 = new ParameterList("graphic2");
        m_parameters = new ParameterList[2];
        m_parameters[0] = parameterList1;
        m_parameters[1] = parameterList2;
        
        parameterList1.add(m_columnsParameter1);
        parameterList2.add(m_columnsParameter2);
    }

    @Override
    public ParameterError checkParameters(AbstractConnectedGraphObject[] graphObjects) {
        return null;
    }

    @Override
    public void userParametersChanged() {
        // nothing to do
    }

    @Override
    public AbstractGraphic cloneGraphic(GraphPanel p) {
        AbstractGraphic clone = new ScatterGraphic(p);
        clone.cloneInfo(this);
        return clone;
    }

    @Override
    public boolean calculationDone() {
        return (m_graphicsModelInterface != null);
    }

    @Override
    public boolean settingsDone() {
        if ((m_parameters==null) || (m_columnsParameter1 == null)) {
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
