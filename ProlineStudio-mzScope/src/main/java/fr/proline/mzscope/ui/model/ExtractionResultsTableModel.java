/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.ui.model;

import fr.proline.mzscope.model.ExtractionResult;
import fr.proline.studio.table.DecoratedTableModel;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.TableCellRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
public class ExtractionResultsTableModel extends DecoratedTableModel {

   final private static Logger logger = LoggerFactory.getLogger(ExtractionResultsTableModel.class);

   public enum Column {

      MZ("mz", "m/z extration value"),
      STATUS("status", "Extraction status : REQUESTED or DONE");

      private final String name;
      private final String tooltip;

      private Column(String name, String tooltip) {
         this.name = name;
         this.tooltip = tooltip;
      }

      public String getName() {
         return name;
      }

      public String getTooltip() {
         return tooltip;
      }

   }

   private List<ExtractionResult> extractionResults = new ArrayList<>(0);

   public void setExtractions(List<ExtractionResult> extractionResults) {
      this.extractionResults = extractionResults;
      fireTableDataChanged();
   }

   @Override
   public int getRowCount() {
      return extractionResults.size();
   }

   @Override
   public int getColumnCount() {
      return Column.values().length;
   }

   @Override
   public String getColumnName(int column) {
      return Column.values()[column].getName(); 
   }

   
   @Override
   public Object getValueAt(int rowIndex, int columnIndex) {
      switch (Column.values()[columnIndex]) {
         case MZ:
            return extractionResults.get(rowIndex).getMz();
         case STATUS:
            return extractionResults.get(rowIndex).getStatus().ordinal();
      }
      return null;
   }

   @Override
   public String getToolTipForHeader(int columnIndex) {
      return Column.values()[columnIndex].getTooltip();
   }

   @Override
   public String getTootlTipValue(int row, int col) {
      return null;
   }

   @Override
   public TableCellRenderer getRenderer(int col) {
      return null;
   }

}
