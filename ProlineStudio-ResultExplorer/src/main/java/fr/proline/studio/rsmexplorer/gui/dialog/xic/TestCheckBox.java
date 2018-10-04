/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.studio.gui.CheckBoxTitledBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 *
 * @author VD225637
 */
public class TestCheckBox extends JDialog  {
    
    public TestCheckBox(){
        UIManager.put("TitledBorder.titleColor", new Color(59, 59, 59));
        
        JPanel panelA = new JPanel(new BorderLayout());
        CheckBoxTitledBorder m_crossAssgnementCBTitle = new CheckBoxTitledBorder("Cross Assignement", true);
        m_crossAssgnementCBTitle.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
        panelA.setBorder(m_crossAssgnementCBTitle);
        
        this.add(panelA);
        this.validate();
        this.pack();
    }
    
    public static void main(String[] args) {
        //		TestMain.doTest();
        TestCheckBox tm = new TestCheckBox();
        tm.setVisible(true);

    }
    
}
