package fr.proline.studio.table;

import fr.proline.studio.export.ExportTextInterface;
import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author JM235353
 */
public class TablePopupMenu extends JPopupMenu {

    public static class CopyCellAction extends AbstractTableAction {

        public CopyCellAction() {
            super("Copy cell");
        }

        @Override
        public void actionPerformed(int col, int row, int[] selectedRows, JTable table) {
            TableCellRenderer renderer = table.getCellRenderer(row, col);
            Component c = table.prepareRenderer(renderer, row, col);
            String text = componentToText(c);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(text), null);
        }

        @Override
        public void updateEnabled(int row, int col, int[] selectedRows, JTable table) {
            setEnabled(row != -1);
        }

        private String componentToText(Component c) {
            if (c instanceof ExportTextInterface) {
                return ((ExportTextInterface) c).getExportText();
            } else if (c instanceof JLabel) {
                return ((JLabel) c).getText();
            } else if (c instanceof AbstractButton) {
                return ((AbstractButton) c).getText();
            }
            return "";
        }
    }
   
    public static class SelectAllAction extends AbstractTableAction {

        public SelectAllAction() {
            super("Select All");
        }

        @Override
        public void actionPerformed(int col, int row, int[] selectedRows, JTable table) {
            table.selectAll();
        }

        @Override
        public void updateEnabled(int row, int col, int[] selectedRows, JTable table) {
            setEnabled(true);
        }

    }

   private ArrayList<AbstractTableAction> m_actions = new ArrayList<>();

   private int m_row;
   private int m_col;
   private int[] m_selectedRows;
   private JTable m_table;

   public TablePopupMenu(boolean setDefaultActions) {
      if (setDefaultActions) {
         addAction(new CopyCellAction());
         addAction(new SelectAllAction());
         addAction(null);
      }
   }

   public TablePopupMenu() {
      this(true);
   }

   public void addAction(AbstractTableAction action) {
      m_actions.add(action);
      if (action != null) {
         action.setTablePopupMenu(this);
      }
   }

   public int getRow() {
      return m_row;
   }

   public int getCol() {
      return m_col;
   }

   public int[] getSelectedRows() {
      return m_selectedRows;
   }

   public JTable getTable() {
      return m_table;
   }

   public void preparePopup() {

      int nbActions = m_actions.size();
      for (int i = 0; i < nbActions; i++) {
         AbstractTableAction action = m_actions.get(i);
         if (action == null) {
            addSeparator();
         } else {
            add(action.getPopupPresenter());
         }
      }

   }

   public void show(int mouseX, int mouseY, JTable table) {

      Point mousePoint = new Point(mouseX, mouseY);

      m_table = table;
      m_selectedRows = table.getSelectedRows();
      m_row = table.rowAtPoint(mousePoint);
      m_col = table.columnAtPoint(mousePoint);

      // update actions
      int nbActions = m_actions.size();
      for (int i = 0; i < nbActions; i++) {
         AbstractTableAction action = m_actions.get(i);
         if (action != null) {
            action.updateEnabled(m_row, m_col, m_selectedRows, table);
         }
      }

      show(table, mouseX, mouseY);

   }

}
