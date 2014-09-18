package fr.proline.studio.gui;

import fr.proline.studio.utils.IconManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Used to create a panel containing other panels separated by SplitPanes
 * @author JM235353
 */
public class SplittedPanelContainer extends JPanel {

    private ArrayList<SplittedPanel> m_panelArray = new ArrayList<>(4);
    private ArrayList<JSplitPane> m_splitPaneArray = new ArrayList<>(3);

    
    private JPanel m_mainPanel;
    private IconifiedPanel m_iconifiedPanel;
    private JPanel m_containerPanel;
    
    private boolean m_aPanelMaximized = false;
    
    public SplittedPanelContainer() {   
    }

    /**
     * Register a panel which will be put in the container panel
     * @param panel 
     */
    public void registerPanel(JComponent panel) {

        SplittedPanel splittedPanel = new SplittedPanel(m_panelArray.size(), panel, this);
        m_panelArray.add(splittedPanel);
    }

    public void registerAddedPanel(JComponent panel) {

        // prepare the heights of components
        int[] heights = getHeights();
        int totalHeight = 0;
        for (int i = 0; i < heights.length; i++) {
            totalHeight += heights[i];
        }
        int heightOfNewPanel = totalHeight/(heights.length+1);
        int decPanel = heightOfNewPanel/heights.length;

        int[] newHeights = new int[heights.length + 1];
        for (int i = 0; i < heights.length; i++) {
            newHeights[i] = heights[i]-decPanel;
        }
        newHeights[heights.length] = heightOfNewPanel;
        
        
        
        SplittedPanel splittedPanel = new SplittedPanel(m_panelArray.size(), panel, this);
        insertPanel(splittedPanel);
        updateButtons();
        
        dispatchHeight(newHeights);
    }
    
   public void registerAddedPanelAsTab(JComponent panel) {

        SplittedPanel splittedBottomPanel = m_panelArray.get(m_panelArray.size()-1);
        JComponent bottomComponent = splittedBottomPanel.getEmbededPanel();

        ArrayList<Component> components = new ArrayList<>();

        if (bottomComponent instanceof JTabbedPane) {
            JTabbedPane tabbedPane = (JTabbedPane) bottomComponent;
            int nbTabs = tabbedPane.getTabCount();
            for (int i = 0; i < nbTabs; i++) {
                components.add(tabbedPane.getComponentAt(i));
            }
            tabbedPane.removeAll();
        } else {
            components.add(bottomComponent);
        }
        
        components.add(panel);
        
        JTabbedPane tb = new ReactiveTabbedPane();
        tb.setBorder(new EmptyBorder(8,8,8,8));
        String name = "";
        int nbTabs = components.size();
        for (int i=0;i<nbTabs;i++) {
            Component curComp = components.get(i);
            String curName = curComp.getName();
            tb.addTab(curName, curComp);
            if (i == 0) {
                name = curName;
            } else {
                name += " / "+curName;
            }
        }
        tb.setName(name);
        tb.setSelectedIndex(nbTabs-1);
       

        splittedBottomPanel.replaceEmbededPanel(tb);
        
        revalidate();
        repaint();

        updateButtons();
    }
   
   public void registerAddedPanelAsSplitted(JComponent panel) {

        SplittedPanel splittedBottomPanel = m_panelArray.get(m_panelArray.size()-1);
        JComponent bottomComponent = splittedBottomPanel.getEmbededPanel();

        JSplitPane sp = new JSplitPane();
        sp.setLeftComponent(bottomComponent);
        sp.setRightComponent(panel);
        sp.setName(bottomComponent.getName() + " / " + panel.getName());
        

        splittedBottomPanel.replaceEmbededPanel(sp);
        
        sp.setDividerLocation(getWidth()/2);
        
        revalidate();
        repaint();

        updateButtons();
        
    }
    
