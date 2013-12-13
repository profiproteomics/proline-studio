package fr.proline.studio.rsmexplorer.gui;

import fr.proline.core.orm.msi.*;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.gui.SquareColorPanel;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.rsmexplorer.gui.renderer.PeptideRenderer;
import fr.proline.studio.utils.CyclicColorPalette;
import fr.proline.studio.utils.DecoratedTable;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Panel to compare Peptides of different proteins
 * @author JM235353
 */
public class RsetPeptidesOfProteinsCmpPanel extends HourglassPanel implements DataBoxPanelInterface {

    private AbstractDataBox m_dataBox;

    /**
     * Creates new form RsetPeptidesOfProteinsCmpPanel
     */
    public RsetPeptidesOfProteinsCmpPanel() {
        initComponents();
        
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        proteinNameTextField = new javax.swing.JTextField();
        proteinNameTextField.setEditable(false);
        proteinNameTextField.setBackground(Color.white);
        scrollPane = new javax.swing.JScrollPane();
        table = new PeptideCmpTable();

        proteinNameTextField.setText(org.openide.util.NbBundle.getMessage(RsetPeptidesOfProteinsCmpPanel.class, "RsetPeptidesOfProteinsCmpPanel.proteinNameTextField.text")); // NOI18N

        table.setModel(new PeptideCmpTableModel());
        scrollPane.setViewportView(table);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(proteinNameTextField)
                    .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(proteinNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField proteinNameTextField;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables

    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        m_dataBox = dataBox;
    }

    @Override
    public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getRemoveAction(splittedPanel);
    }

