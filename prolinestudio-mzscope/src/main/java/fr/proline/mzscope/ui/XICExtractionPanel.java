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
package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.MsnExtractionRequest;
import fr.proline.mzscope.ui.model.MzScopePreferences;
import fr.proline.mzscope.utils.Display;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The extraction Panel contains the different parameters that could be changed
 * for the extraction: mass, tolerance 
 * for a DIA file contains Fragment m/z
 * @author MB243701
 */
public class XICExtractionPanel extends JPanel{
    private static Logger logger = LoggerFactory.getLogger("ProlineStudio.mzScope.XICExtractionPanel");

    private JScrollPane scrollPane;
    private JPanel internalPanel;
    private JPanel mainPanel;
    
    private JPanel massRangePanel;
    private JTextField massRangeTF;
    private JPanel tolerancePanel;
    private JTextField toleranceTF;
    
    private JPanel fragmentMassRangePanel;
    private JTextField  fragmentMassRangeTF;
    private JPanel fragmentTolerancePanel;
    private JTextField fragmentToleranceTF;
    
    private final IMzScopeController appController;
    
    public XICExtractionPanel(IMzScopeController appController) {
        initComponents();
        this.appController = appController;
        toleranceTF.setText(Integer.toString(Math.round(MzScopePreferences.getInstance().getMzPPMTolerance())));
        fragmentToleranceTF.setText(Integer.toString(Math.round(MzScopePreferences.getInstance().getFragmentMzPPMTolerance())));
        
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        this.add(getScrollPane(), BorderLayout.CENTER);
    }
    
    private JScrollPane getScrollPane() {
        if (scrollPane == null) {
            scrollPane = new JScrollPane(getInternalPanel());
            this.add(scrollPane);
        }
        return scrollPane;
    }
    
    private JPanel getInternalPanel(){
        if (internalPanel == null) {
            internalPanel = new JPanel();
            internalPanel.setLayout(new BorderLayout());
            internalPanel.add(getMainPanel(), BorderLayout.PAGE_START);
        }
        return internalPanel;
    }
    
    private JPanel getMainPanel() {
        if (mainPanel == null) {
            mainPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
            mainPanel.add(getlMassRangePanel());
            mainPanel.add(getTolerancePanel());
            mainPanel.add(getFragmentMassRangePanel());
            mainPanel.add(getFragmentTolerancePanel());
        }
        return mainPanel;
    }

    private JPanel getlMassRangePanel() {
        if (massRangePanel == null) {
            massRangePanel = new JPanel();
            massRangePanel.setLayout(new FlowLayout(FlowLayout.LEADING));
            JLabel massRangeLabel = new JLabel();
            massRangeLabel.setText("Precursor Mass :");
            massRangePanel.add(massRangeLabel);
            massRangePanel.add(getMassRangeTF());
        }
        return massRangePanel;
    }


