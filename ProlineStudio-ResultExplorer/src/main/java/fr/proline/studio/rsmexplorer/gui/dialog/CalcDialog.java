package fr.proline.studio.rsmexplorer.gui.dialog;



import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.python.data.ColData;
import fr.proline.studio.python.data.Table;
import fr.proline.studio.python.interpreter.CalcCallback;
import fr.proline.studio.python.interpreter.CalcInterpreterTask;
import fr.proline.studio.python.interpreter.CalcInterpreterThread;
import fr.proline.studio.python.interpreter.ResultVariable;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.gui.calc.DataTree;
import fr.proline.studio.utils.IconManager;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableColumn;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;
import org.python.core.PyObject;



/**
 *
 * @author JM235353
 */
public class CalcDialog extends JDialog {
    
    private JTextPane m_codeArea = null;
    private DefaultHighlighter m_errorHighlighter = null;
    private Highlighter.HighlightPainter m_errorHighlighterPainter = null;
    private boolean m_isHighlighting = false;
    private JTextField m_statusTextField = null;
    private JButton m_executeButton = null;
    private JButton m_loadButton = null;
    private JButton m_saveButton = null;
    private JButton m_clearButton = null;
    private DefaultListModel m_resultsListModel = null;
    private DefaultListModel m_functionsListModel = null;
    private JTabbedPane m_tabbedPane = null;
    private DataCalcTree m_dataTree = null;

    private JFileChooser m_fileChooser;

    
    private static CalcDialog m_calcDialog = null;

    private static final Color ERROR_COLOR = new Color(196,0,0);
    private static final Color OK_COLOR = new Color(0,196,0);
    
    
    public static CalcDialog getCalcDialog(Window parent, JXTable t) {
        if (m_calcDialog == null) {
            m_calcDialog = new CalcDialog(parent);
        }
        
        m_calcDialog.m_dataTree.updataDataNodes();
                


        return m_calcDialog;
    }
    
