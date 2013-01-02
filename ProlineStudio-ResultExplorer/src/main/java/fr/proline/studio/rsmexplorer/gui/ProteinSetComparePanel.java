package fr.proline.studio.rsmexplorer.gui;

import fr.proline.core.orm.msi.*;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import java.awt.*;
import javax.swing.*;
import fr.proline.studio.utils.CyclicColorPalette;
import fr.proline.studio.utils.DecoratedTable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import javax.swing.RowSorter.SortKey;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 *
 * @author JM235353
 */
public class ProteinSetComparePanel extends JPanel implements DataBoxPanelInterface {

    private ProteinSetCmpTable table;
    private AbstractDataBox dataBox;

    public ProteinSetComparePanel() {
        setLayout(new GridLayout());


        table = new ProteinSetCmpTable();
        JScrollPane scrollPane = new JScrollPane(table);


        add(scrollPane);

    }

    public ArrayList<ResultSummary> getResultSummaryList() {
        ProteinSetCmpTableModel tableModel = (ProteinSetCmpTableModel) table.getModel();
        return tableModel.getResultSummaryList();
    }

    public ResultSummary getFirstResultSummary() {
        ProteinSetCmpTableModel tableModel = (ProteinSetCmpTableModel) table.getModel();
        return tableModel.getResultSummary(0);
    }

    public ProteinMatch getProteinMatch(Integer resultSetId) {
        ProteinSetCmpTableModel tableModel = (ProteinSetCmpTableModel) table.getModel();
        return tableModel.getProteinMatch(resultSetId);
    }

    public ArrayList<ProteinMatch> getSelectedProteinMatchArray() {

        // Retrieve Selected Row
        int selectedRow = table.getSelectedRow();

        // nothing selected
        if (selectedRow == -1) {
            return null;
        }

        // convert according to the sorting
        selectedRow = table.convertRowIndexToModel(selectedRow);


        // Retrieve ProteinSet selected
        ProteinSetCmpTableModel tableModel = (ProteinSetCmpTableModel) table.getModel();

        return tableModel.getProteinMatchArray(selectedRow);
    }

    public ResultSet getFirstResultSet() {
        ProteinSetCmpTableModel tableModel = (ProteinSetCmpTableModel) table.getModel();
        ResultSummary rsm = tableModel.getResultSummary(0);
        if (rsm == null) {
            return null;
        }
        return rsm.getResultSet();
    }

