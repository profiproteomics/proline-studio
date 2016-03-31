package fr.proline.studio.rsmexplorer.gui.calc.parameters;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.rsmexplorer.gui.calc.graph.AbstractConnectedGraphObject;
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
    private final AbstractConnectedGraphObject[] m_graphObjects;
    
    private int m_currentPanelNumber = 0;
    
    public FunctionParametersDialog(String name, Window parent, ParameterList[] parameterList, CheckParameterInterface checkParamInterface, AbstractConnectedGraphObject[] graphObjects) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        m_parameterList = parameterList;
        m_checkParamInterface = checkParamInterface;
        m_graphObjects = graphObjects;
        
        setTitle(name+" Settings");

        setButtonVisible(BUTTON_HELP, false);

        setInternalComponent(createInternalPanel(m_currentPanelNumber));

        initOkButton(m_currentPanelNumber, m_parameterList.length);
    }
    
    private void initOkButton(int panelNumber, int nbPanels) {
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
        containerPanel.add(parameterList.getPanel(), c);
        
        
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
                
                replaceInternaleComponent(createInternalPanel(m_currentPanelNumber));
                initOkButton(m_currentPanelNumber, m_parameterList.length);
                repack();
                revalidate();
                repaint();

                return false;
            }
        }
        
    }
    
    
}
