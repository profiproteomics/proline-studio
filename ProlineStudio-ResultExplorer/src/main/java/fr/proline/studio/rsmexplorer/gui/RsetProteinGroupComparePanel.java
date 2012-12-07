package fr.proline.studio.rsmexplorer.gui;

import fr.proline.core.orm.msi.ProteinMatch;
import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.gui.AbstractDialog;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.pattern.DataBoxProteinSetsCmp;
import fr.proline.studio.rsmexplorer.gui.dialog.TreeSelectionDialog;
import fr.proline.studio.rsmexplorer.node.RSMTree;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.*;
import org.openide.util.ImageUtilities;

/**
 *
 * @author JM235353
 */
public class RsetProteinGroupComparePanel extends JPanel implements DataBoxPanelInterface {
    
    public static ImageIcon sameSetIcon = ImageUtilities.loadImageIcon("fr/proline/studio/images/sameset.png", false);
    public static ImageIcon subSetIcon = ImageUtilities.loadImageIcon("fr/proline/studio/images/subset.png", false);
    public static ImageIcon addRemoveIcon = ImageUtilities.loadImageIcon("fr/proline/studio/images/addremove.png", false);

    
    private JPanel internalPanel;
    
    private ProteinSetComparePanel proteinSetComparePanel;
    
    private LegendPanel legendPanel;
    
      /**
     * Creates new form RsetProteinGroupComparePanel
     */
    public RsetProteinGroupComparePanel() {

        
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        internalPanel = new JPanel(new GridBagLayout());
        internalPanel.setBackground(Color.white);
        add(internalPanel, c);
        
        
        GridBagConstraints cInternal = new GridBagConstraints();
        cInternal.insets = new Insets(0, 0, 0, 0);
        cInternal.fill = GridBagConstraints.BOTH;
        cInternal.gridx = 0;
        cInternal.gridy = 0;
        cInternal.weightx = 1;
        cInternal.weighty = 1;
        proteinSetComparePanel = new ProteinSetComparePanel();
        internalPanel.add(proteinSetComparePanel, cInternal);

        cInternal.gridy++;
        cInternal.weighty = 0;
        
        legendPanel = new LegendPanel();
        internalPanel.add(legendPanel, cInternal);
        
    }

    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        proteinSetComparePanel.setDataBox(dataBox);
    }
   
    
    public void setData(ArrayList<ProteinMatch> proteinMatchArray, HashMap<Integer, ArrayList<Integer>> rsmIdMap) {
        proteinSetComparePanel.setData(proteinMatchArray, rsmIdMap);
    }
    
    public ProteinSetComparePanel getComparePanel() {
        return proteinSetComparePanel;
    }
    
    private class LegendPanel extends JPanel {
        
        public LegendPanel() {
            
            //setBackground(Color.white);
            
            setLayout(new GridBagLayout());
            
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(8, 8, 8, 8);
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.BOTH;
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 0;
            c.weighty = 1;
            
            JLabel sameSetLabel = new JLabel("Same Set");
            sameSetLabel.setIcon(sameSetIcon);
            add(sameSetLabel, c);

            c.gridx++;
            add(Box.createHorizontalStrut(6), c);
            
            
            c.gridx++;
            
            JLabel subSetLabel = new JLabel("Sub Set");
            subSetLabel.setIcon(subSetIcon);
            add(subSetLabel, c);
            
            c.gridx++;
            c.weightx = 1;
            add(Box.createHorizontalGlue(), c);
            
            c.gridx++;
            c.weightx = 0;
            c.anchor = GridBagConstraints.NORTHEAST;
            final JButton rsmButton = new JButton("RSM", addRemoveIcon);
            add(rsmButton, c);
            
  
            rsmButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    
                    ResultSet rset = proteinSetComparePanel.getFirstResultSet();
                    if (rset == null) {
                        // no data ready (should not happen)
                        return;
                    }
                    
                    RSMTree tree = RSMTree.getTree().copyResultSetRootSubTree(rset);
                    
                    Window window = SwingUtilities.getWindowAncestor(legendPanel);
                    
                    TreeSelectionDialog treeSelectionDialog = new TreeSelectionDialog(window, tree, "Select Result Summaries");
                    treeSelectionDialog.setLocationRelativeTo(proteinSetComparePanel);
                    treeSelectionDialog.setSelection(proteinSetComparePanel.getResultSummaryList());

                    treeSelectionDialog.setVisible(true);
                    
                    if (treeSelectionDialog.getButtonClicked() == AbstractDialog.BUTTON_OK) {
                        ArrayList<ResultSummary> selectedRsmList = treeSelectionDialog.getSelectedRsmList();
                        
                         DataBoxProteinSetsCmp dataBox = (DataBoxProteinSetsCmp) proteinSetComparePanel.getDataBox();
                         ProteinMatch proteinMatch = (ProteinMatch) dataBox.getData(false, ProteinMatch.class);
                         String proteinMatchName = proteinMatch.getAccession();
                         
                         ArrayList<ProteinMatch> proteinMatchArrayList = new ArrayList<ProteinMatch>();
                         ArrayList<Integer> resultSetIdArrayList = new ArrayList<Integer>();
                         int size = selectedRsmList.size();
                         for (int i=0;i<size;i++) {
                             ResultSummary rsm = selectedRsmList.get(i);
                             Integer resultSetId = rsm.getResultSet().getId();
                             resultSetIdArrayList.add(resultSetId);
                             ProteinMatch pm = proteinSetComparePanel.getProteinMatch(resultSetId);
                             proteinMatchArrayList.add(pm);
                         }
                         

                         dataBox.loadData(proteinMatchArrayList, resultSetIdArrayList, proteinMatchName, selectedRsmList);
                    }
                    
                }
                
            });
            
        }
        
    }
    
    
    
    
}