    public void removeLastPanel() {
        
        SplittedPanel splittedPanel = m_panelArray.get(m_panelArray.size()-1);
        JComponent c = splittedPanel.getEmbededPanel();
        
         if (c instanceof JTabbedPane) {
            JTabbedPane tabbedPane = (JTabbedPane) c;
            int nbTabs = tabbedPane.getComponentCount();
            tabbedPane.removeTabAt(nbTabs-1);
            if (nbTabs == 2) {
                // we must replace the tabbed pas by its first component
                JComponent newEmbeddedComponent = (JComponent)tabbedPane.getComponentAt(0);
                tabbedPane.removeTabAt(0);
                splittedPanel.replaceEmbededPanel(newEmbeddedComponent);
            }
        } else if (c instanceof JSplitPane) {
            JSplitPane splitPane = (JSplitPane) c;
            splittedPanel.replaceEmbededPanel((JComponent)splitPane.getLeftComponent() );
        } else {
            removePanel(m_panelArray.size()-1);
        }
        
        revalidate();
        repaint();

        updateButtons();
    }
    
    public void updateButtons() {
        int size = m_panelArray.size();
        for (int i=0;i<size;i++) {
            SplittedPanel p = m_panelArray.get(i);
            p.updateAssociationButtons();
            
            
            
            p.updateRemoveAddButtons(i==0, i==size-1);
        }
    }
    
    public void splitPanel(JComponent panel, int index) {
        
        // prepare the heights of components
        int[] heights = getHeights();
        int totalHeight = 0;
        for (int i=0;i<heights.length;i++) {
            totalHeight += heights[i];
        }
        double coef = ((double)totalHeight) / ((double)(totalHeight + heights[index-1]));
        
        int[] newHeights = new int[heights.length + 1];
        for (int i=0;i<newHeights.length;i++) {
            if (i<index) {
                newHeights[i] = (int) (((double)heights[i])*coef);
            } else if (i == index) {
                newHeights[i] = newHeights[i-1];
            } else {
                newHeights[i] = (int) (((double)heights[i-1])*coef);
            }
        }
        
        
        
        
        SplittedPanel splittedPanel = new SplittedPanel(index, panel, this);
        for (int i=index;i<m_panelArray.size();i++) {
            m_panelArray.get(i).incIndex();
        }
        m_panelArray.add(index, splittedPanel);
        
        if (index ==0) {
            // in fact never called ! : index>=1 ; JPM.TODO : remove if it does not change
            // panel added at the beginning
            
            JSplitPane splitPane = new JSplitPane();
            splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
            splitPane.setTopComponent(m_panelArray.get(0));
            splitPane.setBottomComponent(m_splitPaneArray.get(0));
            
            m_splitPaneArray.add(0, splitPane);
            
        } else if (index == m_panelArray.size()-1) {
            // panel added at the end
            
            JPanel lastPanel = m_panelArray.get(m_panelArray.size()-2);
            
            if (m_splitPaneArray.size()>0) {
            
                JSplitPane lastSplitPane = m_splitPaneArray.get(m_splitPaneArray.size()-1);
                lastSplitPane.remove(lastPanel);

                JSplitPane splitPane = new JSplitPane();
                splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
                splitPane.setTopComponent(lastPanel);
                splitPane.setBottomComponent(splittedPanel);


                lastSplitPane.setBottomComponent(splitPane);

                m_splitPaneArray.add(splitPane);
            } else {
                JSplitPane splitPane = new JSplitPane();
                splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
                splitPane.setTopComponent(lastPanel);
                splitPane.setBottomComponent(splittedPanel);
            
                m_splitPaneArray.add(splitPane);
                
                m_containerPanel.removeAll();
                m_containerPanel.add(splitPane);
            }
            
        } else {
            // panel added in the middle
            
            JSplitPane splitPaneParent = m_splitPaneArray.get(index-1);
            Component    bottomComponent = splitPaneParent.getBottomComponent();
            splitPaneParent.remove(bottomComponent);
            
            
            JSplitPane splitPane = new JSplitPane();
            splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
            splitPane.setTopComponent(splittedPanel);
            splitPane.setBottomComponent(bottomComponent);
            
            splitPaneParent.setBottomComponent(splitPane);
            
            m_splitPaneArray.add(index, splitPane);
            
        }
        
        updateButtons();
        
        revalidate();
        repaint();
        
        dispatchHeight(newHeights);
        
        //checkCoherence();
    }
    
