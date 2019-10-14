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

import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.id.ProjectId;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.python.interpreter.CalcError;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import fr.proline.studio.rsmexplorer.gui.calc.ProcessCallbackInterface;
import fr.proline.studio.rsmexplorer.gui.calc.graph.FunctionGraphNode;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphConnector;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphNode;
import fr.proline.studio.rsmexplorer.gui.calc.parameters.CheckParameterInterface;
import fr.proline.studio.rsmexplorer.gui.calc.parameters.FunctionParametersDialog;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.utils.IconManager;
import java.awt.Color;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import org.openide.windows.WindowManager;

/**
 * Abstract Parent Function for all functions of the data analyzer
 * @author JM235353
 */
public abstract class AbstractFunction implements CheckParameterInterface {

    protected ArrayList<GlobalTableModelInterface> m_globalTableModelInterface;
    
    protected ParameterList[] m_parameters = null;
    
    private boolean m_calculating = false;
    private boolean m_inError = false;
    protected boolean m_settingsBeingDone = false;
    private String m_errorMessage = null;
    
    private ArrayList<SplittedPanelContainer.PanelLayout> m_autoDisplayLayoutDuringProcess = null;
    
    protected GraphPanel m_panel;
    
    public enum FUNCTION_TYPE {

        AdjustPFunction(0),
        ColumnFilterFunction(1),
        ComputeFDRFunction(2),
        DiffAnalysisFunction(3),
        DiffFunction(4),
        ExpressionFunction(5),
        FilterFunction(6),
        ImportTSVFunction(7),
        JoinFunction(8),
        Log2Function(9),
        Log10Function(10),
        MissingValuesImputationFunction(11),
        NormalizationFunction(12),
        PValueFunction(13),
        QuantiFilterFunction(14),
        SCDiffAnalysisFunction(15),
        TtdFunction(16);

        private int m_id;

        private FUNCTION_TYPE(int id) {
            m_id = id;
        }
        
        public int getId() {
            return m_id;
        }

    }
    
    private FUNCTION_TYPE m_type;
    
    public AbstractFunction(GraphPanel panel, FUNCTION_TYPE type) {
        m_panel = panel;
        m_type = type;
    }

    public int getTypeId() {
        return m_type.getId();
    }
    
    public void inLinkModified() {
        m_parameters = null;
        m_globalTableModelInterface = null;

    }

    public void setAutoDisplayLayoutDuringProcess(ArrayList<SplittedPanelContainer.PanelLayout> layout) {
        m_autoDisplayLayoutDuringProcess = layout;
    }
    
    public ArrayList<SplittedPanelContainer.PanelLayout> getAutoDisplayLayoutDuringProcess() {
        return m_autoDisplayLayoutDuringProcess;
    }
    
    protected void setCalculating(boolean v) {
        if (v ^ m_calculating) {
            m_calculating = v;
            m_panel.repaint();
        }
    }
    
    protected void setInError(CalcError error) {
        if (error == null) {
            setInError(false, null);
        } else {
            setInError(true, error.getFullErrorMessage());
        }
    }
    protected void setInError(boolean v, String errorMessage) {
        if (v ^ m_inError) {
            m_inError = v;
            m_panel.repaint();
        }
        m_errorMessage = errorMessage;
    }
    
    public String getErrorMessage() {
        return m_errorMessage;
    }
    
    public String getName() {
        return getName(-1);
    }
    public abstract String getName(int index);
    public abstract int getNumberOfInParameters();
    public abstract int getNumberOfOutParameters();
    
    public int getMaximumNumberOfInParameters() {
        return getNumberOfInParameters();
    }
    
    
    public String getOutTooltip(int index) {
        return null;
    }
    
    public abstract void process(GraphConnector[] graphObjects, FunctionGraphNode functionGraphNode, ProcessCallbackInterface callback);
    
    public abstract void askDisplay(FunctionGraphNode functionGraphNode, int index);
    public abstract ArrayList<WindowBox> getDisplayWindowBox(FunctionGraphNode functionGraphNode, int index);
    
    public GlobalTableModelInterface getMainGlobalTableModelInterface(int index) {
        if (m_globalTableModelInterface == null) {
            return null;
        }
        return m_globalTableModelInterface.get(index);
    }
    
    public ArrayList<GlobalTableModelInterface> getGlobalTableModelInterfaceList() {
        return m_globalTableModelInterface;
    }
    
