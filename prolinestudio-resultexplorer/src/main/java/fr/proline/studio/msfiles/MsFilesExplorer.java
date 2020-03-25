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
package fr.proline.studio.msfiles;

import fr.proline.studio.dpm.serverfilesystem.ServerFileSystemView;
import fr.proline.studio.rsmexplorer.MzdbFilesTopComponent;
import fr.proline.studio.rsmexplorer.gui.LocalFileSystemTransferHandler;
import fr.proline.studio.rsmexplorer.gui.LocalFileSystemView;
import fr.proline.studio.rsmexplorer.gui.TreeFileChooserPanel;
import fr.proline.studio.rsmexplorer.gui.TreeFileChooserTransferHandler;
import fr.proline.studio.utils.IconManager;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

/**
 *
 * @author AK249877
 */
public class MsFilesExplorer extends JPanel {

    private final TreeFileChooserPanel m_tree;
    private final TreeFileChooserTransferHandler m_transferHandler;
    private final LocalFileSystemView m_localFileSystemView;

    public MsFilesExplorer() {
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;

        m_localFileSystemView = new LocalFileSystemView(new LocalFileSystemTransferHandler(), true);
        add(m_localFileSystemView, c);

        TransferFileButtonsPanel transferPanel = new TransferFileButtonsPanel();
        c.gridy++;
        c.weighty = 0;
        add(transferPanel, c);
        
        

        m_transferHandler = new TreeFileChooserTransferHandler();
        m_transferHandler.addComponent(m_localFileSystemView);

        m_tree = new TreeFileChooserPanel(ServerFileSystemView.getServerFileSystemView(), m_transferHandler, true);
        JScrollPane treeScrollPane = new JScrollPane();
        treeScrollPane.setViewportView(m_tree);
        treeScrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Proline Server File System"), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        c.gridy++;
        c.weighty = 1;
        add(treeScrollPane, c);
        
        m_tree.setFileSelectionListener(transferPanel);
        m_localFileSystemView.setFileSelectionListener(transferPanel);
    }

    public TreeFileChooserPanel getTreeFileChooserPanel() {
        return m_tree;
    }

    public LocalFileSystemView getLocalFileSystemView() {
        return m_localFileSystemView;
    }
    
    public interface FileSelectionInterface {
        void upSelectionChanged(ArrayList<FileToTransfer> files, ArrayList<FileToTransfer> directories);
        void downSelectionChanged(ArrayList<FileToTransfer> files, ArrayList<FileToTransfer> directories);
    }
    
    public class TransferFileButtonsPanel extends JPanel implements FileSelectionInterface {
        
        private JButton m_downButton;
        private JButton m_upButton;
        private ArrayList<FileToTransfer> m_filesUp = null;
        private ArrayList<FileToTransfer> m_directoriesUp = null;
        private ArrayList<FileToTransfer> m_filesDown = null;
        private ArrayList<FileToTransfer> m_directoriesDown = null;
        
        
        public TransferFileButtonsPanel() {
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.BOTH;
            c.insets = new java.awt.Insets(0, 0, 0, 0);

            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1;

            add(Box.createHorizontalGlue(), c);
            
            m_downButton = new JButton(IconManager.getIcon(IconManager.IconType.ARROW_MOVE_DOWN_BIG));
            m_downButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
            m_downButton.setEnabled(false);
            
            c.gridx++;
            c.weightx = 0;
            add(m_downButton, c);
            
            c.gridx++;
            add(Box.createHorizontalStrut(10));
            
            m_upButton = new JButton(IconManager.getIcon(IconManager.IconType.ARROW_MOVE_UP_BIG));
            m_upButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
            m_upButton.setEnabled(false);
            
            c.gridx++;
            add(m_upButton, c);
            
            c.gridx++;
            c.weightx = 1;
            add(Box.createHorizontalGlue(), c);

            m_downButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    
                }
            });
            
            m_upButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ArrayList<File> fileList = new ArrayList<>(); 
                    for (FileToTransfer f : m_filesDown) {
                        fileList.add(f.getFile());
                    }
                    TreePath path = m_directoriesUp.get(0).getPath();
                    MzdbDownloadBatch downloadBatch = new MzdbDownloadBatch(fileList, path, MzdbFilesTopComponent.getExplorer().getLocalFileSystemView().getSelectedRoot());
                    Thread downloadThread = new Thread(downloadBatch);
                    downloadThread.start();
                }
            });
            
            m_downButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ArrayList<File> fileList = new ArrayList<>(); 
                    for (FileToTransfer f : m_filesUp) {
                        fileList.add(f.getFile());
                    }
                    TreePath path = m_directoriesDown.get(0).getPath();
                    if (!m_transferHandler.treeTransfer(fileList, path)) {
                        JOptionPane.showMessageDialog(m_downButton, "You can not transfer selected file(s) to the selected directory.");
                    }
                }
            });
        }

        @Override
        public void upSelectionChanged(ArrayList<FileToTransfer> files, ArrayList<FileToTransfer> directories) {
            m_filesUp = files;
            m_directoriesUp = directories;
            selectionChanged();
        }
        
        @Override
        public void downSelectionChanged(ArrayList<FileToTransfer> files, ArrayList<FileToTransfer> directories) {
            m_filesDown = files;
            m_directoriesDown = directories;
            selectionChanged();
        }
        
        
        public void selectionChanged() {
            boolean downAllowed;
            boolean upAllowed;        
            if ((m_filesUp == null) || (m_filesDown == null)) {
                downAllowed = false;
                upAllowed = false;
            } else {
                downAllowed = (m_directoriesDown.size()==1) && (m_filesDown.isEmpty()) && (m_directoriesUp.isEmpty()) && (!m_filesUp.isEmpty());
                upAllowed = (m_directoriesUp.size()==1) && (m_filesUp.isEmpty()) && (m_directoriesDown.isEmpty()) && (!m_filesDown.isEmpty());
            }
            
            
            m_downButton.setEnabled(downAllowed);
            m_upButton.setEnabled(upAllowed);
        }
    }
    
    

}
