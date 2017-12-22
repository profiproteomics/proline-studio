/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.msfiles;

import java.io.Serializable;
import javax.swing.tree.TreePath;

/**
 *
 * @author AK249877
 */
public class TransferableEntryWrapper implements Serializable {
    
    private final WorkingSetEntry m_entry;
    private final TreePath m_sourcePath;
    
    public TransferableEntryWrapper(WorkingSetEntry entry, TreePath sourcePath){
        m_entry = entry;
        m_sourcePath = sourcePath;
    }
    
    public WorkingSetEntry getEntry(){
        return m_entry;
    }
    
    public TreePath getSource(){
        return m_sourcePath;
    }
}
