package fr.proline.studio.rsmexplorer.gui;

import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.PeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.WindowManager;
import fr.proline.studio.dam.tasks.data.ptm.PTMCluster;
import fr.proline.studio.dam.tasks.data.ptm.PTMPeptideInstance;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.InfoDialog;
import fr.proline.studio.rsmexplorer.gui.dialog.ModifyClusterStatusPanel;
import fr.proline.studio.rsmexplorer.gui.model.PTMPeptidesTableModel;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.TablePopupMenu;
import fr.proline.studio.utils.IconManager;
import fr.proline.studio.utils.StudioResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EditClusterDialog extends DefaultDialog {

  private static final Logger LOG = LoggerFactory.getLogger("ProlineStudio.ResultExplorer.ptm");

  private PTMCluster m_editedCluster;
  private List<PTMPeptideInstance> m_ptmPeptideInstances;
  private List<PTMPeptideInstance> m_removedPeptideInstances;

  private ModifyClusterStatusPanel m_statusPanel;
  private JScrollPane m_ptmPeptidesScrollPane;
  private PTMPeptidesTable m_ptmPeptidesTable;
  private PTMPeptidesTableModel m_ptmPeptidesTableModel;

  private boolean m_peptidesDeleted;
  private boolean m_statusModified;

  public EditClusterDialog(PTMCluster cluster) {
    super(WindowManager.getDefault().getMainWindow());

    setTitle("Edit Cluster "+cluster.getId()+" ["+cluster.getProteinMatch().getAccession()+" / "+cluster.getRepresentativePepMatch().getPeptide().getSequence()+"]");
    setResizable(true);

    StringBuilder helpTextBuilder = new StringBuilder(StudioResourceBundle.getMessage(EditClusterDialog.class, "EditCluster.status.modif.html.help"));
    helpTextBuilder.append("<br><br>").append(StudioResourceBundle.getMessage(EditClusterDialog.class, "EditCluster.peptide.remove.html.help"));
    setHelpHeader(IconManager.getIcon(IconManager.IconType.INFORMATION),"Edit Cluster",helpTextBuilder.toString());

    m_editedCluster = cluster;
    m_ptmPeptideInstances = new ArrayList<>(cluster.getParentPTMPeptideInstances());
    initComponent();

    m_statusPanel.setData(m_editedCluster);
    m_ptmPeptidesTable.setData(m_ptmPeptideInstances);
    m_removedPeptideInstances = new ArrayList<>();
  }

  private void initComponent(){

    JPanel editPanel = new JPanel();
    editPanel.setLayout(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.NORTH;
    c.insets = new java.awt.Insets(2, 2, 2, 2);
    c.fill = GridBagConstraints.BOTH;
    c.gridx = 0;
    c.gridy = 0;


    JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    sp.setResizeWeight(0.5);

    m_statusPanel = new ModifyClusterStatusPanel();
    m_statusPanel.setBorder(new TitledBorder(" Cluster Status "));
    sp.setTopComponent(m_statusPanel);

    JPanel peptidePanel = createCLusterPeptidesPanel();
    sp.setBottomComponent(peptidePanel);

    c.weightx = 1;
    c.weighty = 1;

    editPanel.add(sp, c);

    setInternalComponent(editPanel);
  }

  private JPanel createCLusterPeptidesPanel() {
    JPanel peptidePanel = new JPanel();
    peptidePanel.setBorder(new TitledBorder(" Cluster Peptides "));
    peptidePanel.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.NORTHWEST;
    c.fill = GridBagConstraints.BOTH;
    c.gridx = 0;
    c.gridy = 0;

    JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
    toolbar.setFloatable(false);
    JButton removePeptideButton = new JButton();
    removePeptideButton.setIcon(IconManager.getIcon(IconManager.IconType.CANCEL));
    removePeptideButton.setToolTipText("Remove selected Peptides from Cluster");
    removePeptideButton.addActionListener(e -> {
      if (m_ptmPeptidesTable.getSelectedRowCount() == 1) {
        removeSelectedPeptide();
      } else {
        InfoDialog err = new InfoDialog(WindowManager.getDefault().getMainWindow(),InfoDialog.InfoType.INFO, "Remove Peptide" ,"At least one, and only one, peptide should be selected.");
        err.setButtonVisible(InfoDialog.BUTTON_CANCEL, false);
        err.setButtonName(InfoDialog.BUTTON_OK, "OK");
        err.centerToWindow(WindowManager.getDefault().getMainWindow());
        err.setVisible(true);
      }
    });
    toolbar.add(removePeptideButton);

    JPanel ptmPeptidesPanel = createPtmPeptidesPanel();

    peptidePanel.add(toolbar, c);

    c.gridx++;
    c.weightx = 1;
    c.weighty = 1;
    peptidePanel.add(ptmPeptidesPanel, c);

    return peptidePanel;
  }

  private JPanel createPtmPeptidesPanel() {
    JPanel ptmPeptidesPanel = new JPanel();
    ptmPeptidesPanel.setLayout(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.NORTH;
    //c.insets = new java.awt.Insets(2, 2, 2, 2);
    c.fill = GridBagConstraints.BOTH;
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 1;
    c.weighty = 1;

    // create objects
    m_ptmPeptidesScrollPane = new JScrollPane();

    m_ptmPeptidesTable = new PTMPeptidesTable();
    m_ptmPeptidesTableModel = new PTMPeptidesTableModel(m_ptmPeptidesTable, false, false);
    m_ptmPeptidesTable.setModel(new CompoundTableModel(m_ptmPeptidesTableModel, true));

    TableRowSorter<TableModel> sorter = new TableRowSorter<>(m_ptmPeptidesTable.getModel());
    m_ptmPeptidesTable.setRowSorter(sorter);

    m_ptmPeptidesScrollPane.setViewportView(m_ptmPeptidesTable);
    m_ptmPeptidesTable.setFillsViewportHeight(true);
    m_ptmPeptidesTable.setViewport(m_ptmPeptidesScrollPane.getViewport());


    ptmPeptidesPanel.add(m_ptmPeptidesScrollPane, c);

    return ptmPeptidesPanel;
  }

  private void removeSelectedPeptide(){
    ArrayList<PTMCluster> clusters = new ArrayList<>();
    clusters.add(m_editedCluster);
    PTMPeptideInstance ptmPep = getSelectedPTMPeptideInstance();
    DPeptideMatch repPepMatch = ptmPep.getRepresentativePepMatch(clusters);
    DPeptideMatch clusterPepMatch = m_editedCluster.getRepresentativePepMatch();
    if(repPepMatch != null && clusterPepMatch.getId() == repPepMatch.getId()){
      InfoDialog id = new InfoDialog(WindowManager.getDefault().getMainWindow(), InfoDialog.InfoType.WARNING, "Remove Peptide Error", "Can not remove representative peptide from cluster" );
      id.setButtonName(DefaultDialog.BUTTON_OK, "OK");
      id.setButtonVisible(DefaultDialog.BUTTON_CANCEL, false);
      id.centerToWindow(WindowManager.getDefault().getMainWindow());
      id.setVisible(true);
      return;
    }
//    m_editedCluster.removePeptide(ptmPep);
    m_removedPeptideInstances.add(ptmPep);
    m_ptmPeptideInstances.remove(ptmPep);
    m_peptidesDeleted = true;
    m_ptmPeptidesTable.setData(m_ptmPeptideInstances);

    LOG.debug(" DELETE ptm peptide "+ptmPep.getPeptideInstance().getId());
  }

  private PTMPeptideInstance getSelectedPTMPeptideInstance() {
    if (m_ptmPeptidesTableModel.getRowCount() <= 0) {
      return null;
    }
    PTMPeptidesTableModel.Row row = m_ptmPeptidesTableModel.getPTMPeptideInstanceAt(getSelectedRowInTableModel());
    return row != null ? row.ptmPeptideInstance : null;
  }

  private int getSelectedRowInTableModel() {

    // Retrieve Selected Row
    int selectedRow = m_ptmPeptidesTable.getSelectedRow();

    // nothing selected
    if (selectedRow == -1) {
      return -1;
    }

    CompoundTableModel compoundTableModel = ((CompoundTableModel) m_ptmPeptidesTable.getModel());
    // convert according to the sorting
    selectedRow = m_ptmPeptidesTable.convertRowIndexToModel(selectedRow);
    //find the original index
    selectedRow = compoundTableModel.convertCompoundRowToBaseModelRow(selectedRow);
    return selectedRow;
  }

  @Override
  protected boolean okCalled() {
    m_statusModified = m_statusPanel.applyModifiedStatus();
    return true;
  }

  protected boolean isClusterModified() {
    return  m_statusModified || m_peptidesDeleted;
  }

  protected boolean isStatusModified() {
    return  m_statusModified;
  }

  protected boolean isPeptideDeleted() {
    return  m_peptidesDeleted;
  }

  protected List<PTMPeptideInstance> getRemovedPeptideInstances(){
    return m_removedPeptideInstances;
  }

  private class PTMPeptidesTable extends LazyTable  {

    public PTMPeptidesTable() {
      super(m_ptmPeptidesScrollPane.getVerticalScrollBar());
    }

    @Override
    public TablePopupMenu initPopupMenu() {
      return null;
    }

    @Override
    public void prepostPopupMenu() {

    }

    /**
     * Called whenever the value of the selection changes.
     *
     * @param e the event that characterizes the change.
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
      super.valueChanged(e);
    }

    @Override
    public void addTableModelListener(TableModelListener l) {

    }

    @Override
    public boolean isLoaded() {
      return true;
    }

    @Override
    public int getLoadingPercentage() {
      return 100;
    }

    public void setData(List<PTMPeptideInstance> m_ptmPeptideInstances) {

      List<PTMCluster> clusters = new ArrayList<>();
      clusters.add(m_editedCluster);
      ((PTMPeptidesTableModel) (((CompoundTableModel) getModel()).getBaseModel())).setData(null, new ArrayList<>(m_ptmPeptideInstances),clusters,null);
      //fireTableDataChanged();
    }
  }


}
