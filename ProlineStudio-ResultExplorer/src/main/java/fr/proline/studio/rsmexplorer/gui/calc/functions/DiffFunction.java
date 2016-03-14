 package fr.proline.studio.rsmexplorer.gui.calc.functions;

import fr.proline.studio.comparedata.AbstractJoinDataModel;
import fr.proline.studio.parameter.BooleanParameter;
import fr.proline.studio.parameter.ObjectParameter;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.python.data.Table;
import fr.proline.studio.python.interpreter.CalcError;
import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import fr.proline.studio.rsmexplorer.gui.calc.ProcessCallbackInterface;
import fr.proline.studio.rsmexplorer.gui.calc.graph.AbstractGraphObject;
import fr.proline.studio.rsmexplorer.gui.calc.graph.FunctionGraphNode;
import fr.proline.studio.table.GlobalTableModelInterface;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;

/**
 * Diff Function for the data analyzer
 * @author JM235353
 */
public class DiffFunction extends AbstractFunction {

    private static final String JOIN_COL1 = "JOIN_COL1";
    private static final String JOIN_COL2 = "JOIN_COL2";
    private static final String SOURCE_COL = "SOURCE_COL";

    private ParameterList m_parameterList;
    private ObjectParameter m_paramColumn1;
    private ObjectParameter m_paramColumn2;
    private BooleanParameter m_addSourceCol;
    
    public DiffFunction(GraphPanel panel) {
        super(panel);
    }
    
    @Override
    public void inLinkDeleted() {
        super.inLinkDeleted();
        m_paramColumn1 = null;
        m_paramColumn2 = null;
    }
    
    @Override
    public String getName() {
        return "Diff";
    }

    @Override
    public int getNumberOfInParameters() {
        return 2;
    }

    @Override
    public boolean settingsDone() {
        if (m_parameterList == null) {
            return false;
        }
        return ((m_paramColumn1 != null) && (m_paramColumn2 != null));
    }
    
    @Override
    public boolean calculationDone() {
        if (m_globalTableModelInterface != null) {
            return true;
        }
        return false;
    }
    
    @Override
    public AbstractFunction cloneFunction(GraphPanel panel) {
        return new DiffFunction(panel);
    }
    
    @Override
    public void process(AbstractGraphObject[] graphObjects, FunctionGraphNode functionGraphNode, ProcessCallbackInterface callback) {

        try {

            // check if we have already processed
            if (m_globalTableModelInterface != null) {
                return;
            }

            setCalculating(true);
            setInError(false, null);

            try {
                Table t1 = new Table(graphObjects[0].getGlobalTableModelInterface());
                graphObjects[0].getGlobalTableModelInterface().setName(graphObjects[0].getFullName());
                Table t2 = new Table(graphObjects[1].getGlobalTableModelInterface());
                graphObjects[1].getGlobalTableModelInterface().setName(graphObjects[1].getFullName());

                Table diffTable;
                if ((m_paramColumn1 != null) && (m_paramColumn2 != null)) {
                    Integer key1 = (Integer) m_paramColumn1.getAssociatedObjectValue();
                    Integer key2 = (Integer) m_paramColumn2.getAssociatedObjectValue();
                    Boolean showSourceColumn = (Boolean) m_addSourceCol.getObjectValue();
                    diffTable = Table.diff(t1, t2, key1, key2, showSourceColumn);
                } else {
                    diffTable = Table.diff(t1, t2);
                }
                m_globalTableModelInterface = diffTable.getModel();
            } catch (Exception e) {
                setInError(new CalcError(e, null, -1));
            }
            setCalculating(false);

        } finally {
            callback.finished(functionGraphNode);
        }

    }

    @Override
    public void askDisplay(FunctionGraphNode functionGraphNode) {
        display(functionGraphNode.getPreviousDataName(), getName());
    }
    
