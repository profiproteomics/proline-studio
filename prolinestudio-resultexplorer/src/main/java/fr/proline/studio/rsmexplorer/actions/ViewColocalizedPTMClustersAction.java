package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.dam.tasks.data.ptm.PTMCluster;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.filter.FilterTableModelInterface;
import fr.proline.studio.rsmexplorer.gui.model.PTMClusterTableModel;
import fr.proline.studio.table.AbstractTableAction;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.util.HashSet;
import java.util.List;

public class ViewColocalizedPTMClustersAction extends AbstractTableAction {


  public ViewColocalizedPTMClustersAction() {
    super("View Co-localized Clusters");
  }

  @Override
  public void actionPerformed(int col, int row, int[] selectedRows, JTable table) {

    TableModel tableModel = table.getModel();
    if(!(tableModel instanceof CompoundTableModel && ((CompoundTableModel)tableModel).getBaseModel() instanceof PTMClusterTableModel)){
      return;
    }

    PTMClusterTableModel ptmTbModel = (PTMClusterTableModel) ((CompoundTableModel)tableModel).getBaseModel();

    //Get index in inital model (no sorting/filtering)
    int selectFilteredModelRow = table.convertRowIndexToModel(selectedRows[0]);
    int selectClusterModelRow = ((FilterTableModelInterface) tableModel).convertRowToOriginalModel(selectFilteredModelRow);

    //Get PTMCLuster at specified index
    PTMCluster cluster = (PTMCluster) ptmTbModel.getRowValue(PTMCluster.class, selectClusterModelRow);

    //Get PTMCluster colocalized clusters and filter table with these clusters only
    if(cluster != null){
      List<PTMCluster> colocClusters = cluster.getPTMDataset().getColocatedClusters(cluster);
      HashSet<Integer> restrainRowSet = ((FilterTableModelInterface) tableModel).getRestrainRowSet();
      if (restrainRowSet == null) {
        restrainRowSet = new HashSet<>();
      } else {
        restrainRowSet.clear();
      }

      //Reset filter to access all rows
      ((FilterTableModelInterface) tableModel).restrain(restrainRowSet);

      for (PTMCluster colocCl : colocClusters) {

        //index in initial model
        int modelIndex = ptmTbModel.getModelIndexFor(colocCl);
        // index in view
        int originalModelRow = ((FilterTableModelInterface) tableModel).convertRowToOriginalModel(modelIndex);
        restrainRowSet.add(originalModelRow);
      }

      ((FilterTableModelInterface) tableModel).restrain(restrainRowSet);
    }
  }

  @Override
  public void updateEnabled(int row, int col, int[] selectedRows, JTable table) {
    setEnabled((selectedRows!=null) && (selectedRows.length==1));
  }
}
