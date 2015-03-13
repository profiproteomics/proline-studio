package fr.proline.studio.calc;


import fr.proline.studio.python.data.ColData;
import fr.proline.studio.python.data.Table;
import fr.proline.studio.utils.IconManager;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;
import org.python.util.PythonInterpreter;
import org.jdesktop.swingx.JXTable;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyStringMap;

/**
 *
 * @author JM235353
 */
public class CalcDialog extends JDialog {
    
    private JTextArea m_codeArea = null;
    private JTextField m_statusTextField = null;
    private JButton m_executeButton = null;
    private JButton m_loadButton = null;
    private JButton m_saveButton = null;
    private JButton m_clearButton = null;
    private DefaultListModel m_columnsListModel = null;
    private DefaultListModel m_resultsListModel = null;
    private DefaultListModel m_functionsListModel = null;
    private JTabbedPane m_tabbedPane = null;
    
    private static CalcDialog m_calcDialog = null;

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

        setTitle("Stats Calculator");
        
        add(createInternalPanel());
        
        setSize(680, 400);
        setResizable(true);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    }
    

    
    private JPanel createInternalPanel() {
   
        JPanel internalPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        
        m_statusTextField = new JTextField();
        m_statusTextField.setForeground(Color.red);
        m_statusTextField.setEditable(false);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 1;
        c.gridheight = 3;
        internalPanel.add(createTabbedPane(), c);
        
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridheight = 1;
        internalPanel.add(createCodePanel(), c);

        c.gridy++;
        c.weighty = 0;
        internalPanel.add(m_statusTextField, c);
        
        c.gridy++;
        internalPanel.add(createCalcButtons(), c);
        
        
        return internalPanel;
    }
    
    private JTabbedPane createTabbedPane() {
        m_tabbedPane = new JTabbedPane();

        m_columnsListModel = new DefaultListModel();
        final JList columns = new JList(m_columnsListModel);
        columns.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane columnsScrollPane = new JScrollPane();
        columnsScrollPane.setViewportView(columns);
        m_tabbedPane.add("Columns", columnsScrollPane);
        
        m_resultsListModel = new DefaultListModel();
        final JList results = new JList(m_resultsListModel);
        results.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane resultsScrollPane = new JScrollPane();
        resultsScrollPane.setViewportView(results);
        m_tabbedPane.add("Results", resultsScrollPane);
        
        m_functionsListModel = new DefaultListModel();
        JList functions = new JList(m_functionsListModel);
        JScrollPane functionsScrollPane = new JScrollPane();
        functionsScrollPane.setViewportView(functions);
        m_tabbedPane.add("Functions", functionsScrollPane);
        
        
        columns.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Column column = (Column) columns.getSelectedValue();
                    if (column == null) {
                        return;
                    }
                    column.action();

                    columns.clearSelection();
                }

            }
        });
        
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
        
        return m_tabbedPane;
    }
    
    private JPanel createCodePanel() {
        JPanel codePanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5); 

        m_codeArea = new JTextArea(10, 40); 
        JScrollPane codeScrollPane = new JScrollPane();
        //codeScrollPane.getViewport().setBackground(Color.white);
        codeScrollPane.setViewportView(m_codeArea);
        
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        codePanel.add(codeScrollPane, c);
        
        c.gridx++;
        c.weightx = 0;
        codePanel.add(createCodeToolBar(), c);
        
        return codePanel;
    }
    
    private JToolBar createCodeToolBar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
        
        m_executeButton = new JButton(IconManager.getIcon(IconManager.IconType.EXECUTE));
        toolbar.add(m_executeButton);
        
        JButton saveButton = new JButton(IconManager.getIcon(IconManager.IconType.SAVE_WND));
        toolbar.add(saveButton);
        
        JButton loadButton = new JButton(IconManager.getIcon(IconManager.IconType.LOAD_SETTINGS));
        toolbar.add(loadButton);
        
        JButton clearButton = new JButton(IconManager.getIcon(IconManager.IconType.ERASER));
        toolbar.add(clearButton);
        
        
        m_executeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                m_executeButton.setEnabled(false);
                execute();
            }
            
        });
        
        return toolbar;
    }
    
    private JPanel createCalcButtons() {
        JPanel calcButtonsPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        return calcButtonsPanel;
    }
    
    private void fillColumns() {
        m_columnsListModel.clear();
        JXTable table = Table.getCurrentTable();
        TableModel model = table.getModel();
        int nbColumns = table.getColumnCount();
        for (int i=0;i<nbColumns;i++) {
            String colName = model.getColumnName(table.convertColumnIndexToModel(i));
            m_columnsListModel.addElement(new Column(colName, i));
        }
    }
    
    private void execute() {

        m_statusTextField.setText("");

        m_resultsListModel.clear();
        
        PythonInterpreter interpreter = new PythonInterpreter();
        
        try {
            interpreter.exec("from fr.proline.studio.python.data import Col");
            interpreter.exec("from fr.proline.studio.python.data import Table");
            interpreter.exec("from fr.proline.studio.python.math import Stats");
            interpreter.exec("import math");
            interpreter.exec(m_codeArea.getText());

            PyStringMap locals = (PyStringMap) interpreter.getLocals();
            PyList keysList = locals.keys();
            int nbKeys = keysList.size();
            for (int i=0;i<nbKeys;i++) {
                PyObject key = keysList.__getitem__(i);
                PyObject value = locals.get(key);
                if (value instanceof ColData) {
                    ColData col = (ColData) value;
                    String columnName = col.getColumnName();
                    if ((columnName == null) || (columnName.isEmpty())) {
                        col.setColumnName(key.toString());
                    }
                }
                if ((value instanceof ColData) || (value instanceof PyInteger) || (value instanceof PyFloat)) {
                    
                    m_resultsListModel.addElement(new ResultVariable(key.toString(), value));
                }
            }

            m_tabbedPane.setSelectedIndex(1); // Tab with results
            
        } catch (Throwable e) {
            int lineError = -1;
            StackTraceElement[] stackTraceArray = e.getStackTrace();
            String error = "Executing Error";
            for (int i = 0; i < stackTraceArray.length; i++) {
                String line = stackTraceArray[i].toString();
                if ((line != null) && (!line.isEmpty())) {
                    error = line;
                    lineError = stackTraceArray[i].getLineNumber();
                    break;
                }
            }
            if (lineError != -1) {
                m_statusTextField.setText("At line "+lineError+":"+error);
            } else {
                m_statusTextField.setText(error);
            }
        }
        
        m_executeButton.setEnabled(true);
        
        /*PyObject res = interpreter.get("_res");

        if (res instanceof ColData) {
            ColData col = (ColData) res;
            TableModel model = Table.getCurrentTable().getModel();
            if (model instanceof CompoundTableModel) {
                ((CompoundTableModel) model).addModel(new ExprTableModel(col, ((CompoundTableModel) model).getLastNonFilterModel()));
            }
        }*/

    }
    
    public class ResultVariable {
        
        private final String m_name;
        private final PyObject m_value;
        private boolean m_actionDone = false;
        
        ResultVariable(String name, PyObject v) {
            m_name = name;
            m_value = v;
        }
        
        @Override
        public String toString() {
            if (m_value instanceof ColData) {
                return m_name;
            } else if (m_value instanceof PyFloat) {
                return m_name+"="+((PyFloat) m_value).getValue();
            } else if (m_value instanceof PyInteger) {
                return m_name+"="+((PyInteger) m_value).getValue();
            }
            return null; // should not happen
        }
        
        public void action() {
            if (m_actionDone) {
                return;
            }
            if (m_value instanceof ColData) {
                m_actionDone = true;
                Table.addColumn((ColData) m_value);
            }
        }
    }
    
    public class Column {
        
        private final String m_name;
        private final int m_index;
        
        public Column(String name, int index) {
            m_name = name;
            m_index = index;
        }
        
        @Override
        public String toString() {
            return m_name;
        }
        
        public void action() {
            int pos = m_codeArea.getCaretPosition();
            int index = m_index+1;
            m_codeArea.insert("Table.col("+index+")", pos);
        }
    }

}
