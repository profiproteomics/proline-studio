/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.table;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.JXTable;

/**
 * to test the scrollable control column
 * @author MB243701
 */
public class TableTest extends JFrame{
    
    public TableTest() {
        super("Table test demo");
        init();
    }

    private void init() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JXTable table = new JXTable();
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        CustomTableModel tableModel = new CustomTableModel();
        table.setModel(tableModel);
        CustomColumnControlButton customColumnControl = new CustomColumnControlButton(table);
        table.setColumnControl(customColumnControl);
        table.setColumnControlVisible(true);
        JScrollPane scrollPane = new JScrollPane(table);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);
        pack();
        setSize(450, 350);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                TableTest tableF = new TableTest();
                tableF.setVisible(true);
            }
        });
     }
}

class CustomTableModel implements TableModel{
    private List<String> columns = new ArrayList();
    private int nbR = 10;
    private int nbC = 25;
    
    public CustomTableModel(){
        super();
        for(int i=0; i<nbC; i++){
            columns.add("Column "+(i+1));
        }
    }
            
    @Override
    public int getRowCount() {
        return nbR;
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columns.get(columnIndex);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return "cell " + rowIndex + " " + columnIndex;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }

    @Override
    public void addTableModelListener(TableModelListener l) {

    }

    @Override
    public void removeTableModelListener(TableModelListener l) {

    }
}
