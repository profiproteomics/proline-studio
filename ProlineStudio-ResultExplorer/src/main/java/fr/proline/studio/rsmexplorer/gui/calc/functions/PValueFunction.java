package fr.proline.studio.rsmexplorer.gui.calc.functions;

import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;


/**
 * PValue Function for the data analyzer
 * @author JM235353
 */
public class PValueFunction extends AbstractOnExperienceDesignFunction {

    public PValueFunction(GraphPanel panel) {
        super(panel, "pvalue", "pvalue", "pvalue");
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
        return new PValueFunction(p);
    }





}
