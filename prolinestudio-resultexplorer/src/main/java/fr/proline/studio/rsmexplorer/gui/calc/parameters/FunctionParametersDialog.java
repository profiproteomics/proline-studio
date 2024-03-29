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
package fr.proline.studio.rsmexplorer.gui.calc.parameters;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphConnector;
import fr.proline.studio.utils.IconManager;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import javax.swing.JPanel;

/**
 * Generic Dialog used to set the parameters for data analyzer function
 * @author JM235353
 */
public class FunctionParametersDialog extends DefaultDialog {
    
    private final ParameterList[] m_parameterList;
    private final CheckParameterInterface m_checkParamInterface;
    private final GraphConnector[] m_graphObjects;
    
    
    
    private int m_currentPanelNumber = 0;
    
    public FunctionParametersDialog(String name, Window parent, ParameterList[] parameterList, CheckParameterInterface checkParamInterface, GraphConnector[] graphObjects) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        m_parameterList = parameterList;
        m_checkParamInterface = checkParamInterface;
        m_graphObjects = graphObjects;
        
        setTitle(name+" Settings");

        setButtonVisible(BUTTON_HELP, false);

        setInternalComponent(createInternalPanel(m_currentPanelNumber));

        setButtonsState(m_currentPanelNumber, m_parameterList.length);
    }
    

    
    private void setButtonsState(int panelNumber, int nbPanels) {
        if (nbPanels == 1) {
            // nothing to do
            return;
        }
        
        if (panelNumber < nbPanels - 1) {
            // we are not on the last panel
            setButtonName(DefaultDialog.BUTTON_OK, "Next");
            setButtonIcon(DefaultDialog.BUTTON_OK, IconManager.getIcon(IconManager.IconType.ARROW));
        } else {
            setButtonName(DefaultDialog.BUTTON_OK, "OK");
            setButtonIcon(DefaultDialog.BUTTON_OK, IconManager.getIcon(IconManager.IconType.OK));
        }
        
        // init Back Button
        setButtonVisible(DefaultDialog.BUTTON_BACK, (panelNumber > 0));

        
    }
    
    private JPanel createInternalPanel(int panelNumber) {
        
        JPanel containerPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        ParameterList parameterList = m_parameterList[panelNumber];

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        ParameterList.ParametersPanel panel = parameterList.getPanel();
        panel.setParentDialog(this);
        containerPanel.add(panel, c);

        return containerPanel;
    }
    
    
    @Override
    protected boolean okCalled() {
        

        
        ParameterError error = m_parameterList[m_currentPanelNumber].checkParameters();
        // report error
        if (error == null) {
            error = m_checkParamInterface.checkParameters(m_graphObjects);
        }

        if (error != null) {
            setStatus(true, error.getErrorMessage());
            highlight(error.getParameterComponent());
            return false;
        }
        
        while (true) {
            if (m_currentPanelNumber == m_parameterList.length - 1) {
                // we were on the last panel : ok is called and all parameters are correctly set 
                return true;
            } else {

                m_currentPanelNumber++;
                
                // An entire panel with parameters can have been desabled
                if (!m_parameterList[m_currentPanelNumber].isEnable()) {
                    continue;
                }
                
                replaceInternalComponent(createInternalPanel(m_currentPanelNumber));
                setButtonsState(m_currentPanelNumber, m_parameterList.length);
                repack();
                revalidate();
                repaint();

                return false;
            }
        }
        
    }
    
    @Override
    protected boolean backCalled() {
        while (true) {
            if (m_currentPanelNumber == 0) {
                // we were on the first panel : it should not happen
                return false;
            } else {

                m_currentPanelNumber--;

                // An entire panel with parameters can have been desabled
                if (!m_parameterList[m_currentPanelNumber].isEnable()) {
                    continue;
                }

                replaceInternalComponent(createInternalPanel(m_currentPanelNumber));
                setButtonsState(m_currentPanelNumber, m_parameterList.length);
                repack();
                revalidate();
                repaint();

                return false;
            }
        }
    }

    
}
