package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.core.orm.msi.Ptm;
import fr.proline.core.orm.msi.PtmSpecificity;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.gui.DefaultDialog;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public class IdentifyPtmSitesDialog extends DefaultDialog {

  DDataset m_dataset;
  List<Ptm> m_ptms;
  List<Ptm> m_selectedPtms;

  private JComboBox<String> m_clusteringMethodCbx;

  public IdentifyPtmSitesDialog(Window parent, DDataset dataset, List<Ptm> ptms) {
    super(parent, Dialog.ModalityType.APPLICATION_MODAL);
    m_dataset = dataset;
    ptms.sort(Comparator.comparing(Ptm::getShortName));
    m_ptms = ptms;
    m_selectedPtms = new ArrayList<>(m_ptms);

    setTitle("Identify Ptm sites");
    initInternalPanel();
    pack();
  }

  private void initInternalPanel() {
    JPanel internalPanel = new JPanel();
    internalPanel.setLayout(new java.awt.GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.NORTHWEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new java.awt.Insets(5, 5, 5, 5);

    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 1;

    JLabel label = new JLabel("Clustering method:");
    m_clusteringMethodCbx = new JComboBox<>(new String[] {"dummy", "draft"});
    internalPanel.add(label, c);
    c.gridx++;
    internalPanel.add(m_clusteringMethodCbx, c);

    c.gridx = 0;
    c.gridy++;
    label = new JLabel("PTMs of interest:");
    internalPanel.add(label, c);

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
      internalPanel.add(checkbox, c);
    }

    JPanel marginPanel = new JPanel();
    marginPanel.setLayout(new GridBagLayout());
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new java.awt.Insets(15, 15, 15, 15);
    c.gridx = 0;
    c.gridy = 0;
    marginPanel.add(internalPanel, c);
    setInternalComponent(marginPanel);
  }


  public String getServiceVersion() {
    return "2.0";
  }

  public List<Long> getPtms() {
    return m_selectedPtms.stream().map(p -> p.getId()).collect(Collectors.toList());
  }

  public String getClusteringMethodName() {
    return "dummy";
  }
}
