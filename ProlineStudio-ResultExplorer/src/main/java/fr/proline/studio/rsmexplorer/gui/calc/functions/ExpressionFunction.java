package fr.proline.studio.rsmexplorer.gui.calc.functions;

import fr.proline.studio.gui.expressionbuilder.ExpressionEntity;
import fr.proline.studio.parameter.AbstractParameter;
import fr.proline.studio.parameter.ExpressionParameter;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.python.data.ColBooleanData;
import fr.proline.studio.python.data.ColDoubleData;
import fr.proline.studio.python.data.Table;
import fr.proline.studio.python.data.ValuesTableModel;
import fr.proline.studio.python.interpreter.CalcCallback;
import fr.proline.studio.python.interpreter.CalcError;
import fr.proline.studio.python.interpreter.CalcInterpreterTask;
import fr.proline.studio.python.interpreter.CalcInterpreterThread;
import fr.proline.studio.python.interpreter.ResultVariable;
import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import fr.proline.studio.rsmexplorer.gui.calc.ProcessCallbackInterface;
import fr.proline.studio.rsmexplorer.gui.calc.graph.AbstractConnectedGraphObject;
import fr.proline.studio.rsmexplorer.gui.calc.graph.FunctionGraphNode;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.table.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.table.renderer.DoubleRenderer;
import java.util.ArrayList;
import org.python.core.PyFloat;
import org.python.core.PyInteger;

/**
 *
 * @author JM235353
 */
public class ExpressionFunction extends AbstractFunction {

    private static final String EXPRESSION_KEY = "EXPRESSION_KEY";
    
    private ExpressionParameter m_expressionParameter = null;
    
    public ExpressionFunction(GraphPanel panel) {
        super(panel);
    }
    
    @Override
    public String getName() {
        return "Expression Builder";
    }

    @Override
    public int getNumberOfInParameters() {
        return 1;
    }

    @Override
    public void process(AbstractConnectedGraphObject[] graphObjects, FunctionGraphNode functionGraphNode, ProcessCallbackInterface callback) {
        // check if we have already processed
        if (m_globalTableModelInterface != null) {
            callback.finished(functionGraphNode);
            return;
        }

        setInError(false, null);

        setCalculating(true);

        try {

            GlobalTableModelInterface srcModel = graphObjects[0].getGlobalTableModelInterface();
            final Table sourceTable = new Table(srcModel);

            ResultVariable[] parameters = new ResultVariable[1];
            ResultVariable tableVar = new ResultVariable(sourceTable);
            parameters[0] = tableVar;
            
            


            StringBuilder codeSB = new StringBuilder();
            codeSB.append("t=").append(tableVar.getName()).append('\n');
            codeSB.append("calcRes" + "=" + m_expressionParameter.getStringValue());


            CalcCallback calcCallback = new CalcCallback() {

                @Override
                public void run(ArrayList<ResultVariable> variables, CalcError error) {

                    try {

                        if (variables != null) {
                            // look for res
                            for (ResultVariable var : variables) {
                                if (var.getName().compareTo("calcRes") == 0) {

                                    // we have found the result
                                    GlobalTableModelInterface model = null;
                                    Object res = var.getValue();
                                    if (res instanceof ColDoubleData) {
                                        ColDoubleData col = (ColDoubleData) var.getValue();
                                        col.setColumnName(m_expressionParameter.getHumanExpression());
                                        sourceTable.addColumn(col, null, new DoubleRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 4, true, true));
                                        model = sourceTable.getModel();
                                    } else if (res instanceof ColBooleanData) {
                                        ColBooleanData col = (ColBooleanData) var.getValue();
                                        col.setColumnName(m_expressionParameter.getHumanExpression());
                                        sourceTable.addColumn(col, null, null);
                                        model = sourceTable.getModel();
                                    } else if (res instanceof Table) {
                                        Table t = (Table) var.getValue();
                                        model = t.getModel();
                                    } else if ((res instanceof PyFloat) || (res instanceof PyInteger)) {
                                        ArrayList<String> valuesName = new ArrayList<>(1);
                                        valuesName.add(m_expressionParameter.getHumanExpression());
                                        ArrayList<String> values = new ArrayList<>(1);
                                        values.add(res.toString());
                                        model = new ValuesTableModel(valuesName, values);
                                    }

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
    public void generateDefaultParameters(AbstractConnectedGraphObject[] graphObjects) {
        
        ParameterList parameterTableList = new ParameterList("Expression Parameters");

        GlobalTableModelInterface model =  graphObjects[0].getGlobalTableModelInterface();
        int colCount = model.getColumnCount();


        
        ArrayList<ExpressionEntity> m_functions = new ArrayList<>(20);
        m_functions.add(new ExpressionEntity("(", "(", "("));
        m_functions.add(new ExpressionEntity(")", ")", ")"));
        m_functions.add(new ExpressionEntity("abs", "abs(", "Stats.abs("));
        m_functions.add(new ExpressionEntity(">", ">", ">"));
        m_functions.add(new ExpressionEntity(">=", ">=", ">="));
        m_functions.add(new ExpressionEntity("<", "<", "<"));
        m_functions.add(new ExpressionEntity("<=", "<=", "<="));
        m_functions.add(new ExpressionEntity("=", "=", "=="));
        
        ArrayList<ExpressionEntity> m_variables = new ArrayList<>(colCount);
        
        for (int i = 0; i < colCount; i++) {
            String columnFullName = model.getColumnName(i);
            String name = columnFullName.replaceAll("<br/>", " ");
            name = removeHtmlColor(name);
            int index = i+1;
            String code = "t["+index+"]";

            ExpressionEntity var = new ExpressionEntity(name, name, code);
            
            m_variables.add(var);
        }
        
        m_expressionParameter = new ExpressionParameter(EXPRESSION_KEY, "Expression", m_functions, m_variables);
        m_expressionParameter.forceShowLabel(AbstractParameter.LabelVisibility.NO_VISIBLE);

        parameterTableList.add(m_expressionParameter);      

        
        
        m_parameters = new ParameterList[1];
        m_parameters[0] = parameterTableList;

        parameterTableList.getPanel(); // generate panel at once

        
    }

    private String removeHtmlColor(String value) {
        int colorRemoveStart = value.indexOf("</font>", 0);
        int colorRemoveStop = value.indexOf("</html>", 0);
        if ((colorRemoveStart > -1) && (colorRemoveStop > colorRemoveStart)) {
            value = value.substring(colorRemoveStart + "</font>".length(), colorRemoveStop);
        }

        return value;
    }
    
    @Override
    public void userParametersChanged() {
         m_globalTableModelInterface = null;
    }

    @Override
    public AbstractFunction cloneFunction(GraphPanel p) {
        AbstractFunction clone = new ExpressionFunction(p);
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
        if (m_expressionParameter == null) {
            return false;
        }

        return true;
    }

    @Override
    public ParameterError checkParameters(AbstractConnectedGraphObject[] graphObjects) {
        ParameterError error = m_expressionParameter.checkParameter();

        return error;
    }
    
}
