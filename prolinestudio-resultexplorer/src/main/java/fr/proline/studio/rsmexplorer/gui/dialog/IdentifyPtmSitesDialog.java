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
package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.core.orm.msi.Ptm;
import fr.proline.studio.gui.DefaultDialog;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public class IdentifyPtmSitesDialog extends DefaultDialog {

  private List<Ptm> m_ptms;
  private List<Ptm> m_selectedPtms;

  private JComboBox<String> m_clusteringMethodCbx;

  public IdentifyPtmSitesDialog(Window parent, List<Ptm> ptms) {
    super(parent, Dialog.ModalityType.APPLICATION_MODAL);
    ptms.sort(Comparator.comparing(Ptm::getShortName));
    m_ptms = ptms;
    m_selectedPtms = new ArrayList<>(m_ptms);

    setTitle("Identify Ptm sites");
    setHelpHeaderText("Select the list of modifications of interest <br>" +
            "(other modifications will be ignored during clustering) and the name <br>" +
            "of the method that will be used to clusterize modification sites.</li></ul>");
    initInternalPanel();
    pack();
  }

  private void initInternalPanel() {

    JPanel parametersPanel = new JPanel();
    parametersPanel.setLayout(new java.awt.GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.NORTHWEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new java.awt.Insets(5, 5, 5, 5);

    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 1;

    JLabel label = new JLabel("Clustering method:");
    m_clusteringMethodCbx = new JComboBox<>(new String[] {"Exact Position Matching"});
    m_clusteringMethodCbx.setEnabled(false);
    
    parametersPanel.add(label, c);
    c.gridx++;
    parametersPanel.add(m_clusteringMethodCbx, c);

    c.gridx = 0;
    c.gridy++;
    label = new JLabel("PTMs of interest:");
    parametersPanel.add(label, c);

    for (Ptm ptm : m_ptms) {
      c.gridx = 1;
      c.gridy++;
      JCheckBox checkbox = new JCheckBox(ptm.getFullName(), true);
      checkbox.addActionListener(evt -> {
        if (((JCheckBox)evt.getSource()).isSelected()) {
          m_selectedPtms.add(ptm);
        } else {
          m_selectedPtms.remove(ptm);
        }
      });
      parametersPanel.add(checkbox, c);
    }

    JPanel internalPanel = new JPanel(new BorderLayout());
    internalPanel.add(parametersPanel, BorderLayout.CENTER);

    setInternalComponent(internalPanel);
  }

  public List<Long> getPtms() {
    return m_selectedPtms.stream().map(p -> p.getId()).collect(Collectors.toList());
  }

  public String getClusteringMethodName() {
    return m_clusteringMethodCbx.getItemAt(m_clusteringMethodCbx.getSelectedIndex()).toUpperCase().replaceAll(" ", "_");
  }

}