    private CalcDialog(Window parent) {
         super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("Python Calculator");
        
        add(createInternalPanel());
        
        // Action when the user press on the dialog cross
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent we) {
                cleanupOnClose();
            }
        });

        // Escape Key Action
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        Action actionListener = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                cleanupOnClose();
                setVisible(false);
            }
        };
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(stroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", actionListener);
        
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



        m_tabbedPane.add("Data", createDataPanel());
        
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
                    PyObject o = resultVariable.getValue();
                    if (o instanceof ColData) {
                        ColData col = (ColData) o;
                        Table t = col.getTable();
                        t.addColumn(col);
                    } else if (o instanceof Table) {
                        WindowBox windowBox = WindowBoxFactory.getModelWindowBox(null, resultVariable.getName());
                        windowBox.setEntryData(-1, ((Table)o).getModel() );
                        DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(windowBox);
                        win.open();
                        win.requestActive();
                    }

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
    
    private JPanel createDataPanel() {
        JPanel dataPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JPanel tablePanel = new JPanel(new GridBagLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Tables"));
        JScrollPane treeScrollPane = new JScrollPane();
        m_dataTree = new DataCalcTree();
        treeScrollPane.setViewportView(m_dataTree);
        
        GridBagConstraints c1 = new GridBagConstraints();
        c1.anchor = GridBagConstraints.NORTHWEST;
        c1.fill = GridBagConstraints.BOTH;
        c1.insets = new java.awt.Insets(5, 5, 5, 5);
        c1.gridx = 0;
        c1.gridy = 0;
        c1.weightx = 1;
        c1.weighty = 1;
        tablePanel.add(treeScrollPane, c1);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        dataPanel.add(tablePanel, c);


        return dataPanel;
    }
    
    private JComponent createCodeArea() {

        final Dimension d = new Dimension(420, 400);
        m_codeArea = new JTextPane();/* {

            @Override
            public Dimension getMinimumSize() {
                return d;
            }
            
            @Override
            public Dimension getPreferredSize() {
                return d;
            }
          
        };*/
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
        codeScrollPane.setMinimumSize(d);
        codeScrollPane.setPreferredSize(d);
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
        
        
        m_saveButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = getPyhtonScriptChooser();
                int result = chooser.showSaveDialog(m_saveButton);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    try {
                        
                        if (file.exists()) {
                            String message = "The file already exists. Do you want to overwrite it ?";
                            String title = "Overwrite ?";
                            String[] options = {"Yes", "No"};
                            int reply = JOptionPane.showOptionDialog(m_saveButton, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, "Yes");
                            if (reply != JOptionPane.YES_OPTION) {
                                return;
                            }
                        }
 
                        FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
                        BufferedWriter bw = new BufferedWriter(fw);
                        bw.write(m_codeArea.getText());
                        bw.close();
                        fw.close();

                    } catch (Exception ioException) {

                    }
                }
            }

        });
        
        m_loadButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = getPyhtonScriptChooser();
                int result = chooser.showOpenDialog(m_loadButton);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    try {
                        if (file.exists() && file.canRead()) {
                            FileReader reader = new FileReader(file);
                            BufferedReader br = new BufferedReader(reader);
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = br.readLine()) != null) {
                                sb.append(line).append('\n');
                                if (sb.length() > 40000) {
                                    // security : do not read too much characters if the opened file is not appropriate
                                    break;
                                }
                            }
                            br.close();
                            reader.close();
                            m_codeArea.setText(sb.toString());
                            m_codeArea.requestFocus();
                        }
                    } catch (Exception ioException) {

                    }
                }
            }

        });
        
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


    public void cleanupOnClose() {
        
        Table.setTables(null);

    }
    
    private JFileChooser getPyhtonScriptChooser() {
        if (m_fileChooser == null) {
            Preferences preferences = NbPreferences.root();
            String scriptPath = preferences.get("DefaultPythonScriptPath", null);
            if (scriptPath != null) {
                m_fileChooser = new JFileChooser(new File(scriptPath));
            } else {
                m_fileChooser = new JFileChooser();
            }
            m_fileChooser.setMultiSelectionEnabled(false);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Python Script File (*.py)", "py");
            m_fileChooser.setFileFilter(filter);
        }
        
        return m_fileChooser;
    }

    
    private void fillFunctions() {
        m_functionsListModel.addElement(new Function("bbinomial", "bbinomial = Stats.bbinomial( (,) , (,) )", "bbinomial = Stats.bbinomial( (Table.get(1)[5],Table.get(1)[7]), (Table.get(1)[9],Table.get(1)[11]) )"));
        m_functionsListModel.addElement(new Function("diff", "diffTable = Table.diff(,)", "diffTable = Table.diff(Table.get(1),Table.get(2))"));
        m_functionsListModel.addElement(new Function("join", "joinTable = Table.join(,)", "joinTable = Table.join(Table.get(1),Table.get(2))"));
        m_functionsListModel.addElement(new Function("pvalue", "pvalue = Stats.pvalue( (,) , (,) )", "pvalue = Stats.pvalue( (Table.get(1)[5],Table.get(1)[7]), (Table.get(1)[9],Table.get(1)[11]) )"));
        m_functionsListModel.addElement(new Function("ttd", "ttd = Stats.ttd( (,) , (,) )", "ttd = Stats.ttd( (Table.get(1)[5],Table.get(1)[7]), (Table.get(1)[9],Table.get(1)[11]) )"));
        
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
                m_codeArea.setCaretPosition(pos+m_insertText.length());
                m_codeArea.getCaret().setVisible(true);
                m_codeArea.requestFocusInWindow();
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

    public class DataCalcTree extends DataTree {

        public DataCalcTree() {
            super(new DataTree.ParentDataNode(), true, null);
        }

        @Override
        public void action(DataTree.DataNode node) {
            switch (node.getType()) {
                case COLUMN_DATA: {
                    int pos = m_codeArea.getCaretPosition();
                    int index = ((ColumnDataNode) node).getColumnIndex();
                    try {
                        ViewDataNode viewNode = (ViewDataNode) node.getParent();

                        String textToAdd = "Table.get(" + viewNode.getTableIndex() + ")[" + index + "]";
                        m_codeArea.getDocument().insertString(pos, textToAdd, null);
                        m_codeArea.setCaretPosition(pos + textToAdd.length());
                        m_codeArea.getCaret().setVisible(true);
                        m_codeArea.requestFocusInWindow();
                    } catch (BadLocationException e) {

                    }
                }
                    break;
                case VIEW_DATA: {
                    int pos = m_codeArea.getCaretPosition();
                    int index = ((ViewDataNode) node).getTableIndex();
                    try {

                        String textToAdd = "Table.get(" + index + ")";
                        m_codeArea.getDocument().insertString(pos, textToAdd, null);
                        m_codeArea.setCaretPosition(pos + textToAdd.length());
                        m_codeArea.getCaret().setVisible(true);
                        m_codeArea.requestFocusInWindow();
                    } catch (BadLocationException e) {

                    }
                }
                    
            }
        }

    }
    


}
