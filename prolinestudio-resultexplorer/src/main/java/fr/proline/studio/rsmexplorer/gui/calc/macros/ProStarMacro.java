/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.rsmexplorer.gui.calc.macros;

import fr.proline.studio.graphics.PlotType;
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
import fr.proline.studio.rsmexplorer.gui.calc.graphics.ScatterOrHistogramGraphic;
import java.util.ArrayList;

/**
 *
 * @author JM235353
 */
public class ProStarMacro extends AbstractMacro {

    public ProStarMacro() {
        
        // create nodes (functions or graphics) and links
        addNode(new DataTree.FunctionNode(new ColumnFilterFunction(null), false), 0, 0);
        addNode(new DataTree.FunctionNode(new FilterFunction(null), false), 1, 0); addLink(0,0,1,0);
        addNode(new DataTree.FunctionNode(new LogFunction(null, false), false), 2, 0); addLink(1,0,2,0);
        addNode(new DataTree.FunctionNode(new QuantiFilterFunction(null), true), 3, 0); addLink(2,0,3,0);
        addNode(new DataTree.FunctionNode(new NormalizationFunction(null), true), 4, 0); addLink(3,0,4,0);
        addNode(new DataTree.FunctionNode(new MissingValuesImputationFunction(null), true), 5, 0); addLink(4,0,5,0);
        addNode(new DataTree.FunctionNode(new DiffAnalysisFunction(null), true), 6, 0); addLink(5,0,6,0);
        
        ArrayList<SplittedPanelContainer.PanelLayout> layoutList = new ArrayList<>();
        layoutList.add(SplittedPanelContainer.PanelLayout.VERTICAL);
        CalibrationPlotGraphic cpg = new CalibrationPlotGraphic(null); cpg.setAutoDisplayLayoutDuringProcess(layoutList);
        addNode(new DataTree.GraphicNode(cpg, true), 7, 0); addLink(6,0,7,0);
        
        ScatterOrHistogramGraphic sg = new ScatterOrHistogramGraphic(null, PlotType.SCATTER_PLOT); //sg.setAutoDisplayLayoutDuringProcess(SplittedPanelContainer.PanelLayout.HORIZONTAL);
        addNode(new DataTree.GraphicNode(sg, false), 7, 1); addLink(6,0,7,1);
        
        layoutList = new ArrayList<>();
        layoutList.add(SplittedPanelContainer.PanelLayout.HORIZONTAL);
        layoutList.add(SplittedPanelContainer.PanelLayout.VERTICAL);
        layoutList.add(SplittedPanelContainer.PanelLayout.HORIZONTAL);
        ComputeFDRFunction FDRFunction = new ComputeFDRFunction(null); FDRFunction.setAutoDisplayLayoutDuringProcess(layoutList);
        addNode(new DataTree.FunctionNode(FDRFunction, true), 7, 2); addLink(6,0,7,2);

        
    }
    
    @Override
    public String getName() {
        return "ProStar";
    }
    
    

    
}