    public void associatePanelsAsTab(int bottomPanelIndex) {
        if (bottomPanelIndex ==0) {
            // should not happen
            return;
        }
        
        // prepare the heights of components
        int[] heights = getHeights();
        int totalHeight = 0;
        for (int i=0;i<heights.length;i++) {
            totalHeight += heights[i];
        }
        double coef = ((double)totalHeight) / ((double)(totalHeight - heights[bottomPanelIndex]));
        
        int[] newHeights = new int[heights.length - 1];
        for (int i=0;i<heights.length;i++) {
            if (i<bottomPanelIndex) {
                newHeights[i] = (int) (((double)heights[i])*coef);
            } else if (i > bottomPanelIndex) {
                newHeights[i-1] = (int) (((double)heights[i])*coef);
            }
        }
        
        
        
        
        SplittedPanel splittedTopPanel = m_panelArray.get(bottomPanelIndex-1);
        SplittedPanel splittedBottomPanel = m_panelArray.get(bottomPanelIndex);
        
        JComponent topComponent = splittedTopPanel.getEmbededPanel();
        JComponent bottomComponent = splittedBottomPanel.getEmbededPanel();
        
        removePanel(bottomPanelIndex);
        
        ArrayList<Component> components = new ArrayList<>();
        if (topComponent instanceof JTabbedPane) {
            JTabbedPane tabbedPane = (JTabbedPane) topComponent;
            int nbTabs = tabbedPane.getTabCount();
            for (int i=0;i<nbTabs;i++) {
                components.add(tabbedPane.getComponentAt(i));
            }
            tabbedPane.removeAll();
        } else {
            components.add(topComponent);
        }
        
        if (bottomComponent instanceof JTabbedPane) {
            JTabbedPane tabbedPane = (JTabbedPane) bottomComponent;
            int nbTabs = tabbedPane.getTabCount();
            for (int i = 0; i < nbTabs; i++) {
                components.add(tabbedPane.getComponentAt(i));
            }
            tabbedPane.removeAll();
        } else {
            components.add(bottomComponent);
        }
        
        JTabbedPane tb = new ReactiveTabbedPane();
        tb.setBorder(new EmptyBorder(8,8,8,8));
        String name = "";
        for (int i=0;i<components.size();i++) {
            Component curComp = components.get(i);
            String curName = curComp.getName();
            tb.addTab(curName, curComp);
            if (i == 0) {
                name = curName;
            } else {
                name += " / "+curName;
            }
        }
        tb.setName(name);
       

        splittedTopPanel.replaceEmbededPanel(tb);
        
        revalidate();
        repaint();
        
        dispatchHeight(newHeights);
        
        //checkCoherence();
    }
    
    public void resetDefaultSize() {
        int[] heights = getHeights();
        
        int totalHeight = 0;
        int nb = heights.length;
        for (int i=0;i<nb;i++) {
            totalHeight += heights[i];
        }
        int defaultHeight = totalHeight/nb;
        for (int i=0;i<nb;i++) {
            heights[i] = defaultHeight;
        }
        
        dispatchHeight(heights);
        
    }
    
    /**
     * Must be called when all the sub panels are registered, to finish
     * the creation of the SplittedPanelContainer
     */
    public void createPanel() {

        setLayout(new GridLayout(1, 1));
        
        m_mainPanel = new JPanel();
        m_mainPanel.setLayout(new GridBagLayout());
        add(m_mainPanel);

        m_iconifiedPanel = new IconifiedPanel();
        
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.BOTH;
        m_mainPanel.add(m_iconifiedPanel, c);
        
        m_containerPanel = new JPanel();
        m_containerPanel.setLayout(new GridLayout(1, 1));
        
        c.gridy++;
        c.weighty = 1;
        m_mainPanel.add(m_containerPanel, c);
        
        createPanel(m_containerPanel, 0);
        
        updateButtons();
    }

    private JComponent createPanel(JComponent container, int indexPanel) {

        if (indexPanel == m_panelArray.size() - 1) {
            // there is only one panel left
            container.add(m_panelArray.get(indexPanel));
        } else {
            // embed panel in split pane
            JSplitPane splitPane = new javax.swing.JSplitPane();
            splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
            splitPane.setTopComponent(m_panelArray.get(indexPanel));

            m_splitPaneArray.add(splitPane);

            JPanel bottomPanel = new JPanel();
            GridLayout layout = new GridLayout(1, 1);
            bottomPanel.setLayout(layout);

            splitPane.setBottomComponent(createPanel(bottomPanel, indexPanel + 1));

            container.add(splitPane);
        }

        return container;
    }

