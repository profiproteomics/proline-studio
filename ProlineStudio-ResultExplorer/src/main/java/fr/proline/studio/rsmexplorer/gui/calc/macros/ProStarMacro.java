package fr.proline.studio.rsmexplorer.gui.calc.macros;

import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.rsmexplorer.gui.calc.DataTree;
import fr.proline.studio.rsmexplorer.gui.calc.functions.ColumnFilterFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.ComputeFDRFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.DiffAnalysisFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.FilterFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.LogFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.MissingValuesImputationFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.NormalizationFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.QuantiFilterFunction;
import fr.proline.studio.rsmexplorer.gui.calc.graphics.CalibrationPlotGraphic;
import fr.proline.studio.rsmexplorer.gui.calc.graphics.ScatterGraphic;
import java.util.ArrayList;

/**
 *
 * @author JM235353
 */
public class ProStarMacro extends AbstractMacro {

    public ProStarMacro() {
        
        // create nodes (functions or graphics) and links
        addNode(new DataTree.FunctionNode(new ColumnFilterFunction(null)), 0, 0);
        addNode(new DataTree.FunctionNode(new FilterFunction(null)), 1, 0); addLink(0,0,1,0);
        addNode(new DataTree.FunctionNode(new LogFunction(null, false)), 2, 0); addLink(1,0,2,0);
        addNode(new DataTree.FunctionNode(new QuantiFilterFunction(null)), 3, 0); addLink(2,0,3,0);
        addNode(new DataTree.FunctionNode(new NormalizationFunction(null)), 4, 0); addLink(3,0,4,0);
        addNode(new DataTree.FunctionNode(new MissingValuesImputationFunction(null)), 5, 0); addLink(4,0,5,0);
        addNode(new DataTree.FunctionNode(new DiffAnalysisFunction(null)), 6, 0); addLink(5,0,6,0);
        
        ArrayList<SplittedPanelContainer.PanelLayout> layoutList = new ArrayList<>();
        layoutList.add(SplittedPanelContainer.PanelLayout.VERTICAL);
        CalibrationPlotGraphic cpg = new CalibrationPlotGraphic(null); cpg.setAutoDisplayLayoutDuringProcess(layoutList);
        addNode(new DataTree.GraphicNode(cpg), 7, 0); addLink(6,0,7,0);
        
        ScatterGraphic sg = new ScatterGraphic(null); //sg.setAutoDisplayLayoutDuringProcess(SplittedPanelContainer.PanelLayout.HORIZONTAL);
        addNode(new DataTree.GraphicNode(sg), 7, 1); addLink(6,0,7,1);
        
        layoutList = new ArrayList<>();
        layoutList.add(SplittedPanelContainer.PanelLayout.HORIZONTAL);
        layoutList.add(SplittedPanelContainer.PanelLayout.VERTICAL);
        layoutList.add(SplittedPanelContainer.PanelLayout.HORIZONTAL);
        ComputeFDRFunction FDRFunction = new ComputeFDRFunction(null); FDRFunction.setAutoDisplayLayoutDuringProcess(layoutList);
        addNode(new DataTree.FunctionNode(FDRFunction), 7, 2); addLink(6,0,7,2);

        
    }
    
    @Override
    public String getName() {
        return "ProStar";
    }
    
    

    
}
