/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.ui.dialog;

import fr.proline.studio.gui.DefaultDialog;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.jdesktop.swingx.JXComboBox;

/**
 *
 * @author VD225637
 */
public class SmoothingParamDialog extends DefaultDialog{

        JTextField m_nbrPoint;
        JComboBox m_smoothingMethods; 
        public final static String PARTIAL_SG_SMOOTHER = "Partial Savitzky-Golay Smoother";
        public final static String SG_SMOOTHER = "Savitzky-Golay Smoother";
        public final static String BOTH_SMOOTHER = "All Smoothers";
        String[] methods = {PARTIAL_SG_SMOOTHER, SG_SMOOTHER, BOTH_SMOOTHER};

        public SmoothingParamDialog(Window parent) {
            super(parent, Dialog.ModalityType.APPLICATION_MODAL);
            setTitle("Smooting parameters ");
            setInternalComponent(createInternalPanel());
        }

        private JPanel createInternalPanel() {
            JPanel internalPanel = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.BOTH;
            c.insets = new java.awt.Insets(5, 5, 5, 5);

            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 1;
            c.weightx = 0;
            JLabel nbrPointLabel = new JLabel("Number of points ");
            internalPanel.add(nbrPointLabel, c);
            
            c.gridx++;
            c.weightx = 1;
            m_nbrPoint = new JTextField(10);   
            m_nbrPoint.setText("5");
            m_nbrPoint.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    char c = e.getKeyChar();
                    if (!((c >= '0') && (c <= '9') ||
                        (c == KeyEvent.VK_BACK_SPACE) ||
                        (c == KeyEvent.VK_DELETE))) {
                        getToolkit().beep();
                        e.consume();
                    }
                }
            });
            internalPanel.add(m_nbrPoint, c);                        
            
            c.gridx = 0;
            c.gridy++;
            c.weightx = 0;
            JLabel methodsLabel = new JLabel("Smoothing method ");
            internalPanel.add(methodsLabel, c);
            
            c.gridx++;
            c.weightx = 1;
            m_smoothingMethods = new JXComboBox(methods);
            m_smoothingMethods.setSelectedItem(SG_SMOOTHER);
            internalPanel.add(m_smoothingMethods, c);
            
            return internalPanel;
        }
        
        public int getNbrPoint(){
            return Integer.valueOf(m_nbrPoint.getText());
        }
        
        public String getMethod(){
            return m_smoothingMethods.getSelectedItem().toString();
        }
    
}
