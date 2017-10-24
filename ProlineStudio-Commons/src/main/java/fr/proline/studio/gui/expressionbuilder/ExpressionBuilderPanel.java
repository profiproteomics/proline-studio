package fr.proline.studio.gui.expressionbuilder;

import fr.proline.studio.utils.IconManager;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import org.jdesktop.swingx.JXTextField;

/**
 *
 * Panel of the expression builder with:
 * - a tree with functions
 * - a tree with variables
 * - buttons like a calculator with figures and symbols like * / ( )...
 * 
 * @author JM235353
 */
public class ExpressionBuilderPanel extends JPanel {
    
    private final ArrayList<ExpressionEntity> m_builtExpression = new ArrayList<>();
    private final ArrayList<ExpressionEntity> m_redoExpression = new ArrayList<>();

    private HashMap<Integer, JButton> m_shortCuts = new HashMap<>();
    private ExpressionEntity m_entityPlus;
    private ExpressionEntity m_entityMinus;

            
    private final JPanel m_functionsPanel;
    private final JPanel m_calcPanel;
    private GridBagConstraints m_numberPanelC;
    private final JPanel m_variablesListPanel;
    private ExpressionEntityTree m_functionTree;
    private ExpressionEntityTree m_variableTree;
    
    //private JList m_variablesList;
    private JTextField m_expressionTextField;
    private JButton m_redoButton;
    private JButton m_undoButton;
    private JButton m_clearButton;
    
    private int m_nbButtonsHorizontal;
    
