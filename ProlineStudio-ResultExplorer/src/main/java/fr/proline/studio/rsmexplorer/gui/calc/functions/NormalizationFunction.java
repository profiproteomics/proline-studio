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
public class NormalizationFunction extends AbstractOnExperienceDesignFunction {

    private static final String FAMILY_STRONG_RESCALING = "Global Rescaling";
    private static final String FAMILY_MEDIAN_CENTERING = "Median Centering";
    private static final String FAMILY_MEAN_CENTERING = "Mean Centering";
    private static final String FAMILY_MEAN_CENTERING_SCALING = "Mean Centering Scaling";
    
    private static final String FAMILY_STRONG_OPTION1 = "sum by columns";
    private static final String FAMILY_STRONG_OPTION2 = "quantiles";
    
    private static final String FAMILY_OTHERS_OPTION1 = "overall";
    private static final String FAMILY_OTHERS_OPTION2 = "within conditions";
    
    private static final String NORMALIZATION_FAMILY = "NORMALIZATION_FAMILY";
    private static final String NORMALIZATION_STRONG_OPTION = "NORMALIZATION_STRONG_OPTION";
    private static final String NORMALIZATION_OTHERS_OPTION = "NORMALIZATION_OTHERS_OPTION";

    private ObjectParameter m_normalizationParameter = null;
    private ObjectParameter m_familyStrongOptionParameter = null;
    private ObjectParameter m_familyOthersOptionParameter = null;
    
    private ResultVariable m_tableResultVariable = null;
    
    public NormalizationFunction(GraphPanel panel) {
        super(panel, "Normalize", "normalize", "normalize", null);
    }
    
    @Override
    public int getMinGroups() {
        return 2;
    }

    @Override
    public int getMaxGroups() {
        return 3;
    }

    @Override
    public AbstractFunction cloneFunction(GraphPanel p) {
        AbstractFunction clone = new NormalizationFunction(p);
        clone.cloneInfo(this);
        return clone;
    }
    
    @Override
    public ParameterList getExtraParameterList() {

        ParameterList parameterList = new ParameterList("normalize options");

        String[] normalizationFamily = { FAMILY_STRONG_RESCALING, FAMILY_MEDIAN_CENTERING, FAMILY_MEAN_CENTERING, FAMILY_MEAN_CENTERING_SCALING };

        String[] normalizationOption1Family = { FAMILY_STRONG_OPTION1, FAMILY_STRONG_OPTION2 };
        String[] normalizationOption2Family = { FAMILY_OTHERS_OPTION1, FAMILY_OTHERS_OPTION2 };
        
        
        m_normalizationParameter = new ObjectParameter(NORMALIZATION_FAMILY, "Normalization Type", null, normalizationFamily, null, 0, null);
        m_familyStrongOptionParameter = new ObjectParameter(NORMALIZATION_STRONG_OPTION, "Normalization Option", null, normalizationOption1Family, null, 0, null);
        m_familyOthersOptionParameter = new ObjectParameter(NORMALIZATION_OTHERS_OPTION, "Normalization Option", null, normalizationOption2Family, null, 0, null);

        parameterList.add(m_normalizationParameter);
        parameterList.add(m_familyStrongOptionParameter);
        parameterList.add(m_familyOthersOptionParameter);
        
        
        AbstractLinkedParameters linkedParameters = new AbstractLinkedParameters(parameterList) {
            @Override
            public void valueChanged(String value, Object associatedValue) {
                showParameter(m_familyStrongOptionParameter, (value.compareTo(FAMILY_STRONG_RESCALING) == 0));
                showParameter(m_familyOthersOptionParameter, (value.compareTo(FAMILY_STRONG_RESCALING) != 0));

                updataParameterListPanel();
            }
            
        };


        
        parameterList.getPanel(); // generate panel at once
        m_normalizationParameter.addLinkedParameters(linkedParameters); // link parameter, it will modify the panel


        
        return parameterList;
        
    }
    
    @Override
    public String getExtraValuesForFunctionCall() {
        
        String normalizationParameter = m_normalizationParameter.getStringValue();
        if (normalizationParameter.compareTo(FAMILY_STRONG_RESCALING) == 0) {
            return ",\""+normalizationParameter+"\",\""+m_familyStrongOptionParameter.getStringValue()+"\"";  
        } else {
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
}