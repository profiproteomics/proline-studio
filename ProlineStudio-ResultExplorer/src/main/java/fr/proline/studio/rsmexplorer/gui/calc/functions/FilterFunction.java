package fr.proline.studio.rsmexplorer.gui.calc.functions;

import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.FilterDialog;
import fr.proline.studio.filter.FilterTableModel;
import fr.proline.studio.filter.FilterTableModelInterface;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import fr.proline.studio.rsmexplorer.gui.calc.ProcessCallbackInterface;
import fr.proline.studio.rsmexplorer.gui.calc.graph.FunctionGraphNode;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphConnector;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphNode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import org.openide.windows.WindowManager;

/**
 *
 * @author JM235353
 */
public class FilterFunction extends AbstractFunction {

    private Filter[] m_filters = null;
    private boolean m_settingsDone = false;
    private boolean m_calculationDone = false;
    
    
    public FilterFunction(GraphPanel panel) {
        super(panel);
    }

    @Override
    public void inLinkDeleted() {
        super.inLinkDeleted();

        m_filters = null;
        m_settingsDone = false;
        m_calculationDone = false;
    }

    @Override
    public String getName() {
        return "Rows Filter";
    }

    @Override
    public int getNumberOfInParameters() {
        return 1;
    }
    
    @Override
    public int getNumberOfOutParameters() {
        return 1;
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

            setCalculating(false);

        } finally {
            m_calculationDone = true;
            callback.finished(functionGraphNode);
        }

    }

    @Override
    public void askDisplay(FunctionGraphNode functionGraphNode, int index) {
        display(functionGraphNode.getPreviousDataName(), getName(), index);
    }
    
    @Override
    public ArrayList<WindowBox> getDisplayWindowBox(FunctionGraphNode functionGraphNode, int index) {
        return getDisplayWindowBoxList(functionGraphNode.getPreviousDataName(), getName(), index);
    }

    @Override
    public void generateDefaultParameters(GraphConnector[] graphObjects) {

        addModel(new FilterTableModel(graphObjects[0].getGlobalTableModelInterface()));

        
        LinkedHashMap<Integer, Filter> filtersMap = ((FilterTableModelInterface) m_globalTableModelInterface.get(0)).getFilters();
        m_filters = new Filter[filtersMap.size()];
        int index = 0;
        for (Map.Entry<Integer, Filter> entry : filtersMap.entrySet()) {
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
        dialog.setFilers(m_filters);


        m_settingsBeingDone = true;
        try {
            dialog.setVisible(true);

            if (dialog.getButtonClicked() == FilterDialog.BUTTON_OK) {
                userParametersChanged();
                m_settingsDone = true;

                ((FilterTableModel) m_globalTableModelInterface.get(0)).fireTableDataChanged();
                
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
