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
package fr.proline.mzscope.ui.dialog;

import fr.proline.mzscope.model.FeaturesExtractionRequest;
import fr.proline.mzscope.ui.model.MzScopePreferences;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.TitledBorder;

/**
 * extraction parameters dialog (peakels or features)
 *
 * @author CB205360
 */
public class ExtractionParamsDialog extends JDialog {

    private static final String MS2 = "MS2";
    private static final String MS1 = "MS1";

    // extraction parameters
    private FeaturesExtractionRequest.Builder extractionParams;
    private boolean showMS2Option;


    private JScrollPane scrollPane;
    private JPanel mainPanel;
    private JPanel mzBoundsCardPanel;
    private MS1MzBoundsPanel ms1MzBoundsPanel;
    private MS2MzBoundsPanel ms2MzBoundsPanel;
    private JComboBox msLevelCB;
    private JCheckBox removeBaselineCB;
    private JCheckBox useSmoothingCB;
    private JTextField intensityPercentileTF;
    private JTextField minPeaksCountTF;
    private JTextField minMaxDistanceTF;
    private JTextField minMaxRatioTF;
    private JTextField maxConsecutiveGapsTF;


    public ExtractionParamsDialog(Frame parent, boolean modal) {
        this(parent, modal, true);
    }
    /**
     * Creates new form ExtractionParamsDialog
     *
     * @param parent
     * @param modal
     */
    public ExtractionParamsDialog(Frame parent, boolean modal, boolean showMS2Option) {
        super(parent, modal);
        this.showMS2Option = showMS2Option;
        initComponents();
        ms1MzBoundsPanel.getToleranceTF().setText(Float.toString(MzScopePreferences.getInstance().getMzPPMTolerance()));
        removeBaselineCB.setSelected(FeaturesExtractionRequest.REMOVE_BASELINE);
        useSmoothingCB.setSelected(FeaturesExtractionRequest.USE_SMOOTHING);
        intensityPercentileTF.setText(Float.toString(FeaturesExtractionRequest.INTENSITY_PERCENTILE));
        minMaxRatioTF.setText(Float.toString(FeaturesExtractionRequest.MIN_MAX_RATIO));
        minMaxDistanceTF.setText(Integer.toString(FeaturesExtractionRequest.MIN_MAX_DISTANCE));
        minPeaksCountTF.setText(Integer.toString(FeaturesExtractionRequest.MIN_PEAKS_COUNT));
        maxConsecutiveGapsTF.setText(Integer.toString(FeaturesExtractionRequest.MAX_CONSECUTIVE_GAPS));
        pack();
    }

    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        this.add(getScrollPane(), BorderLayout.CENTER);
    }

    private JScrollPane getScrollPane() {
        if (scrollPane == null) {
            scrollPane = new JScrollPane();
            scrollPane.setViewportView(getMainPanel());
            scrollPane.createVerticalScrollBar();
        }
        return scrollPane;
    }
    
  private JPanel getMainPanel() {
        if (mainPanel == null) {
            mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            if (showMS2Option)
                mainPanel.add(getMsLevelPanel());
            mainPanel.add(getConfigurationPanel());
            mainPanel.add(getMzBoundsPanel());
            mainPanel.add(getButtonPanel());
        }
        return mainPanel;
    }

    private JPanel getMsLevelPanel() {
        JPanel msLevelPanel = new JPanel();
        msLevelPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
        JLabel label = new JLabel();
        label.setText("MS level: ");
        msLevelPanel.add(label);
        msLevelPanel.add(getMsLevelCB());
        return msLevelPanel;
    }

    private JComboBox getMsLevelCB(){
        if (msLevelCB == null) {
            String[] items = {MS1, MS2};
            msLevelCB = new JComboBox(items);
            msLevelCB.addItemListener(e -> {
                CardLayout layout = (CardLayout) mzBoundsCardPanel.getLayout();
                layout.show(mzBoundsCardPanel, (String)e.getItem());
            });
        }
        return msLevelCB;
    }

    private JPanel getMzBoundsPanel() {
        if (mzBoundsCardPanel == null) {
            mzBoundsCardPanel = new JPanel();
            mzBoundsCardPanel.setLayout(new CardLayout());
            ms1MzBoundsPanel = new MS1MzBoundsPanel("Precursor mz");
            mzBoundsCardPanel.add(MS1, ms1MzBoundsPanel);
            ms2MzBoundsPanel = new MS2MzBoundsPanel();
            mzBoundsCardPanel.add(MS2, ms2MzBoundsPanel);
        }
        return mzBoundsCardPanel;
    }

    private JPanel getConfigurationPanel() {
        JPanel configurationPanel = new JPanel();
        configurationPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.WEST;

        JLabel label = new JLabel("Intensity percentile:");
        configurationPanel.add(label, c);
        c.gridx++;
        intensityPercentileTF = new JTextField(10);
        configurationPanel.add(intensityPercentileTF, c);

        c.gridx = 0;
        c.gridy++;
        label = new JLabel("min peaks count:");
        configurationPanel.add(label, c);
        c.gridx++;
        minPeaksCountTF = new JTextField(10);
        configurationPanel.add(minPeaksCountTF, c);

        c.gridx = 0;
        c.gridy++;
        label = new JLabel("max consecutive gaps:");
        configurationPanel.add(label, c);
        c.gridx++;
        maxConsecutiveGapsTF = new JTextField(10);
        configurationPanel.add(maxConsecutiveGapsTF, c);

        c.gridx = 0;
        c.gridy++;
        label = new JLabel("min/max distance (count):");
        configurationPanel.add(label, c);
        c.gridx++;
        minMaxDistanceTF = new JTextField(10);
        configurationPanel.add(minMaxDistanceTF, c);

        c.gridx = 0;
        c.gridy++;
        label = new JLabel("min/max ratio (0-1):");
        configurationPanel.add(label, c);
        c.gridx++;
        minMaxRatioTF = new JTextField(10);
        configurationPanel.add(minMaxRatioTF, c);

        c.gridx = 0;
        c.gridy++;
        removeBaselineCB = new JCheckBox();
        removeBaselineCB.setToolTipText("Remove peakels baseline during peakel detection");
        removeBaselineCB.setText("remove baseline");
        configurationPanel.add(removeBaselineCB, c);

        c.gridx++;
        useSmoothingCB = new JCheckBox();
        useSmoothingCB.setToolTipText("Use smoothing to find peakel local min/min");
        useSmoothingCB.setText("smooth peakels");
        configurationPanel.add(useSmoothingCB, c);


        configurationPanel.setBorder(new TitledBorder("Processing Parameters"));
        return configurationPanel;
    }


    private JPanel getButtonPanel() {
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        JButton okBtn = new JButton();
        okBtn.setText("Ok");
        okBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                okBtnActionPerformed(evt);
            }
        });
        getRootPane().setDefaultButton(okBtn);
        buttonsPanel.add(okBtn);

        JButton cancelBtn = new JButton();
        cancelBtn.setText("Cancel");
        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });

        buttonsPanel.add(cancelBtn);

        return buttonsPanel;
    }

    public void setExtractionParamsTitle(String title) {
        this.setTitle(title);
    }

    public FeaturesExtractionRequest.Builder getExtractionParams() {
        return extractionParams;
    }

    public void showExtractionParamsDialog() {
        setVisible(true);
    }

    private void okBtnActionPerformed(ActionEvent evt) {
        this.extractionParams = FeaturesExtractionRequest.builder();

        if (!showMS2Option || getMsLevelCB().getSelectedItem().equals(MS1)) {

            try {
                extractionParams.setMzTolPPM(Float.parseFloat(ms1MzBoundsPanel.getToleranceTF().getText()));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "tolerance value is incorrect: " + ms1MzBoundsPanel.getToleranceTF().getText());
                return;
            }
            try {
                extractionParams.setIntensityPercentile(Float.parseFloat(intensityPercentileTF.getText()));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Intensity percentile value is incorrect: " + intensityPercentileTF.getText());
                return;
            }
            try {
                extractionParams.setMinPeaksCount(Integer.parseInt(minPeaksCountTF.getText()));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Min peaks count value is incorrect: " + minPeaksCountTF.getText());
                return;
            }
            try {
                extractionParams.setMaxConsecutiveGaps(Integer.parseInt(maxConsecutiveGapsTF.getText()));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Max consecutive gaps value is incorrect: " + maxConsecutiveGapsTF.getText());
                return;
            }
            try {
                extractionParams.setMinmaxDistanceThreshold(Integer.parseInt(minMaxDistanceTF.getText()));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Min/max distance value is incorrect: " + minMaxDistanceTF.getText());
                return;
            }
            try {
                extractionParams.setMaxIntensityRelativeThreshold(Float.parseFloat(minMaxRatioTF.getText()));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Min/max relative intensities ratio value is incorrect: " + minMaxRatioTF.getText());
                return;
            }
            try {
                extractionParams.setMinMz((ms1MzBoundsPanel.getMzBoundsRB().isSelected()) ? Double.parseDouble(ms1MzBoundsPanel.getMinMzTF().getText()) : 0.0);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "min m/z value is incorrect: " + ms1MzBoundsPanel.getMinMzTF().getText());
                return;
            }
            try {
                extractionParams.setMaxMz((ms1MzBoundsPanel.getMzBoundsRB().isSelected()) ? Double.parseDouble(ms1MzBoundsPanel.getMaxMzTF().getText()) : 0.0);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "max m/z value is incorrect: " + ms1MzBoundsPanel.getMaxMzTF().getText());
                return;
            }
            if (ms1MzBoundsPanel.getMzBoundsRB().isSelected() && extractionParams.getMinMz() > extractionParams.getMaxMz()) {
                JOptionPane.showMessageDialog(this, "The min m/z value must be lower than max m/z");
                return;
            }
            if (ms1MzBoundsPanel.getMzRB().isSelected()) {
                try {
                    double m = Double.parseDouble(ms1MzBoundsPanel.getMzTF().getText());
                    extractionParams.setMz(m);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "m/z value is incorrect: " + ms1MzBoundsPanel.getMzTF().getText());
                    return;
                }
            }
        }

        extractionParams.setRemoveBaseline(removeBaselineCB.isSelected());
        extractionParams.setUseSmoothing(useSmoothingCB.isSelected());

        extractionParams.setMsLevel(getMsLevelCB().getSelectedIndex()+1);
        
        if (showMS2Option && getMsLevelCB().getSelectedItem().equals(MS2)) {
            ms2MzBoundsPanel.getExtractionParameters(this.extractionParams);
        }
        
        setVisible(false);
    }

    private void cancelBtnActionPerformed(ActionEvent evt) {
        this.extractionParams = null;
        setVisible(false);
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
                final ExtractionParamsDialog dialog = new ExtractionParamsDialog(new JFrame(), true);
                dialog.setExtractionParamsTitle("Extraction Parameters");
                dialog.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        FeaturesExtractionRequest.Builder builder= dialog.getExtractionParams();
                        if (builder != null) {
                            FeaturesExtractionRequest params = builder.build();
                            System.out.println(params.toString());
                        }
                        System.exit(0);
                    }
                });
                //dialog.setSize(300, 300);
                dialog.showExtractionParamsDialog();
                FeaturesExtractionRequest.Builder builder= dialog.getExtractionParams();
                if (builder != null) {
                    FeaturesExtractionRequest params = builder.build();
                    System.out.println(params.toString());
                }
                System.exit(0);
            }
        });
    }

}