    public ExpressionBuilderPanel(int nbButtonsHorizontal) {
        setBorder(BorderFactory.createTitledBorder(" Expression Builder "));
        setLayout(new GridBagLayout());
        
        m_nbButtonsHorizontal = nbButtonsHorizontal;
        
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(2, 2, 2, 2);
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;

        
        JPanel expressionPanel = createExpressionPanel();
        m_functionsPanel = createFunctionPanel();
        m_variablesListPanel = createVariablesPanel();
        m_calcPanel = createNumberPanel();
        

        c.gridx = 0;
        c.gridwidth = 3;
        c.weightx = 1;
        add(expressionPanel, c);
        
        
        c.gridy++;
        c.gridwidth = 1;
        c.weightx = 0;
        c.weighty = 1;
        add(m_functionsPanel, c);

        c.gridx++;
        add(m_variablesListPanel, c);
        
        c.gridx++;
        add(m_calcPanel, c);
     

        setFocusable(true);
        requestFocusInWindow();
     
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getID() == KeyEvent.KEY_TYPED) {
                    int keyChar = e.getKeyChar();
                    JButton button = m_shortCuts.get(keyChar);
                    if (button != null) {
                        button.doClick();
                    } else if (keyChar == 8) {
                        // backspace
                        if (m_undoButton.isEnabled()) {
                            m_undoButton.doClick();
                        }
                    }
                }
                return false;
            }
        });
    }

    
    private JPanel createExpressionPanel() {
        JPanel expressionPanel = new JPanel(new FlowLayout());
        
        expressionPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(2, 2, 2, 2);
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        
        
        JLabel equalLabel = new JLabel("=");
        equalLabel.setIcon(IconManager.getIcon(IconManager.IconType.FUNCTION));
        m_expressionTextField = new JXTextField();
        m_expressionTextField.setEditable(false);
        m_undoButton = createUndoButton();
        m_redoButton = createRedoButton();
        m_clearButton = createClearButton();
        
        expressionPanel.add(equalLabel, c);

        
        c.gridx++;
        c.weightx = 1;
        expressionPanel.add(m_expressionTextField, c);

        c.weightx = 0;
        c.gridx++;
        expressionPanel.add(m_undoButton, c);

        c.gridx++;
        expressionPanel.add(m_redoButton, c);

        c.gridx++;
        expressionPanel.add(m_clearButton, c);
        
        return expressionPanel;
    }
    
    private JButton createUndoButton() {
        JButton undoButton = new JButton(IconManager.getIcon(IconManager.IconType.UNDO));
        undoButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
        undoButton.setFocusPainted(false);

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
        redoButton.setFocusPainted(false);
        
        
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
        clearButton.setFocusPainted(false);
        
        
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                while (!m_builtExpression.isEmpty()) {
                    m_redoExpression.add(m_builtExpression.remove(m_builtExpression.size() - 1));
                }
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
        
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(2, 2, 2, 2);
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        
        
        m_functionTree = new ExpressionEntityTree(this, ExpressionEntityTree.TreeType.FUNCTIONS);
        JScrollPane scrollPane = new JScrollPane(m_functionTree);
        scrollPane.setPreferredSize(new Dimension(180, 220));
        
        functionsPanel.add(scrollPane, c);
        
        return functionsPanel;
    }

    private JPanel createVariablesPanel() {

        JPanel variablesPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(2, 2, 2, 2);
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;

        m_variableTree = new ExpressionEntityTree(this, ExpressionEntityTree.TreeType.VARIABLES);
        JScrollPane scrollPane = new JScrollPane(m_variableTree);
        scrollPane.setPreferredSize(new Dimension(180, 220));

        variablesPanel.add(scrollPane, c);

        return variablesPanel;

    }
    
    
    private JPanel createNumberPanel() {
        JPanel numberPanel = new JPanel(new GridBagLayout());

        m_numberPanelC = new GridBagConstraints();
        m_numberPanelC.gridx = 0;
        m_numberPanelC.gridy = 0;
        m_numberPanelC.insets = new Insets(2, 2, 2, 2);
        m_numberPanelC.fill = GridBagConstraints.BOTH;
        m_numberPanelC.anchor = GridBagConstraints.NORTHWEST;

        // put empty space at the bottom
        m_numberPanelC.gridy = 100;
        m_numberPanelC.weighty = 1;
        numberPanel.add(Box.createVerticalGlue(), m_numberPanelC);

        m_numberPanelC.gridy = 0;
        m_numberPanelC.weighty = 0;
        
        return numberPanel;
    }
    

    
    public void addEntityToExpression(ExpressionEntity entity) {
        m_builtExpression.add(entity);
        m_redoExpression.clear();
        m_expressionTextField.setText(getDisplayExpression());
        updateEnableButtons();
    }
    
    public void addFunction(ExpressionEntity entity) {
        m_functionTree.addEntity(entity);

    }
    
    public void addVariable(ExpressionEntity entity) {
        m_variableTree.addEntity(entity);
    }
    
    public void addCalcButton(ExpressionEntity entity) {
        
        String code = entity.getCode();
        if (code.length() == 1) {
            char c = code.charAt(0);
            if (c == '+') {
                m_entityPlus = entity;
            } else if (c == '-') {
                m_entityMinus = entity;
            }
        }
        
        JButton calcButton = new JButton(entity.getName());
        calcButton.setFocusPainted(false);
        
        if (entity.getName().compareTo("\u00B1") == 0) { // +/- key
            // specific code for +/- key
            calcButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int index = m_builtExpression.size()-1;
                    while (index>=0) {
                        ExpressionEntity entityCur = m_builtExpression.get(index);
                        String code = entityCur.getCode();
                        if (code.length() == 1) {
                            char c = code.charAt(0);
                            if ((c<'0' || c>'9') && (c != '.')) {
                                // non numeric value
                                if (c == '-') {
                                    // replace entity by +
                                    m_builtExpression.set(index, m_entityPlus);
                                } else if (c == '+') {
                                    // replace entity by -
                                    m_builtExpression.set(index, m_entityMinus);
                                } else {
                                    // insert entity -
                                    m_builtExpression.add(index+1, m_entityMinus);
                                }
                                break;
                            }
                        }
                        
                        index--;
                    }
                    if (index<0) {
                        // we arrived at the beginning
                        m_builtExpression.add(0, m_entityMinus);
                    }
                    

                    m_expressionTextField.setText(getDisplayExpression());
                    updateEnableButtons();
                }
            });
        } else {
            calcButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    m_builtExpression.add(entity);
                    m_redoExpression.clear();
                    m_expressionTextField.setText(getDisplayExpression());
                    updateEnableButtons();
                }
            });
        }
        
        m_calcPanel.add(calcButton, m_numberPanelC);
        
        m_numberPanelC.gridx++;
        if (m_numberPanelC.gridx>=m_nbButtonsHorizontal) {
            m_numberPanelC.gridx = 0;
            m_numberPanelC.gridy++;
        }

        String name = entity.getName();
        if (name.length() == 1) {
            int c = name.charAt(0);
            m_shortCuts.put(new Integer(c), calcButton);
        }
    }
    
    public String getCodeExpression() {
        StringBuilder sb = new StringBuilder();
        for (ExpressionEntity entity : m_builtExpression ) {
            sb.append(entity.getCode());
        }
        return sb.toString();
    }
    
    public String getDisplayExpression() {
        StringBuilder sb = new StringBuilder();
        for (ExpressionEntity entity : m_builtExpression) {
            sb.append(entity.getNameDisplayed());
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
