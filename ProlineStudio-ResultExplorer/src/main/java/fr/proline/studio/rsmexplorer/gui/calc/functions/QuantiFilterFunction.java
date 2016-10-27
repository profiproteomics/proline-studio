package fr.proline.studio.rsmexplorer.gui.calc.functions;

import fr.proline.studio.parameter.IntegerParameter;
import fr.proline.studio.parameter.ObjectParameter;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.python.data.Table;
import fr.proline.studio.python.interpreter.ResultVariable;
import fr.proline.studio.python.model.QuantiFilterModel;
import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import javax.swing.JTextField;
import org.openide.util.NbPreferences;

/**
 *
 * @author JM235353
 */
public class QuantiFilterFunction extends AbstractOnExperienceDesignFunction {

    private static final String GROUP_SEL_KEY = "GROUP_SEL";
    private static final String GROUP_SEL_NAME = "Missing Values Algorithm";
    
    private static final String INTENSITY_VALUE_KEY = "INTENSITY_VALUE";
    private static final String INTENSITY_VALUE_NAME = "Number of Intensity Values";
    
    private static final String QUANTI_FILTER_FUNTION_PARAMETER_LIST_NAME = "Missing Values Filter";
    
    
    
    private ParameterList m_parameterList;
    

    private ObjectParameter m_groupsParameter = null;
    private IntegerParameter m_intensityThreshold = null;
    
    private ResultVariable m_tableResultVariable = null;
    
    
    
    public QuantiFilterFunction(GraphPanel panel) {
        super(panel, "Missing Values Filter", "quantifilter", "quantifilter", null);
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
        AbstractFunction clone = new QuantiFilterFunction(p);
        clone.cloneInfo(this);
        return clone;
    }
    
    @Override
    public ParameterList getExtraParameterList() {

        m_parameterList = new ParameterList(QUANTI_FILTER_FUNTION_PARAMETER_LIST_NAME);

        String[] options = { "Whole groups", "For every group", "At least one group" };
        Integer[] associatedOptions = {QuantiFilterModel.WHOLE_GROUPS, QuantiFilterModel.EVERY_GROUP, QuantiFilterModel.AT_LEAST_ONE_GROUP };       
        
        m_groupsParameter = new ObjectParameter(GROUP_SEL_KEY, GROUP_SEL_NAME, null, options, associatedOptions, -1, null);
        
        m_intensityThreshold = new IntegerParameter(INTENSITY_VALUE_KEY, INTENSITY_VALUE_NAME, JTextField.class, 0, 0, null);
        
        m_parameterList.add(m_groupsParameter);
        m_parameterList.add(m_intensityThreshold);
        
        //m_parameterList.loadParameters(NbPreferences.root(), true);
        
        return m_parameterList;
        
    }
    
    @Override
    public String getExtraValuesForFunctionCall() {
        return ","+m_tableResultVariable.getName()+","+m_groupsParameter.getAssociatedObjectValue().toString()+","+m_intensityThreshold.getStringValue();   
    }
    
    @Override
    public ResultVariable[] getExtraVariables(Table sourceTable) {
        m_tableResultVariable = new ResultVariable(sourceTable);
        ResultVariable[] resultVariables = { m_tableResultVariable };
        return resultVariables;
    }

}
    
