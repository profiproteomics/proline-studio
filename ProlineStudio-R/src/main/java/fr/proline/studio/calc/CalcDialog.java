package fr.proline.studio.calc;


import fr.proline.studio.python.data.ColData;
import fr.proline.studio.python.data.Table;
import fr.proline.studio.python.interpreter.CalcCallback;
import fr.proline.studio.python.interpreter.CalcInterpreterTask;
import fr.proline.studio.python.interpreter.CalcInterpreterThread;
import fr.proline.studio.python.interpreter.ResultVariable;
import fr.proline.studio.table.DecoratedTable;
import fr.proline.studio.table.DecoratedTableModel;
import fr.proline.studio.table.TablePopupMenu;
import fr.proline.studio.utils.IconManager;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import org.jdesktop.swingx.JXTable;


/**
 *
 * @author JM235353
 */
public class CalcDialog extends JDialog {
    
    private JTextPane m_codeArea = null;
    DefaultHighlighter m_errorHighlighter = null;
    Highlighter.HighlightPainter m_errorHighlighterPainter = null;
    private boolean m_isHighlighting = false;
    private JTextField m_statusTextField = null;
    private JButton m_executeButton = null;
    private JButton m_loadButton = null;
    private JButton m_saveButton = null;
    private JButton m_clearButton = null;
    ColumnTableModel m_columnTableModel = null;
    private DefaultListModel m_resultsListModel = null;
    private DefaultListModel m_functionsListModel = null;
    private JTabbedPane m_tabbedPane = null;
    
    private static CalcDialog m_calcDialog = null;

    private static final Color ERROR_COLOR = new Color(196,0,0);
    private static final Color OK_COLOR = new Color(0,196,0);
    
    
    public static CalcDialog getCalcDialog(Window parent, JXTable t) {
        if (m_calcDialog == null) {
            m_calcDialog = new CalcDialog(parent);
        }
                
        Table.setCurrentTable(t);
        
        m_calcDialog.fillColumns();
        
        return m_calcDialog;
    }
    
