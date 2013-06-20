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

/**
 * Used to create a panel containing other panels separated by SplitPanes
 * @author JM235353
 */
public class SplittedPanelContainer extends JPanel {

    private ArrayList<SplittedPanel> panelArray = new ArrayList<>(4);
    private ArrayList<JSplitPane> splitPaneArray = new ArrayList<>(3);

    
    private JPanel mainPanel;
    private IconifiedPanel iconifiedPanel;
    private JPanel containerPanel;
    
    private boolean aPanelMaximized = false;
    
    public SplittedPanelContainer() {   
    }

    /**
     * Register a panel which will be put in the container panel
     * @param panel 
     */
    public void registerPanel(JComponent panel, ArrayList<UserDefinedButton> userDefinedButtonList) {
        SplittedPanel splittedPanel = new SplittedPanel(panelArray.size(), panel, this, userDefinedButtonList);
        panelArray.add(splittedPanel);
    }

    public void registerAddedPanel(JComponent panel, ArrayList<UserDefinedButton> userDefinedButtonList) {
        
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
        
        
        
        SplittedPanel splittedPanel = new SplittedPanel(panelArray.size(), panel, this, userDefinedButtonList);
        insertPanel(splittedPanel);
        updateButtons();
        
        dispatchHeight(newHeights);
    }
    
    public void removeLastPanel() {
        removePanel(panelArray.size()-1, false);
        updateButtons();
    }
    