    @Override
    public ActionListener getAddAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getAddAction(splittedPanel);
    }

    
    public void setData(ArrayList<ProteinMatch> proteinMatchArray, ArrayList<ResultSummary> rsmArray) {
        ((PeptideCmpTableModel) table.getModel()).setData(proteinMatchArray, rsmArray);
        if ((proteinMatchArray == null) || (proteinMatchArray.isEmpty())) {
            proteinNameTextField.setText("");
        } else {
            proteinNameTextField.setText(proteinMatchArray.get(0).getAccession());
        }
    }

    public class PeptideCmpTable extends DecoratedTable {

        public PeptideCmpTable() {
            setDefaultRenderer(PeptideCompare.class, new PeptideCompareRenderer());
            setDefaultRenderer(Peptide.class, new PeptideRenderer());
    
        }

        /**
         * Called whenever the value of the selection changes.
         *
         * @param e the event that characterizes the change.
         */
        @Override
        public void valueChanged(ListSelectionEvent e) {

            super.valueChanged(e);



            m_dataBox.propagateDataChanged(Peptide.class);

        }
    }

    public static class PeptideCompare {

        private ArrayList<Boolean> hasPeptideList = null;

        public PeptideCompare(int size) {
            hasPeptideList = new ArrayList<>(size);
        }

        public void clear() {
            hasPeptideList.clear();
        }
        
        public void addHasPeptide(boolean presence) {
            hasPeptideList.add(Boolean.valueOf(presence));
        }
        
        public int size() {
            return hasPeptideList.size();
        }

        public boolean hasPeptide(int i) {
            return hasPeptideList.get(i).booleanValue();
        }

        public int getColorIndex(int i) {
            return i; //JPM.TODO
        }
    }

    public static class PeptideCmpTableModel extends AbstractTableModel {

        private HashMap<Long, ProteinMatch> rsmToProteinMatchMap = null;
        
        private ArrayList<ProteinMatch> m_proteinMatchArray;
        private ArrayList<ResultSummary> m_rsmArray;
        private HashMap<Long, PeptideMatch> m_peptideMatchMap = new HashMap<>();
        private List<PeptideMatch> m_peptideMatchList = null;
        
        private PeptideCompare m_peptideCompare = null;
        
        public static final int COLTYPE_PEPTIDE_NAME = 0;
        public static final int COLTYPE_RSM_PRESENCE = 1;

        private static final String[] m_columnNames = {"Peptide", "Rsm"};
    
        
        public void setData(ArrayList<ProteinMatch> proteinMatchArray, ArrayList<ResultSummary> rsmArray) {
            
            m_proteinMatchArray = proteinMatchArray;
            m_rsmArray = rsmArray;

            if ((proteinMatchArray == null) || (proteinMatchArray.isEmpty())) {
                m_peptideMatchList = null;
                m_peptideMatchMap.clear();
                
                fireTableStructureChanged();
                return;
            }
            
            if (rsmToProteinMatchMap == null) {
                rsmToProteinMatchMap = new HashMap<>();
            } else {
                rsmToProteinMatchMap.clear();
            }
            
            for (int i = 0; i < proteinMatchArray.size(); i++) {
                ProteinMatch pm = proteinMatchArray.get(i);
                if (pm == null) {
                    continue;
                }
                ProteinMatch.TransientData data = pm.getTransientData();
                Set<Long> rsmIdSet = data.getRecordedRsmId();

                Iterator<Long> it = rsmIdSet.iterator();
                while (it.hasNext()) {

                    rsmToProteinMatchMap.put(it.next(), pm);
                }
            }
            

            m_peptideMatchMap.clear();
            int rsmSize = rsmArray.size();
            for (int i = 0; i < rsmSize; i++) {
                
                Long rsmId = rsmArray.get(i).getId();
                ProteinMatch pm = rsmToProteinMatchMap.get(rsmId);
                
                if (pm == null) {
                    continue; // should not happen
                }
                
                PeptideSet pset = pm.getTransientData().getPeptideSet(rsmId);

                if (pset == null) {
                    // no PeptideSet found for this resultSummary
                    continue;
                }
                PeptideInstance[] peptideInstances = pset.getTransientPeptideInstances();
                if (peptideInstances == null) {
                    continue; // should not happen if the database is correct
                }
                 int nbPeptides = peptideInstances.length;
                for (int j = 0; j < nbPeptides; j++) {
                    PeptideMatch peptideMatch = peptideInstances[j].getTransientData().getBestPeptideMatch();
                    m_peptideMatchMap.put(peptideMatch.getId(), peptideMatch);
                }
            }

            Collection<PeptideMatch> peptideMatchCollection = m_peptideMatchMap.values();
            m_peptideMatchList = new ArrayList<>(peptideMatchCollection);
            Collections.sort(m_peptideMatchList, PeptideComparator.getInstance() );
            
            m_peptideCompare = new PeptideCompare(m_peptideMatchList.size());

            fireTableStructureChanged();
            
        }

        @Override
        public Class getColumnClass(int col) {
            if (col == COLTYPE_PEPTIDE_NAME) {
                return Peptide.class;
            } else if (col == COLTYPE_RSM_PRESENCE) {
                return PeptideCompare.class;
            }
            return null; // should not happen

        }
        
        @Override
        public int getRowCount() {
            if (m_peptideMatchList == null) {
                return 0;
            }
            return m_peptideMatchList.size();
        }

        @Override
        public int getColumnCount() {
            return m_columnNames.length;
        }

        @Override
        public String getColumnName(int col) {
            return m_columnNames[col];
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (m_peptideMatchList == null) {
                return null;
            }
            switch (columnIndex) {
                case COLTYPE_PEPTIDE_NAME: {
                    PeptideMatch peptideMatch = m_peptideMatchList.get(rowIndex);
                    Peptide p = peptideMatch.getTransientData().getPeptide();
                    return p;
                }
                case COLTYPE_RSM_PRESENCE: {
                    m_peptideCompare.clear();
                    
                    PeptideMatch peptideMatchCur = m_peptideMatchList.get(rowIndex);


                    int rsmSize = m_rsmArray.size();
                    for (int i = 0; i < rsmSize; i++) {
                        Long rsmId = m_rsmArray.get(i).getId();
                        ProteinMatch pm = rsmToProteinMatchMap.get(rsmId);
                        PeptideSet pset = pm.getTransientData().getPeptideSet(m_rsmArray.get(i).getId());
                        PeptideInstance[] peptideInstanceArray = pset.getTransientPeptideInstances();
                        if (peptideInstanceArray == null) {
                            System.out.println("peptideInstanceArray null");
                            continue;
                        }
                        int nbPeptides = peptideInstanceArray.length;
                        boolean peptideFound = false;
                        for (int j=0;j<nbPeptides;j++) {
                            PeptideMatch p = peptideInstanceArray[j].getTransientData().getBestPeptideMatch();
                            if (p.getId() == peptideMatchCur.getId()) {
                                peptideFound = true;
                                break;
                            }
                        }
                        m_peptideCompare.addHasPeptide(peptideFound);
                    }
                    
                   return m_peptideCompare;
                }
            }
            return null;
        }
    }

    private static class PeptideComparator implements Comparator {

        private static PeptideComparator instance = null;
        
        public static PeptideComparator getInstance() {
            if (instance == null) {
                instance = new PeptideComparator();
            }
            return instance;
        }
        
        private PeptideComparator() {
            
        }
        
        @Override
        public int compare(Object o1, Object o2) {
            PeptideMatch p1 = (PeptideMatch) o1;
            PeptideMatch p2 = (PeptideMatch) o2;
            float diffScore = p1.getScore() - p2.getScore();
            if (diffScore > 0) {
                return 1;
            }
            if (diffScore < 0) {
                return -1;
            }
            return 0;
        }
    }
    
    public class PeptideCompareRenderer extends DefaultTableCellRenderer {

        private JPanel p = null;
        private GridBagConstraints c = new GridBagConstraints();
        private ArrayList<SquareColorPanel> colorPanelArray = new ArrayList<>();
        private int nbActiveColorPanels = 0;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            if (value == null) {
                return super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
            }
            
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            PeptideCompare peptideCompare = (PeptideCompare) value;

            preparePanel(peptideCompare);

            p.setBackground(comp.getBackground());
            
            return p;
        }

        private void preparePanel(PeptideCompare peptideCompare) {



            if (p == null) {
                p = new JPanel(new GridBagLayout());
                c.fill = GridBagConstraints.BOTH;
                c.gridx = 0;
                c.gridy = 0;
                c.weightx = 0;
                c.weighty = 0;
                c.insets = new Insets(3, 3, 3, 3);
            }


            int size = peptideCompare.size();
            boolean mustBuildPanel = (nbActiveColorPanels != size);

            if (mustBuildPanel) {
                p.removeAll();
                nbActiveColorPanels = size;
            }


            for (int i = colorPanelArray.size(); i < size; i++) {
                colorPanelArray.add(new SquareColorPanel(10));  // square color panel dim = 10x10
            }

            c.gridx = 0;
            for (int i = 0; i < size; i++) {
                SquareColorPanel colorPanel = colorPanelArray.get(i);
                if (peptideCompare.hasPeptide(i)) {
                    colorPanel.setColor(CyclicColorPalette.getColor(peptideCompare.getColorIndex(i)));
                } else {
                    colorPanel.setColor(null); // do not paint panel
                }
                if (mustBuildPanel) {
                    p.add(colorPanel, c);
                    c.gridx++;
                }

            }


        }
    }
}
