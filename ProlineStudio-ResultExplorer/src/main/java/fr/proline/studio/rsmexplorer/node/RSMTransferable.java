package fr.proline.studio.rsmexplorer.node;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.tree.TreePath;

/**
 *
 * @author JM235353
 */
public class RSMTransferable implements Transferable, Serializable {
    
    public final static DataFlavor RSMNodeList_FLAVOR = new DataFlavor(RSMTree.class, "Drag and drop RSM TreePath list");
    
    private Integer m_transferKey= null;
    private Integer m_projectId = null;
    
    private static HashMap<Integer,ArrayList<RSMNode>> transferMap = new HashMap<>();
    
    private static final DataFlavor[] DATA_FLAVORS = { RSMNodeList_FLAVOR };
    
    private static final long serialVersionUID = 1L;
    
    public RSMTransferable(Integer transferKey, Integer projectId) {
        m_transferKey = transferKey;
        m_projectId = projectId;
    }

    public static Integer register(ArrayList<RSMNode> nodes) {
        
        Integer transferKey = Integer.valueOf(m_transferIndex);
        m_transferIndex++;
        
        transferMap.put(transferKey, nodes);
        
        
        return transferKey;
    
    }
    public static ArrayList<RSMNode> getNodeList(Integer transferKey) {
        return transferMap.get(transferKey);
    }
    public static void clearRegisteredData() {
        transferMap.clear();
    }
    private static int m_transferIndex = 0;
    
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return DATA_FLAVORS;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return (DATA_FLAVORS[0].equals(flavor));
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (isDataFlavorSupported(flavor)) {
            return this;
        }

        return null;
    }
    
    public Integer getTransferKey() {
        return m_transferKey;
    }
    
    public Integer getProjectId() {
        return m_projectId;
    }
}
