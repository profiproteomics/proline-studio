package fr.proline.studio.rsmexplorer.gui.calc.functions;

import fr.proline.studio.parameter.AbstractLinkedParameters;
import fr.proline.studio.parameter.DoubleParameter;
import fr.proline.studio.parameter.IntegerParameter;
import fr.proline.studio.parameter.ObjectParameter;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.python.data.ColRef;
import fr.proline.studio.python.data.Table;
import fr.proline.studio.python.interpreter.CalcCallback;
import fr.proline.studio.python.interpreter.CalcError;
import fr.proline.studio.python.interpreter.CalcInterpreterTask;
import fr.proline.studio.python.interpreter.CalcInterpreterThread;
import fr.proline.studio.python.interpreter.ResultVariable;
import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import fr.proline.studio.rsmexplorer.gui.calc.ProcessCallbackInterface;
import fr.proline.studio.rsmexplorer.gui.calc.graph.AbstractConnectedGraphObject;
import fr.proline.studio.rsmexplorer.gui.calc.graph.FunctionGraphNode;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.types.LogInfo;
import fr.proline.studio.types.LogRatio;
import fr.proline.studio.types.PValue;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import org.python.core.PyFloat;

/**
 *
 * @author JM235353
 */
public class ComputeFDRFunction extends AbstractFunction {

    
    private static final String PVALUE_THRESHOLD = "PVALUE_THRESHOLD";
    private static final String LOGFC_THRESHOLD = "LOGFC_THRESHOLD";
    
    private static final String PVALUE_COL_PARAMETER = "PVALUE_COL_PARAMETER";
    private static final String LOGFC_COL_PARAMETER = "FCLOG_COL_PARAMETER";
    private static final String PI0PARAMETER = "PI0PARAMETER";
    private static final String ALPHAPARAMETER = "ALPHAPARAMETER";
    private static final String NBBINSPARAMETER = "NBBINSPARAMETER";
    private static final String PZPARAMETER = "PZPARAMETER";
    private static final String NUMERICVALUEARAMETER = "NUMERICVALUEARAMETER";

    
    private ObjectParameter m_pValueColumnParameter = null;
    private ObjectParameter m_logFCColumnParameter = null;
    
    private DoubleParameter m_pvalueThresholdParameter;
    private DoubleParameter m_logFCThresholdParameter;
    
    private ObjectParameter m_pi0MethodParameter = null;
    private DoubleParameter m_numericValueParameter = null;
    private DoubleParameter m_alphaParameter = null;
    private IntegerParameter m_nbinsParameter = null;
    private DoubleParameter m_pzParameter = null;
    
    private Double m_fdrResult = null;
    
    private ResultVariable m_fdrResultVariable = null;
    
    public ComputeFDRFunction(GraphPanel panel) {
        super(panel);
    }

        @Override
    public void inLinkDeleted() {
        super.inLinkDeleted();
        m_fdrResult = null;
        m_pValueColumnParameter = null;
        m_logFCColumnParameter = null;
        m_pvalueThresholdParameter = null;
        m_logFCThresholdParameter = null;
        m_pi0MethodParameter = null;
        m_numericValueParameter = null;
        m_alphaParameter = null;
        m_nbinsParameter = null;
        m_pzParameter = null;
    }

