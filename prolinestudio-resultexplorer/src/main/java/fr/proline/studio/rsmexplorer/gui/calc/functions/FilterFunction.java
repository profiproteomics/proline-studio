/* 
 * Copyright (C) 2019
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
package fr.proline.studio.rsmexplorer.gui.calc.functions;

import fr.proline.studio.filter.FilterDialog;
import fr.proline.studio.filter.FilterMirroredTableModel;
import fr.proline.studio.filter.FilterTableModel;
import fr.proline.studio.filter.FilterTableModelInterface;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import fr.proline.studio.rsmexplorer.gui.calc.ProcessCallbackInterface;
import static fr.proline.studio.rsmexplorer.gui.calc.functions.ComputeFDRFunction.OUT_DATA_DIFFERENTIAL_PROTEINS;
import static fr.proline.studio.rsmexplorer.gui.calc.functions.ComputeFDRFunction.OUT_DATA_FDR;
import fr.proline.studio.rsmexplorer.gui.calc.graph.FunctionGraphNode;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphConnector;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphNode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import fr.proline.studio.WindowManager;

/**
 *
 * @author JM235353
 */
public class FilterFunction extends AbstractFunction {

    private fr.proline.studio.filter.Filter[] m_filters = null;
    private boolean m_settingsDone = false;
    private boolean m_calculationDone = false;
    
    public final static int OUT_DATA_FILTER = 0;
    public final static int OUT_DATA_REVERSE_FILTER = 1;
    public final static int OUT_VALUES_NUMBER = 2;
    
    
    public FilterFunction(GraphPanel panel) {
        super(panel, FUNCTION_TYPE.FilterFunction);
    }

    @Override
    public void inLinkModified() {
        super.inLinkModified();

        m_filters = null;
        m_settingsDone = false;
        m_calculationDone = false;
    }

    @Override
    public String getName(int index) {
        
        if (index>=0) {
            return "Rows Filter / "+getOutTooltip(index);
        }
        
        return "Rows Filter";
    }

    @Override
    public int getNumberOfInParameters() {
        return 1;
    }
    
    @Override
    public int getNumberOfOutParameters() {
        return OUT_VALUES_NUMBER;
    }
    
    @Override
    public String getOutTooltip(int index) {
        switch (index) {
            case OUT_DATA_FDR:
                return "Values Kept";
            case OUT_DATA_DIFFERENTIAL_PROTEINS:
                return "Values Filtered";
        }
        return null;
    }


    @Override
    public boolean settingsDone() {
        
        return m_settingsDone;
    }

    @Override
    public boolean calculationDone() {
        return m_calculationDone;
    }

    @Override
    public AbstractFunction cloneFunction(GraphPanel p) {
        AbstractFunction clone = new FilterFunction(p);
        clone.cloneInfo(this);
        return clone;
    }

    @Override
    public void process(GraphConnector[] graphObjects, FunctionGraphNode functionGraphNode, ProcessCallbackInterface callback) {
        
        // check if we have already processed
        if (m_calculationDone) {
            callback.finished(functionGraphNode);
            return;
        }
        try {
            setCalculating(true);
            setInError(false, null);

            ((FilterTableModelInterface) m_globalTableModelInterface.get(0)).filter();
            ((FilterTableModelInterface) m_globalTableModelInterface.get(1)).filter();

            setCalculating(false);

        } finally {
            m_calculationDone = true;
            callback.finished(functionGraphNode);
        }

    }

    @Override
    public void askDisplay(FunctionGraphNode functionGraphNode, int index) {
        display(functionGraphNode.getPreviousDataName(), getName(index), index);
    }
    
    @Override
    public ArrayList<WindowBox> getDisplayWindowBox(FunctionGraphNode functionGraphNode, int index) {
        return getDisplayWindowBoxList(functionGraphNode.getPreviousDataName(), getName(index), index);
    }

    @Override
    public void generateDefaultParameters(GraphConnector[] graphObjects) {

        FilterTableModel filterTableModel = new FilterTableModel(graphObjects[0].getGlobalTableModelInterface());
        addModel(filterTableModel);
        addModel(new FilterMirroredTableModel(filterTableModel));
        
        LinkedHashMap<Integer, fr.proline.studio.filter.Filter> filtersMap = ((FilterTableModelInterface) m_globalTableModelInterface.get(0)).getFilters();
        m_filters = new fr.proline.studio.filter.Filter[filtersMap.size()];
        int index = 0;
        for (Map.Entry<Integer, fr.proline.studio.filter.Filter> entry : filtersMap.entrySet()) {
            m_filters[index++] = entry.getValue();
        }
        
        

    }

    @Override
    public ParameterError checkParameters(GraphConnector[] graphObjects) {

        return null;
    }

    @Override
    public boolean settings(GraphConnector[] graphObjects, GraphNode node) {
        if (m_filters == null) {
            generateDefaultParameters(graphObjects);
        }

        FilterDialog dialog = FilterDialog.getDialog(WindowManager.getDefault().getMainWindow());
        dialog.setImageInfo(m_panel, node.getX()-90, node.getY()-50, node.getXEnd()-node.getX()+180, node.getYEnd()-node.getY()+100);
        dialog.centerToWindow(WindowManager.getDefault().getMainWindow());
        dialog.setFilters(m_filters);


        m_settingsBeingDone = true;
        try {
            dialog.setVisible(true);

            if (dialog.getButtonClicked() == FilterDialog.BUTTON_OK) {
                userParametersChanged();
                m_settingsDone = true;

                ((FilterTableModel) m_globalTableModelInterface.get(0)).fireTableDataChanged();
                ((FilterTableModel) m_globalTableModelInterface.get(1)).fireTableDataChanged();
                
                return true;
            }

            return false;
        } finally {
            m_settingsBeingDone = false;
        }
    }

    @Override
    public void userParametersChanged() {
        m_calculationDone = false;

    }

}
