package fr.proline.mzscope.ui.dialog;

import fr.proline.studio.gui.DefaultDialog;
import jwave.transforms.wavelets.Wavelet;
import org.jdesktop.swingx.JXComboBox;

//import javax.swing.*;
import java.awt.*;

import jwave.transforms.wavelets.WaveletBuilder;

import javax.swing.*;

public class WaveletSmootherParamDialog extends DefaultDialog {



    public WaveletSmootherParamDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
        setTitle("Smoothing parameters for wavelet denoising ");
        setInternalComponent(createInternalPanel());
    }
    JComboBox waveletScanParameter;

    JComboBox waveletListCombo;
    JTextField m_nbrPoint;
    JCheckBox checkBox;
    JSpinner sp;


    JLabel thresholdLabel;
    public final static String MAX_CORRELATION="Correlation max";
    public final static String MIN_CORRELATION="Correlation min";
    public final static String MAX_SMOOTHNESS="Max smoothness";
    public final static String BOTH_SMOOTHNESS_AND_CORRELATION="smoothness + correlation";


    public final static String MAX_SNR="Best SNR";

    public final static String MIN_ENERGY_DIFF="Min energy difference";

    public final static String MIN_EUCLIDEAN_DISTANCE="Min euclidean distance";



    String[] waveletParameters={MAX_CORRELATION,MIN_CORRELATION,MAX_SMOOTHNESS,BOTH_SMOOTHNESS_AND_CORRELATION,
            MAX_SNR,MIN_ENERGY_DIFF,MIN_EUCLIDEAN_DISTANCE};


    Wavelet[] arrayOfWavelet=WaveletBuilder.create2arr();

    private String[] getWaveletNames(Wavelet[] arrayOfWavelet){
        String[] listOfWaveletNames=new String[arrayOfWavelet.length];
        for (int k=0;k< arrayOfWavelet.length;k++){
            listOfWaveletNames[k]=arrayOfWavelet[k].getName();
        }
        return listOfWaveletNames;
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
        c.weightx = 1;
        JLabel nbrPointLabel = new JLabel("scan parameters ");
        internalPanel.add(nbrPointLabel, c);

        c.gridy++;
        c.weightx = 1;
        waveletScanParameter = new JXComboBox(waveletParameters);
        internalPanel.add(waveletScanParameter,c);


        JLabel userLabel=new JLabel("Select wavelet: ");
        c.gridy++;
        internalPanel.add(userLabel,c);
        checkBox=new JCheckBox();
        checkBox.setSelected(false);
        checkBox.addActionListener(e -> {
            boolean isSelected = checkBox.isSelected();
            waveletScanParameter.setEnabled(!isSelected);
            waveletListCombo.setEnabled(isSelected);
            if (!isSelected){
                waveletListCombo.setForeground(Color.BLACK);
            }


        });
        c.gridy++;
        internalPanel.add(checkBox,c);

        waveletListCombo=new JComboBox<>(getWaveletNames(arrayOfWavelet));
        waveletListCombo.setEnabled(checkBox.isSelected());
        c.gridy++;
        internalPanel.add(waveletListCombo,c);


        thresholdLabel=new JLabel("threshold cutOff");
        thresholdLabel.setToolTipText("");
        c.gridy++;
        internalPanel.add(thresholdLabel,c);
        SpinnerModel model = new SpinnerNumberModel(
                1.5,
                0,
                10,
                0.1
        );

        sp=new JSpinner(model);
        c.gridy++;
        internalPanel.add(sp,c);

        return internalPanel;
    }

    public String getMethod() {
        return waveletScanParameter.getSelectedItem().toString();
    }

    public String getWaveletSelected(){
        return waveletListCombo.getSelectedItem().toString();
    }

    public double getJSpinnerValue(){
        return (double) sp.getValue();
    }

    public boolean getUserChoice(){
        return checkBox.isSelected();
    }
    @Override
    protected boolean okCalled() {
        return true;


    }

}
