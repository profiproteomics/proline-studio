package fr.proline.studio.rsmexplorer.gui.calc.functions;

import fr.proline.studio.comparedata.JoinDataModel;
import fr.proline.studio.parameter.ObjectParameter;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.python.data.Table;
import fr.proline.studio.rsmexplorer.gui.calc.graph.AbstractGraphObject;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphNode;
import fr.proline.studio.table.GlobalTableModelInterface;
import javax.swing.JComboBox;

/**
 *
 * @author JM235353
 */
public class JoinFunction extends AbstractFunction {

    private static final String JOIN_COL1 = "JOIN_COL1";
    private static final String JOIN_COL2 = "JOIN_COL2";
    
  
    
    private ParameterList m_parameterList;
    private ObjectParameter m_paramColumn1;
    private ObjectParameter m_paramColumn2;
    
    @Override
    public String getName() {
        return "Join";
    }

    @Override
    public int getNumberOfInParameters() {
        return 2;
    }
    
    @Override
    public GraphNode.NodeState getState() {
        return m_state;
    }

    @Override
    public AbstractFunction cloneFunction() {
        return new JoinFunction();
    }
    
    @Override
    public void process(AbstractGraphObject[] graphObjects) {

        Table t1 = new Table(graphObjects[0].getGlobalTableModelInterface());
        Table t2 = new Table(graphObjects[1].getGlobalTableModelInterface());
        
        Table joinedTable;
        if ((m_paramColumn1 != null) && (m_paramColumn2 != null)) {
            Integer key1 = (Integer) m_paramColumn1.getAssociatedObjectValue();
            Integer key2 = (Integer) m_paramColumn2.getAssociatedObjectValue();
            joinedTable = Table.join(t1, t2, key1, key2);
        } else {
            joinedTable = Table.join(t1, t2);
        }

        m_globalTableModelInterface = joinedTable.getModel();
        m_state = GraphNode.NodeState.READY;
    }
    
    @Override
    public void generateDefaultParameters(AbstractGraphObject[] graphObjects) {
        
        if (m_globalTableModelInterface == null) {
            Table t1 = new Table(graphObjects[0].getGlobalTableModelInterface());
            Table t2 = new Table(graphObjects[1].getGlobalTableModelInterface());
            Table joinedTable = Table.join(t1, t2);
            m_globalTableModelInterface = joinedTable.getModel();
        }

        GlobalTableModelInterface model1 = graphObjects[0].getGlobalTableModelInterface();
        int nbColumns = model1.getColumnCount();
        Object[] objectArray1 = new Object[nbColumns];
        Object[] associatedObjectArray1 = new Object[nbColumns];
        for (int i = 0; i < nbColumns; i++) {
            objectArray1[i] = model1.getColumnName(i);
            associatedObjectArray1[i] = i;
        }

        GlobalTableModelInterface model2 = graphObjects[0].getGlobalTableModelInterface();
        nbColumns = model2.getColumnCount();
        Object[] objectArray2 = new Object[nbColumns];
        Object[] associatedObjectArray2 = new Object[nbColumns];
        for (int i = 0; i < nbColumns; i++) {
            objectArray2[i] = model2.getColumnName(i);
            associatedObjectArray2[i] = i;
        }
        
        m_paramColumn1 = new ObjectParameter(JOIN_COL1, "Join Column Key 1", new JComboBox(objectArray1), objectArray1, associatedObjectArray1, ((JoinDataModel)m_globalTableModelInterface).getSelectedKey1(), null);
        m_paramColumn2 = new ObjectParameter(JOIN_COL2, "Join Column Key 2", new JComboBox(objectArray2), objectArray2, associatedObjectArray2, ((JoinDataModel)m_globalTableModelInterface).getSelectedKey2(), null);
        m_parameterList = new ParameterList("Join");
        m_parameters = new ParameterList[1];
        m_parameters[0] = m_parameterList;
        
        m_parameterList.add(m_paramColumn1);
        m_parameterList.add(m_paramColumn2);
        
    }
    
    @Override
    public ParameterError checkParameters() {
        Integer key1 = (Integer) m_paramColumn1.getAssociatedObjectValue();
        Integer key2 = (Integer) m_paramColumn2.getAssociatedObjectValue();
        boolean checkKeys = ((JoinDataModel)m_globalTableModelInterface).checkKeys(key1, key2);
        
        ParameterError error = null;
        if (!checkKeys) {
            error = new ParameterError("Selected Keys are not compatible", m_parameterList.getPanel() );
        }
        return error;
    }
    
    @Override
    public void userParametersChanged() {
        Integer key1 = (Integer) m_paramColumn1.getAssociatedObjectValue();
        Integer key2 = (Integer) m_paramColumn2.getAssociatedObjectValue();
        ((JoinDataModel)m_globalTableModelInterface).setKeys(key1, key2);
        m_state = GraphNode.NodeState.UNSET;
    }
    
}
