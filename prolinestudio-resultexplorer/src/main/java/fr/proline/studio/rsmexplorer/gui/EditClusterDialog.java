package fr.proline.studio.rsmexplorer.gui;

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

  protected static final Logger LOG = LoggerFactory.getLogger("ProlineStudio.ResultExplorer.ptm");

  protected PTMCluster m_editedCluster;
  protected List<PTMPeptideInstance> m_ptmPeptideInstances;

  private ModifyClusterStatusPanel m_statusPanel;
  JScrollPane m_ptmPeptidesScrollPane;
  protected PTMPeptidesTable m_ptmPeptidesTable;
  protected PTMPeptidesTableModel m_ptmPeptidesTableModel;

  private boolean m_peptidesDeleted;
  private boolean m_statusModified;

  public EditClusterDialog(PTMCluster cluster) {
    super(WindowManager.getDefault().getMainWindow(), Dialog.ModalityType.APPLICATION_MODAL);
    setTitle("Edit Cluster "+cluster.getId()+" ["+cluster.getProteinMatch().getAccession()+" / "+cluster.getRepresentativePepMatch().getPeptide().getSequence()+"]");
    setResizable(true);

    m_editedCluster = cluster;
    m_ptmPeptideInstances = cluster.getParentPTMPeptideInstances();
    initComponent();

    m_statusPanel.setData(m_editedCluster);
    m_ptmPeptidesTable.setData(m_ptmPeptideInstances);
  }

  private void initComponent(){
    JPanel editPanel = new JPanel();
    editPanel.setLayout(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.NORTH;
    c.insets = new java.awt.Insets(10, 10, 2, 5);
    c.fill = GridBagConstraints.BOTH;
    c.gridx = 0;
    c.gridy = 0;
    m_statusPanel = new ModifyClusterStatusPanel();
    m_statusPanel.setBorder(new TitledBorder("Cluster Status "));
    editPanel.add(m_statusPanel,c);

    JPanel peptidePanel = new JPanel();
    peptidePanel.setBorder(new TitledBorder("Cluster Peptides "));
    peptidePanel.setLayout(new GridBagLayout());
    GridBagConstraints c1 = new GridBagConstraints();
    c1.anchor = GridBagConstraints.NORTHWEST;
    c1.insets = new java.awt.Insets(5, 5, 5, 5);
    c1.fill = GridBagConstraints.BOTH;
    c1.gridx = 0;
    c1.gridy = 0;
    c1.weightx=0.2;
    JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
    toolbar.setFloatable(false);
    JButton removePeptideButton = new JButton();
    removePeptideButton.setIcon(IconManager.getIcon(IconManager.IconType.CANCEL));
    removePeptideButton.setToolTipText("Remove selected Peptides from Cluster");
    removePeptideButton.addActionListener(e -> {
      if(m_ptmPeptidesTable.getSelectedRowCount() == 1) {
        removeSelectedPeptide();
      }
    });
    toolbar.add(removePeptideButton);
    peptidePanel.add(toolbar, c1);

    JPanel ptmPeptidesPanel = new JPanel();
    ptmPeptidesPanel.setBounds(0, 0, 500, 400);
    ptmPeptidesPanel.setLayout(new BorderLayout());

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
    ptmPeptidesPanel.add(m_ptmPeptidesScrollPane, BorderLayout.CENTER);

    c1.insets = new java.awt.Insets(2, 10, 2, 5);
    c1.gridwidth=1;
    c1.gridx++;
    c1.weightx=0.8;
    peptidePanel.add(ptmPeptidesPanel, c1);

    c.gridy++;
    editPanel.add(peptidePanel, c);

    setInternalComponent(editPanel);
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
      ((PTMPeptidesTableModel) (((CompoundTableModel) getModel()).getBaseModel())).setData(null, m_ptmPeptideInstances,clusters,null);
    }
  }


}
