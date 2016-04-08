package fr.proline.studio.rsmexplorer.gui.calc.functions;


import fr.proline.studio.parameter.ObjectParameter;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.python.data.Table;
import fr.proline.studio.python.interpreter.ResultVariable;
import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;

/**
 *
 * @author JM235353
 */
public class DiffAnalysisFunction extends AbstractOnExperienceDesignFunction {

    private static final String DIFF_ANALYSIS_LIMMA = "Limma";
    private static final String DIFF_ANALYSIS_WELCH = "Welch";

    
    private static final String DIFF_ANALYSIS_TYPE = "DIFF_ANALYSIS_TYPE";


    private ObjectParameter m_diffAnalysisTypeParameter = null;

    
    private ResultVariable m_tableResultVariable = null;
    
    public DiffAnalysisFunction(GraphPanel panel) {
        super(panel, "DiffAnalysis", "diffanalysis", "diffanalysis", null);
    }
    
    @Override
    public int getMinGroups() {
        return 2;
    }

    @Override
    public int getMaxGroups() {
        return 2;
    }

    @Override
    public AbstractFunction cloneFunction(GraphPanel p) {
        AbstractFunction clone = new DiffAnalysisFunction(p);
        clone.cloneInfo(this);
        return clone;
    }
    
    @Override
    public ParameterList getExtraParameterList() {

        ParameterList parameterList = new ParameterList("diff analysis options");

        String[] diffAnalysisTypes = { /*DIFF_ANALYSIS_LIMMA,*/ DIFF_ANALYSIS_WELCH  };  //JPM.TODO

        
        
        m_diffAnalysisTypeParameter = new ObjectParameter(DIFF_ANALYSIS_TYPE, "Diff Analysis Type", null, diffAnalysisTypes, null, 0, null);

        parameterList.add(m_diffAnalysisTypeParameter);

        
        return parameterList;
        
    }
    
    @Override
    public String getExtraValuesForFunctionCall() {
        
        String diffAnalysisParameter = m_diffAnalysisTypeParameter.getStringValue();
        if (diffAnalysisParameter.compareTo(DIFF_ANALYSIS_LIMMA) == 0) {
            return ",\""+diffAnalysisParameter+"\"";  
        } else {
            return ",\""+diffAnalysisParameter+"\"";  
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