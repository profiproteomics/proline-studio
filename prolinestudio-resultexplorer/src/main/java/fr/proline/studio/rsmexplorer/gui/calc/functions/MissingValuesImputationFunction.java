package fr.proline.studio.rsmexplorer.gui.calc.functions;

import fr.proline.studio.parameter.AbstractLinkedParameters;
import fr.proline.studio.parameter.BooleanParameter;
import fr.proline.studio.parameter.DoubleParameter;
import fr.proline.studio.parameter.ObjectParameter;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.python.data.Table;
import fr.proline.studio.python.interpreter.ResultVariable;
import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

/**
 *
 * @author JM235353
 */
public class MissingValuesImputationFunction extends AbstractOnExperienceDesignFunction {

    private static final String MV_IMPUTATION_METHOD_KEY = "MV_IMPUTATION_METHOD";
    private static final String MV_IMPUTATION_METHOD_NAME = "Method";
    
    private static final String MV_IMPUTATION_METHOD_BPCA = "BPCA";
    private static final String MV_IMPUTATION_METHOD_KNN = "KNN";
    private static final String MV_IMPUTATION_METHOD_MLE = "MLE";
    private static final String MV_IMPUTATION_METHOD_QRILC = "QRILC";
    private static final String MV_PERCENTILE = "Percentile";
    
    private static final String PERCENTILE_VALUE_KEY = "PERCENTILE_PERCENTAGE";
    private static final String PERCENTILE_VALUE_NAME = "Percentage";
    
    private static final String SHOW_IMPUTATION_ROWS_KEY = "SHOW_IMPUTATION_ROWS";
    private static final String SHOW_IMPUTATION_ROWS_NAME = "Add Column with imputation squares";
    
    private static final String MISSING_VALUES_IMPUTATION_FUNCTION_PARAMETER_LIST_NAME = "mv imputation options";
    
    private ParameterList m_parameterList = null;

    private ObjectParameter m_methodParameter = null;
    private DoubleParameter m_percentileThresholdParameter = null;
    private BooleanParameter m_addSourceCol = null;
    
    private ResultVariable m_tableResultVariable = null;
    
    
    
    public MissingValuesImputationFunction(GraphPanel panel) {
        super(panel, FUNCTION_TYPE.MissingValuesImputationFunction, "Missing Values Imputation", "mvimputation", "mvimputation", null, null);
    }
    
    @Override
    public int getMinGroups() {
        return 2;
    }

    @Override
    public int getMaxGroups() {
        return 8;
    }

    @Override
    public AbstractFunction cloneFunction(GraphPanel p) {
        AbstractFunction clone = new MissingValuesImputationFunction(p);
        clone.cloneInfo(this);
        return clone;
    }
    
    @Override
    public ParameterList getExtraParameterList() {

        m_parameterList = new ParameterList(MISSING_VALUES_IMPUTATION_FUNCTION_PARAMETER_LIST_NAME);

        String[] mvImputationMethods = { MV_IMPUTATION_METHOD_BPCA, MV_IMPUTATION_METHOD_KNN, MV_IMPUTATION_METHOD_MLE, MV_IMPUTATION_METHOD_QRILC, MV_PERCENTILE };
     
        m_methodParameter = new ObjectParameter(MV_IMPUTATION_METHOD_KEY, MV_IMPUTATION_METHOD_NAME, null, mvImputationMethods, null, 0, null);

        m_percentileThresholdParameter = new DoubleParameter(PERCENTILE_VALUE_KEY, PERCENTILE_VALUE_NAME, JTextField.class, 2.5d, 0.1d, 50.0d);
        
        m_addSourceCol = new BooleanParameter(SHOW_IMPUTATION_ROWS_KEY, SHOW_IMPUTATION_ROWS_NAME, JCheckBox.class, true);
        
        AbstractLinkedParameters linkedParameters = new AbstractLinkedParameters(m_parameterList) {
            @Override
            public void valueChanged(String value, Object associatedValue) {
                
                boolean isPercentile = value.equalsIgnoreCase(MV_PERCENTILE);
                showParameter(m_percentileThresholdParameter, isPercentile);
                showParameter(m_addSourceCol, isPercentile);
                
            }
            
        };
        
        m_parameterList.add(m_methodParameter);
        m_parameterList.add(m_percentileThresholdParameter);
        m_parameterList.add(m_addSourceCol);
    
        m_parameterList.getPanel(); // generate panel at once
        m_methodParameter.addLinkedParameters(linkedParameters); // link parameter, it will modify the panel
        
        return m_parameterList;
        
    }
    
    @Override
    public String getExtraValuesForFunctionCall() {
        
        String methodParameter = m_methodParameter.getStringValue();
        boolean isPercentile = methodParameter.equalsIgnoreCase(MV_PERCENTILE);
        if (isPercentile) {
            
            boolean displayColInfo = ((Boolean)(m_addSourceCol.getObjectValue())).booleanValue();
            
            return ",\""+methodParameter+"\","+m_percentileThresholdParameter.getStringValue()+","+(displayColInfo ? "1": "0");  
        } else {
            return ",\""+methodParameter+"\"";  
        }

    }
    
    @Override
    public ResultVariable[] getExtraVariables(Table sourceTable) {
        m_tableResultVariable = new ResultVariable(sourceTable);
        ResultVariable[] resultVariables = { m_tableResultVariable };
        return resultVariables;
    }

}