    /**
     * Insert a panel which has been previously minimized
     * @param splittedPanel 
     */
    private void insertPanel(SplittedPanel splittedPanel) {
        
        int insertIndex = 0;
        
        int size = m_panelArray.size();
        
        if (size == 0) {
            // First Panel to Be added : put it in the containerPanel
            m_containerPanel.add(splittedPanel);
        } else {
            // look where the panel must be added
            int indexAtCreation = splittedPanel.getIndex();
            
            for (insertIndex = 0; insertIndex < size; insertIndex++) {
                if (indexAtCreation < m_panelArray.get(insertIndex).getIndex()) {
                    break;
                }
            }
            
            if (insertIndex == 0) {
                // insertion at the begining, directly in containerPanel
                Component componentToKeep = m_containerPanel.getComponent(0);
                m_containerPanel.remove(0);
                
                // create a new SplitPane
                JSplitPane splitPane = new javax.swing.JSplitPane();
                splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
                splitPane.setTopComponent(splittedPanel);
                splitPane.setBottomComponent(componentToKeep);
                
                // add splitpane to its new parent
                m_containerPanel.add(splitPane);
                
                // insert split pane to the list
                m_splitPaneArray.add(0, splitPane);
                
            } else if (insertIndex == size) {
                // insertion at the end
                
                if (size == 1) {
                    // only one component is present
                    Component componentToKeep = m_containerPanel.getComponent(0);
                    m_containerPanel.remove(0);
                    
                    // create a new SplitPane
                    JSplitPane splitPane = new javax.swing.JSplitPane();
                    splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
                    splitPane.setTopComponent(componentToKeep);
                    splitPane.setBottomComponent(splittedPanel);
                    
                    // add splitpane to its new parent
                    m_containerPanel.add(splitPane);
                    
                    // insert split pane to the list
                    m_splitPaneArray.add(0, splitPane);
                    
                } else {
                    // at least two components, so at least one splitpane
                    JSplitPane splitPaneParent = m_splitPaneArray.get(insertIndex-2);
                    Component componentToKeep = splitPaneParent.getBottomComponent();
                    splitPaneParent.setBottomComponent(null);

                    JSplitPane splitPane = new javax.swing.JSplitPane();
                    splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
                    splitPane.setTopComponent(componentToKeep);
                    splitPane.setBottomComponent(splittedPanel);

                    splitPaneParent.setBottomComponent(splitPane);

                    // insert split pane to the list
                    m_splitPaneArray.add(insertIndex-1, splitPane);
                }
                
                
            } else {
                // insertion in the middle
                JSplitPane splitPaneParent = m_splitPaneArray.get(insertIndex-1);
                Component componentToKeep = splitPaneParent.getBottomComponent();
                splitPaneParent.setBottomComponent(null);

                JSplitPane splitPane = new javax.swing.JSplitPane();
                splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
                splitPane.setTopComponent(splittedPanel);
                splitPane.setBottomComponent(componentToKeep);

                splitPaneParent.setBottomComponent(splitPane);

                // insert split pane to the list
                m_splitPaneArray.add(insertIndex, splitPane);
            }
            
            
        }
        
        // insert panel to the list
        m_panelArray.add(insertIndex, splittedPanel);
        
        
        revalidate();
        repaint();
        
        //checkCoherence();


    }
    
    /**
     * Remove a Panel when it is minimized
     * @param index 
     */
    private void removePanel(int index) {


        if (index<m_splitPaneArray.size()) {
            JSplitPane splitPane = m_splitPaneArray.get(index);
            Component componentKept = splitPane.getBottomComponent();
            splitPane.setTopComponent(null);
            splitPane.setBottomComponent(null);
            
            if (index>0) {
                JSplitPane parentSplitPane = m_splitPaneArray.get(index-1);
                parentSplitPane.setBottomComponent(componentKept);
            } else {
                m_containerPanel.remove(0);
                m_containerPanel.add(componentKept);
            }
            
            m_splitPaneArray.remove(index);
            
            
        } else {
            
            if (index>0) {
                JSplitPane splitPane = m_splitPaneArray.get(index-1);

                Component componentKept = splitPane.getTopComponent();
                splitPane.setTopComponent(null);
                splitPane.setBottomComponent(null);

                if (index-2>=0) {
                    JSplitPane parentSplitPane = m_splitPaneArray.get(index - 2);
                    parentSplitPane.setBottomComponent(componentKept);
                } else {
                    m_containerPanel.remove(0);
                    m_containerPanel.add(componentKept);
                }
            
                m_splitPaneArray.remove(index-1);
            } else {
                m_containerPanel.remove(0);
            }           
        }
        

        m_panelArray.remove(index);
        for (int i=index;i<m_panelArray.size();i++) {
            m_panelArray.get(i).decIndex();
        }

        
        revalidate();
        repaint();
        
        //checkCoherence();
    }