    public void setData(ArrayList<ProteinMatch> proteinMatchArray, HashMap<Integer, ArrayList<Integer>> rsmIdMap) {

        //previouslyProteinSelected = null;

        ProteinSetCmpTableModel tableModel = ((ProteinSetCmpTableModel) table.getModel());
        tableModel.setData(proteinMatchArray, rsmIdMap);

        // select first data
        if (tableModel.getRowCount() > 0) {
            table.getSelectionModel().setSelectionInterval(0, 0);
        }
    }

    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        this.dataBox = dataBox;
    }

    public AbstractDataBox getDataBox() {
        return dataBox;
    }

    public class ProteinSetCmpTable extends DecoratedTable {

        public ProteinSetCmpTable() {
            setModel(new ProteinSetCmpTableModel());

            setFillsViewportHeight(true);

            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            setDefaultRenderer(ProteinStatus.class, new ProteinStatusRenderer());

            TableCellRenderer defaultRenderer = getTableHeader().getDefaultRenderer();
            
            getTableHeader().setDefaultRenderer(new RsmOrProteinHeaderRenderer(defaultRenderer));


        }

        /**
         * Called whenever the value of the selection changes.
         *
         * @param e the event that characterizes the change.
         */
        @Override
        public void valueChanged(ListSelectionEvent e) {

            super.valueChanged(e);

            dataBox.propagateDataChanged(ProteinMatch.class); //JPM.TODO

        }
    }

    public class ProteinSetCmpTableModel extends AbstractTableModel {

        private ArrayList<ProteinSet> proteinSetArray = null;
        private HashMap<String, ProteinStatus> proteinMatchNameStatusMap = null;
        private ArrayList<String> proteinNameList = null;
        private HashMap<Integer, ProteinMatch> srcProteinMatchMap = null;
        
        private HashMap<ProteinSet, HashMap<String, ProteinMatch>> proteinOfProteinSetsMap = null;
        
        // One column for protein name, other columns are for a ResultSummary
        public static final int COLTYPE_PROTEIN_NAME = 0;

        public void setData(ArrayList<ProteinMatch> proteinMatchSrcArray, HashMap<Integer, ArrayList<Integer>> rsmMap) {


            if ((proteinMatchSrcArray == null) || (proteinMatchSrcArray.isEmpty())) {
                fireTableStructureChanged();
                return;
            }

            if (proteinMatchNameStatusMap == null) {
                proteinMatchNameStatusMap = new HashMap<String, ProteinStatus>();
                proteinNameList = new ArrayList<String>();
                srcProteinMatchMap = new HashMap<Integer, ProteinMatch>();
                proteinOfProteinSetsMap = new HashMap<ProteinSet, HashMap<String, ProteinMatch>>();
            } else {
                proteinMatchNameStatusMap.clear();
                proteinNameList.clear();
                srcProteinMatchMap.clear();
                proteinOfProteinSetsMap.clear();
            }

            if (proteinMatchComparator == null) {
                proteinMatchComparator = new Comparator<String>() {

                    @Override
                    public int compare(String s1, String s2) {
                        ProteinStatus status1 = proteinMatchNameStatusMap.get(s1);
                        ProteinStatus status2 = proteinMatchNameStatusMap.get(s2);
                        return status2.compareTo(status1);
                    }
                };
            }

            if (proteinSetArray == null) {
                proteinSetArray = new ArrayList<ProteinSet>();
            } else {
                proteinSetArray.clear();
            }

            int nbProteinMatch = proteinMatchSrcArray.size();
            for (int protIndex = 0; protIndex < nbProteinMatch; protIndex++) {

                ProteinMatch proteinMatch = proteinMatchSrcArray.get(protIndex);

                srcProteinMatchMap.put(proteinMatch.getResultSet().getId(), proteinMatch);

                ProteinSet[] proteinSetArrayCur = proteinMatch.getTransientData().getProteinSetArray();

                
                
                // retrieve all proteins
                int nbProteinSets = proteinSetArrayCur.length;
                for (int i = 0; i < nbProteinSets; i++) {
                    ProteinSet pset = proteinSetArrayCur[i];
                    ResultSummary rsm = pset.getResultSummary();
                    
                    HashMap<String, ProteinMatch> proteinNameToProteinMatchOfProteinSetMap = new HashMap<String, ProteinMatch>();
                    proteinOfProteinSetsMap.put(pset, proteinNameToProteinMatchOfProteinSetMap);
                    
                    // if rsmMap != null, the user has selected resultSummary to display
                    if (rsmMap != null) {
                        ArrayList<Integer> acceptedRsmList = rsmMap.get(proteinMatch.getResultSet().getId() );

                        if (!acceptedRsmList.contains(rsm.getId() )) {
                            continue;
                        }
                    }

                    proteinSetArray.add(pset);

                    ProteinMatch[] sameSet = pset.getTransientData().getSameSet();
                    ProteinMatch[] subSet = pset.getTransientData().getSubSet();
                    


                    int sameSetLength = sameSet.length;
                    for (int j = 0; j < sameSetLength; j++) {
                        ProteinMatch pm = sameSet[j];
                        String proteinMatchName = pm.getAccession();
                        proteinNameToProteinMatchOfProteinSetMap.put(proteinMatchName, pm);
                        ProteinStatus status = proteinMatchNameStatusMap.get(proteinMatchName);
                        if (status == null) {
                            status = new ProteinStatus();
                            proteinMatchNameStatusMap.put(proteinMatchName, status);
                            proteinNameList.add(proteinMatchName);
                        }
                        status.inSameSet(rsm);

                    }

                    int subSetLength = subSet.length;
                    for (int j = 0; j < subSetLength; j++) {
                        ProteinMatch pm = subSet[j];
                        String proteinMatchName = pm.getAccession();
                        proteinNameToProteinMatchOfProteinSetMap.put(proteinMatchName, pm);
                        ProteinStatus status = proteinMatchNameStatusMap.get(proteinMatchName);
                        if (status == null) {
                            status = new ProteinStatus();
                            proteinMatchNameStatusMap.put(proteinMatchName, status);
                            proteinNameList.add(proteinMatchName);
                        }
                        status.inSubSet(rsm);
                    }
                }

                Collections.sort(proteinNameList, proteinMatchComparator);
            }



            fireTableStructureChanged();
        }
        private Comparator proteinMatchComparator = null;

        public String getProteinMatchName(int rowIndex) {
            return proteinNameList.get(rowIndex);
        }
        
        public ArrayList<ProteinMatch> getProteinMatchArray(int rowIndex) {
            String proteinName = getProteinMatchName(rowIndex);
            int size = proteinSetArray.size();
            
            ArrayList<ProteinMatch> proteinMatchList = new ArrayList<ProteinMatch>(size);
            
            
            for (int i=0;i<size;i++) {
                ProteinMatch pm = proteinOfProteinSetsMap.get(proteinSetArray.get(i)).get(proteinName);
                proteinMatchList.add(pm);
            }
            return proteinMatchList;
            
        }
        

        

        public ProteinSet getProteinSet(int i) {
            return proteinSetArray.get(i);
        }
        
        public ProteinMatch getProteinMatch(Integer resultSetId) {
            return srcProteinMatchMap.get(resultSetId);
        }

        public ArrayList<ResultSummary> getResultSummaryList() {
            if (proteinSetArray == null) {
                return null;
            }

            int size = proteinSetArray.size();
            ArrayList<ResultSummary> rsmArray = new ArrayList<ResultSummary>(size);
            for (int i = 0; i < size; i++) {
                rsmArray.add(proteinSetArray.get(i).getResultSummary());
            }

            return rsmArray;
        }

        public ResultSummary getResultSummary(int index) {
            if ((proteinSetArray == null) || (proteinSetArray.isEmpty())) {
                return null;
            }

            return proteinSetArray.get(index).getResultSummary();
        }

        @Override
        public int getRowCount() {
            if (proteinNameList == null) {
                return 0;
            }
            return proteinNameList.size();
        }

        @Override
        public int getColumnCount() {
            if ((proteinSetArray == null) || (proteinSetArray.isEmpty())) {
                return 1;
            }
            return proteinSetArray.size() + 1;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {

            if (columnIndex == COLTYPE_PROTEIN_NAME) {
                return proteinNameList.get(rowIndex);
            }

            // other columns correspond to a ProteinSet
            String proteinName = proteinNameList.get(rowIndex);
            return proteinMatchNameStatusMap.get(proteinName);

        }

        @Override
        public Class getColumnClass(int col) {
            if (col == COLTYPE_PROTEIN_NAME) {
                return String.class;

            }
            return ProteinStatus.class;
        }

        @Override
        public String getColumnName(int col) {
            if (col == COLTYPE_PROTEIN_NAME) {
                return "Protein";
            }
            if (proteinSetArray == null) {
                return "";
            }
            return "Rsm"+proteinSetArray.get( col - 1).getResultSummary().getId().toString(); //JPM.TODO
        }
    }

    private class ProteinStatus implements Comparable<ProteinStatus> {

        private int sameSetCount = 0;
        private int subSetCount = 0;
        public static final int SAME_SET = 0;
        public static final int SUB_SET = 1;
        public static final int NOT_PRESENT = 2;
        HashMap<ResultSummary, Boolean> sameSetSubSetMap = new HashMap<ResultSummary, Boolean>();

        public ProteinStatus() {
        }

        public int getStatus(ResultSummary rsm) {
            Boolean b = sameSetSubSetMap.get(rsm);
            if (b == null) {
                return NOT_PRESENT;
            }
            if (b.booleanValue()) {
                return SAME_SET;
            } else {
                return SUB_SET;
            }
        }

        public void inSameSet(ResultSummary rsm) {
            sameSetCount++;
            sameSetSubSetMap.put(rsm, Boolean.TRUE);
        }

        public void inSubSet(ResultSummary rsm) {
            subSetCount++;
            sameSetSubSetMap.put(rsm, Boolean.FALSE);
        }

        private int getScore() {
            return sameSetCount * 2 + subSetCount;
        }

        @Override
        public int compareTo(ProteinStatus o) {
            return getScore() - o.getScore();
        }
    }

    /**
     * Renderer to display if a protein is in the sameset or subset of a ProteinSet
     */
    public static class ProteinStatusRenderer extends DefaultTableRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            ProteinStatus proteinStatus = (ProteinStatus) value;

            ProteinSetCmpTableModel model = ((ProteinSetCmpTableModel) table.getModel());
            
            int columnConverted = -1;
            if  (column != -1) {
                columnConverted = table.convertColumnIndexToModel(column);
            }
            
            ResultSummary rsm = model.getResultSummary(columnConverted - 1);

            String nbPeptidesString = null;

            
            int rowConverted = -1;
            if (row != -1) {
                rowConverted = table.convertRowIndexToModel(row);
            }
            
            String proteinMatchName = model.getProteinMatchName(rowConverted);
            
            ProteinSet proteinSet = model.getProteinSet(columnConverted - 1);
            
            ProteinMatch pm = model.proteinOfProteinSetsMap.get(proteinSet).get(proteinMatchName);
            
            PeptideSet pset = (pm == null) ? null : pm.getTransientData().getPeptideSet(rsm.getId());
            
            if (pset == null) {
                nbPeptidesString = "";
            } else {
                nbPeptidesString = String.valueOf(pset.getPeptideCount());
            }

            JLabel l = (JLabel) super.getTableCellRendererComponent(table, nbPeptidesString, isSelected, hasFocus, row, column);

            int status = proteinStatus.getStatus(rsm);
            if (status == ProteinStatus.SAME_SET) {
                l.setIcon(RsetProteinGroupComparePanel.sameSetIcon);
            } else if (status == ProteinStatus.SUB_SET) {
                l.setIcon(RsetProteinGroupComparePanel.subSetIcon);
            } else {
                l.setIcon(null);
            }

            return l;
        }
    }

    /**
     * Renderer used for the header of the columns.
     * It manages the first column which is a Protein or the other columns
     * which are Result Summaries
     */
    public class RsmOrProteinHeaderRenderer extends DefaultTableCellRenderer {

        private TableCellRenderer defaultRenderer = null;
        
        public RsmOrProteinHeaderRenderer(TableCellRenderer defaultRenderer) {
            this.defaultRenderer = defaultRenderer;
        }
        
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            int columnConverted = -1;
            if  (column != -1) {
                columnConverted = table.convertColumnIndexToModel(column);
            }

            // Specific case for the first column which is not a ResultSummary but a Protein Name
            if (columnConverted == ProteinSetCmpTableModel.COLTYPE_PROTEIN_NAME ) {
                return defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
            
            
            final int SQUARE_SIZE = 10;
            
            String columnName = null;
            ImageIcon icon = null;
            
            int colorIndex = columnConverted - 1;
            
            if  (columnConverted == -1) {
                columnName = "";
                icon = null;
            } else {
                if (columnConverted == 0) {
                    columnName = "";
                    icon = null;
                } else {
                    columnName = table.getModel().getColumnName(columnConverted);
                    icon = CyclicColorPalette.getColoredImageIcon(SQUARE_SIZE, SQUARE_SIZE, colorIndex, true);
                }
            }
            
            
            JLabel label = (JLabel) defaultRenderer.getTableCellRendererComponent(table, columnName, isSelected, hasFocus, row, column);
            
            
            
            if (icon != null) {
                
                List<SortKey> sortList = (List<SortKey>) table.getRowSorter().getSortKeys();
                Icon sortIcon = label.getIcon();
                
                if (!sortList.isEmpty() && (sortIcon != null)) {

                    String iconKey = null;
                    SortKey key = sortList.get(0);
                    if (key.getSortOrder() == SortOrder.ASCENDING) {
                        iconKey = SQUARE_SIZE + "x" + colorIndex + "UP";

                    } else if (key.getSortOrder() == SortOrder.DESCENDING) {
                        iconKey = SQUARE_SIZE + "x" + colorIndex + "DOWN";
                    }
                    icon = CyclicColorPalette.getCombinedImageIcon(icon, sortIcon, 3, iconKey);
                }

                label.setIcon(icon);
            }
            
           
            return label;
        }



    }
}
