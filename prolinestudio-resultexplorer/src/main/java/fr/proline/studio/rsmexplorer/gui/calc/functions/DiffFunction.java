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
 package fr.proline.studio.rsmexplorer.gui.calc.functions;

import fr.proline.studio.extendedtablemodel.AbstractJoinDataModel;
import fr.proline.studio.parameter.AbstractLinkedParameters;
import fr.proline.studio.parameter.BooleanParameter;
import fr.proline.studio.parameter.DoubleParameter;
import fr.proline.studio.parameter.ObjectParameter;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.python.data.Table;
import fr.proline.studio.python.interpreter.CalcError;
import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import fr.proline.studio.rsmexplorer.gui.calc.ProcessCallbackInterface;
import fr.proline.studio.rsmexplorer.gui.calc.graph.FunctionGraphNode;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphConnector;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import java.util.ArrayList;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;

/**
 * Diff Function for the data analyzer
 * @author JM235353
 */
public class DiffFunction extends AbstractFunction {

    private static final String JOIN_TABLE1_KEY1 = "JOIN_TABLE1_KEY1";
    private static final String JOIN_TABLE2_KEY1 = "JOIN_TABLE2_KEY1";
    private static final String TOLERANCE_KEY1 = "TOLERANCE_KEY1";
    private static final String JOIN_TABLE1_KEY2 = "JOIN_TABLE1_KEY2";
    private static final String JOIN_TABLE2_KEY2 = "JOIN_TABLE2_KEY2";
    private static final String TOLERANCE_KEY2 = "TOLERANCE_KEY2";
    private static final String SOURCE_COL = "SOURCE_COL";

    private ParameterList m_parameterList;
    private ObjectParameter m_paramTable1Key1;
    private ObjectParameter m_paramTable2Key1;
    private DoubleParameter m_tolerance1;
    private ObjectParameter m_paramTable1Key2;
    private ObjectParameter m_paramTable2Key2;
    private DoubleParameter m_tolerance2;
    private BooleanParameter m_addSourceCol;
    
    public DiffFunction(GraphPanel panel) {
        super(panel, FUNCTION_TYPE.DiffFunction);
    }
    
    @Override
    public void inLinkModified() {
        super.inLinkModified();
        m_paramTable1Key1 = null;
        m_paramTable2Key1 = null;
        m_tolerance1 = null;
        m_paramTable1Key2 = null;
        m_paramTable2Key2 = null;
        m_tolerance2 = null;
        m_addSourceCol = null;
    }
    
    @Override
    public String getName(int index) {
        return "Difference";
    }

    @Override
    public int getNumberOfInParameters() {
        return 2;
    }
    
    @Override
    public int getNumberOfOutParameters() {
        return 1;
    }

    @Override
    public boolean settingsDone() {
        if (m_parameterList == null) {
            return false;
        }
        return ((m_paramTable1Key1 != null) && (m_paramTable2Key1 != null));
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
        AbstractFunction clone = new DiffFunction(p);
        clone.cloneInfo(this);
        return clone;
    }
    
