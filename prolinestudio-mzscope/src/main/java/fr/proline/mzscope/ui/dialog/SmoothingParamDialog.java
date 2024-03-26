/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.ui.dialog;

import fr.proline.studio.gui.DefaultDialog;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Objects;
import javax.swing.*;

import org.jdesktop.swingx.JXComboBox;

/**
 * @author VD225637
 */
public class SmoothingParamDialog extends DefaultDialog {

    JTextField m_nbrPoint;
    JLabel nbrPointLabel;

    JSpinner sp;
    JComboBox m_smoothingMethods;

    JLabel ratioLabel;

    JCheckBox checkBox;

    JLabel convolutionModeJLabel;
    JComboBox modeOfConvolution;

    JComboBox comboBox;
    JPanel savitskyParamsPanel;
    JTextField polyOrderTField;
    JTextField derivativeJTField;
    JTextField deltaJTField;

    public final static String PARTIAL_SG_SMOOTHER = "Partial Savitzky-Golay Smoother";
    public final static String SG_SMOOTHER = "Savitzky-Golay Smoother";
    public final static String BOTH_SMOOTHER = "All Smoothers";

    public final static String SMOOTHING_JDSP = "SmoothingJDSP";

    public final static String CONVOLUTION_METHOD = "ConvolutionJDSP";

    public final static String GAUSSIAN_FITTING_METHOD = "Apache gaussian fitting";

    public final static String WIENER_METHOD = "WienerJDSP";

    public final static String SVGOLAY_JDSP = "SavitskyJDSP";

    public final static String EXPERIMENTAL_TEST = "Experimental method";

    public final static String MEDIAN_FILTER_METHOD = "medianFilterJDSP";

    public final static String SIGNAL_QUALITY_EVALUATION = "signal quality evaluation";
    String[] methods = {PARTIAL_SG_SMOOTHER, SG_SMOOTHER, BOTH_SMOOTHER, SMOOTHING_JDSP, CONVOLUTION_METHOD,
            GAUSSIAN_FITTING_METHOD, WIENER_METHOD, SVGOLAY_JDSP, EXPERIMENTAL_TEST, MEDIAN_FILTER_METHOD, SIGNAL_QUALITY_EVALUATION};
    String[] convolutionMode = {"rectangular", "triangular"};

    private boolean showSavitskyParam = false;


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
        nbrPointLabel = new JLabel("Number of points ");
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
        m_smoothingMethods.setSelectedItem(SVGOLAY_JDSP);

        m_smoothingMethods.addActionListener(e -> {

                    String selectedMethod = Objects.requireNonNull(m_smoothingMethods.getSelectedItem()).toString();
                    if (selectedMethod.equals(SIGNAL_QUALITY_EVALUATION)) {

                       m_nbrPoint.setVisible(true);
                        m_nbrPoint.setEnabled(true);
                        nbrPointLabel.setText("Size of kernel");
                        nbrPointLabel.setToolTipText("Choose the size of kernel to smooth the signal");
                        checkBox.setVisible(false);
                        sp.setVisible(false);
                        ratioLabel.setVisible(false);
                        convolutionModeJLabel.setVisible(false);
                        modeOfConvolution.setVisible(false);
                        savitskyParamsPanel.setVisible(false);

                    } else if (selectedMethod.equals(GAUSSIAN_FITTING_METHOD)) {
                        nbrPointLabel.setVisible(true);
                        m_nbrPoint.setVisible(true);
                        m_nbrPoint.setEnabled(false);
                        checkBox.setText("improve fitting");
                        checkBox.setSelected(false);
                        checkBox.setVisible(true);
                        sp.setVisible(false);
                        sp.setEnabled(false);
                        ratioLabel.setVisible(false);
                        convolutionModeJLabel.setVisible(false);
                        modeOfConvolution.setVisible(false);
                        savitskyParamsPanel.setVisible(false);

                    } else if (selectedMethod.equals(SMOOTHING_JDSP)) {

                        nbrPointLabel.setText("Size of kernel");
                        m_nbrPoint.setEnabled(true);
                        m_nbrPoint.setVisible(true);
                        checkBox.setText("Compute optimal kernel size");
                        checkBox.setToolTipText("if selected calculate an other kernel that fits better the signal");
                        sp.setVisible(false);
                        ratioLabel.setVisible(false);
                        convolutionModeJLabel.setVisible(true);
                        modeOfConvolution.setVisible(true);
                        savitskyParamsPanel.setVisible(false);

                    } else if (selectedMethod.equals(WIENER_METHOD)) {
                        nbrPointLabel.setVisible(true);
                        m_nbrPoint.setVisible(true);
                        m_nbrPoint.setEnabled(true);
                        nbrPointLabel.setText("Filter window size");
                        ratioLabel.setVisible(true);
                        sp.setVisible(true);
                        convolutionModeJLabel.setVisible(false);
                        modeOfConvolution.setVisible(false);
                        savitskyParamsPanel.setVisible(false);

                    } else if (selectedMethod.equals(EXPERIMENTAL_TEST)) {
                        nbrPointLabel.setVisible(true);
                        nbrPointLabel.setText("non defined entry");
                        m_nbrPoint.setVisible(true);
                        m_nbrPoint.setEnabled(true);
                        checkBox.setText("non defined");
                        checkBox.setVisible(true);
                        ratioLabel.setVisible(true);
                        sp.setVisible(true);
                        convolutionModeJLabel.setVisible(false);
                        modeOfConvolution.setVisible(false);
                        savitskyParamsPanel.setVisible(false);

                    } else {


                        if (selectedMethod.equals(SVGOLAY_JDSP)) {
                            savitskyParamsPanel.setVisible(true);

                        } else {
                            savitskyParamsPanel.setVisible(false);
                        }

                        nbrPointLabel.setVisible(true);
                        nbrPointLabel.setText("Window size");
                        m_nbrPoint.setVisible(true);
                        m_nbrPoint.setEnabled(true);
                        checkBox.setText("Use peak restore");
                        checkBox.setToolTipText("If selected will restore peaks");
                        checkBox.setVisible(true);
                        ratioLabel.setVisible(true);
                        convolutionModeJLabel.setVisible(false);
                        modeOfConvolution.setVisible(false);
                        sp.setVisible(true);

                    }

                }
        );
        internalPanel.add(m_smoothingMethods, c);
        c.gridx = 0;
        c.gridy++;
        JLabel peakRestoreLabel = new JLabel();
        internalPanel.add(peakRestoreLabel, c);

