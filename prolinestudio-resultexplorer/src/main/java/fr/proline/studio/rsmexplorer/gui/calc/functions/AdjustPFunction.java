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

import fr.proline.studio.parameter.AbstractLinkedParameters;
import fr.proline.studio.parameter.DoubleParameter;
import fr.proline.studio.parameter.IntegerParameter;
import fr.proline.studio.parameter.ObjectParameter;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.python.data.ColDoubleData;
import fr.proline.studio.python.data.ColRef;
import fr.proline.studio.python.data.Table;
import fr.proline.studio.python.interpreter.CalcCallback;
import fr.proline.studio.python.interpreter.CalcError;
import fr.proline.studio.python.interpreter.CalcInterpreterTask;
import fr.proline.studio.python.interpreter.CalcInterpreterThread;
import fr.proline.studio.python.interpreter.ResultVariable;
import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import fr.proline.studio.rsmexplorer.gui.calc.ProcessCallbackInterface;
import fr.proline.studio.rsmexplorer.gui.calc.graph.FunctionGraphNode;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphConnector;
import fr.proline.studio.table.renderer.DefaultAlignRenderer;
import fr.proline.studio.table.renderer.DoubleRenderer;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.types.PValue;
import fr.proline.studio.types.PvalueAdjusted;
import java.util.ArrayList;
import javax.swing.*;

/**
 *
 * @author jm235353
 */
public class AdjustPFunction extends AbstractFunction {

    private static final String SEL_COLS1 = "SEL_COLS1";
    private static final String PI0PARAMETER = "PI0PARAMETER";
    private static final String ALPHAPARAMETER = "ALPHAPARAMETER";
    private static final String NBBINSPARAMETER = "NBBINSPARAMETER";
    private static final String PZPARAMETER = "PZPARAMETER";
    private static final String NUMERICVALUEARAMETER = "NUMERICVALUEARAMETER";
    
    private ObjectParameter m_columnsParameter1 = null;
    private ObjectParameter m_pi0MethodParameter = null;
    private DoubleParameter m_numericValueParameter = null;
    private DoubleParameter m_alphaParameter = null;
    private IntegerParameter m_nbinsParameter = null;
    private DoubleParameter m_pzParameter = null;
    
    
    public AdjustPFunction(GraphPanel panel) {
        super(panel, FUNCTION_TYPE.AdjustPFunction);
    }
    
    @Override
    public void inLinkModified() {
        super.inLinkModified();
        m_columnsParameter1 = null;
        m_pi0MethodParameter = null;
        m_numericValueParameter = null;
        m_alphaParameter = null;
        m_nbinsParameter = null;
        m_pzParameter = null;
    }
    
