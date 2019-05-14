package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.gui.DefaultDialog;

import java.awt.Dialog;
import java.awt.Window;
import java.util.Arrays;
import java.util.List;


public class IdentifyPtmSitesDialog extends DefaultDialog {

  DDataset m_dataset;

  public IdentifyPtmSitesDialog(Window parent, DDataset dataset) {
    super(parent, Dialog.ModalityType.APPLICATION_MODAL);
    m_dataset = dataset;
  }


  public String getServiceVersion() {
    return "2.0";
  }

  public List<Long> getPtms() {
    return Arrays.asList(16L);
  }

  public String getClusteringMethodName() {
    return "dummy";
  }
}
