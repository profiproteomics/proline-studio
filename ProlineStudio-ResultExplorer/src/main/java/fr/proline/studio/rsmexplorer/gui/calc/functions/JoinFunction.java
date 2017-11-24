package fr.proline.studio.rsmexplorer.gui.calc.functions;

import fr.proline.studio.parameter.AbstractLinkedParameters;
import fr.proline.studio.parameter.BooleanParameter;
import fr.proline.studio.parameter.DoubleParameter;
import fr.proline.studio.parameter.ObjectParameter;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.python.data.Table;
import fr.proline.studio.python.interpreter.CalcError;
import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import fr.proline.studio.rsmexplorer.gui.calc.ProcessCallbackInterface;
import fr.proline.studio.rsmexplorer.gui.calc.graph.FunctionGraphNode;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphConnector;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.extendedtablemodel.MultiJoinDataModel;
import java.util.ArrayList;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;

/**
 * Join Function for the data analyzer
 * @author JM235353
 */
public class JoinFunction extends AbstractFunction {

    private static final String JOIN_TABLE1_KEY1 = "JOIN_TABLE1_KEY1";
    private static final String JOIN_TABLE2_KEY1 = "JOIN_TABLE2_KEY1";
    private static final String TOLERANCE_KEY1 = "TOLERANCE_KEY1";
    private static final String JOIN_TABLE1_KEY2 = "JOIN_TABLE1_KEY2";
    private static final String JOIN_TABLE2_KEY2 = "JOIN_TABLE2_KEY2";
    private static final String TOLERANCE_KEY2 = "TOLERANCE_KEY2";
    private static final String SOURCE_COL = "SOURCE_COL";
  
    
    private ParameterList m_parameterList;
    private ArrayList<ObjectParameter> m_paramTableKey1;
    private ArrayList<ObjectParameter> m_paramTableKey2;
    private DoubleParameter m_tolerance1;
    private DoubleParameter m_tolerance2;
    private BooleanParameter m_addSourceCol;
    
    public JoinFunction(GraphPanel panel) {
        super(panel);
    }
    
    @Override
    public void inLinkDeleted() {
        super.inLinkDeleted();
        m_paramTableKey1 = null;
        m_paramTableKey2 = null;
        m_tolerance1 = null;
        m_tolerance2 = null;
        m_addSourceCol = null;
    }
    
    @Override
    public String getName(int index) {
        return "Join";
    }

    @Override
    public int getNumberOfInParameters() {
        return 2;
    }
    
    @Override
    public int getMaximumNumberOfInParameters() {
        return 16; //JPM.TODO 
    }
    
    @Override
    public int getNumberOfOutParameters() {
        return 1;
    }

    
    @Override
    public boolean settingsDone() {
        if (m_parameterList == null) {
            return false;
        }
        return ((m_paramTableKey1 != null) && (m_paramTableKey2 != null));
    }
    
    @Override
    public boolean calculationDone() {
        if (m_globalTableModelInterface != null) {
            return true;
        }
        return false;
    }
    

    @Override
    public AbstractFunction cloneFunction(GraphPanel p) {
        AbstractFunction clone = new JoinFunction(p);
        clone.cloneInfo(this);
        return clone;
    }

    @Override
    public void process(GraphConnector[] graphObjects, FunctionGraphNode functionGraphNode, ProcessCallbackInterface callback) {
        try {
            // check if we have already processed
            if (m_globalTableModelInterface != null) {
                callback.finished(functionGraphNode);
                return;
            }

            setCalculating(true);
            setInError(false, null);

            try {
                int nbTables = graphObjects.length;
                ArrayList<Table> tables = new ArrayList<>(nbTables);
                for (int i=0;i<nbTables;i++) {
                    Table t = new Table(graphObjects[i].getGlobalTableModelInterface());
                    graphObjects[i].getGlobalTableModelInterface().setName(graphObjects[i].getGraphNode().getDataName());
                    tables.add(t);
                }
                

                Table joinedTable;
                
                if ((m_paramTableKey1 != null) && (m_paramTableKey1 != null)) {
                    
                    ArrayList tableKey1List = new ArrayList<>(nbTables);
                    ArrayList tableKey2List = new ArrayList<>(nbTables);
                    for (int i=0;i<nbTables;i++) {
                        Integer tableKey1 = (Integer) m_paramTableKey1.get(i).getAssociatedObjectValue();
                        tableKey1List.add(tableKey1);
                        Integer tableKey2 = (Integer) m_paramTableKey2.get(i).getAssociatedObjectValue();
                        tableKey2List.add(tableKey2);
                    }
                    Double tolerance1 = Double.valueOf(m_tolerance1.getStringValue());
                    Double tolerance2 = Double.valueOf(m_tolerance2.getStringValue());
                    Boolean showSourceColumn = (Boolean) m_addSourceCol.getObjectValue();
                    
                    joinedTable = Table.join(tables, tableKey1List, tolerance1, tableKey2List, tolerance2 , showSourceColumn);
                } else {
                    joinedTable = Table.join(tables);
                }

                addModel(joinedTable.getModel());

            } catch (Exception e) {
                setInError(new CalcError(e, null, -1));
            }

            setCalculating(false);

        } finally {
            callback.finished(functionGraphNode);
        }

    }

