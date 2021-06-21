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
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


public class IdentifyPtmSitesDialog extends DefaultDialog {

  private final List<Ptm> m_ptms;
  private List<Ptm> m_selectedPtms;
  private HashMap<String, Ptm> m_ptmsByFullName;
  private JLabel m_clusteringMethod;
  private JRadioButton m_clusterizePartiallyIsomorph;
  private JRadioButton m_clusterizeIsomorphOnly;

  public IdentifyPtmSitesDialog(Window parent, List<Ptm> ptms) {
    super(parent, Dialog.ModalityType.APPLICATION_MODAL);
    ptms.sort(Comparator.comparing(Ptm::getShortName));
    m_ptms = ptms;
    m_selectedPtms = new ArrayList<>(m_ptms);
      setDocumentationSuffix("h.2wwbldi");
    setTitle("Identify Ptm sites");
    setHelpHeaderText("Select the list of modifications of interest <br>" +
            "(other modifications will be ignored during clustering) and set <br>" +
            "the method's parameters that will be used to clusterize modification sites.</li></ul>");

    initPTMsList();
    initInternalPanel();
    //pack();
    setResizable(true);
  }

  private void initPTMsList(){
    m_selectedPtms = new ArrayList<>(m_ptms);
    m_ptmsByFullName = new HashMap<>();
    m_ptms.forEach((nextPtm) -> {
        m_ptmsByFullName.put(nextPtm.getFullName(),nextPtm);
      });
  }

  private void initInternalPanel() {

    JPanel clusteringMethodPanel = new JPanel(new GridBagLayout());
    clusteringMethodPanel.setBorder(BorderFactory.createTitledBorder("Clustering method"));
    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 1.0;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new java.awt.Insets(5, 5, 5, 5);

    m_clusteringMethod = new JLabel("<html><b>Rely on localizations inferred from PSMs</b>: this clustering method <br/>" +
            "use the site localization inferred from the validated PSMs, regardless <br/>of the reported localization probabilities.</html>");
    clusteringMethodPanel.add(m_clusteringMethod, c);

    m_clusterizePartiallyIsomorph = new JRadioButton("Group fully cleaved with missed cleaved peptides in same cluster");
    c.gridy++;
    c.insets = new java.awt.Insets(5, 15, 5, 5);
    clusteringMethodPanel.add(m_clusterizePartiallyIsomorph, c);

    m_clusterizeIsomorphOnly = new JRadioButton("Separate fully cleaved and missed cleaved peptides in different clusters");
    c.gridy++;
    c.insets = new java.awt.Insets(5, 15, 5, 5);
    clusteringMethodPanel.add(m_clusterizeIsomorphOnly, c);

    ButtonGroup group = new ButtonGroup();
    group.add(m_clusterizePartiallyIsomorph);
    group.add(m_clusterizeIsomorphOnly);

    m_clusterizePartiallyIsomorph.setSelected(true);

    JPanel parametersPanel = new JPanel();
    parametersPanel.setLayout(new java.awt.GridBagLayout());

    JPanel ptmOIPanel = new JPanel(new GridBagLayout());
    //ptmOIPanel.setBorder(BorderFactory.createTitledBorder("PTMs of interest"));
    c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 1.0;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new java.awt.Insets(5, 15, 5, 5);

    final JCheckBox allSelectedChkBox = new JCheckBox("select/unselect all",true);
    allSelectedChkBox.addActionListener(evt -> {
        boolean isSelectAll = ((JCheckBox) evt.getSource()).isSelected();
        if (ptmOIPanel.getComponentCount() > 0) {
            Component[] allCompo = ptmOIPanel.getComponents();
            for (int i = 0; i < allCompo.length; i++) {
                if (JCheckBox.class.isInstance(allCompo[i])) {
                    String fullName = ((JCheckBox) allCompo[i]).getText();
                    ((JCheckBox) allCompo[i]).setSelected(isSelectAll);
                    Ptm ptm = m_ptmsByFullName.get(fullName);
                    setPTMSelectStatus(ptm, isSelectAll);
                }
            }
        }
    });
    ptmOIPanel.add(allSelectedChkBox, c);
    c.gridx++;

    for (Ptm ptm : m_ptms) {
      JCheckBox checkbox = new JCheckBox(ptm.getFullName(), true);
      checkbox.addActionListener(evt -> {
        setPTMSelectStatus(ptm, ((JCheckBox)evt.getSource()).isSelected());
      });
      ptmOIPanel.add(checkbox, c);
      c.gridy++;
    }

    JScrollPane m_scrollPane = new JScrollPane();
    m_scrollPane.setBorder(BorderFactory.createTitledBorder("PTMs of interest"));
    m_scrollPane.setViewportView(ptmOIPanel);
    m_scrollPane.createVerticalScrollBar();

    c = new GridBagConstraints();
    c.anchor = GridBagConstraints.NORTHWEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new java.awt.Insets(5, 5, 5, 5);
    c.gridx = 0;
    c.gridy = 0;

    parametersPanel.add(clusteringMethodPanel, c);
    c.gridy++;
    c.weighty=1;
    c.fill = GridBagConstraints.BOTH;
    parametersPanel.add(m_scrollPane, c);

     setInternalComponent(parametersPanel);
  }

  private void setPTMSelectStatus(Ptm ptm, boolean selectStatus){
    if (selectStatus) {
        m_selectedPtms.add(ptm);
    } else {
        m_selectedPtms.remove(ptm);
    }
  }

  public List<Long> getPtms() {
    return m_selectedPtms.stream().map(p -> p.getId()).collect(Collectors.toList());
  }

  public String getClusteringMethodName() {
//    return m_clusteringMethodCbx.getItemAt(m_clusteringMethodCbx.getSelectedIndex()).toUpperCase().replaceAll(" ", "_");
    return (m_clusterizeIsomorphOnly.isSelected()) ? "ISOMORPHIC_MATCHING" : "EXACT_POSITION_MATCHING";
  }
}
