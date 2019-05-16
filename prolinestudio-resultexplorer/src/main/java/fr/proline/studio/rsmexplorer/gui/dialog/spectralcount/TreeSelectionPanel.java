package fr.proline.studio.rsmexplorer.gui.dialog.spectralcount;


import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import java.awt.BorderLayout;
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
    
    public static TreeSelectionPanel getTreeSelectionPanel(IdentificationTree tree) {
        m_treeSelectionPanel =  new TreeSelectionPanel(tree);
        return m_treeSelectionPanel;
    }
    
    private TreeSelectionPanel(IdentificationTree tree) {
        m_tree = tree;
        setLayout(new BorderLayout());
        add(createMainPanel(), BorderLayout.CENTER);
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
