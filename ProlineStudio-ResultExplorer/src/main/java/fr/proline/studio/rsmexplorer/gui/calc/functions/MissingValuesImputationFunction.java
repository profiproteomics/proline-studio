package fr.proline.studio.rsmexplorer.gui.calc.functions;

import fr.proline.studio.parameter.AbstractLinkedParameters;
import fr.proline.studio.parameter.ObjectParameter;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.python.data.Table;
import fr.proline.studio.python.interpreter.ResultVariable;
import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;

/**
 *
 * @author JM235353
 */
public class MissingValuesImputationFunction extends AbstractOnExperienceDesignFunction {

    private static final String  MV_IMPUTATION_METHOD = "MV_IMPUTATION_METHOD";
    
    private static final String MV_IMPUTATION_METHOD_BPCA = "BPCA";
    private static final String MV_IMPUTATION_METHOD_KNN = "KNN";
    private static final String MV_IMPUTATION_METHOD_MLE = "MLE";
    private static final String MV_IMPUTATION_METHOD_QRILC = "QRILC";

    private ObjectParameter m_methodParameter = null;

    private ResultVariable m_tableResultVariable = null;
    
    public MissingValuesImputationFunction(GraphPanel panel) {
        super(panel, "Missing Values Imputation", "mvimputation", "mvimputation", null);
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

        ParameterList parameterList = new ParameterList("mv imputation options");

        String[] mvImputationMethods = { MV_IMPUTATION_METHOD_BPCA, MV_IMPUTATION_METHOD_KNN, MV_IMPUTATION_METHOD_MLE, MV_IMPUTATION_METHOD_QRILC };

        
        m_methodParameter = new ObjectParameter(MV_IMPUTATION_METHOD, "Method", null, mvImputationMethods, null, 0, null);

        parameterList.add(m_methodParameter);

        return parameterList;
        
    }
    
    @Override
    public String getExtraValuesForFunctionCall() {
        
        String methodParameter = m_methodParameter.getStringValue();
        return ",\""+methodParameter+"\"";  


    }
    
    @Override
    public ResultVariable[] getExtraVariables(Table sourceTable) {
        m_tableResultVariable = new ResultVariable(sourceTable);
        ResultVariable[] resultVariables = { m_tableResultVariable };
        return resultVariables;
    }

}