package fr.proline.studio.rsmexplorer.gui.calc.functions;

import fr.proline.studio.parameter.AbstractLinkedParameters;
import fr.proline.studio.parameter.DoubleParameter;
import fr.proline.studio.parameter.IntegerParameter;
import fr.proline.studio.parameter.MultiObjectParameter;
import fr.proline.studio.parameter.ObjectParameter;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.python.data.ColData;
import fr.proline.studio.python.data.ColRef;
import fr.proline.studio.python.data.Table;
import fr.proline.studio.python.interpreter.CalcCallback;
import fr.proline.studio.python.interpreter.CalcError;
import fr.proline.studio.python.interpreter.CalcInterpreterTask;
import fr.proline.studio.python.interpreter.CalcInterpreterThread;
import fr.proline.studio.python.interpreter.ResultVariable;
import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import fr.proline.studio.rsmexplorer.gui.calc.ProcessCallbackInterface;
import fr.proline.studio.rsmexplorer.gui.calc.graph.AbstractGraphObject;
import fr.proline.studio.rsmexplorer.gui.calc.graph.FunctionGraphNode;
import fr.proline.studio.table.GlobalTableModelInterface;
import java.util.ArrayList;

/**
 *
 * @author JM235353
 */
public class LogFunction  extends AbstractFunction {

    private static final String SEL_COL1 = "SEL_COL1";

    
    private MultiObjectParameter m_columnsParameter1 = null;

    public LogFunction(GraphPanel panel) {
        super(panel);
    }
    
    @Override
    public void inLinkDeleted() {
        super.inLinkDeleted();
        m_columnsParameter1 = null;
    }
    
    @Override
    public String getName() {
        return "Log";
    }

    @Override
    public int getNumberOfInParameters() {
        return 1;
    }

    @Override
    public void process(AbstractGraphObject[] graphObjects, final FunctionGraphNode functionGraphNode, ProcessCallbackInterface callback) {
        setInError(false, null);
        
        if (m_parameters == null) {
            callback.finished(functionGraphNode);
            return;
        }

        ArrayList colList = (ArrayList) m_columnsParameter1.getAssociatedValues(true);
        if ((colList == null) || (colList.isEmpty())) {
            callback.finished(functionGraphNode);
            return;
        }

        
        // check if we have already processed
        if (m_globalTableModelInterface != null) {
            callback.finished(functionGraphNode);
            return;
        }

        setCalculating(true);
   
        try {

            GlobalTableModelInterface srcModel = graphObjects[0].getGlobalTableModelInterface();
            final Table sourceTable = new Table(srcModel);


            int nbCols = colList.size();
            ResultVariable[] parameters = new ResultVariable[nbCols+1];

            for (int i=0;i<nbCols;i++) {
                ColRef col = sourceTable.getCol((Integer)colList.get(i));
                parameters[i] = new ResultVariable(col);
            }
            
            parameters[nbCols] = new ResultVariable(sourceTable);
            
            


            StringBuilder codeSB = new StringBuilder();
            codeSB.append("logColumn=Stats.log(");
            codeSB.append(parameters[nbCols].getName());
            
            int nbColsToLog = parameters.length-1;
            if (nbColsToLog>1) {
                codeSB.append(",(");
                for (int i = 0; i < parameters.length - 1; i++) {
                    codeSB.append(parameters[i].getName());
                    if (i < parameters.length - 2) {
                        codeSB.append(",");
                    }
                }
                codeSB.append(")");
            } else {
                codeSB.append(",");
                codeSB.append(parameters[0].getName());
            }
            

            codeSB.append(')');
 
            CalcCallback calcCallback = new CalcCallback() {

                @Override
                public void run(ArrayList<ResultVariable> variables, CalcError error) {
                    try {
                        if (variables != null) {
                            // look for res
                            for (ResultVariable var : variables) {
                                if (var.getName().compareTo("logColumn") == 0) {
                                    // we have found the result
                                    Table resTable = (Table) var.getValue();
                                    
                                    
                                    //ColData col = (ColData) var.getValue();
                                    // give a specific column name
                                    //col.setColumnName(columnName);
                                    //sourceTable.addColumn(col, null, new DoubleRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 4, true, true));

                                    m_globalTableModelInterface = resTable.getModel();

                                }
                            }
                        } else if (error != null) {
                            setInError(error);
                        }
                        setCalculating(false);
                    } finally {
                        callback.finished(functionGraphNode);
                    }
                }

            };

            CalcInterpreterTask task = new CalcInterpreterTask(codeSB.toString(), parameters, calcCallback);

            CalcInterpreterThread.getCalcInterpreterThread().addTask(task);

        } catch (Exception e) {
            setInError(new CalcError(e, null, -1));
            setCalculating(false);
            callback.finished(functionGraphNode);
        }

    }
    
    @Override
    public void askDisplay(FunctionGraphNode functionGraphNode) {
        display(functionGraphNode.getPreviousDataName(), getName());
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
                associatedObjectArray1[iKept] = i+1;  // +1 because it is used in python calc expression
                iKept++;
            }
        }

        ParameterList parameterList1 = new ParameterList("param1");
        
        m_columnsParameter1 = new MultiObjectParameter(SEL_COL1, "Columns to log", null, objectArray1, associatedObjectArray1, null, null);


        m_parameters = new ParameterList[1];
        m_parameters[0] = parameterList1;

        parameterList1.add(m_columnsParameter1);

    }

    @Override
    public void userParametersChanged() {
        // need to recalculate model
        m_globalTableModelInterface = null;
    }

    @Override
    public AbstractFunction cloneFunction(GraphPanel p) {
        return new LogFunction(p);
    }

    @Override
    public boolean calculationDone() {
        if (m_globalTableModelInterface != null) {
            return true;
        }
        return false;
    }
    
    @Override
    public boolean settingsDone() {
        
        if (m_parameters == null) {
            return false;
        }
        
        if (m_columnsParameter1 == null) {
            return false;
        }
        
        ArrayList colList = (ArrayList) m_columnsParameter1.getAssociatedValues(true);
        if ((colList == null) || (colList.isEmpty())) {
            return false;
        }

        return true;
    }

    @Override
    public ParameterError checkParameters(AbstractGraphObject[] graphObjects) {
        return null;
    }
    
}