    /*public void checkCoherence() {
        if (panelArray.size()!=splitPaneArray.size()+1) {
            if (panelArray.size()!=0) {
                System.out.println("Size error");
                return;
            }
        }
        
        JSplitPane splitPane;
        for (int i=0;i<splitPaneArray.size();i++) {
            splitPane = splitPaneArray.get(i);
            if (! splitPane.getTopComponent().equals(panelArray.get(i))) {
                System.out.println("component Error type I");
            }
        }
        
        for (int i=0;i<panelArray.size();i++) {
            SplittedPanel p = panelArray.get(i);
            if (p.getIndex() != i) {
                System.out.println("Index ERROR");
            }
        }

    }*/
    

    
    /**
     * Put back a panel which has been minimized in the toolbar
     * @param splittedPanel
     * @param height 
     */
    private void inflate(SplittedPanel splittedPanel, int height) {
        
        // Calculation of height of each panel
        int[] heights = getHeights();
        boolean doHeightCalculation = heights.length>0;
        

        if (doHeightCalculation) {

            int heightToRemove = height / heights.length;

            for (int i = 0; i < heights.length; i++) {
                heights[i] -= heightToRemove;
            }
        }

        // insert panel in container
        insertPanel(splittedPanel);
        
        // End of Calculation of height of each panel
        if (doHeightCalculation) {
            // look where the panel is added
            int indexAtCreation = splittedPanel.getIndex();
            int insertIndex;
            for (insertIndex = 0; insertIndex < m_panelArray.size(); insertIndex++) {
                if (indexAtCreation < m_panelArray.get(insertIndex).getIndex()) {
                    break;
                }
            }

            int[] newHeights = new int[heights.length + 1];
            int newHeightIndex = 0;
            for (int i = 0; i < heights.length; i++) {
                if (i == insertIndex) {
                    newHeights[newHeightIndex++] = height;
                } else {

                    newHeights[newHeightIndex++] = heights[i];
                }
            }

            dispatchHeight(newHeights);

        }
    }
    
    /**
     * Maximize a sub panel, it will be displayed alone in the container
     * @param panel 
     */
    private void maximize(SplittedPanel panel) {
        
        m_aPanelMaximized = true;
        
        int index = m_panelArray.indexOf(panel);
        
        // save previous heights
        previousHeights = getHeights();
        
        // remove the component of this panel
        previousMainComponent = getComponent(0);
        remove(0);
        
        // remove splittedPanel from its parent
        SplittedPanel panelToMaximize = m_panelArray.get(index);
        previousPanelParent = panelToMaximize.getParent();
        previousPanelParent.remove(panelToMaximize);
        
        // put splittedPanel as the component of the panel
        add(panelToMaximize);
        
        revalidate();
        repaint();
        
    }
    private Component previousMainComponent = null;
    private Container previousPanelParent = null;
    private int[] previousHeights = null;
    
    /**
     * Reduce the maximized panel to its original place
     * @param panel 
     */
    private void reduce(SplittedPanel panelToReduce) {
        
        m_aPanelMaximized = false;
        
        // remove splittedPanel maximized
        remove(0);
        
        // put back the splitted panel in its previous parent
        previousPanelParent.add(panelToReduce);
        
        // put back the main component
        add(previousMainComponent);
        
        revalidate();
        repaint();
        
        dispatchHeight(previousHeights);
        
    }
    
    /**
     * Return current heights of sub panels
     * @return 
     */
    private int[] getHeights() {
        int size = m_panelArray.size();
        int[] heights = new int[size];
        
        for (int i=0;i<size;i++) {
            heights[i] = m_panelArray.get(i).getHeight();
        }
        
        return heights;
    }
    
