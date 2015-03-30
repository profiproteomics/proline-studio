/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.MzScopePreferences;
import fr.proline.mzscope.ui.event.ExtractionListener;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.EventListenerList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The extraction Panel contains the different parameters that could be changed
 * for: the extraction: mass, tolerance 
 * or sum modes
 *
 * @author MB243701
 */
public class XICExtractionPanel extends JPanel {

    private static Logger logger = LoggerFactory.getLogger(XICExtractionPanel.class);


    private JTabbedPane extractionTabbedPane;
    private JScrollPane scrollPane;
    private JPanel internalPanel;
    private JPanel mainPanel;
    private JPanel panelMassRange;
    private JLabel massRangeLabel;
    private JTextField massRangeTF;
    private JPanel panelTolerance;
    private JLabel toleranceLabel;
    private JTextField toleranceTF;

    //events
    private EventListenerList extractionListenerList = new EventListenerList();

    public XICExtractionPanel() {
        initComponents();
        toleranceTF.setText(Float.toString(MzScopePreferences.getInstance().getMzPPMTolerance()));
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        this.add(getExtractionTabbedPane(), BorderLayout.CENTER);
    }

    private JTabbedPane getExtractionTabbedPane() {
        if (extractionTabbedPane == null) {
            extractionTabbedPane = new JTabbedPane();
            extractionTabbedPane.setName("extractionTabbedPane");
            extractionTabbedPane.addTab("Extraction", getScrollPane());
        }
        return extractionTabbedPane;
    }

    private JScrollPane getScrollPane() {
        if (scrollPane == null) {
            scrollPane = new JScrollPane(getInternalPanel());
            scrollPane.setName("scrollPane");
            this.add(scrollPane);
        }
        return scrollPane;
    }
    
    private JPanel getInternalPanel(){
        if (internalPanel == null) {
            internalPanel = new JPanel();
            internalPanel.setName("internalPanel");
            internalPanel.setLayout(new BorderLayout());
            internalPanel.add(getMainPanel(), BorderLayout.PAGE_START);
        }
        return internalPanel;
    }

    private JPanel getMainPanel() {
        if (mainPanel == null) {
            mainPanel = new JPanel(new GridBagLayout());
            mainPanel.setName("mainPanel");
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.BOTH;
            c.insets = new java.awt.Insets(5, 5, 5, 5);
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1.0;
            mainPanel.add(getPanelMass(),c);
            c.gridy++;
            mainPanel.add(getPanelTolerance(),c);
        }
        return mainPanel;
    }



    private JPanel getPanelMass() {
        if (panelMassRange == null) {
            panelMassRange = new JPanel();
            panelMassRange.setName("panelMassRange");
            panelMassRange.setLayout(new FlowLayout(FlowLayout.LEADING));
            panelMassRange.add(getMassRangeLabel());
            panelMassRange.add(getMassRangeTF());
        }
        return panelMassRange;
    }

    private JLabel getMassRangeLabel() {
        if (massRangeLabel == null) {
            massRangeLabel = new JLabel();
            massRangeLabel.setName("massRangeLabel");
            massRangeLabel.setText("Mass range:");
        }
        return massRangeLabel;
    }

    private JTextField getMassRangeTF() {
        if (massRangeTF == null) {
            massRangeTF = new JTextField();
            massRangeTF.setName("massRangeTF");
            massRangeTF.setToolTipText("mass range to extract with the specified tolerance");
            massRangeTF.setColumns(7);
            massRangeTF.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    massRangeTFActionPerformed(evt);
                }
            });
        }
        return massRangeTF;
    }

    private JPanel getPanelTolerance() {
        if (panelTolerance == null) {
            panelTolerance = new JPanel();
            panelTolerance.setName("panelTolerance");
            panelTolerance.setLayout(new FlowLayout(FlowLayout.LEADING));
            panelTolerance.add(getToleranceLabel());
            panelTolerance.add(getToleranceTF());
        }
        return panelTolerance;
    }

    private JLabel getToleranceLabel() {
        if (toleranceLabel == null) {
            toleranceLabel = new JLabel();
            toleranceLabel.setName("toleranceLabel");
            toleranceLabel.setText("Tolerance (ppm):");
        }
        return toleranceLabel;
    }

    private JTextField getToleranceTF() {
        if (toleranceTF == null) {
            toleranceTF = new JTextField();
            toleranceTF.setName("toleranceTF");
            toleranceTF.setColumns(5);
            toleranceTF.setToolTipText("Tolerance in ppm");
            toleranceTF.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    toleranceTFActionPerformed(evt);
                }
            });
        }
        return toleranceTF;
    }


    private void massRangeTFActionPerformed(java.awt.event.ActionEvent evt) {
        String text = massRangeTF.getText().trim();
        double minMz = Double.NaN;
        double maxMz = Double.NaN;
        String[] masses = text.split("-");
        minMz = Double.parseDouble(masses[0]);
        if (masses.length == 1) {
            float ppm = Float.parseFloat(toleranceTF.getText().trim());
            MzScopePreferences.getInstance().setMzPPMTolerance(ppm);
            maxMz = minMz + minMz * ppm / 1e6;
            minMz -= minMz * ppm / 1e6;
        } else {
            maxMz = Double.parseDouble(masses[1]);
        }
//        if (rawFilePlot != null) {
//            rawFilePlot.extractChromatogram(minMz, maxMz);
//        }
        fireExtractChromatogram(minMz, maxMz);
    }

    private void toleranceTFActionPerformed(java.awt.event.ActionEvent evt) {
        float ppm = Float.parseFloat(toleranceTF.getText().trim());
        MzScopePreferences.getInstance().setMzPPMTolerance(ppm);
    }

    /**
     * event register
     *
     * @param listener
     */
    public void addExtractionListener(ExtractionListener listener) {
        extractionListenerList.add(ExtractionListener.class, listener);
    }

    public void removeScanHeaderListener(ExtractionListener listener) {
        extractionListenerList.remove(ExtractionListener.class, listener);
    }

    private void fireExtractChromatogram(double minMz, double maxMz) {
        Object[] listeners = extractionListenerList.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == ExtractionListener.class) {
                ((ExtractionListener) listeners[i + 1]).extractChromatogram(minMz, maxMz);
            }
        }
    }
    
}