    private CalcDialog(Window parent) {
         super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("Python Calculator");
        
        add(createInternalPanel());
        
        
        setResizable(true);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        pack();
    }
    

    
    private JComponent createInternalPanel() {
   
        JPanel rightPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        m_statusTextField = new JTextField();
        m_statusTextField.setForeground(ERROR_COLOR);
        m_statusTextField.setEditable(false);
        
        
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        rightPanel.add(createCodeArea(), c);
        
        c.gridy++;
        c.weighty = 0;
        rightPanel.add(m_statusTextField, c);
        
        c.gridx++;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 1;
        c.gridheight = 2;
        rightPanel.add(createCodeToolBar(), c);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createTabbedPane(), rightPanel);

        
        return splitPane;
    }
    
    private JTabbedPane createTabbedPane() {
        
        final Dimension d = new Dimension(180, 400);
        m_tabbedPane = new JTabbedPane() {

            @Override
            public Dimension getMinimumSize() {
                return d;
            }
            
            @Override
            public Dimension getPreferredSize() {
                return d;
            }
          
        };


        m_columnTableModel = new ColumnTableModel();
        ColumnTable columnsTable = new ColumnTable();
        columnsTable.setModel(m_columnTableModel);
        JScrollPane columnsScrollPane = new JScrollPane();
        columnsScrollPane.setViewportView(columnsTable);
        m_tabbedPane.add("Columns", columnsScrollPane);
        
        m_functionsListModel = new DefaultListModel();
        fillFunctions();
        final JList functions = new JList(m_functionsListModel);
        JScrollPane functionsScrollPane = new JScrollPane();
        functionsScrollPane.setViewportView(functions);
        m_tabbedPane.add("Functions", functionsScrollPane);
        
        m_resultsListModel = new DefaultListModel();
        final JList results = new JList(m_resultsListModel);
        results.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane resultsScrollPane = new JScrollPane();
        resultsScrollPane.setViewportView(results);
        m_tabbedPane.add("Results", resultsScrollPane);


        
        results.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    ResultVariable resultVariable = (ResultVariable) results.getSelectedValue();
                    if (resultVariable == null) {
                        return;
                    }
                    resultVariable.action();

                    results.clearSelection();
                }

            }
        });
        
        functions.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Function function = (Function) functions.getSelectedValue();
                    if (function == null) {
                        return;
                    }
                    function.action();

                    functions.clearSelection();
                }

            }
        });
        
        functions.addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                ListModel m = functions.getModel();
                int index = functions.locationToIndex(e.getPoint());
                if (index > -1) {
                    functions.setToolTipText(((Function)m.getElementAt(index)).getDescription());
                } else {
                    functions.setToolTipText(null);
                }
            }

        });
        
        return m_tabbedPane;
    }
    
    private JComponent createCodeArea() {

        final Dimension d = new Dimension(420, 400);
        m_codeArea = new JTextPane() {

            @Override
            public Dimension getMinimumSize() {
                return d;
            }
            
            @Override
            public Dimension getPreferredSize() {
                return d;
            }
          
        };
        m_errorHighlighter = new DefaultHighlighter();
        m_errorHighlighterPainter = new DefaultHighlighter.DefaultHighlightPainter(new Color(255,90,90));
        m_codeArea.setHighlighter(m_errorHighlighter);
        m_codeArea.getDocument().addDocumentListener(new DocumentListener () {

            @Override
            public void insertUpdate(DocumentEvent e) {
                removeHighlighting();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                removeHighlighting();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                removeHighlighting();
            }
            
        });
        JScrollPane codeScrollPane = new JScrollPane();
        codeScrollPane.setViewportView(m_codeArea);

        
        return codeScrollPane;
    }
    
    private JToolBar createCodeToolBar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
        
        m_executeButton = new JButton(IconManager.getIcon(IconManager.IconType.EXECUTE));
        toolbar.add(m_executeButton);
        
        m_saveButton = new JButton(IconManager.getIcon(IconManager.IconType.SAVE_WND));
        toolbar.add(m_saveButton);
        
        m_loadButton = new JButton(IconManager.getIcon(IconManager.IconType.LOAD_SETTINGS));
        toolbar.add(m_loadButton);
        
        m_clearButton = new JButton(IconManager.getIcon(IconManager.IconType.ERASER));
        toolbar.add(m_clearButton);
        
        
        m_executeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                m_executeButton.setEnabled(false);
                m_codeArea.setEnabled(false);
                execute();
            }
            
        });
        
        m_clearButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                m_codeArea.setText("");
            }
            
        });
        
        return toolbar;
    }
    
    /*private JPanel createCalcButtons() {
        JPanel calcButtonsPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        return calcButtonsPanel;
    }*/
    
    private void fillColumns() {

        
        
        JXTable table = Table.getCurrentTable();
        TableModel model = table.getModel();
        int nbColumns = table.getColumnCount();
        ArrayList<Column> columnList = new ArrayList<>(nbColumns);
        for (int i=0;i<nbColumns;i++) {
            String colName = model.getColumnName(table.convertColumnIndexToModel(i));
            columnList.add(new Column(colName, i));
        }
        m_columnTableModel.setValues(columnList);
        
        
    }
    
    private void fillFunctions() {
        m_functionsListModel.addElement(new Function("bbinomial", "bbinomial = Stats.bbinomial( (,) , (,) )", "bbinomial = Stats.bbinomial( (Table.col(5),Table.col(7)), (Table.col(9),Table.col(11)) )"));
        m_functionsListModel.addElement(new Function("pvalue", "pvalue = Stats.pvalue( (,) , (,) )", "pvalue = Stats.pvalue( (Table.col(5),Table.col(7)), (Table.col(9),Table.col(11)) )"));
        m_functionsListModel.addElement(new Function("ttd", "ttd = Stats.ttd( (,) , (,) )", "ttd = Stats.ttd( (Table.col(5),Table.col(7)), (Table.col(9),Table.col(11)) )"));
        
    }
    
    private void execute() {

        m_statusTextField.setText("");
        m_resultsListModel.clear();
        removeHighlighting();
        
        final long timeStart = System.currentTimeMillis();
        
        CalcInterpreterThread interpreterThread = CalcInterpreterThread.getCalcInterpreterThread();
        CalcCallback callback = new CalcCallback() {

            @Override
            public void run(ArrayList<ResultVariable> variables, String error, int lineError) {
                if (variables != null) {
                    long milliseconds = (System.currentTimeMillis()-timeStart);
                    int seconds = (int) (milliseconds / 1000) % 60 ;
                    int minutes = (int) (milliseconds / (1000*60));
                    milliseconds = (milliseconds % 100);
                    
                    
                    String timeDisplay = String.format("Execution Time: %d:%d.%d",minutes,seconds,milliseconds );
                    m_statusTextField.setForeground(OK_COLOR);
                    m_statusTextField.setText(timeDisplay);
                    
                    
                    int nb = variables.size();
                    for (int i = 0; i < nb; i++) {
                        m_resultsListModel.addElement(variables.get(i));
                    }
                    m_tabbedPane.setSelectedIndex(2); // Tab with results
                    
                } else if (error != null) {
                    if (lineError != -1) {

                        try {
                            int[] offset = getLineOffsets(m_codeArea.getText(), lineError);
                            if (offset != null) {
                                m_errorHighlighter.addHighlight(offset[0], offset[1], m_errorHighlighterPainter);
                                m_isHighlighting = true;
                            } else {
                                lineError = -1;
                            }
                        } catch (Exception e2) {
                            lineError = -1;
                        }

                    }

                    m_statusTextField.setForeground(ERROR_COLOR);
                    if (lineError != -1) {
                        m_statusTextField.setText("At line " + lineError + ": " + error);

                    } else {
                        m_statusTextField.setText(error);
                    }
                }
                
                m_executeButton.setEnabled(true);
                m_codeArea.setEnabled(true);
            }
            
        };
        
        interpreterThread.addTask(new CalcInterpreterTask(m_codeArea.getText(), callback));

    }


    
    private int[] getLineOffsets(String text, int lineNumber) throws BadLocationException {
        
        int offset = 0;
        int lineIndex = 1;
        while (true) {
            int startOffset = javax.swing.text.Utilities.getRowStart(m_codeArea, offset);
            int endOffset = javax.swing.text.Utilities.getRowEnd(m_codeArea, offset);
            if (lineIndex == lineNumber) {
                return new int[] {startOffset, endOffset};
            }
            lineIndex++;
            offset = endOffset+1;
            if (offset>=text.length()) {
                break;
            }
        }

        return null;
    }
    
    private void removeHighlighting() {
        if (!m_isHighlighting) {
            return;
        }
        m_errorHighlighter.removeAllHighlights();
        m_isHighlighting = false;
    }
    
    public void centerToWindow(Window w) {


        int width = getWidth();
        int height = getHeight();

        int frameX = w.getX();
        int frameY = w.getY();
        int frameWidth = w.getWidth();
        int frameHeight = w.getHeight();

        int x = frameX + (frameWidth - width) / 2;
        int y = frameY + (frameHeight - height) / 2;

        setLocation(x, y);

    }
    

    
    public class Column {
        
        private final String m_name;
        private final int m_index;
        
        public Column(String name, int index) {
            
            String uname = name.toUpperCase();
            
            String BR = "<BR/>";
            int iBR = uname.indexOf(BR);
            while (iBR != -1) {
                name = name.substring(0, iBR)+" "+name.substring(iBR+BR.length(), name.length());
                uname = name.toUpperCase();
                iBR = uname.indexOf(BR);
            }
            
            BR = "<BR>";
            iBR = uname.indexOf(BR);
            while (iBR != -1) {
                name = name.substring(0, iBR)+" "+name.substring(iBR+BR.length(), name.length());
                uname = name.toUpperCase();
                iBR = uname.indexOf(BR);
            }
            
            m_name = name;
            m_index = index;
        }
        
        @Override
        public String toString() {
            return m_name;
        }
        
        public Integer getIndex() {
            return Integer.valueOf( m_index+1);
        }
        
        public void action() {
            int pos = m_codeArea.getCaretPosition();
            int index = m_index+1;
            try {
                m_codeArea.getDocument().insertString(pos, "Table.col("+index+")", null);
            } catch(BadLocationException e) {
                
            }
        }
    }
    
    public class Function {
        private final String m_name;
        private final String m_insertText;
        private final String m_description;
        
        public Function(String name, String insertText, String description) {
            m_name = name;
            m_insertText = insertText;
            m_description = description;
        }
        
        public void action() {
            int pos = m_codeArea.getCaretPosition();
            try {
                m_codeArea.getDocument().insertString(pos, m_insertText, null);
            } catch (BadLocationException e) {

            }
        }
        
        @Override
        public String toString() {
            return m_name;
        }

        
        public String getDescription() {
            return m_description;
        }
        
    }

    
       public static class ColumnTable extends DecoratedTable implements MouseListener {

        public ColumnTable() {

            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            addMouseListener(this);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getClickCount() == 2) {
                int row = getSelectedRow();
                if (row == -1) {
                    return;
                }
                row = convertRowIndexToModel(row);
                Column c = ((ColumnTableModel) getModel()).getColumn(row);

                if (c == null) {
                    return;
                }
                c.action();

                clearSelection();
            }

        }

        @Override
        public TablePopupMenu initPopupMenu() {
            return null;
        }

        @Override
        public void prepostPopupMenu() {
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

    }

    public static class ColumnTableModel extends DecoratedTableModel {

        public static final int COLTYPE_COLUMN_ID = 0;
        public static final int COLTYPE_COLUMN_NAME = 1;

        private static final String[] m_columnNames = {"Index", "Column"};
        private static final String[] m_columnTooltips = {"Column Index", "Column Name"};

        ArrayList<Column> m_columns = null;

        public void setValues(ArrayList<Column> columns) {
            m_columns = columns;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            if (m_columns == null) {
                return 0;
            }
            return m_columns.size();
        }

        @Override
        public int getColumnCount() {
            return m_columnNames.length;
        }

        @Override
        public Class getColumnClass(int col) {
            switch (col) {
                case COLTYPE_COLUMN_ID:
                    return Integer.class;
                case COLTYPE_COLUMN_NAME:
                    return String.class;
            }
            return null;
        }

        @Override
        public String getColumnName(int col) {
            return m_columnNames[col];
        }
        
        @Override
        public Object getValueAt(int row, int col) {
            switch (col) {
                case COLTYPE_COLUMN_ID:
                    return m_columns.get(row).getIndex();
                case COLTYPE_COLUMN_NAME:
                    return m_columns.get(row).toString();
            }
            return null;
        }

        public Column getColumn(int row) {
            return m_columns.get(row);
        }

        @Override
        public String getToolTipForHeader(int col) {
            return m_columnTooltips[col];
        }

        @Override
        public String getTootlTipValue(int row, int col) {
            return null;
        }

    }

}
