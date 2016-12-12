package fr.proline.studio.gui.expressionbuilder;

import fr.proline.studio.utils.IconManager;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import org.jdesktop.swingx.JXTextField;

/**
 *
 * @author JM235353
 */
public class ExpressionBuilderPanel extends JPanel {
    
    private final ArrayList<ExpressionEntity> m_builtExpression = new ArrayList<>();
    private final ArrayList<ExpressionEntity> m_redoExpression = new ArrayList<>();

    private final JPanel m_functionsPanel;
    private final JPanel m_numberPanel;
    private final JScrollPane m_variablesListScrollPane;
    private JList m_variablesList;
    private final JTextField m_expressionTextField;
    private final JButton m_redoButton;
    private final JButton m_undoButton;
    private final JButton m_clearButton;
    
    public ExpressionBuilderPanel() {
        setBorder(BorderFactory.createTitledBorder(" Expression Builder "));
        setLayout(new GridBagLayout());
        
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(2, 2, 2, 2);
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;

        JLabel equalLabel = new JLabel("=");
        m_expressionTextField = new JXTextField();
        m_expressionTextField.setEditable(false);
        m_undoButton = createUndoButton();
        m_redoButton = createRedoButton();
        m_clearButton = createClearButton();
        
        m_functionsPanel = createFunctionPanel();
        m_numberPanel = createNumberPanel();
        m_variablesListScrollPane = createVariablesList();
        
        add(equalLabel, c);

        
        c.gridx++;
        c.weightx = 1;
        add(m_expressionTextField, c);

        c.weightx = 0;
        c.gridx++;
        add(m_undoButton, c);

        
        c.gridx++;
        add(m_redoButton, c);

        c.gridx++;
        add(m_clearButton, c);
        
        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 5;
        c.weightx = 1;
        add(m_functionsPanel, c);
        
        c.gridy++;
        add(m_numberPanel, c);

        c.gridy++;
        add(m_variablesListScrollPane, c);
        
    }
    
