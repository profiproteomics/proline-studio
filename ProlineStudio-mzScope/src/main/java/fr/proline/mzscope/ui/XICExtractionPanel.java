package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.Ms1ExtractionRequest;
import fr.proline.mzscope.model.MzScopePreferences;
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
 * @author MB243701
 */
public class XICExtractionPanel extends JPanel{
    private static Logger logger = LoggerFactory.getLogger("ProlineStudio.mzScope.XICExtractionPanel");

    private JScrollPane scrollPane;
    private JPanel internalPanel;
    private JPanel mainPanel;
    private JPanel panelMassRange;
    private JLabel massRangeLabel;
    private JTextField massRangeTF;
    private JPanel panelTolerance;
    private JLabel toleranceLabel;
    private JLabel toleranceUnitLabel;
    private JTextField toleranceTF;

   private final IExtractionExecutor extractionExecutor;
    
    public XICExtractionPanel(IExtractionExecutor extractionExecutor) {
        initComponents();
        this.extractionExecutor = extractionExecutor;
        toleranceTF.setText(Float.toString(MzScopePreferences.getInstance().getMzPPMTolerance()));
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        this.add(getScrollPane(), BorderLayout.CENTER);
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
            mainPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 2));
            mainPanel.setName("mainPanel");
            mainPanel.add(getPanelMass());
            mainPanel.add(getPanelTolerance());
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
            massRangeTF.setColumns(10);
            massRangeTF.setPreferredSize(new Dimension(massRangeTF.getPreferredSize().width, 16));
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
            panelTolerance.add(getToleranceUnitLabel());
            
        }
        return panelTolerance;
    }

    private JLabel getToleranceLabel() {
        if (toleranceLabel == null) {
            toleranceLabel = new JLabel();
            toleranceLabel.setName("toleranceLabel");
            toleranceLabel.setText("+/-");
        }
        return toleranceLabel;
    }

   private JLabel getToleranceUnitLabel() {
        if (toleranceUnitLabel == null) {
            toleranceUnitLabel = new JLabel();
            toleranceUnitLabel.setName("toleranceUnitLabel");
            toleranceUnitLabel.setText("ppm");
        }
        return toleranceUnitLabel;
    }

    private JTextField getToleranceTF() {
        if (toleranceTF == null) {
            toleranceTF = new JTextField();
            toleranceTF.setName("toleranceTF");
            toleranceTF.setColumns(5);
            toleranceTF.setPreferredSize(new Dimension(toleranceTF.getPreferredSize().width, 16));
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
        if (massRangeTF.getText() == null || massRangeTF.getText().isEmpty()){
            return;
        }
        String text = massRangeTF.getText().trim();
        double minMz = Double.NaN;
        double maxMz = Double.NaN;
        String[] masses = text.split("-");
        try{
            minMz = Double.parseDouble(masses[0]);
        }catch(NumberFormatException e){
            JOptionPane.showMessageDialog(this, "The mass is incorrect: "+masses[0]);
            return;
        }
        if (masses.length == 1) {
            try{
                float ppm = Float.parseFloat(toleranceTF.getText().trim());
                MzScopePreferences.getInstance().setMzPPMTolerance(ppm);
                maxMz = minMz + minMz * ppm / 1e6;
                minMz -= minMz * ppm / 1e6;
            }catch(NumberFormatException e){
                JOptionPane.showMessageDialog(this, "The tolerance is incorrect: "+toleranceTF.getText().trim());
                return;
            }
            
        } else {
            try{
                maxMz = Double.parseDouble(masses[1]);
            }catch(NumberFormatException e){
                JOptionPane.showMessageDialog(this, "The max mass is incorrect: "+masses[1]);
                return;
            }
        }
        
        if (extractionExecutor != null) {
         extractionExecutor.extractChromatogramMass(Ms1ExtractionRequest.builder().setMinMz(minMz).setMaxMz(maxMz).build());
        }
    }

    private void toleranceTFActionPerformed(java.awt.event.ActionEvent evt) {
        float ppm = Float.parseFloat(toleranceTF.getText().trim());
        MzScopePreferences.getInstance().setMzPPMTolerance(ppm);
        massRangeTFActionPerformed(evt);
    }
}
