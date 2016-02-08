package fr.proline.studio.rsmexplorer.gui.calc.graphics;

import fr.proline.studio.parameter.MultiObjectParameter;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.python.data.ColRef;
import fr.proline.studio.python.data.PythonImage;
import fr.proline.studio.python.data.Table;
import fr.proline.studio.python.interpreter.CalcCallback;
import fr.proline.studio.python.interpreter.CalcError;
import fr.proline.studio.python.interpreter.CalcInterpreterTask;
import fr.proline.studio.python.interpreter.CalcInterpreterThread;
import fr.proline.studio.python.interpreter.ResultVariable;
import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import fr.proline.studio.rsmexplorer.gui.calc.graph.AbstractGraphObject;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphicGraphNode;
import fr.proline.studio.table.GlobalTableModelInterface;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JM235353
 */
public abstract class AbstractMatrixPlotGraphic extends AbstractGraphic {
    
    private static final String SEL_COLS1 = "SEL_COLS1";

    private MultiObjectParameter m_columnsParameter1 = null;

    private String m_functionName = null;
    
    public AbstractMatrixPlotGraphic(GraphPanel panel, String functionName) {
        super(panel);
        
        m_functionName = functionName;
    }

    
    @Override
    public void inLinkDeleted() {
        super.inLinkDeleted();
        m_columnsParameter1 = null;
    }

    
    @Override
    public void process(final AbstractGraphObject[] graphObjects, GraphicGraphNode graphicGraphNode, final boolean display) {
        setInError(false, null);
        
        if (m_columnsParameter1 == null) {
            return;
        }

        List colList1 =(List) m_columnsParameter1.getAssociatedValues(true);
        if ((colList1 == null) || (colList1.isEmpty())) {
            return;
        }
        
        // check if we have already processed
        if (m_generatedImage != null) {
            if (display) {
                display(graphObjects[0].getDataName(), getName());
            }
            return;
        }

        setCalculating(true);
   
        try {

            GlobalTableModelInterface srcModel = graphObjects[0].getGlobalTableModelInterface();
            final Table sourceTable = new Table(srcModel);

            ResultVariable[] parameters = new ResultVariable[colList1.size()];
            for (int i = 0; i < colList1.size(); i++) {
                Integer colIndex = (Integer) colList1.get(i);
                ColRef col = sourceTable.getCol(colIndex);
                parameters[i] = new ResultVariable(col);
            }


            StringBuilder codeSB = new StringBuilder();
            codeSB.append("plot=Stats."+m_functionName+"((");
            for (int i = 0; i < parameters.length; i++) {
                codeSB.append(parameters[i].getName());
                if (i<parameters.length-1) {
                    codeSB.append(',');
                }
            }

            codeSB.append("))");

            CalcCallback callback = new CalcCallback() {

                @Override
                public void run(ArrayList<ResultVariable> variables, CalcError error) { 
                    if (variables != null) {
                        // look for res
                        for (ResultVariable var : variables) {
                            if (var.getName().compareTo("plot") == 0) {
                                // we have found the result
                                PythonImage image = (PythonImage) var.getValue();
                                m_generatedImage = image.getImage();

                                if (display) {
                                    display(graphObjects[0].getDataName(), var.getName());
                                }
                            }
                        }
                    } else if (error != null) {
                        //JPM.TODO
                        setInError(error);
                    }
                    setCalculating(false);
                }

            };

            CalcInterpreterTask task = new CalcInterpreterTask(codeSB.toString(), parameters, callback);

            CalcInterpreterThread.getCalcInterpreterThread().addTask(task);

        } catch (Exception e) {
            setInError(new CalcError(e, null, -1));
            setCalculating(false);
        }
    }

    @Override
    public void generateDefaultParameters(AbstractGraphObject[] graphObjects) {
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
        for (int i = 0; i < nbColumns; i++) {
            Class c = model1.getDataColumnClass(i);
            if (c.equals(Float.class) || c.equals(Double.class)) {
                objectArray1[iKept] = model1.getColumnName(i);
                associatedObjectArray1[iKept] = i+1;  // +1 because it is used in python calc expression
                iKept++;
            }
        }

        ParameterList parameterList1 = new ParameterList("param1");
        
        m_columnsParameter1 = new MultiObjectParameter(SEL_COLS1, "Columns", null, objectArray1, associatedObjectArray1, null, null);


        m_parameters = new ParameterList[1];
        m_parameters[0] = parameterList1;

        parameterList1.add(m_columnsParameter1);


    }

    @Override
    public void userParametersChanged() {
        m_generatedImage = null;
    }


    @Override
    public boolean calculationDone() {
        if (m_generatedImage != null) {
            return true;
        }
        return false;
    }

    @Override
    public boolean settingsDone() {
        if (m_columnsParameter1 == null) {
            return false;
        }
        
        List colList1 = (List) m_columnsParameter1.getAssociatedValues(true);
        
        if ((colList1 == null) || (colList1.isEmpty())) {
            return false;
        }

        return true;
    }

    @Override
    public ParameterError checkParameters() {
        return null;
    }


}