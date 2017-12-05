package fr.proline.studio.rsmexplorer.gui.calc.functions;

import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;

/**
 * Ttd Function for the data analyzer
 *
 * @author JM235353
 */
public class TtdFunction extends AbstractOnExperienceDesignFunction {

    public TtdFunction(GraphPanel panel) {
        super(panel, FUNCTION_TYPE.TtdFunction, "ttd", "ttd", "ttd", null, null);
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
        AbstractFunction clone = new TtdFunction(p);
        clone.cloneInfo(this);
        return clone;
    }

}

