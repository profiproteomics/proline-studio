/*
 * Copyright (C) 2019
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
package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.ExtractionRequest;
import fr.proline.mzscope.ui.model.MzScopePreferences;
import fr.proline.mzscope.utils.Display;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The extraction Panel contains the different parameters that could be changed
 * for the extraction: mass, tolerance for a DIA file contains Fragment m/z
 *
 * @author MB243701
 */
public class ExtractionToolbar extends JPanel implements PropertyChangeListener {

  private static final int COMPONENTS_HEIGHT = 16;
  private static final int COMPONENTS_GAP = 3;

  private static Logger logger = LoggerFactory.getLogger(ExtractionToolbar.class);

  private static DecimalFormat PRECISION_DF = new DecimalFormat("#.####");
  private static DecimalFormat SINGLE_DIGIT_DF = new DecimalFormat("#.#");

  private JTextField massRangeTF;
  private JTextField toleranceTF;
  private JTextField fragmentMassRangeTF;
  private JTextField fragmentToleranceTF;

  private final IMzScopeController appController;
  private JTextField mobilityToleranceTF;
  private JTextField mobilityRangeTF;


  public ExtractionToolbar(IMzScopeController appController) {
    initComponents();
    this.appController = appController;
    appController.addPropertyChangeListener(IMzScopeController.CURRENT_RAWFILE_VIEWER, this);
    MzScopePreferences.getInstance().addPropertyChangeListener(MzScopePreferences.LAST_EXTRACTION_REQUEST, this);
    toleranceTF.setText(Integer.toString(Math.round(MzScopePreferences.getInstance().getMzPPMTolerance())));
    fragmentToleranceTF.setText(Integer.toString(Math.round(MzScopePreferences.getInstance().getFragmentMzPPMTolerance())));
  }

