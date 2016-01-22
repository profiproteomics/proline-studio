package fr.proline.studio.rsmexplorer.gui.calc.graphics;

import fr.proline.studio.parameter.MultiObjectParameter;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.python.data.ColRef;
import fr.proline.studio.python.data.PythonImage;
import fr.proline.studio.python.data.Table;
import fr.proline.studio.python.interpreter.CalcCallback;
import fr.proline.studio.python.interpreter.CalcError;
import fr.proline.studio.python.interpreter.CalcInterpreterTask;
import fr.proline.studio.python.interpreter.CalcInterpreterThread;
import fr.proline.studio.python.interpreter.ResultVariable;
import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import fr.proline.studio.rsmexplorer.gui.calc.graph.AbstractGraphObject;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphicGraphNode;
import fr.proline.studio.table.GlobalTableModelInterface;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JM235353
 */
public class DensityPlotGraphic extends AbstractMatrixPlotGraphic {
    
    
    public DensityPlotGraphic(GraphPanel panel) {
        super(panel, "densityPlot");
    }
   
    @Override
    public String getName() {
        return "Density Plot";
    }

    @Override
    public AbstractGraphic cloneGraphic(GraphPanel p) {
        return new DensityPlotGraphic(p);
    }


}