class MS2MzBoundsPanel extends JPanel {
   
    private JTextField mzTF;
    private MS1MzBoundsPanel fragmentMzBoundsPanel;
    private JPanel tolerancePanel;
    private JTextField toleranceTF;
    
    public MS2MzBoundsPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel innerPanel = new JPanel();
        innerPanel.setBorder(new TitledBorder("Precursor mz"));
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
        
        JPanel innerPanel2 = new JPanel();
        innerPanel2.setLayout(new FlowLayout(FlowLayout.LEADING));
        innerPanel2.add(new JLabel("Detect at m/z"));
        innerPanel2.add(getMzTF());
        
        innerPanel.add(getTolerancePanel());
        innerPanel.add(innerPanel2);
        add(innerPanel);
        
        fragmentMzBoundsPanel = new MS1MzBoundsPanel("Fragment mz");
        add(fragmentMzBoundsPanel);

        fragmentMzBoundsPanel.getToleranceTF().setText(Float.toString(MzScopePreferences.getInstance().getFragmentMzPPMTolerance()));
    }
    
    JTextField getMzTF() {
        if (mzTF == null) {
            mzTF = new JTextField();
            mzTF.setText("0.0");
            mzTF.setColumns(5);
        }
        return mzTF;
    }
    
       private JPanel getTolerancePanel() {
        if (tolerancePanel == null) {
            tolerancePanel = new JPanel();
            tolerancePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            JLabel toleranceLabel = new JLabel();
            toleranceLabel.setText("m/z tolerance (ppm):");
            tolerancePanel.add(toleranceLabel);
            tolerancePanel.add(getToleranceTF());
        }
        return tolerancePanel;
    }
        
    JTextField getToleranceTF() {
        if (toleranceTF == null) {
            toleranceTF = new JTextField();
            toleranceTF.setColumns(5);
            toleranceTF.setToolTipText("Tolerance in ppm");
            toleranceTF.setText(Float.toString(MzScopePreferences.getInstance().getMzPPMTolerance()));
        }
        return toleranceTF;
    }

    void getExtractionParameters(FeaturesExtractionRequest.Builder extractionParams) {
        try {
            extractionParams.setMzTolPPM(Float.parseFloat(getToleranceTF().getText()));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "tolerance value is incorrect: " + getToleranceTF().getText());
            return;
        }
        try {
                extractionParams.setMz(Double.parseDouble(getMzTF().getText()));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "m/z value is incorrect: " + getMzTF().getText());
                return;
        }
        try {
            extractionParams.setFragmentMzTolPPM(Float.parseFloat(fragmentMzBoundsPanel.getToleranceTF().getText()));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "tolerance value is incorrect: " + fragmentMzBoundsPanel.getToleranceTF().getText());
            return;
        }
        try {
            extractionParams.setFragmentMinMz((fragmentMzBoundsPanel.getMzBoundsRB().isSelected()) ? Double.parseDouble(fragmentMzBoundsPanel.getMinMzTF().getText()) : 0.0);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "min m/z value is incorrect: " + fragmentMzBoundsPanel.getMinMzTF().getText());
            return;
        }
        try {
            extractionParams.setFragmentMaxMz((fragmentMzBoundsPanel.getMzBoundsRB().isSelected()) ? Double.parseDouble(fragmentMzBoundsPanel.getMaxMzTF().getText()) : 0.0);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "max m/z value is incorrect: " + fragmentMzBoundsPanel.getMaxMzTF().getText());
            return;
        }
        if (fragmentMzBoundsPanel.getMzBoundsRB().isSelected() && extractionParams.getFragmentMinMz() > extractionParams.getFragmentMaxMz()) {
            JOptionPane.showMessageDialog(this, "The min m/z value must be lower than max m/z");
            return;
        }
        if (fragmentMzBoundsPanel.getMzRB().isSelected()) {
            try {
                extractionParams.setFragmentMz(Double.parseDouble(fragmentMzBoundsPanel.getMzTF().getText()));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "m/z value is incorrect: " + fragmentMzBoundsPanel.getMzTF().getText());
                return;
            }
        }
    }

}