    @Override
    public String getName(int index) {
        if (m_pi0MethodParameter == null) {
            return "PValue Adjustment";
        }
        StringBuilder columnNameSb = new StringBuilder("PValue Adjustment ");
        String pi0Method = m_pi0MethodParameter.getStringValue();
        if (pi0Method.compareTo("Numeric Value") == 0) {
            columnNameSb.append(m_numericValueParameter.getStringValue());
        } else {
            columnNameSb.append(pi0Method);
        }
        return columnNameSb.toString();
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
    public void process(GraphConnector[] graphObjects, final FunctionGraphNode functionGraphNode, ProcessCallbackInterface callback) {
        setInError(false, null);
        
        if (m_parameters == null) {
            callback.finished(functionGraphNode);
            return;
        }

        Integer colIndex =(Integer) m_columnsParameter1.getAssociatedObjectValue();
        if ((colIndex == null) || (colIndex == -1)) {
            callback.finished(functionGraphNode);
            return;
        }
        
        // check if we have already processed
        if (m_globalTableModelInterface != null) {
            callback.finished(functionGraphNode);
            return;
        }

        setCalculating(true);
   
        try {

            GlobalTableModelInterface srcModel = graphObjects[0].getGlobalTableModelInterface();
            final Table sourceTable = new Table(srcModel);

            ResultVariable[] parameters = new ResultVariable[1];
            ColRef col = sourceTable.getCol(colIndex);
            parameters[0] = new ResultVariable(col);

            StringBuilder codeSB = new StringBuilder();
            StringBuilder columnNameSb = new StringBuilder();
            createRCode(codeSB, columnNameSb, parameters, m_pi0MethodParameter, m_numericValueParameter, m_alphaParameter, m_nbinsParameter, m_pzParameter);
            
            /*
            codeSB.append("adjustP=Stats.adjustP(");
            for (int i = 0; i < parameters.length; i++) {
                codeSB.append(parameters[i].getName());
            }
            
            columnNameSb = new StringBuilder("adjustP ");
            String pi0Method = m_pi0MethodParameter.getStringValue();
            if (pi0Method.compareTo("Numeric Value") == 0) {
                codeSB.append(',');
                codeSB.append(m_numericValueParameter.getStringValue());
                columnNameSb.append(m_numericValueParameter.getStringValue());
            } else {
                codeSB.append(",\"").append(pi0Method).append("\"");
                columnNameSb.append(pi0Method);
            }
            codeSB.append(",").append(m_alphaParameter.getStringValue());
            codeSB.append(",").append(m_nbinsParameter.getStringValue());
            codeSB.append(",").append(m_pzParameter.getStringValue());
            codeSB.append(')');*/
            
            final String columnName = columnNameSb.toString();
            
            CalcCallback calcCallback = new CalcCallback() {

                @Override
                public void run(ArrayList<ResultVariable> variables, CalcError error) {
                    try {
                        if (variables != null) {
                            // look for res
                            for (ResultVariable var : variables) {
                                if (var.getName().compareTo("adjustP") == 0) {
                                    // we have found the result
                                    ColDoubleData col = (ColDoubleData) var.getValue();
                                    // give a specific column name
                                    col.setColumnName(columnName);
                                    sourceTable.addColumn(col, new PvalueAdjusted(), new DoubleRenderer(new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class), JLabel.RIGHT), 4, true, true));

                                    addModel(sourceTable.getModel());

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
    
    public static void createRCode(StringBuilder codeSB, StringBuilder columnNameSb, ResultVariable[] parameters, ObjectParameter pi0MethodParameter, DoubleParameter numericValueParameter, DoubleParameter alphaParameter, IntegerParameter nbinsParameter, DoubleParameter pzParameter) {

        codeSB.append("adjustP=Stats.adjustP(");
        for (int i = 0; i < parameters.length; i++) {
            codeSB.append(parameters[i].getName());
        }

        columnNameSb.append("adjustP ");
        String pi0Method = pi0MethodParameter.getStringValue();
        if (pi0Method.compareTo("Numeric Value") == 0) {
            codeSB.append(',');
            codeSB.append(numericValueParameter.getStringValue());
            columnNameSb.append(numericValueParameter.getStringValue());
        } else {
            codeSB.append(",\"").append(pi0Method).append("\"");
            columnNameSb.append(pi0Method);
        }
        codeSB.append(",").append(alphaParameter.getStringValue());
        codeSB.append(",").append(nbinsParameter.getStringValue());
        codeSB.append(",").append(pzParameter.getStringValue());
        codeSB.append(')');

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
        GlobalTableModelInterface model1 = graphObjects[0].getGlobalTableModelInterface();
        int nbColumns = model1.getColumnCount();
        int nbColumnsKept = 0;
        for (int i = 0; i < nbColumns; i++) {
            Class c = model1.getDataColumnClass(i);
            if (c.equals(Float.class) || c.equals(Double.class)) {
                nbColumnsKept++;
            }
        }
        Object[] objectArray1 = new Object[nbColumnsKept];
        Object[] associatedObjectArray1 = new Object[nbColumnsKept];
        int iKept = 0;
        int selectedIndex1 = -1;
        for (int i = 0; i < nbColumns; i++) {
            Class c = model1.getDataColumnClass(i);
            if (c.equals(Float.class) || c.equals(Double.class)) {
                objectArray1[iKept] = model1.getColumnName(i);
                PValue pvalue = (PValue) model1.getColValue(PValue.class, i);
                if (pvalue != null) {
                    selectedIndex1 = iKept;
                }
                associatedObjectArray1[iKept] = i+1;  // +1 because it is used in python calc expression
                iKept++;
            }
        }

        ParameterList parameterList1 = new ParameterList("param1");
        
        m_columnsParameter1 = new ObjectParameter(SEL_COLS1, "P Values Column", null, objectArray1, associatedObjectArray1, selectedIndex1, null);

        String[] pi0Values = { "Numeric Value", "abh", "bky", "jiang", "histo", "langaas", "pounds", "slim", "st.boot", "st.spline" };
        m_pi0MethodParameter = new ObjectParameter(PI0PARAMETER, "pi0 Method", pi0Values, 0, null);
        
        m_numericValueParameter = new DoubleParameter(NUMERICVALUEARAMETER, "Pi0 Value", JTextField.class, 1d, 0d, 1d); 
        m_alphaParameter = new DoubleParameter(ALPHAPARAMETER, "Alpha", JTextField.class, 0.05, 0d, 1d);
        m_nbinsParameter = new IntegerParameter(NBBINSPARAMETER, "Number of Bins", JSpinner.class, 20, 5, 100);
        m_pzParameter = new DoubleParameter(PZPARAMETER, "Pz", JTextField.class, 0.05, 0.01, 0.1);
        
        AbstractLinkedParameters linkedParameters = new AbstractLinkedParameters(parameterList1) {
            @Override
            public void valueChanged(String value, Object associatedValue) {
                showParameter(m_numericValueParameter, (value.compareTo("Numeric Value") == 0));
                showParameter(m_alphaParameter, (value.compareTo("bky") == 0));
                showParameter(m_nbinsParameter, ((value.compareTo("jiang") == 0) || (value.compareTo("histo") == 0)));
                showParameter(m_pzParameter, (value.compareTo("slim") == 0));

                updateParameterListPanel();
            }
            
        };

        m_parameters = new ParameterList[1];
        m_parameters[0] = parameterList1;

        parameterList1.add(m_columnsParameter1);
        parameterList1.add(m_pi0MethodParameter);
        parameterList1.add(m_numericValueParameter);
        parameterList1.add(m_alphaParameter);
        parameterList1.add(m_nbinsParameter);
        parameterList1.add(m_pzParameter);
        
        parameterList1.getPanel(); // generate panel at once
        m_pi0MethodParameter.addLinkedParameters(linkedParameters); // link parameter, it will modify the panel


    }

    @Override
    public void userParametersChanged() {
        // need to recalculate model
        m_globalTableModelInterface = null;
    }

    @Override
    public AbstractFunction cloneFunction(GraphPanel p) {
        AbstractFunction clone = new AdjustPFunction(p);
        clone.cloneInfo(this);
        return clone;
    }

    @Override
    public boolean calculationDone() {
        if (m_globalTableModelInterface != null) {
            return true;
        }
        return false;
    }
    
    @Override
    public boolean settingsDone() {
        
        if (m_parameters == null) {
            return false;
        }
        
        if (m_columnsParameter1 == null) {
            return false;
        }
        
        Integer colIndex =(Integer) m_columnsParameter1.getAssociatedObjectValue();
        if ((colIndex == null) || (colIndex == -1)) {
            return false;
        }

        return true;
    }

    @Override
    public ParameterError checkParameters(GraphConnector[] graphObjects) {
        return null;
    }
    
}
