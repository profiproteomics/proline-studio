package fr.proline.studio.rsmexplorer.gui.calc.functions;

import fr.proline.studio.parameter.MultiObjectParameter;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.python.data.ColData;
import fr.proline.studio.python.data.ColRef;
import fr.proline.studio.python.data.Table;
import fr.proline.studio.python.interpreter.CalcCallback;
import fr.proline.studio.python.interpreter.ResultVariable;
import fr.proline.studio.python.interpreter.CalcInterpreterTask;
import fr.proline.studio.python.interpreter.CalcInterpreterThread;
import fr.proline.studio.rsmexplorer.gui.calc.graph.AbstractGraphObject;
import fr.proline.studio.table.GlobalTableModelInterface;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JM235353
 */
public class TtdFunction extends AbstractFunction {

    private static final String SEL_COLS1 = "SEL_COLS1";
    private static final String SEL_COLS2 = "SEL_COLS2";
    
    private MultiObjectParameter m_columnsParameter1 = null;
    private MultiObjectParameter m_columnsParameter2 = null;
    
    @Override
    public String getName() {
        return "ttd";
    }

    @Override
    public int getNumberOfInParameters() {
        return 1;
    }

    @Override
    public void process(AbstractGraphObject[] graphObjects, final boolean display) {
        
        
        if (m_columnsParameter1 == null) {
            //m_state = GraphNode.NodeState.UNSET;
            return;
        }
        
        List colList1 =(List) m_columnsParameter1.getAssociatedSelectedObjectValue();
        List colList2 =(List) m_columnsParameter2.getAssociatedSelectedObjectValue();
        if ((colList1 == null) || (colList1.isEmpty()) || (colList2 == null) || (colList2.isEmpty())) {
            //m_state = GraphNode.NodeState.UNSET;
            return;
        }

        try {

            GlobalTableModelInterface srcModel = graphObjects[0].getGlobalTableModelInterface();
            final Table sourceTable = new Table(srcModel);

            ResultVariable[] parameters = new ResultVariable[colList1.size()+colList2.size()];

            for (int i = 0; i < colList1.size(); i++) {
                Integer colIndex = (Integer) colList1.get(i);
                ColRef col = sourceTable.getCol(colIndex);
                parameters[i] = new ResultVariable(col);
            }

            for (int i = 0; i < colList2.size(); i++) {
                Integer colIndex = (Integer) colList2.get(i);
                ColRef col = sourceTable.getCol(colIndex);
                parameters[i+colList1.size()] = new ResultVariable(col);
            }

            StringBuilder codeSB = new StringBuilder();
            codeSB.append("ttd=Stats.ttd((");
            for (int i=0;i<colList1.size();i++) {
                codeSB.append(parameters[i].getName());
                if (i<colList1.size()-1) {
                    codeSB.append(',');
                }
            }
            codeSB.append("),(");
            for (int i=0;i<colList2.size();i++) {
                codeSB.append(parameters[i+colList1.size()].getName());
                if (i<colList2.size()-1) {
                    codeSB.append(',');
                }
            }
            codeSB.append("))");

            
            CalcCallback callback = new CalcCallback() {

                @Override
                public void run(ArrayList<ResultVariable> variables, String error, int lineError) {
                    if (variables != null) {
                        // look for res
                        for (ResultVariable var : variables) {
                            if (var.getName().compareTo("ttd") == 0) {
                                // we have found the result
                                ColData col = (ColData) var.getValue();
                                sourceTable.addColumn(col);
                                m_globalTableModelInterface = sourceTable.getModel();
                                if (display) {
                                    display(var.getName());
                                }
                            }
                        }
                    } else if (error != null) {
                        //JPM.TODO
                        
                    }
                }
                
            };
            
            CalcInterpreterTask task = new CalcInterpreterTask(codeSB.toString(), parameters, callback);
            
            CalcInterpreterThread.getCalcInterpreterThread().addTask(task);

        } catch (Exception e) {
            //m_state = GraphNode.NodeState.UNSET;
        }
        
        
    }

    /*@Override
    public GraphNode.NodeState getState() {
        return m_state;
    }*/
    
    @Override
    public boolean settingsDone() {
        if (m_columnsParameter1 == null) {
            return false;
        }

        List colList1 = (List) m_columnsParameter1.getAssociatedSelectedObjectValue();
        List colList2 = (List) m_columnsParameter2.getAssociatedSelectedObjectValue();
        if ((colList1 == null) || (colList1.isEmpty()) || (colList2 == null) || (colList2.isEmpty())) {
            return false;
        }
        
        return true;
    }

    @Override
    public boolean calculationDone() {
        if (m_globalTableModelInterface != null) {
            return true;
        }
        return false;
    }
    
    @Override
    public void generateDefaultParameters(AbstractGraphObject[] graphObjects) {
        GlobalTableModelInterface model1 = graphObjects[0].getGlobalTableModelInterface();
        int nbColumns = model1.getColumnCount();
        int nbColumnsKept = 0;
        for (int i = 0; i < nbColumns; i++) {
            Class c = model1.getDataColumnClass(i);
            if (c.equals(Float.class) || c.equals(Double.class)) {
                nbColumnsKept++;
            }
        }
        Object[] objectArray1 = new Object[nbColumnsKept];
        Object[] associatedObjectArray1 = new Object[nbColumnsKept];
        int iKept = 0;
        for (int i = 0; i < nbColumns; i++) {
            Class c = model1.getDataColumnClass(i);
            if (c.equals(Float.class) || c.equals(Double.class)) {
                objectArray1[iKept] = model1.getColumnName(i);
                associatedObjectArray1[iKept] = i;
                iKept++;
            }
        }
        
        m_columnsParameter1 = new MultiObjectParameter(SEL_COLS1, "First Column Group", null, objectArray1, associatedObjectArray1, null, null);
        m_columnsParameter2 = new MultiObjectParameter(SEL_COLS2, "Second Column Group", null, objectArray1, associatedObjectArray1, null, null);
     
        ParameterList parameterList1 = new ParameterList("ttd1");
        ParameterList parameterList2 = new ParameterList("ttd2");
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
    }

    @Override
    public AbstractFunction cloneFunction() {
        return new TtdFunction();
    }





}