class MS1MzBoundsPanel extends JPanel {

    private JPanel noBoundsPanel;
    private JRadioButton noBoundsRB;
    private JPanel mzBoundsPanel;
    private JRadioButton mzBoundsRB;
    private JTextField maxMzTF;
    private JTextField minMzTF;
    private JPanel mzPanel;
    private JRadioButton mzRB;
    private JTextField mzTF;
    private JPanel tolerancePanel;
    private JTextField toleranceTF;
    
    public MS1MzBoundsPanel(String title) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new TitledBorder(title));
        add(getTolerancePanel());
        add(getNoBoundsPanel());
        add(getMassBoundsPanel());
        add(getMassPanel());
        ButtonGroup group = new ButtonGroup();
        group.add(noBoundsRB);
        group.add(mzBoundsRB);
        group.add(mzRB);

    }

    private JPanel getTolerancePanel() {
        if (tolerancePanel == null) {
            tolerancePanel = new JPanel();
            tolerancePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            JLabel toleranceLabel = new JLabel();
            toleranceLabel.setText("m/z tolerance (ppm):");
            tolerancePanel.add(toleranceLabel);
            tolerancePanel.add(getToleranceTF());
        }
        return tolerancePanel;
    }
        
    JTextField getToleranceTF() {
        if (toleranceTF == null) {
            toleranceTF = new JTextField();
            toleranceTF.setColumns(5);
            toleranceTF.setToolTipText("Tolerance in ppm");
            toleranceTF.setText(Float.toString(MzScopePreferences.getInstance().getMzPPMTolerance()));
        }
        return toleranceTF;
    }

     private JPanel getNoBoundsPanel() {
        if (noBoundsPanel == null) {
            noBoundsPanel = new JPanel();
            noBoundsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            noBoundsPanel.add(getNoBoundsRB());
        }
        return noBoundsPanel;
    }

    JRadioButton getNoBoundsRB() {
        if (noBoundsRB == null) {
            noBoundsRB = new JRadioButton("No m/z bounds");
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
        if (mzBoundsPanel == null) {
            mzBoundsPanel = new JPanel();
            mzBoundsPanel.setLayout(new BoxLayout(mzBoundsPanel, BoxLayout.Y_AXIS));
            JPanel prb = new JPanel();
            prb.setLayout(new FlowLayout(FlowLayout.LEFT));
            prb.add(getMzBoundsRB());
            mzBoundsPanel.add(prb);
            JPanel pmin = new JPanel();
            pmin.setLayout(new FlowLayout(FlowLayout.LEFT));
            JLabel labelMinMz = new JLabel();
            labelMinMz.setText("minimum m/z:");
            pmin.add(labelMinMz);
            pmin.add(getMinMzTF());
            mzBoundsPanel.add(pmin);
            JPanel pmax = new JPanel();
            pmax.setLayout(new FlowLayout(FlowLayout.LEFT));
            JLabel labelMaxMz = new JLabel();
            labelMaxMz.setText("maximum m/z:");
            pmax.add(labelMaxMz);
            pmax.add(getMaxMzTF());
            mzBoundsPanel.add(pmax);

        }
        return mzBoundsPanel;
    }

    JRadioButton getMzBoundsRB() {
        if (mzBoundsRB == null) {
            mzBoundsRB = new JRadioButton("Enable m/z bounds");
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

    JTextField getMinMzTF() {
        if (minMzTF == null) {
            minMzTF = new JTextField();
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

    JTextField getMaxMzTF() {
        if (maxMzTF == null) {
            maxMzTF = new JTextField();
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
        if (mzPanel == null) {
            mzPanel = new JPanel();
            mzPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            mzPanel.add(getMzRB());
            mzPanel.add(getMzTF());

        }
        return mzPanel;
    }

    JTextField getMzTF() {
        if (mzTF == null) {
            mzTF = new JTextField();
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

    JRadioButton getMzRB() {
        if (mzRB == null) {
            mzRB = new JRadioButton("Detect at m/z:");
            mzRB.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setMassBoundsEnabled(false);
                    setMassEnabled(true);
                }
            });
        }
        return mzRB;
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

    private void minMzTFFocusGained(FocusEvent evt) {
        minMzTF.selectAll();
    }

    private void maxMzTFFocusGained(FocusEvent evt) {
        maxMzTF.selectAll();
    }

    private void mzTFFocusGained(FocusEvent evt) {
        mzTF.selectAll();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled); 
        getMaxMzTF().setEnabled(enabled && getMzBoundsRB().isSelected());
        getMinMzTF().setEnabled(enabled && getMzBoundsRB().isSelected());
        getMzTF().setEnabled(enabled && getMzRB().isSelected());
        getToleranceTF().setEnabled(enabled);
        getMzBoundsRB().setEnabled(enabled);
        getMzRB().setEnabled(enabled);
        getNoBoundsRB().setEnabled(enabled);
    }
   
    
}
