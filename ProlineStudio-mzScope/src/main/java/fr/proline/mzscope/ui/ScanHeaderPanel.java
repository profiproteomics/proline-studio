package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.Spectrum;
import fr.proline.mzscope.ui.event.ScanHeaderListener;
import fr.proline.mzscope.utils.MzScopeConstants.DisplayMode;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.JTextField;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
    
    private final static String TXT_KEEP_MSLEVEL = "Keep same ms level";
    private final static String TXT_XIC_OVERLAY = "XIC Overlay";

    private JScrollPane scrollPane;
    private JPanel mainPanel;
    private JPanel panelTitle;
    private JLabel labelTitle;
    private JPanel panelPrecursor;
    private JLabel labelPrecursor;
    private JPanel panelScanIndex;
    private JLabel labelScanIndex;
    private JSpinner spinnerScanIndex;
    private SpinnerModel spinnerScanIndexModel;
    private JPanel panelRetentionTime;
    private JLabel labelRetentionTime;
    private JTextField textFieldRetentionTime;
    private JPanel panelMsLevel;
    private JLabel labelMsLevel;
    private JTextField textFieldMsLevel;
    private JCheckBox cbKeepMsLevel;
    private JCheckBox cbXicOverlay;

    // mzdb fileName
    private String mzdbFileName;
    // current scan information
    private Spectrum scan;
    // list of all scanIndex
    private List<Integer> scanIndexList;
    
    // true by default, but mode is hidden for multi rawFile
    private boolean displayXicModeVisible;

    //events
    private EventListenerList listenerList = new EventListenerList();

    public ScanHeaderPanel(Spectrum scan, List<Integer> scanIndexList, boolean displayXicModeVisible) {
        this.scan = scan;
        this.displayXicModeVisible = displayXicModeVisible;
        this.scanIndexList = scanIndexList;
        initComponents();
    }

    private void initComponents() {
        this.setLayout(new BorderLayout());
        this.add(getScrollPane(), BorderLayout.CENTER);
        updateScan();
    }
    
    private JScrollPane getScrollPane() {
        if (scrollPane == null) {
            scrollPane = new JScrollPane(getMainPanel());
            scrollPane.setName("scrollPane");
            this.add(scrollPane);
        }
        return scrollPane;
    }
    
    private JPanel getMainPanel() {
       if (this.mainPanel == null) {
            mainPanel = new JPanel();
            mainPanel.setName("mainPanel");
            mainPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 2));
            mainPanel.add(getPanelScanIndex());
            mainPanel.add(getPanelRetentionTime());
            mainPanel.add(getPanelMsLevel());
            mainPanel.add(getPanelTitle());
            mainPanel.add(getPanelPrecursor());
        }
        return this.mainPanel; 
    }

    private JPanel getPanelTitle() {
        if (this.panelTitle == null) {
            panelTitle = new JPanel();
            panelTitle.setName("panelTitle");
            panelTitle.setLayout(new FlowLayout());
            panelTitle.add(getLabelTitle());
        }
        return this.panelTitle;
    }
    
    private JPanel getPanelPrecursor() {
        if (this.panelPrecursor == null) {
            panelPrecursor = new JPanel();
            panelPrecursor.setName("panelPrecursor");
            panelPrecursor.setLayout(new FlowLayout());
            panelPrecursor.add(getLabelPrecursor());
        }
        return this.panelPrecursor;
    }
    
    private JPanel getPanelScanIndex() {
        if (this.panelScanIndex == null) {
            panelScanIndex = new JPanel();
            panelScanIndex.setName("panelScanIndex");
            panelScanIndex.setLayout(new FlowLayout());
            panelScanIndex.add(getLabelScanIndex());
            panelScanIndex.add(getSpinnerScanIndex());
        }
        return this.panelScanIndex;
    }

    private JPanel getPanelRetentionTime() {
        if (this.panelRetentionTime == null) {
            panelRetentionTime = new JPanel();
            panelRetentionTime.setName("panelRetentionTime");
            panelRetentionTime.setLayout(new FlowLayout());
            panelRetentionTime.add(getLabelRetentionTime());
            panelRetentionTime.add(getTextFieldRetentionTime());
        }
        return this.panelRetentionTime;
    }

    private JPanel getPanelMsLevel() {
        if (this.panelMsLevel == null) {
            panelMsLevel = new JPanel();
            panelMsLevel.setName("panelMsLevel");
            panelMsLevel.setLayout(new FlowLayout());
            panelMsLevel.add(getLabelMsLevel());
            panelMsLevel.add(getTextFieldMsLevel());
            panelMsLevel.add(getCbKeepMsLevel());
            panelMsLevel.add(getCbXicOverlay());
        }
        return this.panelMsLevel;
    }

    private JLabel getLabelTitle() {
        if (this.labelTitle == null) {
            labelTitle = new JLabel(mzdbFileName);
            labelTitle.setName("labelTitle");
        }
        return this.labelTitle;
    }
    
    private JLabel getLabelPrecursor() {
        if (this.labelPrecursor == null) {
            labelPrecursor = new JLabel("");
            labelPrecursor.setName("labelPrecursor");
        }
        return this.labelPrecursor;
    }
    
    private JLabel getLabelScanIndex() {
        if (this.labelScanIndex == null) {
            labelScanIndex = new JLabel("Scan Index:");
            labelScanIndex.setName("labelScanIndex");
        }
        return this.labelScanIndex;
    }

    private JLabel getLabelRetentionTime() {
        if (this.labelRetentionTime == null) {
            labelRetentionTime = new JLabel("rt:");
            labelRetentionTime.setName("labelRetentionTime");
        }
        return this.labelRetentionTime;
    }

    private JLabel getLabelMsLevel() {
        if (this.labelMsLevel == null) {
            labelMsLevel = new JLabel("ms:");
            labelMsLevel.setName("labelMsLevel");
        }
        return this.labelMsLevel;
    }

    private JSpinner getSpinnerScanIndex() {
        if (this.spinnerScanIndex == null) {
            spinnerScanIndexModel = new SpinnerListModel(scanIndexList);
            spinnerScanIndex = new JSpinner(spinnerScanIndexModel);
            ((DefaultEditor) spinnerScanIndex.getEditor()).getTextField().setEditable(false);
            ((DefaultEditor) spinnerScanIndex.getEditor()).getTextField().setColumns(5);
            spinnerScanIndex.setName("spinnerScanIndex");
            spinnerScanIndex.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    scanIndexChanged(e);
                }
            });
        }
        return this.spinnerScanIndex;
    }

    private JTextField getTextFieldRetentionTime() {
        if (this.textFieldRetentionTime == null) {
            textFieldRetentionTime = new JTextField();
            textFieldRetentionTime.setToolTipText("retention time in min");
            textFieldRetentionTime.setColumns(6);
            textFieldRetentionTime.setName("textFieldRetentionTime");
            textFieldRetentionTime.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    retentionTimeActionPerformed(evt);
                }
            });
        }
        return this.textFieldRetentionTime;
    }

    private JTextField getTextFieldMsLevel() {
        if (this.textFieldMsLevel == null) {
            textFieldMsLevel = new JTextField();
            textFieldMsLevel.setEditable(false);
            textFieldMsLevel.setColumns(3);
            textFieldMsLevel.setName("textFieldMsLevel");
            textFieldMsLevel.setToolTipText("msLevel");
        }
        return this.textFieldMsLevel;
    }
    
    private JCheckBox getCbKeepMsLevel() {
        if (this.cbKeepMsLevel == null) {
            this.cbKeepMsLevel = new JCheckBox(TXT_KEEP_MSLEVEL);
            this.cbKeepMsLevel.setSelected(true);
            this.cbKeepMsLevel.setName("cbKeepMsLevel");
            this.cbKeepMsLevel.setToolTipText("You can also use the keyboard's arrows (or Ctrl+arrows to keep the same ms level)");
            this.cbKeepMsLevel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    msLevelActionPerformed(e);
                }
            });
        }
        return cbKeepMsLevel;
    }
    
    private JCheckBox getCbXicOverlay() {
        if (this.cbXicOverlay == null) {
            this.cbXicOverlay = new JCheckBox(TXT_XIC_OVERLAY);
            this.cbXicOverlay.setSelected(false);
            this.cbXicOverlay.setName("cbXicOverlay");
            this.cbXicOverlay.setToolTipText("You can also use the Alt key");
            this.cbXicOverlay.setVisible(displayXicModeVisible);
            this.cbXicOverlay.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    xicOverlayActionPerformed(e);
                }
            });
        }
        return cbXicOverlay;
    }
    

    private void updateScan() {
        if (scan == null) {
            clearValues();
        } else {
            if (scan.getPrecursorMz() == null) {
                labelPrecursor.setText("");
            }else{
                StringBuilder builder = new StringBuilder();
                builder.append(MASS_FORMATTER.format(scan.getPrecursorMz())).append(" (");
                builder.append(scan.getPrecursorCharge()).append("+)");
                labelPrecursor.setText(builder.toString());
            }
            try{
                spinnerScanIndex.setValue(scan.getIndex());
            }catch(IllegalArgumentException e){
                
            }
            textFieldRetentionTime.setText(TIME_FORMATTER.format(scan.getRetentionTime() / 60.0));
            textFieldMsLevel.setText(Integer.toString(scan.getMsLevel()));
        }
    }

    private void clearValues() {
        labelTitle.setText("");
        labelPrecursor.setText("");
        spinnerScanIndex.setValue(scanIndexList.get(0));
        textFieldRetentionTime.setText("");
        textFieldMsLevel.setText("");
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
     * set the scan index list (previous, current, next)
     * @param scanIndexList 
     */
    public void setScanIndexList(List<Integer> scanIndexList) {
        this.scanIndexList = scanIndexList;
        this.spinnerScanIndexModel = new SpinnerListModel(scanIndexList);
        if (scanIndexList.size() > 1){
            spinnerScanIndexModel.setValue(scanIndexList.get(1));
        }
        this.spinnerScanIndex.setModel(spinnerScanIndexModel);
        ((DefaultEditor) spinnerScanIndex.getEditor()).getTextField().setEditable(false);
        ((DefaultEditor) spinnerScanIndex.getEditor()).getTextField().setColumns(5);
    }

    /**
     * set the mzdb fileName
     * @param fileName 
     */
    public void setMzdbFileName(String fileName) {
        this.mzdbFileName = fileName;
        this.labelTitle.setText(fileName);
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
    
    private void fireXicOverlay(DisplayMode mode) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == ScanHeaderListener.class) {
                ((ScanHeaderListener) listeners[i + 1]).updateXicDisplayMode(mode);
            }
        }
    }

    public void retentionTimeActionPerformed(ActionEvent evt) {
        if (scan == null) {
            return;
        }
        // check value
        String value = this.textFieldRetentionTime.getText();
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
        int scanIndex = ((Integer)spinnerScanIndex.getValue()).intValue();
        fireUpdateScanIndex(scanIndex);
    }
    
    public void msLevelActionPerformed(ActionEvent evt) {
        if (scan == null) {
            return;
        }
        fireKeepMsLevel(this.cbKeepMsLevel.isSelected());
    }
    
    public void xicOverlayActionPerformed(ActionEvent evt) {
        fireXicOverlay(this.cbXicOverlay.isSelected() ? DisplayMode.OVERLAY : DisplayMode.REPLACE);
    }

}
