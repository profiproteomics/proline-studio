package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.PeptideInstance;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.rsmexplorer.gui.dialog.DataBoxChooserDialog;
import fr.proline.studio.utils.IconManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import org.openide.windows.WindowManager;

/**
 *
 * @author JM235353
 */
public class WindowBoxFactory {
    
    public static WindowBox getUserDefinedWindowBox(String name, AbstractDataBox databox, boolean isDecoy) {
        AbstractDataBox[] boxes = new AbstractDataBox[1];
        boxes[0] = databox;

        IconManager.IconType iconType = isDecoy ? IconManager.IconType.RSET_DECOY : IconManager.IconType.RSET;
        WindowBox winBox = new WindowBox(name, generatePanel(boxes), boxes[0], IconManager.getImage(iconType));
        //winBox.resetDefaultSize(); //JPM.WART
        return winBox;
    }
    
    public static WindowBox getPeptidesWindowBox(String name, boolean isDecoy) {
        return getPeptidesForRsetOnlyWindowBox(name, isDecoy);
        // create boxes
       /* AbstractDataBox[] boxes = new AbstractDataBox[5];
        boxes[0] = new DataBoxRsetPeptide();
        boxes[1] = new DataBoxRsetPeptideSpectrum();
        boxes[1].setLayout(AbstractDataBox.DataBoxLayout.TABBED);
        boxes[2] = new DataBoxRsetProteinsForPeptideMatch();
        boxes[3] = new DataBoxProteinSetsCmp();
        boxes[4] = new DataBoxRsetPeptidesOfProteinsCmp();
        boxes[4].setLayout(AbstractDataBox.DataBoxLayout.HORIZONTAL);
        
        return new WindowBox( name, generatePanel(boxes), boxes[0] );*/
    }
    
    public static WindowBox getPeptidesForRsetOnlyWindowBox(String name, boolean isDecoy) {
        
        // create boxes
        AbstractDataBox[] boxes = new AbstractDataBox[3];
        boxes[0] = new DataBoxRsetPeptide();
        boxes[1] = new DataBoxRsetPeptideSpectrum();
        boxes[1].setLayout(AbstractDataBox.DataBoxLayout.TABBED);
        boxes[2] = new DataBoxRsetProteinsForPeptideMatch();
        
        IconManager.IconType iconType = isDecoy ? IconManager.IconType.RSET_DECOY : IconManager.IconType.RSET;
        WindowBox winBox = new WindowBox( name, generatePanel(boxes), boxes[0], IconManager.getImage(iconType) );
        winBox.resetDefaultSize(); //JPM.WART
        return winBox;
        
    }
    
    public static WindowBox getRsmPSMWindowBox(String name, boolean isDecoy) {
        // create boxes
        AbstractDataBox[] boxes = new AbstractDataBox[1];
        boxes[0] = new DataBoxRsmPeptide();

        IconManager.IconType iconType = isDecoy ? IconManager.IconType.RSM_DECOY : IconManager.IconType.RSM;
        return new WindowBox( name, generatePanel(boxes), boxes[0], IconManager.getImage(iconType) );
    }  
    
    public static WindowBox getRsmPeptidesWindowBox(String name, boolean isDecoy) {
        // create boxes
        AbstractDataBox[] boxes = new AbstractDataBox[4];
        boxes[0] = new DataBoxRsmPeptideInstances();
        boxes[1] = new DataBoxRsmProteinSet().init(false);
        boxes[2] = new DataBoxRsmProteinsOfProteinSet();
        boxes[3] = new DataBoxRsmPeptidesOfProtein();
        boxes[3].setLayout(AbstractDataBox.DataBoxLayout.HORIZONTAL);

        IconManager.IconType iconType = isDecoy ? IconManager.IconType.RSM_DECOY : IconManager.IconType.RSM;
        return new WindowBox( name, generatePanel(boxes), boxes[0], IconManager.getImage(iconType));
    }
    