    /**
     * Dispatch the new heights
     * The code seems intricate, but as we have JSplitPane in JSplitPane and
     * the fact that setDividerLocation use a event mechanism, we are obliged
     * to do the setDividerLocation() of the different JSplitPane in different
     * successive invokeLater
     * @param heights 
     */
    private void dispatchHeight(final int[] heights) {

        int nbSplits = m_splitPaneArray.size();

        if (nbSplits == 0) {
            return;
        }
        
        final Runnable[] rs = new Runnable[nbSplits];
        
        for (int i = 0; i < nbSplits; i++) {
            final JSplitPane s =  m_splitPaneArray.get(i);
            final int height = heights[i];
            final int _i = i;
            rs[i] = new Runnable() {

                @Override
                public void run() {
                    s.setDividerLocation(height);
                    if (_i+1<rs.length) {
                        // Start resize of the next JSplitPane
                         SwingUtilities.invokeLater(rs[_i+1]);
                    }
                }
            };
        }

        // Start resize of the first JSplitPane
        SwingUtilities.invokeLater(rs[0]);

    }


    
    
    /**
     * Icon Button with minimum margin
     */
    private static class MiniButton extends JButton {
        public MiniButton(Icon icon) {
            super(icon);
            setMargin(new java.awt.Insets(0, 0, 0, 0));
            setBorderPainted(false);
            setFocusPainted(false);
            setContentAreaFilled(false);
        }
    }

    
    /**
     * Each panel added by the user is embedded in a SplittedPanel
     * with buttons to maximize, minimize, reduce
     */
    private class SplittedPanel extends JPanel {

        private JComponent m_embededPanel;
        private SplittedPanelContainer m_container;
        private int m_index;
        

        // buttons
        private JButton m_maximizeButton;
        private JButton m_reduceButton;
        private JButton m_associateButton;
        private JButton m_dissociateButton;
        private JButton m_removeButton;
        private JButton m_addButton;
        
        
        private JPanel m_rightButtonPanel;
        
        
        public SplittedPanel(int index, JComponent embededPanel, SplittedPanelContainer container) {
            
            m_embededPanel = embededPanel;
            m_container = container;
            m_index = index;

            setName(embededPanel.getName());
            initComponents();
        }

        public void updateRemoveAddButtons(boolean firstPanel, boolean lastPanel) {


            // remove button is visible for the last panel
            // except if there is only one panel, in this case the remove button
            // is visible only if there is a split pane or a tabbed pane (to remove a sub component)
            if (!lastPanel) {
                m_removeButton.setVisible(false);
            } else {
                if (firstPanel) {
                    if ((m_embededPanel instanceof JTabbedPane) || (m_embededPanel instanceof JSplitPane)) {
                        m_removeButton.setVisible(true);
                    } else {
                        m_removeButton.setVisible(false);
                    }
                } else {
                    m_removeButton.setVisible(true);
                }
            }

            m_addButton.setVisible(lastPanel);


        }
        
        public JComponent getEmbededPanel() {
            return m_embededPanel;
        }
        
        public void replaceEmbededPanel(JComponent embededPanel) {
            if (embededPanel.equals(m_embededPanel)) {
                return;
            }
            removeAll();
            m_embededPanel = embededPanel;
            setName(embededPanel.getName());
            initComponents();
        }
        
        public int getIndex() {
            return m_index;
        }
        
        public void incIndex() {
            m_index++;
        }
        
        public void decIndex() {
            m_index--;
        }
        