    @Override
    public void generateDefaultParameters(AbstractGraphObject[] graphObjects) {

        GlobalTableModelInterface modelForDefaultKey = m_globalTableModelInterface;
        
        if (modelForDefaultKey == null) {
            Table t1 = new Table(graphObjects[0].getGlobalTableModelInterface());
            Table t2 = new Table(graphObjects[1].getGlobalTableModelInterface());
            Table diffTable = Table.diff(t1, t2);
            modelForDefaultKey = diffTable.getModel();
        }

        GlobalTableModelInterface model1 = graphObjects[0].getGlobalTableModelInterface();
        int nbColumns = model1.getColumnCount();
        int nbColumnsKept = 0;
        for (int i = 0; i < nbColumns; i++) {
            Class c = model1.getDataColumnClass(i);
            if (c.equals(String.class) || c.equals(Integer.class) || c.equals(Long.class)) {
                nbColumnsKept++;
            }
        }
        Object[] objectArray1 = new Object[nbColumnsKept];
        Object[] associatedObjectArray1 = new Object[nbColumnsKept];
        int iKept = 0;
        for (int i = 0; i < nbColumns; i++) {
            Class c = model1.getDataColumnClass(i);
            if (c.equals(String.class) || c.equals(Integer.class) || c.equals(Long.class)) {
                objectArray1[iKept] = model1.getColumnName(i);
                associatedObjectArray1[iKept] = i;  // no +1 because it is not used in python calc expression
                iKept++;
            }
        }

        GlobalTableModelInterface model2 = graphObjects[1].getGlobalTableModelInterface();
        nbColumns = model2.getColumnCount();
        nbColumnsKept = 0;
        for (int i = 0; i < nbColumns; i++) {
            Class c = model2.getDataColumnClass(i);
            if (c.equals(String.class) || c.equals(Integer.class) || c.equals(Long.class)) {
                nbColumnsKept++;
            }
        }
        Object[] objectArray2 = new Object[nbColumnsKept];
        Object[] associatedObjectArray2 = new Object[nbColumnsKept];
        iKept = 0;
        for (int i = 0; i < nbColumns; i++) {
            Class c = model2.getDataColumnClass(i);
            if (c.equals(String.class) || c.equals(Integer.class) || c.equals(Long.class)) {
                objectArray2[iKept] = model2.getColumnName(i);
                associatedObjectArray2[iKept] = i;
                iKept++;
            }
        }

        m_paramColumn1 = new ObjectParameter(JOIN_COL1, graphObjects[0].getFullName() + " Join Column Key", new JComboBox(objectArray1), objectArray1, associatedObjectArray1, ((AbstractJoinDataModel) modelForDefaultKey).getSelectedKey1(), null);
        m_paramColumn2 = new ObjectParameter(JOIN_COL2, graphObjects[1].getFullName() + " Join Column Key", new JComboBox(objectArray2), objectArray2, associatedObjectArray2, ((AbstractJoinDataModel) modelForDefaultKey).getSelectedKey2(), null);
        m_addSourceCol = new BooleanParameter(SOURCE_COL, "Add Source Info", JCheckBox.class, true);
        m_parameterList = new ParameterList("Diff");
        m_parameters = new ParameterList[1];
        m_parameters[0] = m_parameterList;

        m_parameterList.add(m_paramColumn1);
        m_parameterList.add(m_paramColumn2);
        m_parameterList.add(m_addSourceCol);

    }

    @Override
    public ParameterError checkParameters(AbstractGraphObject[] graphObjects) {
        Integer key1 = (Integer) m_paramColumn1.getAssociatedObjectValue();
        Integer key2 = (Integer) m_paramColumn2.getAssociatedObjectValue();
        
        GlobalTableModelInterface modelForDefaultKey = m_globalTableModelInterface;
        
        if (modelForDefaultKey == null) {
            Table t1 = new Table(graphObjects[0].getGlobalTableModelInterface());
            Table t2 = new Table(graphObjects[1].getGlobalTableModelInterface());
            Table joinedTable = Table.join(t1, t2);
            modelForDefaultKey = joinedTable.getModel();
        }
        
        boolean checkKeys = ((AbstractJoinDataModel) modelForDefaultKey).checkKeys(key1, key2);

        ParameterError error = null;
        if (!checkKeys) {
            error = new ParameterError("Selected Keys are not compatible", m_parameterList.getPanel());
        }
        return error;
    }
    
    @Override
    public void userParametersChanged() {
        m_globalTableModelInterface = null;
    }
    
}