    @Override
    public void askDisplay(FunctionGraphNode functionGraphNode, int index) {
        display(functionGraphNode.getPreviousDataName(), getName(index), index);
    }
    
    @Override
    public ArrayList<WindowBox> getDisplayWindowBox(FunctionGraphNode functionGraphNode, int index) {
        return getDisplayWindowBoxList(functionGraphNode.getPreviousDataName(), getName(index), index);
    }
    
    @Override
    public void generateDefaultParameters(GraphConnector[] graphObjects) {

        
        GlobalTableModelInterface modelForDefaultKey = getMainGlobalTableModelInterface(0);
        
        int nbTables = graphObjects.length;
        
        int[] keys = null;

        if (modelForDefaultKey == null) {
            
            /*ArrayList<Table> tables = new ArrayList<>(nbTables);
            for (int i=0;i<nbTables;i++) {
                tables.add(new Table(graphObjects[i].getGlobalTableModelInterface()));
            }

            Table joinedTable = Table.join(tables);
            modelForDefaultKey = joinedTable.getModel();*/
            
            ArrayList<GlobalTableModelInterface> data = new ArrayList<>(graphObjects.length);
            for (GraphConnector connector : graphObjects) {
                data.add(connector.getGlobalTableModelInterface());
            }
            
            keys = MultiJoinDataModel.selectKeys(data);
        }

        m_paramTableKey1 = new ArrayList<>();
        m_paramTableKey2 = new ArrayList<>();
        for (int i=0;i<nbTables;i++) {
            GlobalTableModelInterface model = graphObjects[i].getGlobalTableModelInterface();
            
            int nbColumns = model.getColumnCount();
            int nbColumnsKept = 0;
            for (int j = 0; j < nbColumns; j++) {
                Class c = model.getDataColumnClass(j);
                if (c.equals(String.class) || c.equals(Integer.class) || c.equals(Long.class) || c.equals(Float.class) || c.equals(Double.class)) {
                    nbColumnsKept++;
                }
            }
            
            Object[] objectArrayTableKey1 = new Object[nbColumnsKept];
            Object[] associatedObjectArrayTableKey1 = new Object[nbColumnsKept];
            int iKept = 0;
            for (int j = 0; j < nbColumns; j++) {
                Class c = model.getDataColumnClass(j);
                if (c.equals(String.class) || c.equals(Integer.class) || c.equals(Long.class) || c.equals(Float.class) || c.equals(Double.class)) {
                    objectArrayTableKey1[iKept] = model.getColumnName(j);
                    associatedObjectArrayTableKey1[iKept] = j;  // no +1 because it is not used in python calc expression
                    iKept++;
                }
            }
            
            int nb = objectArrayTableKey1.length;
            Object[] objectArrayTableKey2 = new Object[nb + 1];
            Object[] associatedObjectArrayTableKey2 = new Object[nb + 1];
            objectArrayTableKey2[0] = "<No Second Key>";
            associatedObjectArrayTableKey2[0] = new Integer(-1);
            for (int j = 0; j < nb; j++) {
                objectArrayTableKey2[j + 1] = objectArrayTableKey1[j];
                associatedObjectArrayTableKey2[j + 1] = associatedObjectArrayTableKey1[j];
            }
            
            ObjectParameter paramTableKey1 = new ObjectParameter(JOIN_TABLE1_KEY1, graphObjects[0].getFullName() + " Join Column Key 1", new JComboBox(objectArrayTableKey1), objectArrayTableKey1, associatedObjectArrayTableKey1, keys[i], null);
            m_paramTableKey1.add(paramTableKey1);
            
            ObjectParameter paramTableKey2 = new ObjectParameter(JOIN_TABLE1_KEY2, graphObjects[0].getFullName() + " Join Column Key 2", new JComboBox(objectArrayTableKey2), objectArrayTableKey2, associatedObjectArrayTableKey2, 0, null);
            m_paramTableKey2.add(paramTableKey2);

        }

        m_tolerance1 = new DoubleParameter(TOLERANCE_KEY1, "Tolerance", JTextField.class, 0.0, 0.0, null);
        m_tolerance2 = new DoubleParameter(TOLERANCE_KEY2, "Tolerance 2", JTextField.class, 0.0, 0.0, null);

        m_addSourceCol = new BooleanParameter(SOURCE_COL, "Add Source Info", JCheckBox.class, true);
        
        m_parameterList = new ParameterList("Join");
        
        AbstractLinkedParameters linkedParameters1 = new AbstractLinkedParameters(m_parameterList) {
            @Override
            public void valueChanged(String value, Object associatedValue) {
                int index = ((Integer)associatedValue).intValue();
                GlobalTableModelInterface model = graphObjects[0].getGlobalTableModelInterface();
                Class c = model.getDataColumnClass(index);
                showParameter(m_tolerance1, (c.equals(Double.class) || c.equals(Float.class)));
                updateParameterListPanel();
            }

        };
        
        AbstractLinkedParameters linkedParameters2 = new AbstractLinkedParameters(m_parameterList) {
            @Override
            public void valueChanged(String value, Object associatedValue) {
                int index = ((Integer) associatedValue).intValue();
                if (index == -1) {
                    showParameter(m_tolerance2, false);
                } else {
                    GlobalTableModelInterface model = graphObjects[0].getGlobalTableModelInterface();
                    Class c = model.getDataColumnClass(index);
                    showParameter(m_tolerance2, (c.equals(Double.class) || c.equals(Float.class)));
                }
                updateParameterListPanel();
            }

        };
        
        
        
        m_parameters = new ParameterList[1];
        m_parameters[0] = m_parameterList;

        for (int i=0;i<nbTables;i++) {
            m_parameterList.add(m_paramTableKey1.get(i));
        }
        m_parameterList.add(m_tolerance1);
        
        for (int i=0;i<nbTables;i++) {
            m_parameterList.add(m_paramTableKey2.get(i));
        }
        m_parameterList.add(m_tolerance2);
        

        m_parameterList.add(m_addSourceCol);
        
        m_parameterList.getPanel(); // generate panel at once
        for (int i=0;i<nbTables;i++) {
            m_paramTableKey1.get(i).addLinkedParameters(linkedParameters1); // link parameter, it will modify the panel
            m_paramTableKey2.get(i).addLinkedParameters(linkedParameters2); // link parameter, it will modify the panel
        }
    }

