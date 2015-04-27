package fr.proline.studio.rsmexplorer.gui.calc;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;


public class DataTreeTransferable implements Transferable, Serializable {
    
    public final static DataFlavor DATANODE_FLAVOR = new DataFlavor(DataTree.DataNode.class, "Drag and drop Data TreePath list");

    
    private Integer m_transferKey = null;
    
    private static final HashMap<Integer, TransferData> transferMap = new HashMap<>();
    
    private static final DataFlavor[] DATA_FLAVORS = { DATANODE_FLAVOR };
    
    private static final long serialVersionUID = 1L;
    
    public DataTreeTransferable(Integer transferKey) {
        m_transferKey = transferKey;
    }

    public static Integer register(TransferData data) {
        
        Integer transferKey = m_transferIndex;
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

    
    public static class TransferData {

        private DataTree.DataNode[] m_dataNodes = null;
        
        public TransferData() {
            
        }
        
        public void setDataNodes(DataTree.DataNode[] dataNode) {
            m_dataNodes = dataNode;
        }

        public DataTree.DataNode[] getDataNodes() {
            return m_dataNodes;
        }
        
    }
}
