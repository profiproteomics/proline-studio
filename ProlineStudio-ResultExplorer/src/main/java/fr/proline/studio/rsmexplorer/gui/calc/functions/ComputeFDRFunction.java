package fr.proline.studio.rsmexplorer.gui.calc.functions;

import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.PlotBaseAbstract;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.graphics.cursor.AbstractCursor;
import fr.proline.studio.graphics.cursor.CursorInfo;
import fr.proline.studio.graphics.cursor.CursorInfoList;
import fr.proline.studio.graphics.cursor.HorizontalCursor;
import fr.proline.studio.graphics.cursor.VerticalCursor;
import fr.proline.studio.parameter.AbstractLinkedParameters;
import fr.proline.studio.parameter.AbstractParameter;
import fr.proline.studio.parameter.ComponentParameterInterface;
import fr.proline.studio.parameter.DoubleParameter;
import fr.proline.studio.parameter.IntegerParameter;
import fr.proline.studio.parameter.ObjectParameter;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.parameter.ValuesFromComponentParameter;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataboxGraphics;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.python.data.ColDoubleData;
import fr.proline.studio.python.data.ColRef;
import fr.proline.studio.python.model.ExprTableModel;
import fr.proline.studio.python.data.Table;
import fr.proline.studio.python.model.ValuesTableModel;
import fr.proline.studio.python.interpreter.CalcCallback;
import fr.proline.studio.python.interpreter.CalcError;
import fr.proline.studio.python.interpreter.CalcInterpreterTask;
import fr.proline.studio.python.interpreter.CalcInterpreterThread;
import fr.proline.studio.python.interpreter.ResultVariable;
import fr.proline.studio.rsmexplorer.gui.GraphicsPanel;
import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import fr.proline.studio.rsmexplorer.gui.calc.ProcessCallbackInterface;
import static fr.proline.studio.rsmexplorer.gui.calc.functions.AdjustPFunction.createRCode;
import fr.proline.studio.rsmexplorer.gui.calc.graph.FunctionGraphNode;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphConnector;
import fr.proline.studio.rsmexplorer.gui.calc.graphics.LockedDataGraphicsModel;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.table.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.table.renderer.DoubleRenderer;
import fr.proline.studio.types.LogInfo;
import fr.proline.studio.types.LogRatio;
import fr.proline.studio.types.PValue;
import fr.proline.studio.types.PvalueAdjusted;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import org.python.core.PyFloat;

/**
 *
 * @author JM235353
 */
public class ComputeFDRFunction extends AbstractFunction {

    
    private static final String PVALUE_THRESHOLD = "PVALUE_THRESHOLD";
    private static final String LOGFC_THRESHOLD = "LOGFC_THRESHOLD";
    
    private static final String PVALUE_COL_PARAMETER = "PVALUE_COL_PARAMETER";
    private static final String LOGFC_COL_PARAMETER = "FCLOG_COL_PARAMETER";
    private static final String PI0PARAMETER = "PI0PARAMETER";
    private static final String ALPHAPARAMETER = "ALPHAPARAMETER";
    private static final String NBBINSPARAMETER = "NBBINSPARAMETER";
    private static final String PZPARAMETER = "PZPARAMETER";
    private static final String NUMERICVALUEARAMETER = "NUMERICVALUEARAMETER";

    
    private ObjectParameter m_pValueColumnParameter = null;
    private ObjectParameter m_logFCColumnParameter = null;
    
    private DoubleParameter m_pvalueThresholdParameter;
    private DoubleParameter m_logFCThresholdParameter;
    
    private ObjectParameter m_pi0MethodParameter = null;
    private DoubleParameter m_numericValueParameter = null;
    private DoubleParameter m_alphaParameter = null;
    private IntegerParameter m_nbinsParameter = null;
    private DoubleParameter m_pzParameter = null;
    private ValuesFromComponentParameter m_graphicalParameter = null;

    private VerticalCursor m_verticalCursor = null;
    private VerticalCursor m_mirrorVerticalCursor = null;
    private HorizontalCursor m_horizontalCursor = null;
    
