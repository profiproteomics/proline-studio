package fr.proline.studio.rsmexplorer.gui.dialog.spectralcount;


import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.gui.WizardPanel;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.tree.TreePath;

/**
 *
 * @author JM235353
 */
public class TreeSelectionPanel extends JPanel {
    
   
    private IdentificationTree m_tree = null;
    private static TreeSelectionPanel m_treeSelectionPanel = null;
    
    public static TreeSelectionPanel getTreeSelectionPanel() {
        return m_treeSelectionPanel;
    }
    
    public static TreeSelectionPanel getTreeSelectionPanel(IdentificationTree tree, String wizardLabel, String wizardHelp) {
        m_treeSelectionPanel =  new TreeSelectionPanel(tree, wizardLabel,wizardHelp );
        return m_treeSelectionPanel;
    }
    
    private TreeSelectionPanel(IdentificationTree tree, String wizardLabel, String wizardHelp) {
        m_tree = tree;

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        add(new WizardPanel(wizardLabel, wizardHelp), c);
        
        c.gridy++;
        c.weighty = 1;
        add(createMainPanel(), c);


 
    }

    private JComponent createMainPanel() {
        JScrollPane scrollPane = new JScrollPane(m_tree);
        return scrollPane;
    }
    
    
    public void setSelection(ArrayList<ResultSummary> rsmArray) {
        m_tree.setSelection(rsmArray);
    }
    
    public ArrayList<DDataset> getSelectedDatasetList() {
        ArrayList<DDataset> returnedList = new ArrayList<>();
        getSelectedData(returnedList, null);
        return returnedList;
    }
    
    public ArrayList<DataSetNode> getSelectedRSMDSNodeList() {
        ArrayList<DataSetNode> returnedList = new ArrayList<>();
        getSelectedData(null, returnedList);
        return returnedList;
    }
    
    public TreePath[] getSelectionPaths() {
        return m_tree.getSelectionPaths();
    }
    
    public IdentificationTree getTree() {
        return m_tree;
    }

    public void getSelectedData(ArrayList<DDataset> selectedDatasetList, ArrayList<DataSetNode> selectedRSMDSNodeList) {
        TreePath[] paths = m_tree.getSelectionPaths();
        int size = paths.length;
        for (int i = 0; i < size; i++) {
            AbstractNode node = (AbstractNode) paths[i].getLastPathComponent();
            if (node.getType() == AbstractNode.NodeTypes.DATA_SET) {
                DataSetNode dataSetNode = (DataSetNode) node;

                if (selectedDatasetList!=null) {
                    selectedDatasetList.add(dataSetNode.getDataset());
                }
                if (selectedRSMDSNodeList!=null) {
                    selectedRSMDSNodeList.add(dataSetNode);
                }
            }
        }
    }

    
}
