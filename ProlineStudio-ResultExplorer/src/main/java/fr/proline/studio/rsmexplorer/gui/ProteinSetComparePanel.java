package fr.proline.studio.rsmexplorer.gui;

import fr.proline.core.orm.msi.ProteinMatch;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.gui.SquareColorPanel;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.rsmexplorer.gui.model.ProteinSetCmpTableModel;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.jdesktop.swingx.util.PaintUtils;

/**
 *
 * @author JM235353
 */
public class ProteinSetComparePanel extends JPanel implements DataBoxPanelInterface {

    private ProteinSetCmpTable table;

    //private ProteinMatch previouslyProteinSelected = null;
    
    private AbstractDataBox dataBox;
    
    public ProteinSetComparePanel() {
        setLayout(new GridLayout());

        
        table = new ProteinSetCmpTable();
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.white);

 
     

        add(scrollPane);

    }

    public ArrayList<ResultSummary> getResultSummaryList() {
        ProteinSetCmpTableModel tableModel = (ProteinSetCmpTableModel) table.getModel();
        return tableModel.getResultSummaryList();
    }
    
    public ResultSummary getFirstResultSummary() {
        ProteinSetCmpTableModel tableModel = (ProteinSetCmpTableModel) table.getModel();
        return tableModel.getFirstResultSummary();
    }
    
    public ProteinMatch getSelectedProteinMatch() {

        // Retrieve Selected Row
        int selectedRow = table.getSelectedRow();

        // nothing selected
        if (selectedRow == -1) {
            return null;

        }
        
        // convert according to the sorting
        selectedRow = table.convertRowIndexToModel(selectedRow);

        // Retrieve Selected Column
        int selectedColumn = table.getSelectedColumn();
        
        // nothing selected
        if (selectedColumn == -1) {
            return null;

        }
        
        // convert according to the sorting of columns
        selectedColumn = table.convertColumnIndexToModel(selectedColumn);

        if (selectedColumn == ProteinSetCmpTableModel.COLTYPE_SAMESET_SUBSET) {
            // not a ProteinMatch
            return null;
        }
        
        // Retrieve ProteinSet selected
        ProteinSetCmpTableModel tableModel = (ProteinSetCmpTableModel) table.getModel();

        return (ProteinMatch) tableModel.getValueAt(selectedRow, selectedColumn);
    }
    
    public void setData(ProteinMatch proteinMatch) {
        
        //previouslyProteinSelected = null;
        
        ProteinSetCmpTableModel tableModel = ((ProteinSetCmpTableModel) table.getModel());
        tableModel.setData(proteinMatch);
       
        boolean hasData = (tableModel.getRowCount()>0);
        
        if (hasData) {
            int colCount = tableModel.getColumnCount();
            
             TableColumn col = table.getColumnModel().getColumn(0);        
            col.setPreferredWidth(80);
            for (int i=1;i<colCount;i++) {
                col = table.getColumnModel().getColumn(i);        
                col.setPreferredWidth(200);
            }
        }
        
        // select first data
        if (tableModel.getRowCount()>0) {
            table.changeSelection(0,1,false,false);
        }
    }

    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        this.dataBox = dataBox;
    }


    public class ProteinSetCmpTable extends JTable {

        public ProteinSetCmpTable() {
            setModel(new ProteinSetCmpTableModel());

            setFillsViewportHeight(true);
            getTableHeader().setBackground(Color.white);
            
            setCellSelectionEnabled(true);
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            setShowGrid(false);
            //setShowVerticalLines(true);
            setDefaultRenderer(ProteinMatch.class, new ProteinMatchCmpRenderer());
            setDefaultRenderer(Boolean.class, new SameSetSubSetRenderer());


            getTableHeader().setDefaultRenderer(new ResultSummaryRenderer());

            setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

            // JPM.HACK : due to combined selection of cells in different rows/cols,
            // we need to force a repaint after a selection
            // (use of ListListener does not work, because it is not triggered
            // when the user select a different cell in the same row)
            addMouseListener(new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent e) {
                    table.repaint();
                }
            });
            
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


    public class ProteinMatchCmpRenderer implements TableCellRenderer {

        private ProteinMatchPanel rendererPanel;
        private JPanel blankPanel;
        
        public ProteinMatchCmpRenderer() {
            rendererPanel = new ProteinMatchPanel();

            blankPanel = new JPanel();
            blankPanel.setOpaque(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
  
            ProteinMatch proteinMatch = (ProteinMatch) value;

            if (proteinMatch == null) {
                return blankPanel;
            }

            ProteinSetCmpTableModel model = ((ProteinSetCmpTableModel) table.getModel());
            
            int status = model.getStatus(proteinMatch);
            
            int highlightColor = ProteinMatchPanel.HIGHLIGHT_NO;
            if (status == ProteinSetCmpTableModel.STATUS_NOT_ALWAYS_PRESENT ) {
                highlightColor = ProteinMatchPanel.HIGHLIGHT_RED;
            } else if (status == ProteinSetCmpTableModel.STATUS_SAME_OR_SUB_SET ) {
                highlightColor = ProteinMatchPanel.HIGHLIGHT_YELLOW;
            }
            
            if (!isSelected) {
                int selectedCol = table.getSelectedColumn();
                if (selectedCol >0) { // first column is for SameSet / SubSet
                    selectedCol = table.convertColumnIndexToModel(selectedCol);
                    
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow!=-1) {
                        selectedRow = table.convertRowIndexToModel(selectedRow);
                        
                        Object selection = model.getValueAt(selectedRow, selectedCol); //JPM.HACK : possible problem with converted column when a column is being dragged
                        if (selection instanceof ProteinMatch) {
                            ProteinMatch selectedProteinMatch = (ProteinMatch) selection;
                        if (selectedProteinMatch != null) { // could really happen == null
                                if (proteinMatch.getAccession().compareTo(selectedProteinMatch.getAccession()) == 0) {
                                    isSelected = true;
                                }
                            }
                        }
                    }
                    
                }
                
            }
            
            rendererPanel.setSelected(isSelected);
            rendererPanel.setValues(proteinMatch.getAccession(), proteinMatch.getScore(), highlightColor);

            int height = rendererPanel.getPreferredSize().height;
            if (table.getRowHeight(row) < height) {
                table.setRowHeight(row, height);
            }

            return rendererPanel;
        }
    }

    public class ResultSummaryRenderer extends JPanel implements TableCellRenderer {

        private JLabel textLabel;
        private SquareColorPanel colorPanel;
        private BlankPanel blankPanel = new BlankPanel();
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            
            column = table.convertColumnIndexToModel(column);
            
            if (column == 0) {
                return blankPanel;
            }
            textLabel.setText(table.getModel().getColumnName(column));
            colorPanel.setColor( CyclicColorPalette.getColor(column-1) );
            return this;
        }

        public ResultSummaryRenderer() {
            setLayout(new GridBagLayout());

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1;
            c.weighty = 0;
            c.insets = new Insets(3, 0, 3, 0);
            add(Box.createHorizontalGlue(), c);

            c.gridx++;
            c.weightx = 0;
            colorPanel = new SquareColorPanel(10); // size of color panel is set to 10x10
            add(colorPanel, c);

            c.gridx++;
            add(Box.createHorizontalStrut(5), c);

            c.gridx++;
            textLabel = new JLabel();
            add(textLabel, c);

            c.gridx++;
            c.weightx = 1;
            add(Box.createHorizontalGlue(), c);

            c.gridx++;
            c.weightx = 0;
            add(new JSeparator(SwingConstants.VERTICAL), c);

            

            setBackground(Color.white);
        }

 
        
        public class BlankPanel extends JPanel {
            public BlankPanel() {
            setLayout(new GridBagLayout());
            setBackground(Color.white);

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1;
            c.weighty = 0;
            c.insets = new Insets(3, 0, 3, 0);
            add(Box.createGlue(), c);

            c.gridx++;
            c.weightx = 0;
            add(new JLabel(" "), c);
            
            c.gridx++;
            add(new JSeparator(SwingConstants.VERTICAL), c);

            }
        }
    }

    public class ProteinMatchPanel extends JPanel {

        public final static int HIGHLIGHT_NO = 0;
        public final static int HIGHLIGHT_YELLOW = 1;
        public final static int HIGHLIGHT_RED = 2;
        private ProteinNameLabel proteinLabel;
        private ScoreLabel scoreLabel;
        private boolean selected = false;

        public ProteinMatchPanel() {
            setBackground(Color.white);
            setLayout(new GridBagLayout());

            JPanel internalPanel = new JPanel() {

                @Override
                public void paint(Graphics g) {
                    super.paint(g);

                    Graphics2D g2 = (Graphics2D) g;

                    int width = getWidth();
                    int height = getHeight();

                    g2.setColor(Color.black);

                    if (selected) {
                        g2.setStroke(new BasicStroke(3));
                        g2.drawRect(1, 1, width - 3, height - 3);
                    } else {
                        g2.setStroke(new BasicStroke(1));
                        g2.drawRect(0, 0, width - 1, height - 1);
                    }

                }
            };


            internalPanel.setLayout(new GridBagLayout());
            internalPanel.setBackground(Color.white);

            proteinLabel = new ProteinNameLabel();
            proteinLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            proteinLabel.setBackground(Color.white);

            scoreLabel = new ScoreLabel();
            scoreLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            scoreLabel.setBackground(Color.white);

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1;
            c.weighty = 0;



            internalPanel.add(proteinLabel, c);

            c.gridy++;
            internalPanel.add(scoreLabel, c);

            c.gridx = 0;
            c.gridy = 0;
            c.insets = new Insets(5, 5, 5, 5);
            add(internalPanel, c);

        }


        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public void setValues(String proteinName, double score, int highlight) {
            proteinLabel.setText(proteinName);
            proteinLabel.setHighlightState(highlight);
            scoreLabel.setScore(score);
        }

        public class ScoreLabel extends JLabel {

            private Color greenHighlight = null;
            private double score = 0;

            public ScoreLabel() {
                Color base = PaintUtils.setSaturation(Color.GREEN, .7f);
                greenHighlight = PaintUtils.setAlpha(base, 125);
                setOpaque(false);
            }

            public void setScore(double score) {
                this.score = score;
                setText(String.valueOf(score));
            }

            @Override
            public void paint(Graphics g) {


                g.setColor(greenHighlight);

                int rectWidth = (int) Math.round(((score / 100) * (getWidth() - 1)));
                g.fillRect(0, 0, rectWidth, getHeight() - 1);
                super.paint(g);

                g.setColor(Color.black);
                g.drawLine(0, 0, getWidth() - 1, 0);


            }
        }

        public class ProteinNameLabel extends JLabel {

            private Color yellowHighlight = null;
            private Color redHighlight = null;
            private int highlightState = 0;

            public ProteinNameLabel() {
                yellowHighlight = PaintUtils.setAlpha(PaintUtils.setSaturation(Color.yellow, .7f), 125);
                redHighlight = PaintUtils.setAlpha(PaintUtils.setSaturation(Color.red, .7f), 125);
                setOpaque(false);
            }

            public void setHighlightState(int highlightState) {
                this.highlightState = highlightState;
            }

            @Override
            public void paint(Graphics g) {

                if (highlightState == HIGHLIGHT_YELLOW) {
                    g.setColor(yellowHighlight);

                    g.fillRect(0, 0, getWidth(), getHeight());
                } else if (highlightState == HIGHLIGHT_RED) {
                    g.setColor(redHighlight);

                    g.fillRect(0, 0, getWidth(), getHeight());
                }


                super.paint(g);



            }
        }
    }

    public class SameSetSubSetRenderer implements TableCellRenderer {

        private SameSetSubSetPanel sameSetPanel;
        private SameSetSubSetPanel subSetPanel;
        private JPanel blankPanel;

        public SameSetSubSetRenderer() {
            sameSetPanel = new SameSetSubSetPanel(true);
            subSetPanel = new SameSetSubSetPanel(false);
            blankPanel = new JPanel();
            blankPanel.setOpaque(false);



        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Boolean sameSet = (Boolean) value;

            if (sameSet == null) {
                return blankPanel;
            }
            if (sameSet.booleanValue()) {
                return sameSetPanel;
            }
            return subSetPanel;

        }

        private class SameSetSubSetPanel extends JPanel {

            //boolean sameSet;

            public SameSetSubSetPanel(boolean sameSet) {

                //this.sameSet = sameSet;

                setLayout(new GridBagLayout());
                setBackground(Color.white);

                GridBagConstraints c = new GridBagConstraints();
                c.fill = GridBagConstraints.BOTH;
                c.gridx = 0;
                c.gridy = 0;
                c.insets = new Insets(5, 0, 5, 0);

                c.weightx = 1;
                add(Box.createGlue(), c);

                c.weightx = 0;
                c.gridx++;
                JLabel subSetLabel = new JLabel((sameSet) ? "Same Set" : "Sub Set");
                subSetLabel.setBorder(new CompoundBorder(BorderFactory.createLineBorder(Color.black), BorderFactory.createEmptyBorder(2, 2, 2, 2)));
                add(subSetLabel, c);

                c.gridy++;
                c.gridx = 0;
                c.weightx = 1;
                c.weighty = 1;
                c.gridwidth = 2;
                add(Box.createGlue(), c);

            }

        }
    }
}