    @Override
    public String getName() {
        if (m_pi0MethodParameter == null) {
            return "Compute FDR";
        }
        StringBuilder columnNameSb = new StringBuilder("Compute FDR ");
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
    public void process(AbstractConnectedGraphObject[] graphObjects, final FunctionGraphNode functionGraphNode, ProcessCallbackInterface callback) {
        setInError(false, null);
        
        if (m_parameters == null) {
            callback.finished(functionGraphNode);
            return;
        }

        Integer pvalueColIndex =(Integer) m_pValueColumnParameter.getAssociatedObjectValue();
        if ((pvalueColIndex == null) || (pvalueColIndex == -1)) {
            callback.finished(functionGraphNode);
            return;
        }
        
        Integer logFCColIndex =(Integer) m_logFCColumnParameter.getAssociatedObjectValue();
        if ((logFCColIndex == null) || (logFCColIndex == -1)) {
            callback.finished(functionGraphNode);
            return;
        }
        
        // check if we have already processed
        if (m_fdrResult != null) {
            callback.finished(functionGraphNode);
            return;
        }

        setCalculating(true);
   
        try {

            GlobalTableModelInterface srcModel = graphObjects[0].getGlobalTableModelInterface();
            final Table sourceTable = new Table(srcModel);

            ResultVariable[] parameters = new ResultVariable[2];
            ColRef pvalueCol = sourceTable.getCol(pvalueColIndex);
            parameters[0] = new ResultVariable(pvalueCol);
            ColRef logFCCol = sourceTable.getCol(logFCColIndex);
            parameters[1] = new ResultVariable(logFCCol);


            StringBuilder codeSB = new StringBuilder();
            codeSB.append("computedFDR=Stats.computeFDR(");
            for (int i = 0; i < parameters.length; i++) {
                codeSB.append(parameters[i].getName());
                codeSB.append(',');
            }

            codeSB.append(m_pvalueThresholdParameter.getStringValue());
            codeSB.append(',');
            codeSB.append(m_logFCThresholdParameter.getStringValue());
            
            
            String pi0Method = m_pi0MethodParameter.getStringValue();
            if (pi0Method.compareTo("Numeric Value") == 0) {
                codeSB.append(',');
                codeSB.append(m_numericValueParameter.getStringValue());
            } else {
                codeSB.append(",\"").append(pi0Method).append("\"");
            }
            codeSB.append(",").append(m_alphaParameter.getStringValue());
            codeSB.append(",").append(m_nbinsParameter.getStringValue());
            codeSB.append(",").append(m_pzParameter.getStringValue());
            codeSB.append(')');

            CalcCallback calcCallback = new CalcCallback() {

                @Override
                public void run(ArrayList<ResultVariable> variables, CalcError error) {
                    try {
                        if (variables != null) {
                            // look for res
                            for (ResultVariable var : variables) {
                                if (var.getName().compareTo("computedFDR") == 0) {
                                    // we have found the result
                                    PyFloat fdr = (PyFloat) var.getValue();
                                    m_fdrResult = fdr.getValue();

                                    //m_globalTableModelInterface = sourceTable.getModel();
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
        JOptionPane.showMessageDialog(m_panel, "FDR Value= "+m_fdrResult.toString(), "FDR", JOptionPane.INFORMATION_MESSAGE);
    }
    
    @Override
    public WindowBox getDisplayWindowBox(FunctionGraphNode functionGraphNode) {
        return null; //JPM.TODO
    }
    
    
    @Override
    public void generateDefaultParameters(AbstractConnectedGraphObject[] graphObjects) {
        
        GlobalTableModelInterface model1 = graphObjects[0].getGlobalTableModelInterface();
        int nbColumns = model1.getColumnCount();
        int nbColumnsKept = 0;
        for (int i = 0; i < nbColumns; i++) {
            Class c = model1.getDataColumnClass(i);
            if (c.equals(Float.class) || c.equals(Double.class)) {
                nbColumnsKept++;
            }
        }
        Object[] pValueObjectArray = new Object[nbColumnsKept];
        Object[] pValueAssociatedObjectArray = new Object[nbColumnsKept];
        int iKept = 0;
        int selectedIndexPValue = -1;
        for (int i = 0; i < nbColumns; i++) {
            Class c = model1.getDataColumnClass(i);
            if (c.equals(Float.class) || c.equals(Double.class)) {
                pValueObjectArray[iKept] = model1.getColumnName(i);
                PValue pvalue = (PValue) model1.getColValue(PValue.class, i);
                if (pvalue != null) {
                    LogInfo log = (LogInfo) model1.getColValue(LogInfo.class, i);
                    if ((log == null) || (log.noLog())) {
                        selectedIndexPValue = iKept;
                    }
                }
                pValueAssociatedObjectArray[iKept] = i+1;  // +1 because it is used in python calc expression
                iKept++;
            }
        }
        
        Object[] logFCObjectArray = new Object[nbColumnsKept];
        Object[] logFCAssociatedObjectArray = new Object[nbColumnsKept];
        iKept = 0;
        int selectedIndexLogFC = -1;
        for (int i = 0; i < nbColumns; i++) {
            Class c = model1.getDataColumnClass(i);
            if (c.equals(Float.class) || c.equals(Double.class)) {
                logFCObjectArray[iKept] = model1.getColumnName(i);
                LogRatio logRatio = (LogRatio) model1.getColValue(LogRatio.class, i);
                if (logRatio != null) {
                    selectedIndexLogFC = iKept;
                }
                logFCAssociatedObjectArray[iKept] = i+1;  // +1 because it is used in python calc expression
                iKept++;
            }
        }

        ParameterList parameterList1 = new ParameterList("param1");
        
        m_pValueColumnParameter = new ObjectParameter(PVALUE_COL_PARAMETER, "P Values Column", null, pValueObjectArray, pValueAssociatedObjectArray, selectedIndexPValue, null);
        m_logFCColumnParameter = new ObjectParameter(LOGFC_COL_PARAMETER, "Log FC Column", null, logFCObjectArray, logFCAssociatedObjectArray, selectedIndexLogFC, null);

        m_pvalueThresholdParameter = new DoubleParameter(PVALUE_THRESHOLD, "PValue Threshold", JTextField.class, 0d, 0d, null);
        m_logFCThresholdParameter = new DoubleParameter(LOGFC_THRESHOLD, "Log FC Threshold", JTextField.class, 0d, 0d, null);

        
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

        parameterList1.add(m_pValueColumnParameter);
        parameterList1.add(m_logFCColumnParameter);
        parameterList1.add(m_pvalueThresholdParameter);
        parameterList1.add(m_logFCThresholdParameter);
        parameterList1.add(m_pi0MethodParameter);
        parameterList1.add(m_numericValueParameter);
        parameterList1.add(m_alphaParameter);
        parameterList1.add(m_nbinsParameter);
        parameterList1.add(m_pzParameter);
        
        parameterList1.getPanel(); // generate panel at once
        m_pi0MethodParameter.addLinkedParameters(linkedParameters); // link parameter, it will modify the panel

        // forbid to change values for some methods
        m_nbinsParameter.getComponent().setEnabled(false);
        m_pzParameter.getComponent().setEnabled(false);
        m_alphaParameter.getComponent().setEnabled(false);

    }
    
    @Override
    public AbstractFunction cloneFunction(GraphPanel p) {
        return new ComputeFDRFunction(p);
    }
    
    /*
    @Override
    public ParameterList getExtraParameterList() {

        ParameterList parameterList = new ParameterList("compute FDR options");

        m_pvalueThresholdParameter = new DoubleParameter(PVALUE_THRESHOLD, "PValue Threshold", JTextField.class, 0d, 0d, null);
        
        m_logFCThresholdParameter = new DoubleParameter(LOGFC_THRESHOLD, "LogInfo FC Threshold", JTextField.class, 0d, 0d, null);



        
        
        String[] pi0Values = { "Numeric Value", "abh", "jiang", "histo", "langaas", "pounds", "slim", "st.boot", "st.spline" };
        m_pi0MethodParameter = new ObjectParameter(PI0PARAMETER, "pi0 Method", pi0Values, 0, null);
        
        m_numericValueParameter = new DoubleParameter(NUMERICVALUEARAMETER, "Pi0 Value", JTextField.class, 1d, 0d, 1d);
        m_nbinsParameter = new IntegerParameter(NBBINSPARAMETER, "Number of Bins", JSpinner.class, 20, 5, 100);
        m_pzParameter = new DoubleParameter(PZPARAMETER, "Pz", JTextField.class, 0.05, 0.01, 0.1);
        
        AbstractLinkedParameters linkedParameters = new AbstractLinkedParameters(parameterList) {
            @Override
            public void valueChanged(String value, Object associatedValue) {
                showParameter(m_numericValueParameter, (value.compareTo("Numeric Value") == 0));
                showParameter(m_nbinsParameter, ((value.compareTo("jiang") == 0) || (value.compareTo("histo") == 0)));
                showParameter(m_pzParameter, (value.compareTo("slim") == 0));

                updataParameterListPanel();
            }
            
        };


        parameterList.add(m_pvalueThresholdParameter);
        parameterList.add(m_logFCThresholdParameter);
        parameterList.add(m_pi0MethodParameter);
        parameterList.add(m_numericValueParameter);
        parameterList.add(m_nbinsParameter);
        parameterList.add(m_pzParameter);
        

        
        parameterList.getPanel(); // generate panel at once
        m_pi0MethodParameter.addLinkedParameters(linkedParameters); // link parameter, it will modify the panel

        // forbid to change values for some methods
        m_nbinsParameter.getComponent().setEnabled(false);
        m_pzParameter.getComponent().setEnabled(false);
        
        return parameterList;
        
    }*/
    
    /*@Override
    public String getExtraValuesForFunctionCall() {
        
        String pvalueParameter = m_pvalueThresholdParameter.getStringValue();
        String logFCParameter = m_logFCThresholdParameter.getStringValue();
        
        String pi0Method = m_pi0MethodParameter.getStringValue();
        if (pi0Method.compareTo("Numeric Value") == 0) {
            pi0Method = m_numericValueParameter.getStringValue();
        }

        return ","+pvalueParameter+","+logFCParameter+",\""+pi0Method+"\"";  

    }
    
    @Override
    public ResultVariable[] getExtraVariables(Table sourceTable) {
        m_fdrResultVariable = new ResultVariable(sourceTable);
        ResultVariable[] resultVariables = { m_fdrResultVariable };
        return resultVariables;
    }*/
    
    @Override
    public boolean calculationDone() {
        if (m_fdrResult != null) {
            return true;
        }
        return false;
    }
    
    @Override
    public void userParametersChanged() {
        // need to recalculate model
        m_fdrResult = null;
    }
    
    @Override
    public boolean settingsDone() {

        if (m_parameters == null) {
            return false;
        }

        if (m_pValueColumnParameter == null) {
            return false;
        }

        Integer colIndex = (Integer) m_pValueColumnParameter.getAssociatedObjectValue();
        if ((colIndex == null) || (colIndex == -1)) {
            return false;
        }

        return true;
    }
    
    @Override
    public ParameterError checkParameters(AbstractConnectedGraphObject[] graphObjects) {
        return null;
    }

}