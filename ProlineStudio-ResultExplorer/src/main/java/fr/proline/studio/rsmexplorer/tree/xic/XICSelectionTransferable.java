package fr.proline.studio.rsmexplorer.tree.xic;


import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Wrapping of Data which is drag and dropped
 * @author JM235353
 */
public class XICSelectionTransferable implements Transferable, Serializable {
    
    public final static DataFlavor RSMNodeList_FLAVOR = new DataFlavor(SelectionTree.class, "Drag and drop Identification Summary");

    
    private Integer m_transferKey = null;

    
    private static HashMap<Integer, TransferData> transferMap = new HashMap<>();
    
    private static final DataFlavor[] DATA_FLAVORS = { RSMNodeList_FLAVOR };
    
    private static final long serialVersionUID = 1L;
    
    public XICSelectionTransferable(Integer transferKey) {
        m_transferKey = transferKey;
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

     
    
    public static class TransferData {

        private ArrayList<DataSetNode> m_datasetList = null;
        private ArrayList<AbstractNode> m_designList = null;
        
        
        
        public TransferData() {
            
        }
        
        public boolean isDesignData() {
            return (m_designList != null);
        }
        public AbstractNode.NodeTypes getDesignNodeType() {
            if ((m_designList == null) || (m_designList.isEmpty())) {
                return null;
            }
            return m_designList.get(0).getType();
            
        }

        public void setDatasetList(ArrayList<DataSetNode> list) {
            m_datasetList = list;
        }

        public ArrayList<DataSetNode> getDatasetList() {
            return m_datasetList;
        }

        public void setDesignList(ArrayList<AbstractNode> list) {
            m_designList = list;
        }

        public ArrayList<AbstractNode> getDesignList() {
            return m_designList;
        }
        
    }
}
