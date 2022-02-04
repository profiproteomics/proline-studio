package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.dam.data.SelectLevelEnum;
import fr.proline.studio.dam.tasks.data.ptm.PTMCluster;
import fr.proline.studio.rsmexplorer.gui.SelectLevelRadioButtonGroup;
import fr.proline.studio.utils.IconManager;

import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.ParseException;

public class ModifyClusterStatusPanel extends JPanel {

  protected static final String CMD_VALIDATED = "Validated";
  protected static final String CMD_INVALIDATED = "Invalidated";

  private SelectLevelRadioButtonGroup m_validRButton;
  private SelectLevelRadioButtonGroup m_invalidRButton;
  private ButtonGroup m_statusButtonGroup;
  private JFormattedTextField m_statusConfidenceLevelTF;
  private JTextArea m_statusConfidenceInfoTF;
  private PTMCluster m_editedCluster;

  public ModifyClusterStatusPanel() {
    initComponent();
  }

  private void initComponent() {
    setLayout(new BorderLayout());
    JPanel p = new JPanel(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.NORTHWEST;
    c.insets = new java.awt.Insets(10, 10, 2, 5);
    c.fill = GridBagConstraints.BOTH;
    c.gridx = 0;
    c.gridy = 0;

    JLabel statusTextLabel = new JLabel("Modification Cluster Status:");
    p.add(statusTextLabel, c);

    c.gridy++;
    c.gridx++;
    c.fill = GridBagConstraints.NONE;
    c.insets = new java.awt.Insets(2, 2, 2, 2);
    m_validRButton = new SelectLevelRadioButtonGroup(p, c, CMD_VALIDATED, IconManager.getIcon(IconManager.IconType.VALIDATED));
    m_validRButton.getRadioButton().setActionCommand(CMD_VALIDATED);

    c.gridy++;
    c.gridx = 1;
    m_invalidRButton = new SelectLevelRadioButtonGroup(p, c, CMD_INVALIDATED,IconManager.getIcon(IconManager.IconType.INVALIDATED));
    m_invalidRButton.getRadioButton().setActionCommand(CMD_INVALIDATED);

    m_statusButtonGroup = new ButtonGroup();
    m_statusButtonGroup.add(m_validRButton.getRadioButton());
    m_statusButtonGroup.add(m_invalidRButton.getRadioButton());
    c.fill = GridBagConstraints.BOTH;
    c.gridy++;
    c.gridx = 0;
    c.weightx=0.2;
    c.insets = new java.awt.Insets(5, 10, 2, 5);
    JLabel statConfidenceLabel = new JLabel("Status Confidence level:");
    p.add(statConfidenceLabel, c);

    c.gridx++;
    c.gridwidth = 2;
    c.weightx=0.8;

    m_statusConfidenceLevelTF = new JFormattedTextField();
    m_statusConfidenceLevelTF.setFormatterFactory(new DefaultFormatterFactory(new NumberAndNullFormatter()));
    m_statusConfidenceLevelTF.setColumns(10);
    p.add(m_statusConfidenceLevelTF, c);

    c.gridy++;
    c.gridx = 0;
    c.weightx=0.2;
    JLabel statConfidenceInfo = new JLabel("Status Confidence description:");
    p.add(statConfidenceInfo, c);

    c.gridx++;
    c.weightx=0.8;
    m_statusConfidenceInfoTF = new JTextArea(5,20);
    p.add(m_statusConfidenceInfoTF, c);
    add(p, BorderLayout.CENTER);
  }

  /**
   * Apply Modification to currently displayed cluster
   *
   * @return true is modification has been done.
   */
  public boolean applyModifiedStatus(){

    String command = m_statusButtonGroup.getSelection().getActionCommand();
    boolean changedDone = false;

    if( command.equals(CMD_VALIDATED) && m_editedCluster.getSelectionLevel()<2) {
      m_editedCluster.setSelectionLevel(3);
      changedDone = true;
    }

    if(command.equals(CMD_INVALIDATED) && m_editedCluster.getSelectionLevel()>=2) {
      m_editedCluster.setSelectionLevel(0);
      changedDone = true;
    }

    //get confidence parameters values
    String confidenceDesc = m_statusConfidenceInfoTF.getText();
    Integer confidenceNotation = (m_statusConfidenceLevelTF.getValue() == null || (m_statusConfidenceLevelTF.getValue().toString().isEmpty()) ) ? null : new Integer(m_statusConfidenceLevelTF.getValue().toString());

    if( (confidenceDesc != null && !confidenceDesc.equals(m_editedCluster.getSelectionInfo())) || (confidenceDesc == null &&  m_editedCluster.getSelectionInfo() !=null))  {
      m_editedCluster.setSelectionInfo(confidenceDesc);
        changedDone = true;
    }

    if( (confidenceNotation != null && !confidenceNotation.equals(m_editedCluster.getSelectionNotation())) || (confidenceNotation == null &&  m_editedCluster.getSelectionNotation() !=null)) {
      m_editedCluster.setSelectionNotation(confidenceNotation);
      changedDone = true;
    }

    return changedDone;
  }

  public void setData(PTMCluster clusterToModify) {

    m_editedCluster = clusterToModify;
    int clusterSelectionLevel = clusterToModify.getSelectionLevel();
    Integer clusterNotation = clusterToModify.getSelectionNotation();
    String clusterDescription= clusterToModify.getSelectionInfo();

    //Set selection level in Panel
    SelectLevelEnum val =SelectLevelEnum.valueOf(clusterSelectionLevel);
    switch (val){
      case RESET_AUTO:
      case UNKNOWN:
        break;
      case DESELECTED_AUTO:
      case DESELECTED_MANUAL:
        m_invalidRButton.getRadioButton().setSelected( true);
        break;

      case SELECTED_AUTO:
      case SELECTED_MANUAL:
        m_validRButton.getRadioButton().setSelected( true);
        break;
    }

    //Set description if common exist
      m_statusConfidenceInfoTF.setText(clusterDescription);

    //Set confidence notation if common exist
      m_statusConfidenceLevelTF.setValue(clusterNotation);
  }

   private static class NumberAndNullFormatter extends NumberFormatter {
     @Override
     public Object stringToValue(String text) throws ParseException {
       if ( text.length() == 0 )
         return null;
       return super.stringToValue(text);
     }

   }


}