    @Override
    public ParameterError checkParameters(GraphConnector[] graphObjects) {
        /*Integer keyTable1Key1 = (Integer) m_paramTable1Key1.getAssociatedObjectValue();
        Integer keyTable2Key1 = (Integer) m_paramTable2Key1.getAssociatedObjectValue();
        
        Integer keyTable1Key2 = (Integer) m_paramTable1Key2.getAssociatedObjectValue();
        Integer keyTable2Key2 = (Integer) m_paramTable2Key2.getAssociatedObjectValue();
        
        GlobalTableModelInterface modelForDefaultKey = getMainGlobalTableModelInterface(0);
        
        if (modelForDefaultKey == null) {
            Table t1 = new Table(graphObjects[0].getGlobalTableModelInterface());
            Table t2 = new Table(graphObjects[1].getGlobalTableModelInterface());
            Table joinedTable = Table.join(t1, t2);
            modelForDefaultKey = joinedTable.getModel(); //JPM.TODO
        }
        
        boolean checkKeys = ((AbstractJoinDataModel) modelForDefaultKey).checkKeys(keyTable1Key1, keyTable2Key1) && ((AbstractJoinDataModel) modelForDefaultKey).checkKeys(keyTable1Key2, keyTable2Key2);
*/
        ParameterError error = null;
        /*if (!checkKeys) {
            error = new ParameterError("Selected Keys are not compatible", m_parameterList.getPanel());
        }*/
        return error;
    }

    @Override
    public void userParametersChanged() {
        m_globalTableModelInterface = null;

    }

}
