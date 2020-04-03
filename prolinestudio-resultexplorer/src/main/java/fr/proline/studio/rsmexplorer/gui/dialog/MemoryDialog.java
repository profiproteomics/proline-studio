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
package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.dam.memory.TransientMemoryCacheManager;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.gui.admin.FragmentationRuleSetPanel;
import fr.proline.studio.rsmexplorer.gui.admin.PeaklistSoftwarePanel;
import fr.proline.studio.rsmexplorer.gui.admin.ProjectsPanel;
import fr.proline.studio.rsmexplorer.gui.admin.UserAccountsPanel;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Window;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;

/**
 *
 * Visualize unused and used TransientData memory blocks to be able to free them
 * 
 * @author Jean-Philippe
 */
public class MemoryDialog extends DefaultDialog  {

    private static MemoryDialog m_singletonDialog = null;

    private JList<String> m_usedList = null;
    private JList<String> m_freeList = null;
    
    
    
    public static MemoryDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new MemoryDialog(parent);
        } 
        return m_singletonDialog;
    }

    private MemoryDialog(Window parent) {
        super(parent, Dialog.ModalityType.MODELESS);
        setTitle("Memory Management");

        setSize(new Dimension(640, 480));
        setMinimumSize(new Dimension(400, 360));
        setResizable(true);

        //setDocumentationSuffix("h.gktawz9n942e");

        setButtonName(BUTTON_OK, "Free Memory");

        initInternalPanel();

    }
    
    private void update() {
               
        ArrayList<String> freeCacheList = TransientMemoryCacheManager.getSingleton().getFreeCacheList();
        DefaultListModel<String> freeListModel = new DefaultListModel<>();
        for (String cacheName :  freeCacheList) {
            freeListModel.addElement(cacheName);
        }
        m_freeList.setModel(freeListModel);
        

        ArrayList<String> usedCacheList = TransientMemoryCacheManager.getSingleton().getUsedCacheList();
        DefaultListModel<String> usedListModel = new DefaultListModel<>();
        for (String cacheName :  usedCacheList) {
            usedListModel.addElement(cacheName);
        }
        m_usedList.setModel(usedListModel); 
    }

    private void initInternalPanel() {

        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new java.awt.GridBagLayout());

        JTabbedPane tabbedPane = new JTabbedPane(); 
        

        m_freeList = new JList<>();
        JScrollPane freeListScrollPane = new JScrollPane(m_freeList);


        m_usedList = new JList<>();       
        JScrollPane usedListScrollPane = new JScrollPane(m_usedList);

        tabbedPane.add("Recoverable Memory", freeListScrollPane);
        tabbedPane.add("Used Memory", usedListScrollPane);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.weighty = 1.0;
        internalPanel.add(tabbedPane, c);

        setInternalComponent(internalPanel);

    }

    /*@Override
    public void pack() {
        // forbid pack by overloading the method
    }*/

    @Override
    public void setVisible(boolean b) {
        update();
        super.setVisible(b);
    }
    
    @Override
    protected boolean okCalled() {
        
        TransientMemoryCacheManager.getSingleton().freeUnusedCache();
        System.gc(); 
        
        return true;
    }



}
