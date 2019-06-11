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

  List<Ptm> m_ptms;
  List<Ptm> m_selectedPtms;

  private JComboBox<String> m_clusteringMethodCbx;
  private JComboBox<String> m_serviceVersionCbx;

  public IdentifyPtmSitesDialog(Window parent, List<Ptm> ptms) {
    super(parent, Dialog.ModalityType.APPLICATION_MODAL);
    ptms.sort(Comparator.comparing(Ptm::getShortName));
    m_ptms = ptms;
    m_selectedPtms = new ArrayList<>(m_ptms);

    setTitle("Identify Ptm sites");
    setHelpHeaderText("Select the Ptm identification service: v1.0 generates a list of all identified <br> sites while v2.0 generates a Ptm dataset in which sites are clusterized.<br>"+
            " <ul><li>v2.0 parameters: Select the list of modifications of interest <br>" +
            "(other modifications will be ignored during clustering) and the name <br>" +
            "of the method that will be used to clusterize modification sites.</li></ul>");
    initInternalPanel();
    pack();
  }

  private void initInternalPanel() {

    JPanel parametersV2Panel = new JPanel();
    parametersV2Panel.setLayout(new java.awt.GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.NORTHWEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new java.awt.Insets(5, 5, 5, 5);

    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 1;

    JLabel label = new JLabel("Clustering method:");
    m_clusteringMethodCbx = new JComboBox<>(new String[] {"draft"});
    m_clusteringMethodCbx.setEnabled(false);
    
    parametersV2Panel.add(label, c);
    c.gridx++;
    parametersV2Panel.add(m_clusteringMethodCbx, c);

    c.gridx = 0;
    c.gridy++;
    label = new JLabel("PTMs of interest:");
    parametersV2Panel.add(label, c);

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
      parametersV2Panel.add(checkbox, c);
    }

    final JPanel parametersPanel = new JPanel(new CardLayout());

    JPanel headerPanel = new JPanel(new GridBagLayout());
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new java.awt.Insets(15, 15, 15, 15);
    c.gridx = 0;
    c.gridy = 0;

    String[] versions = new String[] {"Ptm Sites (v1.0)", "Ptm Dataset (v2.0)"};
    m_serviceVersionCbx = new JComboBox(versions);
    m_serviceVersionCbx.addItemListener( evt -> {
      CardLayout cl = (CardLayout) (parametersPanel.getLayout());
      cl.show(parametersPanel, (String) evt.getItem());
    });

    label = new JLabel("Service version:");
    headerPanel.add(label, c);
    c.gridx++;
    headerPanel.add(m_serviceVersionCbx, c);

    parametersPanel.add(new JPanel(), versions[0]);
    parametersPanel.add(parametersV2Panel, versions[1]);

    JPanel internalPanel = new JPanel(new BorderLayout());
    internalPanel.add(headerPanel, BorderLayout.NORTH);
    internalPanel.add(parametersPanel, BorderLayout.CENTER);

    setInternalComponent(internalPanel);
  }


  public String getServiceVersion() {
    return m_serviceVersionCbx.getItemAt(m_serviceVersionCbx.getSelectedIndex()).contains("2.0") ? "2.0" : "1.0";
  }

  public List<Long> getPtms() {
    return m_selectedPtms.stream().map(p -> p.getId()).collect(Collectors.toList());
  }

  public String getClusteringMethodName() {
    return "draft";
  }
}
