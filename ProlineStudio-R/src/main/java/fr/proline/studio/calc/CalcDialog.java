package fr.proline.studio.calc;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.python.data.ColData;
import fr.proline.studio.python.data.ExprTableModel;
import fr.proline.studio.python.data.Table;
import fr.proline.studio.table.CompoundTableModel;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.TableModel;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

/**
 *
 * @author JM235353
 */
public class CalcDialog extends DefaultDialog {
    
    private JTextArea m_calcTextArea = null;
    
    private static CalcDialog m_calcDialog = null;

    public static CalcDialog getCalcDialog(Window parent, JTable t) {
        if (m_calcDialog == null) {
            m_calcDialog = new CalcDialog(parent);
        }
                
        Table.setCurrentTable(t);
        return m_calcDialog;
    }
    
    private CalcDialog(Window parent) {
         super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("Stats Calculator");
        
        setInternalComponent(createInternalPanel());
        
        setResizable(true);
    }
    
    private JPanel createInternalPanel() {
   
        JPanel internalPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        m_calcTextArea = new JTextArea(10, 40); 
        m_calcTextArea.setText("Stats.pvalue( (Table.col(5),Table.col(7)), (Table.col(9),Table.col(11)) )");

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        internalPanel.add(m_calcTextArea, c);

        
        return internalPanel;
    }
    
    @Override
    protected boolean okCalled() {

        PythonInterpreter interpreter = new PythonInterpreter();
        interpreter.exec("from fr.proline.studio.python.data import Col");
        interpreter.exec("from fr.proline.studio.python.data import Table");
        interpreter.exec("from fr.proline.studio.python.math import Stats");
        interpreter.exec("_res = "+m_calcTextArea.getText());  // ColTest('Test')+ColTest('Foo')
        PyObject res = interpreter.get("_res");

        if (res instanceof ColData) {
            ColData col = (ColData) res;
            TableModel model = Table.getCurrentTable().getModel();
            if (model instanceof CompoundTableModel) {
                ((CompoundTableModel) model).addModel(new ExprTableModel(col, ((CompoundTableModel) model).getLastNonFilterModel()));
            }
        }

        return true;
    }

}
