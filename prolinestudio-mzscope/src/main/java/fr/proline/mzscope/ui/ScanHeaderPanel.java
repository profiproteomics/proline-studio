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
package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.Spectrum;
import fr.proline.mzscope.ui.event.ScanHeaderListener;
import fr.proline.studio.utils.IconManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * header panel for the spectrum panel, which contains information (scanIndex,
 * retentionTime, msLevel...) These values can be changed by the user, to allow
 * him/her to move into the plot
 *
 * @author MB243701
 */
public class ScanHeaderPanel extends JPanel {
    
    private static final Logger logger = LoggerFactory.getLogger(ScanHeaderPanel.class);

    private final static DecimalFormat TIME_FORMATTER = new DecimalFormat("0.00");
    private final static DecimalFormat MASS_FORMATTER = new DecimalFormat("0.####");
    
    private JPanel mainPanel;
    private JPanel titlePanel;
    private JLabel titleLabel;
    private JPanel precursorPanel;
    private JLabel precursorLabel;
    private JPanel scanIndexPanel;
    private JLabel scanIndexLabel;
    private JSpinner scansSpinner;
    private SpinnerModel scansSpinnerModel;
    private JPanel retentionTimePanel;
    private JLabel retentionTimeLabel;
    private JTextField retentionTimeTF;
    private JPanel msLevelPanel;
    private JLabel msLevelLabel;
    private JTextField msLevelTF;
    private JToggleButton keepSameMsLevelTB;

    // mzdb fileName
    private String mzdbFileName;
    // current scan information
    private Spectrum scan;
    
    //events
    private EventListenerList listenerList = new EventListenerList();

    public ScanHeaderPanel(Spectrum scan, SpinnerModel model) {
        this.scan = scan;
        this.scansSpinnerModel = model;
        this.
        initComponents();
    }

    private void initComponents() {
        this.setLayout(new BorderLayout());
        this.add(getMainPanel(), BorderLayout.CENTER);
        
        updateScan();
    }
    
    private JPanel getMainPanel() {
       if (this.mainPanel == null) {
            mainPanel = new JPanel();
            mainPanel.setName("mainPanel");
            mainPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
            mainPanel.add(getPanelScanIndex());
            mainPanel.add(getPanelRetentionTime());
            mainPanel.add(getPanelMsLevel());
            mainPanel.add(getPanelPrecursor());
            mainPanel.add(getPanelTitle());
        }
        return this.mainPanel; 
    }

    private JPanel getPanelTitle() {
        if (this.titlePanel == null) {
            titlePanel = new JPanel();
            titlePanel.setName("panelTitle");
            titlePanel.setLayout(new FlowLayout(FlowLayout.LEADING, 2, 0));
            titlePanel.add(getTitleLabel());
        }
        return this.titlePanel;
    }
    