        private void initComponents() {

            setLayout(new GridBagLayout());

            final SplittedPanel _p = this;
           
            
            
            // Create Graphical Objects for rightButtonPanel
            m_rightButtonPanel = new JPanel();
            m_rightButtonPanel.setLayout(new GridBagLayout());

            ImageIcon maximizeIcon = IconManager.getIcon(IconManager.IconType.MAXIMIZE);
            
            m_maximizeButton = new MiniButton(maximizeIcon);
            m_maximizeButton.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    m_maximizeButton.setVisible(false);
                    m_reduceButton.setVisible(true);
                    m_container.maximize(_p);
                    
                    updateButtons();
                }
            });
            
            ImageIcon minimizeIcon = IconManager.getIcon(IconManager.IconType.MINIMIZE);
            
            m_reduceButton = new MiniButton(minimizeIcon);
            m_reduceButton.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    m_maximizeButton.setVisible(true);
                    m_reduceButton.setVisible(false);
                    m_container.reduce(_p);
                    
                    updateButtons();
                }
            });
            m_reduceButton.setVisible(false);
           
            
            m_associateButton = new MiniButton(IconManager.getIcon(IconManager.IconType.ASSOCIATE));
            m_associateButton.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    m_container.associatePanelsAsTab(m_index);
                    
                    updateButtons();
                }
            });
            
            
            final SplittedPanel _splittedPanel = this;
            m_dissociateButton = new MiniButton(IconManager.getIcon(IconManager.IconType.DISSOCIATE));
            m_dissociateButton.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    
                    JTabbedPane tabbedPane = (JTabbedPane) m_embededPanel;
                    int nbTabs = tabbedPane.getTabCount();
                    
                    JComponent lastComponent = (JComponent) tabbedPane.getComponentAt(nbTabs-1);
                    tabbedPane.remove(nbTabs-1);
                    
                    Component firstComponent;
                    if (nbTabs == 2) {
                        // one tab left
                        firstComponent = tabbedPane.getComponentAt(0);
                    } else {
                        firstComponent = tabbedPane;
                    }
                    
                    _splittedPanel.replaceEmbededPanel((JComponent) firstComponent);
                    
                    _splittedPanel.repaint();
                    
                    m_container.splitPanel(lastComponent, m_index+1);
                    
                    updateButtons();
                }
            });


            m_removeButton = new MiniButton(IconManager.getIcon(IconManager.IconType.MINUS11));
            m_removeButton.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    UserActions userAction = lookForUserActions(m_embededPanel);
                    if (userAction != null) {
                        userAction.getRemoveAction(m_container).actionPerformed(e);
                    }
                }
            });

            m_addButton = new MiniButton(IconManager.getIcon(IconManager.IconType.PLUS11));
            m_addButton.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    UserActions userAction = lookForUserActions(m_embededPanel);
                    if (userAction != null) {
                        userAction.getAddAction(m_container).actionPerformed(e);  
                    }
                }
            });

            GridBagConstraints cButton = new GridBagConstraints();

            // Put Graphical Objects in rightButtonPanel
            cButton.gridx = 0;
            cButton.gridy = 0;
            cButton.weighty = 0;
            m_rightButtonPanel.add(m_maximizeButton, cButton);
            cButton.gridy++;
            m_rightButtonPanel.add(m_reduceButton, cButton);
            cButton.gridy++;
            m_rightButtonPanel.add(m_associateButton, cButton);
            cButton.gridy++;
            m_rightButtonPanel.add(m_dissociateButton, cButton);
            cButton.weighty = 1;
            cButton.gridy++;
            m_rightButtonPanel.add(Box.createVerticalBox(), cButton);
            
            cButton.weighty = 0;
            

            cButton.gridy++;
            m_rightButtonPanel.add(m_removeButton, cButton);

            cButton.gridy++;
            m_rightButtonPanel.add(m_addButton, cButton);
            
            

            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1;
            c.weighty = 1;
            c.fill = GridBagConstraints.BOTH;

           
            add(m_embededPanel, c);

            // put button in SplittedPanel
            c.gridx++;
            c.weightx = 0;
            add(m_rightButtonPanel, c);

        }

        public void updateAssociationButtons() {
            
            // if a panel has been maximized,
            // or if a panel has been iconified
            // we don't allow association button
            if (!m_iconifiedPanel.isEmpty() || m_aPanelMaximized) {
                m_associateButton.setEnabled(false);
                m_dissociateButton.setEnabled(false);
                return;
            }
            
            if (m_embededPanel instanceof JTabbedPane) {
                m_associateButton.setEnabled(m_index>0);
                m_dissociateButton.setEnabled(true);
            } else {
                m_associateButton.setEnabled(m_index>0);
                m_dissociateButton.setEnabled(false);
            }
        }
        
    }
    
    private UserActions lookForUserActions(Component c) {
        if (c instanceof UserActions) {
            return ((UserActions) c);
        } else if (c instanceof JTabbedPane) {
            JTabbedPane tabbedPane = (JTabbedPane) c;
            return lookForUserActions(tabbedPane.getComponentAt(tabbedPane.getTabCount() - 1));

        } else if (c instanceof JSplitPane) {
            JSplitPane splitPane = (JSplitPane) c;
            return lookForUserActions(splitPane.getRightComponent());
        }
        return null;
    }
    
    /**
     * Toolbar which contains minimized sub-panels
     */
    private class IconifiedPanel extends javax.swing.JPanel implements ActionListener {
        
        //private SplittedPanelContainer splittedPanelContainer;
        
        private ArrayList<JButton> buttons = new ArrayList<>(4);
        private ArrayList<JSeparator> separators = new ArrayList<>(4);
        
        private ArrayList<SplittedPanel> splittedPanels = new ArrayList<>(4);
        private ArrayList<Integer> previousHeights = new ArrayList<>(4);
        
        private MouseAdapter mouseListenerForButtons;
        
        
        
        public IconifiedPanel() {

            
            setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
            
            mouseListenerForButtons = new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    ((JButton)e.getSource()).setContentAreaFilled(true);
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    ((JButton)e.getSource()).setContentAreaFilled(false);
                }
            };
            
        }
        
        public boolean isEmpty() {
            return splittedPanels.isEmpty();
        }
        
        public void addPanel(SplittedPanel panel, int height) {
            String name = panel.getName();
            int index = panel.getIndex();
            
            int size = splittedPanels.size();
            int i;
            for (i=0;i< size;i++) {
                if (index<splittedPanels.get(i).getIndex()) {
                    break;
                }
            }
            
            
            JButton button = new JButton(name);
            button.setMargin(new Insets(1,1,1,1));
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setContentAreaFilled(false);
            button.addMouseListener(mouseListenerForButtons);

            add(button, i*2);
            buttons.add(i, button);
            
            JSeparator separator = new JSeparator(SwingConstants.VERTICAL) {

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(3,8);
                }
                
            };
            add(separator, i*2+1);
            separators.add(i, separator);
            
            splittedPanels.add(i, panel);
            previousHeights.add(i, height);
            
            button.addActionListener(this);
            
            revalidate();
            repaint();
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            JButton b = (JButton) e.getSource();
            
            int index = buttons.indexOf(b);
            removePanel(index);
           
            updateButtons();
        }
        
        
        private void removePanel(int index) {
            
            // remove objects from arrays
            SplittedPanel splittedPanel = splittedPanels.remove(index);
            
            JButton b = buttons.remove(index);
            JSeparator separator = separators.remove(index);
            
            Integer height = previousHeights.remove(index);
            
            // remove the button and the separator of the graphical
            remove(b);
            remove(separator);
            
            
            inflate(splittedPanel, height);
            
            revalidate();
            repaint();
        }
        

        
        
    }

    
    
    public static interface UserActions {
        public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel);
        
        public ActionListener getAddAction(SplittedPanelContainer splittedPanel);
    }
    
    
    public interface ReactiveTabbedComponent {
        public void setShowed(boolean showed);
    }
    
    public static class ReactiveTabbedPane extends JTabbedPane {

        private int m_lastSelectedIndex = -1;
        
        public ReactiveTabbedPane() {
            addChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) {
                    
                    JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
                    int selectedIndex = tabbedPane.getSelectedIndex();
                    if (selectedIndex == m_lastSelectedIndex) {
                        return;
                    }
                    m_lastSelectedIndex = selectedIndex;
                    
                    int nbTabs = tabbedPane.getTabCount();
                    for (int i=0;i<nbTabs;i++) {
                        Component c = tabbedPane.getComponentAt(i);
                        if (c instanceof ReactiveTabbedComponent) {
                            ((ReactiveTabbedComponent)c).setShowed(i == selectedIndex);
                        }
                    }
                    
                    
                }
            });

        }
        
        @Override
        public void insertTab(String title, Icon icon, Component component, String tip, int index) {
            super.insertTab(title, icon, component, tip, index);
            int selectedIndex = getSelectedIndex();
            int nbTabs = getTabCount();
            for (int i = 0; i < nbTabs; i++) {
                Component c = getComponentAt(i);
                if (c instanceof ReactiveTabbedComponent) {
                    ((ReactiveTabbedComponent) c).setShowed(i == selectedIndex);
                }
            }
            m_lastSelectedIndex = selectedIndex;
        }


    }
}
