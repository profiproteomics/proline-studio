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
package fr.proline.studio.rsmexplorer.gui.calc.graphics;

import fr.proline.studio.WindowManager;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.python.interpreter.CalcError;
import fr.proline.studio.rserver.dialog.ImageViewerTopComponent;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopPanel;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopPanel;
import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import fr.proline.studio.rsmexplorer.gui.calc.ProcessCallbackInterface;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphConnector;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphNode;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphicGraphNode;
import fr.proline.studio.rsmexplorer.gui.calc.parameters.CheckParameterInterface;
import fr.proline.studio.rsmexplorer.gui.calc.parameters.FunctionParametersDialog;
import fr.proline.studio.utils.IconManager;
import java.awt.Image;
import java.util.ArrayList;
import javax.swing.ImageIcon;

/**
 * Base class for the definition of a Graphic (scatter, histogram...) with its parameters
 * in the data analyzer
 * @author JM235353
 */
public abstract class AbstractGraphic implements CheckParameterInterface {

    protected LockedDataGraphicsModel m_graphicsModelInterface;
    protected Image m_generatedImage;
    
    protected ParameterList[] m_parameters = null;

    private boolean m_calculating = false;
    private boolean m_settingsBeingDone = false;
    private boolean m_inError = false;
    private String m_errorMessage = null;
    
    private  ArrayList<SplittedPanelContainer.PanelLayout> m_autoDisplayLayoutDuringProcess = null;
    
    protected GraphPanel m_panel;

    public enum GRAPHIC_TYPE {
        CalibrationPlotGraphic(0),
        ParallelCoordinatesGraphic(1),
        ScatterGraphic(2),
        HistogramGraphic(3),
        VennDiagramGraphic(4),
        BoxPlotGraphic(5),
        DensityPlotGraphic(6),
        VarianceDistPlotGraphic(7);
        
        
        private int m_id;

        GRAPHIC_TYPE(int id) {
            m_id = id;
        }
        
        public int getId() {
            return m_id;
        }

    }
    
    private GRAPHIC_TYPE m_type;
    
    public AbstractGraphic(GraphPanel panel, GRAPHIC_TYPE graphicType) {
        m_panel = panel;
        m_type = graphicType;
    }
    
    public int getTypeId() {
        return m_type.getId();
    }
    
    
    public void cloneInfo(AbstractGraphic src) {
        m_autoDisplayLayoutDuringProcess = src.m_autoDisplayLayoutDuringProcess;
    }

    public void inLinkDeleted() {
        m_parameters = null;
        m_graphicsModelInterface = null;
        m_generatedImage = null;
    }

    
    public abstract String getName();

    public LockedDataGraphicsModel getGraphicsModelInterface() {
        return m_graphicsModelInterface;
    }

    public abstract void process(GraphConnector[] graphObjects, GraphicGraphNode graphicGraphNode, ProcessCallbackInterface callback);
    public abstract void generateDefaultParameters(GraphConnector[] graphObjects);
    public abstract void userParametersChanged();
    public abstract AbstractGraphic cloneGraphic(GraphPanel p);
    

    
    public void setAutoDisplayLayoutDuringProcess( ArrayList<SplittedPanelContainer.PanelLayout> layout) {
        m_autoDisplayLayoutDuringProcess = layout;
    }
    
    public ArrayList<SplittedPanelContainer.PanelLayout> getAutoDisplayLayoutDuringProcess() {
        return m_autoDisplayLayoutDuringProcess;
    }
        
    public void display(String dataName) {
        String functionName = getName();
        if (m_generatedImage != null) {
            ImageViewerTopComponent win = new ImageViewerTopComponent(dataName + " " + functionName, m_generatedImage);
            WindowManager.getDefault().getMainWindow().displayWindow(win);
        } else {
            WindowBox windowBox = WindowBoxFactory.getGraphicsWindowBox(dataName, m_graphicsModelInterface, true);
            DataBoxViewerTopPanel win = new DataBoxViewerTopPanel(windowBox);
            WindowManager.getDefault().getMainWindow().displayWindow(win);
        }
    }
    
    public  WindowBox getDisplayWindowBox(String dataName) {
        String functionName = getName();
        if (m_generatedImage != null) {
            return WindowBoxFactory.getImageWindowBox(dataName+" "+functionName, m_generatedImage);
        } else {
            return WindowBoxFactory.getGraphicsWindowBox(dataName, m_graphicsModelInterface, true);
        }
    }
    
    public abstract boolean calculationDone();
    public abstract boolean settingsDone();
    public boolean isCalculating() {
        return m_calculating;
    }
    public boolean inError() {
        return m_inError;
    }
    public boolean isSettingsBeingDone() {
        return m_settingsBeingDone;
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
        return IconManager.getIcon(IconManager.IconType.PENCIL_RULER);
    }
    
}