    private JPanel getPanelPrecursor() {
        if (this.precursorPanel == null) {
            precursorPanel = new JPanel();
            precursorPanel.setName("panelPrecursor");
            precursorPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 2, 0));
            precursorPanel.add(getPrecursorLabel());
        }
        return this.precursorPanel;
    }
    
    private JPanel getPanelScanIndex() {
        if (this.scanIndexPanel == null) {
            scanIndexPanel = new JPanel();
            scanIndexPanel.setName("panelScanIndex");
            scanIndexPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 2, 0));
            scanIndexPanel.add(getScanIndexLabel());
            scanIndexPanel.add(getScansSpinner());
        }
        return this.scanIndexPanel;
    }

    private JPanel getPanelRetentionTime() {
        if (this.retentionTimePanel == null) {
            retentionTimePanel = new JPanel();
            retentionTimePanel.setName("panelRetentionTime");
            retentionTimePanel.setLayout(new FlowLayout(FlowLayout.LEADING, 2, 0));
            retentionTimePanel.add(getRetentionTimeLabel());
            retentionTimePanel.add(getTextFieldRetentionTime());
        }
        return this.retentionTimePanel;
    }

    private JPanel getPanelMsLevel() {
        if (this.msLevelPanel == null) {
            msLevelPanel = new JPanel();
            msLevelPanel.setName("panelMsLevel");
            msLevelPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 2, 0));
            msLevelPanel.add(getLabelMsLevel());
            msLevelPanel.add(getTextFieldMsLevel());
            msLevelPanel.add(getKeepSameMsLevelBtn());
        }
        return this.msLevelPanel;
    }

    private JLabel getTitleLabel() {
        if (this.titleLabel == null) {
            titleLabel = new JLabel(mzdbFileName);
            titleLabel.setName("labelTitle");
        }
        return this.titleLabel;
    }
    
    private JLabel getPrecursorLabel() {
        if (this.precursorLabel == null) {
            precursorLabel = new JLabel("");
            precursorLabel.setName("labelPrecursor");
        }
        return this.precursorLabel;
    }
    
    private JLabel getScanIndexLabel() {
        if (this.scanIndexLabel == null) {
            scanIndexLabel = new JLabel("Scan :");
            scanIndexLabel.setName("labelScanIndex");
        }
        return this.scanIndexLabel;
    }

    private JLabel getRetentionTimeLabel() {
        if (this.retentionTimeLabel == null) {
            retentionTimeLabel = new JLabel("rt:");
            retentionTimeLabel.setName("labelRetentionTime");
        }
        return this.retentionTimeLabel;
    }

    private JLabel getLabelMsLevel() {
        if (this.msLevelLabel == null) {
            msLevelLabel = new JLabel("ms:");
            msLevelLabel.setName("labelMsLevel");
        }
        return this.msLevelLabel;
    }

    private JSpinner getScansSpinner() {
        if (this.scansSpinner == null) {
            scansSpinner = new JSpinner(scansSpinnerModel);
            ((DefaultEditor) scansSpinner.getEditor()).getTextField().setEditable(true);
            ((DefaultEditor) scansSpinner.getEditor()).getTextField().setColumns(5);
            scansSpinner.setName("spinnerScanIndex");
        }
        return this.scansSpinner;
    }

    private JTextField getTextFieldRetentionTime() {
        if (this.retentionTimeTF == null) {
            retentionTimeTF = new JTextField();
            retentionTimeTF.setToolTipText("retention time in min");
            retentionTimeTF.setColumns(6);
            retentionTimeTF.setName("textFieldRetentionTime");
            retentionTimeTF.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    retentionTimeActionPerformed(evt);
                }
            });
        }
        return this.retentionTimeTF;
    }

    private JTextField getTextFieldMsLevel() {
        if (this.msLevelTF == null) {
            msLevelTF = new JTextField();
            msLevelTF.setEditable(false);
            msLevelTF.setColumns(3);
            msLevelTF.setName("textFieldMsLevel");
            msLevelTF.setToolTipText("msLevel");
        }
        return this.msLevelTF;
    }
    
    private JToggleButton getKeepSameMsLevelBtn() {
        if (this.keepSameMsLevelTB == null) {
            this.keepSameMsLevelTB = new JToggleButton(){
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(24,24); 
                }
                
            };
            this.keepSameMsLevelTB.setBorderPainted(false);
            this.keepSameMsLevelTB.setIcon(IconManager.getIcon(IconManager.IconType.SAME_MS_LEVEL));
            this.keepSameMsLevelTB.setSelected(true);
            this.keepSameMsLevelTB.setName("cbKeepMsLevel");
            this.keepSameMsLevelTB.setToolTipText("Stay on the same previous MS level while navigating. This can also be done by using the keyboard's arrows (or Ctrl+arrows to keep the same ms level)");
            this.keepSameMsLevelTB.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    msLevelActionPerformed(e);
                }
            });
        }
        return keepSameMsLevelTB;
    }

    private void updateScan() {
        if (scan == null) {
            clearValues();
        } else {
            if (scan.getPrecursorMz() == null) {
                precursorLabel.setText("");
            }else{
                StringBuilder builder = new StringBuilder();
                builder.append(MASS_FORMATTER.format(scan.getPrecursorMz())).append(" (");
                builder.append(scan.getPrecursorCharge()).append("+)");
                precursorLabel.setText(builder.toString());
            }
            retentionTimeTF.setText(TIME_FORMATTER.format(scan.getRetentionTime() / 60.0));
            msLevelTF.setText(Integer.toString(scan.getMsLevel()));
        }
    }

    private void clearValues() {
        titleLabel.setText("");
        precursorLabel.setText("");
        retentionTimeTF.setText("");
        msLevelTF.setText("");
    }

    /**
     * update the scan information
     *
     * @param scan
     */
    public void setScan(Spectrum scan) {
        this.scan = scan;
        updateScan();
    }

    /**
     * set the mzdb fileName
     * @param fileName 
     */
    public void setMzdbFileName(String fileName) {
        this.mzdbFileName = fileName;
        this.titleLabel.setText(fileName);
    }
    /**
     * event register
     * @param listener 
     */
    public void addScanHeaderListener(ScanHeaderListener listener) {
        listenerList.add(ScanHeaderListener.class, listener);
    }

    public void removeScanHeaderListener(ScanHeaderListener listener) {
        listenerList.remove(ScanHeaderListener.class, listener);
    }

    private void fireUpdateScanIndex(Integer scanIndex) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == ScanHeaderListener.class) {
                ((ScanHeaderListener) listeners[i + 1]).updateScanIndex(scanIndex);
            }
        }
    }

    private void fireUpdateRetentionTime(float retentionTime) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == ScanHeaderListener.class) {
                ((ScanHeaderListener) listeners[i + 1]).updateRetentionTime(retentionTime);
            }
        }
    }

    private void fireKeepMsLevel(boolean keepMsLevel) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == ScanHeaderListener.class) {
                ((ScanHeaderListener) listeners[i + 1]).keepMsLevel(keepMsLevel);
            }
        }
    }

    public void retentionTimeActionPerformed(ActionEvent evt) {
        if (scan == null) {
            return;
        }
        // check value
        String value = this.retentionTimeTF.getText();
        if (value == null || value.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "The retention time could not be empty!");
            updateScan();
            return;
        }
        float retentionTimeMin;
        try {
            retentionTimeMin = Float.parseFloat(value);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "The format of the retention time is incorrect!");
            updateScan();
            return;
        }
        // time expressed in min, => sec
        float retentionTime = (float) (retentionTimeMin * 60.0);
        fireUpdateRetentionTime(retentionTime);
    }

    public void scanIndexChanged(ChangeEvent e) {
        if (scan == null) {
            return;
        }
        int scanIndex = ((Integer)scansSpinner.getValue()).intValue();
        fireUpdateScanIndex(scanIndex);
    }
    
    public void msLevelActionPerformed(ActionEvent evt) {
        if (scan == null) {
            return;
        }
        fireKeepMsLevel(this.keepSameMsLevelTB.isSelected());
    }

}
