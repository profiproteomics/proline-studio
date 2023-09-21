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
import javax.swing.*;

import org.jdesktop.swingx.JXComboBox;

/**
 *
 * @author VD225637
 */
public class SmoothingParamDialog extends DefaultDialog {

    JTextField m_nbrPoint;
    JTextField ratioPeak;
    JSpinner sp;
    JComboBox m_smoothingMethods;

    JCheckBox checkBox;
    public final static String PARTIAL_SG_SMOOTHER = "Partial Savitzky-Golay Smoother";
    public final static String SG_SMOOTHER = "Savitzky-Golay Smoother";
    public final static String BOTH_SMOOTHER = "All Smoothers";

    public final static String SMOOTHING_JDSP = "SmoothingJDSP";

    public final static String CONVOLUTION_METHOD = "ConvolutionJDSP";

    public final static String GAUSSIAN_FITTING_METHOD = "Apache gaussian fitting";

    public final static String WIENER_METHOD = "WienerJDSP";

    public final static String SVGOLAY2_METHOD = "SavitskyJDSP";

    public final static String EXPERIMENTAL_TEST = "Polynomial fitting";

    public final static String MEDIAN_FILTER_METHOD = "medianFilterJDSP";

    public final static String SIGNAL_QUALITY_EVALUATION = "signal quality evaluation";
    String[] methods = {PARTIAL_SG_SMOOTHER, SG_SMOOTHER, BOTH_SMOOTHER, SMOOTHING_JDSP, CONVOLUTION_METHOD,
            GAUSSIAN_FITTING_METHOD, WIENER_METHOD, SVGOLAY2_METHOD, EXPERIMENTAL_TEST, MEDIAN_FILTER_METHOD, SIGNAL_QUALITY_EVALUATION};


    public SmoothingParamDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
        setTitle("Smoothing parameters ");
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
        m_smoothingMethods.setSelectedItem(SIGNAL_QUALITY_EVALUATION);
        internalPanel.add(m_smoothingMethods, c);
        c.gridx=0;
        c.gridy++;
        JLabel peakRestoreLabel=new JLabel("peak restore:  ");
        internalPanel.add(peakRestoreLabel,c);
        checkBox = new JCheckBox("Use peak restore");
        checkBox.setSelected(false);
        checkBox.addActionListener(e -> {
            boolean isSelected = checkBox.isSelected();
            sp.setEnabled(isSelected);
        });
        c.gridx++;
        internalPanel.add(checkBox,c);
        JLabel ratioLabel=new JLabel("Peak ratio");
        ratioLabel.setToolTipText("a ratio of zero will restore all peaks, a ratio of 1 will restore only the peak with" +
                "max value");
        c.gridx=0;
        c.gridy++;
        internalPanel.add(ratioLabel,c);

        //ratioPeak= new JTextField(5);
        SpinnerModel model = new SpinnerNumberModel(
                0.5, //valeur initiale
                0,
                1,
                0.05
        );
        sp = new JSpinner(model);
        sp.setEnabled(false);
        c.gridx++;

        internalPanel.add(sp,c);
        return internalPanel;
    }

    public int getNbrPoint() {
        return Integer.valueOf(m_nbrPoint.getText());
    }

    public String getMethod() {
        return m_smoothingMethods.getSelectedItem().toString();
    }

    public boolean getUsePeakrestore(){
        return checkBox.isSelected();
    }
    public double getRatioPeak(){
        return Double.valueOf(ratioPeak.getText());
    }
    public double getJSpinnerValue(){
        return (double) sp.getValue();
    }



}
