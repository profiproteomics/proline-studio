package fr.proline.studio.rsmexplorer.gui.calc.functions;

import fr.proline.studio.parameter.AbstractLinkedParameters;
import fr.proline.studio.parameter.ObjectParameter;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.python.data.Table;
import fr.proline.studio.python.interpreter.ResultVariable;
import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import fr.proline.studio.rsmexplorer.gui.calc.ProcessCallbackInterface;
import fr.proline.studio.rsmexplorer.gui.calc.graph.AbstractConnectedGraphObject;
import fr.proline.studio.rsmexplorer.gui.calc.graph.FunctionGraphNode;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphConnector;
import fr.proline.studio.table.GlobalTableModelInterface;

/**
 *
 * @author JM235353
 */
public class NormalizationFunction extends AbstractOnExperienceDesignFunction {

    private static final String FAMILY_STRONG_RESCALING = "Global Rescaling";
    private static final String FAMILY_MEDIAN_CENTERING = "Median Centering";
    private static final String FAMILY_MEAN_CENTERING = "Mean Centering";
    private static final String FAMILY_MEAN_CENTERING_SCALING = "Mean Centering Scaling";
    private static final String FAMILY_NONE = "None";
    
    
    private static final String FAMILY_STRONG_OPTION1 = "sum by columns";
    private static final String FAMILY_STRONG_OPTION2 = "quantiles";
    
    private static final String FAMILY_OTHERS_OPTION1 = "overall";
    private static final String FAMILY_OTHERS_OPTION2 = "within conditions";
    
    private static final String NORMALIZATION_FAMILY_KEY = "NORMALIZATION_FAMILY";
    private static final String NORMALIZATION_FAMILY_NAME = "Normalization Type";
    
    private static final String NORMALIZATION_STRONG_OPTION_KEY = "NORMALIZATION_STRONG_OPTION";
    private static final String NORMALIZATION_OTHERS_OPTION_KEY = "NORMALIZATION_OTHERS_OPTION";
    private static final String NORMALIZATION_OPTION_NAME_1 = "Normalization Option (Strong)";
    private static final String NORMALIZATION_OPTION_NAME_2 = "Normalization Option (Others)";
    
    private static final String NORMALIZATION_FUNCTION_PARAMETER_LIST_NAME = "normalize options";

    private ObjectParameter m_normalizationParameter = null;
    private ObjectParameter m_familyStrongOptionParameter = null;
    private ObjectParameter m_familyOthersOptionParameter = null;
    
    private ResultVariable m_tableResultVariable = null;
    
    private ParameterList m_parameterList = null;
    
    public NormalizationFunction(GraphPanel panel) {
        super(panel, "Normalization", "normalize", "normalize", null, null);
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
        AbstractFunction clone = new NormalizationFunction(p);
        clone.cloneInfo(this);
        return clone;
    }
    
    @Override
    public ParameterList getExtraParameterList() {

        m_parameterList = new ParameterList(NORMALIZATION_FUNCTION_PARAMETER_LIST_NAME);

        String[] normalizationFamily = { FAMILY_STRONG_RESCALING, FAMILY_MEDIAN_CENTERING, FAMILY_MEAN_CENTERING, FAMILY_MEAN_CENTERING_SCALING, FAMILY_NONE };

        String[] normalizationOption1Family = { FAMILY_STRONG_OPTION1, FAMILY_STRONG_OPTION2 };
        String[] normalizationOption2Family = { FAMILY_OTHERS_OPTION1, FAMILY_OTHERS_OPTION2 };    
        
        m_normalizationParameter = new ObjectParameter(NORMALIZATION_FAMILY_KEY, NORMALIZATION_FAMILY_NAME, null, normalizationFamily, null, 0, null);
        m_familyStrongOptionParameter = new ObjectParameter(NORMALIZATION_STRONG_OPTION_KEY, NORMALIZATION_OPTION_NAME_1, null, normalizationOption1Family, null, 0, null);
        m_familyOthersOptionParameter = new ObjectParameter(NORMALIZATION_OTHERS_OPTION_KEY, NORMALIZATION_OPTION_NAME_2, null, normalizationOption2Family, null, 0, null);

        m_parameterList.add(m_normalizationParameter);
        m_parameterList.add(m_familyStrongOptionParameter);
        m_parameterList.add(m_familyOthersOptionParameter);
        
        AbstractLinkedParameters linkedParameters = new AbstractLinkedParameters(m_parameterList) {
            @Override
            public void valueChanged(String value, Object associatedValue) {
                if (value.compareTo(FAMILY_NONE) != 0) {
                showParameter(m_familyStrongOptionParameter, (value.compareTo(FAMILY_STRONG_RESCALING) == 0));
                showParameter(m_familyOthersOptionParameter, (value.compareTo(FAMILY_STRONG_RESCALING) != 0));
                } else {
                  showParameter(m_familyStrongOptionParameter,false);
                  showParameter(m_familyOthersOptionParameter,false);  
                }
                updateParameterListPanel();
            }
            
        };
        
        m_parameterList.getPanel(); // generate panel at once
        m_normalizationParameter.addLinkedParameters(linkedParameters); // link parameter, it will modify the panel
        
        //m_parameterList.loadParameters(NbPreferences.root());
        
        return m_parameterList;
        
    }
    
    @Override
    public String getExtraValuesForFunctionCall() {
        
        String normalizationParameter = m_normalizationParameter.getStringValue();
        if (normalizationParameter.compareTo(FAMILY_STRONG_RESCALING) == 0) {
            return ",\""+normalizationParameter+"\",\""+m_familyStrongOptionParameter.getStringValue()+"\"";  
        } else if (normalizationParameter.compareTo(FAMILY_NONE) == 0) {
            return ",\""+normalizationParameter+"\",\"\"";          
        } else{
            return ",\""+normalizationParameter+"\",\""+m_familyOthersOptionParameter.getStringValue()+"\"";  
        }

    }
    
    @Override
    public ResultVariable[] getExtraVariables(Table sourceTable) {
        m_tableResultVariable = new ResultVariable(sourceTable);
        ResultVariable[] resultVariables = { m_tableResultVariable };
        return resultVariables;
    }

    @Override
    public boolean addLabelParameter() {
        return true;
    }
    
    @Override
    public void process(GraphConnector[] graphObjects, FunctionGraphNode functionGraphNode, ProcessCallbackInterface callback) {
        String normalizationParameter = m_normalizationParameter.getStringValue();
        if (normalizationParameter.compareTo(FAMILY_NONE) == 0) {
            GlobalTableModelInterface srcModel = graphObjects[0].getGlobalTableModelInterface();
            addModel(srcModel);
            
            callback.finished(functionGraphNode);
            return;
        } else {
            super.process(graphObjects, functionGraphNode, callback);
        }
    }
}