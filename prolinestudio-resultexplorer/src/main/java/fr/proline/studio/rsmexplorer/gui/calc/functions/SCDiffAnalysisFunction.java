package fr.proline.studio.rsmexplorer.gui.calc.functions;

import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import fr.proline.studio.types.PValue;


/**
 * Beta Binomial Function for the data analyzer
 * @author JM235353
 */
public class SCDiffAnalysisFunction extends AbstractOnExperienceDesignFunction {

    public SCDiffAnalysisFunction(GraphPanel panel) {
        super(panel, FUNCTION_TYPE.SCDiffAnalysisFunction, "SC Differential Analysis", "sc_diffanalysis", "bbinomial", null, new PValue());
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
        AbstractFunction clone = new SCDiffAnalysisFunction(p);
        clone.cloneInfo(this);
        return clone;
    }





}
