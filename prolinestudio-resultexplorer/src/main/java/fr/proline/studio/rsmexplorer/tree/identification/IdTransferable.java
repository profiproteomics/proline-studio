/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.rsmexplorer.tree.identification;

import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.core.orm.msi.ResultSet;
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
public class IdTransferable implements Transferable, Serializable {
    
    public final static DataFlavor RSMNodeList_FLAVOR = new DataFlavor(IdentificationTree.class, "Drag and drop RSM TreePath list");

    
    private Integer m_transferKey = null;
    private long m_projectId = -1;
    
    private static HashMap<Integer, TransferData> transferMap = new HashMap<>();
    
    private static final DataFlavor[] DATA_FLAVORS = { RSMNodeList_FLAVOR };
    
    private static final long serialVersionUID = 1L;
    
    public IdTransferable(Integer transferKey, long projectId) {
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
    
    public long getProjectId() {
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
        
        public void setNodeList(ArrayList<AbstractNode> list) {
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