    private JTextField getMassRangeTF() {
        if (massRangeTF == null) {
            massRangeTF = new JTextField();
            massRangeTF.setToolTipText("<html><p width=\"500\">Precursor mass range to extract. A single value can be entered (the following tolerance value will be used) or a range of values using '-' delimiter</p></html>");
            massRangeTF.setColumns(10);
            massRangeTF.setPreferredSize(new Dimension(massRangeTF.getPreferredSize().width, 16));
            massRangeTF.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    startExtraction();
                }
            });
        }
        return massRangeTF;
    }

    private JPanel getTolerancePanel() {
        if (tolerancePanel == null) {
            tolerancePanel = new JPanel();
            tolerancePanel.setLayout(new FlowLayout(FlowLayout.LEADING));
            JLabel toleranceLabel = new JLabel();
            toleranceLabel.setText("+/-");
            tolerancePanel.add(toleranceLabel);
            tolerancePanel.add(getToleranceTF());
            JLabel toleranceUnitLabel = new JLabel();
            toleranceUnitLabel.setText("ppm");
            tolerancePanel.add(toleranceUnitLabel);
            
        }
        return tolerancePanel;
    }

    private JTextField getToleranceTF() {
        if (toleranceTF == null) {
            toleranceTF = new JTextField();
            toleranceTF.setColumns(3);
            toleranceTF.setPreferredSize(new Dimension(toleranceTF.getPreferredSize().width, 16));
            toleranceTF.setToolTipText("Precursor mass tolerance in ppm");
            toleranceTF.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    float ppm = Float.parseFloat(toleranceTF.getText().trim());
                    MzScopePreferences.getInstance().setMzPPMTolerance(ppm);
                    if (!getMassRangeTF().getText().isEmpty()) {
                        startExtraction();
                    }
                }
            });
        }
        return toleranceTF;
    }
    
    private JPanel getFragmentMassRangePanel() {
        if (fragmentMassRangePanel == null) {
            fragmentMassRangePanel = new JPanel();
            fragmentMassRangePanel.setLayout(new FlowLayout(FlowLayout.LEADING));
            JLabel fragmentMassLabel = new JLabel();
            fragmentMassLabel.setText("Fragment mass:");
            fragmentMassRangePanel.add(fragmentMassLabel);
            fragmentMassRangePanel.add(getFragmentMassRangeTF());
        }
        return fragmentMassRangePanel;
    }
    
 
    private JTextField getFragmentMassRangeTF() {
        if (fragmentMassRangeTF == null) {
            fragmentMassRangeTF = new JTextField();
            fragmentMassRangeTF.setToolTipText("<html><p width=\"500\">Fragment mass range to extract. A single value can be entered (the following tolerance value will be used) or a range of values using '-' delimiter</p></html>");
            fragmentMassRangeTF.setColumns(10);
            fragmentMassRangeTF.setPreferredSize(new Dimension(fragmentMassRangeTF.getPreferredSize().width, 16));
            fragmentMassRangeTF.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    startExtraction();
                }
            });
        }
        return fragmentMassRangeTF;
    }
    
       private JPanel getFragmentTolerancePanel() {
        if (fragmentTolerancePanel == null) {
            fragmentTolerancePanel = new JPanel();
            fragmentTolerancePanel.setLayout(new FlowLayout(FlowLayout.LEADING));
            JLabel gramentToleranceLabel = new JLabel();
            gramentToleranceLabel.setText("+/-");
            fragmentTolerancePanel.add(gramentToleranceLabel);
            fragmentTolerancePanel.add(getFragmentToleranceTF());
            JLabel fragmentToleranceUnitLabel = new JLabel();
            fragmentToleranceUnitLabel.setText("ppm");
            fragmentTolerancePanel.add(fragmentToleranceUnitLabel);
            
        }
        return fragmentTolerancePanel;
    }

    private JTextField getFragmentToleranceTF() {
        if (fragmentToleranceTF == null) {
            fragmentToleranceTF = new JTextField();
            fragmentToleranceTF.setColumns(3);
            fragmentToleranceTF.setPreferredSize(new Dimension(fragmentToleranceTF.getPreferredSize().width, 16));
            fragmentToleranceTF.setToolTipText("Fragment mass tolerance in ppm");
            fragmentToleranceTF.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    float ppm = Float.parseFloat(fragmentToleranceTF.getText().trim());
                    MzScopePreferences.getInstance().setFragmentMzPPMTolerance(ppm);
                    if (!getFragmentMassRangeTF().getText().isEmpty()) {
                        startExtraction();
                    }
                }
            });
        }
        return fragmentToleranceTF;
    }


    private void startExtraction() {
        if (massRangeTF.getText() == null || massRangeTF.getText().isEmpty()){
            return;
        }
        MsnExtractionRequest.Builder builder = MsnExtractionRequest.builder();
        String text = massRangeTF.getText().trim();
        double firstMzValue = Double.NaN;
        double secondMzValue = Double.NaN;
        float ppm;
        String[] masses = text.split("-");
        try{
            firstMzValue = Double.parseDouble(masses[0]); // will be updated later in this method
        }catch(NumberFormatException e){
            JOptionPane.showMessageDialog(this, "The mass value is incorrect: "+masses[0]);
            return;
        }
        try{
            ppm = Float.parseFloat(toleranceTF.getText().trim());
            MzScopePreferences.getInstance().setMzPPMTolerance(ppm);
        }catch(NumberFormatException e){
            JOptionPane.showMessageDialog(this, "The tolerance value is incorrect: "+toleranceTF.getText().trim());
            return;
        }
        if (masses.length == 1) {
            builder.setMz(firstMzValue);
            builder.setMzTolPPM(ppm);
        } else {
            try{
                secondMzValue = Double.parseDouble(masses[1]);
                builder.setMinMz(firstMzValue);
                builder.setMaxMz(secondMzValue);
            }catch(NumberFormatException e){
                JOptionPane.showMessageDialog(this, "The max mass value is incorrect: "+masses[1]);
                return;
            }
        }

        if (fragmentMassRangeTF.isEnabled() && fragmentMassRangeTF.getText() != null && !fragmentMassRangeTF.getText().isEmpty() ){
        text = fragmentMassRangeTF.getText().trim();
        masses = text.split("-");
        try{
            firstMzValue = Double.parseDouble(masses[0]); // will be updated later in this method
        }catch(NumberFormatException e){
            JOptionPane.showMessageDialog(this, "The mass value is incorrect: "+masses[0]);
            return;
        }
        try{
            ppm = Float.parseFloat(fragmentToleranceTF.getText().trim());
            MzScopePreferences.getInstance().setFragmentMzPPMTolerance(ppm);
        }catch(NumberFormatException e){
            JOptionPane.showMessageDialog(this, "The tolerance value is incorrect: "+fragmentToleranceTF.getText().trim());
            return;
        }
        if (masses.length == 1) {
            builder.setFragmentMz(firstMzValue);
            builder.setFragmentMzTolPPM(ppm);
        } else {
            try{
                secondMzValue = Double.parseDouble(masses[1]);
                builder.setFragmentMinMz(firstMzValue);
                builder.setFragmentMaxMz(secondMzValue);
            }catch(NumberFormatException e){
                JOptionPane.showMessageDialog(this, "The max mass value is incorrect: "+masses[1]);
                return;
            }
        }


        }
        
        IRawFileViewer currentViewer = appController.getCurrentRawFileViewer();
        if (currentViewer != null) {
            currentViewer.extractAndDisplayChromatogram(builder.build(), new Display(currentViewer.getChromatogramDisplayMode()), null);
        }
    }

    public void setDIAEnabled(boolean diaEnabled){
        getFragmentMassRangeTF().setEnabled(diaEnabled);
        getFragmentToleranceTF().setEditable(diaEnabled);
    }
        
}