    public void updateButtons() {
        int size = panelArray.size();
        for (int i=0;i<size;i++) {
            SplittedPanel p = panelArray.get(i);
            p.updateAssociationButtons();
            p.updateUserDefinedButton(i==size-1);
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
        
        
        
        
        SplittedPanel splittedPanel = new SplittedPanel(index, panel, this, null);  //JPM.TODO ???
        for (int i=index;i<panelArray.size();i++) {
            panelArray.get(i).incIndex();
        }
        panelArray.add(index, splittedPanel);
        
        if (index ==0) {
            // in fact never called ! : index>=1 ; JPM.TODO : remove if it does not change
            // panel added at the beginning
            
            JSplitPane splitPane = new JSplitPane();
            splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
            splitPane.setTopComponent(panelArray.get(0));
            splitPane.setBottomComponent(splitPaneArray.get(0));
            
            splitPaneArray.add(0, splitPane);
            
        } else if (index == panelArray.size()-1) {
            // panel added at the end
            
            JPanel lastPanel = panelArray.get(panelArray.size()-2);
            
            if (splitPaneArray.size()>0) {
            
                JSplitPane lastSplitPane = splitPaneArray.get(splitPaneArray.size()-1);
                lastSplitPane.remove(lastPanel);

                JSplitPane splitPane = new JSplitPane();
                splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
                splitPane.setTopComponent(lastPanel);
                splitPane.setBottomComponent(splittedPanel);


                lastSplitPane.setBottomComponent(splitPane);

                splitPaneArray.add(splitPane);
            } else {
                JSplitPane splitPane = new JSplitPane();
                splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
                splitPane.setTopComponent(lastPanel);
                splitPane.setBottomComponent(splittedPanel);
            
                splitPaneArray.add(splitPane);
                
                containerPanel.removeAll();
                containerPanel.add(splitPane);
            }
            
        } else {
            // panel added in the middle
            
            JSplitPane splitPaneParent = splitPaneArray.get(index-1);
            Component    bottomComponent = splitPaneParent.getBottomComponent();
            splitPaneParent.remove(bottomComponent);
            
            
            JSplitPane splitPane = new JSplitPane();
            splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
            splitPane.setTopComponent(splittedPanel);
            splitPane.setBottomComponent(bottomComponent);
            
            splitPaneParent.setBottomComponent(splitPane);
            
            splitPaneArray.add(index, splitPane);
            
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
        
        
        
        
        SplittedPanel splittedTopPanel = panelArray.get(bottomPanelIndex-1);
        SplittedPanel splittedBottomPanel = panelArray.get(bottomPanelIndex);
        
        JComponent topComponent = splittedTopPanel.getEmbededPanel();
        JComponent bottomComponent = splittedBottomPanel.getEmbededPanel();
        
        removePanel(bottomPanelIndex, false);
        
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
        
        JTabbedPane tb = new JTabbedPane();
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
        
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        add(mainPanel);

        iconifiedPanel = new IconifiedPanel();
        
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.BOTH;
        mainPanel.add(iconifiedPanel, c);
        
        containerPanel = new JPanel();
        containerPanel.setLayout(new GridLayout(1, 1));
        
        c.gridy++;
        c.weighty = 1;
        mainPanel.add(containerPanel, c);
        
        createPanel(containerPanel, 0);
        
        updateButtons();
    }

    private JComponent createPanel(JComponent container, int indexPanel) {

        if (indexPanel == panelArray.size() - 1) {
            // there is only one panel left
            container.add(panelArray.get(indexPanel));
        } else {
            // embed panel in split pane
            JSplitPane splitPane = new javax.swing.JSplitPane();
            splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
            splitPane.setTopComponent(panelArray.get(indexPanel));

            splitPaneArray.add(splitPane);

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
        
        int size = panelArray.size();
        
        if (size == 0) {
            // First Panel to Be added : put it in the containerPanel
            containerPanel.add(splittedPanel);
        } else {
            // look where the panel must be added
            int indexAtCreation = splittedPanel.getIndex();
            
            for (insertIndex = 0; insertIndex < size; insertIndex++) {
                if (indexAtCreation < panelArray.get(insertIndex).getIndex()) {
                    break;
                }
            }
            
            if (insertIndex == 0) {
                // insertion at the begining, directly in containerPanel
                Component componentToKeep = containerPanel.getComponent(0);
                containerPanel.remove(0);
                
                // create a new SplitPane
                JSplitPane splitPane = new javax.swing.JSplitPane();
                splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
                splitPane.setTopComponent(splittedPanel);
                splitPane.setBottomComponent(componentToKeep);
                
                // add splitpane to its new parent
                containerPanel.add(splitPane);
                
                // insert split pane to the list
                splitPaneArray.add(0, splitPane);
                
            } else if (insertIndex == size) {
                // insertion at the end
                
                if (size == 1) {
                    // only one component is present
                    Component componentToKeep = containerPanel.getComponent(0);
                    containerPanel.remove(0);
                    
                    // create a new SplitPane
                    JSplitPane splitPane = new javax.swing.JSplitPane();
                    splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
                    splitPane.setTopComponent(componentToKeep);
                    splitPane.setBottomComponent(splittedPanel);
                    
                    // add splitpane to its new parent
                    containerPanel.add(splitPane);
                    
                    // insert split pane to the list
                    splitPaneArray.add(0, splitPane);
                    
                } else {
                    // at least two components, so at least one splitpane
                    JSplitPane splitPaneParent = splitPaneArray.get(insertIndex-2);
                    Component componentToKeep = splitPaneParent.getBottomComponent();
                    splitPaneParent.setBottomComponent(null);

                    JSplitPane splitPane = new javax.swing.JSplitPane();
                    splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
                    splitPane.setTopComponent(componentToKeep);
                    splitPane.setBottomComponent(splittedPanel);

                    splitPaneParent.setBottomComponent(splitPane);

                    // insert split pane to the list
                    splitPaneArray.add(insertIndex-1, splitPane);
                }
                
                
            } else {
                // insertion in the middle
                JSplitPane splitPaneParent = splitPaneArray.get(insertIndex-1);
                Component componentToKeep = splitPaneParent.getBottomComponent();
                splitPaneParent.setBottomComponent(null);

                JSplitPane splitPane = new javax.swing.JSplitPane();
                splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
                splitPane.setTopComponent(splittedPanel);
                splitPane.setBottomComponent(componentToKeep);

                splitPaneParent.setBottomComponent(splitPane);

                // insert split pane to the list
                splitPaneArray.add(insertIndex, splitPane);
            }
            
            
        }
        
        // insert panel to the list
        panelArray.add(insertIndex, splittedPanel);
        
        
        revalidate();
        repaint();
        
        //checkCoherence();


    }
    
    /**
     * Remove a Panel when it is minimized
     * @param index 
     */
    private void removePanel(int index, boolean iconifyRemovedPanel) {

        // height of the panel removed
        int height = panelArray.get(index).getHeight();
        
        if (index<splitPaneArray.size()) {
            JSplitPane splitPane = splitPaneArray.get(index);
            Component componentKept = splitPane.getBottomComponent();
            splitPane.setTopComponent(null);
            splitPane.setBottomComponent(null);
            
            if (index>0) {
                JSplitPane parentSplitPane = splitPaneArray.get(index-1);
                parentSplitPane.setBottomComponent(componentKept);
            } else {
                containerPanel.remove(0);
                containerPanel.add(componentKept);
            }
            
            splitPaneArray.remove(index);
            
            
        } else {
            
            if (index>0) {
                JSplitPane splitPane = splitPaneArray.get(index-1);
                //splitPane.setBottomComponent(null);

                Component componentKept = splitPane.getTopComponent();
                splitPane.setTopComponent(null);
                splitPane.setBottomComponent(null);

                if (index-2>=0) {
                    JSplitPane parentSplitPane = splitPaneArray.get(index - 2);
                    parentSplitPane.setBottomComponent(componentKept);
                } else {
                    containerPanel.remove(0);
                    containerPanel.add(componentKept);
                }
            
                splitPaneArray.remove(index-1);
            } else {
                containerPanel.remove(0);
            }           
        }
        

        SplittedPanel splittedPanel = panelArray.remove(index);
        for (int i=index;i<panelArray.size();i++) {
            panelArray.get(i).decIndex();
        }
        
        if (iconifyRemovedPanel) {
            iconifiedPanel.addPanel(splittedPanel, height);
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
     * Minimize a Panel (put it in a toolbar at the top of the container panel)
     * @param panel 
     */
    private void minimize(SplittedPanel panel) {

        // Calculation of height of each panel
        int index = panelArray.indexOf(panel);
        
        boolean calculateNewHeight = (panelArray.size()>2);
        
        int[] newHeights = null;
        if (calculateNewHeight) {
            // calculate new height for each panel
            int[] heights = getHeights();

            int heightToAdd = (heights[index]) / (heights.length - 1);

            newHeights = new int[heights.length - 1];

            int newHeightIndex = 0;
            for (int i = 0; i < heights.length; i++) {
                if (i == index) {
                    continue;
                }

                newHeights[newHeightIndex] = heights[i] + heightToAdd;

                newHeightIndex++;
            }
        }
        
        
        // remove the panel minimized
        removePanel(index, true);
        
        // dispatch new heights
        if (calculateNewHeight) {
            dispatchHeight(newHeights);
        }

    }
    
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
            for (insertIndex = 0; insertIndex < panelArray.size(); insertIndex++) {
                if (indexAtCreation < panelArray.get(insertIndex).getIndex()) {
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
        
        aPanelMaximized = true;
        
        int index = panelArray.indexOf(panel);
        
        // save previous heights
        previousHeights = getHeights();
        
        // remove the component of this panel
        previousMainComponent = getComponent(0);
        remove(0);
        
        // remove splittedPanel from its parent
        SplittedPanel panelToMaximize = panelArray.get(index);
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
        
        aPanelMaximized = false;
        
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
        int size = panelArray.size();
        int[] heights = new int[size];
        
        for (int i=0;i<size;i++) {
            heights[i] = panelArray.get(i).getHeight();
        }
        
        return heights;
    }
    
    /**
     * Dispatch the new heights
     * @param heights 
     */
    private void dispatchHeight(final int[] heights) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                // set new height to split pane
                int nbSplits = splitPaneArray.size();
                for (int i = nbSplits - 1; i >= 0; i--) {
                    splitPaneArray.get(i).setDividerLocation(heights[i]);
                }
            }
        });



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
     * Icon Button with a callback
     */
    /*private class MiniCallbackButton extends JButton {
        
        private ButtonProviderInterface m_buttonProvider = null;
        private int m_index = -1;
        
        public MiniCallbackButton(ButtonProviderInterface buttonProvider, int index) {
            super(buttonProvider.getButtonIcon(index));

            setMargin(new java.awt.Insets(3, 3, 3, 3));
            setFocusPainted(false);
            
            m_buttonProvider = buttonProvider;
            m_index = index;

            setToolTipText(buttonProvider.getTooltip(index));
            
            addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    m_buttonProvider.buttonClicked(m_index);
                }
            });
        }
    }*/
    
    
    
    /**
     * Each panel added by the user is embedded in a SplittedPanel
     * with buttons to maximize, minimize, reduce
     */
    private class SplittedPanel extends JPanel {

        private JComponent m_embededPanel;
        private SplittedPanelContainer m_container;
        private int m_index;
        
        private boolean m_last;

        // buttons
        private JButton m_maximizeButton;
        private JButton m_reduceButton;
        private JButton m_minimizeButton;
        private JButton m_associateButton;
        private JButton m_dissociateButton;
        private  ArrayList<UserDefinedButton> m_userDefinedButtonList = null;
        private JPanel m_rightButtonPanel;
        
        
        public SplittedPanel(int index, JComponent embededPanel, SplittedPanelContainer container, ArrayList<UserDefinedButton> userDefinedButtonList) {
            
            m_embededPanel = embededPanel;
            m_container = container;
            m_index = index;
            m_userDefinedButtonList = userDefinedButtonList;
            setName(embededPanel.getName());
            initComponents();
        }

        public void updateUserDefinedButton(boolean lastPanel) {
            if (m_userDefinedButtonList == null) {
                return;
            }
            
            int nb = m_userDefinedButtonList.size();
            for (int i=0;i<nb;i++) {
                UserDefinedButton b = m_userDefinedButtonList.get(i);
                b.setVisible(b.hasToBeVisible(lastPanel)); 
            }
        }
        
        public JComponent getEmbededPanel() {
            return m_embededPanel;
        }
        
        public void replaceEmbededPanel(JComponent embededPanel) {
            if (embededPanel.equals(this.m_embededPanel)) {
                return;
            }
            removeAll();
            this.m_embededPanel = embededPanel;
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
                    m_minimizeButton.setVisible(false);
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
                    m_minimizeButton.setVisible(true);
                    m_container.reduce(_p);
                    
                    updateButtons();
                }
            });
            m_reduceButton.setVisible(false);
            
            ImageIcon deleteIcon = IconManager.getIcon(IconManager.IconType.DELETE);
            
            
            m_minimizeButton = new MiniButton(deleteIcon);
            m_minimizeButton.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    m_maximizeButton.setVisible(true);
                    m_reduceButton.setVisible(false);
                    m_minimizeButton.setVisible(true);

                    m_container.minimize(_p);
                    
                    updateButtons();
                }
            });
            
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


            GridBagConstraints cButton = new GridBagConstraints();

            // Put Graphical Objects in rightButtonPanel
            cButton.gridx = 0;
            cButton.gridy = 0;
            cButton.weighty = 0;
            m_rightButtonPanel.add(m_maximizeButton, cButton);
            cButton.gridy++;
            m_rightButtonPanel.add(m_reduceButton, cButton);
            cButton.gridy++;
            m_rightButtonPanel.add(m_minimizeButton, cButton);
            cButton.gridy++;
            m_rightButtonPanel.add(m_associateButton, cButton);
            cButton.gridy++;
            m_rightButtonPanel.add(m_dissociateButton, cButton);
            cButton.weighty = 1;
            cButton.gridy++;
            m_rightButtonPanel.add(Box.createVerticalBox(), cButton);
            
            // add user defined buttons
            if (m_userDefinedButtonList != null) {
                cButton.weighty = 0;

                int nb = m_userDefinedButtonList.size();
                for (int i = 0; i < nb; i++) {
                    cButton.gridy++;
                    UserDefinedButton b = m_userDefinedButtonList.get(i);
                    m_rightButtonPanel.add(b, cButton);
                }

            }
            
            


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
            if (!iconifiedPanel.isEmpty() || aPanelMaximized) {
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
    
    public static class UserDefinedButton extends MiniButton {

        public UserDefinedButton(Icon icon, ActionListener actionListener) {
            super(icon);

            addActionListener(actionListener);
            
        }

        public boolean hasToBeVisible(boolean lastPanel) {
            return lastPanel;
        }
        
    }
    
}
