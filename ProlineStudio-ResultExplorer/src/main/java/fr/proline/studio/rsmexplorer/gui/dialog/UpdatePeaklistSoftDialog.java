/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog;


import fr.proline.core.orm.uds.PeaklistSoftware;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.gui.DefaultDialog;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

/**
 *
 * @author VD225637
 */
public class UpdatePeaklistSoftDialog extends DefaultDialog {
    
    private JComboBox m_peaklistSoftwaresComboBox = null;
    private static UpdatePeaklistSoftDialog m_singletonDialog = null;
    private HashMap<String, PeaklistSoftware> m_peaklistByIdent = null;
          
     public static UpdatePeaklistSoftDialog getDialog(Window parent/*, long projectId*/) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new UpdatePeaklistSoftDialog(parent);
        }

        return m_singletonDialog;
    }
     
     private UpdatePeaklistSoftDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("Update Spectrum Parameters");
       
//        setHelpURL("http://biodev.extra.cea.fr/docs/proline/doku.php?id=how_to:studio:importmascot");
        
//        setButtonVisible(BUTTON_LOAD, true);
//        setButtonVisible(BUTTON_SAVE, true);
        
        setResizable(true);

        setInternalComponent(createInternalPanel());   
        pack();
    }

    private JPanel createInternalPanel() {
         
        JPanel panel = new JPanel(new GridBagLayout()); 
        panel.setBorder(BorderFactory.createTitledBorder("Peaklist Softwares"));
        GridBagConstraints c = new GridBagConstraints();        
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        JTextArea helpTxt = new JTextArea("Choose Peaklist Software to use to generate (new) spectrum parameters.");
        helpTxt.setRows(1);
        helpTxt.setForeground(Color.gray);
        panel.add(helpTxt, c);
        
        
        c.gridx++;
        c.gridwidth = 1;
        c.gridy=0;
        JLabel peaklistSoftwareLabel = new JLabel("Peaklist Software :");
        peaklistSoftwareLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(peaklistSoftwareLabel, c);
        
        c.gridy++;                      
        PeaklistSoftware[] peaksofts = DatabaseDataManager.getDatabaseDataManager().getPeaklistSoftwaresWithNullArray();
        m_peaklistSoftwaresComboBox = new JComboBox();   
        m_peaklistByIdent = new HashMap<>();
        for(PeaklistSoftware nextPLS : peaksofts){
            if(nextPLS == null)
                 m_peaklistSoftwaresComboBox.addItem(null);
            else{
                String nextPLSIdent = getPeakListSoftwareString(nextPLS);
                m_peaklistByIdent.put(nextPLSIdent, nextPLS);
                m_peaklistSoftwaresComboBox.addItem(nextPLSIdent);
            }
        }      
        panel.add(m_peaklistSoftwaresComboBox, c);        
        panel.revalidate();
        return panel;
    }
    
    public String getPeakListSoftwareString(PeaklistSoftware o) {
        String version = o.getVersion();
        if (version == null) {
            return o.getName();
        }
        return o.getName()+" "+version;
    }
    
    public Long getSelectedPeaklistSoftwareId(){
        if(m_peaklistSoftwaresComboBox.getSelectedItem() != null){
            return m_peaklistByIdent.get(m_peaklistSoftwaresComboBox.getSelectedItem().toString()).getId();   
        }
            
        return null;
    }
     
    
    @Override
    protected boolean okCalled() {
        return checkParameters();
    }

    private boolean checkParameters() {
        // check parameters
        if (m_peaklistSoftwaresComboBox.getSelectedItem() == null) {
            setStatus(true, "Specify Peaklist software to use");
            highlight(m_peaklistSoftwaresComboBox);
            return false;
        }
        return true;
    }
}