  private void initComponents() {
    setLayout(new BorderLayout());

    JPanel mainPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, COMPONENTS_GAP));

    JPanel massRangePanel = new JPanel();
    massRangePanel.setLayout(new FlowLayout(FlowLayout.LEADING, COMPONENTS_GAP, 0));
    massRangePanel.add(new JLabel("Precursor m/z:"));
    massRangePanel.add(getMassRangeTF());

    massRangePanel.add(new JLabel("+/-"));
    massRangePanel.add(getToleranceTF());
    massRangePanel.add(new JLabel("ppm,"));

    mainPanel.add(massRangePanel);

    JPanel fragmentMassRangePanel = new JPanel();
    fragmentMassRangePanel.setLayout(new FlowLayout(FlowLayout.LEADING, COMPONENTS_GAP, 0));
    fragmentMassRangePanel.add(new JLabel("Fragment m/z:"));
    fragmentMassRangePanel.add(getFragmentMassRangeTF());
    fragmentMassRangePanel.add(new JLabel("+/-"));
    fragmentMassRangePanel.add(getFragmentToleranceTF());
    fragmentMassRangePanel.add(new JLabel("ppm,"));

    mainPanel.add(fragmentMassRangePanel);

    JPanel mobilityRangePanel = new JPanel();
    mobilityRangePanel.setLayout(new FlowLayout(FlowLayout.LEADING, COMPONENTS_GAP, 0));
    mobilityRangePanel.add(new JLabel("Ion mobility:"));
    mobilityRangePanel.add(getMobilityRangeTF());
    mobilityRangePanel.add(new JLabel("+/-"));
    mobilityRangePanel.add(getMobilityToleranceTF());

    mainPanel.add(mobilityRangePanel);

    this.add(mainPanel, BorderLayout.WEST);
  }

  private JTextField getMassRangeTF() {
    if (massRangeTF == null) {
      massRangeTF = new JTextField();
      massRangeTF.setToolTipText("<html><p width=\"500\">Precursor mass range to extract. A single value can be entered (the following tolerance value will be used) or a range of values using '-' delimiter</p></html>");
      massRangeTF.setColumns(10);
      massRangeTF.setPreferredSize(new Dimension(massRangeTF.getPreferredSize().width, COMPONENTS_HEIGHT));
      massRangeTF.addActionListener(evt -> startExtraction());
    }
    return massRangeTF;
  }

  private JTextField getToleranceTF() {
    if (toleranceTF == null) {
      toleranceTF = new JTextField();
      toleranceTF.setColumns(5);
      toleranceTF.setPreferredSize(new Dimension(toleranceTF.getPreferredSize().width, COMPONENTS_HEIGHT));
      toleranceTF.setToolTipText("Precursor mass tolerance in ppm");
      toleranceTF.addActionListener(evt -> startExtraction());
    }
    return toleranceTF;
  }

  private JTextField getFragmentMassRangeTF() {
    if (fragmentMassRangeTF == null) {
      fragmentMassRangeTF = new JTextField();
      fragmentMassRangeTF.setToolTipText("<html><p width=\"500\">Fragment mass range to extract. A single value can be entered (the following tolerance value will be used) or a range of values using '-' delimiter</p></html>");
      fragmentMassRangeTF.setColumns(10);
      fragmentMassRangeTF.setPreferredSize(new Dimension(fragmentMassRangeTF.getPreferredSize().width, COMPONENTS_HEIGHT));
      fragmentMassRangeTF.addActionListener(evt -> startExtraction());
    }
    return fragmentMassRangeTF;
  }


  private JTextField getFragmentToleranceTF() {
    if (fragmentToleranceTF == null) {
      fragmentToleranceTF = new JTextField();
      fragmentToleranceTF.setColumns(5);
      fragmentToleranceTF.setPreferredSize(new Dimension(fragmentToleranceTF.getPreferredSize().width, COMPONENTS_HEIGHT));
      fragmentToleranceTF.setToolTipText("Fragment mass tolerance in ppm");
      fragmentToleranceTF.addActionListener(evt -> startExtraction());
    }
    return fragmentToleranceTF;
  }


  private JTextField getMobilityRangeTF() {
    if (mobilityRangeTF == null) {
      mobilityRangeTF = new JTextField();
      mobilityRangeTF.setToolTipText("<html><p width=\"500\">Ion mobility mass range to extract. A single value can be entered (the following tolerance value will be used) or a range of values using '-' delimiter</p></html>");
      mobilityRangeTF.setColumns(12);
      mobilityRangeTF.setPreferredSize(new Dimension(mobilityRangeTF.getPreferredSize().width, COMPONENTS_HEIGHT));
      mobilityRangeTF.addActionListener(evt -> startExtraction());
    }
    return mobilityRangeTF;
  }


  private JTextField getMobilityToleranceTF() {
    if (mobilityToleranceTF == null) {
      mobilityToleranceTF = new JTextField();
      mobilityToleranceTF.setColumns(5);
      mobilityToleranceTF.setPreferredSize(new Dimension(mobilityToleranceTF.getPreferredSize().width, COMPONENTS_HEIGHT));
      mobilityToleranceTF.setToolTipText("Ion mobility tolerance (abs)");
      mobilityToleranceTF.addActionListener(evt -> startExtraction());
    }
    return mobilityToleranceTF;
  }

  private void startExtraction() {
//    if (massRangeTF.getText() == null || massRangeTF.getText().isEmpty()) {
//      return;
//    }
    ExtractionRequest.Builder builder = ExtractionRequest.builder(this);
    double firstValue = Double.NaN;
    double secondValue = Double.NaN;
    float toleranceValue;
    String text;
    String[] stringValues;

    if (massRangeTF.isEnabled() && massRangeTF.getText() != null && !massRangeTF.getText().isEmpty()) {
      text = massRangeTF.getText().trim();
      stringValues = text.split("-");
      try {
        firstValue = Double.parseDouble(stringValues[0]); // will be updated later in this method
      } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "The mass value is incorrect: " + stringValues[0]);
        return;
      }
      try {
        toleranceValue = Float.parseFloat(toleranceTF.getText().trim());
      } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "The tolerance value is incorrect: " + toleranceTF.getText().trim());
        return;
      }
      if (stringValues.length == 1) {
        builder.setMz(firstValue);
        builder.setMzTolPPM(toleranceValue);
      } else {
        try {
          secondValue = Double.parseDouble(stringValues[1]);
          builder.setMinMz(firstValue);
          builder.setMaxMz(secondValue);
        } catch (NumberFormatException e) {
          JOptionPane.showMessageDialog(this, "The max mass value is incorrect: " + stringValues[1]);
          return;
        }
      }
    }

    if (fragmentMassRangeTF.isEnabled() && fragmentMassRangeTF.getText() != null && !fragmentMassRangeTF.getText().isEmpty()) {
      text = fragmentMassRangeTF.getText().trim();
      stringValues = text.split("-");
      try {
        firstValue = Double.parseDouble(stringValues[0]); // will be updated later in this method
      } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "The mass value is incorrect: " + stringValues[0]);
        return;
      }
      try {
        toleranceValue = Float.parseFloat(fragmentToleranceTF.getText().trim());
      } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "The tolerance value is incorrect: " + fragmentToleranceTF.getText().trim());
        return;
      }
      if (stringValues.length == 1) {
        builder.setFragmentMz(firstValue);
        builder.setFragmentMzTolPPM(toleranceValue);
      } else {
        try {
          secondValue = Double.parseDouble(stringValues[1]);
          builder.setFragmentMinMz(firstValue);
          builder.setFragmentMaxMz(secondValue);
        } catch (NumberFormatException e) {
          JOptionPane.showMessageDialog(this, "The max mass value is incorrect: " + stringValues[1]);
          return;
        }
      }
    }

    if (mobilityRangeTF.isEnabled() && mobilityRangeTF.getText() != null && !mobilityRangeTF.getText().isEmpty()) {
      text = mobilityRangeTF.getText().trim();
      stringValues = text.split("-");
      try {
        firstValue = Double.parseDouble(stringValues[0]); // will be updated later in this method
      } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "The mobility value is incorrect: " + stringValues[0]);
        return;
      }

      if (stringValues.length == 1) {
        try {
          toleranceValue = Float.parseFloat(mobilityToleranceTF.getText().trim());
        } catch (NumberFormatException e) {
          JOptionPane.showMessageDialog(this, "The tolerance value is incorrect: " + mobilityToleranceTF.getText().trim());
          return;
        }
        builder.setMobility(firstValue);
        builder.setMobilityTol(toleranceValue);
      } else {
        try {
          secondValue = Double.parseDouble(stringValues[1]);
          builder.setMinMobility(firstValue);
          builder.setMaxMobility(secondValue);
        } catch (NumberFormatException e) {
          JOptionPane.showMessageDialog(this, "The max mobility value is incorrect: " + stringValues[1]);
          return;
        }
      }
    }

    IRawFileViewer currentViewer = appController.getCurrentRawFileViewer();
    if (currentViewer != null) {
      currentViewer.extractAndDisplay(builder.build(), new Display(currentViewer.getChromatogramDisplayMode()), null);
    }

  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (evt.getPropertyName() == IMzScopeController.CURRENT_RAWFILE_VIEWER) {

      IRawFileViewer viewer = (IRawFileViewer) evt.getNewValue();
      getFragmentMassRangeTF().setEnabled(viewer.getCurrentRawfile().isDIAFile());
      getFragmentToleranceTF().setEditable(viewer.getCurrentRawfile().isDIAFile());
      getMobilityRangeTF().setEnabled(viewer.getCurrentRawfile().hasIonMobilitySeparation());
      getMobilityToleranceTF().setEditable(viewer.getCurrentRawfile().hasIonMobilitySeparation());

    } else if (evt.getPropertyName() == MzScopePreferences.LAST_EXTRACTION_REQUEST) {

      ExtractionRequest request = (ExtractionRequest) evt.getNewValue();
      if ((request.getMsLevel() == 1)) {
        switch (request.getMzRequestType()) {
          case CENTERED:
            getToleranceTF().setText(SINGLE_DIGIT_DF.format(request.getMzTolPPM()));
            getMassRangeTF().setText(PRECISION_DF.format(request.getMz()));
            break;
          case RANGE:
            getMassRangeTF().setText(PRECISION_DF.format(request.getMinMz()) + "-" + PRECISION_DF.format(request.getMaxMz()));
            getToleranceTF().setText("");
            break;
          default:
            getMassRangeTF().setText("");
            getToleranceTF().setText("");
        }
      }

      if (request.getMsLevel() == 2) {
        getToleranceTF().setText(Double.toString(request.getMzTolPPM()));
        getMassRangeTF().setText(PRECISION_DF.format(request.getMz()));
        switch (request.getFragmentRequestType()) {
          case CENTERED:
            getFragmentToleranceTF().setText(SINGLE_DIGIT_DF.format(request.getFragmentMzTolPPM()));
            getFragmentMassRangeTF().setText(PRECISION_DF.format(request.getFragmentMz()));
            break;
          case RANGE:
            getFragmentMassRangeTF().setText(PRECISION_DF.format(request.getFragmentMinMz() + "-" + request.getFragmentMaxMz()));
            getFragmentToleranceTF().setText("");
            break;
          default:
            getFragmentMassRangeTF().setText("");
            getFragmentToleranceTF().setText("");
        }
      }

      switch (request.getMobilityRequestType()) {
        case CENTERED:
          getMobilityRangeTF().setText(PRECISION_DF.format(request.getMobility()));
          getMobilityToleranceTF().setText(PRECISION_DF.format(request.getMobilityTol()));
          break;
        case RANGE:
          getMobilityRangeTF().setText(PRECISION_DF.format(request.getMinMobility()) + "-" + PRECISION_DF.format(request.getMaxMobility()));
          getMobilityToleranceTF().setText("");
          break;
      }

    }
  }

}
