package fr.proline.studio.rsmexplorer.node;

import fr.proline.core.orm.msi.ResultSet;
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

    
    private Integer m_transferKey = null;
    private Integer m_projectId = null;
    
    private static HashMap<Integer, TransferData> transferMap = new HashMap<>();
    
    private static final DataFlavor[] DATA_FLAVORS = { RSMNodeList_FLAVOR };
    
    private static final long serialVersionUID = 1L;
    
    public RSMTransferable(Integer transferKey, Integer projectId) {
        m_transferKey = transferKey;
        m_projectId = projectId;
    }

    public static Integer register(TransferData data) {
        
        Integer transferKey = Integer.valueOf(m_transferIndex);
        m_transferIndex++;
        
        transferMap.put(transferKey, data);
        
        
        return transferKey;
    
    }

 
    
    
    public static TransferData getData(Integer transferKey) {
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
    
    
    public static class TransferData {
        
        private boolean m_isNodeList;
        private ArrayList m_dataList = null;
        
        public TransferData() {
            
        }
        
        public void setResultSetList(ArrayList<ResultSet> list) {
            m_isNodeList = false;
            m_dataList = list;
        }
        
        public void setNodeList(ArrayList<RSMNode> list) {
            m_isNodeList = true;
            m_dataList = list;
        }
        
        public boolean isNodeList() {
            return m_isNodeList;
        }
        
        public ArrayList getDataList() {
            return m_dataList;
        }
        
    }
}
