package fr.proline.studio.rsmexplorer.gui.calc.functions;

import fr.proline.studio.gui.AdvancedSelectionPanel;
import fr.proline.studio.parameter.MultiObjectParameter;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.python.data.FilterColumnTableModel;
import fr.proline.studio.python.interpreter.CalcError;
import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import fr.proline.studio.rsmexplorer.gui.calc.ProcessCallbackInterface;
import fr.proline.studio.rsmexplorer.gui.calc.graph.FunctionGraphNode;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphConnector;
import fr.proline.studio.table.GlobalTableModelInterface;
import java.util.ArrayList;

/**
 * Function used to remove columns from a model.
 * @author JM235353
 */
public class ColumnFilterFunction extends AbstractFunction {

    
    private static final String COLUMNS_VISIBILITY_KEY = "COLUMNS_VISIBILITY_KEY";
    
    private MultiObjectParameter<ColumnIdentifier> m_columnsVisibilityParameter = null;
    
    public ColumnFilterFunction(GraphPanel panel) {
        super(panel);
    }

    @Override
    public void inLinkDeleted() {
        super.inLinkDeleted();

        m_columnsVisibilityParameter = null;
    }

    @Override
    public String getName(int index) {
        return "Columns Filter";
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
        
        if (m_parameters == null) {
            return false;
        }
        
        if (m_columnsVisibilityParameter == null) {
            return false;
        }

        return true;
    }

    @Override
    public boolean calculationDone() {
        if (m_globalTableModelInterface != null) {
            return true;
        }
        return false;
    }

    @Override
    public AbstractFunction cloneFunction(GraphPanel p) {
        AbstractFunction clone = new ColumnFilterFunction(p);
        clone.cloneInfo(this);
        return clone;
    }

    @Override
    public void process(GraphConnector[] graphObjects, final FunctionGraphNode functionGraphNode, ProcessCallbackInterface callback) {
        setInError(false, null);
        
        // check if we have already processed
        if (m_globalTableModelInterface != null) {
            callback.finished(functionGraphNode);
            return;
        }
        
        if (m_parameters == null) {
            callback.finished(functionGraphNode);
            return;
        }

        m_columnsVisibilityParameter.getAssociatedValues(true);
        
        ArrayList<Integer> colList = (ArrayList<Integer>) m_columnsVisibilityParameter.getAssociatedValues(true);
        if (colList == null) {
            callback.finished(functionGraphNode);
            return;
        }

        int size = colList.size();
        int[] colListArray = new int[size];
        for (int i=0;i<size;i++) {
            colListArray[i] = colList.get(i);
        }
        
        setCalculating(true);
        
        try {

            addModel(new FilterColumnTableModel(graphObjects[0].getGlobalTableModelInterface(), colListArray));

            
            setCalculating(false);
            callback.finished(functionGraphNode);
        } catch (Exception e) {
            setInError(new CalcError(e, null, -1));
            setCalculating(false);
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

        ParameterList parameterTableList = new ParameterList("Table Parameters");

        GlobalTableModelInterface model =  graphObjects[0].getGlobalTableModelInterface();
        int colCount = model.getColumnCount();


        ColumnIdentifier[] columnNamesArray = new ColumnIdentifier[colCount];
        Integer[] columnNamesIndexArray = new Integer[colCount];
        boolean[] selection = new boolean[colCount];


        for (int i = 0; i < colCount; i++) {
            String columnFullName = model.getColumnName(i);
            columnNamesArray[i] = new ColumnIdentifier(columnFullName.replaceAll("<br/>", " "));
            columnNamesIndexArray[i] = i;

            selection[i] = true;
        }

        m_columnsVisibilityParameter = new MultiObjectParameter<>(COLUMNS_VISIBILITY_KEY, "Columns Visibility", "Visible Columns", "Hidden Columns", AdvancedSelectionPanel.class, columnNamesArray, columnNamesIndexArray, selection, null);

        parameterTableList.add(m_columnsVisibilityParameter);      

        
        
        m_parameters = new ParameterList[1];
        m_parameters[0] = parameterTableList;

        parameterTableList.getPanel(); // generate panel at once

    }

    @Override
    public ParameterError checkParameters(GraphConnector[] graphObjects) {

        return null;
    }



    @Override
    public void userParametersChanged() {
        m_globalTableModelInterface = null;

    }
    

    
    public class ColumnGroup implements Comparable<ColumnGroup> {

        public String m_groupName;
        public boolean m_selected;

        public ColumnGroup(String groupName, boolean selected) {
            m_groupName = groupName;
            m_selected = selected;
        }

        @Override
        public int compareTo(ColumnGroup o) {
            return m_groupName.compareTo(o.m_groupName);
        }

    }
    
    
    public class ColumnIdentifier {

        private String m_name;
        
        public ColumnIdentifier(String name) {
            m_name = name;
        }
        
        @Override
        public String toString() {
            return m_name;
        }
    }

}
