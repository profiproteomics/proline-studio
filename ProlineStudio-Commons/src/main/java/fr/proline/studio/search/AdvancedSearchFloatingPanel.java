package fr.proline.studio.search;

import fr.proline.studio.filter.Filter;
import fr.proline.studio.utils.IconManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

/**
 * Panel which can be put over another panel and can do a search
 * @author JM235353
 */
public class AdvancedSearchFloatingPanel extends JPanel {
    
    private Filter[] m_filters = null;
    
    private JToggleButton m_srcButton = null;
    private ApplySearchInterface m_search = null;
    
    private JButton m_searchButton;

    private JComboBox m_columnComboBox;
    private final JPanel m_searchOptionPanel = new JPanel(new GridBagLayout());

    
    public AdvancedSearchFloatingPanel(ApplySearchInterface search) {
        this(search, "*", "<html>Search is based on wildcards:<br>  '*' : can replace all characters<br>  '?' : can replace one character<br><br>Use 'FOO*' to search a string starting with FOO. </html>");
    }
    public AdvancedSearchFloatingPanel(ApplySearchInterface search, String defaultSearchValue, String defaultTooltip) {

        m_search = search;
        
        setBorder(BorderFactory.createLineBorder(Color.darkGray, 1, true));
        setOpaque(true);
        setLayout(new FlowLayout());
        
        JButton closeButton = new JButton(IconManager.getIcon(IconManager.IconType.CROSS_SMALL7));
        closeButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        

        closeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                if (m_srcButton != null) {
                    m_srcButton.setSelected(false);
                }
            }
        });

        
        m_columnComboBox = new JComboBox();
        
        m_searchButton = new JButton(IconManager.getIcon(IconManager.IconType.SEARCH_ARROW));
        m_searchButton.setMargin(new Insets(1, 1, 1, 1));
        m_searchButton.setFocusPainted(false);
        
        m_searchButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                
                Filter f = (Filter)m_columnComboBox.getSelectedItem();
                f.setDefined(true);
                boolean hasChanged = f.registerValues();
                m_search.doSearch(f, hasChanged);
                

            }
        });
        
        add(closeButton);
        add(m_columnComboBox);
        add(m_searchOptionPanel);
        add(m_searchButton);

        
        Dimension d = getPreferredSize();
        setBounds(0,0,(int)d.getWidth(),(int)d.getHeight());
        
        
        MouseAdapter dragGestureAdapter = new MouseAdapter() {
            int dX, dY;
            
            @Override
            public void mouseDragged(MouseEvent e) {
                Component panel = e.getComponent();
                
                int newX = e.getLocationOnScreen().x - dX;
                int newY = e.getLocationOnScreen().y - dY;
                
                Component parentComponent = panel.getParent();
                int  parentX = parentComponent.getX();
                if (newX<parentX) {
                    newX = parentX;
                }
                int parentY = parentComponent.getY();
                if (newY<parentY) {
                    newY = parentY;
                }
                int parentWidth = parentComponent.getWidth();
                if (newX+panel.getWidth()>parentWidth-parentX) {
                    newX = parentWidth-parentX-panel.getWidth();
                }
                int parentHeight = parentComponent.getHeight();
                if (newY+panel.getHeight()>parentHeight-parentY) {
                    newY = parentHeight-parentY-panel.getHeight();
                }
                
                
                panel.setLocation(newX, newY);
                
                dX = e.getLocationOnScreen().x - panel.getX();
                dY = e.getLocationOnScreen().y - panel.getY();
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                JPanel panel = (JPanel) e.getComponent();
                panel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                dX = e.getLocationOnScreen().x - panel.getX();
                dY = e.getLocationOnScreen().y - panel.getY();
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                JPanel panel = (JPanel) e.getComponent();
                panel.setCursor(null);
            }
        };
        
        
        addMouseMotionListener(dragGestureAdapter);
        addMouseListener(dragGestureAdapter);
        
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                List<JTextField> list = new ArrayList<>();
                getJTextFields(m_searchOptionPanel, list);
                list.get(0).requestFocusInWindow();
            }
            
        }) ;
        setVisible(false);

    }

    public void setFilers(Filter[] filters) {
        m_filters = filters;

        initPrefilterSelectedPanel();
    }
    
    public boolean hasFilters() {
        return (m_filters!=null);
    }
    
    
    public void initPrefilterSelectedPanel() {
        m_searchOptionPanel.removeAll();
        m_columnComboBox.removeAllItems();

        int nbParameters = m_filters.length;
        for (int i = 0; i < nbParameters; i++) {
            final Filter f = m_filters[i];
            m_columnComboBox.addItem(f);
        }
        
        Filter f =(Filter) m_columnComboBox.getSelectedItem();
        
        modifySearchOptionPanel(f, true);

        
        m_columnComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Filter f = (Filter) m_columnComboBox.getSelectedItem();
                f.setDefined(true);
                modifySearchOptionPanel(f, false);
                
            }
        });
        
    }
    
    private void modifySearchOptionPanel(Filter f, boolean firstTime) {
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.gridy = 0;

        m_searchOptionPanel.removeAll();
        c.gridx = 0;
        f.createComponents(m_searchOptionPanel, c);
        
        // add listeners to text field for return key
        addListener(m_searchOptionPanel);
        
        revalidate();
        Dimension d = getPreferredSize();
        if (firstTime) {
            setBounds(0, 0, (int) d.getWidth(), (int) d.getHeight());
        } else {
            setBounds(getX(), getY(), (int) d.getWidth(), (int) d.getHeight());
        }
    }

    private void addListener(Container container) {
        List<JTextField> textFields = new ArrayList<>();
        getJTextFields(container, textFields);
        for (final JTextField tf : textFields) {
            tf.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    m_searchButton.doClick();
                }
            }
            );
            tf.addFocusListener(new FocusListener() {

                @Override
                public void focusGained(FocusEvent e) {
                    if (!e.isTemporary()) {
                        if ((tf.getText() != null) && tf.getText().endsWith("*")) {
                            tf.select(0, tf.getText().length()-1);
                        } else {
                            tf.selectAll();
                        }
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                }

            });
        }
    }
    
    private void getJTextFields(Container container, List<JTextField> list) {
        for (Component c : container.getComponents()) {
            if (c instanceof JTextField) {
                list.add((JTextField) c);
            } else if (c instanceof Container) {
                getJTextFields((Container) c, list);
            }
        }
    }
    
    public void setToggleButton(JToggleButton srcButton) {
        m_srcButton = srcButton;
    }
    
    public void enableSearch(boolean enable) {
        if (m_srcButton != null) {
            m_searchButton.setEnabled(enable);
        }
    }

    
    
}
