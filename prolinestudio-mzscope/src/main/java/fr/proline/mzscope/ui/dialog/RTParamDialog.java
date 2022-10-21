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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author CB205360
 */
public class RTParamDialog extends DefaultDialog{

        JTextField m_rtToleranceTF;

        public RTParamDialog(Window parent) {
            super(parent, Dialog.ModalityType.APPLICATION_MODAL);
            setTitle("Retention Time tolerance");
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
            JLabel nbrPointLabel = new JLabel("RT tolerance (s):");
            internalPanel.add(nbrPointLabel, c);
            
            c.gridx++;
            c.weightx = 1;
            m_rtToleranceTF = new JTextField(6);   
            m_rtToleranceTF.setText("40");
            m_rtToleranceTF.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    char c = e.getKeyChar();
                    if (!((c >= '0') && (c <= '9') ||
                        (c == '.') ||
                        (c == KeyEvent.VK_BACK_SPACE) ||
                        (c == KeyEvent.VK_DELETE))) {
                        getToolkit().beep();
                        e.consume();
                    }
                }
            });
            internalPanel.add(m_rtToleranceTF, c);                        
            
            
            return internalPanel;
        }
        
        public float getRTTolerance(){
            return Float.valueOf(m_rtToleranceTF.getText());
        }
        
}
