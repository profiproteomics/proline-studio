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

    private static final String DIFF_ANALYSIS_TYPE_KEY = "DIFF_ANALYSIS_TYPE";
    private static final String DIFF_ANALYSIS_TYPE_NAME = "Diff Analysis Type";

    private static final String DIFF_ANALYSIS_FUNTION_PARAMETER_LIST_NAME = "diff analysis options";

    private ParameterList m_parameterList = null;

    private ObjectParameter m_diffAnalysisTypeParameter = null;

    private ResultVariable m_tableResultVariable = null;

    public DiffAnalysisFunction(GraphPanel panel) {
        super(panel, "XIC Differential Analysis", "xic_diffanalysis", "diffanalysis", null, null);
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
        AbstractFunction clone = new DiffAnalysisFunction(p);
        clone.cloneInfo(this);
        return clone;
    }

    @Override
    public ParameterList getExtraParameterList() {

        m_parameterList = new ParameterList(DIFF_ANALYSIS_FUNTION_PARAMETER_LIST_NAME);

        String[] diffAnalysisTypes = {DIFF_ANALYSIS_LIMMA, DIFF_ANALYSIS_WELCH};

        m_diffAnalysisTypeParameter = new ObjectParameter(DIFF_ANALYSIS_TYPE_KEY, DIFF_ANALYSIS_TYPE_NAME, null, diffAnalysisTypes, null, 0, null);

        m_parameterList.add(m_diffAnalysisTypeParameter);
        
        //m_parameterList.loadParameters(NbPreferences.root());

        return m_parameterList;

    }

    @Override
    public String getExtraValuesForFunctionCall() {

        String diffAnalysisParameter = m_diffAnalysisTypeParameter.getStringValue();
        if (diffAnalysisParameter.compareTo(DIFF_ANALYSIS_LIMMA) == 0) {
            return ",\"" + diffAnalysisParameter + "\"";
        } else {
            return ",\"" + diffAnalysisParameter + "\"";
        }

    }

    @Override
    public ResultVariable[] getExtraVariables(Table sourceTable) {
        m_tableResultVariable = new ResultVariable(sourceTable);
        ResultVariable[] resultVariables = {m_tableResultVariable};
        return resultVariables;
    }

    @Override
    public boolean addLabelParameter() {
        return true;
    }
}
