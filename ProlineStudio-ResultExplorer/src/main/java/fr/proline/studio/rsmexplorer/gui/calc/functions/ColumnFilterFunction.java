package fr.proline.studio.rsmexplorer.gui.calc.functions;

import fr.proline.studio.export.ExportModelInterface;
import fr.proline.studio.parameter.AbstractLinkedParameters;
import fr.proline.studio.parameter.MultiObjectParameter;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.python.data.FilterColumnTableModel;
import fr.proline.studio.python.interpreter.CalcError;
import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import fr.proline.studio.rsmexplorer.gui.calc.ProcessCallbackInterface;
import fr.proline.studio.rsmexplorer.gui.calc.graph.AbstractConnectedGraphObject;
import fr.proline.studio.rsmexplorer.gui.calc.graph.FunctionGraphNode;
import fr.proline.studio.table.GlobalTableModelInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author JM235353
 */
public class ColumnFilterFunction extends AbstractFunction {

    
    private static final String COLUMNS_VISIBILITY_KEY = "COLUMNS_VISIBILITY_KEY";
    private static final String COLUMNS_GROUP_VISIBILITY_KEY = "COLUMNS_GROUP_VISIBILITY_KEY";
    
    private MultiObjectParameter m_columnsGroupVisibilityParameter = null;
    private MultiObjectParameter m_columnsVisibilityParameter = null;
    
    public ColumnFilterFunction(GraphPanel panel) {
        super(panel);
    }

    @Override
    public void inLinkDeleted() {
        super.inLinkDeleted();

        m_columnsVisibilityParameter = null;
        m_columnsGroupVisibilityParameter = null;
    }

    @Override
    public String getName() {
        return "Columns Filter";
    }

    @Override
    public int getNumberOfInParameters() {
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
    public void process(AbstractConnectedGraphObject[] graphObjects, final FunctionGraphNode functionGraphNode, ProcessCallbackInterface callback) {
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
            m_globalTableModelInterface = new FilterColumnTableModel(graphObjects[0].getGlobalTableModelInterface(), colListArray);
            
            setCalculating(false);
            callback.finished(functionGraphNode);
        } catch (Exception e) {
            setInError(new CalcError(e, null, -1));
            setCalculating(false);
            callback.finished(functionGraphNode);
        }


    }

    @Override
    public void askDisplay(FunctionGraphNode functionGraphNode) {
        display(functionGraphNode.getPreviousDataName(), getName());
    }
    
    @Override
    public WindowBox getDisplayWindowBox(FunctionGraphNode functionGraphNode) {
        return getDisplayWindowBox(functionGraphNode.getPreviousDataName(), getName());
    }