    public int getNumberOfResults() {
        if (m_globalTableModelInterface == null) {
            return 0;
        }
        return m_globalTableModelInterface.size();
    }

    public void addModel(GlobalTableModelInterface model) {
        if (m_globalTableModelInterface == null) {
            m_globalTableModelInterface = new ArrayList<>();
        }

        m_globalTableModelInterface.add(model);
    }

    public abstract void generateDefaultParameters(GraphConnector[] graphObjects);
    
    public abstract void userParametersChanged();
    public abstract AbstractFunction cloneFunction(GraphPanel p);
    

    protected void display(String dataName, String functionName, int resultIndex) {
        WindowBox windowBox = WindowBoxFactory.getGenericWindowBox(dataName, functionName, IconManager.IconType.CHALKBOARD, false);
        ProjectId projectId = (ProjectId) m_globalTableModelInterface.get(resultIndex).getSingleValue(ProjectId.class);
        long id = (projectId!=null) ? projectId.getId() : -1L;
        windowBox.setEntryData(id, m_globalTableModelInterface.get(resultIndex));
        DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(windowBox);
        win.open();
        win.requestActive();
    }
    
    protected ArrayList<WindowBox> getDisplayWindowBoxList(String dataName, String functionName, int resultIndex) {
        if (m_globalTableModelInterface == null) {
            return null;
        }
        
        ArrayList<WindowBox> windowBoxList = null;
        int size = m_globalTableModelInterface.size();
        if ((resultIndex == -1) || (resultIndex >= size)) {

            windowBoxList = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                windowBoxList.add(getDisplayWindowBoxSingle(dataName, functionName, i));
            }
        } else {
            windowBoxList = new ArrayList<>(1);
            windowBoxList.add(getDisplayWindowBoxSingle(dataName, functionName, resultIndex));
        }
        return windowBoxList;
    }
    protected WindowBox getDisplayWindowBoxSingle(String dataName, String functionName, int resultIndex) {
        WindowBox windowBox = WindowBoxFactory.getGenericWindowBox(dataName, functionName, IconManager.IconType.CHALKBOARD, false);
        ProjectId projectId = (ProjectId) m_globalTableModelInterface.get(resultIndex).getSingleValue(ProjectId.class);
        long id = (projectId!=null) ? projectId.getId() : -1L;
        windowBox.setEntryData(id, m_globalTableModelInterface.get(resultIndex));
        return windowBox;
    }
    
    public abstract boolean calculationDone();
    public abstract boolean settingsDone();
    public boolean isCalculating() {
        return m_calculating;
    }
    public boolean inError() {
        return m_inError;
    }
    public void resetError() {
        m_inError = false;
        m_errorMessage = null;
    }
    public boolean isSettingsBeingDone() {
        return m_settingsBeingDone;
    }
    
    public boolean settings(GraphConnector[] graphObjects, GraphNode node) {
        
        boolean thereIsNoParameterAtStart = (m_parameters == null);
        
        if (thereIsNoParameterAtStart) {
            generateDefaultParameters(graphObjects);
        }
        if (m_parameters == null) {
            return false;
        }

        m_settingsBeingDone = true;
        try {
            FunctionParametersDialog dialog = new FunctionParametersDialog(getName(), WindowManager.getDefault().getMainWindow(), m_parameters, this, graphObjects);
            dialog.setImageInfo(m_panel, node.getX()-60, node.getY()-40, node.getXEnd()-node.getX()+120, node.getYEnd()-node.getY()+80);
            dialog.centerToWindow(WindowManager.getDefault().getMainWindow());
            dialog.setVisible(true);
            if (dialog.getButtonClicked() == FunctionParametersDialog.BUTTON_OK) {
                userParametersChanged();
                return true;
            }
            if (thereIsNoParameterAtStart) {
                m_parameters = null; // JPM.HACK : we do not keep default parameters, to avoid that default parameters are used at the next execution
            }
            return false;
        } finally {
            m_settingsBeingDone = false;
        }
    }

    
    public ImageIcon getIcon() {
        return IconManager.getIcon(IconManager.IconType.FUNCTION);
    }
    
    public Color getFrameColor() {
        return null;
    }
    
    public String getDataName() {
        return null;
    }
    
    public void cloneInfo(AbstractFunction src) {
        m_autoDisplayLayoutDuringProcess = src.m_autoDisplayLayoutDuringProcess;
    }
    
}
