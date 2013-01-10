package fr.proline.studio.gui;

import fr.proline.studio.utils.IconManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.ArrayList;
import javax.swing.*;
import org.openide.util.ImageUtilities;

/**
 * Used to create a panel containing other panels separated by SplitPanes
 * @author JM235353
 */
public class SplittedPanelContainer extends JPanel {

    private ArrayList<SplittedPanel> panelArray = new ArrayList<SplittedPanel>(4);
    private ArrayList<JSplitPane> splitPaneArray = new ArrayList<JSplitPane>(3);

    
    private JPanel mainPanel;
    private IconifiedPanel iconifiedPanel;
    private JPanel containerPanel;
    
    public SplittedPanelContainer() {   
    }

    /**
     * Register a panel which will be put in the container panel
     * @param panel 
     */
    public void registerPanel(JComponent panel) {
        SplittedPanel splittedPanel = new SplittedPanel(panelArray.size(), panel, this);
        panelArray.add(splittedPanel);
    }

    public void resetDefaultSize() {
        int[] heights = getHeights();
        
        int totalHeight = 0;
        int nb = heights.length;
        for (int i=0;i<nb;i++) {
            totalHeight += heights[i];
        }
        int defaultHeight = totalHeight/4;
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
    private void removePanel(int index) {

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
        iconifiedPanel.addPanel(splittedPanel, height);
        
        
        
        revalidate();
        repaint();
        
        //checkCoherence();
    }

    /*public void checkCoherence() {
        if (panelArray.size()!=splitPaneArray.size()+1) {
            if ((panelArray.size()!=0) || (panelArray.size()!=0)) {
                System.out.println("Size error");
                return;
            }
        }
        
        JSplitPane splitPane = null;
        for (int i=0;i<splitPaneArray.size();i++) {
            splitPane = splitPaneArray.get(i);
            if (! splitPane.getTopComponent().equals(panelArray.get(i))) {
                System.out.println("component Error type I");
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
        removePanel(index);
        
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
     * Each panel added by the user is embedded in a SplittedPanel
     * with buttons to maximize, minimize, reduce
     */
    private class SplittedPanel extends javax.swing.JPanel {

        private JComponent embededPanel;
        private SplittedPanelContainer container;
        private int index;

        // buttons
        private JButton maximizeButton;
        private JButton reduceButton;
        private JButton minimizeButton;
        private JPanel buttonPanel;
        
        
        public SplittedPanel(int index, JComponent embededPanel, SplittedPanelContainer container) {
            
            this.embededPanel = embededPanel;
            this.container = container;
            this.index = index;
            setName(embededPanel.getName());
            initComponents();
        }

        public int getIndex() {
            return index;
        }
        
        private void initComponents() {

            setLayout(new GridBagLayout());

            final SplittedPanel _p = this;
            
            // Create Graphical Objects for buttonPanel
            buttonPanel = new JPanel();
            buttonPanel.setLayout(new GridBagLayout());

            ImageIcon maximizeIcon = IconManager.getIcon(IconManager.IconType.MAXIMIZE);
            
            maximizeButton = new JButton(maximizeIcon);
            maximizeButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
            maximizeButton.setBorderPainted(false);
            maximizeButton.setFocusPainted(false);
            maximizeButton.setContentAreaFilled(false);
            maximizeButton.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    maximizeButton.setVisible(false);
                    reduceButton.setVisible(true);
                    minimizeButton.setVisible(false);
                    container.maximize(_p);
                }
            });
            
            ImageIcon minimizeIcon = IconManager.getIcon(IconManager.IconType.MINIMIZE);
            
            reduceButton = new JButton(minimizeIcon);
            reduceButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
            reduceButton.setBorderPainted(false);
            reduceButton.setFocusPainted(false);
            reduceButton.setContentAreaFilled(false);
            reduceButton.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    maximizeButton.setVisible(true);
                    reduceButton.setVisible(false);
                    minimizeButton.setVisible(true);
                    container.reduce(_p);
                }
            });
            reduceButton.setVisible(false);
            
            ImageIcon deleteIcon = IconManager.getIcon(IconManager.IconType.DELETE);
            
            
            minimizeButton = new JButton(deleteIcon);
            minimizeButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
            minimizeButton.setBorderPainted(false);
            minimizeButton.setFocusPainted(false);
            minimizeButton.setContentAreaFilled(false);
            minimizeButton.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    maximizeButton.setVisible(true);
                    reduceButton.setVisible(false);
                    minimizeButton.setVisible(true);

                    container.minimize(_p);
                }
            });
            


            // Put Graphical Objects in buttonPanel
            GridBagConstraints cButton = new GridBagConstraints();
            cButton.gridx = 0;
            cButton.gridy = 0;
            buttonPanel.add(maximizeButton, cButton);
            cButton.gridy++;
            buttonPanel.add(reduceButton, cButton);
            cButton.gridy++;
            buttonPanel.add(minimizeButton, cButton);
            cButton.gridy++;
            cButton.weighty = 1;
            buttonPanel.add(Box.createVerticalGlue(), cButton);


            // put embededPanel in SplittedPanel
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1;
            c.weighty = 1;
            c.fill = GridBagConstraints.BOTH;
            add(embededPanel, c);

            // put button in SplittedPanel
            c.gridx++;
            c.weightx = 0;
            add(buttonPanel, c);

        }

        
    }
    
    /**
     * Toolbar which contains minimized sub-panels
     */
    private class IconifiedPanel extends javax.swing.JPanel implements ActionListener {
        
        //private SplittedPanelContainer splittedPanelContainer;
        
        private ArrayList<JButton> buttons = new ArrayList<JButton>(4);
        private ArrayList<JSeparator> separators = new ArrayList<JSeparator>(4);
        
        private ArrayList<SplittedPanel> splittedPanels = new ArrayList<SplittedPanel>(4);
        private ArrayList<Integer> previousHeights = new ArrayList<Integer>(4);
        
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
    
}
