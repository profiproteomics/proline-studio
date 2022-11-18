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

import fr.proline.mzscope.model.IonMobilityIndex;
import fr.proline.mzscope.model.MobilitySpectrum;
import fr.proline.mzscope.model.Spectrum;
import fr.proline.mzscope.ui.event.ScanHeaderListener;
import fr.proline.studio.utils.IconManager;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import javax.swing.*;
import javax.swing.JSpinner.DefaultEditor;
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

    private JLabel titleLabel;
    private JLabel precursorLabel;
    private JTextField retentionTimeTF;
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
        this.initComponents(model);
    }

    private void initComponents(SpinnerModel model) {
//        this.setBackground(Color.pink);
        GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 0, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new java.awt.Insets(2, 0, 2, 0), 0, 0);
        this.setLayout(new GridBagLayout());
        this.add(getPanelScanIndex(model), c);
        c.gridx++;
        this.add(getPanelRetentionTime(), c);
        c.gridx++;
        c.gridx++;
        this.add(getPanelMsLevel(), c);
        c.gridx++;
        precursorLabel = new JLabel("");
        this.add(precursorLabel, c);
        c.gridx++;
        c.insets = new java.awt.Insets(2, 5, 2, 0);
        titleLabel = new JLabel(mzdbFileName);
        this.add(titleLabel, c);
        updateScan();
    }

    private JPanel getPanelScanIndex(SpinnerModel model) {
        JPanel scanIndexPanel = new JPanel();
        scanIndexPanel.setLayout(new BoxLayout(scanIndexPanel, BoxLayout.LINE_AXIS));
        JLabel scanIndexLabel = new JLabel("scan:");
        scanIndexPanel.add(scanIndexLabel);
        JSpinner scansSpinner = new JSpinner(model);
        ((DefaultEditor) scansSpinner.getEditor()).getTextField().setEditable(true);
        ((DefaultEditor) scansSpinner.getEditor()).getTextField().setColumns(6);
        scanIndexPanel.add(scansSpinner);
        return scanIndexPanel;
    }

    private JPanel getPanelRetentionTime() {
        JPanel retentionTimePanel = new JPanel();
        retentionTimePanel.setLayout(new BoxLayout(retentionTimePanel, BoxLayout.LINE_AXIS));
        JLabel retentionTimeLabel = new JLabel("rt:");
        retentionTimePanel.add(retentionTimeLabel);
        retentionTimeTF = new JTextField();
        retentionTimeTF.setToolTipText("retention time in min");
        retentionTimeTF.setColumns(6);
        retentionTimeTF.addActionListener(evt -> retentionTimeActionPerformed(evt));
        retentionTimePanel.add(retentionTimeTF);
        return retentionTimePanel;
    }

    private JPanel getPanelMsLevel() {
        JPanel msLevelPanel = new JPanel();
        msLevelPanel.setLayout(new BoxLayout(msLevelPanel, BoxLayout.LINE_AXIS));
        JLabel msLevelLabel = new JLabel("ms:");
        msLevelPanel.add(msLevelLabel);
        msLevelTF = new JTextField();
        msLevelTF.setEditable(false);
        msLevelTF.setColumns(3);
        msLevelTF.setToolTipText("msLevel");
        msLevelPanel.add(msLevelTF);
        msLevelPanel.add(getKeepSameMsLevelBtn());
        return msLevelPanel;
    }

    private JToggleButton getKeepSameMsLevelBtn() {
        this.keepSameMsLevelTB = new JToggleButton() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(24, 24);
            }

        };
        this.keepSameMsLevelTB.setBorderPainted(false);
        this.keepSameMsLevelTB.setIcon(IconManager.getIcon(IconManager.IconType.SAME_MS_LEVEL));
        this.keepSameMsLevelTB.setSelected(true);
        this.keepSameMsLevelTB.setToolTipText("Stay on the same previous MS level while navigating. This can also be done by using the keyboard's arrows (or Ctrl+arrows to keep the same ms level)");
        this.keepSameMsLevelTB.addActionListener(e -> msLevelActionPerformed(e));
        return keepSameMsLevelTB;
    }

    private void updateScan() {
        if (scan == null) {
            clearValues();
        } else {


            if (scan.getPrecursorMz() == null) {
                precursorLabel.setText("");
            } else {
                StringBuilder builder = new StringBuilder();
                builder.append(MASS_FORMATTER.format(scan.getPrecursorMz())).append(" (");
                builder.append(scan.getPrecursorCharge()).append("+)");
                precursorLabel.setText(builder.toString());
            }
            retentionTimeTF.setText(TIME_FORMATTER.format(scan.getRetentionTime() / 60.0));
            msLevelTF.setText(Integer.toString(scan.getMsLevel()));
            if(scan.getTitle()!=null && !scan.getTitle().isEmpty()) {
                titleLabel.setText(scan.getTitle());
            }
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

    public void msLevelActionPerformed(ActionEvent evt) {
        if (scan == null) {
            return;
        }
        fireKeepMsLevel(this.keepSameMsLevelTB.isSelected());
    }

}