    public static WindowBox getProteinSetsWindowBox(String name, boolean isDecoy) {
        
        // create boxes
        AbstractDataBox[] boxes = new AbstractDataBox[5];
        boxes[0] = new DataBoxRsmProteinSet().init(true);
        boxes[1] = new DataBoxRsmProteinsOfProteinSet();
        boxes[2] = new DataBoxRsmPeptidesOfProtein();
        boxes[3] = new DataBoxRsmProteinAndPeptideSequence();
        boxes[4] = new DataBoxRsetPeptideSpectrum();
        boxes[4].setLayout(AbstractDataBox.DataBoxLayout.TABBED);
        
        
        IconManager.IconType iconType = isDecoy ? IconManager.IconType.RSM_DECOY : IconManager.IconType.RSM;
        
        return new WindowBox( name, generatePanel(boxes), boxes[0], IconManager.getImage(iconType));
    }
    
    public static WindowBox getAllResultSetWindowBox(String name) {
        
        // create boxes
        AbstractDataBox[] boxes = new AbstractDataBox[1];
        boxes[0] = new DataBoxRsetAll();

        WindowBox winBox = new WindowBox(name, generatePanel(boxes), boxes[0], null);

        return winBox;

    }

    public static WindowBox getTaskListWindowBox() {
        AbstractDataBox[] boxes = new AbstractDataBox[2];
        boxes[0] = new DataBoxTaskList();
        boxes[1] = new DataBoxTaskDescription();

        WindowBox winBox = new WindowBox("Tasks Log", generatePanel(boxes), boxes[0], null);

        return winBox;
    }

    private static SplittedPanelContainer generatePanel(AbstractDataBox[] boxes) {

        // link boxes together
        int nb = boxes.length - 1;
        for (int i = 0; i < nb; i++) {
            boxes[i].setNextDataBox(boxes[i + 1]);
        }

        // create panels for each Box
        nb = boxes.length;
        for (int i = 0; i < nb; i++) {
            boxes[i].createPanel();
        }

        // create container panel for TABBED AND HORIZONTAL Boxes
        int nbContainerPanels = 0;
        for (int i = 0; i < nb; i++) {
            if (boxes[i].getLayout() == AbstractDataBox.DataBoxLayout.VERTICAL) {
                nbContainerPanels++;
            }
        }

        JComponent[] panels = new JComponent[nbContainerPanels];
        int panelIdx = 0;
        AbstractDataBox.DataBoxLayout prevLayout = AbstractDataBox.DataBoxLayout.VERTICAL;
        for (int i = 0; i < nb; i++) {
            AbstractDataBox.DataBoxLayout layout = boxes[i].getLayout();
            if (layout == AbstractDataBox.DataBoxLayout.VERTICAL) {
                panels[panelIdx++] = (JPanel) boxes[i].getPanel();
            } else if (layout == AbstractDataBox.DataBoxLayout.HORIZONTAL) {
                JSplitPane sp = new JSplitPane();
                JComponent leftComponent = panels[--panelIdx];
                sp.setLeftComponent(leftComponent);
                JComponent rightComponent = (JComponent) boxes[i].getPanel();
                sp.setRightComponent(rightComponent);
                sp.setName(leftComponent.getName() + " / " + rightComponent.getName());

                sp.setDividerLocation(350); //JPM.TODO
                panels[panelIdx++] = sp;
            } else if (layout == AbstractDataBox.DataBoxLayout.TABBED) {
                if (prevLayout == AbstractDataBox.DataBoxLayout.TABBED) {
                    JTabbedPane tb = (JTabbedPane) panels[panelIdx - 1];
                    tb.addTab(boxes[i].getName(), (JPanel) boxes[i].getPanel());
                    tb.setName(tb.getName() + " / " + boxes[i].getName());
                } else {
                    JTabbedPane tb = new JTabbedPane();
                    tb.setBorder(new EmptyBorder(8, 8, 8, 8));
                    tb.addTab(boxes[i - 1].getName(), panels[--panelIdx]);
                    tb.addTab(boxes[i].getName(), (JPanel) boxes[i].getPanel());
                    tb.setName(boxes[i - 1].getName() + " / " + boxes[i].getName());
                    panels[panelIdx++] = tb;

                }
            }
            prevLayout = layout;
        }


        SplittedPanelContainer splittedPanel = new SplittedPanelContainer();

        final AbstractDataBox lastDatabox = boxes[boxes.length - 1];

        ArrayList<SplittedPanelContainer.UserDefinedButton> userButtonList = new ArrayList<>();
        
        NextDataBoxActionListener addAction = new NextDataBoxActionListener(splittedPanel, lastDatabox);
        SplittedPanelContainer.UserDefinedButton addBoxButton = new SplittedPanelContainer.UserDefinedButton(IconManager.getIcon(IconManager.IconType.PLUS11), addAction);
        userButtonList.add(addBoxButton);

        int previousBoxIndex = boxes.length - 2;
        if (previousBoxIndex >= 0) {
            RemoveDataBoxActionListener removeAction = new RemoveDataBoxActionListener(splittedPanel, boxes[previousBoxIndex]);
            SplittedPanelContainer.UserDefinedButton removeBoxButton = new SplittedPanelContainer.UserDefinedButton(IconManager.getIcon(IconManager.IconType.MINUS11), removeAction);
            userButtonList.add(removeBoxButton);
        }

        for (int i = 0; i < nbContainerPanels; i++) {
            splittedPanel.registerPanel(panels[i], (i == nbContainerPanels - 1) ? userButtonList : null);
        }
        splittedPanel.createPanel();

        return splittedPanel;
    }
    
