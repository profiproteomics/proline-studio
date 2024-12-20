/* 
 * Copyright (C) 2019
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
package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.EmptyRawFile;
import fr.proline.mzscope.ui.model.RawFileListModel;
import fr.proline.mzscope.model.IRawFile;
import fr.proline.mzscope.utils.IPopupMenuDelegate;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * list which contains the mzdb filenames
 *
 * @author MB243701
 */
public class RawFilesPanel extends JPanel implements ListCellRenderer {

   final private static Logger logger = LoggerFactory.getLogger(RawFilesPanel.class);

   private final RawFileListModel rawFilesListModel;
   private JList<IRawFile> rawFilesList;
   private JPopupMenu popupMenu;
   private JScrollPane scrollPane;
   private JLabel openedRawFilesLabel;
   private final IPopupMenuDelegate popupMenuDelegate;

   private  ListCellRenderer defaultListCellRenderer;
   /**
    * Creates new form RawFilesPanel
    * 
    * @param popupDelegate that will be responsible for popupmenu item creation and popupmenu items update
    */
   public RawFilesPanel(IPopupMenuDelegate popupDelegate) {
      this.popupMenuDelegate = popupDelegate;
      initComponents();
      rawFilesListModel = (RawFileListModel) rawFilesList.getModel();
   }

   private void initComponents() {
      openedRawFilesLabel = new JLabel();
      scrollPane = new JScrollPane();
      rawFilesList = new JList();
      popupMenu = new JPopupMenu();
      popupMenuDelegate.initPopupMenu(popupMenu);
      openedRawFilesLabel.setText("mzDB files");

      rawFilesList.setModel(new RawFileListModel());

      rawFilesList.setComponentPopupMenu(popupMenu);
      rawFilesList.setLayoutOrientation(JList.VERTICAL);
      rawFilesList.addMouseListener(new java.awt.event.MouseAdapter() {
         @Override
         public void mouseClicked(java.awt.event.MouseEvent evt) {
            rawFilesListMouseClicked(evt);
         }

         @Override
         public void mousePressed(java.awt.event.MouseEvent evt) {
            rawFilesListMousePressed(evt);
         }
      });
      defaultListCellRenderer = rawFilesList.getCellRenderer();
      rawFilesList.setCellRenderer(this);
      scrollPane.setViewportView(rawFilesList);

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
      this.setLayout(layout);
      layout.setHorizontalGroup(
              layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(layout.createSequentialGroup()
                      .addGap(5, 5, 5)
                      .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                              .addGroup(layout.createSequentialGroup()
                                      .addComponent(scrollPane)
                                      .addGap(5, 5, 5))
                              .addGroup(layout.createSequentialGroup()
                                      .addComponent(openedRawFilesLabel)
                                      .addContainerGap(156, Short.MAX_VALUE))))
      );
      layout.setVerticalGroup(
              layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(layout.createSequentialGroup()
                      .addGap(5, 5, 5)
                      .addComponent(openedRawFilesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                      .addGap(5, 5, 5)
                      .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE)
                      .addGap(5, 5, 5))
      );

      openedRawFilesLabel.getAccessibleContext().setAccessibleName("Raw files");
   }

   private void rawFilesListMouseClicked(MouseEvent evt) {
      if (evt.getClickCount() == 2) {
         if (rawFilesList.getSelectedIndex() > -1) {
            popupMenuDelegate.getDefaultAction().actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "default"));
         }
      }
   }

   private void rawFilesListMousePressed(MouseEvent evt) {
      popupMenuDelegate.updatePopupMenu();
      if (rawFilesList.getSelectedIndex() > -1 && SwingUtilities.isRightMouseButton(evt)) {
         popupMenu.show((JComponent) evt.getSource(), evt.getX(), evt.getY());
      }
   }

   public JPopupMenu getPopupMenu() {
      return popupMenu;
   }

   public List<IRawFile> getSelectedValues() {
      return rawFilesList.getSelectedValuesList();
   }

   public void addFile(IRawFile file) {
      rawFilesListModel.add(file);
   }

   public void removeFile(IRawFile file) {
      rawFilesListModel.removeFile(file);
   }

   public void removeAllFiles() {
      rawFilesListModel.removeAllFiles();
   }

   @Override
   public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

      Component c = defaultListCellRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      if(value instanceof EmptyRawFile )
         c.setEnabled(false);
      return c;
   }
}
