package fr.proline.studio.rsmexplorer.gui.calc.macros;

import fr.proline.studio.rsmexplorer.gui.calc.DataTree;
import fr.proline.studio.rsmexplorer.gui.calc.functions.ComputeFDRFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.DiffAnalysisFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.FilterFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.LogFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.MissingValuesImputationFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.NormalizationFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.QuantiFilterFunction;
import fr.proline.studio.rsmexplorer.gui.calc.graphics.CalibrationPlotGraphic;
import fr.proline.studio.rsmexplorer.gui.calc.graphics.ScatterGraphic;

/**
 *
 * @author JM235353
 */
public class ProStarMacro extends AbstractMacro {

    public ProStarMacro() {
        
        // create nodes (functions or graphics) and links
        addNode(new DataTree.FunctionNode(new FilterFunction(null)), 0, 0);
        addNode(new DataTree.FunctionNode(new LogFunction(null, false)), 1, 0); addLink(0,0,1,0);
        addNode(new DataTree.FunctionNode(new QuantiFilterFunction(null)), 2, 0); addLink(1,0,2,0);
        addNode(new DataTree.FunctionNode(new NormalizationFunction(null)), 3, 0); addLink(2,0,3,0);
        addNode(new DataTree.FunctionNode(new MissingValuesImputationFunction(null)), 4, 0); addLink(3,0,4,0);
        addNode(new DataTree.FunctionNode(new DiffAnalysisFunction(null)), 5, 0); addLink(4,0,5,0);
        
        CalibrationPlotGraphic cpg = new CalibrationPlotGraphic(null); cpg.setAutoDisplayDuringProcess();
        addNode(new DataTree.GraphicNode(cpg), 6, 0); addLink(5,0,6,0);
        
        ScatterGraphic sg = new ScatterGraphic(null); sg.setAutoDisplayDuringProcess();
        addNode(new DataTree.GraphicNode(sg), 6, 1); addLink(5,0,6,1);
        addNode(new DataTree.FunctionNode(new ComputeFDRFunction(null)), 6, 2); addLink(5,0,6,2);

        
    }
    
    @Override
    public String getName() {
        return "ProStar";
    }
    
    

    
}