    public static class RemoveDataBoxActionListener implements ActionListener {

        SplittedPanelContainer m_splittedPanel;
        AbstractDataBox m_previousDatabox;

        public RemoveDataBoxActionListener(SplittedPanelContainer splittedPanel, AbstractDataBox previousDatabox) {
            m_splittedPanel = splittedPanel;
            m_previousDatabox = previousDatabox;
        }

        @Override
        public void actionPerformed(ActionEvent event) {

            m_previousDatabox.setNextDataBox(null);
            m_splittedPanel.removeLastPanel();


        }
    }

    public static class NextDataBoxActionListener implements ActionListener {

        SplittedPanelContainer m_splittedPanel;
        AbstractDataBox m_previousDatabox;

        public NextDataBoxActionListener(SplittedPanelContainer splittedPanel, AbstractDataBox previousDatabox) {
            m_splittedPanel = splittedPanel;
            m_previousDatabox = previousDatabox;
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            DataBoxChooserDialog dialog = new DataBoxChooserDialog(WindowManager.getDefault().getMainWindow(), m_previousDatabox);
            dialog.setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
            dialog.setVisible(true);
            if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                AbstractDataBox genericDatabox = dialog.getSelectedDataBox();
                try {
                    AbstractDataBox newGenericDatabox = (AbstractDataBox) genericDatabox.getClass().newInstance(); // copy the databox
                    if (newGenericDatabox instanceof DataBoxRsmProteinSet) {
                        ((DataBoxRsmProteinSet) newGenericDatabox).init(((DataBoxRsmProteinSet)genericDatabox).getForRSM());
                    }
                    genericDatabox = newGenericDatabox;
                } catch (InstantiationException | IllegalAccessException e) {
                    // should never happen
                }
                
                m_previousDatabox.setNextDataBox(genericDatabox);

                
                RemoveDataBoxActionListener removeAction = new RemoveDataBoxActionListener(m_splittedPanel, m_previousDatabox);
                NextDataBoxActionListener addAction = new NextDataBoxActionListener(m_splittedPanel, genericDatabox);
                SplittedPanelContainer.UserDefinedButton removeBoxButton = new SplittedPanelContainer.UserDefinedButton(IconManager.getIcon(IconManager.IconType.MINUS11), removeAction);
                SplittedPanelContainer.UserDefinedButton addBoxButton = new SplittedPanelContainer.UserDefinedButton(IconManager.getIcon(IconManager.IconType.PLUS11), addAction);
                ArrayList<SplittedPanelContainer.UserDefinedButton> userButtonList = new ArrayList<>();
                userButtonList.add(addBoxButton);
                userButtonList.add(removeBoxButton);

                genericDatabox.createPanel();
                
                
                
                m_splittedPanel.registerAddedPanel((JPanel) genericDatabox.getPanel(), userButtonList);

                // update display of added databox
                final AbstractDataBox _genericDatabox = genericDatabox;
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        if (_genericDatabox instanceof DataBoxRsmProteinSet) {
                            //JPM.WART : DataBoxRsmProteinSet use two possible data as in parameter
                            _genericDatabox.dataChanged(PeptideInstance.class);
                        } else {
                            // for other databox, no need to indicate the main in parameter
                            _genericDatabox.dataChanged(null);
                        }
                    }
                    
                });

            }

        }
    }

}