    private JButton createUndoButton() {
        JButton undoButton = new JButton(IconManager.getIcon(IconManager.IconType.UNDO));
        undoButton.setMargin(new java.awt.Insets(1, 1, 1, 1));

        undoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                m_redoExpression.add(m_builtExpression.remove(m_builtExpression.size()-1));
                m_expressionTextField.setText(getDisplayExpression());
                updateEnableButtons();
            }
        });
        undoButton.setEnabled(false);
        
        return undoButton;
    }
    
    private JButton createRedoButton() {
        JButton redoButton = new JButton(IconManager.getIcon(IconManager.IconType.REDO));
        redoButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
        
        
        redoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                m_builtExpression.add(m_redoExpression.remove(m_redoExpression.size()-1));
                m_expressionTextField.setText(getDisplayExpression());
                updateEnableButtons();
            }
        });
        redoButton.setEnabled(false);

        return redoButton;
    }
    
    private JButton createClearButton() {
        JButton clearButton = new JButton(IconManager.getIcon(IconManager.IconType.ERASER));
        clearButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
        
        
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                m_builtExpression.clear();
                m_expressionTextField.setText(getDisplayExpression());
                updateEnableButtons();
            }
        });
        clearButton.setEnabled(false);
        
        
        return clearButton;
    }
    
    private void updateEnableButtons() {
        m_redoButton.setEnabled(!m_redoExpression.isEmpty());
        m_undoButton.setEnabled(!m_builtExpression.isEmpty());
        m_clearButton.setEnabled(!m_builtExpression.isEmpty());

    }
    
    private JPanel createFunctionPanel() {
        JPanel functionsPanel = new JPanel(new GridBagLayout());
        
        m_functionsPanelConstraints = new GridBagConstraints();
        m_functionsPanelConstraints.gridx = 0;
        m_functionsPanelConstraints.gridy = 0;
        m_functionsPanelConstraints.insets = new Insets(2, 2, 2, 2);
        m_functionsPanelConstraints.fill = GridBagConstraints.BOTH;
        m_functionsPanelConstraints.anchor = GridBagConstraints.NORTHWEST;
        
        return functionsPanel;
    }
    private GridBagConstraints m_functionsPanelConstraints;
    
    private JPanel createNumberPanel() {
        JPanel numberPanel = new JPanel(new FlowLayout());
        
        numberPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(2, 2, 2, 2);
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        
        JLabel label = new JLabel("Number:");
        
        final JTextField numberTextField = new JTextField();
        
        
        JButton addButton = new JButton(IconManager.getIcon(IconManager.IconType.PLUS_16X16));
        addButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                String text = numberTextField.getText().trim();
                
                boolean parseWorks = false;
                
                try {
                    Double.parseDouble(text);
                    parseWorks = true;
                } catch (Exception e1) {  
                }
                try {
                    Boolean.parseBoolean(text);
                    parseWorks = true;
                } catch (Exception e1) {  
                }
                if (parseWorks) {
                    ExpressionEntity entity = new ExpressionEntity(text, text, text);
                    m_builtExpression.add(entity);
                    m_expressionTextField.setText(getDisplayExpression());
                    updateEnableButtons();
                    numberTextField.setText("");
                }
            }
        });
        
        
        c.gridx = 0;
        c.gridy = 0;
        numberPanel.add(label, c);
        
        c.gridx++;
        c.weightx = 1;
        numberPanel.add(numberTextField, c);
        
        c.gridx++;
        c.weightx = 0;
        numberPanel.add(addButton, c);
        
        return numberPanel;
    }
    
    private JScrollPane createVariablesList() {
        m_variablesList = new JList(new DefaultListModel());
        
        // no selection allowed
        m_variablesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(m_variablesList);
        
        m_variablesList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                if (evt.getClickCount() == 1) {
                    ExpressionEntity entity = (ExpressionEntity) list.getSelectedValue();
                    m_builtExpression.add(entity);
                    m_expressionTextField.setText(getDisplayExpression());
                    updateEnableButtons();
                }
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        m_variablesList.clearSelection();
                    }
                    
                });
            }
        });
        
        m_variablesList.setCellRenderer(EntityListRenderer.getRenderer()); 
        
        return scrollPane;
    }
    
    public void addFunction(ExpressionEntity entity) {
        JButton functionButton = new JButton(entity.getName());
        functionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                m_builtExpression.add(entity);
                m_expressionTextField.setText(getDisplayExpression());
                updateEnableButtons();
            }
        });
        
        m_functionsPanelConstraints.gridx++;
        m_functionsPanel.add(functionButton, m_functionsPanelConstraints);
    }
    
    public void addVariable(ExpressionEntity entity) {
        DefaultListModel listModel = (DefaultListModel) m_variablesList.getModel();
        listModel.addElement(entity);
    }
    
    public String getCodeExpression() {
        StringBuilder sb = new StringBuilder();
        for (ExpressionEntity entity : m_builtExpression ) {
            sb.append(entity.getCode()).append(' ');
        }
        return sb.toString();
    }
    
    public String getDisplayExpression() {
        StringBuilder sb = new StringBuilder();
        for (ExpressionEntity entity : m_builtExpression) {
            sb.append(entity.getNameDisplayed()).append(' ');
        }
        return sb.toString();
    }
    
    private static class EntityListRenderer extends DefaultListCellRenderer {

        private static EntityListRenderer m_singleton = null;

        public static EntityListRenderer getRenderer() {
            if (m_singleton == null) {
                m_singleton = new EntityListRenderer();
            }
            return m_singleton;
        }

        private EntityListRenderer() {
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            return super.getListCellRendererComponent(list, ((ExpressionEntity) value).getName(), index, isSelected, cellHasFocus);
        }
    }
 

}
