/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.ui.dialog;

import fr.proline.studio.gui.DefaultDialog;
import org.jdesktop.swingx.JXComboBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * @author CB205360
 */
public class IsotopicPredictionParamDialog extends DefaultDialog {

  JComboBox m_spectrum;
  public final static String CURRENT_SPECTRUM = "Current Spectrum";
  public final static String REFERENCE_SPECTRUM = "Reference Spectrum";

  String[] spectrum = {CURRENT_SPECTRUM, REFERENCE_SPECTRUM};

  public IsotopicPredictionParamDialog(Window parent) {
    super(parent, ModalityType.APPLICATION_MODAL);
    setTitle("Isotopic prediction parameters");
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
    JLabel nbrPointLabel = new JLabel("Spectrum to annotate:");
    internalPanel.add(nbrPointLabel, c);

    c.gridx++;
    c.weightx = 1;
    m_spectrum = new JXComboBox(spectrum);
    m_spectrum.setSelectedItem(CURRENT_SPECTRUM);
    internalPanel.add(m_spectrum, c);

    return internalPanel;
  }

  public String getSpectrum() {
    return m_spectrum.getSelectedItem().toString();
  }

}