    @Override
    public void generateDefaultParameters(AbstractConnectedGraphObject[] graphObjects) {

        ParameterList parameterTableList = new ParameterList("Table Parameters");

        GlobalTableModelInterface model =  graphObjects[0].getGlobalTableModelInterface();
        int colCount = model.getColumnCount();
        
        ExportModelInterface exportableModel = null;
        if (model instanceof ExportModelInterface) {
            exportableModel = (ExportModelInterface) model;
        }
    

        Object[] columnNamesArray = new Object[colCount];
        Integer[] columnNamesIndexArray = new Integer[colCount];
        boolean[] selection = new boolean[colCount];
        
        HashMap<String, Integer> similarColumnsNumberMap = new HashMap<>();
        HashMap<String, String> similarColumnsColorsMap = new HashMap<>();
        
        for (int i = 0; i < colCount; i++) {
            String columnFullName = model.getColumnName(i);
            columnNamesArray[i] = columnFullName.replaceAll("<br/>"," ");
            columnNamesIndexArray[i] = i;
            selection[i] = true;
            
            if (exportableModel != null) {

                String columnExportName = exportableModel.getExportColumnName(i);
                int indexSpace = columnExportName.lastIndexOf(' ');
                if (indexSpace != -1) {
                    columnExportName = columnExportName.substring(0, indexSpace);
                }
                if (columnExportName.length()>0) {
                    Integer nb = similarColumnsNumberMap.get(columnExportName);
                    if (nb == null) {
                        similarColumnsNumberMap.put(columnExportName, 1);
                    } else {
                        similarColumnsNumberMap.put(columnExportName, nb+1);
                    }
                    
                    int colorIndexStart = columnFullName.indexOf("<font color='", 0);
                    int colorIndexStop = columnFullName.indexOf("</font>", 0);
                    if ((colorIndexStart>-1) && (colorIndexStop>colorIndexStart)) {
                        String colorName = columnFullName.substring(colorIndexStart,colorIndexStop+"</font>".length());
                        String curColorName = similarColumnsColorsMap.get(columnExportName);
                        if (curColorName != null) {
                            if (curColorName.compareTo(colorName) != 0) {
                                similarColumnsColorsMap.put(columnExportName, "");
                            }
                        } else {
                            similarColumnsColorsMap.put(columnExportName, colorName);
                        }
                    } else {
                        similarColumnsColorsMap.put(columnExportName, "");
                    }
                }
                if (indexSpace != -1) {
                    columnExportName = exportableModel.getExportColumnName(i);
                    columnExportName = columnExportName.substring(indexSpace, columnExportName.length());
                    if (columnExportName.length() > 0) {
                        Integer nb = similarColumnsNumberMap.get(columnExportName);
                        if (nb == null) {
                            similarColumnsNumberMap.put(columnExportName, 1);
                        } else {
                            similarColumnsNumberMap.put(columnExportName, nb + 1);
                        }
                        
                        int colorIndexStart = columnFullName.indexOf("<font color='", 0);
                        int colorIndexStop = columnFullName.indexOf("</font>", 0);
                        if ((colorIndexStart > -1) && (colorIndexStop > colorIndexStart)) {
                            String colorName = columnFullName.substring(colorIndexStart, colorIndexStop + "</font>".length());
                            String curColorName = similarColumnsColorsMap.get(columnExportName);
                            if (curColorName != null) {
                                if (curColorName.compareTo(colorName) != 0) {
                                    similarColumnsColorsMap.put(columnExportName, "");
                                }
                            } else {
                                similarColumnsColorsMap.put(columnExportName, colorName);
                            }
                        } else {
                            similarColumnsColorsMap.put(columnExportName, "");
                        }
                    }
                }
            }

        }
        
        // suppression des lignes à un résultat dans similarColumnsNumberMap
        Set<String> colNamesSet = similarColumnsNumberMap.keySet();
        String[] colNamesArray = colNamesSet.toArray(new String[colNamesSet.size()]);
        for (int i = 0; i < colNamesArray.length; i++) {
            String colName = colNamesArray[i];
            Integer nb = similarColumnsNumberMap.get(colName);
            if (nb <= 1) {
                similarColumnsNumberMap.remove(colName);
            }
        }
        String[] columnGroupNames = null;
        int nbGroups = similarColumnsNumberMap.size();
        if (nbGroups > 0) {
            columnGroupNames = new String[nbGroups];
            Iterator<String> it = similarColumnsNumberMap.keySet().iterator();
            int i = 0;
            while (it.hasNext()) {
                String name = it.next();
                String colorName = similarColumnsColorsMap.get(name);
                columnGroupNames[i] = (colorName.length() > 0) ? "<html>" + colorName + name + "</html>" : name;
                i++;
            }

            Arrays.sort( columnGroupNames );
            Object[] columnGroupNamesArray = new Object[nbGroups];
            boolean[] groupSelection = new boolean[nbGroups];
            for (i = 0; i < nbGroups; i++) {
                columnGroupNamesArray[i] = columnGroupNames[i];
                groupSelection[i] = true;

            }
            m_columnsGroupVisibilityParameter = new MultiObjectParameter(COLUMNS_GROUP_VISIBILITY_KEY, "Columns Type Visibility", null, columnGroupNamesArray, null, groupSelection, null, false);
        }
        
                
        m_columnsVisibilityParameter = new MultiObjectParameter(COLUMNS_VISIBILITY_KEY, "Columns Visibility", null, columnNamesArray, columnNamesIndexArray, selection, null, true);
        
         AbstractLinkedParameters linkedParameter2 = null;
        if (m_columnsGroupVisibilityParameter != null) {
            
            final ExportModelInterface _exportableModel = exportableModel;
            
            linkedParameter2 = new AbstractLinkedParameters(parameterTableList) {
                @Override
                public void valueChanged(String value, Object associatedValue) {
                    
                    // color
                    int colorRemoveStart = value.indexOf("</font>", 0);
                    int colorRemoveStop = value.indexOf("</html>", 0);
                    if ((colorRemoveStart>-1) && (colorRemoveStop>colorRemoveStart)) {
                        value = value.substring(colorRemoveStart+"</font>".length(), colorRemoveStop);
                    }
                    
                    for (int i = 0; i < colCount; i++) {
                        String columnExportName = _exportableModel.getExportColumnName(i);
                        int indexSpace = columnExportName.lastIndexOf(' ');
                        if (indexSpace != -1) {
                            columnExportName = columnExportName.substring(0, indexSpace);
                        }
                        if (columnExportName.compareTo(value) == 0) {
                            m_columnsVisibilityParameter.setSelection(i, ((Boolean)associatedValue).booleanValue());
                        }
                        if (indexSpace != -1) {
                            columnExportName = _exportableModel.getExportColumnName(i);
                            columnExportName = columnExportName.substring(indexSpace, columnExportName.length());
                            if (columnExportName.compareTo(value) == 0) {
                                m_columnsVisibilityParameter.setSelection(i, ((Boolean) associatedValue).booleanValue());
                            }
                        }
                    }

                }

            };
        }
        

        parameterTableList.add(m_columnsVisibilityParameter);      
        if (m_columnsGroupVisibilityParameter != null) {
            parameterTableList.add(m_columnsGroupVisibilityParameter);
        }
        
        
        
        m_parameters = new ParameterList[1];
        m_parameters[0] = parameterTableList;

        parameterTableList.getPanel(true); // generate panel at once

        
        
        if (m_columnsGroupVisibilityParameter != null) {
            m_columnsGroupVisibilityParameter.addLinkedParameters(linkedParameter2);
        }
        


    }

    @Override
    public ParameterError checkParameters(AbstractConnectedGraphObject[] graphObjects) {

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

}
