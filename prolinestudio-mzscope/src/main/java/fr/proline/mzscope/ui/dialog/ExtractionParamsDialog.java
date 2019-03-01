package fr.proline.mzscope.ui.dialog;

import fr.proline.mzscope.model.FeaturesExtractionRequest;
import fr.proline.mzscope.ui.model.MzScopePreferences;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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

    private JScrollPane scrollPane;
    private JPanel mainPanel;
    private JPanel cardPanel;
    private MzBoundsPanel precursorMzBoundsPanel;
    private Ms2Panel ms2Panel;
    private JPanel msLevelPanel;
    private JComboBox msLevelCbx;
    private JPanel buttonsPanel;
    private JButton cancelBtn;
    private JButton okBtn;
    private JTextField mzTF;
    private JPanel removeBaselinePanel;
    private JCheckBox removeBaselineCB;
    
    
    private boolean showMS2Option;
    

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
        precursorMzBoundsPanel.getToleranceTF().setText(Float.toString(MzScopePreferences.getInstance().getMzPPMTolerance()));
        getRootPane().setDefaultButton(okBtn);
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
            mainPanel.add(getBaselineRemoverPanel());
            mainPanel.add(getMzBoundsPanel());
            mainPanel.add(getButtonPanel());
        }
        return mainPanel;
    }
  
    private JPanel getMsLevelPanel(){
        if (msLevelPanel == null) {
            msLevelPanel = new JPanel();
            msLevelPanel.setLayout(new FlowLayout(FlowLayout.LEADING,5,5));
            JLabel label = new JLabel();
            label.setText("MS level: ");
            msLevelPanel.add(label);
            msLevelPanel.add(getMsLevelCbx());
        }
        return msLevelPanel;
    }

    private JComboBox getMsLevelCbx(){
        if (msLevelCbx == null) {
            String[] items = {MS1, MS2};
            msLevelCbx = new JComboBox(items);
            msLevelCbx.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    CardLayout layout = (CardLayout)cardPanel.getLayout();
                    layout.show(cardPanel, (String)e.getItem());
                }
            });
        }
        return msLevelCbx;
    }

    private JPanel getMzBoundsPanel() {
        if (cardPanel == null) {
            cardPanel = new JPanel();
            cardPanel.setLayout(new CardLayout());
            JPanel innerPanel = new JPanel();
            innerPanel.setLayout(new FlowLayout());
            precursorMzBoundsPanel = new MzBoundsPanel("Precursor mz");
            innerPanel.add(precursorMzBoundsPanel);
            cardPanel.add(MS1, innerPanel);
            cardPanel.add(MS2, getMs2Panel());
        }
        return cardPanel;
    }
    
    
    private Component getMs2Panel() {
        if (ms2Panel == null) {
            ms2Panel = new Ms2Panel();
        }
        
        return ms2Panel;
    }
      
    private JPanel getBaselineRemoverPanel() {
        if (removeBaselinePanel == null) {
            removeBaselinePanel = new JPanel();
            removeBaselinePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            removeBaselinePanel.add(getBaselineCB());
        }
        return removeBaselinePanel;
    }


    private JCheckBox getBaselineCB() {
        if (removeBaselineCB == null) {
            removeBaselineCB = new JCheckBox();
            removeBaselineCB.setToolTipText("Remove peakels baseline during peakel detection");
            removeBaselineCB.setText("use baseline remover");
        }
        return removeBaselineCB;
    }

  
    private JPanel getButtonPanel() {
        if (buttonsPanel == null) {
            buttonsPanel = new JPanel();
            buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
            buttonsPanel.add(getOkButton());
            buttonsPanel.add(getCancelButton());

        }
        return buttonsPanel;
    }

    private JButton getCancelButton() {
        if (cancelBtn == null) {
            cancelBtn = new JButton();
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

    public FeaturesExtractionRequest.Builder getExtractionParams() {
        return extractionParams;
    }

    public void showExtractionParamsDialog() {
        setVisible(true);
    }

    private void okBtnActionPerformed(ActionEvent evt) {
        this.extractionParams = FeaturesExtractionRequest.builder();

        if (!showMS2Option || getMsLevelCbx().getSelectedItem().equals(MS1)) {

            try {
                extractionParams.setMzTolPPM(Float.parseFloat(precursorMzBoundsPanel.getToleranceTF().getText()));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "tolerance value is incorrect: " + precursorMzBoundsPanel.getToleranceTF().getText());
                return;
            }
            try {
                extractionParams.setMinMz((precursorMzBoundsPanel.getMzBoundsRB().isSelected()) ? Double.parseDouble(precursorMzBoundsPanel.getMinMzTF().getText()) : 0.0);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "min m/z value is incorrect: " + precursorMzBoundsPanel.getMinMzTF().getText());
                return;
            }
            try {
                extractionParams.setMaxMz((precursorMzBoundsPanel.getMzBoundsRB().isSelected()) ? Double.parseDouble(precursorMzBoundsPanel.getMaxMzTF().getText()) : 0.0);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "max m/z value is incorrect: " + precursorMzBoundsPanel.getMaxMzTF().getText());
                return;
            }
            if (precursorMzBoundsPanel.getMzBoundsRB().isSelected() && extractionParams.getMinMz() > extractionParams.getMaxMz()) {
                JOptionPane.showMessageDialog(this, "The min m/z value must be lower than max m/z");
                return;
            }
            if (precursorMzBoundsPanel.getMzRB().isSelected()) {
                try {
                    double m = Double.parseDouble(precursorMzBoundsPanel.getMzTF().getText());
                    extractionParams.setMz(m);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "m/z value is incorrect: " + precursorMzBoundsPanel.getMzTF().getText());
                    return;
                }
            }
        }

        extractionParams.setRemoveBaseline(removeBaselineCB.isSelected());
        extractionParams.setMsLevel(getMsLevelCbx().getSelectedIndex()+1);
        
        if (showMS2Option && getMsLevelCbx().getSelectedItem().equals(MS2)) {
            ms2Panel.getExtractionParameters(this.extractionParams);
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

class Ms2Panel extends JPanel {
   
    private JTextField mzTF;
    private MzBoundsPanel fragmentMzBoundsPanel;
    private JPanel tolerancePanel;
    private JTextField toleranceTF;
    
    public Ms2Panel() {
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
        
        fragmentMzBoundsPanel = new MzBoundsPanel("Fragment mz");
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

class MzBoundsPanel extends JPanel {

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
    
    public MzBoundsPanel(String title) {
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
