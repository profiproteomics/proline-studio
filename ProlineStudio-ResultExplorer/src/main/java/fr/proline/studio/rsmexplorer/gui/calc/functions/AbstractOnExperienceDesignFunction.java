package fr.proline.studio.rsmexplorer.gui.calc.functions;

import fr.proline.core.orm.uds.BiologicalGroup;
import fr.proline.core.orm.uds.GroupSetup;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.gui.JCheckBoxList;
import fr.proline.studio.gui.JCheckBoxListPanel;
import fr.proline.studio.parameter.AbstractLinkedParameters;
import fr.proline.studio.parameter.MultiObjectParameter;
import fr.proline.studio.parameter.ObjectParameter;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.python.data.ColDoubleData;
import fr.proline.studio.python.data.ColRef;
import fr.proline.studio.python.data.Table;
import fr.proline.studio.python.interpreter.CalcCallback;
import fr.proline.studio.python.interpreter.CalcError;
import fr.proline.studio.python.interpreter.ResultVariable;
import fr.proline.studio.python.interpreter.CalcInterpreterTask;
import fr.proline.studio.python.interpreter.CalcInterpreterThread;
import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import fr.proline.studio.rsmexplorer.gui.calc.ProcessCallbackInterface;
import fr.proline.studio.rsmexplorer.gui.calc.graph.AbstractConnectedGraphObject;
import fr.proline.studio.rsmexplorer.gui.calc.graph.FunctionGraphNode;
import fr.proline.studio.table.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.table.renderer.DoubleRenderer;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.types.GroupSelection;
import fr.proline.studio.types.QuantitationType;
import fr.proline.studio.types.XicGroup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Abstract class for functions on Experience Disgn for the data analyzer
 * @author JM235353
 */
public abstract class AbstractOnExperienceDesignFunction extends AbstractFunction {

    private static final String NBGROUPS_KEY = "NBGROUPS";
    private static final String QUANTITATIONTYPE_KEY = "QUANTITATIONTYPE";
    private static final String SEL_COLS_PREFIX = "SEL_COLS";

    private ObjectParameter m_nbGroupsParameter = null;
    private ObjectParameter m_quantitationTypeParameter = null;
    private MultiObjectParameter[] m_columnsParameterArray = null;
    
    private final String m_functionName;
    private final String m_resultName;
    private final String m_pythonCall;
    private final Object m_colExtraInfo;
    
    private Table m_sourceTable = null;
    
    private final ArrayList<String> m_groupNames = new ArrayList<>();
    
    public AbstractOnExperienceDesignFunction(GraphPanel panel, String functionName, String resultName, String pythonCall, Object colExtraInfo) {
        super(panel);
        
        m_resultName = resultName;
        m_functionName = functionName;
        m_pythonCall = pythonCall;
        m_colExtraInfo = colExtraInfo;
    }
    
    @Override
    public void inLinkDeleted() {
        super.inLinkDeleted();
        m_nbGroupsParameter = null;
        m_quantitationTypeParameter = null;
        m_columnsParameterArray = null;
    }
    
    public abstract int getMinGroups();
    public abstract int getMaxGroups();
    
    @Override
    public String getName() {
        return m_functionName;
    }

    @Override
    public int getNumberOfInParameters() {
        return 1;
    }

