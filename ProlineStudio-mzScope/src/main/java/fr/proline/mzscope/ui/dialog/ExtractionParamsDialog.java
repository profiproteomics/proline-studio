package fr.proline.mzscope.ui.dialog;

import fr.proline.mzscope.model.ExtractionParams;
import fr.proline.mzscope.model.MzScopePreferences;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

/**
 * extraction parameters dialog (peakels or features)
 *
 * @author CB205360
 */
public class ExtractionParamsDialog extends JDialog {

    // extraction parameters

    private ExtractionParams extractionParams;

    private JScrollPane scrollPane;
    private JPanel mainPanel;
    private JPanel panelBounds;
    private JPanel panelButton;
    private JButton cancelBtn;
    private JButton okBtn;
    private JPanel panelTolerance;
    private JLabel toleranceLabel;
    private JTextField toleranceTF;
    private JPanel panelNoBounds;
    private JRadioButton noBoundsRB;
    private JPanel panelMassBounds;
    private JRadioButton mzBoundsRB;
    private JTextField maxMzTF;
    private JTextField minMzTF;
    private JLabel labelMinMz;
    private JLabel labelMaxMz;
    private JPanel panelMass;
    private JRadioButton massRB;
    private JTextField mzTF;

    /**
     * Creates new form ExtractionParamsDialog
     *
     * @param parent
     * @param modal
     */
    public ExtractionParamsDialog(Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        toleranceTF.setText(Float.toString(MzScopePreferences.getInstance().getMzPPMTolerance()));
        getRootPane().setDefaultButton(okBtn);
        pack();
    }

    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        this.add(getScrollPane(), BorderLayout.CENTER);
        ButtonGroup group = new ButtonGroup();
        group.add(noBoundsRB);
        group.add(mzBoundsRB);
        group.add(massRB);
    }

    private JScrollPane getScrollPane() {
        if (scrollPane == null) {
            scrollPane = new JScrollPane();
            scrollPane.setName("scrollPane");
            scrollPane.setViewportView(getMainPanel());
            scrollPane.createVerticalScrollBar();
        }
        return scrollPane;
    }

    private JPanel getMainPanel() {
        if (mainPanel == null) {
            mainPanel = new JPanel();
            mainPanel.setName("mainPanel");
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.add(getTolerancePanel());
            mainPanel.add(getPanelBounds());
            mainPanel.add(getButtonPanel());
        }
        return mainPanel;
    }

    private JPanel getPanelBounds() {
        if (panelBounds == null) {
            panelBounds = new JPanel();
            panelBounds.setName("panelBounds");
            panelBounds.setLayout(new BoxLayout(panelBounds, BoxLayout.Y_AXIS));
            panelBounds.add(getNoBoundsPanel());
            panelBounds.add(getMassBoundsPanel());
            panelBounds.add(getMassPanel());
        }
        return panelBounds;
    }

    private JPanel getTolerancePanel() {
        if (panelTolerance == null) {
            panelTolerance = new JPanel();
            panelTolerance.setName("panelTolerance");
            panelTolerance.setLayout(new FlowLayout(FlowLayout.LEFT));
            panelTolerance.add(getToleranceLabel());
            panelTolerance.add(getToleranceTF());
        }
        return panelTolerance;
    }

    private JLabel getToleranceLabel() {
        if (toleranceLabel == null) {
            toleranceLabel = new JLabel();
            toleranceLabel.setName("toleranceLabel");
            toleranceLabel.setText("m/z tolerance (ppm):");
        }
        return toleranceLabel;
    }

    private JTextField getToleranceTF() {
        if (toleranceTF == null) {
            toleranceTF = new JTextField();
            toleranceTF.setName("toleranceTF");
            toleranceTF.setColumns(5);
            toleranceTF.setToolTipText("Tolerance in ppm");
            toleranceTF.setText("5.0");
        }
        return toleranceTF;
    }

    private JPanel getNoBoundsPanel() {
        if (panelNoBounds == null) {
            panelNoBounds = new JPanel();
            panelNoBounds.setName("panelNoBounds");
            panelNoBounds.setLayout(new FlowLayout(FlowLayout.LEFT));
            panelNoBounds.add(getNoBoundsRB());
        }
        return panelNoBounds;
    }

    private JRadioButton getNoBoundsRB() {
        if (noBoundsRB == null) {
            noBoundsRB = new JRadioButton("No m/z bounds");
            noBoundsRB.setName("noBoundsRB");
            noBoundsRB.setSelected(true);
            noBoundsRB.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setMassBoundsEnabled(false);
                    setMassEnabled(false);
                }
            });
        }
        return noBoundsRB;
    }

    private JPanel getMassBoundsPanel() {
        if (panelMassBounds == null) {
            panelMassBounds = new JPanel();
            panelMassBounds.setName("panelMassBounds");
            panelMassBounds.setLayout(new BoxLayout(panelMassBounds, BoxLayout.Y_AXIS));
            JPanel prb = new JPanel();
            prb.setLayout(new FlowLayout(FlowLayout.LEFT));
            prb.add(getMassBoundsRB());
            panelMassBounds.add(prb);
            JPanel pmin = new JPanel();
            pmin.setLayout(new FlowLayout(FlowLayout.LEFT));
            pmin.add(getLabelMinMz());
            pmin.add(getMinMzTF());
            panelMassBounds.add(pmin);
            JPanel pmax = new JPanel();
            pmax.setLayout(new FlowLayout(FlowLayout.LEFT));
            pmax.add(getLabelMaxMz());
            pmax.add(getMaxMzTF());
            panelMassBounds.add(pmax);

        }
        return panelMassBounds;
    }

    private JRadioButton getMassBoundsRB() {
        if (mzBoundsRB == null) {
            mzBoundsRB = new JRadioButton("Enable m/z bounds");
            mzBoundsRB.setName("mzBoundsRB");
            mzBoundsRB.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setMassBoundsEnabled(true);
                    setMassEnabled(false);
                }
            });
        }
        return mzBoundsRB;
    }

    private JLabel getLabelMinMz() {
        if (labelMinMz == null) {
            labelMinMz = new JLabel();
            labelMinMz.setName("labelMinMz");
            labelMinMz.setText("minimum m/z:");
        }
        return labelMinMz;
    }

    private JLabel getLabelMaxMz() {
        if (labelMaxMz == null) {
            labelMaxMz = new JLabel();
            labelMaxMz.setName("labelMaxMz");
            labelMaxMz.setText("maximum m/z:");
        }
        return labelMaxMz;
    }

    private JTextField getMinMzTF() {
        if (minMzTF == null) {
            minMzTF = new JTextField();
            minMzTF.setName("minMzTF");
            minMzTF.setText("0.0");
            minMzTF.setColumns(5);
            minMzTF.setEnabled(false);
            minMzTF.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent evt) {
                    minMzTFFocusGained(evt);
                }
            });
        }
        return minMzTF;
    }

    private JTextField getMaxMzTF() {
        if (maxMzTF == null) {
            maxMzTF = new JTextField();
            maxMzTF.setName("maxMzTF");
            maxMzTF.setText("0.0");
            maxMzTF.setColumns(5);
            maxMzTF.setEnabled(false);
            maxMzTF.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent evt) {
                    maxMzTFFocusGained(evt);
                }
            });
        }
        return maxMzTF;
    }

    private JPanel getMassPanel() {
        if (panelMass == null) {
            panelMass = new JPanel();
            panelMass.setName("panelMass");
            panelMass.setLayout(new FlowLayout(FlowLayout.LEFT));
            panelMass.add(getMassRB());
            panelMass.add(getMzTF());

        }
        return panelMass;
    }

    private JTextField getMzTF() {
        if (mzTF == null) {
            mzTF = new JTextField();
            mzTF.setName("mzTF");
            mzTF.setText("0.0");
            mzTF.setColumns(5);
            mzTF.setEnabled(false);
            mzTF.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent evt) {
                    mzTFFocusGained(evt);
                }
            });
        }
        return mzTF;
    }

    private JRadioButton getMassRB() {
        if (massRB == null) {
            massRB = new JRadioButton("Detect at m/z:");
            massRB.setName("massRB");
            massRB.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setMassBoundsEnabled(false);
                    setMassEnabled(true);
                }
            });
        }
        return massRB;
    }

    private JPanel getButtonPanel() {
        if (panelButton == null) {
            panelButton = new JPanel();
            panelButton.setName("panelButton");
            panelButton.setLayout(new FlowLayout(FlowLayout.CENTER));
            panelButton.add(getOkButton());
            panelButton.add(getCancelButton());

        }
        return panelButton;
    }

    private JButton getCancelButton() {
        if (cancelBtn == null) {
            cancelBtn = new JButton();
            cancelBtn.setName("cancelBtn");
            cancelBtn.setText("Cancel");
            cancelBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    cancelBtnActionPerformed(evt);
                }
            });
        }
        return cancelBtn;
    }

    private JButton getOkButton() {
        if (okBtn == null) {
            okBtn = new JButton();
            okBtn.setName("okBtn");
            okBtn.setText("Ok");
            okBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    okBtnActionPerformed(evt);
                }
            });
        }
        return okBtn;
    }

    public void setExtractionParamsTitle(String title) {
        this.setTitle(title);
    }

    public ExtractionParams getExtractionParams() {
        return extractionParams;
    }

    public void showExtractionParamsDialog() {
        setVisible(true);
    }

    private void setMassBoundsEnabled(boolean enabled) {
        getMinMzTF().setEnabled(enabled);
        getMaxMzTF().setEnabled(enabled);
        if (enabled) {
            minMzTF.requestFocusInWindow();
        }
    }

    private void setMassEnabled(boolean enabled) {
        getMzTF().setEnabled(enabled);
        if (enabled) {
            mzTF.requestFocusInWindow();
        }
    }

    private void okBtnActionPerformed(ActionEvent evt) {
        this.extractionParams = new ExtractionParams();
        try {
            extractionParams.mzTolPPM = (Float.parseFloat(toleranceTF.getText()));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "tolerance value is incorrect: " + toleranceTF.getText());
            return;
        }
        try {
            extractionParams.minMz = ((mzBoundsRB.isSelected()) ? Double.parseDouble(minMzTF.getText()) : 0.0);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "min m/z value is incorrect: " + minMzTF.getText());
            return;
        }
        try {
            extractionParams.maxMz = ((mzBoundsRB.isSelected()) ? Double.parseDouble(maxMzTF.getText()) : 0.0);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "max m/z value is incorrect: " + maxMzTF.getText());
            return;
        }
        if (mzBoundsRB.isSelected() && extractionParams.minMz > extractionParams.maxMz) {
            JOptionPane.showMessageDialog(this, "The min m/z value must be lower than max m/z");
            return;
        }
        if (massRB.isSelected()) {
            try {
                double m = Double.parseDouble(mzTF.getText());
                extractionParams.minMz = m;
                extractionParams.maxMz = m + m * extractionParams.mzTolPPM / 1e6;
                extractionParams.minMz -= m * extractionParams.mzTolPPM / 1e6;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "m/z value is incorrect: " + mzTF.getText());
                return;
            }
        }

        setVisible(false);
    }

    private void cancelBtnActionPerformed(ActionEvent evt) {
        this.extractionParams = null;
        setVisible(false);
    }

    private void minMzTFFocusGained(FocusEvent evt) {
        minMzTF.selectAll();
    }

    private void maxMzTFFocusGained(FocusEvent evt) {
        maxMzTF.selectAll();
    }

    private void mzTFFocusGained(FocusEvent evt) {
        mzTF.selectAll();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(ExtractionParamsDialog.class.getName()).log(Level.SEVERE, null, ex);
        }

        /* Create and display the dialog */
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                ExtractionParamsDialog dialog = new ExtractionParamsDialog(new JFrame(), true);
                dialog.setExtractionParamsTitle("Extraction Parameters");
                dialog.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                });
                //dialog.setSize(300, 300);
                dialog.setVisible(true);
            }
        });
    }

}