        checkBox = new JCheckBox("Use peak restore: ");
        checkBox.setHorizontalTextPosition(SwingConstants.LEFT);
        checkBox.setSelected(false);
        checkBox.addActionListener(e -> {
            boolean isSelected = checkBox.isSelected();
            sp.setEnabled(isSelected);

            if (m_smoothingMethods.getSelectedItem().equals(SMOOTHING_JDSP)) {
                if (isSelected) {
                    sp.setEnabled(false);
                    m_nbrPoint.setEnabled(false);
                } else {
                    nbrPointLabel.setVisible(true);
                    m_nbrPoint.setVisible(true);
                    m_nbrPoint.setEnabled(true);
                }
            }
        });
        c.gridx++;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.CENTER;
        internalPanel.add(checkBox, c);
        c.gridwidth = 1;
        c.weightx = 1;
        ratioLabel = new JLabel("Peak ratio");
        ratioLabel.setToolTipText("a ratio of zero will restore all peaks, a ratio of 1 will restore only the peak with" +
                "max value");
        c.gridx = 0;
        c.gridy++;
        internalPanel.add(ratioLabel, c);

        SpinnerModel model = new SpinnerNumberModel(
                0.1,
                0,
                1,
                0.05
        );
        sp = new JSpinner(model);
        sp.setEnabled(false);
        c.gridx++;
        c.fill = GridBagConstraints.HORIZONTAL;

        internalPanel.add(sp, c);
        c.gridx = 0;
        c.gridy++;
        convolutionModeJLabel = new JLabel("Mode of convolution");
        convolutionModeJLabel.setVisible(false);
        c.fill = GridBagConstraints.NONE;
        internalPanel.add(convolutionModeJLabel, c);
        c.gridx++;
        modeOfConvolution = new JComboBox<>(convolutionMode);
        modeOfConvolution.setVisible(false);
        modeOfConvolution.setSelectedItem(convolutionMode[1]);
        internalPanel.add(modeOfConvolution, c);
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        c.gridheight = 3;
        c.fill = GridBagConstraints.BOTH;
        savitskyParamsPanel = createSavitskyJPanel();
        internalPanel.add(savitskyParamsPanel, c);
        internalPanel.setPreferredSize(new Dimension(300,270));

        return internalPanel;
    }

    private JPanel createSavitskyJPanel() {
        JPanel savistkyPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new java.awt.Insets(5, 5, 5, 5);
        savistkyPanel.setBorder(BorderFactory.createTitledBorder("Savitsky smoothing"));
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;

        String[] modes = {"wrap", "nearest", "constant", "mirror"};
        comboBox = new JComboBox<>(modes);
        comboBox.setSelectedItem("nearest");
        savistkyPanel.add(comboBox, gbc);
        JLabel polyOrderLabel = new JLabel("poly order: ");
        gbc.gridx++;
        gbc.anchor = GridBagConstraints.EAST;
        savistkyPanel.add(polyOrderLabel);
        polyOrderTField = new JTextField(8);
        polyOrderTField.setText(String.valueOf(3));
        gbc.gridx++;
        savistkyPanel.add(polyOrderTField, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel derivJLabel = new JLabel("derivative order: ");
        gbc.anchor = GridBagConstraints.WEST;
        savistkyPanel.add(derivJLabel, gbc);
        derivativeJTField = new JTextField(8);
        derivativeJTField.setText(String.valueOf(0));
        gbc.gridx++;
        gbc.anchor = GridBagConstraints.EAST;
        savistkyPanel.add(derivativeJTField, gbc);
        JLabel deltaSpacing = new JLabel("delta spacing :");
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.WEST;
        savistkyPanel.add(deltaSpacing, gbc);
        deltaJTField = new JTextField(8);
        deltaJTField.setText(String.valueOf(5));
        gbc.gridx++;
        gbc.anchor = GridBagConstraints.EAST;
        savistkyPanel.add(deltaJTField, gbc);

        savistkyPanel.setVisible(true);

        return savistkyPanel;

    }

    public int getNbrPoint() {
        return Integer.parseInt(m_nbrPoint.getText());
    }

    public String getMethod() {
        return m_smoothingMethods.getSelectedItem().toString();
    }

    public String getModeOfConvolution() {
        return modeOfConvolution.getSelectedItem().toString();
    }

    public boolean getUsePeakrestore() {
        return checkBox.isSelected();
    }

    public double getJSpinnerValue() {
        return (double) sp.getValue();
    }

    public String getSavitskyMode() {
        return comboBox.getSelectedItem().toString();

    }

    public int getPolyOrder() {
        return Integer.parseInt(polyOrderTField.getText());
    }

    public int getDerivativeOrder() {
        return Integer.parseInt(derivativeJTField.getText());
    }

    public int getDeltaSpacing() {
        return Integer.parseInt(deltaJTField.getText());
    }


}