    @Override
    public void process(GraphConnector[] graphObjects, FunctionGraphNode functionGraphNode, ProcessCallbackInterface callback) {

        try {

            // check if we have already processed
            if (m_globalTableModelInterface != null) {
                return;
            }

            setCalculating(true);
            setInError(false, null);

            try {
                Table t1 = new Table(graphObjects[0].getGlobalTableModelInterface());
                graphObjects[0].getGlobalTableModelInterface().setName(graphObjects[0].getGraphNode().getDataName());
                Table t2 = new Table(graphObjects[1].getGlobalTableModelInterface());
                graphObjects[1].getGlobalTableModelInterface().setName(graphObjects[1].getGraphNode().getDataName());

                Table diffTable;
                if ((m_paramTable1Key1 != null) && (m_paramTable2Key1 != null)) {
                    Integer table1Key1 = (Integer) m_paramTable1Key1.getAssociatedObjectValue();
                    Integer table2Key1 = (Integer) m_paramTable2Key1.getAssociatedObjectValue();
                    Double tolerance1 = Double.valueOf(m_tolerance1.getStringValue());
                    Integer table1Key2 = (Integer) m_paramTable1Key2.getAssociatedObjectValue();
                    Integer table2Key2 = (Integer) m_paramTable2Key2.getAssociatedObjectValue();
                    Double tolerance2 = Double.valueOf(m_tolerance2.getStringValue());
                    Boolean showSourceColumn = (Boolean) m_addSourceCol.getObjectValue();
                    
                    diffTable = Table.diff(t1, t2, table1Key1, table2Key1, tolerance1, table1Key2, table2Key2, tolerance2, showSourceColumn);
                } else {
                    diffTable = Table.diff(t1, t2);
                }
                
                addModel(diffTable.getModel());


            } catch (Exception e) {
                setInError(new CalcError(e, null, -1));
            }
            setCalculating(false);

        } finally {
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

        
        GlobalTableModelInterface modelForDefaultKey = getMainGlobalTableModelInterface(0);
        
        if (modelForDefaultKey == null) {
            Table t1 = new Table(graphObjects[0].getGlobalTableModelInterface());
            Table t2 = new Table(graphObjects[1].getGlobalTableModelInterface());
            Table joinedTable = Table.join(t1, t2);
            modelForDefaultKey = joinedTable.getModel();
        }

        GlobalTableModelInterface model1 = graphObjects[0].getGlobalTableModelInterface();
        int nbColumns = model1.getColumnCount();
        int nbColumnsKept = 0;
        for (int i = 0; i < nbColumns; i++) {
            Class c = model1.getDataColumnClass(i);
            if (c.equals(String.class) || c.equals(Integer.class) || c.equals(Long.class) || c.equals(Float.class) || c.equals(Double.class)) {
                nbColumnsKept++;
            }
        }
        Object[] objectArrayTable1Key1 = new Object[nbColumnsKept];
        Object[] associatedObjectArrayTable1Key1 = new Object[nbColumnsKept];
        int iKept = 0;
        for (int i = 0; i < nbColumns; i++) {
            Class c = model1.getDataColumnClass(i);
            if (c.equals(String.class) || c.equals(Integer.class) || c.equals(Long.class) || c.equals(Float.class) || c.equals(Double.class)) {
                objectArrayTable1Key1[iKept] = model1.getColumnName(i);
                associatedObjectArrayTable1Key1[iKept] = i;  // no +1 because it is not used in python calc expression
                iKept++;
            }
        }

        GlobalTableModelInterface model2 = graphObjects[1].getGlobalTableModelInterface();
        nbColumns = model2.getColumnCount();
        nbColumnsKept = 0;
        for (int i = 0; i < nbColumns; i++) {
            Class c = model2.getDataColumnClass(i);
            if (c.equals(String.class) || c.equals(Integer.class) || c.equals(Long.class) || c.equals(Float.class) || c.equals(Double.class)) {
                nbColumnsKept++;
            }
        }
        Object[] objectArrayTable2Key1 = new Object[nbColumnsKept];
        Object[] associatedObjectArrayTable2Key1 = new Object[nbColumnsKept];
        iKept = 0;
        for (int i = 0; i < nbColumns; i++) {
            Class c = model2.getDataColumnClass(i);
            if (c.equals(String.class) || c.equals(Integer.class) || c.equals(Long.class) || c.equals(Float.class) || c.equals(Double.class)) {
                objectArrayTable2Key1[iKept] = model2.getColumnName(i);
                associatedObjectArrayTable2Key1[iKept] = i;
                iKept++;
            }
        }

        int nb = objectArrayTable1Key1.length;
        Object[] objectArrayTable1Key2 = new Object[nb+1];
        Object[] associatedObjectArrayTable1Key2 = new Object[nb+1];
        objectArrayTable1Key2[0] = "<No Second Key>";
        associatedObjectArrayTable1Key2[0] = new Integer(-1);
        for (int i=0;i<nb;i++) {
            objectArrayTable1Key2[i+1] = objectArrayTable1Key1[i];
            associatedObjectArrayTable1Key2[i+1] = associatedObjectArrayTable1Key1[i];
        }

        nb = objectArrayTable2Key1.length;
        Object[] objectArrayTable2Key2 = new Object[nb+1];
        Object[] associatedObjectArrayTable2Key2 = new Object[nb+1];
        objectArrayTable2Key2[0] = "<No Second Key>";
        associatedObjectArrayTable2Key2[0] = new Integer(-1);
        for (int i=0;i<nb;i++) {
            objectArrayTable2Key2[i+1] = objectArrayTable2Key1[i];
            associatedObjectArrayTable2Key2[i+1] = associatedObjectArrayTable2Key1[i];
        }
        
        m_paramTable1Key1 = new ObjectParameter(JOIN_TABLE1_KEY1, graphObjects[0].getFullName() + " Join Column Key 1", new JComboBox(objectArrayTable1Key1), objectArrayTable1Key1, associatedObjectArrayTable1Key1, ((AbstractJoinDataModel) modelForDefaultKey).getSelectedKey1(), null);
        m_paramTable2Key1 = new ObjectParameter(JOIN_TABLE2_KEY1, graphObjects[1].getFullName() + " Join Column Key 1", new JComboBox(objectArrayTable2Key1), objectArrayTable2Key1, associatedObjectArrayTable2Key1, ((AbstractJoinDataModel) modelForDefaultKey).getSelectedKey2(), null);
        m_tolerance1 = new DoubleParameter(TOLERANCE_KEY1, "Tolerance", JTextField.class, 0.0, 0.0, null);
        m_paramTable1Key2 = new ObjectParameter(JOIN_TABLE1_KEY2, graphObjects[0].getFullName() + " Join Column Key 2", new JComboBox(objectArrayTable1Key2), objectArrayTable1Key2, associatedObjectArrayTable1Key2, 0, null);
        m_paramTable2Key2 = new ObjectParameter(JOIN_TABLE2_KEY2, graphObjects[1].getFullName() + " Join Column Key 2", new JComboBox(objectArrayTable2Key2), objectArrayTable2Key2, associatedObjectArrayTable2Key2, 0, null);
        m_tolerance2 = new DoubleParameter(TOLERANCE_KEY2, "Tolerance 2", JTextField.class, 0.0, 0.0, null);
        
        m_addSourceCol = new BooleanParameter(SOURCE_COL, "Add Source Info", JCheckBox.class, true);
        
        m_parameterList = new ParameterList("Join");
        
        AbstractLinkedParameters linkedParameters1 = new AbstractLinkedParameters(m_parameterList) {
            @Override
            public void valueChanged(String value, Object associatedValue) {
                int index = ((Integer)associatedValue).intValue();
                Class c = model1.getDataColumnClass(index);
                showParameter(m_tolerance1, (c.equals(Double.class) || c.equals(Float.class)));
                updateParameterListPanel();
            }

        };
        
        AbstractLinkedParameters linkedParameters2 = new AbstractLinkedParameters(m_parameterList) {
            @Override
            public void valueChanged(String value, Object associatedValue) {
                int index = ((Integer) associatedValue).intValue();
                if (index == -1) {
                    showParameter(m_tolerance2, false);
                } else {
                    Class c = model1.getDataColumnClass(index);
                    showParameter(m_tolerance2, (c.equals(Double.class) || c.equals(Float.class)));
                }
                updateParameterListPanel();
            }

        };
        
        
        
        m_parameters = new ParameterList[1];
        m_parameters[0] = m_parameterList;

        m_parameterList.add(m_paramTable1Key1);
        m_parameterList.add(m_paramTable2Key1);
        m_parameterList.add(m_tolerance1);
        m_parameterList.add(m_paramTable1Key2);
        m_parameterList.add(m_paramTable2Key2);
        m_parameterList.add(m_tolerance2);
        m_parameterList.add(m_addSourceCol);
        
        m_parameterList.getPanel(); // generate panel at once
        m_paramTable1Key1.addLinkedParameters(linkedParameters1); // link parameter, it will modify the panel
        m_paramTable1Key2.addLinkedParameters(linkedParameters2); // link parameter, it will modify the panel
    }

    @Override
    public ParameterError checkParameters(GraphConnector[] graphObjects) {
        Integer keyTable1Key1 = (Integer) m_paramTable1Key1.getAssociatedObjectValue();
        Integer keyTable2Key1 = (Integer) m_paramTable2Key1.getAssociatedObjectValue();
        
        Integer keyTable1Key2 = (Integer) m_paramTable1Key2.getAssociatedObjectValue();
        Integer keyTable2Key2 = (Integer) m_paramTable2Key2.getAssociatedObjectValue();
        
        GlobalTableModelInterface modelForDefaultKey = getMainGlobalTableModelInterface(0);
        
        if (modelForDefaultKey == null) {
            Table t1 = new Table(graphObjects[0].getGlobalTableModelInterface());
            Table t2 = new Table(graphObjects[1].getGlobalTableModelInterface());
            Table joinedTable = Table.join(t1, t2);
            modelForDefaultKey = joinedTable.getModel();
        }
        
        boolean checkKeys = ((AbstractJoinDataModel) modelForDefaultKey).checkKeys(keyTable1Key1, keyTable2Key1) && ((AbstractJoinDataModel) modelForDefaultKey).checkKeys(keyTable1Key2, keyTable2Key2);

        ParameterError error = null;
        if (!checkKeys) {
            error = new ParameterError("Selected Keys are not compatible", m_parameterList.getPanel());
        }
        return error;
    }
    
    @Override
    public void userParametersChanged() {
        m_globalTableModelInterface = null;
    }
    
}
