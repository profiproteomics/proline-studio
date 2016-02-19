package fr.proline.studio.rsmexplorer.gui.calc.functions;

import fr.proline.studio.parameter.AbstractLinkedParameters;
import fr.proline.studio.parameter.DoubleParameter;
import fr.proline.studio.parameter.IntegerParameter;
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
import fr.proline.studio.table.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.DoubleRenderer;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.types.PValue;
import java.util.ArrayList;
import javax.swing.JSpinner;
import javax.swing.JTextField;

/**
 *
 * @author jm235353
 */
public class AdjustPFunction extends AbstractFunction {

    private static final String SEL_COLS1 = "SEL_COLS1";
    private static final String PI0PARAMETER = "PI0PARAMETER";
    private static final String ALPHAPARAMETER = "ALPHAPARAMETER";
    private static final String NBBINSPARAMETER = "NBBINSPARAMETER";
    private static final String PZPARAMETER = "PZPARAMETER";
    private static final String NUMERICVALUEARAMETER = "NUMERICVALUEARAMETER";
    
    private ObjectParameter m_columnsParameter1 = null;
    private ObjectParameter m_pi0MethodParameter = null;
    private DoubleParameter m_numericValueParameter = null;
    private DoubleParameter m_alphaParameter = null;
    private IntegerParameter m_nbinsParameter = null;
    private DoubleParameter m_pzParameter = null;
    
    
    public AdjustPFunction(GraphPanel panel) {
        super(panel);
    }
    
    @Override
    public void inLinkDeleted() {
        super.inLinkDeleted();
        m_columnsParameter1 = null;
        m_pi0MethodParameter = null;
        m_numericValueParameter = null;
        m_alphaParameter = null;
        m_nbinsParameter = null;
        m_pzParameter = null;
    }
    
    @Override
    public String getName() {
        if (m_pi0MethodParameter == null) {
            return "AdjustP";
        }
        StringBuilder columnNameSb = new StringBuilder("AdjustP ");
        String pi0Method = m_pi0MethodParameter.getStringValue();
        if (pi0Method.compareTo("Numeric Value") == 0) {
            columnNameSb.append(m_numericValueParameter.getStringValue());
        } else {
            columnNameSb.append(pi0Method);
        }
        return columnNameSb.toString();
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

        Integer colIndex =(Integer) m_columnsParameter1.getAssociatedObjectValue();
        if ((colIndex == null) || (colIndex == -1)) {
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

            ResultVariable[] parameters = new ResultVariable[1];
            ColRef col = sourceTable.getCol(colIndex);
            parameters[0] = new ResultVariable(col);


            StringBuilder codeSB = new StringBuilder();
            codeSB.append("adjustP=Stats.adjustP(");
            for (int i = 0; i < parameters.length; i++) {
                codeSB.append(parameters[i].getName());
            }
            
            StringBuilder columnNameSb = new StringBuilder("adjustP ");
            String pi0Method = m_pi0MethodParameter.getStringValue();
            if (pi0Method.compareTo("Numeric Value") == 0) {
                codeSB.append(',');
                codeSB.append(m_numericValueParameter.getStringValue());
                columnNameSb.append(m_numericValueParameter.getStringValue());
            } else {
                codeSB.append(",\"").append(pi0Method).append("\"");
                columnNameSb.append(pi0Method);
            }
            codeSB.append(",").append(m_alphaParameter.getStringValue());
            codeSB.append(",").append(m_nbinsParameter.getStringValue());
            codeSB.append(",").append(m_pzParameter.getStringValue());
            codeSB.append(')');
            
            final String columnName = columnNameSb.toString();
            
            CalcCallback calcCallback = new CalcCallback() {

                @Override
                public void run(ArrayList<ResultVariable> variables, CalcError error) {
                    try {
                        if (variables != null) {
                            // look for res
                            for (ResultVariable var : variables) {
                                if (var.getName().compareTo("adjustP") == 0) {
                                    // we have found the result
                                    ColData col = (ColData) var.getValue();
                                    // give a specific column name
                                    col.setColumnName(columnName);
                                    sourceTable.addColumn(col, new PValue(), new DoubleRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 4, true, true));

                                    m_globalTableModelInterface = sourceTable.getModel();

                                    /*if (display) {
                                     display(functionGraphNode.getPreviousDataName(), var.getName());
                                     }*/
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
        
        m_columnsParameter1 = new ObjectParameter(SEL_COLS1, "P Values Column", null, objectArray1, associatedObjectArray1, -1, null);

        String[] pi0Values = { "Numeric Value", "abh", "bky", "jiang", "histo", "langaas", "pounds", "slim", "st.boot", "st.spline" };
        m_pi0MethodParameter = new ObjectParameter(PI0PARAMETER, "pi0 Method", pi0Values, 0, null);
        
        m_numericValueParameter = new DoubleParameter(NUMERICVALUEARAMETER, "Pi0 Value", JTextField.class, 1d, 0d, 1d); 
        m_alphaParameter = new DoubleParameter(ALPHAPARAMETER, "Alpha", JTextField.class, 0.05, 0d, 1d);
        m_nbinsParameter = new IntegerParameter(NBBINSPARAMETER, "Number of Bins", JSpinner.class, 20, 5, 100);
        m_pzParameter = new DoubleParameter(PZPARAMETER, "Pz", JTextField.class, 0.05, 0.01, 0.1);
        
        AbstractLinkedParameters linkedParameters = new AbstractLinkedParameters(parameterList1) {
            @Override
            public void valueChanged(String value, Object associatedValue) {
                showParameter(m_numericValueParameter, (value.compareTo("Numeric Value") == 0));
                showParameter(m_alphaParameter, (value.compareTo("bky") == 0));
                showParameter(m_nbinsParameter, ((value.compareTo("jiang") == 0) || (value.compareTo("histo") == 0)));
                showParameter(m_pzParameter, (value.compareTo("slim") == 0));

                updataParameterListPanel();
            }
            
        };

        m_parameters = new ParameterList[1];
        m_parameters[0] = parameterList1;

        parameterList1.add(m_columnsParameter1);
        parameterList1.add(m_pi0MethodParameter);
        parameterList1.add(m_numericValueParameter);
        parameterList1.add(m_alphaParameter);
        parameterList1.add(m_nbinsParameter);
        parameterList1.add(m_pzParameter);
        
        parameterList1.getPanel(); // generate panel at once
        m_pi0MethodParameter.addLinkedParameters(linkedParameters); // link parameter, it will modify the panel


    }

    @Override
    public void userParametersChanged() {
        // need to recalculate model
        m_globalTableModelInterface = null;
    }

    @Override
    public AbstractFunction cloneFunction(GraphPanel p) {
        return new AdjustPFunction(p);
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
        
        Integer colIndex =(Integer) m_columnsParameter1.getAssociatedObjectValue();
        if ((colIndex == null) || (colIndex == -1)) {
            return false;
        }

        return true;
    }

    @Override
    public ParameterError checkParameters(AbstractGraphObject[] graphObjects) {
        return null;
    }
    
}