    public final static int OUT_DATA_FDR = 0;
    public final static int OUT_DATA_DIFFERENTIAL_PROTEINS = 1;
    public final static int OUT_VALUES_NUMBER = 2;
    
    
    public ComputeFDRFunction(GraphPanel panel) {
        super(panel);
    }

        @Override
    public void inLinkModified() {
        super.inLinkModified();
        m_pValueColumnParameter = null;
        m_logFCColumnParameter = null;
        m_pvalueThresholdParameter = null;
        m_logFCThresholdParameter = null;
        m_pi0MethodParameter = null;
        m_numericValueParameter = null;
        m_alphaParameter = null;
        m_nbinsParameter = null;
        m_pzParameter = null;
    }

    @Override
    public String getName(int index) {
        if (m_pi0MethodParameter == null) {
            return "FDR Computation";
        }
        StringBuilder columnNameSb = new StringBuilder("FDR Computation ");
        String pi0Method = m_pi0MethodParameter.getStringValue();
        if (pi0Method.compareTo("Numeric Value") == 0) {
            columnNameSb.append(m_numericValueParameter.getStringValue());
        } else {
            columnNameSb.append(pi0Method);
        }
        
        if (index>=0) {
            columnNameSb.append(" / ");
            columnNameSb.append(getOutTooltip(index));
        }
        
        return columnNameSb.toString();
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
                return "FDR";
            case OUT_DATA_DIFFERENTIAL_PROTEINS:
                return "Differential Proteins";
        }
        return null;
    }

    
    @Override
    public void process(GraphConnector[] graphObjects, final FunctionGraphNode functionGraphNode, ProcessCallbackInterface callback) {
        setInError(false, null);
        
        if (m_parameters == null) {
            callback.finished(functionGraphNode);
            return;
        }

        Integer pvalueColIndex =(Integer) m_pValueColumnParameter.getAssociatedObjectValue();
        if ((pvalueColIndex == null) || (pvalueColIndex == -1)) {
            callback.finished(functionGraphNode);
            return;
        }
        
        Integer logFCColIndex =(Integer) m_logFCColumnParameter.getAssociatedObjectValue();
        if ((logFCColIndex == null) || (logFCColIndex == -1)) {
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

            ResultVariable[] parameters = new ResultVariable[2];
            ColRef pvalueCol = sourceTable.getCol(pvalueColIndex);
            parameters[0] = new ResultVariable(pvalueCol);
            ColRef logFCCol = sourceTable.getCol(logFCColIndex);
            parameters[1] = new ResultVariable(logFCCol);

            ResultVariable[] parametersForAdjustP = new ResultVariable[1];
            parametersForAdjustP[0] = parameters[0];

            StringBuilder codeSB1 = new StringBuilder();
            StringBuilder codeSB2 = new StringBuilder();
            
            codeSB1.append("computedFDR=Stats.computeFDR(");
            codeSB2.append("differentialProteins=Stats.differentialProteins(");
            
            StringBuilder codeSB3 = new StringBuilder();
            StringBuilder columnNameSb3 = new StringBuilder();
            createRCode(codeSB3, columnNameSb3, parametersForAdjustP, m_pi0MethodParameter, m_numericValueParameter, m_alphaParameter, m_nbinsParameter, m_pzParameter);
            
            
            StringBuilder codeSBFirstParameters = new StringBuilder();;
            for (int i = 0; i < parameters.length; i++) {
                codeSBFirstParameters.append(parameters[i].getName());
                codeSBFirstParameters.append(',');
            }

            codeSBFirstParameters.append(m_pvalueThresholdParameter.getStringValue());
            codeSBFirstParameters.append(',');
            codeSBFirstParameters.append(m_logFCThresholdParameter.getStringValue());
            
            String firstParameters = codeSBFirstParameters.toString();
            codeSB1.append(firstParameters);
            codeSB2.append(firstParameters);
            
            
            String pi0Method = m_pi0MethodParameter.getStringValue();
            if (pi0Method.compareTo("Numeric Value") == 0) {
                codeSB1.append(',');
                codeSB1.append(m_numericValueParameter.getStringValue());
            } else {
                codeSB1.append(",\"").append(pi0Method).append("\"");
            }
            codeSB1.append(",").append(m_alphaParameter.getStringValue());
            codeSB1.append(",").append(m_nbinsParameter.getStringValue());
            codeSB1.append(",").append(m_pzParameter.getStringValue());
            
            
            codeSB1.append(')');
            codeSB2.append(')');

            CalcCallback calcCallback = new CalcCallback() {

                @Override
                public void run(ArrayList<ResultVariable> variables, CalcError error) {
                    
                    m_stepCounts++;
                    try {
                        if (variables != null) {
                            // look for res
                            for (ResultVariable var : variables) {
                                if (var.getName().compareTo("computedFDR") == 0) {
                                    // we have found the result
                                    PyFloat fdr = (PyFloat) var.getValue();
                                    ArrayList<String> valuesName = new ArrayList<>(1);
                                    valuesName.add("FDR");
                                    ArrayList<String> values = new ArrayList<>(1);
                                    values.add(fdr.toString()+"%");
                                    
                                    addModel(new ValuesTableModel(valuesName, values));

                                } else if (var.getName().compareTo("differentialProteins") == 0) {
                                    // we have found the result
                                    Table resTable = (Table) var.getValue();

                                    ExprTableModel model = (ExprTableModel) resTable.getModel();
                                    
                                    GlobalTableModelInterface srcModel = graphObjects[0].getGlobalTableModelInterface();
                                    int[] cols = srcModel.getBestColIndex(PlotType.SCATTER_PLOT);
                                    int bestXColumnIndex = (cols!= null) ? cols[0] : -1;
                                    int bestYColumnIndex = (cols!= null) ? cols[1] : -1;
                                    
                                    CursorInfoList cursorInfoListX = new CursorInfoList();
                                    CursorInfo cursorInfo1 = new CursorInfo(m_verticalCursor.getValue());
                                    cursorInfo1.setColor(Color.blue);
                                    cursorInfo1.setStroke(AbstractCursor.LINE2_STROKE);
                                    cursorInfo1.setSelectable(Boolean.FALSE);
                                    cursorInfoListX.addCursorInfo(cursorInfo1);
                                    CursorInfo cursorInfo2 = new CursorInfo(m_mirrorVerticalCursor.getValue());
                                    cursorInfo2.setColor(Color.blue);
                                    cursorInfo2.setStroke(AbstractCursor.LINE2_STROKE);
                                    cursorInfo2.setSelectable(Boolean.FALSE);
                                    cursorInfoListX.addCursorInfo(cursorInfo2);
                                    model.addExtraColumnInfo(bestXColumnIndex, cursorInfoListX);
                                    
                                    CursorInfoList cursorInfoListY = new CursorInfoList();
                                    CursorInfo cursorInfoY = new CursorInfo(m_horizontalCursor.getValue());
                                    cursorInfoY.setColor(Color.blue);
                                    cursorInfoY.setStroke(AbstractCursor.LINE2_STROKE);
                                    cursorInfoY.setSelectable(Boolean.FALSE);
                                    cursorInfoListY.addCursorInfo(cursorInfoY);
                                    model.addExtraColumnInfo(bestYColumnIndex, cursorInfoListY);
                                    
                                    addModel(model);

                                } else if (var.getName().compareTo("adjustP") == 0) {
                                    // we have found the result
                                    ColDoubleData col = (ColDoubleData) var.getValue();
                                    // give a specific column name
                                    col.setColumnName(columnNameSb3.toString());
                                    sourceTable.addColumn(col, new PvalueAdjusted(), new DoubleRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 4, true, true));

                                }
                            }
                        } else if (error != null) {
                            setInError(error);
                        }
                        setCalculating(false);
                    } finally {
                        if (m_stepCounts == 3) {
                            callback.finished(functionGraphNode);
                        }
                    }
                }

            };

            m_stepCounts = 0;
            CalcInterpreterTask task1 = new CalcInterpreterTask(codeSB1.toString(), parameters, calcCallback);
            CalcInterpreterThread.getCalcInterpreterThread().addTask(task1);
            CalcInterpreterTask task3 = new CalcInterpreterTask(codeSB3.toString(), parameters, calcCallback);
            CalcInterpreterThread.getCalcInterpreterThread().addTask(task3);
            CalcInterpreterTask task2 = new CalcInterpreterTask(codeSB2.toString(), parameters, calcCallback);
            CalcInterpreterThread.getCalcInterpreterThread().addTask(task2);


        } catch (Exception e) {
            setInError(new CalcError(e, null, -1));
            setCalculating(false);
            callback.finished(functionGraphNode);
        }

    }
    private int m_stepCounts = 0;
    
    @Override 
    public void askDisplay(FunctionGraphNode functionGraphNode, int index) {
        display(functionGraphNode.getPreviousDataName(), getName(index), index);
    }
    
    @Override
    public ArrayList<WindowBox> getDisplayWindowBox(FunctionGraphNode functionGraphNode, int index) {
        return getDisplayWindowBoxList(functionGraphNode.getPreviousDataName(), getName(index), index);
    }
    

    /*@Override
    protected WindowBox getDisplayWindowBoxSingle(String dataName, String functionName, int resultIndex) {
        if (resultIndex <= 0) {
            return super.getDisplayWindowBoxSingle(dataName, functionName, resultIndex);
        } else {
            AbstractDataBox[] databoxes = new AbstractDataBox[2];
            databoxes[0] = new DataboxGeneric(dataName, functionName, false);
            databoxes[1] = new DataboxGraphics();
            databoxes[1].setLayout(SplittedPanelContainer.PanelLayout.HORIZONTAL);

            String windowName = (dataName == null) ? functionName : dataName + " " + functionName;
            WindowBox wbox = WindowBoxFactory.getFromBoxesWindowBox(windowName, databoxes, false, false, ' ');

            databoxes[0].setEntryData(m_globalTableModelInterface.get(1));
            return wbox;
        }
    }*/
    
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
        Object[] pValueObjectArray = new Object[nbColumnsKept];
        Object[] pValueAssociatedObjectArray = new Object[nbColumnsKept];
        int iKept = 0;
        int selectedIndexPValue = -1;
        for (int i = 0; i < nbColumns; i++) {
            Class c = model1.getDataColumnClass(i);
            if (c.equals(Float.class) || c.equals(Double.class)) {
                pValueObjectArray[iKept] = model1.getColumnName(i);
                PValue pvalue = (PValue) model1.getColValue(PValue.class, i);
                if (pvalue != null) {
                    LogInfo log = (LogInfo) model1.getColValue(LogInfo.class, i);
                    if ((log == null) || (log.noLog())) {
                        selectedIndexPValue = iKept;
                    }
                }
                pValueAssociatedObjectArray[iKept] = i+1;  // +1 because it is used in python calc expression
                iKept++;
            }
        }
        
        Object[] logFCObjectArray = new Object[nbColumnsKept];
        Object[] logFCAssociatedObjectArray = new Object[nbColumnsKept];
        iKept = 0;
        int selectedIndexLogFC = -1;
        for (int i = 0; i < nbColumns; i++) {
            Class c = model1.getDataColumnClass(i);
            if (c.equals(Float.class) || c.equals(Double.class)) {
                logFCObjectArray[iKept] = model1.getColumnName(i);
                LogRatio logRatio = (LogRatio) model1.getColValue(LogRatio.class, i);
                if (logRatio != null) {
                    selectedIndexLogFC = iKept;
                }
                logFCAssociatedObjectArray[iKept] = i+1;  // +1 because it is used in python calc expression
                iKept++;
            }
        }

        ParameterList parameterList1 = new ParameterList("param1");
        
        m_pValueColumnParameter = new ObjectParameter(PVALUE_COL_PARAMETER, "P Values Column", null, pValueObjectArray, pValueAssociatedObjectArray, selectedIndexPValue, null);
        m_logFCColumnParameter = new ObjectParameter(LOGFC_COL_PARAMETER, "Log FC Column", null, logFCObjectArray, logFCAssociatedObjectArray, selectedIndexLogFC, null);

        m_pvalueThresholdParameter = new DoubleParameter(PVALUE_THRESHOLD, "-Log10(PValue) Threshold", JTextField.class, 0d, 0d, null);
        m_logFCThresholdParameter = new DoubleParameter(LOGFC_THRESHOLD, "Log FC Threshold", JTextField.class, 0d, 0d, null);

        
        String[] pi0Values = { "Numeric Value", "abh", "bky", "jiang", "histo", "langaas", "pounds", "slim", "st.boot", "st.spline" };
        m_pi0MethodParameter = new ObjectParameter(PI0PARAMETER, "pi0 Method", pi0Values, 0, null);
        
        m_numericValueParameter = new DoubleParameter(NUMERICVALUEARAMETER, "Pi0 Value", JTextField.class, 1d, 0d, 1d); 
        m_alphaParameter = new DoubleParameter(ALPHAPARAMETER, "Alpha", JTextField.class, 0.05, 0d, 1d);
        m_nbinsParameter = new IntegerParameter(NBBINSPARAMETER, "Number of Bins", JSpinner.class, 20, 5, 100);
        m_pzParameter = new DoubleParameter(PZPARAMETER, "Pz", JTextField.class, 0.05, 0.01, 0.1);
        
        final GraphicsPanel graphicsPanel = createScatterParameter(model1);

        
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
        
        AbstractLinkedParameters graphicalLinkParameterVerticalCursor = new AbstractLinkedParameters(parameterList1) {
            @Override
            public void valueChanged(String value, Object associatedValue) {
                Double d = (Double) associatedValue;
                m_verticalCursor.setValue(d);
                m_mirrorVerticalCursor.setValue(-d);
                graphicsPanel.getPlotPanel().repaint();
            }
            
        };
        
        AbstractLinkedParameters graphicalLinkParameterHorizontalCursor = new AbstractLinkedParameters(parameterList1) {
            @Override
            public void valueChanged(String value, Object associatedValue) {
                Double d = (Double) associatedValue;
                m_horizontalCursor.setValue(d);
                graphicsPanel.getPlotPanel().repaint();
            }
            
        };

        m_parameters = new ParameterList[1];
        m_parameters[0] = parameterList1;

        parameterList1.add(m_pValueColumnParameter);
        parameterList1.add(m_logFCColumnParameter);
        parameterList1.add(m_pvalueThresholdParameter);
        parameterList1.add(m_logFCThresholdParameter);
        parameterList1.add(m_pi0MethodParameter);
        parameterList1.add(m_numericValueParameter);
        parameterList1.add(m_alphaParameter);
        parameterList1.add(m_nbinsParameter);
        parameterList1.add(m_pzParameter);
        parameterList1.add(m_graphicalParameter);
        
        parameterList1.getPanel(); // generate panel at once
        m_pi0MethodParameter.addLinkedParameters(linkedParameters); // link parameter, it will modify the panel
        m_logFCThresholdParameter.addLinkedParameters(graphicalLinkParameterVerticalCursor);
        m_pvalueThresholdParameter.addLinkedParameters(graphicalLinkParameterHorizontalCursor);
        
        // forbid to change values for some methods
        m_nbinsParameter.getComponent().setEnabled(false);
        m_pzParameter.getComponent().setEnabled(false);
        m_alphaParameter.getComponent().setEnabled(false);

    }
    
    private GraphicsPanel createScatterParameter(GlobalTableModelInterface srcModel) {
        int[] cols = srcModel.getBestColIndex(PlotType.SCATTER_PLOT);

        LockedDataGraphicsModel graphicsModelInterface = new LockedDataGraphicsModel(srcModel, PlotType.SCATTER_PLOT, cols);
        AbstractDataBox box = new DataboxGraphics(true);
        box.createPanel();
        box.setEntryData(graphicsModelInterface);
        GraphicsPanel graphicsPanel = (GraphicsPanel) box.getPanel();
        
        PlotBaseAbstract plot = graphicsPanel.getPlotGraphics();
        final BasePlotPanel basePlotPanel = plot.getBasePlotPanel();
        basePlotPanel.setPreferredSize(new Dimension(600,400));
        

        m_verticalCursor = new VerticalCursor(basePlotPanel, 0);
        m_verticalCursor.setMinValue(new Double(0));
        m_verticalCursor.setColor(Color.blue);
        m_verticalCursor.setStroke(AbstractCursor.LINE2_STROKE);
        m_mirrorVerticalCursor = new VerticalCursor(basePlotPanel, 0);
        m_mirrorVerticalCursor.setColor(Color.blue);
        m_mirrorVerticalCursor.setStroke(AbstractCursor.LINE2_STROKE);
        

        m_horizontalCursor = new HorizontalCursor(basePlotPanel, 0);
        m_horizontalCursor.setMinValue(new Double(0));
        m_horizontalCursor.setColor(Color.blue);
        m_horizontalCursor.setStroke(AbstractCursor.LINE2_STROKE);

        plot.addCursor(m_horizontalCursor);
        plot.addCursor(m_mirrorVerticalCursor);
        plot.addCursor(m_verticalCursor);
        
        
        ComponentParameterInterface componentParameter = new ComponentParameterInterface() {
            @Override
            public JComponent getComponent() {
                return basePlotPanel;
            }

            @Override
            public ParameterError checkParameter() {
                return null;
            }

            @Override
            public String getStringValue() {
                return String.valueOf(m_verticalCursor.getValue());
            }

            @Override
            public Object getObjectValue() {
                return m_verticalCursor.getValue();
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                AbstractCursor c = (AbstractCursor) e.getSource();
                
                if (c instanceof VerticalCursor) {
                    String v = ((VerticalCursor)c).getFormattedValue();
                    v = v.replaceAll(",", "."); //JPM.WART for 2,0E2
                    double d = new BigDecimal(v).doubleValue();
                    if (d<0) {
                        d = -d;
                    }
                    m_logFCThresholdParameter.setValue(String.valueOf(d));
                } else if  (c instanceof HorizontalCursor) {
                    String v = ((HorizontalCursor)c).getFormattedValue();
                    v = v.replaceAll(",", "."); //JPM.WART for 2,0E2
                    double d = new BigDecimal(v).doubleValue();
                    m_pvalueThresholdParameter.setValue(String.valueOf(d));
                }
            }
            
        };
        
        
        m_verticalCursor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                VerticalCursor c = (VerticalCursor) e.getSource();
                double v = c.getValue();
                 if (m_mirrorVerticalCursor.getValue() == -v) {
                    return;
                }
                m_mirrorVerticalCursor.setValue(-v);
            }
            
        });
        m_verticalCursor.addActionListener(componentParameter);
        
        m_mirrorVerticalCursor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                VerticalCursor c = (VerticalCursor) e.getSource();
                double v = c.getValue();
                if (m_verticalCursor.getValue() == -v) {
                    return;
                }
                m_verticalCursor.setValue(-v);
            }
            
        });
         m_mirrorVerticalCursor.addActionListener(componentParameter);
        
        
        m_horizontalCursor.addActionListener(componentParameter);
        
        m_graphicalParameter = new ValuesFromComponentParameter("LogFCAndPValue", "LogFCAndPValue", componentParameter); 
        m_graphicalParameter.forceShowLabel(AbstractParameter.LabelVisibility.NO_VISIBLE);

        return graphicsPanel;
    }
    
    @Override
    public AbstractFunction cloneFunction(GraphPanel p) {
        AbstractFunction clone = new ComputeFDRFunction(p);
        clone.cloneInfo(this);
        return clone;
    }
    
    
    @Override
    public boolean calculationDone() {
        if ((m_globalTableModelInterface != null) && (!m_globalTableModelInterface.isEmpty())) {
            return true;
        }
        return false;
    }
    
    @Override
    public void userParametersChanged() {
        // need to recalculate model
        m_globalTableModelInterface = null;
    }
    
    @Override
    public boolean settingsDone() {

        if (m_parameters == null) {
            return false;
        }

        if (m_pValueColumnParameter == null) {
            return false;
        }

        Integer colIndex = (Integer) m_pValueColumnParameter.getAssociatedObjectValue();
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