    @Override
    public void process(AbstractConnectedGraphObject[] graphObjects, FunctionGraphNode functionGraphNode, ProcessCallbackInterface callback) {
        
        int nbColList;
        int nbCols;

        // check if we have already processed
        if (m_globalTableModelInterface != null) {
            callback.finished(functionGraphNode);
            return;
        }

        setInError(false, null);

        if (m_columnsParameterArray == null) {
            callback.finished(functionGraphNode);
            return;
        }

        final GroupSelection groupSelection = new GroupSelection();
        
        nbColList = ((Integer) m_nbGroupsParameter.getAssociatedObjectValue()).intValue();
        nbCols = 0;
        for (int i = 0; i < nbColList; i++) {
            ArrayList colList = (ArrayList) m_columnsParameterArray[i].getAssociatedValues(true);
            if ((colList == null) || (colList.isEmpty())) {
                callback.finished(functionGraphNode);
                return;
            }
            
            groupSelection.addGroup(colList);
            
            nbCols += colList.size();
        }

        
        
        setCalculating(true);

        try {

            GlobalTableModelInterface srcModel = graphObjects[0].getGlobalTableModelInterface();
            final Table sourceTable = new Table(srcModel);

            ResultVariable[] extraVariables = getExtraVariables(sourceTable);
            int nbExtraVariables = (extraVariables == null) ? 0 : extraVariables.length;
            ResultVariable[] parameters = new ResultVariable[nbCols+nbExtraVariables];

            int nbSizeDone = 0;
            for (int j = 0; j < nbColList; j++) {
                List colList = (List) m_columnsParameterArray[j].getAssociatedValues(true);
                for (int i = 0; i < colList.size(); i++) {
                    Integer colIndex = (Integer) colList.get(i);
                    ColRef col = sourceTable.getCol(colIndex);
                    parameters[i + nbSizeDone] = new ResultVariable(col);
                }
                nbSizeDone += colList.size();
            }
            for (int i=0;i<nbExtraVariables;i++) {
                parameters[nbSizeDone+i] = extraVariables[i];
            }

            StringBuilder codeSB = new StringBuilder();
            codeSB.append(m_resultName + "=Stats." + m_pythonCall + "((");

            nbSizeDone = 0;
            for (int j = 0; j < nbColList; j++) {
                codeSB.append('(');
                List colList = (List) m_columnsParameterArray[j].getAssociatedValues(true);
                for (int i = 0; i < colList.size(); i++) {
                    codeSB.append(parameters[i + nbSizeDone].getName());
                    if (i < colList.size() - 1) {
                        codeSB.append(',');
                    }
                }
                nbSizeDone += colList.size();
                codeSB.append(')');
                if (j < nbColList - 1) {
                    codeSB.append(',');
                }
            }
            codeSB.append(')');
            
            if (addLabelParameter()) {

                codeSB.append(",(");
                // labels of groups 
                boolean first = true;
                for (int j = 0; j < nbColList; j++) {
                    List colList = (List) m_columnsParameterArray[j].getAssociatedValues(true);
                    for (int i = 0; i < colList.size(); i++) {
                        if (first) {
                            first = false;
                        } else {
                            codeSB.append(',');
                        }
                        if (m_groupNames.size() == nbColList) {
                            codeSB.append("\"" + m_groupNames.get(j) + "\"");
                        } else {
                            codeSB.append("\"group" + j + "\"");
                        }
                    }
                }
                codeSB.append(")");
            }
            
            String extraValues = getExtraValuesForFunctionCall();
            if (extraValues != null) {
                codeSB.append(extraValues);
            }
            codeSB.append(')');

            CalcCallback calcCallback = new CalcCallback() {

                @Override
                public void run(ArrayList<ResultVariable> variables, CalcError error) {

                    try {

                        if (variables != null) {
                            // look for res
                            for (ResultVariable var : variables) {
                                if (var.getName().compareTo(m_resultName) == 0) {

                                    // we have found the result
                                    GlobalTableModelInterface model = null;
                                    Object res = var.getValue();
                                    if (res instanceof ColDoubleData) {
                                        ColDoubleData col = (ColDoubleData) var.getValue();
                                        sourceTable.addColumn(col, m_colExtraInfo, new DoubleRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 4, true, true));
                                        model = sourceTable.getModel();
                                    } else if (res instanceof Table) {
                                        Table t = (Table) var.getValue();
                                        model = t.getModel();
                                    }

                                    // save the group selection in the result
                                    model.addSingleValue(groupSelection);
                                    addModel(model);
                                    
                                }
                            }
                        } else if (error != null) {
                            setInError(error);
                        }
                        setCalculating(false);
                    } finally {
                        callback.finished(functionGraphNode);
                    }
                }

            };

            CalcInterpreterTask task = new CalcInterpreterTask(codeSB.toString(), parameters, calcCallback);

            CalcInterpreterThread.getCalcInterpreterThread().addTask(task);

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
    public ArrayList<WindowBox> getDisplayWindowBox(FunctionGraphNode functionGraphNode) {
        return getDisplayWindowBox(functionGraphNode.getPreviousDataName(), getName());
    }

    @Override
    public boolean settingsDone() {
        
        if (m_parameters == null) {
            return false;
        }
        
        if (m_columnsParameterArray == null) {
            return false;
        }

        int nbColList = ((Integer) m_nbGroupsParameter.getAssociatedObjectValue()).intValue();
        for (int i = 0; i < nbColList; i++) {
            List colList = (List) m_columnsParameterArray[i].getAssociatedValues(true);
            if ((colList == null) || (colList.isEmpty())) {
                return false;
            }
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
    public void generateDefaultParameters(AbstractConnectedGraphObject[] graphObjects) {



        
        GlobalTableModelInterface sourceModel = graphObjects[0].getGlobalTableModelInterface();
        
        boolean hasQuantitationTypeParameter = false;
        int nbGroupsFound = -1;
        int nbGroups = getMinGroups(); // default value
        
        GroupSelection groupSelection = (GroupSelection) sourceModel.getSingleValue(GroupSelection.class);
        if (groupSelection != null) {
            nbGroupsFound = groupSelection.getNumberOfGroups();
            nbGroups = nbGroupsFound;

            String[] groupArray = new String[1];
            groupArray[0] = nbGroupsFound + "group(s)";
            Object[] groupAssociatedArray = new Object[1];
            groupAssociatedArray[0] = nbGroupsFound;
            m_nbGroupsParameter = new ObjectParameter(NBGROUPS_KEY, "Number of Groups", null, groupArray, groupAssociatedArray, (nbGroupsFound == 2) ? 0 : 1, null);

            m_columnsParameterArray = new MultiObjectParameter[nbGroupsFound];
            for (int i = 0; i < nbGroupsFound; i++) {
                ArrayList<Integer> selectedCols = groupSelection.getGroup(i);
                Object[] selectedColsObject = selectedCols.toArray();
                boolean[] selection = new boolean[selectedCols.size()];
                for (int j = 0; j < selection.length; j++) {
                    selection[j] = true;
                }
                m_columnsParameterArray[i] = new MultiObjectParameter(SEL_COLS_PREFIX + i, "group " + i, null, selectedColsObject, selectedColsObject, selection, null, true);
                m_columnsParameterArray[i].setCompulsory(2); // at least two values in a group
            }
            
            ParameterList extraParameterList = getExtraParameterList();
            int nbExtraParameterList = (extraParameterList == null) ? 0 : 1;

            m_parameters = new ParameterList[nbExtraParameterList];

            if (extraParameterList != null) {
                m_parameters[0] = extraParameterList;
            }
            
        } else {

            int nbColumns = sourceModel.getColumnCount();

            ArrayList<Integer> columnKept = new ArrayList<>();
            HashSet<QuantitationType> m_quantitationsSet = new HashSet<>();
            for (int i = 0; i < nbColumns; i++) {
                QuantitationType quantitationType = (QuantitationType) sourceModel.getColValue(QuantitationType.class, i);
                if (quantitationType != null) {
                    columnKept.add(i);
                    m_quantitationsSet.add(quantitationType);
                }
            }
            hasQuantitationTypeParameter = !m_quantitationsSet.isEmpty();

            if (!hasQuantitationTypeParameter) {
                for (int i = 0; i < nbColumns; i++) {
                    Class c = sourceModel.getDataColumnClass(i);
                    if (c.equals(Float.class) || c.equals(Double.class) || c.equals(Integer.class) || c.equals(Long.class)) {
                        columnKept.add(i);
                    }
                }
            }
            int nbColumnsKept = columnKept.size();
            Object[] objectArray1 = new Object[nbColumnsKept];
            Object[] associatedObjectArray1 = new Object[nbColumnsKept];
            for (int i = 0; i < nbColumnsKept; i++) {
                objectArray1[i] = sourceModel.getColumnName(columnKept.get(i)).replaceAll("<br/>"," ");
                associatedObjectArray1[i] = columnKept.get(i) + 1;  // +1 because it is used in python calc expression
            }

            
            String[] groupNameList = {"First Group", "Second Group", "Third Group", "Fourth Group", "Fifth Group", "Sixth Group", "Seventh Group", "Eighth Group"};

            boolean[][] selection = new boolean[getMaxGroups()][nbColumnsKept];
            for (boolean[] row : selection) {
                Arrays.fill(row, false);
            }

            
            DDataset dataset = (DDataset) sourceModel.getSingleValue(DDataset.class);
            if (dataset != null) {
                GroupSetup groupSetup = dataset.getGroupSetup();
                if (groupSetup != null) {
                    List<BiologicalGroup> listBiologicalGroups = groupSetup.getBiologicalGroups();
                    nbGroups = listBiologicalGroups.size();

                    if ((nbGroups >= getMinGroups()) && (nbGroups <= getMaxGroups())) {
                        nbGroupsFound = nbGroups;
                        for (int i = 0; i < nbGroups; i++) {
                            groupNameList[i] = listBiologicalGroups.get(i).getName() + " Group";
                        }

                        if (hasQuantitationTypeParameter) {

                            m_groupNames.clear();
                            HashMap<Long, Integer> groupMap = new HashMap<>();  // id group -> numéro de group
                            for (int i = 0; i < nbColumnsKept; i++) {
                                QuantitationType quantitationType = (QuantitationType) sourceModel.getColValue(QuantitationType.class, columnKept.get(i));
                                if ((quantitationType.getType() == QuantitationType.WEIGHTED_SC) || (quantitationType.getType() == QuantitationType.ABUNDANCE)) {
                                    XicGroup group = (XicGroup) sourceModel.getColValue(XicGroup.class, columnKept.get(i));
                                    if (!groupMap.containsKey(group.getId())) {
                                        groupMap.put(group.getId(), groupMap.size());
                                        String name = group.getName();
                                        if (name != null) {
                                            m_groupNames.add(group.getName());
                                        }
                                    }
                                }
                            }

                            if (groupMap.size() == nbGroups) { // should be always true
                                for (int j = 0; j < nbGroups; j++) {
                                    for (int i = 0; i < nbColumnsKept; i++) {
                                        QuantitationType quantitationType = (QuantitationType) sourceModel.getColValue(QuantitationType.class, columnKept.get(i));
                                        XicGroup group = (XicGroup) sourceModel.getColValue(XicGroup.class, columnKept.get(i));
                                        if (((quantitationType.getType() == QuantitationType.WEIGHTED_SC) || (quantitationType.getType() == QuantitationType.ABUNDANCE)) && (groupMap.get(group.getId()) == j)) {
                                            selection[j][i] = true;
                                        }
                                    }
                                }
                            }
                        }

                    } else {
                        nbGroups = getMinGroups();
                    }

                }
            }

            int nbGroupChoices = getMaxGroups() - getMinGroups() + 1;
            String[] groupArray = new String[nbGroupChoices];
            Object[] groupAssociatedArray = new Object[nbGroupChoices];
            for (int i = 0; i < nbGroupChoices; i++) {
                groupArray[i] = String.valueOf(getMinGroups() + i) + " Groups";
                groupAssociatedArray[i] = getMinGroups() + i;
            }

            m_nbGroupsParameter = new ObjectParameter(NBGROUPS_KEY, "Number of Groups", null, groupArray, groupAssociatedArray, (nbGroups == 2) ? 0 : 1, null);

            QuantitationType[] quantitationArray = m_quantitationsSet.toArray(new QuantitationType[0]);
            Integer[] quantitationTypeArray = new Integer[quantitationArray.length];
            int quantitationSelected = -1;
            for (int i = 0; i < quantitationArray.length; i++) {
                quantitationTypeArray[i] = quantitationArray[i].getType();
                if ((quantitationTypeArray[i] == QuantitationType.WEIGHTED_SC) || (quantitationTypeArray[i] == QuantitationType.ABUNDANCE)) {
                    // WEIGHTED_SC type is pre-selected
                    quantitationSelected = i;

                }
            }
            if (hasQuantitationTypeParameter) {
                m_quantitationTypeParameter = new ObjectParameter(QUANTITATIONTYPE_KEY, "Quantitation Type", null, quantitationArray, quantitationTypeArray, quantitationSelected, null);
            }

            m_columnsParameterArray = new MultiObjectParameter[getMaxGroups()];
            for (int i = 0; i < getMaxGroups(); i++) {
                m_columnsParameterArray[i] = new MultiObjectParameter(SEL_COLS_PREFIX + i, groupNameList[i], null, objectArray1, associatedObjectArray1, selection[i], null, true);
                m_columnsParameterArray[i].setCompulsory(2); // at least two values in a group
            }

            ParameterList extraParameterList = getExtraParameterList();
            int nbExtraParameterList = (extraParameterList == null) ? 0 : 1;

            m_parameters = new ParameterList[getMaxGroups() + 1 + nbExtraParameterList];
            m_parameters[0] = new ParameterList("group and quantitation");
            m_parameters[0].add(m_nbGroupsParameter);
            if (hasQuantitationTypeParameter) {
                m_parameters[0].add(m_quantitationTypeParameter);
            }
            m_parameters[0].getPanel(true); // generate panel at once (needed for showParameter)
            for (int i = 0; i < getMaxGroups(); i++) {  // 3 groups max
                m_parameters[i + 1] = new ParameterList("bbinomial" + i);
                m_parameters[i + 1].add(m_columnsParameterArray[i]);
            }

            if (extraParameterList != null) {
                m_parameters[m_parameters.length - 1] = extraParameterList;
            }

            final int _nbGroupsFound = nbGroupsFound;
            if (getMaxGroups() >= 3) {
                for (int i = 3; i < getMaxGroups() + 1; i++) {
                    final Integer number = i;
                    AbstractLinkedParameters nbGroupslinkedParameters = new AbstractLinkedParameters(m_parameters[i]) {
                        @Override
                        public void valueChanged(String value, Object associatedValue) {
                            
                            enableList(((Integer)associatedValue)>=number);
                        }

                    };
                    m_nbGroupsParameter.addLinkedParameters(nbGroupslinkedParameters); // link parameter, it will modify the panel
                }
            }

            AbstractLinkedParameters quantitationVisibilityParameters = new AbstractLinkedParameters(m_parameters[0]) {
                @Override
                public void valueChanged(String value, Object associatedValue) {
                    if (m_quantitationTypeParameter != null) {
                        showParameter(m_quantitationTypeParameter, (((Integer) associatedValue) == _nbGroupsFound));
                    }
                }

            };
            m_nbGroupsParameter.addLinkedParameters(quantitationVisibilityParameters); // link parameter, it will modify the panel

            if (hasQuantitationTypeParameter) {
                final int _nbGroups = nbGroups;
                for (int i = 0; i < getMaxGroups(); i++) {
                    final int _groudIndex = i;
                    AbstractLinkedParameters quantitationTypelinkedParameters = new AbstractLinkedParameters(m_parameters[i]) {

                        private boolean doingValueChanged = false;

                        @Override
                        public void valueChanged(String value, Object associatedValue) {

                            if (doingValueChanged) {
                                return;
                            }
                            try {
                                doingValueChanged = true;

                                JCheckBoxList cb = ((JCheckBoxListPanel) m_columnsParameterArray[_groudIndex].getComponent()).getCheckBoxList();
                                cb.reinitSelection();

                                Integer quantitationChoosen = (Integer) associatedValue;

                                HashMap<Long, Integer> groupMap = new HashMap<>();  // id group -> numéro de group
                                for (int i = 0; i < nbColumnsKept; i++) {
                                    QuantitationType quantitationType = (QuantitationType) sourceModel.getColValue(QuantitationType.class, columnKept.get(i));
                                    if ((quantitationType.getType() == QuantitationType.WEIGHTED_SC) || (quantitationType.getType() == QuantitationType.ABUNDANCE)) {
                                        XicGroup group = (XicGroup) sourceModel.getColValue(XicGroup.class, columnKept.get(i));
                                        if (!groupMap.containsKey(group.getId())) {
                                            groupMap.put(group.getId(), groupMap.size());
                                        }
                                    }
                                }

                                if (groupMap.size() == _nbGroups) { // should be always true
                                    for (int i = 0; i < nbColumnsKept; i++) {
                                        QuantitationType quantitationType = (QuantitationType) sourceModel.getColValue(QuantitationType.class, columnKept.get(i));
                                        XicGroup group = (XicGroup) sourceModel.getColValue(XicGroup.class, columnKept.get(i));
                                        if ((quantitationType.getType() == quantitationChoosen) && (groupMap.get(group.getId()) == _groudIndex)) {
                                            cb.selectItem(i);
                                        }
                                    }
                                }

                            } finally {
                                doingValueChanged = false;
                            }

                        }
                    };
                    m_quantitationTypeParameter.addLinkedParameters(quantitationTypelinkedParameters);
                }
            }
        }

        

    }

    @Override
    public ParameterError checkParameters(AbstractConnectedGraphObject[] graphObjects) {
        return null;
    }

    @Override
    public void userParametersChanged() {
        // need to recalculate model
        m_globalTableModelInterface = null;
    }
    
    public ParameterList getExtraParameterList() {
        return null;
    }
    
    public String getExtraValuesForFunctionCall() {
        return null;
    }

    public ResultVariable[] getExtraVariables(Table sourceTable) {
        return null;
    }
    
    public boolean addLabelParameter() {
        return false;
    }
}
